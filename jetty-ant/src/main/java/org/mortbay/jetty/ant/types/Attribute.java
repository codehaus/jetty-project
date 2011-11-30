package org.mortbay.jetty.ant.types;

public class Attribute
{

    String name;
    
    String value;
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setValue(String value)
    {
        this.value = value;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getValue()
    {
        return value;
    }
}
