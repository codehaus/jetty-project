package org.eclipse.jetty.npn;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

/**
 * Server that just accepts socket connections and advertises spdy/2.
 * This is useful to test with Chromium and see if Chromium actually
 * selects the spdy/2 protocol, and if our implementation works.
 */
public class TestServer
{
    public static void main(String[] args) throws Exception
    {
        SSLContext context = SSLSupport.newSSLContext();
        SSLServerSocket server = (SSLServerSocket)context.getServerSocketFactory().createServerSocket(8443);
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
}
