package org.mortbay.jetty.jmx.ws.service;

import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

public interface JMXService
{

    public abstract Set<ObjectName> getObjectNames(JMXServiceURL jmxServiceURL);
    
    public abstract Set<String> getObjectNamesByPrefix(JMXServiceURL jmxServiceURL, String prefix);

    public abstract MBeanAttributeInfo[] getAttributes(JMXServiceURL jmxServiceURL, String objectName) throws InstanceNotFoundException;

    public abstract MBeanOperationInfo[] getOperations(JMXServiceURL jmxServiceURL, String objectName) throws InstanceNotFoundException; 

    public abstract Object invoke(JMXServiceURL jmxServiceURL, String objectName, String operationName, Object[] params, String[] signature);

    public abstract Object getAttribute(JMXServiceURL jmxServiceURL, String objectName, String attributeName) throws InstanceNotFoundException;

}