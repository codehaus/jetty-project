//========================================================================
//$Id$
//Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.jetty.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.plugin.util.Scanner;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.xml.XmlConfiguration;

/**
 * Jetty6RunWarExploded
 *
 *
 *@goal run-exploded
 *@execute phase=package
 */
public class Jetty6RunWarExploded extends AbstractJetty6Mojo
{

    /**
     * List of connectors to use. If none are configured
     * then we use a single SelectChannelConnector at port 8080
     * 
     * @parameter 
     */
    private Connector[] connectors;
    
    
    /**
     * The location of the war file.
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private File webApp;

    
    /**
     * An optional jetty xml configuration file.
     * @parameter
     */
    private String jettyConfig;
  
    
    /**
     * List of security realms to set up. Optional.
     * @parameter
     */
    private UserRealm[] userRealms;
    
    
    /**
     * @see org.mortbay.jetty.plugin.AbstractJettyMojo#getConfiguredUserRealms()
     */
    public Object[] getConfiguredUserRealms()
    {
        return this.userRealms;
    }

    /**
     * @see org.mortbay.jetty.plugin.AbstractJettyMojo#getConfiguredConnectors()
     */
    public Object[] getConfiguredConnectors()
    {
        return this.connectors;
    }

    /**
     * 
     * @see org.mortbay.jetty.plugin.AbstractJettyMojo#checkPomConfiguration()
     */
    public void checkPomConfiguration() throws MojoExecutionException
    {
        return;
    }

    /**
     * @see org.mortbay.jetty.plugin.AbstractJettyMojo#configureScanner()
     */
    public void configureScanner() throws MojoExecutionException
    {
        final ArrayList scanList = new ArrayList();
        scanList.add(getProject().getFile());
        File webInfDir = new File(webApp,"WEB-INF");
        scanList.add(new File(webInfDir, "web.xml"));
        scanList.add(new File(webInfDir, "classes"));
        scanList.add(new File(webInfDir, "lib"));
        setScanList(scanList);
        
        ArrayList listeners = new ArrayList();
        listeners.add(new Scanner.Listener()
        {
            public void changesDetected(Scanner scanner, List changes)
            {
                try
                {
                    getLog().info("Stopping webapp ...");
                    getWebApplication().stop();
                    getLog().info("Reconfiguring webapp ...");

                    checkPomConfiguration();
                    
                    // check if we need to reconfigure the scanner,
                    // which is if the pom changes
                    if (changes.contains(getProject().getFile().getCanonicalPath()))
                    {
                        getLog().info("Reconfiguring scanner after change to pom.xml ...");
                        ArrayList scanList = getScanList();
                        scanList.clear();
                        scanList.add(getProject().getFile());
                        File webInfDir = new File(webApp,"WEB-INF");
                        scanList.add(new File(webInfDir, "web.xml"));
                        scanList.add(new File(webInfDir, "classes"));
                        scanList.add(new File(webInfDir, "lib"));
                        setScanList(scanList);
                        scanner.setRoots(scanList);
                    }

                    getLog().info("Restarting webapp ...");
                    getWebApplication().start();
                    getLog().info("Restart completed.");
                }
                catch (Exception e)
                {
                    getLog().error("Error reconfiguring/restarting webapp after change in watched files",e);
                }
            }
        });
        setScannerListeners(listeners);
    }


    /* (non-Javadoc)
     * @see org.mortbay.jetty.plugin.util.AbstractJettyMojo#finishConfigurationBeforeStart()
     */
    public void finishConfigurationBeforeStart() throws Exception
    {
        if (jettyConfig == null)
            return;
        
        getLog().info( "Configuring Jetty from xml configuration file: " + jettyConfig );
        XmlConfiguration xmlConfiguration = new XmlConfiguration(jettyConfig);
        xmlConfiguration.configure(getServer().getProxiedObject());   
    }

    public void configureWebApplication () throws Exception
    {
        super.configureWebApplication();
        
        getWebApplication().setWebAppSrcDir(webApp);
    }
    
    public void execute () throws MojoExecutionException, MojoFailureException
    {
        super.execute();
    }
    
}
