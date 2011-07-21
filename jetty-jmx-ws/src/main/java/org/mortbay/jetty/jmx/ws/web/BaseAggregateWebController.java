package org.mortbay.jetty.jmx.ws.web;

import org.mortbay.jetty.jmx.ws.service.AggregateService;
import org.mortbay.jetty.jmx.ws.service.impl.AggregateServiceImpl;
import org.mortbay.jetty.jmx.ws.service.impl.JMXServiceImpl;

public class BaseAggregateWebController
{
    protected static AggregateService aggregateService = new AggregateServiceImpl(JMXServiceImpl.getInstance());

    public void setAggregateService(AggregateService aggregateService)
    {
        BaseAggregateWebController.aggregateService = aggregateService;
    }
}
