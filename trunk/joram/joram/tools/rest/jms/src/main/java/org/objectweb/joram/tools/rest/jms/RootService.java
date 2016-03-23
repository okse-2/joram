/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2016 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.joram.tools.rest.jms;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/")
public class RootService {

  
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String info(@Context UriInfo uriInfo) {
    URI jndiURI = uriInfo.getBaseUriBuilder().path("jndi").build();
    URI jmsURI = uriInfo.getBaseUriBuilder().path("jms").build();
    URI contextURI = uriInfo.getBaseUriBuilder().path("context").build();
    return "<html><body>" +
        "<br><a href=\""+jndiURI+"\">"+jndiURI+"</a>"+
        "<br><a href=\""+jmsURI+"\">"+jmsURI+"</a>"+
        "<br><a href=\""+contextURI+"\">"+contextURI+"</a>"+
        "</body></html>";
  }
}
