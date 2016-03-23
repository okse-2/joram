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

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSSecurityRuntimeException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

@Path("/" + JndiService.JNDI)
public class JndiService {

  public static Logger logger = Debug.getLogger(JndiService.class.getName());
  
  private final Helper helper = Helper.getInstance();
  public static final String JNDI = "jndi";
  
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String info(@Context UriInfo uriInfo) {
    StringBuilder buff = new StringBuilder();
    buff.append("<html>");
    buff.append("<body>");
    
    buff.append("<h3>lookup (GET)</h3>");
    buff.append("<pre>");
    buff.append("set a destination-name: " + uriInfo.getAbsolutePathBuilder() + "/{<b>destination-name</b>}");
    buff.append("</pre>");
    
    buff.append("<h3>create a producer (HEAD)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/{<b>destination-name</b>}/"+JmsService.JMS_CREATE_PROD);
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>client-id:</b> The client identifier for the JMSContext's connection");
    buff.append("\n  <b>name:</b> The producer name for the producer JMSContext");
    buff.append("\n  <b>session-mode:</b> AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE,  DUPS_OK_ACKNOWLEDGE or SESSION_TRANSACTED");
    buff.append("\n  <b>delivery-mode:</b> Specifies the delivery mode of messages that are sent using this JMSProducer");
    buff.append("\n  <b>delivery-delay:</b> Sets the minimum length of time in milliseconds that must elapse after a message is sent before the JMS provider may deliver the message to a consumer");
    buff.append("\n  <b>correlation-id:</b> Specifies that messages sent using this JMSProducer will have their JMSCorrelationID header value set to the specified correlation ID");
    buff.append("\n  <b>priority:</b> Specifies the priority of messages that are sent using this JMSProducer");
    buff.append("\n  <b>timeTo-live:</b> Specifies the time to live of messages that are sent using this JMSProducer");
    buff.append("\n  <b>idle-timeout:</b> Allows to set the idle time in milliseconds in which the producer context will be closed if idle");
    buff.append("\n  <b>user:</b> Specifies the userName for the JMS connection");
    buff.append("\n  <b>password:</b> Specifies the password for the JMS connection");
    buff.append("</pre>");
    
    buff.append("<h3>create a consumer (HEAD)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/{<b>destination-name</b>}/"+JmsService.JMS_CREATE_CONS);
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>client-id:</b> The client identifier for the JMSContext's connection");
    buff.append("\n  <b>name:</b> The producer name for the producer JMSContext");
    buff.append("\n  <b>session-mode:</b> AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE,  DUPS_OK_ACKNOWLEDGE or SESSION_TRANSACTED");
    buff.append("\n  <b>message-selector:</b> Only messages with properties matching the message selector expression are delivered");
    //TODO: buff.append("\n  no-local:</b> if true then any messages published to the topic using this session's connection");
    buff.append("\n  <b>durable:</b> true to creates an durable subscription on the specified topic");
    buff.append("\n  <b>shared:</b> true for shared");
    buff.append("\n  <b>sub-name:</b> the name used to identify this subscription");
    buff.append("\n  <b>idle-timeout:</b> Allows to set the idle time in milliseconds in which the consumer context will be closed if idle");
    buff.append("\n  <b>user:</b> Specifies the userName for the JMS connection");
    buff.append("\n  <b>password:</b> Specifies the password for the JMS connection");
    buff.append("</pre>");

    buff.append("<h3>close a producer or a consumer (DELETE)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/{<b>name</b>}");
    buff.append("</pre>");
    buff.append("</body>");
    buff.append("</html>");
    return buff.toString();
  }
  
  @GET
  @Path("/{destName}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response lookupDestination(
      @Context HttpHeaders headers,
      @PathParam("destName") String destName,
      @Context UriInfo uriInfo) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "getDestination(" + headers + ", " + destName + ", " + uriInfo + ")");

    Response.ResponseBuilder builder = null;
    try {

      try {
        if (helper.lookupDestination(destName) == null) {
          builder = Response.status(Response.Status.NOT_FOUND);
          UriBuilder nextBuilder = uriInfo.getAbsolutePathBuilder();
          URI next = nextBuilder.build();
          builder.link(next, "lookup");
          return builder.build();
        }
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage());
        UriBuilder nextBuilder = uriInfo.getAbsolutePathBuilder();
        URI next = nextBuilder.build();
        builder.link(next, "lookup");
        return builder.build();
      }

      builder = Response.status(Response.Status.CREATED);

      //link to the create producer
      UriBuilder nextBuilder = uriInfo.getAbsolutePathBuilder().path(JmsService.JMS_CREATE_PROD);
      builder.link(nextBuilder.build(), JmsService.JMS_CREATE_PROD);
      //link to the create producer DUPS_OK_ACKNOWLEDGE
      nextBuilder = uriInfo.getAbsolutePathBuilder().path(JmsService.JMS_CREATE_PROD);
      nextBuilder.queryParam("session-mode", JMSContext.DUPS_OK_ACKNOWLEDGE);
      builder.link(nextBuilder.build(), JmsService.JMS_CREATE_PROD+"-dups-ok");
      //link to the create producer SESSION_TRANSACTED
      nextBuilder = uriInfo.getAbsolutePathBuilder().path(JmsService.JMS_CREATE_PROD);
      nextBuilder.queryParam("session-mode", JMSContext.SESSION_TRANSACTED);
      builder.link(nextBuilder.build(), JmsService.JMS_CREATE_PROD+"-transacted");
      //link to the create consumer
      nextBuilder = uriInfo.getAbsolutePathBuilder().path(JmsService.JMS_CREATE_CONS);
      builder.link(nextBuilder.build(), JmsService.JMS_CREATE_CONS);
      //link to the create consumer DUPS_OK_ACKNOWLEDGE
      nextBuilder = uriInfo.getAbsolutePathBuilder().path(JmsService.JMS_CREATE_CONS);
      nextBuilder.queryParam("session-mode", JMSContext.DUPS_OK_ACKNOWLEDGE);
      builder.link(nextBuilder.build(), JmsService.JMS_CREATE_CONS+"-dups-ok");
      //link to the create consumer CLIENT_ACKNOWLEDGE
      nextBuilder = uriInfo.getAbsolutePathBuilder().path(JmsService.JMS_CREATE_CONS);
      nextBuilder.queryParam("session-mode", JMSContext.CLIENT_ACKNOWLEDGE);
      builder.link(nextBuilder.build(), JmsService.JMS_CREATE_CONS+"-client-ack");
      //link to create consumer SESSION_TRANSACTED
      nextBuilder = uriInfo.getAbsolutePathBuilder().path(JmsService.JMS_CREATE_CONS);
      nextBuilder.queryParam("session-mode", JMSContext.SESSION_TRANSACTED);
      builder.link(nextBuilder.build(), JmsService.JMS_CREATE_CONS+"-transacted");
      //link to the lookup
      nextBuilder = uriInfo.getAbsolutePathBuilder();
      builder.link(nextBuilder.build(), "lookup");
      return builder.build();
    } finally {
      JmsContextService.logLinks(builder);
    }
  }
  
  @HEAD
  @Path("/{destName}/"+ JmsService.JMS_CREATE_PROD)
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.TEXT_PLAIN)
  public Response createProducer(
      @Context HttpHeaders headers,
      @PathParam("destName") String destName,
      @QueryParam("client-id") String clientID,
      @QueryParam("name") String prodName,
      @DefaultValue(""+JMSContext.AUTO_ACKNOWLEDGE)@QueryParam("session-mode") int sessionMode,
      @DefaultValue(""+Message.DEFAULT_DELIVERY_MODE)@QueryParam("delivery-mode") int deliveryMode,
      @DefaultValue(""+Message.DEFAULT_DELIVERY_DELAY)@QueryParam("delivery-delay") long deliveryDelay,
      @QueryParam("correlation-id") String correlationID,
      @DefaultValue(""+Message.DEFAULT_PRIORITY)@QueryParam("priority") int priority,
      @DefaultValue(""+Message.DEFAULT_TIME_TO_LIVE)@QueryParam("timeTo-live")long timeToLive,
      @DefaultValue("0")@QueryParam("idle-timeout") long idleTimeout,
      @QueryParam("user") String userName,
      @QueryParam("password")String password,
      @Context UriInfo uriInfo) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "HEAD: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "createProducer(" + headers + ", " + destName + ", " + clientID + ", " + prodName + ", " + 
          sessionMode + ", " + deliveryMode + ", " + deliveryDelay + ", " + correlationID + ", " + priority + ", " + timeToLive + ", " + 
          idleTimeout + ", " + userName + ", " + uriInfo + ")");

    Response.ResponseBuilder builder = null;
    try {

      String prodId = null;
      try {
        // lookup the destination
        Destination dest = helper.lookupDestination(destName);
        
        // create the producer
        prodId = helper.createProducer(userName, password, clientID, prodName, dest, sessionMode, 
            deliveryMode, deliveryDelay, correlationID, priority, timeToLive, destName, (dest instanceof Queue), idleTimeout);
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, e);
        if (e instanceof JMSSecurityRuntimeException)
          builder = Response.status(Response.Status.UNAUTHORIZED).entity(e.toString());
        else
          builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      builder = Response.status(Response.Status.CREATED);

      // link send message
      UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(JmsContextService.CONTEXT).path(prodId);
      builder.link(nextBuilder.build(), JmsContextService.CONTEXT_SEND);

      // link send next message
      long id = helper.getSessionCtx(prodId).getLastId() + 1;
      nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(JmsContextService.CONTEXT).path(prodId).path(""+id);
      builder.link(nextBuilder.build(), JmsContextService.CONTEXT_SEND_NEXT);

      // link delete producer
      nextBuilder = UriBuilder.fromPath(uriInfo.getBaseUri().toString()).path(JmsService.JMS).path(prodId);
      builder.link(nextBuilder.build(), "close-" + JmsContextService.CONTEXT);

      return builder.build();
    } finally {
      JmsContextService.logLinks(builder);
    }
  }

  @HEAD
  @Path("/{destName}/"+ JmsService.JMS_CREATE_CONS)
  @Produces(MediaType.TEXT_PLAIN)
  public Response createConsumer(
      @Context HttpHeaders headers,
      @PathParam("destName") String destName,
      @QueryParam("client-id") String clientID,
      @QueryParam("name") String consName,
      @DefaultValue(""+JMSContext.AUTO_ACKNOWLEDGE)@QueryParam("session-mode") int sessionMode,
      @QueryParam("message-selector") String messageSelector,
      @DefaultValue("false")@QueryParam("no-local") boolean noLocal,
      @DefaultValue("false")@QueryParam("durable") boolean durable,
      @DefaultValue("false")@QueryParam("shared") boolean shared,
      @QueryParam("sub-name") String subName,
      @DefaultValue("0")@QueryParam("idle-timeout") long idleTimeout,
      @QueryParam("user") String userName,
      @QueryParam("password")String password,
      @Context UriInfo uriInfo) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "HEAD: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "createConsumer(" + headers + ", " + destName + ", " + clientID + ", " + consName + ", " + 
          sessionMode + ", " + messageSelector + ", " + noLocal + ", " + durable + ", " + shared + ", " + subName + ", " + 
          idleTimeout + ", " + userName + ", " + uriInfo + ")");

    Response.ResponseBuilder builder = null;
    try {

      String consId = null;
      try {
        // lookup the destination
        Destination dest = helper.lookupDestination(destName);
        
        // create the consumer
        consId = helper.createConsumer(userName, password, clientID, consName, dest, sessionMode, messageSelector, 
            noLocal, durable, shared, subName, destName, (dest instanceof Queue), idleTimeout);
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, e);
        if (e instanceof JMSSecurityRuntimeException)
          builder = Response.status(Response.Status.UNAUTHORIZED).entity(e.toString());
        else
          builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      builder = Response.status(Response.Status.CREATED);

      // link consume message
      UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(JmsContextService.CONTEXT).path(consId);
      builder.link(nextBuilder.build(), JmsContextService.CONTEXT_CONSUME);

      // link consume next message
      long id = helper.getSessionCtx(consId).getLastId() + 1;
      nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(JmsContextService.CONTEXT).path(consId).path(""+id);
      builder.link(nextBuilder.build(), JmsContextService.CONTEXT_CONSUME_NEXT);

      // link delete consumer
      nextBuilder = UriBuilder.fromPath(uriInfo.getBaseUri().toString()).path(JmsService.JMS).path(consId);
      builder.link(nextBuilder.build(), "close-" + JmsContextService.CONTEXT);

      return builder.build();
    } finally {
      JmsContextService.logLinks(builder);
    }
  }
  
  @DELETE
  @Path("/{name}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.TEXT_PLAIN)
  public Response closeSessionCtx(
      @Context HttpHeaders headers,
      @PathParam("name") String ctxName,
      @Context UriInfo uriInfo) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "DELETE: " + uriInfo.getAbsolutePathBuilder());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "closeSessionCtx(" + headers + ", " + ctxName + ", " + uriInfo + ")");

    
    Response.ResponseBuilder builder = null;
    try {

      if (ctxName == null) {
        builder = Response.status(Response.Status.EXPECTATION_FAILED).entity("The context name is null.");
        return builder.build();
      }

      try {
        // close context
        helper.closeSessionCtx(ctxName);
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      builder = Response.status(Response.Status.OK);

      // link jndi 
      UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(JNDI);//TODO
      builder.link(nextBuilder.build(), JNDI);
      
      // link jms 
      nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(JmsService.JMS);//TODO
      builder.link(nextBuilder.build(), JmsService.JMS);

      return builder.build();
    } finally {
      JmsContextService.logLinks(builder);
    }
  }
}
