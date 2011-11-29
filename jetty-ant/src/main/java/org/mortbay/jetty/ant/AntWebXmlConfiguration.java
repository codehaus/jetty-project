// ========================================================================
// Copyright 2006-2007 Sabre Holdings.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty.ant;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;

import org.mortbay.jetty.ant.utils.TaskLog;
import org.apache.tools.ant.AntClassLoader;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Descriptor;
import org.eclipse.jetty.webapp.StandardDescriptorProcessor;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlParser.Node;


/**
 * This configuration object provides additional way to inject application
 * properties into the configured web application. The list of classpath files,
 * the application base directory and web.xml file could be specified in this
 * way.
 *
 * @author Jakub Pawlowicz
 * @author Athena Yao
 */
public class AntWebXmlConfiguration extends WebXmlConfiguration
{
    private static final Logger LOG = Log.getLogger(WebXmlConfiguration.class);

    public class StandardDescriptorProcessorForAnt extends StandardDescriptorProcessor
    {
        @Override
        public void visitContextParam(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitContextParam(context, descriptor, node);
        }

        @Override
        public void visitDisplayName(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitDisplayName(context, descriptor, node);
        }

        @Override
        public void visitServlet(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitServlet(context, descriptor, node);
        }

        @Override
        public void visitServletMapping(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitServletMapping(context, descriptor, node);
        }

        @Override
        public void visitSessionConfig(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitSessionConfig(context, descriptor, node);
        }

        @Override
        public void visitMimeMapping(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitMimeMapping(context, descriptor, node);
        }

        @Override
        public void visitWelcomeFileList(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitWelcomeFileList(context, descriptor, node);
        }

        @Override
        public void visitLocaleEncodingList(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitLocaleEncodingList(context, descriptor, node);
        }

        @Override
        public void visitErrorPage(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitErrorPage(context, descriptor, node);
        }

        @Override
        public void visitTagLib(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitTagLib(context, descriptor, node);
        }

        @Override
        public void visitJspConfig(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitJspConfig(context, descriptor, node);
        }

        @Override
        public void visitSecurityConstraint(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitSecurityConstraint(context, descriptor, node);
        }

        @Override
        public void visitLoginConfig(WebAppContext context, Descriptor descriptor, Node node) throws Exception
        {
            super.visitLoginConfig(context, descriptor, node);
        }

        @Override
        public void visitSecurityRole(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitSecurityRole(context, descriptor, node);
        }

        @Override
        public void visitFilter(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitFilter(context, descriptor, node);
        }

        @Override
        public void visitFilterMapping(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitFilterMapping(context, descriptor, node);
        }

        @Override
        public void visitListener(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitListener(context, descriptor, node);
        }

        @Override
        public void visitDistributable(WebAppContext context, Descriptor descriptor, Node node)
        {
            super.visitDistributable(context, descriptor, node);
        }

        @Override
        public String getSystemClassPath(WebAppContext context)
        {
            
            ClassLoader loader = context.getClassLoader();
            if (loader.getParent() != null)
                loader = loader.getParent();
  
            StringBuilder classpath=new StringBuilder();
            
            while (loader != null)
            {
                if (loader instanceof URLClassLoader)
                {
                    URL[] urls = ((URLClassLoader)loader).getURLs();
                    if (urls != null)
                    {     
                        for (int i=0;i<urls.length;i++)
                        {
                            try
                            {
                                Resource resource = context.newResource(urls[i]);
                                File file=resource.getFile();
                                if (file!=null && file.exists())
                                {
                                    if (classpath.length()>0)
                                        classpath.append(File.pathSeparatorChar);
                                    classpath.append(file.getAbsolutePath());
                                }
                            }
                            catch (IOException e)
                            {
                                LOG.debug(e);
                            }
                        }
                    }
                }
                else if (loader instanceof AntClassLoader)
                {
                    classpath.append(((AntClassLoader)loader).getClasspath());
                }
                
                loader = loader.getParent();
            }
            return classpath.toString();
        }
        
    }
    
    
    
    
    /** List of classpath files. */
    private List classPathFiles;

    /** Web application root directory. */
    private File webAppBaseDir;

    /** Web application web.xml file. */
    private File webXmlFile;

    private File webDefaultXmlFile;

    public AntWebXmlConfiguration() throws ClassNotFoundException
    {
    }

    public File getWebDefaultXmlFile()
    {
        return this.webDefaultXmlFile;
    }

    public void setWebDefaultXmlFile(File webDefaultXmlfile)
    {
        this.webDefaultXmlFile = webDefaultXmlfile;
    }

    public void setClassPathFiles(List classPathFiles)
    {
        this.classPathFiles = classPathFiles;
    }

    public void setWebAppBaseDir(File webAppBaseDir)
    {
        this.webAppBaseDir = webAppBaseDir;
    }

    public void setWebXmlFile(File webXmlFile)
    {
        this.webXmlFile = webXmlFile;

        if (webXmlFile.exists())
        {
            TaskLog.log("web.xml file = " + webXmlFile);
        }
    }

    /**
     * Adds classpath files into web application classloader, and
     * sets web.xml and base directory for the configured web application.
     *
     * @see WebXmlConfiguration#configure(WebAppContext)
     */
    public void configure(WebAppContext context) throws Exception
    {
        if (context.isStarted())
        {
            TaskLog.log("Cannot configure webapp after it is started");
            return;
        }
       

        if (webXmlFile.exists())
        {
            context.setDescriptor(webXmlFile.getCanonicalPath());
        }
        
        context.getMetaData().addDescriptorProcessor(new StandardDescriptorProcessorForAnt());
        //super.configure(context);

        Iterator filesIterator = classPathFiles.iterator();

        while (filesIterator.hasNext())
        {
            File classPathFile = (File) filesIterator.next();
            if (classPathFile.exists())
            {
                ((WebAppClassLoader) context.getClassLoader())
                        .addClassPath(classPathFile.getCanonicalPath());
            }
        }
    }    
}
