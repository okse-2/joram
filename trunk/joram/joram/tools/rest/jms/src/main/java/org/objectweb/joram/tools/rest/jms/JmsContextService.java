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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.dyade.aaa.common.Debug;

@Path("/" + JmsContextService.CONTEXT)
public class JmsContextService {
  
  public static Logger logger = Debug.getLogger(JmsContextService.class.getName());
  
  private final Helper helper = Helper.getInstance();
  public static final String CONTEXT = "context";
  public static final String CONTEXT_SEND = "send-message";
  public static final String CONTEXT_SEND_NEXT = "send-next-message";
  public static final String CONTEXT_CONSUME = "receive-message";
  public static final String CONTEXT_CONSUME_NEXT = "receive-next-message";
  public static final String CONTEXT_COMMIT = "commit";
  public static final String CONTEXT_ACK = "acknowledge";
  public static final String CONTEXT_ACK_MSG = "acknowledge-message";

  public static final String JMS_HEADER = "header";
  public static final String JMS_PROPERTIES = "properties";
  public static final String JMS_BODY = "body";
  public static final String JMS_TYPE = "type";
  
  public static void logLinks(Response.ResponseBuilder builder) {
    if (builder == null)
      return;
    Response response = builder.build();
    if (response != null) {
      Set<Link> links = response.getLinks();
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO, "links: ");
      for (Link link : links) {
        if (logger.isLoggable(BasicLevel.INFO))
          logger.log(BasicLevel.INFO, "\t" + link.getRel() + ": " + link.getUri());
      }
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO, "");
    }
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public String info(@Context UriInfo uriInfo) {
    StringBuilder buff = new StringBuilder();
    buff.append("<html>");
    buff.append("<body>");
    
    buff.append("<h3>send a message (POST)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/{<b>name</b>}");
    buff.append("\n" + uriInfo.getAbsolutePathBuilder() + "/{<b>name</b>/{<b>id</b>}");
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>delivery-mode:</b> Specifies the delivery mode of messages that are sent using this JMSProducer");
    buff.append("\n  <b>correlation-id:</b> Specifies that messages sent using this JMSProducer will have their JMSCorrelationID header value set to the specified correlation ID");
    buff.append("\n  <b>priority:</b> Specifies the priority of messages that are sent using this JMSProducer");
    buff.append("\n  <b>timeTo-live:</b> Specifies the time to live of messages that are sent using this JMSProducer");
    buff.append("</pre>");
    
    buff.append("<h3>send a message JSON (POST)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/{<b>name</b>}");
    buff.append("\n" + uriInfo.getAbsolutePathBuilder() + "/{<b>name</b>/{<b>id</b>}");
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>delivery-mode:</b> Specifies the delivery mode of messages that are sent using this JMSProducer");
    buff.append("\n  <b>correlation-id:</b> Specifies that messages sent using this JMSProducer will have their JMSCorrelationID header value set to the specified correlation ID");
    buff.append("\n  <b>priority:</b> Specifies the priority of messages that are sent using this JMSProducer");
    buff.append("\n  <b>timeTo-live:</b> Specifies the time to live of messages that are sent using this JMSProducer");
    buff.append("\n  <b>type:</b> The JMS message type BytesMessage, MapMessage, ObjectMessage, StreamMessage or TextMessage (default: TextMessage)");
    buff.append("\n<b>post:</b>");
    buff.append("\n  <b>json:</b> JSon maps contains the " + JMS_BODY + ", the " + JMS_PROPERTIES + " and the " + JMS_HEADER);
    buff.append("\n example:");
    buff.append("\n  {");
    buff.append("\n    \"jmsBody\":\"my test message.\",");
    buff.append("\n    \"jmsProperties\": {");
    buff.append("\n        \"p1\":\"value1\",");
    buff.append("\n        \"p2\":[\"12\",\"java.lang.Integer\"],");
    buff.append("\n        \"p3\":true,");
    buff.append("\n        \"p4\":[\"123456789\",\"java.lang.Long\"],");
    buff.append("\n        \"p5\":3");
    buff.append("\n      }");
    buff.append("\n    }");
    buff.append("</pre>");
 
    buff.append("<h3>consume a message (GET)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/{<b>name</b>}");
    buff.append("\n" + uriInfo.getAbsolutePathBuilder() + "/{<b>name</b>/{<b>id</b>}");
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>timeout:</b>  The timeout value (in milliseconds) for receive (default=-1 indefinitely, 0=receiveNoWait");
    //TODO: buff.append("\n  no-local:</b> if true then any messages published to the topic using this session's connection");
    buff.append("\n  <b>durable:</b> true to creates an durable subscription on the specified topic");
    buff.append("\n  <b>shared:</b> true for shared");
    buff.append("\n  <b>sub-name:</b> the name used to identify this subscription");
    buff.append("</pre>");
    
    buff.append("<h3>commit the producer or consumer messages (HEAD)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/{<b>name</b>}/" + CONTEXT_COMMIT);
    buff.append("</pre>");
    
    buff.append("<h3>acknowledge the producer or consumer (DELETE)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/{<b>name</b>}");
    buff.append("\n" + uriInfo.getAbsolutePathBuilder() + "/{<b>name</b>}/{<b>id</b>}");
    buff.append("</pre>");
    
    buff.append("</body>");
    buff.append("</html>");
    return buff.toString();
  }

  @POST
  @Path("/{name}")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public Response sendMsg(
      @Context HttpHeaders headers,
      @PathParam("name") String prodName,
      @DefaultValue("-1")@QueryParam("delivery-mode") int deliveryMode,
      @DefaultValue("-1")@QueryParam("delivery-time") long deliveryTime,
      @DefaultValue("-1")@QueryParam("priority") int priority,
      @DefaultValue("-1")@QueryParam("timeTo-live") long timeToLive,
      @QueryParam("correlation-id") String correlationID,
      @Context UriInfo uriInfo,
      String body) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "POST: " + uriInfo.getAbsolutePathBuilder());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "sendMsg(" + headers + ", " + prodName + ", " + deliveryMode + ", " +
          deliveryTime + ", " + priority + ", " + timeToLive + ", " + correlationID + ", " + uriInfo + ", " + body);
    
    Response.ResponseBuilder builder = null;
    try {
      if (prodName == null) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "sendMsg: The producer name is null.");
        builder = Response.status(Response.Status.EXPECTATION_FAILED).entity("The producer name is null.");
        return builder.build();
      }

      long msgId = 0;
      try {
        // send the message
        msgId = helper.send(prodName, TextMessage.class.getSimpleName(), null, null, body, 
            deliveryMode, deliveryTime, priority, timeToLive, correlationID);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "sendMsg: msgId = " + msgId); 
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      builder = Response.status(Response.Status.OK);

      // link send message
      UriBuilder nextBuilder = uriInfo.getAbsolutePathBuilder();
      URI next = nextBuilder.build();
      builder.link(next, CONTEXT_SEND);

      // link send next message
      nextBuilder = uriInfo.getAbsolutePathBuilder().path(""+(msgId+1));
      next = nextBuilder.build();
      builder.link(next, CONTEXT_SEND_NEXT);

      //TODO : DUPS_OK

      try {
        if (helper.getSessionCtx(prodName).getJmsContext().getTransacted()) {
          // link commit message
          nextBuilder = uriInfo.getAbsolutePathBuilder().path(CONTEXT_COMMIT);
          builder.link(nextBuilder.build(), CONTEXT_COMMIT);
        }
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }
      return builder.build();
    } finally {
      logLinks(builder);
    }
  }
  
  @POST
  @Path("/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_PLAIN)
  public Response sendMsgJson(
      @Context HttpHeaders headers,
      @PathParam("name") String prodName,
      @DefaultValue("-1")@QueryParam("delivery-mode") int deliveryMode,
      @DefaultValue("-1")@QueryParam("delivery-time") long deliveryTime,
      @DefaultValue("-1")@QueryParam("priority") int priority,
      @DefaultValue("-1")@QueryParam("timeTo-live") long timeToLive,
      @QueryParam("correlation-id") String correlationID,
      @Context UriInfo uriInfo,
      String json) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "POST: " + uriInfo.getAbsolutePathBuilder());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "sendMsg(" + headers + ", " + prodName + ", " + deliveryMode + ", " +
          deliveryTime + ", " + priority + ", " + timeToLive + ", " + correlationID + ", " + 
          uriInfo + ", " + json);
    
    Response.ResponseBuilder builder = null;
    try {
      if (prodName == null) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "sendMsg: The producer name is null.");
        builder = Response.status(Response.Status.EXPECTATION_FAILED).entity("The producer name is null.");
        return builder.build();
      }
      
      long msgId = 0;
      try {
        String jmsType = null;
        Map jmsHeaders = null;
        Map jmsProps = null;
        Object jmsBody = null;
        if (json != null) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "json = " + json);
          Gson gson = new GsonBuilder().create();
          HashMap<String, Object> maps = gson.fromJson(json, HashMap.class);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "maps = " + maps);
          
          // get the message type
          jmsType = (String) maps.get(JMS_TYPE);
          
          // get the headers
          jmsHeaders = (Map) maps.get(JMS_HEADER);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "jmsHeaders = " + jmsHeaders);
          
          // get the properties
          jmsProps = (Map) maps.get(JMS_PROPERTIES);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "jmsProps = " + jmsProps);

          // get the body
          jmsBody = maps.get(JMS_BODY);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "jmsBody = " + jmsBody);
        }

        // send the message
        msgId = helper.send(prodName, jmsType, jmsHeaders, jmsProps, jmsBody, 
            deliveryMode, deliveryTime, priority, timeToLive, correlationID);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "sendMsg: msgId = " + msgId); 
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      builder = Response.status(Response.Status.OK);

      // link send message
      UriBuilder nextBuilder = uriInfo.getAbsolutePathBuilder();
      URI next = nextBuilder.build();
      builder.link(next, CONTEXT_SEND);

      // link send next message
      nextBuilder = uriInfo.getAbsolutePathBuilder().path(""+(msgId+1));
      next = nextBuilder.build();
      builder.link(next, CONTEXT_SEND_NEXT);

      //TODO : DUPS_OK

      try {
        if (helper.getSessionCtx(prodName).getJmsContext().getTransacted()) {
          // link commit message
          nextBuilder = uriInfo.getAbsolutePathBuilder().path(CONTEXT_COMMIT);
          builder.link(nextBuilder.build(), CONTEXT_COMMIT);
        }
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }
      return builder.build();
    } finally {
      logLinks(builder);
    }
  }

  @POST
  @Path("/{name}/{id}")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public Response sendMsg(
      @Context HttpHeaders headers,
      @PathParam("name") String prodName,
      @PathParam("id") long id,
      @DefaultValue("-1")@QueryParam("delivery-mode") int deliveryMode,
      @DefaultValue("-1")@QueryParam("delivery-time") long deliveryTime,
      @DefaultValue("-1")@QueryParam("priority") int priority,
      @DefaultValue("-1")@QueryParam("timeTo-live") long timeToLive,
      @QueryParam("correlation-id") String correlationID,
      @Context UriInfo uriInfo,
      String body) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "POST: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "sendMsg(" + headers + ", " + prodName + ", " + id + ", " + deliveryMode + ", " +
          deliveryTime + ", " + priority + ", " + timeToLive + ", " + correlationID + ", " + uriInfo + ", " + body);

    Response.ResponseBuilder builder = null;
    try {
      if (prodName == null) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "sendMsg: The producer name is null.");
        builder = Response.status(Response.Status.EXPECTATION_FAILED).entity("The producer name is null.");
        return builder.build();
      }

      SessionContext prodCtx = helper.getSessionCtx(prodName);
      if (prodCtx == null) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "Unknown " + prodName + ", prodCtx == null");  
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unknown " + prodName + ", prodCtx == null");
        return builder.build();
      }

      if (id < prodCtx.getLastId()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "sendMsg: The message already send, nothing to do.");
        //message already send, nothing to do
        builder = Response.status(Response.Status.OK);
        //TODO
        return builder.build();
      }

      try {
        // send the message
        long msgId = helper.send(prodName, TextMessage.class.getSimpleName(), null, null, body, 
            deliveryMode, deliveryTime, priority, timeToLive, correlationID);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "sendMsg: msgId = " + msgId); 
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      builder = Response.status(Response.Status.OK);

      // link send message
      UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(prodName);
      builder.link(nextBuilder.build(), CONTEXT_SEND);

      // link send next message
      nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(prodName).path(""+(prodCtx.getLastId()+1));
      builder.link(nextBuilder.build(), CONTEXT_SEND_NEXT);

      //TODO : DUPS_OK

      try {
        if (prodCtx.getJmsContext().getTransacted()) {
          // link commit message
          nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(prodName).path(CONTEXT_COMMIT);
          builder.link(nextBuilder.build(), CONTEXT_COMMIT);
        }
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }
      return builder.build();
    } finally {
      logLinks(builder);
    }
  }
  
  @POST
  @Path("/{name}/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_PLAIN)
  public Response sendMsgJson(
      @Context HttpHeaders headers,
      @PathParam("name") String prodName,
      @PathParam("id") long id,
      @DefaultValue("-1")@QueryParam("delivery-mode") int deliveryMode,
      @DefaultValue("-1")@QueryParam("delivery-time") long deliveryTime,
      @DefaultValue("-1")@QueryParam("priority") int priority,
      @DefaultValue("-1")@QueryParam("timeTo-live") long timeToLive,
      @QueryParam("correlation-id") String correlationID,
      @Context UriInfo uriInfo,
      String json) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "POST: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "sendMsg(" + headers + ", " + prodName + ", " + id + ", " + deliveryMode + ", " +
          deliveryTime + ", " + priority + ", " + timeToLive + ", " + correlationID + ", " + uriInfo + ", " + json);

    Response.ResponseBuilder builder = null;
    try {
      if (prodName == null) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "sendMsg: The producer name is null.");
        builder = Response.status(Response.Status.EXPECTATION_FAILED).entity("The producer name is null.");
        return builder.build();
      }

      SessionContext prodCtx = helper.getSessionCtx(prodName);
      if (prodCtx == null) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "Unknown " + prodName + ", prodCtx == null");  
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unknown " + prodName + ", prodCtx == null");
        return builder.build();
      }

      if (id < prodCtx.getLastId()) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "sendMsg: The message already send, nothing to do.");
        //message already send, nothing to do
        builder = Response.status(Response.Status.OK);
        //TODO
        return builder.build();
      }

      try {
        Map jmsHeaders = null;
        Map jmsProps = null;
        Object jmsBody =null;
        String jmsType = null;
        if (json != null) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "json = " + json);
          Gson gson = new GsonBuilder().create();
          HashMap<String, Object> maps = gson.fromJson(json, HashMap.class);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "maps = " + maps);

          // get the jms message type
          jmsType = (String) maps.get(JMS_TYPE);

          // get the jms headers
          jmsHeaders = (Map) maps.get(JMS_HEADER);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "jmsHeaders = " + jmsHeaders);

          // get the jms properties
          jmsProps = (Map) maps.get(JMS_PROPERTIES);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "jmsProps = " + jmsProps);

          // get the jms body
          jmsBody = maps.get(JMS_BODY);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "jmsBody = " + jmsBody);
        }

        // send the message
        long msgId = helper.send(prodName, jmsType, jmsHeaders, jmsProps, jmsBody, 
            deliveryMode, deliveryTime, priority, timeToLive, correlationID);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "sendMsg: msgId = " + msgId); 
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      builder = Response.status(Response.Status.OK);

      // link send message
      UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(prodName);
      builder.link(nextBuilder.build(), CONTEXT_SEND);

      // link send next message
      nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(prodName).path(""+(prodCtx.getLastId()+1));
      builder.link(nextBuilder.build(), CONTEXT_SEND_NEXT);

      //TODO : DUPS_OK

      try {
        if (prodCtx.getJmsContext().getTransacted()) {
          // link commit message
          nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(prodName).path(CONTEXT_COMMIT);
          builder.link(nextBuilder.build(), CONTEXT_COMMIT);
        }
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }
      return builder.build();
    } finally {
      logLinks(builder);
    }
  }

  @GET
  @Path("/{name}")
  @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
  @Consumes(MediaType.TEXT_PLAIN)
  public Response consumeMsg(
      @Context HttpHeaders headers,
      @PathParam("name") String consName,
      @DefaultValue("-1")@QueryParam("timeout") long timeout,
      @DefaultValue("false")@QueryParam("no-local") boolean noLocal,
      @DefaultValue("false")@QueryParam("durable") boolean durable,
      @DefaultValue("false")@QueryParam("shared") boolean shared,
      @QueryParam("sub-name") String subName,
      @Context UriInfo uriInfo) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "consumeTextMsg(" + headers + ", " + consName + ", " + timeout + ", " +
          noLocal + ", " + durable + ", " + shared + ", " + subName + ", " + uriInfo);

    Response.ResponseBuilder builder = null;
    try {
      if (consName == null) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "sendMsg: The consumer name is null.");
        builder = Response.status(Response.Status.EXPECTATION_FAILED).entity("The consumer name is null.");
        return builder.build();
      }

      Message message = null;
      try {
        // receive the message
        message = helper.consume(consName, timeout, noLocal, durable, shared, subName, -1);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "consumeTextMsg: message = " + message); 
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage());
        return builder.build();
      }
      
      Object msg = null;
      boolean jsonMedia = false;
      List<MediaType> medias = headers.getAcceptableMediaTypes();
      if (medias.contains(MediaType.APPLICATION_JSON_TYPE)) {
        jsonMedia = true;
      }
      
      try {
        if (message instanceof TextMessage) {
          if (jsonMedia) {
            HashMap jsonMap = new HashMap<>();
            jsonMap.put(JMS_TYPE, message.getClass().getSimpleName());
            jsonMap.put(JMS_BODY, ((TextMessage) message).getText());
            jsonMap.put(JMS_PROPERTIES, getPropertiesToJsonMap(message));
            jsonMap.put(JMS_HEADER, getHeaderToJsonMap(message));
            msg = jsonMap;
          } else {
            msg = ((TextMessage) message).getText();
          }
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "consumeTextMsg: msg = " + msg);

        } else if (message instanceof MapMessage) {
          if (jsonMedia) {
            HashMap jsonMap = new HashMap<>();
            jsonMap.put(JMS_TYPE, message.getClass().getSimpleName());
            jsonMap.put(JMS_BODY, message.getBody(Map.class));
            jsonMap.put(JMS_PROPERTIES, getPropertiesToJsonMap(message));
            jsonMap.put(JMS_HEADER, getHeaderToJsonMap(message));
            msg = jsonMap;
          } else {
            builder = Response.status(Response.Status.NO_CONTENT).entity("Only available with MediaType.APPLICATION_JSON");
            return builder.build();
          }

        } else if (message instanceof BytesMessage) {
          if (jsonMedia) {
            HashMap jsonMap = new HashMap<>();
            jsonMap.put(JMS_TYPE, message.getClass().getSimpleName());
            jsonMap.put(JMS_BODY, message.getBody(byte[].class));
            jsonMap.put(JMS_PROPERTIES, getPropertiesToJsonMap(message));
            jsonMap.put(JMS_HEADER, getHeaderToJsonMap(message));
            msg = jsonMap;
          } else {
            builder = Response.status(Response.Status.NO_CONTENT).entity("Only available with MediaType.APPLICATION_JSON");
            return builder.build();
          }
          
        } else if (message != null) {
          throw new JMSException("Invalide Message type: " + message);
        }
      } catch (JMSException e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage());
        return builder.build();
      }
      
      if (timeout > 0 && message == null) {
        // time out expire
        builder = Response.status(Response.Status.NO_CONTENT);
      } else {
        // add message entity for the client
        if (jsonMedia) {
          Gson gson = new GsonBuilder().create();
          String json = gson.toJson(msg);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "json = " + json);
          builder = Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(json);
        } else {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "msg = " + msg);
          builder = Response.status(Response.Status.OK).entity(msg);
        }
      }
      
      ConsumerContext consCtx = (ConsumerContext) helper.getSessionCtx(consName);
      try {
        if (consCtx.getJmsContext().getTransacted()) {
          // link commit consumer message
          UriBuilder nextBuilder = uriInfo.getAbsolutePathBuilder().path(CONTEXT_COMMIT);
          builder.link(nextBuilder.build(), CONTEXT_COMMIT);
        }
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      try {
        if (consCtx.getJmsContext().getSessionMode() == JMSContext.CLIENT_ACKNOWLEDGE) {
          // link acknowledge all message
          UriBuilder nextBuilder = uriInfo.getAbsolutePathBuilder();
          builder.link(nextBuilder.build(), CONTEXT_ACK);

          long id = consCtx.getId(message);
          if (id > 0) {
            // link acknowledge a message
            nextBuilder = uriInfo.getAbsolutePathBuilder().path(""+id);
            builder.link(nextBuilder.build(), CONTEXT_ACK_MSG);
          }
        }

        long id = consCtx.getLastId();
        // link consume next message
        UriBuilder nextBuilder = uriInfo.getAbsolutePathBuilder().path(""+(id+1));
        builder.link(nextBuilder.build(), CONTEXT_CONSUME_NEXT);

      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      // link consume message
      UriBuilder nextBuilder = uriInfo.getAbsolutePathBuilder();
      URI next = nextBuilder.build();
      builder.link(next, CONTEXT_CONSUME);

      return builder.build();
    } finally {
      logLinks(builder);
    }
  }
 
  @GET
  @Path("/{name}/{id}")
  @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
  @Consumes(MediaType.TEXT_PLAIN)
  public Response consumeMsg(
      @Context HttpHeaders headers,
      @PathParam("name") String consName,
      @PathParam("id") long id,
      @DefaultValue("-1")@QueryParam("timeout") long timeout,
      @DefaultValue("false")@QueryParam("no-local") boolean noLocal,
      @DefaultValue("false")@QueryParam("durable") boolean durable,
      @DefaultValue("false")@QueryParam("shared") boolean shared,
      @QueryParam("sub-name") String subName,
      @Context UriInfo uriInfo) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "consumeTextMsg(" + headers + ", " + consName + ", " + id + ", " + timeout + ", " +
          noLocal + ", " + durable + ", " + shared + ", " + subName + ", " + uriInfo);

    Response.ResponseBuilder builder = null;
    try {
      if (consName == null) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "consumeMsg: The consumer name is null.");
        builder = Response.status(Response.Status.EXPECTATION_FAILED).entity("The consumer name is null.");
        return builder.build();
      }

      Message message = null;
      try {
        // receive the message
        message = helper.consume(consName, timeout, noLocal, durable, shared, subName, id);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "consumeTextMsg: message = " + message); 
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage());
        return builder.build();
      }
      
      boolean jsonMedia = false;
      List<MediaType> medias = headers.getAcceptableMediaTypes();
      if (medias.contains(MediaType.APPLICATION_JSON_TYPE))
        jsonMedia = true;
      
      Object msg = null;
      try {
        if (message instanceof TextMessage) {
          if (jsonMedia) {
            HashMap jsonMap = new HashMap<>();
            jsonMap.put(JMS_TYPE, message.getClass().getSimpleName());
            jsonMap.put(JMS_BODY, ((TextMessage) message).getText());
            jsonMap.put(JMS_PROPERTIES, getPropertiesToJsonMap(message));
            jsonMap.put(JMS_HEADER, getHeaderToJsonMap(message));
            msg = jsonMap;
          } else {
            msg = ((TextMessage) message).getText();
          }
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "consumeTextMsg: msg = " + msg);

        } else if (message instanceof MapMessage) {
          if (jsonMedia) {
            HashMap jsonMap = new HashMap<>();
            jsonMap.put(JMS_TYPE, message.getClass().getSimpleName());
            jsonMap.put(JMS_BODY, message.getBody(Map.class));
            jsonMap.put(JMS_PROPERTIES, getPropertiesToJsonMap(message));
            jsonMap.put(JMS_HEADER, getHeaderToJsonMap(message));
            msg = jsonMap;
          } else {
            builder = Response.status(Response.Status.NO_CONTENT).entity("Only available with MediaType.APPLICATION_JSON");
            return builder.build();
          }

        } else if (message instanceof BytesMessage) {
          if (jsonMedia) {
            HashMap jsonMap = new HashMap<>();
            jsonMap.put(JMS_TYPE, message.getClass().getSimpleName());
            jsonMap.put(JMS_BODY, message.getBody(byte[].class));
            jsonMap.put(JMS_PROPERTIES, getPropertiesToJsonMap(message));
            jsonMap.put(JMS_HEADER, getHeaderToJsonMap(message));
            msg = jsonMap;
          } else {
            builder = Response.status(Response.Status.NO_CONTENT).entity("Only available with MediaType.APPLICATION_JSON");
            return builder.build();
          }
          
        } else if (message != null) {
          throw new JMSException("Invalide Message type: " + message);
        }
      } catch (JMSException e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage());
        return builder.build();
      }
      
      if (timeout > 0 && message == null) {
        // time out expire
        builder = Response.status(Response.Status.NO_CONTENT);
      } else {
        // add message entity for the client
        if (jsonMedia) {
          Gson gson = new GsonBuilder().create();
          String json = gson.toJson(msg);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "json = " + json);
          builder = Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(json);
        } else {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "msg = " + msg);
          builder = Response.status(Response.Status.OK).entity(msg);
        }
      }

      ConsumerContext consCtx = (ConsumerContext) helper.getSessionCtx(consName);
      try {
        if (consCtx.getJmsContext().getTransacted()) {
          // link commit consumer message
          UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(consName).path(CONTEXT_COMMIT);
          builder.link(nextBuilder.build(), CONTEXT_COMMIT);
        }
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      try {
        if (consCtx.getJmsContext().getSessionMode() == JMSContext.CLIENT_ACKNOWLEDGE) {
          // link acknowledge all message
          UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(consName);
          builder.link(nextBuilder.build(), CONTEXT_ACK);

          // link acknowledge a message
          nextBuilder = uriInfo.getAbsolutePathBuilder();
          builder.link(nextBuilder.build(), CONTEXT_ACK_MSG);
        }

        id = consCtx.getLastId();
        // link consume next message
        UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(consName).path(""+(id+1));
        builder.link(nextBuilder.build(), CONTEXT_CONSUME_NEXT);

      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      // link consume message
      UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(consName);
      URI next = nextBuilder.build();
      builder.link(next, CONTEXT_CONSUME);

      return builder.build();
    } finally {
      logLinks(builder);
    }
  }

  private Map getPropertiesToJsonMap(Message message) throws JMSException {
    HashMap<String, Object> props = new HashMap<>();
    Enumeration<String> names = message.getPropertyNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      Object v = message.getObjectProperty(name);
      if (v != null) {
        String[] value = {""+v, v.getClass().getName()};
        props.put(name, value);
      }
    }
    return props;
  }
  
  private Map getHeaderToJsonMap(Message message) {
    HashMap<String, Object> header = new HashMap<>();
    try {
      if (message.getJMSDeliveryMode() == DeliveryMode.NON_PERSISTENT)
        header.put("DeliveryMode", "NON_PERSISTENT");
      else if (message.getJMSDeliveryMode() == DeliveryMode.PERSISTENT)
        header.put("DeliveryMode", "PERSISTENT");
    } catch (JMSException e) { }
    try {
      header.put("Priority", message.getJMSPriority());
    } catch (JMSException e) { }
    try {
      header.put("Redelivered", message.getJMSRedelivered());
    } catch (JMSException e) { }
    try {
      header.put("Timestamp", message.getJMSTimestamp());
    } catch (JMSException e) { }
    try {
      if (message.getJMSExpiration() > 0)
        header.put("Expiration", message.getJMSExpiration());
    } catch (JMSException e) { }
    try {
      if (message.getJMSCorrelationID() != null)
        header.put("CorrelationID", message.getJMSCorrelationID());
    } catch (JMSException e) { }
    try {
      if (message.getJMSCorrelationIDAsBytes() != null)
        header.put("CorrelationIDAsBytes", message.getJMSCorrelationIDAsBytes());
    } catch (JMSException e) { }
    try {
      if (message.getJMSDestination() != null)
        header.put("Destination", message.getJMSDestination());
    } catch (JMSException e) { }
    try {
      if (message.getJMSMessageID() != null)
        header.put("MessageID", message.getJMSMessageID());
    } catch (JMSException e) { }
    try {
      if (message.getJMSReplyTo() != null)
        header.put("ReplyTo", message.getJMSReplyTo());
    } catch (JMSException e) { }
    try {
      if ( message.getJMSType() != null)
        header.put("Type", message.getJMSType());
    } catch (JMSException e) { }
    return header;
  }
  
  @HEAD
  @Path("/{name}/" + CONTEXT_COMMIT)
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.TEXT_PLAIN)
  public Response commit(
      @Context HttpHeaders headers,
      @PathParam("name") String ctxName,
      @Context UriInfo uriInfo) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "HEAD: " + uriInfo.getAbsolutePathBuilder());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "commit(" + headers + ", " + ctxName + ", " + uriInfo);
    
    Response.ResponseBuilder builder = null;
    try {

      if (ctxName == null) {
        builder = Response.status(Response.Status.EXPECTATION_FAILED).entity("The producer/consumer name is null.");
        return builder.build();
      }

      SessionContext ctx = helper.getSessionCtx(ctxName);
      if (ctx == null) {
        ctx = helper.getSessionCtx(ctxName);
      }

      try {
        if (! ctx.getJmsContext().getTransacted()) {
          builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity("The jmsContext for " + ctxName + " is not transacted.");
          return builder.build();
        }
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      try {
        // commit
        helper.commit(ctxName);
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      builder = Response.status(Response.Status.OK);

      if (ctx instanceof ProducerContext) {
        // link send message
        UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(ctxName);
        builder.link(nextBuilder.build(), CONTEXT_SEND);

        // link send next message
        nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(ctxName).path(""+(ctx.getLastId()+1));
        builder.link(nextBuilder.build(), CONTEXT_SEND_NEXT);
      } 

      if (ctx instanceof ConsumerContext) {
        // link consume message
        UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(ctxName);
        builder.link(nextBuilder.build(), CONTEXT_CONSUME);

        // link consume next message
        nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(ctxName).path("" + (ctx.getLastId()+1));
        builder.link(nextBuilder.build(), CONTEXT_CONSUME_NEXT);
      }

      return builder.build();
    } finally {
      logLinks(builder);
    }
  }

  @DELETE
  @Path("/{name}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.TEXT_PLAIN)
  public Response acknowledgeCons(
      @Context HttpHeaders headers,
      @PathParam("name") String ctxName,
      @Context UriInfo uriInfo) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "DELETE: " + uriInfo.getAbsolutePathBuilder());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "acknowledgeCons(" + headers + ", " + ctxName + ", " + uriInfo);
    
    Response.ResponseBuilder builder = null;
    try {

      if (ctxName == null) {
        builder = Response.status(Response.Status.EXPECTATION_FAILED).entity("The consumer name is null.");
        return builder.build();
      }

      SessionContext ctx = helper.getSessionCtx(ctxName);
      try {
        if (ctx.getJmsContext().getSessionMode() != JMSContext.CLIENT_ACKNOWLEDGE) {
          builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity("The jmsContext for " + ctxName + " is not in CLIENT_ACKNOWLEDGE mode.");
          return builder.build();
        }
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      try {
        // acknowledge
        helper.acknowledgeAllMsg(ctxName);
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      builder = Response.status(Response.Status.OK);

      // link consume message
      UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(ctxName);
      builder.link(nextBuilder.build(), CONTEXT_CONSUME);

      // link consume next message
      nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(ctxName).path("" + (ctx.getLastId()+1));
      builder.link(nextBuilder.build(), CONTEXT_CONSUME_NEXT);

      return builder.build();
    } finally {
      logLinks(builder);
    }
  }

  @DELETE
  @Path("/{name}/{id}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.TEXT_PLAIN)
  public Response acknowledgeCons(
      @Context HttpHeaders headers,
      @PathParam("name") String ctxName,
      @PathParam("id") long id,
      @Context UriInfo uriInfo) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "DELETE: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "acknowledgeCons(" + headers + ", " + ctxName + ", " + id + ", " + uriInfo);

    Response.ResponseBuilder builder = null;
    try {

      if (ctxName == null) {
        builder = Response.status(Response.Status.EXPECTATION_FAILED).entity("The consumer name is null.");
        return builder.build();
      }

      SessionContext ctx = helper.getSessionCtx(ctxName);
      try {
        if (ctx.getJmsContext().getSessionMode() != JMSContext.CLIENT_ACKNOWLEDGE) {
          builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
              entity("The jmsContext for " + ctxName + " is not in CLIENT_ACKNOWLEDGE mode.");
          return builder.build();
        }
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      try {
        // acknowledge
        helper.acknowledgeMsg(ctxName, id);
      } catch (Exception e) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "", e);
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
        return builder.build();
      }

      builder = Response.status(Response.Status.OK);

      // link consume message
      UriBuilder nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(ctxName);
      builder.link(nextBuilder.build(), CONTEXT_CONSUME);

      // link consume next message
      nextBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CONTEXT).path(ctxName).path("" + (ctx.getLastId()+1));
      builder.link(nextBuilder.build(), CONTEXT_CONSUME_NEXT);

      return builder.build();
    } finally {
      logLinks(builder);
    }
  }
}
