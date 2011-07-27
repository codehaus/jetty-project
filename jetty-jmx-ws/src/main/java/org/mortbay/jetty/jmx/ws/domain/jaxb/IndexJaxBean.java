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

package org.mortbay.jetty.jmx.ws.domain.jaxb;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

/* ------------------------------------------------------------ */
/**
 */
@XmlRootElement(name = "Index")
public class IndexJaxBean
{
    public URI mBeans;
    public URI nodes;

    public IndexJaxBean()
    {
    }

    public IndexJaxBean(UriInfo uriInfo)
    {
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        this.mBeans = uriBuilder.path("mbeans").build();
        uriBuilder = uriInfo.getAbsolutePathBuilder();
        this.nodes = uriBuilder.path("nodes").build();
    }

}
