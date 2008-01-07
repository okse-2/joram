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
 * Initial developer(s): David Feliot
 */
package fr.dyade.aaa.jndi2.haclient;

import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.jndi2.client.NamingConnection;
import fr.dyade.aaa.jndi2.client.Trace;

public class HANamingContextFactory implements InitialContextFactory {

	  public HANamingContextFactory() {
	  }


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
        "HANamingContextFactory.getInitialContext(" + env + ')');
    return new fr.dyade.aaa.jndi2.client.NamingContextImpl(
      getNamingConnection(env), 
      new CompositeName());    
  }

  private static String getEnvProperty(Hashtable env, 
                                       String propName) {
    String value = null;
    if (env != null) {
      value = (String) env.get(propName);
    }      
    if (value == null) {
      value = System.getProperty(propName, null);
    }
    return value;
  }

  public static NamingConnection getNamingConnection(
    Hashtable env) 
    throws NamingException {    
    try {
      NamingConnection namingConnection;
      
      // URL should be as: hascn://host:port
      String url = getEnvProperty(env, "hascn.naming.provider.url");
      
      if (url == null) {
        url = getEnvProperty(env, Context.PROVIDER_URL);
      }
    
      if (url != null) {
        StringTokenizer tokenizer = new StringTokenizer(url, "/:,");
        if (! tokenizer.hasMoreElements()) 
          throw new NamingException("URL not valid:" + url);
        String protocol = tokenizer.nextToken();        
        if (protocol.equals("hascn")) {
          HANamingConnection haNamingConnection = 
            new HANamingConnection();
          while (tokenizer.hasMoreElements()) {
            haNamingConnection.addServerAddress(
              tokenizer.nextToken(),
              Integer.parseInt(tokenizer.nextToken()));
          }
          namingConnection = haNamingConnection;
        } else {
          throw new NamingException("Unknown protocol:" + protocol);
        }
      } else {
        throw new NamingException(
          "URL " + Context.PROVIDER_URL + " not defined");
      }
      return namingConnection;
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
