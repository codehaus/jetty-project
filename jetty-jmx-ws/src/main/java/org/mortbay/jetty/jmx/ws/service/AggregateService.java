package org.mortbay.jetty.jmx.ws.service;

import java.util.Collection;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.ws.rs.core.UriInfo;

import org.mortbay.jetty.jmx.ws.domain.JMXNode;
import org.mortbay.jetty.jmx.ws.domain.jaxb.NodeJaxBean;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanOperationJaxBeans;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanShortJaxBeans;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.OperationReturnValueJaxBeans;




public interface AggregateService
{
    public Set<NodeJaxBean> getNodes();
    
    public MBeanShortJaxBeans getMBeanShortJaxBeans(UriInfo uriInfo, Collection<JMXNode> jmxNodes);

    public MBeanAttributeJaxBeans getAttributesMetaData(UriInfo uriInfo, Collection<JMXNode> jmxNodes, String objectName) throws InstanceNotFoundException;
    
    public MBeanOperationJaxBeans getOperationsMetaData(UriInfo uriInfo, Collection<JMXNode> jmxNodes, String objectName) throws InstanceNotFoundException;
    
    public MBeanAttributeValueJaxBeans getAllAttributeValues(Collection<JMXNode> jmxNodes, String objectName) throws InstanceNotFoundException;
    
    public MBeanAttributeValueJaxBeans getAttributeValues(Collection<JMXNode> jmxNodes, String objectName, String attributeName) throws InstanceNotFoundException;
    
    public OperationReturnValueJaxBeans invokeOperation(Collection<JMXNode> jmxNodes, String objectName, String operationName);

    public OperationReturnValueJaxBeans invokeOperation(Collection<JMXNode> jmxNodes, String objectName, String operationName, Object[] params, String[] signature);
}
