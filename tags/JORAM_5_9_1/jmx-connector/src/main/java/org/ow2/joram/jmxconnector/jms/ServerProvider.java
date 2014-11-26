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

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerProvider;
import javax.management.remote.JMXServiceURL;

import org.ow2.joram.jmxconnector.server.JMSConnectorServer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * <p>A provider for creating JMS connector servers.</p>
 */
public class ServerProvider implements JMXConnectorServerProvider {
  /***
   * Create a new JMSConnectorServer using the given URL. Each successful
   * call to this method produces a different <code>JMSConnectorServer</code>
   * object.</p>
   *
   * @param url the address of the JMS provider to connect to. A correct
   * URL typically has the form <code>service:jmx:jms://host:port/name</code>.
   *
   * @param env a read-only Map containing named attributes to determine
   * how the connection is made.
   *
   * @param mbs the MBean server that this connector server
   * is attached to.
   *
   * @return the created JMSConnectorServer.
   *
   * @exception NullPointerException if <code>url</code> or
   * <code>env</code> is null.
   *
   * @exception IOException this provider throws {@code MalformedURLException}
   * if the protocol in the {@code url} is not recognized.
   */
  public JMXConnectorServer newJMXConnectorServer(JMXServiceURL url,
                                                  Map env,
                                                  MBeanServer mbs) throws IOException {
    System.err.println("URL: " + url.getProtocol() + ", " + url.getHost() + ", " + url.getPort() + ", " + url.getURLPath());
    String protocol = url.getProtocol();
    if ("jms".equals(protocol))
      return new JMSConnectorServer(url, env, mbs);
    
    throw new MalformedURLException("Unknown protocol " + protocol + " for provider: " + this.getClass().getName());
  }
}
