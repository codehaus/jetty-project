package org.mortbay.jetty.jmx.ws.web;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import org.mortbay.jetty.jmx.ws.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext>
{

    private JAXBContext context;
    private Class<?>[] types =    { MBeanAttributeValueJaxBeans.class };

    public JAXBContextResolver() throws Exception
    {
        JSONConfiguration config = JSONConfiguration.mapped().arrays("Attribute").build();
        context = new JSONJAXBContext(config,types);
    }

    public JAXBContext getContext(Class<?> objectType)
    {
        for (Class<?> type : types)
        {
            if (type == objectType)
            {
                return context;
            }
        }
        return null;
    }
}
