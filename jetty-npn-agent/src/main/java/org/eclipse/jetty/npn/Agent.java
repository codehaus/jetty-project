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
