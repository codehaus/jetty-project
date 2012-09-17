package org.mortbay.jetty.plugin;

import java.io.File;

import org.eclipse.jetty.annotations.AbstractDiscoverableAnnotationHandler;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.annotations.AnnotationParser.DiscoverableAnnotationHandler;
import org.eclipse.jetty.annotations.ClassNameResolver;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.MetaData;
import org.eclipse.jetty.webapp.WebAppContext;

public class MavenAnnotationConfiguration extends AnnotationConfiguration
{
    private static final Logger LOG = Log.getLogger(MavenAnnotationConfiguration.class);

    /* ------------------------------------------------------------ */
    @Override
    public void parseWebInfClasses(final WebAppContext context, final AnnotationParser parser) throws Exception
    {
        JettyWebAppContext jwac = (JettyWebAppContext)context;
       if (jwac.getClassPathFiles() == null || jwac.getClassPathFiles().size() == 0)
            super.parseWebInfClasses (context, parser);
        else
        {
            LOG.debug("Scanning classes ");
            //Look for directories on the classpath and process each one of those
            
            MetaData metaData = context.getMetaData();
            if (metaData == null)
               throw new IllegalStateException ("No metadata");

            parser.clearHandlers();
            for (DiscoverableAnnotationHandler h:_discoverableAnnotationHandlers)
            {
                if (h instanceof AbstractDiscoverableAnnotationHandler)
                    ((AbstractDiscoverableAnnotationHandler)h).setResource(null); //
            }
            parser.registerHandlers(_discoverableAnnotationHandlers);
            parser.registerHandler(_classInheritanceHandler);
            parser.registerHandlers(_containerInitializerAnnotationHandlers);


            for (File f:jwac.getClassPathFiles())
            {
                //scan the equivalent of the WEB-INF/classes directory that has been synthesised by the plugin
                if (f.isDirectory() && f.exists())
                {
                    doParse(context, parser, Resource.newResource(f.toURL()));
                }
            }
            
            //if an actual WEB-INF/classes directory also exists (eg because of overlayed wars) then scan that
            //too
            Resource classesDir = context.getWebInf().addPath("classes/");
            if (classesDir.exists())
            {
                    doParse(context, parser, classesDir);
            }
        }
    }
    
    
    public void doParse (final WebAppContext context, final AnnotationParser parser, Resource resource)
    throws Exception
    { 
            parser.parse(resource, new ClassNameResolver()
            {
                public boolean isExcluded (String name)
                {
                    if (context.isSystemClass(name)) return true;
                    if (context.isServerClass(name)) return false;
                    return false;
                }

                public boolean shouldOverride (String name)
                {
                    //looking at webapp classpath, found already-parsed class of same name - did it come from system or duplicate in webapp?
                    if (context.isParentLoaderPriority())
                        return false;
                    return true;
                }
            });            
    }
}
