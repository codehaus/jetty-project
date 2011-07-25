package org.mortbay.jetty.jmx.ws.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXServiceURL;
import javax.ws.rs.core.UriInfo;

import org.mortbay.jetty.jmx.ws.domain.JMXNode;
import org.mortbay.jetty.jmx.ws.domain.jaxb.NodeJaxBean;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanAttributeJaxBean;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanAttributeValueJaxBean;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanOperationJaxBean;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanOperationJaxBeans;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanShortJaxBean;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanShortJaxBeans;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.OperationReturnValueJaxBean;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.OperationReturnValueJaxBeans;
import org.mortbay.jetty.jmx.ws.service.AggregateService;
import org.mortbay.jetty.jmx.ws.service.JMXNodeService;
import org.mortbay.jetty.jmx.ws.service.JMXService;



public class AggregateServiceImpl implements AggregateService
{
    private static final String[] HEAP_MEMORY_KEYS = new String[]
    { "init", "used", "committed", "max" };
    public static final String ID_REPLACE_REGEX = ",id=\\d+";

    private JMXService _jmxService;
    // TODO: IOC
    private JMXNodeService _jmxNodeService = new JMXNodeServiceImpl();

    public AggregateServiceImpl(JMXService jmxService)
    {
        this._jmxService = jmxService;
    }

    public Set<NodeJaxBean> getNodes()
    {
        Set<NodeJaxBean> nodes = new TreeSet<NodeJaxBean>();
        for (JMXNode jmxNode : _jmxNodeService.getNodes())
        {
            JMXServiceURL jmxServiceURL = jmxNode.getJmxServiceURL();
            String jettyVersion;
            try
            {
                jettyVersion = getJettyVersions(jmxServiceURL);
                Map<String, Long> nodeMemoryUsageMap = getMemoryByNode(jmxNode);
                NodeJaxBean NodeJaxBean = new NodeJaxBean.NodeJaxBeanBuilder(jmxNode.getNodeName(),jettyVersion,jmxServiceURL).memory(nodeMemoryUsageMap)
                        .threadCount(getThreadCount(jmxServiceURL)).peakThreadCount(getPeakThreadCount(jmxServiceURL)).build();
                nodes.add(NodeJaxBean);
            }
            catch (InstanceNotFoundException e)
            {
                throw new IllegalStateException("While fetching data for: " + jmxNode.getNodeName(),e);
            }
        }
        return nodes;
    }

    private String getJettyVersions(JMXServiceURL jmxServiceURL) throws InstanceNotFoundException
    {
        return (String)_jmxService.getAttribute(jmxServiceURL,JMXServiceImpl.JETTY_SERVER_MBEAN,"version");
    }

    private Map<String, Long> getMemoryByNode(JMXNode jmxNode) throws InstanceNotFoundException
    {
        JMXServiceURL jmxServiceURL = jmxNode.getJmxServiceURL();
        CompositeData memoryCompositeData = (CompositeData)_jmxService.getAttribute(jmxServiceURL,JMXServiceImpl.MEMORY_MXBEAN,
                JMXServiceImpl.MEMORY_MXBEAN_HEAP);
        Map<String, Long> nodeMemoryUsageMap = new HashMap<String, Long>();
        for (String memoryKey : HEAP_MEMORY_KEYS)
        {
            if (memoryCompositeData == null)
                continue;
            Long heapUsage = (Long)memoryCompositeData.get(memoryKey);
            nodeMemoryUsageMap.put(memoryKey,heapUsage);
        }
        return nodeMemoryUsageMap;
    }

    private int getThreadCount(JMXServiceURL jmxServiceURL) throws InstanceNotFoundException
    {
        Object value = _jmxService.getAttribute(jmxServiceURL,JMXServiceImpl.THREADING_MXBEAN,"ThreadCount");
        if (value == null)
            return -1;
        return (Integer)value;
    }

    private int getPeakThreadCount(JMXServiceURL jmxServiceURL) throws InstanceNotFoundException
    {
        Object value = _jmxService.getAttribute(jmxServiceURL,JMXServiceImpl.THREADING_MXBEAN,"PeakThreadCount");
        if (value == null)
            return -1;
        return (Integer)value;
    }

    public MBeanShortJaxBeans getMBeanShortJaxBeans(UriInfo uriInfo, Collection<JMXNode> jmxNodes)
    {
        Set<ObjectName> commonObjectNames = new HashSet<ObjectName>();

        Set<MBeanShortJaxBean> mBeanShortJaxBeans = new TreeSet<MBeanShortJaxBean>();
        for (JMXNode jmxNode : jmxNodes)
        {
            Set<ObjectName> nodeObjectNames = _jmxService.getObjectNames(jmxNode.getJmxServiceURL());
            if (commonObjectNames.isEmpty())
                commonObjectNames.addAll(nodeObjectNames);
            else
                removeObjectNamesWhichDoNotExistOnCurrentNode(commonObjectNames,nodeObjectNames);

        }
        for (String objectNameString : getObjectNamesWithoutId(commonObjectNames))
        {
            mBeanShortJaxBeans.add(new MBeanShortJaxBean(uriInfo,objectNameString));
        }
        return new MBeanShortJaxBeans(mBeanShortJaxBeans);
    }

    private void removeObjectNamesWhichDoNotExistOnCurrentNode(Set<ObjectName> commonObjectNames, Set<ObjectName> nodeObjectNames)
    {
        for (Iterator<ObjectName> it = commonObjectNames.iterator(); it.hasNext();)
        {
            ObjectName objectName = it.next();
            if (!nodeObjectNames.contains(objectName))
                it.remove();
        }
    }

    private Set<String> getObjectNamesWithoutId(Set<ObjectName> objectNames)
    {
        Set<String> objectNameStrings = new TreeSet<String>();
        for (ObjectName objectName : objectNames)
        {
            objectNameStrings.add(objectName.toString().replaceFirst(ID_REPLACE_REGEX,""));
        }
        return objectNameStrings;
    }

    public MBeanAttributeValueJaxBeans getAllAttributeValues(Collection<JMXNode> jmxNodes, String objectName) throws InstanceNotFoundException
    {
        Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans = new TreeSet<MBeanAttributeValueJaxBean>();
        Map<String, MBeanAttributeInfo> mBeanAttributeInfos = aggregateMBeanAttributeInfos(jmxNodes,objectName);
        for (JMXNode jmxNode : jmxNodes)
        {
            JMXServiceURL jmxServiceURL = jmxNode.getJmxServiceURL();
            Set<String> objectNames = parseObjectNameToAggregateMBeansWithMultipleIDs(objectName,jmxServiceURL);
            try
            {
                mBeanAttributeValueJaxBeans.addAll(getAttributeValues(objectNames,mBeanAttributeInfos,jmxNode));
            }
            catch (InstanceNotFoundException e)
            {
                objectNames = _jmxService.getObjectNamesByPrefix(jmxServiceURL,objectName);
                mBeanAttributeValueJaxBeans.addAll(getAttributeValues(objectNames,mBeanAttributeInfos,jmxNode));
            }
        }
        return new MBeanAttributeValueJaxBeans(mBeanAttributeValueJaxBeans);
    }

    private Set<MBeanAttributeValueJaxBean> getAttributeValues(Set<String> objectNames, Map<String, MBeanAttributeInfo> mBeanAttributeInfos,
            JMXNode jmxNode) throws InstanceNotFoundException
    {
        Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans = new TreeSet<MBeanAttributeValueJaxBean>();
        for (String attributeName : mBeanAttributeInfos.keySet())
        {
            getAttributeValuesForNode(attributeName,mBeanAttributeValueJaxBeans,jmxNode,objectNames);
        }
        return mBeanAttributeValueJaxBeans;
    }

    public MBeanAttributeValueJaxBeans getAttributeValues(Collection<JMXNode> jmxNodes, String objectName, String attributeName)
            throws InstanceNotFoundException
    {
        Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans = new TreeSet<MBeanAttributeValueJaxBean>();
        for (JMXNode jmxNode : jmxNodes)
        {
            JMXServiceURL jmxServiceURL = jmxNode.getJmxServiceURL();
            Set<String> objectNames = parseObjectNameToAggregateMBeansWithMultipleIDs(objectName,jmxServiceURL);
            try
            {
                getAttributeValuesForNode(attributeName,mBeanAttributeValueJaxBeans,jmxNode,objectNames);
            }
            catch (InstanceNotFoundException e)
            {
                objectNames = _jmxService.getObjectNamesByPrefix(jmxServiceURL,objectName);
                getAttributeValuesForNode(attributeName,mBeanAttributeValueJaxBeans,jmxNode,objectNames);
            }
        }
        return new MBeanAttributeValueJaxBeans(mBeanAttributeValueJaxBeans);
    }

    private void getAttributeValuesForNode(String attributeName, Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans,
            JMXNode jmxNode, Set<String> objectNames) throws InstanceNotFoundException
    {
        for (String aggregatedObjectName : objectNames)
        {
            Object value = _jmxService.getAttribute(jmxNode.getJmxServiceURL(),aggregatedObjectName,attributeName);
            mBeanAttributeValueJaxBeans.add(new MBeanAttributeValueJaxBean(attributeName,jmxNode.getNodeName(),aggregatedObjectName,value));
        }
    }

    public MBeanOperationJaxBeans getOperationsMetaData(UriInfo uriInfo, Collection<JMXNode> jmxNodes, String objectName)
            throws InstanceNotFoundException
    {
        Set<MBeanOperationJaxBean> mBeanOperationJaxBeans = new TreeSet<MBeanOperationJaxBean>();
        Map<String, MBeanOperationInfo> mBeanOperations = aggregateOperations(jmxNodes,objectName);

        for (MBeanOperationInfo mBeanOperationInfo : mBeanOperations.values())
            mBeanOperationJaxBeans.add(new MBeanOperationJaxBean(uriInfo,mBeanOperationInfo));

        return new MBeanOperationJaxBeans(objectName,mBeanOperationJaxBeans);
    }

    private Map<String, MBeanOperationInfo> aggregateOperations(Collection<JMXNode> jmxNodes, String objectName) throws InstanceNotFoundException
    {

        Map<String, MBeanOperationInfo> mBeanOperations = new HashMap<String, MBeanOperationInfo>();
        Set<MBeanOperationInfo> nonEqualOperationsToRemove = new HashSet<MBeanOperationInfo>();
        // TODO: three nested for loops and some if clauses...too high cyclomatic complexity
        for (JMXNode jmxNode : jmxNodes)
        {
            JMXServiceURL jmxServiceURL = jmxNode.getJmxServiceURL();
            Set<String> objectNames = parseObjectNameToAggregateMBeansWithMultipleIDs(objectName,jmxServiceURL);
            try
            {
                if (!aggregateMBeanOperationInfosForNode(objectNames,mBeanOperations,nonEqualOperationsToRemove,jmxServiceURL))
                    return Collections.emptyMap();
            }
            catch (InstanceNotFoundException e)
            {
                objectNames = _jmxService.getObjectNamesByPrefix(jmxServiceURL,objectName);
                if (!aggregateMBeanOperationInfosForNode(objectNames,mBeanOperations,nonEqualOperationsToRemove,jmxServiceURL))
                    return Collections.emptyMap();
            }
        }

        removeNonEqualOperations(mBeanOperations,nonEqualOperationsToRemove);

        return mBeanOperations;
    }

    private boolean aggregateMBeanOperationInfosForNode(Set<String> objectNames, Map<String, MBeanOperationInfo> mBeanOperations,
            Set<MBeanOperationInfo> nonEqualOperationsToRemove, JMXServiceURL jmxServiceURL) throws InstanceNotFoundException
    {
        for (String aggregatedObjectName : objectNames)
        {
            MBeanOperationInfo[] mBeanOperationInfoArray = _jmxService.getOperations(jmxServiceURL,aggregatedObjectName);
            if (mBeanOperationInfoArray != null)
                for (MBeanOperationInfo mBeanOperationInfo : mBeanOperationInfoArray)
                {
                    if (mBeanOperations.containsKey(mBeanOperationInfo.getName())
                            && !mBeanOperations.get(mBeanOperationInfo.getName()).equals(mBeanOperationInfo))
                        nonEqualOperationsToRemove.add(mBeanOperationInfo);
                    else
                        mBeanOperations.put(mBeanOperationInfo.getName(),mBeanOperationInfo);
                }
            else
                return false;
        }
        return true;
    }

    private void removeNonEqualOperations(Map<String, MBeanOperationInfo> mBeanOperations, Set<MBeanOperationInfo> nonEqualOperationsToRemove)
    {
        for (MBeanOperationInfo mBeanOperationInfo : nonEqualOperationsToRemove)
        {
            mBeanOperations.remove(mBeanOperationInfo.getName());
        }
    }

    public MBeanAttributeJaxBeans getAttributesMetaData(UriInfo uriInfo, Collection<JMXNode> jmxNodes, String objectName)
            throws InstanceNotFoundException
    {
        Set<MBeanAttributeJaxBean> mBeanAttributeJaxBeans = new TreeSet<MBeanAttributeJaxBean>();
        Map<String, MBeanAttributeInfo> mBeanAttributeInfos = aggregateMBeanAttributeInfos(jmxNodes,objectName);

        for (String attributeName : mBeanAttributeInfos.keySet())
            mBeanAttributeJaxBeans.add(new MBeanAttributeJaxBean(uriInfo,mBeanAttributeInfos.get(attributeName)));

        return new MBeanAttributeJaxBeans(mBeanAttributeJaxBeans);
    }

    private Map<String, MBeanAttributeInfo> aggregateMBeanAttributeInfos(Collection<JMXNode> jmxNodes, String objectName)
            throws InstanceNotFoundException
    {
        Map<String, MBeanAttributeInfo> mBeanAttributeInfos = new HashMap<String, MBeanAttributeInfo>();
        Set<String> nonEqualAttributeNamesToRemove = new HashSet<String>();
        for (JMXNode jmxNode : jmxNodes)
        {
            JMXServiceURL jmxServiceURL = jmxNode.getJmxServiceURL();
            Set<String> objectNames = parseObjectNameToAggregateMBeansWithMultipleIDs(objectName,jmxServiceURL);

            try
            {
                if (!aggregateMBeanAttributeInfosForNode(mBeanAttributeInfos,nonEqualAttributeNamesToRemove,jmxServiceURL,objectNames))
                    return Collections.emptyMap();
            }
            catch (InstanceNotFoundException e)
            {
                objectNames = _jmxService.getObjectNamesByPrefix(jmxServiceURL,objectName);
                if (!aggregateMBeanAttributeInfosForNode(mBeanAttributeInfos,nonEqualAttributeNamesToRemove,jmxServiceURL,objectNames))
                    return Collections.emptyMap();
            }
        }

        removeDuplicates(mBeanAttributeInfos,nonEqualAttributeNamesToRemove);

        return mBeanAttributeInfos;
    }

    private boolean aggregateMBeanAttributeInfosForNode(Map<String, MBeanAttributeInfo> mBeanAttributeInfos, Set<String> nonEqualAttributeNamesToRemove,
            JMXServiceURL jmxServiceURL, Set<String> objectNames) throws InstanceNotFoundException
    {
        for (String aggregatedObjectName : objectNames)
        {
            MBeanAttributeInfo[] mBeanAttributeInfoArray = _jmxService.getAttributes(jmxServiceURL,aggregatedObjectName);
            if (mBeanAttributeInfoArray != null)
                addAttributeInfosToMap(mBeanAttributeInfos,nonEqualAttributeNamesToRemove,mBeanAttributeInfoArray);
            else
                return false;
        }
        return true;
    }

    Set<String> parseObjectNameToAggregateMBeansWithMultipleIDs(String objectName, JMXServiceURL jmxServiceURL)
    {
        Set<String> objectNames = new TreeSet<String>();
        objectNames.add(objectName);

        String string = objectName.toString();
        String idMatchRegex = ".*?" + ID_REPLACE_REGEX;

        if (string.matches(idMatchRegex))
        {
            String objectNamePrefix = objectName.replaceFirst(ID_REPLACE_REGEX,"");
            objectNames = _jmxService.getObjectNamesByPrefix(jmxServiceURL,objectNamePrefix);
        }
        return objectNames;
    }

    private void addAttributeInfosToMap(Map<String, MBeanAttributeInfo> mBeanAttributeInfos, Set<String> duplicatesToRemove,
            MBeanAttributeInfo[] mBeanAttributeInfoArray)
    {
        for (MBeanAttributeInfo mBeanAttributeInfo : mBeanAttributeInfoArray)
        {
            String attributeName = mBeanAttributeInfo.getName();
            if (mBeanAttributeInfos.containsKey(attributeName) && !mBeanAttributeInfo.equals(mBeanAttributeInfos.get(attributeName)))
                duplicatesToRemove.add(attributeName);
            else
                mBeanAttributeInfos.put(attributeName,mBeanAttributeInfo);
        }
    }

    private void removeDuplicates(Map<String, MBeanAttributeInfo> mBeanAttributeInfos, Set<String> duplicatesToRemove)
    {
        for (String attributeName : duplicatesToRemove)
            mBeanAttributeInfos.remove(attributeName);
    }

    public OperationReturnValueJaxBeans invokeOperation(Collection<JMXNode> jmxNodes, String objectName, String operationName)
    {
        Set<OperationReturnValueJaxBean> operationReturnValueJaxBeans = new TreeSet<OperationReturnValueJaxBean>();
        for (JMXNode jmxNode : jmxNodes)
        {
            Object returnValue = _jmxService.invoke(jmxNode.getJmxServiceURL(),objectName,operationName,null,null);
            operationReturnValueJaxBeans.add(new OperationReturnValueJaxBean(jmxNode.getNodeName(),returnValue));
        }
        return new OperationReturnValueJaxBeans(operationReturnValueJaxBeans);
    }

    public OperationReturnValueJaxBeans invokeOperation(Collection<JMXNode> jmxNodes, String objectName, String operationName, Object[] params,
            String[] signature)
    {
        Set<OperationReturnValueJaxBean> operationReturnValueJaxBeans = new TreeSet<OperationReturnValueJaxBean>();
        for (JMXNode jmxNode : jmxNodes)
        {
            Object returnValue = _jmxService.invoke(jmxNode.getJmxServiceURL(),objectName,operationName,params,signature);
            operationReturnValueJaxBeans.add(new OperationReturnValueJaxBean(jmxNode.getNodeName(),returnValue));
        }
        return new OperationReturnValueJaxBeans(operationReturnValueJaxBeans);
    }
}
