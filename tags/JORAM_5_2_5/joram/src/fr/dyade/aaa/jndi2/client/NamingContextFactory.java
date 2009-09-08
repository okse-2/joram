/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.jndi2.client;

import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.objectweb.util.monolog.api.BasicLevel;

public class NamingContextFactory implements InitialContextFactory {
  /**
   * Name of the property which defines the listener port used when
   * creating an initial context using this factory.
   */
  public final static String JAVA_PORT_PROPERTY = "java.naming.factory.port";

  /**
   * Name of the property which defines the host name used when
   * creating an initial context using this factory.
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
  public Context getInitialContext(Hashtable env) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "NamingContextFactory.getInitialContext(" + env + ')');

    return new NamingContextImpl(getNamingConnection(env), 
                                 new CompositeName());    
  }

  public static NamingConnection getNamingConnection(Hashtable env) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "NamingContextFactory.getNamingConnection(" + env + ')');

    try {
      String host;
      int port;
      
      // The URL format is scn://host:port
      String url = (String) env.get(SCN_PROVIDER_URL);
      if (url == null)
        url = (String) env.get(Context.PROVIDER_URL);
      if (url != null && !url.equals("")) {
        StringTokenizer tokenizer = new StringTokenizer(url, "/:,");
        if (! tokenizer.hasMoreElements()) 
          throw new NamingException("URL not valid:" + url);
        String protocol = tokenizer.nextToken();        
        if (protocol.equals("scn")) {
          host = tokenizer.nextToken();
          String portStr = tokenizer.nextToken();
          port = Integer.parseInt(portStr);
        } else {
          throw new NamingException("Unknown protocol:" + protocol);
        }
      } else {        
        host = (String) env.get(SCN_HOST_PROPERTY);
        if (host == null)
          host = System.getProperty(SCN_HOST_PROPERTY);
        if (host == null) 
          host = (String) env.get(JAVA_HOST_PROPERTY);
        if (host == null)
          host = System.getProperty(JAVA_HOST_PROPERTY);
        if (host == null)
          host = "localhost";

        String portStr = (String) env.get(SCN_PORT_PROPERTY);
        if (portStr == null)
          portStr = System.getProperty(SCN_PORT_PROPERTY);
        if (portStr == null)
          portStr = (String) env.get(JAVA_PORT_PROPERTY);
        if (portStr == null)
          portStr = System.getProperty(JAVA_PORT_PROPERTY);
        if (portStr == null)
          portStr = "16400";

        port = Integer.parseInt(portStr);
      }

      SimpleNamingConnection namingConnection = new SimpleNamingConnection(host, port, env);
      namingConnection.init(host, port, env);

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
