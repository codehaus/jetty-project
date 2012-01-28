package org.eclipse.jetty.npn;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import org.junit.Assert;
import org.junit.Test;

public class NextProtoNegoTest
{
    @Test
    public void testSSLSocket() throws Exception
    {
        SSLContext context = SSLSupport.newSSLContext();

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
                    socket.startHandshake();
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
        client.startHandshake();

        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));

        client.close();

        server.close();
    }

    @Test
    public void testSSLEngine() throws Exception
    {
        final SSLContext context = SSLSupport.newSSLContext();

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
                    ByteBuffer buffer = ByteBuffer.allocate(Math.max(encrypted.capacity(), decrypted.capacity()));

                    SocketChannel socket = server.accept();

                    sslEngine.beginHandshake();
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, sslEngine.getHandshakeStatus());

                    // Read ClientHello
                    socket.read(buffer);
                    buffer.flip();
                    SSLEngineResult result = sslEngine.unwrap(buffer, decrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_TASK, result.getHandshakeStatus());
                    sslEngine.getDelegatedTask().run();
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, sslEngine.getHandshakeStatus());

                    // Generate and write ServerHello
                    buffer.clear();
                    result = sslEngine.wrap(buffer, encrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
                    encrypted.flip();
                    socket.write(encrypted);

                    // Read up to Finished
                    socket.read(buffer);
                    buffer.flip();
                    result = sslEngine.unwrap(buffer, decrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_TASK, result.getHandshakeStatus());
                    sslEngine.getDelegatedTask().run();
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, sslEngine.getHandshakeStatus());
                    if (!buffer.hasRemaining())
                    {
                        buffer.clear();
                        socket.read(buffer);
                        buffer.flip();
                    }
                    result = sslEngine.unwrap(buffer, decrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
                    if (!buffer.hasRemaining())
                    {
                        buffer.clear();
                        socket.read(buffer);
                        buffer.flip();
                    }
                    result = sslEngine.unwrap(buffer, decrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());

                    // Seems that with NPN in place, we need one more
                    // unwrap() call, that is not needed with without NPN
                    if (SSLEngineResult.HandshakeStatus.NEED_UNWRAP == result.getHandshakeStatus())
                    {
                        if (!buffer.hasRemaining())
                        {
                            buffer.clear();
                            socket.read(buffer);
                            buffer.flip();
                        }
                        result = sslEngine.unwrap(buffer, decrypted);
                        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_TASK, result.getHandshakeStatus());
                        sslEngine.getDelegatedTask().run();
                        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, sslEngine.getHandshakeStatus());
                    }
                    else
                    {
                        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
                    }

                    // Generate and write ChangeCipherSpec
                    encrypted.clear();
                    result = sslEngine.wrap(buffer, encrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
                    encrypted.flip();
                    socket.write(encrypted);
                    // Generate and write Finished
                    encrypted.clear();
                    result = sslEngine.wrap(buffer, encrypted);
                    Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
                    Assert.assertSame(SSLEngineResult.HandshakeStatus.FINISHED, result.getHandshakeStatus());
                    encrypted.flip();
                    socket.write(encrypted);
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
        ByteBuffer buffer = ByteBuffer.allocate(Math.max(encrypted.capacity(), decrypted.capacity()));

        SocketChannel client = SocketChannel.open(server.getLocalAddress());

        sslEngine.beginHandshake();
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, sslEngine.getHandshakeStatus());

        // Generate and write ClientHello
        SSLEngineResult result = sslEngine.wrap(buffer, encrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
        encrypted.flip();
        client.write(encrypted);

        // Read Server Hello
        client.read(buffer);
        buffer.flip();
        result = sslEngine.unwrap(buffer, decrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_TASK, result.getHandshakeStatus());
        sslEngine.getDelegatedTask().run();
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, sslEngine.getHandshakeStatus());

        // Generate and write ClientKeyExchange
        encrypted.clear();
        result = sslEngine.wrap(buffer, encrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
        encrypted.flip();
        client.write(encrypted);
        // Generate and write ChangeCipherSpec
        encrypted.clear();
        result = sslEngine.wrap(buffer, encrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_WRAP, result.getHandshakeStatus());
        encrypted.flip();
        client.write(encrypted);
        // Generate and write Finished
        encrypted.clear();
        result = sslEngine.wrap(buffer, encrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        encrypted.flip();
        client.write(encrypted);

        // Seems that with NPN in place, we need one more
        // wrap() call, that is not needed with without NPN
        if (SSLEngineResult.HandshakeStatus.NEED_WRAP == result.getHandshakeStatus())
        {
            encrypted.clear();
            result = sslEngine.wrap(buffer, encrypted);
            Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
            encrypted.flip();
            client.write(encrypted);
        }
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());

        // Read ChangeCipherSpec
        buffer.clear();
        client.read(buffer);
        buffer.flip();
        result = sslEngine.unwrap(buffer, decrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, result.getHandshakeStatus());
        // Read Finished
        if (!buffer.hasRemaining())
        {
            buffer.clear();
            client.read(buffer);
            buffer.flip();
        }
        result = sslEngine.unwrap(buffer, decrypted);
        Assert.assertSame(SSLEngineResult.Status.OK, result.getStatus());
        Assert.assertSame(SSLEngineResult.HandshakeStatus.FINISHED, result.getHandshakeStatus());

        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}
