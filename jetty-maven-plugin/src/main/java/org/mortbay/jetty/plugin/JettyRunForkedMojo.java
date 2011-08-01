// ========================================================================
// Copyright (c) Webtide LLC
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

package org.mortbay.jetty.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.util.IO;
import org.mortbay.jetty.runner.Runner;

/**
 * <p>
 *  This goal is used to assemble your webapp into a war and automatically deploy it to Jetty in a forked JVM.
 *  </p>
 *  <p>
 *  Once invoked, the plugin can be configured to run continuously, scanning for changes in the project and to the
 *  war file and automatically performing a 
 *  hot redeploy when necessary. 
 *  </p>
 *  <p>
 *  You may also specify the location of a jetty.xml file whose contents will be applied before any plugin configuration.
 *  This can be used, for example, to deploy a static webapp that is not part of your maven build. 
 *  </p>
 *  <p>
 *  There is a <a href="run-war-mojo.html">reference guide</a> to the configuration parameters for this plugin, and more detailed information
 *  with examples in the <a href="http://docs.codehaus.org/display/JETTY/Maven+Jetty+Plugin/">Configuration Guide</a>.
 *  </p>
 * 
 * @goal run-forked
 * @requiresDependencyResolution runtime
 * @execute phase="package"
 * @description Runs Jetty in forked JVM on a war file
 *
 */
public class JettyRunForkedMojo extends AbstractMojo
{
    /**
     * The maven project.
     *
     * @parameter expression="${executedProject}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Location of a jetty xml configuration file whose contents 
     * will be applied before any plugin configuration. Optional.
     * @parameter
     */
    private String jettyXml;
    
    /**
     * The context path for the webapp. Defaults to the
     * name of the webapp's artifact.
     *
     * @parameter expression="/${project.artifactId}"
     * @required
     * @readonly
     */
    private String contextPath;

    /**
     * The location of the war file.
     * @parameter expression="${project.build.directory}/${project.build.finalName}.war"
     * @required
     */
    private File webApp;

    /**
     * Location of a context xml configuration file whose contents
     * will be applied to the webapp AFTER anything in &lt;webAppConfig&gt;.Optional.
     * @parameter
     */
    private String webAppXml;

    /**
     * A RequestLog implementation to use for the webapp at runtime.
     * Optional.
     * @parameter
     */
    protected RequestLog requestLog;
    
    /**  
     * @parameter expression="${jetty.skip}" default-value="false"
     */
    private boolean skip;

    /**
     * Port to listen to stop jetty on executing -DSTOP.PORT=&lt;stopPort&gt; 
     * -DSTOP.KEY=&lt;stopKey&gt; -jar start.jar --stop
     * @parameter
     */
    protected int stopPort;
    
    /**
     * Key to provide when stopping jetty on executing java -DSTOP.KEY=&lt;stopKey&gt; 
     * -DSTOP.PORT=&lt;stopPort&gt; -jar start.jar --stop
     * @parameter
     */
    protected String stopKey;

    /**
     * @parameter expression="${plugin.artifacts}"
     * @readonly
     */
    private List pluginArtifacts;
    
    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        getLog().info("Configuring Jetty for project: " + project.getName());
        if (skip)
        {
            getLog().info("Skipping Jetty start: jetty.skip==true");
            return;
        }
        PluginLog.setLog(getLog());
        checkPomConfiguration();
        startJettyRunner();
    }

    /* ------------------------------------------------------------ */
    public void startJettyRunner() throws MojoExecutionException
    {
        Process process = null;
        
        try
        {
            List<String> cmd = new ArrayList<String>();
            cmd.add(getJavaBin());
            
            String classPath = getClassPath();
            if (classPath != null && classPath.length() > 0)
            {
                cmd.add("-cp");
                cmd.add(classPath);
            }
            cmd.add(Runner.class.getCanonicalName());
            
            if (stopPort > 0 && stopKey != null)
            {
                cmd.add("--stop-port");
                cmd.add(Integer.toString(stopPort));
                cmd.add("--stop-key");
                cmd.add(stopKey);
            }
            if (jettyXml != null)
            {
                cmd.add("--config");
                cmd.add(jettyXml);
            }
            if (requestLog != null)
            {
                cmd.add("--log");
                cmd.add(requestLog.toString());
            }
            if (webAppXml != null)
            {
                cmd.add(webAppXml);
            }
            else
            {
                if (contextPath != null)
                {
                    cmd.add("--path");
                    cmd.add(contextPath);
                }
                cmd.add(webApp.getCanonicalPath());
            }
            
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.directory(project.getBasedir());
            process = builder.start();

            ConsoleParser parser = new ConsoleParser();
            List<String[]> connList = parser.newPattern("Started [A-Za-z]*Connector@([0-9]*\\.[0-9]*\\.[0-9]*\\.[0-9]*):([0-9]*)",1);

            startPump("STDOUT",parser,process.getInputStream());
            startPump("STDERR",parser,process.getErrorStream());

            parser.waitForDone(60,TimeUnit.SECONDS);

            if (connList.isEmpty())
            {
                throw new InterruptedException();
            }
        }
        catch (InterruptedException ex)
        {
            if (process != null)
                process.destroy();
            
            throw new MojoExecutionException("Failed to start Jetty within time limit");
        }
        catch (Exception ex)
        {
            if (process != null)
                process.destroy();
            
            throw new MojoExecutionException("Failed to create Jetty process", ex);
        }
    }
    
    /**
     * @see org.mortbay.jetty.plugin.AbstractJettyMojo#checkPomConfiguration()
     */
    public void checkPomConfiguration() throws MojoExecutionException
    {
       return;        
    }
    
    public String getClassPath() throws IOException
    {
        StringBuilder classPath = new StringBuilder();
        for (Object obj : pluginArtifacts)
        {
            Artifact artifact = (Artifact) obj;
            if ("jar".equals(artifact.getType()))
            {
                if (classPath.length() > 0)
                {
                    classPath.append(':');
                }
                classPath.append(artifact.getFile().getCanonicalPath());
            }
        }
        return pathSeparators(classPath.toString());
    }

    private String getJavaBin()
    {
        String javaexes[] = new String[]
        { "java", "java.exe" };

        File javaHomeDir = new File(System.getProperty("java.home"));
        for (String javaexe : javaexes)
        {
            File javabin = new File(javaHomeDir,fileSeparators("bin/" + javaexe));
            if (javabin.exists() && javabin.isFile())
            {
                return javabin.getAbsolutePath();
            }
        }

        return "java";
    }
    
    public static String fileSeparators(String path)
    {
        StringBuilder ret = new StringBuilder();
        for (char c : path.toCharArray())
        {
            if ((c == '/') || (c == '\\'))
            {
                ret.append(File.separatorChar);
            }
            else
            {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    public static String pathSeparators(String path)
    {
        StringBuilder ret = new StringBuilder();
        for (char c : path.toCharArray())
        {
            if ((c == ',') || (c == ':'))
            {
                ret.append(File.pathSeparatorChar);
            }
            else
            {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    private void startPump(String mode, ConsoleParser parser, InputStream inputStream)
    {
        ConsoleStreamer pump = new ConsoleStreamer(mode,inputStream);
        pump.setParser(parser);
        Thread thread = new Thread(pump,"ConsoleStreamer/" + mode);
        thread.start();
    }

    private static class ConsoleParser
    {
        private List<ConsolePattern> patterns = new ArrayList<ConsolePattern>();
        private CountDownLatch latch;
        private int count;

        public List<String[]> newPattern(String exp, int cnt)
        {
            ConsolePattern pat = new ConsolePattern(exp,cnt);
            patterns.add(pat);
            count += cnt;

            return pat.getMatches();
        }

        public void parse(String line)
        {
            for (ConsolePattern pat : patterns)
            {
                Matcher mat = pat.getMatcher(line);
                if (mat.find())
                {
                    int num = 0, count = mat.groupCount();
                    String[] match = new String[count];
                    while (num++ < count)
                    {
                        match[num - 1] = mat.group(num);
                    }
                    pat.getMatches().add(match);

                    if (pat.getCount() > 0)
                    {
                        getLatch().countDown();
                    }
                }
            }
        }

        public void waitForDone(long timeout, TimeUnit unit) throws InterruptedException
        {
            getLatch().await(timeout,unit);
        }

        private CountDownLatch getLatch()
        {
            synchronized (this)
            {
                if (latch == null)
                {
                    latch = new CountDownLatch(count);
                }
            }

            return latch;
        }
    }

    private static class ConsolePattern
    {
        private Pattern pattern;
        private List<String[]> matches;
        private int count;

        ConsolePattern(String exp, int cnt)
        {
            pattern = Pattern.compile(exp);
            matches = new ArrayList<String[]>();
            count = cnt;
        }

        public Matcher getMatcher(String line)
        {
            return pattern.matcher(line);
        }

        public List<String[]> getMatches()
        {
            return matches;
        }

        public int getCount()
        {
            return count;
        }
    }

    /**
     * Simple streamer for the console output from a Process
     */
    private static class ConsoleStreamer implements Runnable
    {
        private String mode;
        private BufferedReader reader;
        private ConsoleParser parser;

        public ConsoleStreamer(String mode, InputStream is)
        {
            this.mode = mode;
            this.reader = new BufferedReader(new InputStreamReader(is));
        }

        public void setParser(ConsoleParser connector)
        {
            this.parser = connector;
        }

        public void run()
        {
            String line;
            try
            {
                while ((line = reader.readLine()) != (null))
                {
                    if (parser != null)
                    {
                        parser.parse(line);
                    }
                    System.out.println("[" + mode + "] " + line);
                }
            }
            catch (IOException ignore)
            {
                /* ignore */
            }
            finally
            {
                IO.close(reader);
            }
            // System.out.printf("ConsoleStreamer/%s finished%n",mode);
        }
    }
}
