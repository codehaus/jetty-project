// ========================================================================
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

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/* ------------------------------------------------------------ */
/**
 */
@XmlRootElement(name = "MBeans")
public class MBeanShortJaxBean implements Comparable<MBeanShortJaxBean>
{
    @XmlElement(name = "ObjectName")
    public String objectName;
    @XmlElement(name = "URL")
    public URI url;

    public MBeanShortJaxBean()
    {
    }

    public MBeanShortJaxBean(UriInfo uriInfo, String objectName)
    {
        this.objectName = objectName;
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path(objectName);
        uriBuilder.replaceQueryParam("");
        this.url = uriBuilder.build();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MBeanShortJaxBean [objectName=");
        builder.append(objectName);
        builder.append(", url=");
        builder.append(url);
        builder.append("]");
        return builder.toString();
    }

    public int compareTo(MBeanShortJaxBean o)
    {
       return this.objectName.compareTo(o.objectName);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((objectName == null)?0:objectName.hashCode());
        result = prime * result + ((url == null)?0:url.hashCode());
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
        MBeanShortJaxBean other = (MBeanShortJaxBean)obj;
        if (objectName == null)
        {
            if (other.objectName != null)
                return false;
        }
        else if (!objectName.equals(other.objectName))
            return false;
        if (url == null)
        {
            if (other.url != null)
                return false;
        }
        else if (!url.equals(other.url))
            return false;
        return true;
    }

}
