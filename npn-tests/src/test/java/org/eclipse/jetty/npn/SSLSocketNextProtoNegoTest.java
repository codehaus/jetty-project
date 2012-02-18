/*
 * Copyright (c) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.jetty.npn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class SSLSocketNextProtoNegoTest
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
}
