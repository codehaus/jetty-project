package org.mortbay.jetty.jmx.ws.util;

import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import org.eclipse.jetty.client.Address;

public class JMXServiceURLUtils
{

    private static MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private static ObjectName name;
    private static JMXServiceURL rmiServiceURL;

    public static JMXServiceURL getLocalJMXServiceURL()
    {
        if (rmiServiceURL == null)
        {
            try
            {
                name = new ObjectName("org.eclipse.jetty:name=rmiconnectorserver");
                rmiServiceURL = (JMXServiceURL)mbs.getAttribute(name,"Address");
            }
            catch (Exception e)
            {
                throw new IllegalStateException(e);
            }

        }
        return rmiServiceURL;
    }
    
    public static JMXServiceURL getJMXServiceURL(Address address){
        return getJMXServiceURL("service:jmx:rmi:///jndi/rmi://" + address.getHost()+ ":" + address.getPort()+ "/jettyjmx");
    }

    public static JMXServiceURL getJMXServiceURL(String jmxServiceURLString)
    {
        try
        {
            return new JMXServiceURL(jmxServiceURLString);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalStateException(e);
        }
    }

}
