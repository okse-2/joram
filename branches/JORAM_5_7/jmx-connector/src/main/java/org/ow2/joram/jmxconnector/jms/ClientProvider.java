/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): D.E. Boumchedda (ScalAgent D.T.)
 * Contributor(s): A. Freyssinet (ScalAgent D.T.)
 */
package org.ow2.joram.jmxconnector.jms;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorProvider;
import javax.management.remote.JMXServiceURL;

import org.ow2.joram.jmxconnector.client.JMSConnector;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * <p>A client provider for creating JMS connector clients.
 */
public class ClientProvider implements JMXConnectorProvider {
  /***
   * <p>Create a new JMSConnector client that is ready to connect to
   * the JMS provider at the given address.  Each successful call
   * to this method produces a different <code>JMSConnector</code>
   * object.</p>
   * 
   * @param url the address of the JMS provider to connect to. A correct
   * URL typically has the form <code>service:jmx:jms://host:port/name</code>.
   * 
   * @param env a read-only Map containing named attributes to determine
   * how the connection is made.
   * 
   * @return the created JMSConnector
   * 
   * @throws IOException This provider throws {@code MalformedURLException}
   * if the protocol in the {@code url} is not recognized.
   */
  public JMXConnector newJMXConnector(JMXServiceURL url, Map env) throws IOException {
    System.err.println("URL: " + url.getProtocol() + ", " + url.getHost() + ", " + url.getPort() + ", " + url.getURLPath());
    String[] credentials = (String[]) env.get("jmx.remote.credentials");
    if (credentials != null) {
      for (int i=0; i<credentials.length; i++) 
        System.err.println(credentials[i]);
    }
    
    String protocol = url.getProtocol();
    if ("jms".equals(protocol))
      return new JMSConnector(env, url);
    
    throw new MalformedURLException("Unknown protocol " + protocol + " for provider: " + this.getClass().getName());
  }
}
