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
package org.objectweb.joram.tools.rest.admin;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.inject.Singleton;
import javax.jms.ConnectionFactory;
import javax.jms.JMSSecurityRuntimeException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.DatatypeConverter;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.dyade.aaa.common.Debug;

@Path("/")
@Singleton
public class AdminService implements ContainerRequestFilter {

  private static final String AUTHORIZATION_PROPERTY = "Authorization";
  private static final String AUTHENTICATION_SCHEME = "Basic";
  
  public static Logger logger = Debug.getLogger(AdminService.class.getName());
  private final AdminHelper helper = AdminHelper.getInstance();
  public static final String ADMIN = "admin";

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
    
    buff.append("<h3>create a default destination (GET)</h3>");
    buff.append("<pre>");
    buff.append("create a queue or topic: " + uriInfo.getAbsolutePathBuilder() + "/[queue|topic]/{<b>name</b>}");
    buff.append("</pre>");
    
    buff.append("<h3>create a destination (POST)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/[queue|topic]/{<b>name</b>}");
    buff.append("\n<b>path:</b>");
    buff.append("\n  <b>name:</b> The queue or topic name");
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>class-name:</b> The className (default = org.objectweb.joram.mom.dest.[Queue|Topic])");
    buff.append("\n  <b>server-id:</b> The serverId (default the local server Id");
    buff.append("\n  <b>free-reading:</b> boolean for free reading (default = false)");
    buff.append("\n  <b>free-writing:</b> boolean for free writing (default = false)");
    buff.append("\n  <b>jndi-bind:</b> boolean for jndi re-binding the destination (default = true)");
    buff.append("\n  <b>jndi-name:</b> Jndi name for this destination (default = the destination name)");
    buff.append("\n<b>post:</b>");
    buff.append("\n  <b>json:</b> The destination properties (in Json)");
    buff.append("</pre>");
    
    buff.append("<h3>delete a destination (DELETE)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/[queue|topic]/{<b>name</b>}");
    buff.append("\n<b>path:</b>");
    buff.append("\n  <b>name:</b> The queue or topic name");
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>server-id:</b> The serverId (default the local server Id");
    buff.append("\n  <b>jndi-unbind:</b> boolean for jndi un-binding the destination (default = true)");
    buff.append("\n  <b>jndi-name:</b> Jndi name for this destination (default = the destination name)");
    buff.append("</pre>");
    
    buff.append("<h3>list destinations (queue|topic) (GET)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/[queue|topic]");
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>server-id:</b> The serverId (default the local server Id");
    buff.append("</pre>");
    
    buff.append("<h3>create a user (GET)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/user/{<b>name</b>}");
    buff.append("\n<b>path:</b>");
    buff.append("\n  <b>name:</b> The user name");
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>password:</b> The user password");
    buff.append("</pre>");
    
    buff.append("<h3>create a user (POST)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/user/{<b>name</b>}");
    buff.append("\n<b>path:</b>");
    buff.append("\n  <b>name:</b> The user name");
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>password:</b> The user password");
    buff.append("\n  <b>identity-class-name:</b> the identity className (default = org.objectweb.joram.shared.security.SimpleIdentity)");
    buff.append("\n  <b>server-id:</b> The serverId (default the local server Id)");
    buff.append("\n<b>post:</b>");
    buff.append("\n  <b>json:</b> The user properties (in Json)");
    buff.append("</pre>");
    
    buff.append("<h3>delete a user (DELETE)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/user/{<b>name</b>}");
    buff.append("\n<b>path:</b>");
    buff.append("\n  <b>name:</b> The user name");
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>password:</b> The user password");
    buff.append("\n  <b>server-id:</b> The serverId (default the local server Id)");
    buff.append("</pre>");
      
    buff.append("<h3>create a TCP connection factory (GET)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/tcp/create");
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>host:</b> The host name (default = localhost)");
    buff.append("\n  <b>port:</b> the port value (default = 16010)");
    buff.append("\n  <b>reliable-class:</b> the reliable class (default = org.objectweb.joram.client.jms.tcp.ReliableTcpClient)");
    buff.append("\n  <b>jndi-name:</b> registered name of the ConnectionFactory in JNDI");
    buff.append("</pre>");
    
    buff.append("<h3>create a Local connection factory (GET)</h3>");
    buff.append("<pre>");
    buff.append(uriInfo.getAbsolutePathBuilder() + "/local/create");
    buff.append("\n<b>options:</b>");
    buff.append("\n  <b>jndi-name:</b> registered name of the ConnectionFactory in JNDI");
    buff.append("</pre>");
    
    buff.append("</body>");
    buff.append("</html>");
    return buff.toString();
  }
  
  @GET
  @Path("/tcp/create")
  @Produces({MediaType.TEXT_PLAIN})
  public synchronized Response createTcpConnectionFactory(
      @Context HttpHeaders headers,
      @QueryParam("jndi-name") String jndiName,
      @QueryParam("host") String host, 
      @QueryParam("port") int port,
      @DefaultValue("org.objectweb.joram.client.jms.tcp.ReliableTcpClient") @QueryParam("reliable-class") String reliableClass,
      @Context UriInfo uriInfo) {
 
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "createTcpConnectionFactory(" + headers + ", " + jndiName + ", " + host + ", " + port + 
          ", " + reliableClass + ", " + uriInfo + ")");

    Response.ResponseBuilder builder = null;
    try {
      // Create tcp connection factory
      ConnectionFactory cf = null;
      if (host == null)
        cf = helper.createTcpConnectionFactory();
      else
        cf = helper.createTcpConnectionFactory(host, port, reliableClass);

      // bind the connection factory
      if (jndiName != null)
        helper.rebind(jndiName, cf);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "createTcpConnectionFactory cf = " + cf);

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
    //TODO: links ?
    return builder.build();
  }

  @GET
  @Path("/local/create")
  @Produces({MediaType.TEXT_PLAIN})
  public synchronized Response createLocalConnectionFactory(
      @Context HttpHeaders headers,
      @QueryParam("jndi-name") String jndiName,
      @Context UriInfo uriInfo) {
 
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "createLocalConnectionFactory(" + headers + ", " + jndiName + ", " + uriInfo + ")");

    Response.ResponseBuilder builder = null;
    try {
      //Create local connection factory
      ConnectionFactory cf = helper.createlocalConnectionFactory();

      // bind the local connection factory
      if (jndiName != null)
        helper.rebind(jndiName, cf);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "createLocalConnectionFactory cf = " + cf);

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
    //TODO: links ?
    return builder.build();
  }

  @GET
  @Path("/queue")
  @Produces({MediaType.TEXT_PLAIN})
  public String listQueue(
      @Context HttpHeaders headers,
      @QueryParam("server-id") int serverId,
      @Context UriInfo uriInfo) throws ConnectException, AdminException {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "listQueue(" + headers + ", " + serverId + ", " + uriInfo + ")");

    if (serverId == -1)
      serverId = helper.getLocalServerId();
    ArrayList<Destination> queues = helper.getQueueNames(serverId);
    StringBuilder buff = new StringBuilder();
    for (Destination queue : queues) {
      buff.append(queue.getAdminName()).append("\n");
    }
    return buff.toString();
  }
  
  @GET
  @Path("/queue/{destName}")
  @Produces({MediaType.TEXT_PLAIN})
  public synchronized Response createQueue(
      @Context HttpHeaders headers,
      @PathParam("destName") String destName,
      @DefaultValue("org.objectweb.joram.mom.dest.Queue") @QueryParam("class-name") String className,
      @QueryParam("server-id") int serverId,
      @Context UriInfo uriInfo) {
    return createQueue(headers, destName, className, serverId, true, true, true, null, uriInfo, null);
  }
  
  @POST
  @Path("/queue/{destName}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  public synchronized Response createQueue(
      @Context HttpHeaders headers,
      @PathParam("destName") String destName,
      @DefaultValue("org.objectweb.joram.mom.dest.Queue") @QueryParam("class-name") String className,
      @DefaultValue("-1") @QueryParam("server-id") int serverId,
      @DefaultValue("false") @QueryParam("free-reading") boolean freeReading,
      @DefaultValue("false") @QueryParam("free-writing") boolean freeWriting,
      @DefaultValue("true") @QueryParam("jndi-bind") boolean bind,
      @QueryParam("jndi-name") String jndiName,
      @Context UriInfo uriInfo,
      String json) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "POST: " + uriInfo.getAbsolutePathBuilder());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "createQueue(" + headers + ", " + destName + ", " + className + ", " + serverId + 
          freeReading + ", " + freeWriting + ", " + bind + ", " + uriInfo + ")");

    Response.ResponseBuilder builder = null;
    Properties props = null;
    if (json != null) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "json = " + json);
      Gson gson = new GsonBuilder().create();
      props = gson.fromJson(json, Properties.class);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "props = " + props);
    }

    try {
      Destination dest = null;
      // create the queue
      if (props != null) {
        if (props.containsKey("className"))
          className = props.getProperty("className");
        if (serverId == -1)
          serverId = helper.getLocalServerId();
        if (props.containsKey("serverId"))
          serverId = Integer.parseInt(props.getProperty("serverId"));
        dest = helper.createQueue(serverId, destName, className, props);
      } else if (serverId > -1) {
        dest = helper.createQueue(serverId, destName);
      } else {
        dest = helper.createQueue(destName);
      }
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,"dest = " + dest);

      // set right
      if (freeReading)
        dest.setFreeReading();
      if (freeWriting)
        dest.setFreeWriting();

      if (bind || jndiName != null) {
        String name = dest.getAdminName();
        if (jndiName != null)
          name = jndiName;
        helper.rebind(name, dest);
      }

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
    //TODO: links ?
    return builder.build();
  }

  @GET
  @Path("/topic")
  @Produces({MediaType.TEXT_PLAIN})
  public String listTopic(
      @Context HttpHeaders headers,
      @QueryParam("server-id") int serverId,
      @Context UriInfo uriInfo) throws ConnectException, AdminException {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "listTopic(" + headers + ", " + serverId + ", " + uriInfo + ")");

    if (serverId == -1)
      serverId = helper.getLocalServerId();
    ArrayList<Destination> topics = helper.getTopicNames(serverId);
    StringBuilder buff = new StringBuilder();
    for (Destination topic : topics) {
      buff.append(topic.getAdminName()).append("\n");
    }
    return buff.toString();
  }
  
  @GET
  @Path("/topic/{destName}")
  @Produces({MediaType.TEXT_PLAIN})
  public synchronized Response createTopic(
      @Context HttpHeaders headers,
      @PathParam("destName") String destName,
      @DefaultValue("org.objectweb.joram.mom.dest.Topic") @QueryParam("class-name") String className,
      @QueryParam("server-id") int serverId,
      @Context UriInfo uriInfo) {
    return createQueue(headers, destName, className, serverId, true, true, true, null, uriInfo, null);
  }
  
  @POST
  @Path("/topic/{destName}")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  public synchronized Response createTopic(
      @Context HttpHeaders headers,
      @PathParam("destName") String destName,
      @DefaultValue("org.objectweb.joram.mom.dest.Topic") @QueryParam("class-name") String className,
      @QueryParam("server-id") int serverId,
      @DefaultValue("false") @QueryParam("free-reading") boolean freeReading,
      @DefaultValue("false") @QueryParam("free-writing") boolean freeWriting,
      @DefaultValue("true") @QueryParam("jndi-bind") boolean bind,
      @QueryParam("jndi-name") String jndiName,
      @Context UriInfo uriInfo,
      String json) {
 
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "POST: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "createTopic(" + headers + ", " + destName + ", " + className + ", " + serverId + 
          freeReading + ", " + freeWriting + ", " + bind + ", " + uriInfo + ")");

    Response.ResponseBuilder builder = null;
    Properties props = null;
    if (json != null) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "json = " + json);
      Gson gson = new GsonBuilder().create();
      props = gson.fromJson(json, Properties.class);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,"props = " + props);
    }

    try {
      Destination dest = null;
      // create the queue
      if (props != null) {
        if (props.containsKey("className"))
          className = props.getProperty("className");
        if (serverId == -1)
          serverId = helper.getLocalServerId();
        if (props.containsKey("serverId"))
          serverId = Integer.parseInt(props.getProperty("serverId"));
        dest = helper.createTopic(serverId, destName, className, props);
      } else if (serverId > -1) {
        dest = helper.createTopic(serverId, destName);
      } else {
        dest = helper.createTopic(destName);
      }
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "dest = " + dest);

      // set right
      if (freeReading)
        dest.setFreeReading();
      if (freeWriting)
        dest.setFreeWriting();

      if (bind || jndiName != null) {
        String name = dest.getAdminName();
        if (jndiName != null)
          name = jndiName;
        helper.rebind(name, dest);
      }

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
    //TODO: links ?
    return builder.build();
  }

  @GET
  @Path("/user/{userName}")
  @Produces({MediaType.TEXT_PLAIN})
  public synchronized Response createUser(
      @Context HttpHeaders headers,
      @PathParam("userName") String userName,
      @NotNull @QueryParam("password")String password,
      @Context UriInfo uriInfo) {
    return createUser(headers, userName, password, -1, SimpleIdentity.class.getName(), uriInfo, null);
  }
  
  @POST
  @Path("/user/{userName}")
  @Produces({MediaType.TEXT_PLAIN})
  @Consumes(MediaType.APPLICATION_JSON)
  public synchronized Response createUser(
      @Context HttpHeaders headers,
      @PathParam("userName") String userName,
      @NotNull @QueryParam("password")String password,
      @DefaultValue("-1") @QueryParam("server-id") int serverId,
      @DefaultValue("org.objectweb.joram.shared.security.SimpleIdentity") @QueryParam("identity-class-name") String identityClassName,
      @Context UriInfo uriInfo,
      String json) {
 
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "POST: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "createUser(" + headers + ", " + userName + ", " + uriInfo + ")");

    Response.ResponseBuilder builder = null;

    if (password == null) {
      builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("The password is null.");
      return builder.build();
    }

    Properties props = null;
    if (json != null) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "json = " + json);
      Gson gson = new GsonBuilder().create();
      props = gson.fromJson(json, Properties.class);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "props = " + props);
    }

    try {
      // Create the user
      User user = null;
      if (props == null) {
        user = helper.createUser(userName, password);
      } else {
        if (serverId < 0)
          serverId = helper.getLocalServerId();
        user = helper.createUser(userName, password, serverId, identityClassName, props);
      }

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "user = " + user);

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
    //TODO: links ?
    return builder.build();
  }
  
  @DELETE
  @Path("/user/{userName}")
  @Produces({MediaType.TEXT_PLAIN})
  public synchronized Response deleteUser(
      @Context HttpHeaders headers,
      @PathParam("userName") String userName,
      @NotNull @QueryParam("password") String password,
      @DefaultValue("-1") @QueryParam("server-id") int serverId,
      @Context UriInfo uriInfo) {
 
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "DELETE: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "deleteUser(" + headers + ", " + userName + ", " + uriInfo + ")");

    Response.ResponseBuilder builder = null;
    
    if (password == null) {
      builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("The password is null.");
      return builder.build();
    }
    
    try {
      // delete the user
      if (serverId < 0)
        serverId = helper.getLocalServerId();
      helper.deleteUser(userName, password, serverId);

    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, e);
      if (e instanceof JMSSecurityRuntimeException)
        builder = Response.status(Response.Status.UNAUTHORIZED).entity(e.toString());
      else
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
      return builder.build();
    }

    builder = Response.status(Response.Status.OK);
    return builder.build();
  }

  @DELETE
  @Path("/queue/{destName}")
  @Produces(MediaType.TEXT_PLAIN)
  public synchronized Response deleteQueue(
      @Context HttpHeaders headers,
      @PathParam("destName") String destName,
      @DefaultValue("-1") @QueryParam("server-id") int serverId,
      @DefaultValue("true") @QueryParam("jndi-unbind") boolean unbind,
      @QueryParam("jndi-name") String jndiName,
      @Context UriInfo uriInfo) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "DELETE: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "deleteQueue(" + headers + ", " + destName + ", " + ", " + serverId + 
          ", " + unbind + ", " + uriInfo + ")");
    
    Response.ResponseBuilder builder = null;
    try {
      // delete the queue
      if (serverId < 0)
        serverId = helper.getLocalServerId();
      Destination dest = helper.createQueue(serverId, destName);
      String name = dest.getAdminName();
      dest.delete();
      
      if (unbind || jndiName != null) {
        if (jndiName != null)
          name = jndiName;
        helper.unbind(name);
      }

    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, e);
      if (e instanceof JMSSecurityRuntimeException)
        builder = Response.status(Response.Status.UNAUTHORIZED).entity(e.toString());
      else
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
      return builder.build();
    }

    builder = Response.status(Response.Status.OK);
    return builder.build();
  }
  
  @DELETE
  @Path("/topic/{destName}")
  @Produces(MediaType.TEXT_PLAIN)
  public synchronized Response deleteTopic(
      @Context HttpHeaders headers,
      @PathParam("destName") String destName,
      @DefaultValue("-1") @QueryParam("server-id") int serverId,
      @DefaultValue("true") @QueryParam("jndi-unbind") boolean unbind,
      @QueryParam("jndi-name") String jndiName,
      @Context UriInfo uriInfo) {

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "DELETE: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "deleteTopic(" + headers + ", " + destName + ", " + ", " + serverId + 
          ", " + unbind + ", " + uriInfo + ")");
    
    Response.ResponseBuilder builder = null;
    try {
      // delete the topic
      if (serverId < 0)
        serverId = helper.getLocalServerId();
      Destination dest = helper.createTopic(serverId, destName);
      String name = dest.getAdminName();
      dest.delete();
      
      if (unbind || jndiName != null) {
        if (jndiName != null)
          name = jndiName;
        helper.unbind(name);
      }

    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, e);
      if (e instanceof JMSSecurityRuntimeException)
        builder = Response.status(Response.Status.UNAUTHORIZED).entity(e.toString());
      else
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString());
      return builder.build();
    }

    builder = Response.status(Response.Status.OK);
    return builder.build();
  }
  
  @Override
  public void filter(ContainerRequestContext requestContext)
      throws IOException {

    if (!helper.authenticationRequired()) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "no authentication.");
      // no authentication
      return;
    }
    
    // request headers
    final MultivaluedMap<String, String> headers = requestContext.getHeaders();
    // authorization header
    final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "authorization = " + authorization);
    
    if (authorization == null) {
      Response response = Response.status(Response.Status.UNAUTHORIZED)
          .header("WWW-Authenticate", "Basic realm=\"executives\"")
          .entity("You cannot access this resource").build();
      requestContext.abortWith(response);
      return;
    }
    
    // get encoded username and password
    final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");

    // decode username and password
    String usernameAndPassword = new String(DatatypeConverter.parseBase64Binary(encodedUserPassword));
    final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
    final String username = tokenizer.nextToken();
    final String password = tokenizer.nextToken();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "username = " + username);
    
    // Verifying username and password
    if (helper.getRestAdminRoot().equals(username) && helper.getRestAdminPass().equals(password)) {
      // the valid authentication
      return;
    }
    
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN, "Bad authorization: " + username + ":" + password);

    Response response = Response.status(Response.Status.UNAUTHORIZED)
        .header("WWW-Authenticate", "Basic realm=\"executives\"")
        .entity("You cannot access this resource").build();
    requestContext.abortWith(response);
    return;
  }
}
