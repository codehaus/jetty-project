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

package org.mortbay.jetty.jmx.ws.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.mortbay.jetty.jmx.ws.domain.JMXNode;
import org.mortbay.jetty.jmx.ws.service.JMXNodeService;
import org.mortbay.jetty.jmx.ws.service.impl.JMXNodeServiceImpl;



/* ------------------------------------------------------------ */
/**
 */
public class FilterNodesUtils
{
    //TODO: IOC
    private static JMXNodeService jmxNodeService = new JMXNodeServiceImpl();
    
    /**
     * @return all known nodes if nodes param == null, otherwise filters known nodes by nodeNames in nodes param
     */
    public static Collection<JMXNode> getNodesToAggregate(String nodes)
    {
        Collection<JMXNode> jmxNodes = jmxNodeService.getNodes();
        if (nodes != null)
        {
            List<String> nodeList = Arrays.asList(nodes.split(","));
            Collection<JMXNode> nodesToCollect = new HashSet<JMXNode>();
            for (JMXNode jmxNode : jmxNodes)
            {
                if (nodeList.contains(jmxNode.getNodeName()))
                    nodesToCollect.add(jmxNode);
            }
            return nodesToCollect;
        }
        return jmxNodes;
    }
}
