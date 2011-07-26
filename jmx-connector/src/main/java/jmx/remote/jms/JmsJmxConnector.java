/**
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
 * Initial developer(s): Djamel-Eddine Boumchedda
 * 
 */

package jmx.remote.jms;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;

import org.objectweb.joram.client.jms.MessageProducer;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.TemporaryQueue;
import org.objectweb.joram.client.jms.admin.AdminException;

import com.sun.java.browser.net.ProxyService;

/**
 * In the Class <b>JmsJmxConnector</b>, the methodes of the client connector are
 * implemented like : connect,getMBeanServerConnection() ...
 * 
 * The <i>MBeanServerConnection</i> is provided by the
 * <i>MBeanServerConnectionDelegate</i> Class.
 * 
 * @author Djamel-Eddine Boumchedda
 * 
 */
public class JmsJmxConnector implements JMXConnector {
  private JMXServiceURL jmsURL;
  private Map env;
  private boolean connected = false;
  MBeanServerConnectionDelegate mbeanServerConnectionDelegate;

  public JmsJmxConnector(Map env, JMXServiceURL url) throws IOException {
    this.env = env;
    this.jmsURL = url;
    String path = new File("").getAbsolutePath();
    /*
     * File f = new File(path+"\\trace-Client"); PrintStream pS = new
     * PrintStream(f); Exception e = new Exception(); e.printStackTrace(pS);
     */
    // set any props in the url
    // JmsJmxConnectorSupport.populateProperties(this,jmsURL);

  }

  public void connect() throws IOException {
    // TODO Auto-generated method stub
    connect(this.env);

  }

  public void connect(Map<String, ?> env) throws IOException {
    // TODO Auto-generated method stub
    try {
        
      //We created the connection from the connection factory registered
      //In the jndi
      
      // Récupération du contexte JNDI
      Context jndiContext = new InitialContext();
      // Recherche des objets administrés
      ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
      jndiContext.close();
      // Création des artéfacts nécessaires pour se connecter à la file et au
      // sujet
      Connection connection = connectionFactory.createConnection();
      System.out.println("Connection : " + connection.toString());
      connection.start();

      mbeanServerConnectionDelegate = new MBeanServerConnectionDelegate(connection);


    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
  }

 

  public MBeanServerConnection getMBeanServerConnection() throws IOException {
    // TODO Auto-generated method stub
    return mbeanServerConnectionDelegate;
  }

  public MBeanServerConnection getMBeanServerConnection(Subject delegationSubject) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  public void close() throws IOException {
    // TODO Auto-generated method stub

  }

  public void addConnectionNotificationListener(NotificationListener listener, NotificationFilter filter,
      Object handback) {
    // TODO Auto-generated method stub

  }

  public void removeConnectionNotificationListener(NotificationListener listener)
      throws ListenerNotFoundException {
    // TODO Auto-generated method stub

  }

  public void removeConnectionNotificationListener(NotificationListener l, NotificationFilter f,
      Object handback) throws ListenerNotFoundException {
    // TODO Auto-generated method stub

  }

  public String getConnectionId() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

}
