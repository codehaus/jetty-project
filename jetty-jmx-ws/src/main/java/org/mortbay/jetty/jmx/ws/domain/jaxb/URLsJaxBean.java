package org.mortbay.jetty.jmx.ws.domain.jaxb;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Node")
public class URLsJaxBean implements Comparable<URLsJaxBean>
{

    @XmlElement(name = "nodeName")
    public String nodeName;
    @XmlElement(name = "URL")
    public URI uri;

    public URLsJaxBean()
    {
    }

    public URLsJaxBean(UriInfo uriInfo, String nodeName, String path)
    {
        this.nodeName = nodeName;
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        this.uri = uriBuilder.build();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("URLsJaxBean [nodeName=");
        builder.append(nodeName);
        builder.append(", uri=");
        builder.append(uri);
        builder.append("]");
        return builder.toString();
    }

    public int compareTo(URLsJaxBean o)
    {
        return nodeName.compareTo(o.nodeName);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodeName == null)?0:nodeName.hashCode());
        result = prime * result + ((uri == null)?0:uri.hashCode());
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
        URLsJaxBean other = (URLsJaxBean)obj;
        if (nodeName == null)
        {
            if (other.nodeName != null)
                return false;
        }
        else if (!nodeName.equals(other.nodeName))
            return false;
        if (uri == null)
        {
            if (other.uri != null)
                return false;
        }
        else if (!uri.equals(other.uri))
            return false;
        return true;
    }

}