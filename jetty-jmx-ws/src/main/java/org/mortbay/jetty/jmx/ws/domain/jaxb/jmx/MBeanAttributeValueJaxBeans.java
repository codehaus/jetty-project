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

import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/* ------------------------------------------------------------ */
/**
 */
@XmlRootElement
public class MBeanAttributeValueJaxBeans
{
    @XmlElement(name = "Attribute")
    public Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans = new TreeSet<MBeanAttributeValueJaxBean>();

    public MBeanAttributeValueJaxBeans()
    {
    }

    public MBeanAttributeValueJaxBeans(Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans)
    {
        this.mBeanAttributeValueJaxBeans = mBeanAttributeValueJaxBeans;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MBeanAttributeValuesJaxBean [mBeanAttributeValueJaxBeans=");
        builder.append(mBeanAttributeValueJaxBeans);
        builder.append("]");
        return builder.toString();
    }
}
