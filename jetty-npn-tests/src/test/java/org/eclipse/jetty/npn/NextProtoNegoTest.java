package org.eclipse.jetty.npn;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NextProtoNegoTest
{
    private SSLContext context;

    @Before
    public void initSSLContext() throws Exception
    {
        KeyStore keyStore = getKeyStore("keystore", "storepwd");
        KeyManager[] keyManagers = getKeyManagers(keyStore, "keypwd");

        KeyStore trustStore = getKeyStore("truststore", "storepwd");
        TrustManager[] trustManagers = getTrustManagers(trustStore);

        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        context = SSLContext.getInstance("TLSv1");
        context.init(keyManagers, trustManagers, secureRandom);
    }

    @Test
    public void testSSLSocket() throws Exception
    {
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
    public void testServer() throws Exception
    {
        final SSLServerSocket server = (SSLServerSocket)context.getServerSocketFactory().createServerSocket(8443);
        while (true)
        {
            SSLSocket socket = (SSLSocket)server.accept();
            socket.setUseClientMode(false);
            NextProtoNego.put(socket, new NextProtoNego.ServerProvider()
            {
                @Override
                public List<String> protocols()
                {
                    return Arrays.asList("spdy/2", "http/1.1");
                }

                @Override
                public void protocolSelected(String protocol)
                {
                    System.err.println("protocol = " + protocol);
                }
            });
            try
            {
                socket.startHandshake();
            }
            catch (IOException x)
            {
                x.printStackTrace();
            }
        }
    }

    // TODO: add tests for SSLEngine and for renegotiations

    protected KeyStore getKeyStore(String keyStoreResource, String keyStorePassword) throws Exception
    {
        if (keyStoreResource == null)
            return null;
        InputStream keyStoreStream = getClass().getClassLoader().getResourceAsStream(keyStoreResource);
        if (keyStoreStream == null)
        {
            File keyStoreFile = new File(keyStoreResource);
            if (keyStoreFile.exists() && keyStoreFile.canRead())
                keyStoreStream = new FileInputStream(keyStoreFile);
        }
        if (keyStoreStream == null)
            return null;
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(keyStoreStream, keyStorePassword == null ? null : keyStorePassword.toCharArray());
        keyStoreStream.close();
        return keyStore;
    }

    protected KeyManager[] getKeyManagers(KeyStore keyStore, String password) throws Exception
    {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, password == null ? null : password.toCharArray());
        return keyManagerFactory.getKeyManagers();
    }

    protected TrustManager[] getTrustManagers(KeyStore trustStore) throws Exception
    {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }
}
