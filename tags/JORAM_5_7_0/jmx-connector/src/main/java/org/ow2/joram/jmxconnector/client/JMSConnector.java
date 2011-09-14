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
package org.ow2.joram.jmxconnector.client;

import java.io.IOException;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;


import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * <p>This class implements the {@link JMXConnector} interface. An object
 * of this class is needed to establish a connection to a connector server.</p>
 * 
 * The <code>MBeanServerConnection</code> interface is provided by the
 * <code>MBeanServerConnectionDelegate</code> Class.
 * 
 * @see javax.management.remote.JMXConnector
 */
public class JMSConnector implements JMXConnector {
  private static final Logger logger = Debug.getLogger(JMSConnector.class.getName());
  
  private JMXServiceURL url;
  private Map env;
  
  private Connection cnx = null;
  private boolean connected = false;

  MBeanServerConnectionDelegate mbeanServerConnectionDelegate;


  public JMSConnector(Map env, JMXServiceURL url) {
    this.env = env;
    this.url = url;
  }

  public void connect() throws IOException {
    connect(env);
  }

  public void connect(Map<String, ?> env) throws IOException {
    try {
      if (cnx != null) {
        if (connected) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "JMSConnector.connect: already connected.");
          return;
        }
        logger.log(BasicLevel.ERROR, "JMSConnector.connect: already closed.");
        throw new IOException("Connector closed");
      }
      
      ConnectionFactory connectionFactory = TcpConnectionFactory.create(url.getHost(), url.getPort());
      String[] credentials = null;
      if (env != null)
        credentials = (String[]) env.get("jmx.remote.credentials");
            
      if ((credentials != null) && (credentials.length == 2)) {
        cnx = connectionFactory.createConnection(credentials[0], credentials[1]);
      } else {
        cnx = connectionFactory.createConnection();
      }
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JMSConnector.connect: Connection established.");

      cnx.start();
      connected = true;
      
      // Gets queue name from URL
      String qname = url.getURLPath();
      if ((qname != null) && (qname.length() != 0) && (qname.charAt(0) == '/'))
        qname = qname.substring(1);

      mbeanServerConnectionDelegate = new MBeanServerConnectionDelegate(cnx, qname);
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "JMSConnector.connect: Connection failed.", exc);
      if (cnx != null) {
        try {
          cnx.close();
        } catch (JMSException e) {}
      }
      cnx = null;
    } 
  }

  public MBeanServerConnection getMBeanServerConnection() throws IOException {
    return mbeanServerConnectionDelegate;
  }

  public MBeanServerConnection getMBeanServerConnection(Subject delegationSubject) throws IOException {
    return null;
  }

  public void close() throws IOException {
    try {
      cnx.close();
    } catch (JMSException exc) {
      logger.log(BasicLevel.ERROR, "JMSConnector.close:", exc);
    } finally {
      connected = false;
    }
  }

  public void addConnectionNotificationListener(NotificationListener listener,
                                                NotificationFilter filter,
                                                Object handback) {
  }

  public void removeConnectionNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
  }

  public void removeConnectionNotificationListener(NotificationListener l,
                                                   NotificationFilter f,
                                                   Object handback) throws ListenerNotFoundException {
  }

  public String getConnectionId() throws IOException {
    return null;
  }
}
