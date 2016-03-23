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
package org.objectweb.joram.tools.rest.jmx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.inject.Singleton;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.DatatypeConverter;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.google.gson.stream.JsonWriter;

import fr.dyade.aaa.common.Debug;

@Path("/")
@Singleton
public class JmxRestService implements ContainerRequestFilter {

  private static final String AUTHORIZATION_PROPERTY = "Authorization";
  private static final String AUTHENTICATION_SCHEME = "Basic";
  
  public static Logger logger = Debug.getLogger(JmxRestService.class.getName());
  static MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
  private final JmxHelper helper = JmxHelper.getInstance();
  
  public static final String HTML_PATH = "htlm";
  public static final String DOMAINS = "domains";
  public static final String OBJECT_NAME = "object-name";
  public static final String ATTRIBUTE_NAME = "attribute-name";
 
  @GET
  @Path("/help")
  @Produces(MediaType.TEXT_HTML)
  public String info(@Context UriInfo uriInfo) {
    StringBuilder buff = new StringBuilder();
    buff.append("<html>");
    buff.append("<body>");
    
    buff.append("<h3>Browse (GET)</h3>");
    buff.append("<a href=\"");
    buff.append(uriInfo.getBaseUri());
    buff.append("\">");
    buff.append("Browse JMX NBeans");
    buff.append("</a>");
    buff.append("<br>");
    
    buff.append("<h3>domains (GET)</h3>");
    buff.append("<a href=\"");
    buff.append(uriInfo.getBaseUriBuilder().path(DOMAINS).build());
    buff.append("\">");
    buff.append(uriInfo.getBaseUriBuilder().path(DOMAINS).build());
    buff.append("</a>");
    buff.append("<br>");
    
    buff.append("</body>");
    buff.append("</html>");
    return buff.toString();
  }
  
  @GET
  @Path("/" + DOMAINS)
  @Produces(MediaType.TEXT_HTML)
  public String getDomains(@Context UriInfo uriInfo) {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "getDomains(" + uriInfo + ")");

    StringBuilder buff = new StringBuilder();
    buff.append("<html><body>\n");
    String domains[] = mbs.getDomains();
    for (String domain : domains) {
      buff.append("<br>  <a href=\"");
      buff.append(uriInfo.getAbsolutePathBuilder()
          .path(domain));
      buff.append("\">");
      buff.append(domain);
      buff.append("</a>");
    }
    buff.append("</body></html>");
    return buff.toString();
  }
  
  @GET
  @Path("/" + DOMAINS)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDomainsJson(@Context UriInfo uriInfo) throws Exception {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "getDomainsJson(" + uriInfo + ")");
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    ResponseBuilder builder = Response.status(Response.Status.OK);
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
    writer.setIndent("  ");
    String[] domains = mbs.getDomains();
    writer.beginArray();
    for (String domain : domains) {
      writer.value(domain);
      //link to this domain
      builder.link(uriInfo.getAbsolutePathBuilder().path(domain).build(), domain);
    }
    writer.flush();
    writer.endArray();
    writer.close();
    String json =  out.toString();
    // add Json to the response
    builder.entity(json);
    
    return builder.build();
  }
  
  @GET
  @Path("/"+ DOMAINS + "/{domain}")
  @Produces(MediaType.TEXT_HTML)
  public String getDomain(
      @PathParam("domain") String domain,
      @Context UriInfo uriInfo) throws MalformedObjectNameException, NullPointerException {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "getDomain(" + domain + ", " + uriInfo + ")");
    
    StringBuilder buff = new StringBuilder();
    buff.append("<html><body>\n");
    Set<ObjectName> ObjectNames = mbs.queryNames(ObjectName.getInstance(domain + ":*"), null);
    for (ObjectName objectName : ObjectNames) {
      buff.append("<br>  <a href=\"");
      buff.append(uriInfo.getAbsolutePathBuilder()
          .path(objectName.getCanonicalName()));
      buff.append("\">");
      buff.append(objectName.getCanonicalName());
      buff.append("</a>");
    }
    buff.append("</body></html>");
    return buff.toString();
  }
  
  @GET
  @Path("/" + DOMAINS + "/{domain}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDomainJson(
      @PathParam("domain") String domain,
      @Context UriInfo uriInfo) throws Exception {
    
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "getDomainJson(" + domain + ", " + uriInfo + ")");
    
    ResponseBuilder builder = Response.status(Response.Status.OK);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
    writer.setIndent("  ");
    
    Set<ObjectName> ObjectNames = mbs.queryNames(ObjectName.getInstance(domain + ":*"), null);
    writer.beginObject();
    int i = 0;
    for (ObjectName objectName : ObjectNames) {
      URI uri = uriInfo.getAbsolutePathBuilder()
          .path(objectName.getCanonicalName()).build();
      //link to this objectName
      String name = "objectName" + (i++);
      builder.link(uri, name);
      writer.name(objectName.getCanonicalName());
      writer.value(name);
    }
    writer.flush();
    writer.endObject();
    writer.close();
    String json = out.toString();
    // add Json to the response
    builder.entity(json);

    return builder.build();
  }
  
  @GET
  @Path("/domains/{domain}/{objectName}")
  @Produces(MediaType.TEXT_HTML)
  public String getObjectName(
      @PathParam("domain") String domain,
      @PathParam("objectName") String objName,
      @Context UriInfo uriInfo) 
          throws IntrospectionException, InstanceNotFoundException, MalformedObjectNameException, 
          ReflectionException, NullPointerException {
    
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "getObjectName(" + domain + ", " + objName + ", " + uriInfo + ")");
    
    StringBuilder buff = new StringBuilder();
    buff.append("<html><body>\n");
    ObjectName objectName = ObjectName.getInstance(objName);
    MBeanInfo mbeanInfo = mbs.getMBeanInfo(objectName);
    MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
    for (MBeanAttributeInfo attribute : attributes) {
      buff.append("<br>  <a href=\"");
      buff.append(uriInfo.getAbsolutePathBuilder()
          .path(attribute.getName()));
      buff.append("\">");
      buff.append(attribute.getName());
      buff.append("</a>\t=\t");
      try {
        buff.append(getAttribute(objectName, attribute.getName()));
      } catch (Exception e) {
        buff.append(e.getClass().getName() + ": " + e.getMessage());
      }
      buff.append("\n");
    }
    buff.append("</body></html>");
    return buff.toString();
  }
  
  @GET
  @Path("/domains/{domain}/{objectName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getObjectNameJson(
      @PathParam("domain") String domain,
      @PathParam("objectName") String objName,
      @Context UriInfo uriInfo) 
          throws Exception {
    
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "getObjectNameJson(" + domain + ", " + objName + ", " + uriInfo + ")");
    
    ResponseBuilder builder = Response.status(Response.Status.OK);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
    writer.setIndent("  ");
    ObjectName objectName = ObjectName.getInstance(objName);
    MBeanInfo mbeanInfo = mbs.getMBeanInfo(objectName);
    MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
    writer.beginObject();
    for (MBeanAttributeInfo attribute : attributes) {
      //link
      builder.link(uriInfo.getAbsolutePathBuilder()
          .path(attribute.getName()).build(), attribute.getName());
      writer.name(attribute.getName());
      try {
        writer.value(getAttribute(objectName, attribute.getName()));
      } catch (Exception e) {
        writer.value(e.getClass().getName() + ": " + e.getMessage());
      }
    }
    writer.flush();
    writer.endObject();
    writer.close();
    String json = out.toString();
    // add Json to the response
    builder.entity(json);

    return builder.build();
  }
  
  @GET
  @Path("/domains/{domain}/{objectName}/{attribute}")
  @Produces(MediaType.TEXT_PLAIN)
  public String getAttribute(
      @PathParam("domain") String domain,
      @PathParam("objectName") String objectName,
      @PathParam("attribute") String attribute,
      @Context UriInfo uriInfo) throws Exception {
    
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "getAttribute(" + domain + ", " + objectName + ", " + uriInfo + ")");
    
    return getAttribute(ObjectName.getInstance(objectName), attribute);
  }
  
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getMBeanInfo(@Context UriInfo uriInfo) {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "getMBeanInfo(" + uriInfo + ")");
    
    StringBuilder buff = new StringBuilder();
    buff.append("<html><body>\n");
    
    buff.append("<head>\n");
    buff.append("<style media=\"screen\" type=\"text/css\">\n");
    
    /* CSS */
    buff.append("input {\n");
    buff.append("  display: none;\n");
    buff.append("}\n");
    
    buff.append("input ~ ul {\n");
    buff.append("  display: none;\n");
    buff.append("  }\n");
    buff.append(" input:checked ~ ul {\n");
    buff.append("  display: block;\n");
    buff.append(" }\n");
    
    buff.append("input ~ .fa-angle-double-down {\n");
    buff.append("  display: none;\n");
    buff.append(" }\n");
    buff.append(" input:checked ~ .fa-angle-double-right {\n");
    buff.append("  display: none;\n");
    buff.append(" }\n");
    buff.append(" input:checked ~ .fa-angle-double-down {\n");
    buff.append("  display: inline;\n");
    buff.append(" }\n");
    
    buff.append("li {\n");
    buff.append("  display: block;\n");
    buff.append("  font-family: 'Arial';\n");
    buff.append("  font-size: 15px;\n");
    buff.append("  padding: 0.2em;\n");
    buff.append("  border: 1px solid transparent;\n");
    buff.append("}\n");
    
    buff.append("li:hover {\n");
    buff.append("  border: 1px solid grey;\n");
    buff.append("  border-radius: 3px;\n");
    buff.append("  background-color: lightgrey;\n");
    buff.append("}\n");
    
    buff.append(" </style>\n");
    buff.append("</head>\n");
    
    // help
    buff.append("<a href=\"");
    buff.append(uriInfo.getBaseUri());
    buff.append("help\">");
    buff.append("help</a>");
    buff.append("<br>");
    buff.append("<br>");
    //buff.append("<h3>MBeans browser</h3>");
    buff.append("<h3 style=\"background-color:#dcdcdc; padding:10px;\">MBeans browser</h3>");
    buff.append("<br>");
    try {
      buff.append("<div id=\"tree\">\n");
      HashMap<String, HashMap<String, ArrayList<ObjectName>>> domains = beanToTree();
      int i = 0;
      for (Entry<String, HashMap<String, ArrayList<ObjectName>>> entry : domains.entrySet()) {
        String domain = entry.getKey();
        openDomain(buff, domain, ++i);
        for (Entry<String, ArrayList<ObjectName>> typeEntry : entry.getValue().entrySet()) {
          String type = typeEntry.getKey();
          openType(buff, type, ++i);
          for (ObjectName objectName : typeEntry.getValue()) {
            openAttribute(buff, uriInfo, objectName, ++i);
            closeAttribute(buff);
          }
          closeType(buff);
        }
        closeDomain(buff);
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    buff.append("</body></html>");

    return buff.toString();
  }
  
  private HashMap<String, HashMap<String, ArrayList<ObjectName>>> beanToTree() {
    HashMap<String, HashMap<String, ArrayList<ObjectName>>> domains = 
        new HashMap<String, HashMap<String, ArrayList<ObjectName>>>();
    Set<ObjectName> objectNames = new TreeSet<ObjectName>(mbs.queryNames(null, null));
    for (ObjectName objectName: objectNames) {
      // get the current domain
      String domain = objectName.getDomain();
      // get type map for this domain
      HashMap<String, ArrayList<ObjectName>> types = domains.get(domain);
      if (types == null) {
        types = new HashMap<String, ArrayList<ObjectName>>();
        domains.put(domain, types);
      }
      String type = objectName.getKeyProperty("type");
      if (type == null) {
        type = objectName.getKeyProperty("server");
        if (type == null)
          type = objectName.getKeyProperty("host");
      }
      // get attribute list for this type
      ArrayList<ObjectName> attributtes = types.get(type);
      if (attributtes == null) {
        attributtes = new ArrayList<ObjectName>();
        types.put(type, attributtes);
      }
      attributtes.add(objectName);
    }
    return domains;
  }
  
  private void openDomain(StringBuilder buff, String domain, int i) {
    buff.append("<ul>\n");
    buff.append("  <li>\n");
    buff.append("    <input type=\"checkbox\" id=\"domain"+i+"\" />\n");
    buff.append("    <i class=\"fa fa-angle-double-right\"></i>\n");
    buff.append("    <i class=\"fa fa-angle-double-down\"></i>\n");
    buff.append("    <label for=\"domain"+i+"\">");
    buff.append(domain);
    buff.append("</label>\n\n");
  }
  
  private void closeDomain(StringBuilder buff) {
    buff.append("  </li>\n");
    buff.append("</ul>\n");
  }
  
  private void openType(StringBuilder buff, String type, int i) {
    buff.append("  <ul>\n");
    buff.append("    <li>\n");
    buff.append("      <input type=\"checkbox\" id=\"type"+i+"\" />\n");
    buff.append("      <i class=\"fa fa-angle-double-right\"></i>\n");
    buff.append("      <i class=\"fa fa-angle-double-down\"></i>\n");
    buff.append("      <label for=\"type"+i+"\">");
    buff.append(type);
    buff.append("</label>\n\n");
  }
  
  private void closeType(StringBuilder buff) {  
    buff.append("    </li>\n");
    buff.append("  </ul>\n");
  }
  
  private void openAttribute(StringBuilder buff, UriInfo uriInfo, ObjectName objectName, int i) throws Exception {
    buff.append("      <ul>\n");
    buff.append("        <li>\n");
    buff.append("          <input type=\"checkbox\" id=\"objName"+i+"\" />\n");
    buff.append("          <i class=\"fa fa-angle-double-right\"></i>\n");
    buff.append("          <i class=\"fa fa-angle-double-down\"></i>\n");
    buff.append("          <label for=\"objName"+i+"\">");
    buff.append(objectName.getCanonicalKeyPropertyListString());
    buff.append("</label>\n\n");
    
    buff.append("          <ul>\n");
    MBeanAttributeInfo[] attributes = mbs.getMBeanInfo(objectName).getAttributes();
    for (MBeanAttributeInfo attribute : attributes) {
      buff.append("              <li><a href=\"");
      buff.append(uriInfo.getBaseUriBuilder()
          .path(DOMAINS)
          .path(objectName.getDomain())
          .path(objectName.getCanonicalName())
          .path(attribute.getName()));
      buff.append("\">");
      buff.append(attribute.getName());
      buff.append("</a>\t=\t");
      try {
        buff.append(getAttribute(objectName, attribute.getName()));
      } catch (Exception e) {
        buff.append(e.getClass().getName() + ": " + e.getMessage());
      }
      buff.append("\n");
    }
  }
  
  private void closeAttribute(StringBuilder buff) {
    buff.append("              </li>\n");
    buff.append("            </ul>\n");
    buff.append("        </li>\n");
    buff.append("      </ul>\n");
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getAllMBeanInfoJson(
      @Context UriInfo uriInfo) throws Exception {
    
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "GET: " + uriInfo.getAbsolutePathBuilder());
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "getAllMBeanInfoJson(" + uriInfo + ")");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
    writer.setIndent("  ");

    Set<ObjectName> objectNames = new TreeSet<ObjectName>(mbs.queryNames(null, null));
    writer.beginObject();
    for (ObjectName objectName: objectNames) {
      MBeanInfo mbeanInfo = mbs.getMBeanInfo(objectName);
      MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
      writer.name(objectName.toString());
      //writer.setIndent("    ");
      writer.beginArray();
      for (MBeanAttributeInfo attribute : attributes) {
        writer.value(attribute.getName());
      }
      writer.endArray();
    }
    writer.endObject();
    writer.flush();
    writer.close();
    return out.toString();
  }
  
  private String getAttribute(ObjectName objectName, String attributeName) 
      throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
    Object obj = mbs.getAttribute(objectName, attributeName);
    if (obj != null) {
      if (obj.getClass().isArray()) {
        String type = obj.getClass().getSimpleName();
        switch (type) {
        case "int[]":
          return Arrays.toString((int[]) obj);
        case "long[]":
          return Arrays.toString((long[]) obj);
        case "short[]":
          return Arrays.toString((short[]) obj);
        case "boolean[]":
          return Arrays.toString((boolean[]) obj);
        case "char[]":
          return Arrays.toString((char[]) obj);
        case "double[]":
          return Arrays.toString((double[]) obj);
        case "float[]":
          return Arrays.toString((float[]) obj);
        case "byte[]":
          return Arrays.toString((byte[]) obj);
        case "String[]":
          return Arrays.toString((String[]) obj);
        case "Object[]":
          return Arrays.toString((Object[]) obj);

        default:
          return obj.toString();
        }
      }
      return obj.toString();
    }
    return null;
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
    if (helper.getRestJmxRoot().equals(username) && helper.getRestJmxPass().equals(password)) {
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
