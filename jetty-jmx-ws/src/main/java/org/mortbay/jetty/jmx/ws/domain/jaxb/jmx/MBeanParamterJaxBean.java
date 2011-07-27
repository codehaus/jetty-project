package org.mortbay.jetty.jmx.ws.domain.jaxb.jmx;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Parameter")
public class MBeanParamterJaxBean implements Comparable<MBeanParamterJaxBean>
{
    @XmlElement(name = "ParameterName")
    public String name;
    @XmlElement(name = "Description")
    public String description;
    @XmlElement(name = "Type")
    public String type;

    public MBeanParamterJaxBean()
    {
    }

    public MBeanParamterJaxBean(String name, String description, String type)
    {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MBeanParamterJaxBean [name=");
        builder.append(name);
        builder.append(", type=");
        builder.append(type);
        builder.append("]");
        return builder.toString();
    }

    public int compareTo(MBeanParamterJaxBean o)
    {
        return this.name.compareTo(o.name);
    }

}