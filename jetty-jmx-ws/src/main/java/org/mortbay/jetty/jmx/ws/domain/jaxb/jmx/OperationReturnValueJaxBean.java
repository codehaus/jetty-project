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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/* ------------------------------------------------------------ */
/**
 */
@XmlRootElement
public class OperationReturnValueJaxBean implements Comparable<OperationReturnValueJaxBean>
{

    @XmlElement(name = "NodeName")
    public String nodeName;
    public String returnValue;

    public OperationReturnValueJaxBean()
    {
    }

    public OperationReturnValueJaxBean(String nodeName, Object object)
    {
        this.nodeName = nodeName;
        this.returnValue = object == null?"success":object.toString();
    }

    public int compareTo(OperationReturnValueJaxBean o)
    {
        return nodeName.compareTo(o.nodeName);
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("OperationReturnValueJaxBean [nodeName=");
        builder.append(nodeName);
        builder.append(", returnValue=");
        builder.append(returnValue);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodeName == null)?0:nodeName.hashCode());
        result = prime * result + ((returnValue == null)?0:returnValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OperationReturnValueJaxBean other = (OperationReturnValueJaxBean)obj;
        if (nodeName == null)
        {
            if (other.nodeName != null)
                return false;
        }
        else if (!nodeName.equals(other.nodeName))
            return false;
        if (returnValue == null)
        {
            if (other.returnValue != null)
                return false;
        }
        else if (!returnValue.equals(other.returnValue))
            return false;
        return true;
    }
}
