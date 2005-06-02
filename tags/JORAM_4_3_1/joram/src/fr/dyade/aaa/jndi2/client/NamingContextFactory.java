/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): David Feliot, Nicolas Tachker
 */
package fr.dyade.aaa.jndi2.client;

import javax.naming.spi.*;
import javax.naming.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class NamingContextFactory implements InitialContextFactory {

  /**
   *  This property which defines the listener port must be passed
   *  when creating an initial context using this factory.
   */
  public final static String JAVA_PORT_PROPERTY = "java.naming.factory.port";

  /**
   *  This property which defines the host name must be passed
   *  when creating an initial context using this factory.
   */
  public final static String JAVA_HOST_PROPERTY = "java.naming.factory.host";

  /**
   * Specific property. It is useful when several naming provider use
   * the same property.
   */
  public final static String SCN_PORT_PROPERTY = "scn.naming.factory.port";

  /**
   * Specific property. It is useful when several naming provider use
   * the same property.
   */
  public final static String SCN_HOST_PROPERTY = "scn.naming.factory.host";

  /**
   * Specific property. It is useful when several naming provider use
   * the same property.
   */
  public final static String SCN_PROVIDER_URL = "scn.naming.provider.url";

  /**
   * @param  env  This contains the hostname and the port.
   * @return  A JNDI initial context.
   * @exception  NamingException  Thrown if the host and port properties 
   * aren't strings, if the port string does not represent a valid number, 
   * or if an exception is thrown from the NamingContext constructor.
   */
  public Context getInitialContext(Hashtable env)
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(
        BasicLevel.DEBUG, 
        "NamingContextFactory.getInitialContext(" + env + ')');
    return new fr.dyade.aaa.jndi2.client.NamingContextImpl(
      getNamingConnection(env), 
      new CompositeName());    
  }

  public static NamingConnection getNamingConnection(
    Hashtable env) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(
        BasicLevel.DEBUG, 
        "NamingContextFactory.getNamingConnection(" + env + ')'); 
    try {
      NamingConnection namingConnection;
      // The URL format is scn://host:port
      String url = (String) env.get(SCN_PROVIDER_URL);
      if (url == null) url = (String) env.get(Context.PROVIDER_URL);
      if (url != null && !url.equals("")) {
        StringTokenizer tokenizer = new StringTokenizer(url, "/:,");
        if (! tokenizer.hasMoreElements()) 
          throw new NamingException("URL not valid:" + url);
        String protocol = tokenizer.nextToken();        
        if (protocol.equals("scn")) {
          String host = tokenizer.nextToken();
          String portStr = tokenizer.nextToken();
          int port = Integer.parseInt(portStr);
          namingConnection = new SimpleNamingConnection(host, port, env);
        } else {
          throw new NamingException("Unknown protocol:" + protocol);
        }
      } else {        
        String host = (String) env.get(SCN_HOST_PROPERTY);
        if (host == null) host = (String) System.getProperty(SCN_HOST_PROPERTY);
        if (host == null) host = (String) env.get(JAVA_HOST_PROPERTY);
        if (host == null) host = (String) System.getProperty(JAVA_HOST_PROPERTY);
        if (host == null) host = "localhost";

        String portStr = (String) env.get(SCN_PORT_PROPERTY);
        if (portStr == null) portStr = (String) System.getProperty(SCN_PORT_PROPERTY);
        if (portStr == null) portStr = (String) env.get(JAVA_PORT_PROPERTY);
        if (portStr == null) portStr = (String) System.getProperty(JAVA_PORT_PROPERTY);
        if (portStr == null) portStr = "16400";

        int port = Integer.parseInt(portStr);
        namingConnection = new SimpleNamingConnection(
          host, port, env);
      }
      return namingConnection;
    } catch (NumberFormatException e) {
      NamingException nx = new NamingException();
      nx.setRootCause(e);
      throw nx;
    } catch (Exception e) {
      NamingException nx = new NamingException();
      nx.setRootCause(e);
      throw nx;
    }
  }
}
