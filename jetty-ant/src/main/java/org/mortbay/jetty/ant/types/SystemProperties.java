//========================================================================
//Copyright 2006-2007 Sabre Holdings.
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

package org.mortbay.jetty.ant.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.taskdefs.Property;
import org.mortbay.jetty.ant.utils.TaskLog;

/**
 * Ant <systemProperties/> tag definition.
 * 
 * @author Jakub Pawlowicz
 */
public class SystemProperties
{

    private List systemProperties = new ArrayList();

    public List getSystemProperties()
    {
        return systemProperties;
    }

    public void addSystemProperty(Property property)
    {
        systemProperties.add(property);
    }

    /**
     * Set a System.property with this value if it is not already set.
     * 
     * @returns true if property has been set
     */
    public static boolean setIfNotSetAlready(Property property)
    {
        if (System.getProperty(property.getName()) == null)
        {
            System.setProperty(property.getName(), (property.getValue() == null ? "" : property
                    .getValue()));
            TaskLog.log("Setting property '" + property.getName() + "' to value '"
                    + property.getValue() + "'");
            return true;
        }

        return false;
    }
}
