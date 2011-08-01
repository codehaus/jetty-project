// ========================================================================
// Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.jmx.ws.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mortbay.jetty.jmx.ws.domain.jaxb.IndexJaxBean;


/* ------------------------------------------------------------ */
/**
 */
@Path("/")
public class Index
{
    @Context
    UriInfo uriInfo;

    @GET
    @Produces(
    { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public IndexJaxBean getObjectNames()
    {
        return new IndexJaxBean(uriInfo);
    }

}