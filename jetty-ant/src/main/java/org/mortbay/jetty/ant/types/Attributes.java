package org.mortbay.jetty.ant.types;

import java.util.ArrayList;
import java.util.List;

public class Attributes
{

    List _attributes = new ArrayList();
    
    public void addAttribute(Attribute attr )
    {
        _attributes.add(attr);
    }
    
    public List getAttributes()
    {
        return _attributes;
    }
}
