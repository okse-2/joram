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
  public final static String PORT_PROPERTY = "java.naming.factory.port";

  /**
   *  This property which defines the host name must be passed
   *  when creating an initial context using this factory.
   */
  public final static String HOST_PROPERTY = "java.naming.factory.host";


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
    try {
      String host = null;
      String portStr = null;

      // URL should be as: joram://host:port, or as: host:port      
      String url = null;
      if (env != null) {
        url = (String) env.get(Context.PROVIDER_URL);
      }      
      if (url == null)
        url = System.getProperty(Context.PROVIDER_URL, null);
    
      if (url != null) {
        if (url.startsWith("scn")) {
          int indexOfHost = url.indexOf("//") == -1 ? 0 : url.indexOf("//") + 2; 
          int indexOfPort = url.indexOf(":", indexOfHost) + 1;
        
          host = url.substring(indexOfHost, indexOfPort - 1);
          portStr = url.substring(indexOfPort);
        }
      }
    
      if (host == null && env != null)
        host = (String) env.get(HOST_PROPERTY);
      if (host == null)
        host = System.getProperty(HOST_PROPERTY, null);
      if (host == null) {
        //default host
        host = "localhost";
      }
    
      if (portStr == null && env != null)
        portStr = (String) env.get(PORT_PROPERTY);
      if (portStr == null)
        portStr = System.getProperty(PORT_PROPERTY, null);
      if (portStr == null) {
        //default port
        portStr = "16400";
      }
      int port = Integer.parseInt(portStr);

      return new NamingConnection(host, port);
    } catch (ClassCastException e) {
      NamingException nx = 
        new NamingException(
          "ClassCastException!  Are " + 
          HOST_PROPERTY + " and " + 
          PORT_PROPERTY + " String objects?");
      nx.setRootCause(e);
      throw nx;
    } catch (NumberFormatException e) {
      NamingException nx = 
        new NamingException("the " + PORT_PROPERTY + 
                            " is not a valid integer");
      nx.setRootCause(e);
      throw nx;
    } catch (Exception e) {
      NamingException nx = 
        new NamingException(
          "exception creating NamingContext: " +
          e.toString());
      nx.setRootCause(e);
      throw nx;
    }
  }
}
