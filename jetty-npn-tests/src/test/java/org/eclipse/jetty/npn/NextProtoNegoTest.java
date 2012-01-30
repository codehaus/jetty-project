package org.eclipse.jetty.npn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class NextProtoNegoTest
{
    // TODO: while handshake seems working, application data does not.
    @Ignore
    @Test
    public void testSSLSocket() throws Exception
    {
        SSLContext context = SSLSupport.newSSLContext();

        final CountDownLatch handshakeLatch = new CountDownLatch(2);

        final String data = "data";
        final String protocolName = "test";
        final CountDownLatch latch = new CountDownLatch(4);
        final SSLServerSocket server = (SSLServerSocket)context.getServerSocketFactory().createServerSocket(0);
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    SSLSocket socket = (SSLSocket)server.accept();
                    socket.setUseClientMode(false);
                    NextProtoNego.put(socket, new NextProtoNego.ServerProvider()
                    {
                        @Override
                        public List<String> protocols()
                        {
                            latch.countDown();
                            return Arrays.asList(protocolName);
                        }

                        @Override
                        public void protocolSelected(String protocol)
                        {
                            Assert.assertEquals(protocolName, protocol);
                            latch.countDown();
                        }
                    });
                    socket.addHandshakeCompletedListener(new HandshakeCompletedListener()
                    {
                        @Override
                        public void handshakeCompleted(HandshakeCompletedEvent event)
                        {
                            handshakeLatch.countDown();
                        }
                    });
                    socket.startHandshake();

                    socket.setSoTimeout(1000);
                    InputStream serverInput = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(serverInput, "UTF-8"));
                    String line = reader.readLine();
                    Assert.assertEquals(data, line);

                    OutputStream serverOutput = socket.getOutputStream();
                    serverOutput.write(data.getBytes("UTF-8"));
                    serverOutput.flush();

                    socket.close();
                }
                catch (IOException x)
                {
                    x.printStackTrace();
                }
            }
        }.start();

        SSLSocket client = (SSLSocket)context.getSocketFactory().createSocket("localhost", server.getLocalPort());
        client.setUseClientMode(true);
        NextProtoNego.put(client, new NextProtoNego.ClientProvider()
        {
            @Override
            public boolean supports()
            {
                latch.countDown();
                return true;
            }

            @Override
            public String selectProtocol(List<String> protocols)
            {
                Assert.assertEquals(1, protocols.size());
                String protocol = protocols.get(0);
                Assert.assertEquals(protocolName, protocol);
                latch.countDown();
                return protocol;
            }
        });

        client.addHandshakeCompletedListener(new HandshakeCompletedListener()
        {
            @Override
            public void handshakeCompleted(HandshakeCompletedEvent event)
            {
                handshakeLatch.countDown();
            }
        });

        client.startHandshake();

        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
        Assert.assertTrue(handshakeLatch.await(5, TimeUnit.SECONDS));

        // Check whether we can write real data to the connection
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(data.getBytes("UTF-8"));
        clientOutput.flush();

        client.setSoTimeout(1000);
        InputStream clientInput = client.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput, "UTF-8"));
        String line = reader.readLine();
        Assert.assertEquals(data, line);
        line = reader.readLine();
        Assert.assertNull(line);

        client.close();

        server.close();
    }

    @Test
    public void testSSLEngine() throws Exception
    {
        final SSLContext context = SSLSupport.newSSLContext();

        final String data = "data";
        final String protocolName = "test";
        final CountDownLatch latch = new CountDownLatch(4);
        final ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(47443)); // TODO: revert to 0
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    SSLEngine sslEngine = context.createSSLEngine();
                    sslEngine.setUseClientMode(false);
                    NextProtoNego.put(sslEngine, new NextProtoNego.ServerProvider()
                    {
                        @Override
                        public List<String> protocols()
                        {
                            latch.countDown();
                            return Arrays.asList(protocolName);
                        }

                        @Override
                        public void protocolSelected(String protocol)
                        {
                            Assert.assertEquals(protocolName, protocol);
                            latch.countDown();
                        }
                    });
                    ByteBuffer encrypted = ByteBuffer.allocate(sslEngine.getSession().getPacketBufferSize());
                    ByteBuffer decrypted = ByteBuffer.allocate(sslEngine.getSession().getApplicationBufferSize());

                    SocketChannel socket = server.accept();

                    sslEngine.beginHandshake();
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, sslEngine.getHandshakeStatus());

                    // Read ClientHello
                    socket.read(encrypted);
                    encrypted.flip();
                    SSLEngineResult result = sslEngine.unwrap(encrypted, decrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_TASK, result.getHandshakeStatus());
                    sslEngine.getDelegatedTask().run();
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, sslEngine.getHandshakeStatus());

                    // Generate and write ServerHello
                    encrypted.clear();
                    result = sslEngine.wrap(decrypted, encrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
                    encrypted.flip();
                    socket.write(encrypted);

                    // Read up to Finished
                    encrypted.clear();
                    socket.read(encrypted);
                    encrypted.flip();
                    result = sslEngine.unwrap(encrypted, decrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_TASK, result.getHandshakeStatus());
                    sslEngine.getDelegatedTask().run();
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, sslEngine.getHandshakeStatus());
                    if (!encrypted.hasRemaining())
                    {
                        encrypted.clear();
                        socket.read(encrypted);
                        encrypted.flip();
                    }
                    result = sslEngine.unwrap(encrypted, decrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
                    if (!encrypted.hasRemaining())
                    {
                        encrypted.clear();
                        socket.read(encrypted);
                        encrypted.flip();
                    }
                    result = sslEngine.unwrap(encrypted, decrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());

                    // With NPN in place, we need one more unwrap() call, that is
                    // not needed with without NPN, for the NextProtocol message
                    if (SSLEngineResult.HandshakeStatus.NEED_UNWRAP == result.getHandshakeStatus())
                    {
                        if (!encrypted.hasRemaining())
                        {
                            encrypted.clear();
                            socket.read(encrypted);
                            encrypted.flip();
                        }
                        result = sslEngine.unwrap(encrypted, decrypted);
                        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    }
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());

                    // Generate and write ChangeCipherSpec
                    encrypted.clear();
                    result = sslEngine.wrap(decrypted, encrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
                    encrypted.flip();
                    socket.write(encrypted);
                    // Generate and write Finished
                    encrypted.clear();
                    result = sslEngine.wrap(decrypted, encrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.FINISHED, result.getHandshakeStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, sslEngine.getHandshakeStatus());
                    encrypted.flip();
                    socket.write(encrypted);

                    // Read data
                    encrypted.clear();
                    socket.read(encrypted);
                    encrypted.flip();
                    decrypted.clear();
                    result = sslEngine.unwrap(encrypted, decrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, result.getHandshakeStatus());

                    // Echo the data back
                    encrypted.clear();
                    decrypted.flip();
                    result = sslEngine.wrap(decrypted, encrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, result.getHandshakeStatus());
                    encrypted.flip();
                    socket.write(encrypted);

                    // Close
                    encrypted.clear();
                    socket.read(encrypted);
                    encrypted.flip();
                    decrypted.clear();
                    result = sslEngine.unwrap(encrypted, decrypted);
                    Assert.assertSame(SSLEngineResult.Status.CLOSED, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
                    encrypted.clear();
                    Assert.assertEquals(-1, socket.read(encrypted));
                    encrypted.clear();
                    result = sslEngine.wrap(decrypted, encrypted);
                    Assert.assertSame(SSLEngineResult.Status.CLOSED, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, result.getHandshakeStatus());
                    encrypted.flip();
                    socket.write(encrypted);
                    socket.close();
                }
                catch (Exception x)
                {
                    x.printStackTrace();
                }
            }
        }.start();

        SSLEngine sslEngine = context.createSSLEngine();
        sslEngine.setUseClientMode(true);
        NextProtoNego.put(sslEngine, new NextProtoNego.ClientProvider()
        {
            @Override
            public boolean supports()
            {
                latch.countDown();
                return true;
            }

            @Override
            public String selectProtocol(List<String> protocols)
            {
                Assert.assertEquals(1, protocols.size());
                String protocol = protocols.get(0);
                Assert.assertEquals(protocolName, protocol);
                latch.countDown();
                return protocol;
            }
        });
        ByteBuffer encrypted = ByteBuffer.allocate(sslEngine.getSession().getPacketBufferSize());
        ByteBuffer decrypted = ByteBuffer.allocate(sslEngine.getSession().getApplicationBufferSize());

        SocketChannel client = SocketChannel.open(server.getLocalAddress());

        sslEngine.beginHandshake();
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, sslEngine.getHandshakeStatus());

        // Generate and write ClientHello
        SSLEngineResult result = sslEngine.wrap(decrypted, encrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
        encrypted.flip();
        client.write(encrypted);

        // Read Server Hello
        while (sslEngine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP)
        {
            encrypted.clear();
            client.read(encrypted);
            encrypted.flip();
            result = sslEngine.unwrap(encrypted, decrypted);
            Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
            if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK)
                sslEngine.getDelegatedTask().run();
        }
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, sslEngine.getHandshakeStatus());

        // Generate and write ClientKeyExchange
        encrypted.clear();
        result = sslEngine.wrap(decrypted, encrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
        encrypted.flip();
        client.write(encrypted);
        // Generate and write ChangeCipherSpec
        encrypted.clear();
        result = sslEngine.wrap(decrypted, encrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
        encrypted.flip();
        client.write(encrypted);
        // Generate and write Finished
        encrypted.clear();
        result = sslEngine.wrap(decrypted, encrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        encrypted.flip();
        client.write(encrypted);

        // With NPN in place, we need one more wrap() call, that is
        // not needed with without NPN, for the NexProtocol message
        if (SSLEngineResult.HandshakeStatus.NEED_WRAP == result.getHandshakeStatus())
        {
            encrypted.clear();
            result = sslEngine.wrap(decrypted, encrypted);
            Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
            encrypted.flip();
            client.write(encrypted);
        }
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());

        // Read ChangeCipherSpec
        encrypted.clear();
        client.read(encrypted);
        encrypted.flip();
        decrypted.clear();
        result = sslEngine.unwrap(encrypted, decrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
        // Read Finished
        if (!encrypted.hasRemaining())
        {
            encrypted.clear();
            client.read(encrypted);
            encrypted.flip();
        }
        result = sslEngine.unwrap(encrypted, decrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.FINISHED, result.getHandshakeStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, sslEngine.getHandshakeStatus());

        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Now try to write real data to see if it works
        encrypted.clear();
        result = sslEngine.wrap(ByteBuffer.wrap(data.getBytes("UTF-8")), encrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, result.getHandshakeStatus());
        encrypted.flip();
        client.write(encrypted);

        // Read echo
        encrypted.clear();
        client.read(encrypted);
        encrypted.flip();
        decrypted.clear();
        result = sslEngine.unwrap(encrypted, decrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, result.getHandshakeStatus());

        decrypted.flip();
        Assert.assertEquals(data, Charset.forName("UTF-8").decode(decrypted).toString());

        // Close
        sslEngine.closeOutbound();
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, sslEngine.getHandshakeStatus());
        encrypted.clear();
        result = sslEngine.wrap(decrypted, encrypted);
        Assert.assertSame(SSLEngineResult.Status.CLOSED, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
        encrypted.flip();
        client.write(encrypted);
        client.shutdownOutput();
        encrypted.clear();
        client.read(encrypted);
        encrypted.flip();
        decrypted.clear();
        result = sslEngine.unwrap(encrypted, decrypted);
        Assert.assertSame(SSLEngineResult.Status.CLOSED, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, result.getHandshakeStatus());
        client.close();
    }
}
