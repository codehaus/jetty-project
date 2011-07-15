package org.mortbay.jetty.rhttp.connector;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.embedded.HelloHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.mortbay.jetty.rhttp.client.RHTTPClient;
import org.mortbay.jetty.rhttp.client.JettyClient;

/**
 * A Test content server that uses a {@link ReverseHTTPConnector}.
 * The main of this class starts 3 TestReversionServers with IDs A, B and C.
 */
public class TestReverseServer extends Server
{
    TestReverseServer(String targetId)
    {
        setHandler(new HelloHandler("Hello "+targetId,"Hi from "+targetId));
        
        HttpClient httpClient = new HttpClient();
        RHTTPClient client = new JettyClient(httpClient,"http://localhost:8080/__rhttp",targetId);
        ReverseHTTPConnector connector = new ReverseHTTPConnector(client);
        
        addConnector(connector);
    }
    
    public static void main(String... args) throws Exception
    {
        Log.getLogger("org.mortbay.jetty.rhttp.client").setDebugEnabled(true);
        
        TestReverseServer[] node = new TestReverseServer[] { new TestReverseServer("A"),new TestReverseServer("B"),new TestReverseServer("C") };
        
        for (TestReverseServer s : node)
            s.start();

        for (TestReverseServer s : node)
            s.join();
    }
}
