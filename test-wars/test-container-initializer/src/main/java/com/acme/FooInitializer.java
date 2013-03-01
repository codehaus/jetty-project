package com.acme;

import java.util.Set;
import java.util.ArrayList;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.HandlesTypes;
import javax.servlet.ServletContainerInitializer;

@HandlesTypes ({javax.servlet.Servlet.class, Foo.class})
public class FooInitializer implements ServletContainerInitializer
{
    public static class BarListener implements ServletContextListener
    {

        /** 
         * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
         */
        @Override
        public void contextInitialized(ServletContextEvent sce)
        {
            throw new IllegalStateException("BAR LISTENER CALLED!");
        }

        /** 
         * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
         */
        @Override
        public void contextDestroyed(ServletContextEvent sce)
        {
            
        }
        
    }

    public static class FooListener implements ServletContextListener
    {

        /** 
         * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
         */
        @Override
        public void contextInitialized(ServletContextEvent sce)
        {
            //Can add a ServletContextListener from a ServletContainerInitializer
            sce.getServletContext().setAttribute("com.acme.AnnotationTest.listenerTest", Boolean.TRUE);
            
            //Can't add a ServletContextListener from a ServletContextListener
            try
            {
                sce.getServletContext().addListener(new BarListener());
                sce.getServletContext().setAttribute("com.acme.AnnotationTest.listenerRegoTest", Boolean.FALSE);
            }
            catch (UnsupportedOperationException e)
            {
                sce.getServletContext().setAttribute("com.acme.AnnotationTest.listenerRegoTest", Boolean.TRUE);
            }
            catch (Exception e)
            {
                sce.getServletContext().setAttribute("com.acme.AnnotationTest.listenerRegoTest", Boolean.FALSE);
            }
        }

        /** 
         * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
         */
        @Override
        public void contextDestroyed(ServletContextEvent sce)
        {
            
        }
    }
        

    public void onStartup(Set<Class<?>> classes, ServletContext context)
    {
        context.setAttribute("com.acme.Foo", new ArrayList<Class>(classes));
        ServletRegistration.Dynamic reg = context.addServlet("AnnotationTest", "com.acme.AnnotationTest");
        context.setAttribute("com.acme.AnnotationTest.complete", (reg == null));
        context.addListener(new FooListener());
    }
}
