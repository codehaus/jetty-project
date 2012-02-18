/*
 * Copyright (c) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.jetty.npn;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

public class Agent
{
    public static void premain(String configuration, Instrumentation instrumentation)
    {
        agentmain(configuration, instrumentation);
    }

    public static void agentmain(String configuration, Instrumentation instrumentation)
    {
        Map<String, String> options = new HashMap<>();
        if (configuration != null)
        {
            String[] args = configuration.split(",");
            for (String arg : args)
            {
                String[] keyValue = arg.split("=");
                options.put(keyValue[0], keyValue[1]);
            }
        }
        instrumentation.addTransformer(new Transformer(options));
    }
}
