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
 * Initial developer(s): Djamel-Eddine Boumchedda
 * 
 */

package jmx.remote.jms;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.jms.JMSException;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * In the Class <b>Jms JmxConnectorServer</b>, the methodes of the server
 * connector are implemented like : start, stop ...
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 * 
 */
public class JmsJmxConnectorServer extends JMXConnectorServer {
  private static final Logger logger = Debug.getLogger(JmsJmxConnectorServer.class.getName());
  private JMXServiceURL urlServer;
  private final Map envServer;
  private URI jmsURL;
  private boolean stopped = true;

  public JmsJmxConnectorServer(JMXServiceURL url, Map environment, MBeanServer server) throws IOException {
    this.urlServer = url;
    this.envServer = environment;

  }

  public void start() throws IOException {
    // TODO Auto-generated method stub
    stopped = false;
    try {
      JMSConnector jmsConnector = new JMSConnector();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Instantiation of the class JmsConnector in the start method of the server connector.");
      }
      
    } catch (NamingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (AdminException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public void stop() throws IOException {
    // TODO Auto-generated method stub
    if (!stopped) {
      stopped = true;
      // .....
    }

  }

  public boolean isActive() {
    // TODO Auto-generated method stub
    return !stopped;
  }

  public JMXServiceURL getAddress() {
    // TODO Auto-generated method stub
    return urlServer;
  }

  public Map<String, ?> getAttributes() {
    // TODO Auto-generated method stub
    return null;
  }

}
