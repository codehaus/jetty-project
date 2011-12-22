//========================================================================
//$Id$
//Copyright 2000-2011 Mort Bay Consulting Pty. Ltd.
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


/**
 *  <p>
 *  This goal is similar to the jetty:run goal, EXCEPT that it is designed to be bound to an execution inside your pom, rather
 *  than being run from the command line. 
 *  </p>
 *  <p>
 *  When using it, be careful to ensure that you bind it to a phase in which all necessary generated files and classes for the webapp
 *  will have been created. If you run it from the command line, then also ensure that all necessary generated files and classes for
 *  the webapp already exist.
 *  </p>
 * 
 * @goal start
 * @requiresDependencyResolution compile+runtime
 * @execute phase="validate"
 * @description Runs jetty directly from a maven project from a binding to an execution in your pom
 */
public class JettyStartMojo extends JettyRunMojo
{
}
