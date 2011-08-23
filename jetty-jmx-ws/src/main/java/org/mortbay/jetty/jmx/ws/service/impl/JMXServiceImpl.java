// ========================================================================
// Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================

package org.mortbay.jetty.jmx.ws.service.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.jetty.util.log.Log;
import org.mortbay.jetty.jmx.ws.service.JMXService;

public class JMXServiceImpl implements JMXService
{
    public static final String MEMORY_MXBEAN = "java.lang:type=Memory";
    public static final String THREADING_MXBEAN = "java.lang:type=Threading";
    public static final String LOGGING_MBEAN = "java.util.logging:type=Logging";
    public static final String JETTY_SERVER_MBEAN = "org.eclipse.jetty.server:type=server,id=0";
    public static final String JETTY_SERVER_MBEANID1 = "org.eclipse.jetty.server:type=server,id=1";
    public static final String MEMORY_MXBEAN_HEAP = "HeapMemoryUsage";
    public static final String MEMORY_MXBEAN_NONHEAP = "NonHeapMemoryUsage";
    public static final String MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION = "ObjectPendingFinalizationCount";
    public static final String MEMORY_MXBEAN_VERBOSE = "Verbose";

    private static Map<JMXServiceURL, MBeanServerConnection> connections = new HashMap<JMXServiceURL, MBeanServerConnection>();

    private static JMXService jmxService;

    private JMXServiceImpl()
    {
    }

    public static JMXService getInstance()
    {
        if (jmxService == null)
        {
            jmxService = new JMXServiceImpl();
        }
        return jmxService;
    }
    
    public Set<ObjectName> getObjectNames(JMXServiceURL jmxServiceURL)
    {
        try
        {
            return getConnection(jmxServiceURL).queryNames(null,null);
        }
        catch (IOException e)
        {
            Log.warn("getObjectNames: ",e);
        }
        return Collections.emptySet();
    }

    public Set<String> getObjectNamesByPrefix(JMXServiceURL jmxServiceURL, String prefix)
    {
        Set<ObjectName> objectNames = getObjectNames(jmxServiceURL);
        Set<String> filteredObjectNames = new HashSet<String>();
        for (ObjectName objectName : objectNames)
        {
            String canonicalName = objectName.toString();
            if (canonicalName.startsWith(prefix))
                filteredObjectNames.add(objectName.toString());
        }
        return filteredObjectNames;
    }

    public MBeanAttributeInfo[] getAttributes(JMXServiceURL jmxServiceURL, String objectName) throws InstanceNotFoundException
    {
        try
        {
            return getConnection(jmxServiceURL).getMBeanInfo(new ObjectName(objectName)).getAttributes();
        }
        catch (InstanceNotFoundException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            Log.warn("getAttributes: ",e);
        }
        return new MBeanAttributeInfo[] {};
    }

    public MBeanOperationInfo[] getOperations(JMXServiceURL jmxServiceURL, String objectName) throws InstanceNotFoundException
    {
        try
        {
            return getConnection(jmxServiceURL).getMBeanInfo(new ObjectName(objectName)).getOperations();
        }
        catch (InstanceNotFoundException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            Log.warn("getOperations: ",e);
        }
        return new MBeanOperationInfo[] {};
    }

    public Object invoke(JMXServiceURL jmxServiceURL, String objectName, String operationName, Object[] params, String[] signature)
    {
        try
        {
            Log.debug("invoke: jmxServiceURL: " + jmxServiceURL.toString() + ", objectName: " + objectName + ", operationName: " + operationName); //TODO: remove me
            return getConnection(jmxServiceURL).invoke(new ObjectName(objectName),operationName,params,signature);
        }
        catch (Exception e)
        {
            Log.warn("invoke: jmxServiceURL: " + jmxServiceURL.toString() + ", objectName: " + objectName + ", operationName: " + operationName,e);
            return e.toString();
        }
    }

    public Object getAttribute(JMXServiceURL jmxServiceURL, String objectName, String attribute) throws InstanceNotFoundException
    {
        try
        {
            return getConnection(jmxServiceURL).getAttribute(new ObjectName(objectName),attribute);
        }
        catch (InstanceNotFoundException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            Log.warn("Couldn't get attribute: " + attribute,e);
            return e.toString();
        }

    }

    private MBeanServerConnection getConnection(JMXServiceURL jmxServiceURL) throws IOException
    {
        if (connections.get(jmxServiceURL) == null)
        {
            Log.debug("getConnection: opening jmx connection to: " + jmxServiceURL.toString());
            JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceURL,null);
            connections.put(jmxServiceURL,jmxConnector.getMBeanServerConnection());
        }
        return connections.get(jmxServiceURL);
    }

}
