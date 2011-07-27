package org.mortbay.jetty.jmx.ws.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.remote.JMXServiceURL;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mortbay.jetty.jmx.ws.AbstractMockitoTest;
import org.mortbay.jetty.jmx.ws.domain.JMXNode;
import org.mortbay.jetty.jmx.ws.domain.jaxb.NodeJaxBean;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanAttributeValueJaxBean;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanOperationJaxBeans;
import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanShortJaxBeans;
import org.mortbay.jetty.jmx.ws.service.AggregateService;
import org.mortbay.jetty.jmx.ws.service.JMXNodeService;
import org.mortbay.jetty.jmx.ws.service.JMXService;
import org.mortbay.jetty.jmx.ws.util.JMXServiceURLUtils;

public class AggregateServiceTest extends AbstractMockitoTest
{
    private static final String NODE1 = "localhost:8888";
    private static final String NODE2 = "localhost:9999";
    private static final String NODE3 = "localhost:7777";
    private static final String JETTY_VERSION = "7.3.0-SNAPSHOT";

    @Mock
    JMXService _jmxService;
    @Mock
    JMXNodeService _jmxNodeService;
    @Mock
    UriInfo _uriInfo;
    @Mock
    UriBuilder _uriBuilder;

    // class under test
    @InjectMocks
    AggregateService _aggregateServiceImpl = new AggregateServiceImpl(_jmxService);

    // test data
    Collection<JMXNode>_jmxNodes;
    JMXNode _localCloudtideNode;
    JMXNode _remoteCloudtideNode2;
    JMXNode _remoteCloudtideNode3;
    JMXServiceURL _jmxServiceURLNode1 = JMXServiceURLUtils.getJMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jettyjmx");
    JMXServiceURL _jmxServiceURLNode2 = JMXServiceURLUtils.getJMXServiceURL("service:jmx:rmi://node2/jndi/rmi://localhost:1100/jettyjmx");
    JMXServiceURL _jmxServiceURLNode3 = JMXServiceURLUtils.getJMXServiceURL("service:jmx:rmi://node3/jndi/rmi://localhost:1101/jettyjmx");
    CompositeDataSupport _compositeDataHeapNode1;
    CompositeDataSupport _compositeDataHeapNode2;
    CompositeDataSupport _compositeDataHeapNode3;
    CompositeDataSupport _compositeDataNonHeapNode1;
    CompositeDataSupport _compositeDataNonHeapNode2;
    CompositeDataSupport _compositeDataNonHeapNode3;

    int _nodes = 2;

    final long _init = 0L;
    long _usedNode1 = 14122104L;
    long _comittedNode1 = 851000192L;
    long _max = 129957888L;
    long _usedNode2 = 15622104L;
    long _comittedNode2 = 955000192L;
    long _maxNode2 = 134957888L;
    boolean _verboseNode1 = true;
    boolean _verboseNode2 = false;
    boolean _verboseNode3 = false;
    int _objectsPendingFinalizationNode1 = 0;
    int _objectsPendingFinalizationNode2 = 1;
    int _objectsPendingFinalizationNode3 = 1;
    private String _jettyServerBeanPrefix = JMXServiceImpl.JETTY_SERVER_MBEAN.replaceFirst(AggregateServiceImpl.ID_REPLACE_REGEX,"");
    private Set<String> _jettyServerObjectNames = new TreeSet<String>();

    @Before
    public void setUp() throws Exception
    {
        _localCloudtideNode = new JMXNode("localNode",_jmxServiceURLNode1);
        _remoteCloudtideNode2 = new JMXNode(NODE2,_jmxServiceURLNode2);
        _remoteCloudtideNode3 = new JMXNode(NODE3,_jmxServiceURLNode3);
        _jmxNodes = new HashSet<JMXNode>();
        _jmxNodes.add(_localCloudtideNode);
        _jmxNodes.add(_remoteCloudtideNode2);
        _jmxNodes.add(_remoteCloudtideNode3);

        String[] names = new String[]
        { "init", "committed", "max", "used" };

        @SuppressWarnings("rawtypes")
        OpenType[] itemTypes = new OpenType[]
        { SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG };

        CompositeType compositeType = new CompositeType("mem","mem",names,names,itemTypes);
        _compositeDataHeapNode1 = new CompositeDataSupport(compositeType,fillCompositeDataMap(_init,_usedNode1,_comittedNode1,_max));
        _compositeDataHeapNode2 = new CompositeDataSupport(compositeType,fillCompositeDataMap(_init,_usedNode2,_comittedNode2,_maxNode2));
        _compositeDataHeapNode3 = _compositeDataHeapNode2;
        _compositeDataNonHeapNode1 = new CompositeDataSupport(compositeType,fillCompositeDataMap(_init,123L,123L,123L));
        _compositeDataNonHeapNode2 = new CompositeDataSupport(compositeType,fillCompositeDataMap(_init,234L,234L,234L));
        _compositeDataNonHeapNode3 = _compositeDataNonHeapNode2;

        _jettyServerObjectNames.add(JMXServiceImpl.JETTY_SERVER_MBEAN);
        _jettyServerObjectNames.add(JMXServiceImpl.JETTY_SERVER_MBEANID1);
    }

    private Map<String, Long> fillCompositeDataMap(Long init, Long used, Long committed, Long max)
    {
        Map<String, Long> map = new TreeMap<String, Long>();
        map.put("init",init);
        map.put("committed",committed);
        map.put("max",max);
        map.put("used",used);
        return map;
    }

    @Test
    public void testGetNodes() throws InstanceNotFoundException
    {
        int threadCount = 20;
        int peakThreadCount = 30;

        setExpectationsForJmxServiceGetMemoryAttribute();
        when(_jmxService.getAttribute(any(JMXServiceURL.class),eq(JMXServiceImpl.JETTY_SERVER_MBEAN),eq("version"))).thenReturn(JETTY_VERSION);
        when(_jmxService.getAttribute(any(JMXServiceURL.class),eq(JMXServiceImpl.THREADING_MXBEAN),eq("ThreadCount"))).thenReturn(threadCount);
        when(_jmxService.getAttribute(any(JMXServiceURL.class),eq(JMXServiceImpl.THREADING_MXBEAN),eq("PeakThreadCount"))).thenReturn(peakThreadCount);
        Set<NodeJaxBean> nodes = _aggregateServiceImpl.getNodes();
        assertEquals("Expected 3 nodes",3,nodes.size());
        for (NodeJaxBean node : nodes)
        {
            assertTrue(node.getHeapUsed() > 0);
            if (NODE1.equals(node.getName()))
                assertEquals(_jmxServiceURLNode1,node.getJmxServiceURL());
            assertEquals("Jetty Version wrong",JETTY_VERSION,node.getJettyVersion());
            assertEquals("ThreadCount wrong",threadCount,node.getThreadCount());
            assertEquals("PeakThreadCount wrong",peakThreadCount,node.getPeakThreadCount());
        }
    }

    private void setExpectationsForJmxServiceGetMemoryAttribute() throws InstanceNotFoundException
    {
        when(_jmxNodeService.getNodes()).thenReturn(_jmxNodes);
        when(_jmxService.getAttribute(_jmxServiceURLNode1,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_HEAP)).thenReturn(_compositeDataHeapNode1);
        when(_jmxService.getAttribute(_jmxServiceURLNode2,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_HEAP)).thenReturn(_compositeDataHeapNode2);
        when(_jmxService.getAttribute(_jmxServiceURLNode3,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_HEAP)).thenReturn(_compositeDataHeapNode3);
    }

    @Test
    public void testGetAllAttributeValues() throws URISyntaxException, InstanceNotFoundException
    {
        setExpectationsForJmxServiceGetAttributes();
        setExpectationsForJmxServiceGetMemoryAttribute();
        when(_jmxService.getAttribute(_jmxServiceURLNode1,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_NONHEAP)).thenReturn(
                _compositeDataNonHeapNode1);
        when(_jmxService.getAttribute(_jmxServiceURLNode2,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_NONHEAP)).thenReturn(
                _compositeDataNonHeapNode2);
        when(_jmxService.getAttribute(_jmxServiceURLNode3,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_NONHEAP)).thenReturn(
                _compositeDataNonHeapNode3);
        when(_jmxService.getAttribute(_jmxServiceURLNode1,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION)).thenReturn(
                _objectsPendingFinalizationNode1);
        when(_jmxService.getAttribute(_jmxServiceURLNode2,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION)).thenReturn(
                _objectsPendingFinalizationNode2);
        when(_jmxService.getAttribute(_jmxServiceURLNode3,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION)).thenReturn(
                _objectsPendingFinalizationNode3);
        when(_jmxService.getAttribute(_jmxServiceURLNode1,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_VERBOSE)).thenReturn(
                _objectsPendingFinalizationNode1);
        when(_jmxService.getAttribute(_jmxServiceURLNode2,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_VERBOSE)).thenReturn(
                _objectsPendingFinalizationNode2);
        when(_jmxService.getAttribute(_jmxServiceURLNode3,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_VERBOSE)).thenReturn(
                _objectsPendingFinalizationNode3);

        MBeanAttributeValueJaxBeans attributes = _aggregateServiceImpl.getAllAttributeValues(_jmxNodes,
                JMXServiceImpl.MEMORY_MXBEAN);

        assertEquals("expected four values for each of two nodes and the local node",12,attributes.mBeanAttributeValueJaxBeans.size());
    }

    @Test
    public void testGetAttributeValues() throws InstanceNotFoundException
    {
        setExpectationsForJmxServiceGetMemoryAttribute();
        MBeanAttributeValueJaxBeans mBeanAttributeValuesJaxBean = _aggregateServiceImpl.getAttributeValues(_jmxNodes,JMXServiceImpl.MEMORY_MXBEAN,JMXServiceImpl.MEMORY_MXBEAN_HEAP);
        assertEquals(_jmxNodes.size(),mBeanAttributeValuesJaxBean.mBeanAttributeValueJaxBeans.size());
        for (MBeanAttributeValueJaxBean mBeanAttributeValueJaxBean : mBeanAttributeValuesJaxBean.mBeanAttributeValueJaxBeans)
        {
            if (NODE1.equals(mBeanAttributeValueJaxBean.nodeName))
                assertEquals(_compositeDataHeapNode1.toString(),mBeanAttributeValueJaxBean.value);
        }
    }

    @Test
    public void testGetAttributeValuesForJettyServerMBeanWithMultipleIds() throws InstanceNotFoundException
    {
        setExpectationsForGetObjectNamesByPrefix();
        when(_jmxService.getAttribute(any(JMXServiceURL.class),eq(JMXServiceImpl.JETTY_SERVER_MBEAN),eq("someAttributeName"))).thenReturn("someValue");
        when(_jmxService.getAttribute(any(JMXServiceURL.class),eq(JMXServiceImpl.JETTY_SERVER_MBEANID1),eq("someAttributeName"))).thenReturn("someOtherValue");
        MBeanAttributeValueJaxBeans mBeanAttributeValuesJaxBean = _aggregateServiceImpl.getAttributeValues(_jmxNodes,JMXServiceImpl.JETTY_SERVER_MBEAN,"someAttributeName");
        assertEquals(_jmxNodes.size() * _jettyServerObjectNames.size(),
                mBeanAttributeValuesJaxBean.mBeanAttributeValueJaxBeans.size());
    }

    @Test
    public void testGetOperationsMetaData() throws UriBuilderException, URISyntaxException, InstanceNotFoundException
    {
        setDefaultGetOperationsExpectations();

        when(_jmxService.getOperations(_jmxServiceURLNode3,JMXServiceImpl.MEMORY_MXBEAN)).thenReturn(prepareMBeanOperationInfoArray("operation1","desc 1"));

        MBeanOperationJaxBeans mBeanOperationsInfoJaxBean = _aggregateServiceImpl.getOperationsMetaData(_uriInfo,_jmxNodes,JMXServiceImpl.MEMORY_MXBEAN);

        assertEquals(1,mBeanOperationsInfoJaxBean.mBeanOperationJaxBeans.size());
    }

    @Test
    public void testGetOperationsMetaDataForServerBean() throws UriBuilderException, URISyntaxException, InstanceNotFoundException
    {
        MBeanOperationInfo[] mBeanOperationInfos = prepareMBeanOperationInfoArray("operation1","desc 1");
        setUriInfoExpectations();
        setExpectationsForGetObjectNamesByPrefix();
        when(_jmxService.getOperations(any(JMXServiceURL.class),eq(JMXServiceImpl.JETTY_SERVER_MBEAN))).thenReturn(mBeanOperationInfos);
        when(_jmxService.getOperations(any(JMXServiceURL.class),eq(JMXServiceImpl.JETTY_SERVER_MBEANID1))).thenReturn(null);

        MBeanOperationJaxBeans mBeanOperationsInfoJaxBean = _aggregateServiceImpl.getOperationsMetaData(_uriInfo,_jmxNodes,JMXServiceImpl.JETTY_SERVER_MBEAN);

        assertEquals(0,mBeanOperationsInfoJaxBean.mBeanOperationJaxBeans.size());
    }

    @Test
    public void testGetOperationsMetaDataForServerBeanWithMissingId() throws UriBuilderException, URISyntaxException, InstanceNotFoundException
    {
        MBeanOperationInfo[] mBeanOperationInfos = prepareMBeanOperationInfoArray("operation1","desc 1");
        setUriInfoExpectations();
        setExpectationsForGetObjectNamesByPrefix();
        when(_jmxService.getOperations(any(JMXServiceURL.class),eq(_jettyServerBeanPrefix))).thenThrow(new InstanceNotFoundException());
        when(_jmxService.getOperations(any(JMXServiceURL.class),eq(JMXServiceImpl.JETTY_SERVER_MBEAN))).thenReturn(mBeanOperationInfos);
        when(_jmxService.getOperations(any(JMXServiceURL.class),eq(JMXServiceImpl.JETTY_SERVER_MBEANID1))).thenReturn(mBeanOperationInfos);

        MBeanOperationJaxBeans mBeanOperationsInfoJaxBean = _aggregateServiceImpl.getOperationsMetaData(_uriInfo,_jmxNodes,_jettyServerBeanPrefix);

        assertEquals(1,mBeanOperationsInfoJaxBean.mBeanOperationJaxBeans.size());
    }

    @Test
    public void testGetOperationsMetaDataWithSlightlyDifferencesOnAThirdNode() throws UriBuilderException, URISyntaxException, InstanceNotFoundException
    {
        setDefaultGetOperationsExpectations();
        when(_jmxService.getOperations(_jmxServiceURLNode3,JMXServiceImpl.MEMORY_MXBEAN)).thenReturn(
                prepareMBeanOperationInfoArray("operation1","desc is different"));

        MBeanOperationJaxBeans mBeanOperationsInfoJaxBean = _aggregateServiceImpl.getOperationsMetaData(_uriInfo,_jmxNodes,JMXServiceImpl.MEMORY_MXBEAN);

        assertEquals(0,mBeanOperationsInfoJaxBean.mBeanOperationJaxBeans.size());
    }

    private void setDefaultGetOperationsExpectations() throws URISyntaxException, InstanceNotFoundException
    {
        MBeanOperationInfo[] mBeanOperationInfos = prepareMBeanOperationInfoArray("operation1","desc 1");
        addThirdNodeToLocalCloudtide();

        setUriInfoExpectations();
        when(_jmxService.getOperations(_jmxServiceURLNode1,JMXServiceImpl.MEMORY_MXBEAN)).thenReturn(mBeanOperationInfos);
        when(_jmxService.getOperations(_jmxServiceURLNode2,JMXServiceImpl.MEMORY_MXBEAN)).thenReturn(mBeanOperationInfos);

    }

    private void addThirdNodeToLocalCloudtide()
    {
        _jmxNodes.add(new JMXNode(NODE3,_jmxServiceURLNode3));
    }

    private MBeanOperationInfo[] prepareMBeanOperationInfoArray(String name, String description)
    {
        MBeanOperationInfo mBeanOperationInfo = new MBeanOperationInfo(name,description,null,"String",1);
        MBeanOperationInfo[] mBeanOperationInfos = new MBeanOperationInfo[]
        { mBeanOperationInfo };
        return mBeanOperationInfos;
    }

    @Test
    public void testGetAttributesMetaData() throws UriBuilderException, URISyntaxException, InstanceNotFoundException
    {
        setExpectationsForGetAttributesMetaDataTests();
        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,_jmxNodes,JMXServiceImpl.JETTY_SERVER_MBEAN);

        assertEquals(1,mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());
    }

    @Test
    public void testGetAttributesMetaDataWithMissingIdInObjectName() throws UriBuilderException, URISyntaxException, InstanceNotFoundException
    {
        setExpectationsForGetAttributesMetaDataTests();
        when(_jmxService.getAttributes(any(JMXServiceURL.class),eq(_jettyServerBeanPrefix))).thenThrow(new InstanceNotFoundException());
        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,_jmxNodes,_jettyServerBeanPrefix);

        assertEquals(1,mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());
    }

    @Test
    public void testGetAttributesMetaDataWithMissingAttributeOnOneNode() throws UriBuilderException, URISyntaxException, InstanceNotFoundException
    {
        setExpectationsForGetAttributesMetaDataTests();
        when(_jmxService.getAttributes(eq(_jmxServiceURLNode1),eq(JMXServiceImpl.JETTY_SERVER_MBEAN))).thenReturn(null);
        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,_jmxNodes,JMXServiceImpl.JETTY_SERVER_MBEAN);

        assertEquals(0,mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());
    }

    private void setExpectationsForGetAttributesMetaDataTests() throws URISyntaxException, InstanceNotFoundException
    {
        MBeanAttributeInfo[] mBeanAttributeInfos = prepareMBeanAttributeInfoArrayForJettyServerMBean();
        setExpectationsForGetObjectNamesByPrefix();
        setUriInfoExpectations();
        when(_jmxService.getAttributes(any(JMXServiceURL.class),eq(JMXServiceImpl.JETTY_SERVER_MBEAN))).thenReturn(mBeanAttributeInfos);
        when(_jmxService.getAttributes(any(JMXServiceURL.class),eq(JMXServiceImpl.JETTY_SERVER_MBEANID1))).thenReturn(mBeanAttributeInfos);
    }

    private void setExpectationsForGetObjectNamesByPrefix()
    {
        when(_jmxService.getObjectNamesByPrefix(any(JMXServiceURL.class),eq(_jettyServerBeanPrefix))).thenReturn(_jettyServerObjectNames);
    }

    private MBeanAttributeInfo[] prepareMBeanAttributeInfoArrayForJettyServerMBean()
    {
        MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo("Name","Type","description",true,false,false);
        return new MBeanAttributeInfo[]
        { mBeanAttributeInfo };
    }

    @Test
    public void testGetAttributesMetaDataWithSlightlyDifferentMetadataOnBothNodes() throws UriBuilderException, URISyntaxException, InstanceNotFoundException
    {
        setExpectationsForJmxServiceGetAttributes();
        when(_jmxService.getAttributes(_jmxServiceURLNode2,JMXServiceImpl.MEMORY_MXBEAN)).thenReturn(prepareMBeanAttributeInfoArrayForMemoryMXBean(false));

        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,_jmxNodes,JMXServiceImpl.MEMORY_MXBEAN);

        assertEquals(0,mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());
    }

    @Test
    public void testGetAttributesMetaDataWithBeanAvailableOnlyOnOneOfTwoNodes() throws UriBuilderException, URISyntaxException, InstanceNotFoundException
    {
        setExpectationsForJmxServiceGetAttributes();
        when(_jmxService.getAttributes(_jmxServiceURLNode2,JMXServiceImpl.MEMORY_MXBEAN)).thenReturn(null);

        MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = _aggregateServiceImpl.getAttributesMetaData(_uriInfo,_jmxNodes,JMXServiceImpl.MEMORY_MXBEAN);

        assertEquals(0,mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());

    }

    private MBeanAttributeInfo[] prepareMBeanAttributeInfoArrayForMemoryMXBean(boolean isReadable)
    {
        MBeanAttributeInfo mBeanAttributeInfoHeap = new MBeanAttributeInfo(JMXServiceImpl.MEMORY_MXBEAN_HEAP,"type","description",isReadable,false,false);
        MBeanAttributeInfo mBeanAttributeInfoNonHeap = new MBeanAttributeInfo(JMXServiceImpl.MEMORY_MXBEAN_NONHEAP,"type","description",isReadable,false,false);
        MBeanAttributeInfo mBeanAttributeInfoObjectPending = new MBeanAttributeInfo(JMXServiceImpl.MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION,"type",
                "description",isReadable,false,false);
        MBeanAttributeInfo mBeanAttributeInfoVerbose = new MBeanAttributeInfo(JMXServiceImpl.MEMORY_MXBEAN_VERBOSE,"type","description",isReadable,false,false);
        MBeanAttributeInfo[] mBeanAttributeInfos = new MBeanAttributeInfo[]
        { mBeanAttributeInfoHeap, mBeanAttributeInfoNonHeap, mBeanAttributeInfoObjectPending, mBeanAttributeInfoVerbose };
        return mBeanAttributeInfos;
    }

    private void setExpectationsForJmxServiceGetAttributes() throws URISyntaxException, InstanceNotFoundException
    {
        MBeanAttributeInfo[] mBeanAttributeInfos = prepareMBeanAttributeInfoArrayForMemoryMXBean(true);

        setUriInfoExpectations();
        when(_jmxService.getAttributes(any(JMXServiceURL.class),eq(JMXServiceImpl.MEMORY_MXBEAN))).thenReturn(mBeanAttributeInfos);
    }

    @Test
    public void testGetObjectNames() throws MalformedObjectNameException, NullPointerException, URISyntaxException
    {
        ObjectName aThirdCommonObjectName = new ObjectName(JMXServiceImpl.JETTY_SERVER_MBEAN);
        Set<ObjectName> commonObjectNames = getCommonObjectNameSet();
        commonObjectNames.add(aThirdCommonObjectName);

        addThirdNodeToLocalCloudtide();

        setCommonExpectationsForGetObjectNameTests(commonObjectNames);
        when(_jmxService.getObjectNames(_jmxServiceURLNode2)).thenReturn(commonObjectNames);

        MBeanShortJaxBeans mBeanShortJaxBeans = _aggregateServiceImpl.getMBeanShortJaxBeans(_uriInfo,_jmxNodes);

        assertEquals("Expected to get three common mBeanShortJaxBeans as they exist on all nodes",3,mBeanShortJaxBeans.mbeans.size());
    }

    @Test
    public void testGetObjectNamesWithAnAdditionalObjectNameOnOneNode() throws MalformedObjectNameException, NullPointerException, URISyntaxException
    {
        ObjectName onlyOnOneNodeObjectName = new ObjectName(JMXServiceImpl.JETTY_SERVER_MBEAN);

        Set<ObjectName> commonObjectNames = getCommonObjectNameSet();
        Set<ObjectName> commonObjectNamesPlusThreading = new HashSet<ObjectName>();
        commonObjectNamesPlusThreading.addAll(commonObjectNames);
        commonObjectNamesPlusThreading.add(onlyOnOneNodeObjectName);

        addThirdNodeToLocalCloudtide();

        setCommonExpectationsForGetObjectNameTests(commonObjectNames);
        when(_jmxService.getObjectNames(_jmxServiceURLNode2)).thenReturn(commonObjectNamesPlusThreading);

        MBeanShortJaxBeans mBeanShortJaxBeans = _aggregateServiceImpl.getMBeanShortJaxBeans(_uriInfo,_jmxNodes);

        assertEquals("Expected to get two common mBeanShortJaxBeans as the THREADING_MXBEAN only exists on one node",2,mBeanShortJaxBeans.mbeans.size());
    }

    private void setCommonExpectationsForGetObjectNameTests(Set<ObjectName> commonObjectNames) throws URISyntaxException
    {
        setUriInfoExpectations();
        when(_jmxService.getObjectNames(_jmxServiceURLNode1)).thenReturn(commonObjectNames);
        when(_jmxService.getObjectNames(_jmxServiceURLNode3)).thenReturn(commonObjectNames);
    }

    private Set<ObjectName> getCommonObjectNameSet() throws MalformedObjectNameException
    {
        ObjectName commonToAllNodesObjectName = new ObjectName(JMXServiceImpl.MEMORY_MXBEAN);
        ObjectName anotherCommonToAllNodesObjectName = new ObjectName(JMXServiceImpl.THREADING_MXBEAN);
        Set<ObjectName> commonObjectNames = new HashSet<ObjectName>();
        commonObjectNames.add(commonToAllNodesObjectName);
        commonObjectNames.add(anotherCommonToAllNodesObjectName);
        return commonObjectNames;
    }

    private void setUriInfoExpectations() throws URISyntaxException
    {
        when(_uriInfo.getAbsolutePathBuilder()).thenReturn(_uriBuilder);
        when(_uriBuilder.path(any(String.class))).thenReturn(_uriBuilder);
        when(_uriBuilder.build()).thenReturn(new URI("http://testuri/test"));
    }

    @Test
    public void testInvokeOperation()
    {
        _aggregateServiceImpl.invokeOperation(_jmxNodes,JMXServiceImpl.MEMORY_MXBEAN,"gc");
        verify(_jmxService,times(3)).invoke(any(JMXServiceURL.class),eq(JMXServiceImpl.MEMORY_MXBEAN),eq("gc"),any(Object[].class),any(String[].class));
    }

    @Test
    public void testInvokeOperationWithParameters()
    {
        String stringSignature = "java.lang.String";

        Object[] params = new Object[]
        { "com.intalio", "FINEST" };

        String[] signature = new String[]
        { stringSignature, stringSignature };

        _aggregateServiceImpl.invokeOperation(_jmxNodes,JMXServiceImpl.LOGGING_MBEAN,"setLoggerLevel",params,
                signature);
        verify(_jmxService,times(3)).invoke(any(JMXServiceURL.class),eq(JMXServiceImpl.LOGGING_MBEAN),eq("setLoggerLevel"),aryEq(params),aryEq(signature));
    }

}
