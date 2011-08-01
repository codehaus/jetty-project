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

package org.mortbay.jetty.jmx.ws.domain.jaxb.jmx;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/* ------------------------------------------------------------ */
/**
 */
@XmlRootElement(name = "MBeanOperations")
public class MBeanOperationJaxBeans
{

    @XmlElement(name = "ObjectName")
    public String objectName;
    @XmlElement(name = "Operation")
    public Set<MBeanOperationJaxBean> mBeanOperationJaxBeans = new HashSet<MBeanOperationJaxBean>();

    public MBeanOperationJaxBeans()
    {
    }

    public MBeanOperationJaxBeans(String objectName, Set<MBeanOperationJaxBean> mBeanOperationJaxBeans)
    {
        this.objectName = objectName;
        this.mBeanOperationJaxBeans = mBeanOperationJaxBeans;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MBeanOperationsJaxBean [objectName=");
        builder.append(objectName);
        builder.append(", operations=");
        builder.append(mBeanOperationJaxBeans);
        builder.append("]");
        return builder.toString();
    }

}
