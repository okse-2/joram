/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2011 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * Allows the creation of JoramAdmin instances connected to servers.
 */
public class JoramAdminConnect implements JoramAdminConnectMBean {

  public static Logger logger = Debug.getLogger(JoramAdminConnect.class.getName());
  
  public JoramAdminConnect() {
  }
  
  public static void main(String args[]) {
    try {

      JoramAdminConnect admin = new JoramAdminConnect();
      admin.registerMBean();
      
      synchronized (admin) {
        admin.wait();
      }
      
      admin.unregisterMBean();
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }

  public void registerMBean() {
    try {
      MXWrapper.registerMBean(this, "JoramAdmin", "type=AdminConnect");
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramAdmin.registerMBean", e);
    }
  }

  public void unregisterMBean() {
    try {
      MXWrapper.unregisterMBean("JoramAdmin", "type=AdminConnect");
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramAdmin.unregisterMBean",e);
    }
  }

  /**
   * Creates an administration connection with default parameters, a JoramAdmin
   * MBean is created and registered in the given domain.
   * 
   * @param name The name of the corresponding JMX domain.
   * 
   * @exception AdminException   If the creation fails.
   * @exception ConnectException if the connection is closed or broken
   */
  public void connect(String name) throws ConnectException, AdminException {
    connect(name, "localhost", 16010, "root", "root");
  }

  /**
   * Creates an administration connection with given parameters, a JoramAdmin
   * MBean is created and registered.
   * 
   * @param name The name of the corresponding JMX domain.
   * @param host The hostname of the server.
   * @param port The listening port of the server.
   * @param user The login identification of the administrator.
   * @param pass The password of the administrator.
   *
   * @exception AdminException   If the creation fails.
   * @exception ConnectException if the connection is closed or broken
   */
  public void connect(String name,
                      String host, int port,
                      String user, String pass) throws ConnectException, AdminException {
    ConnectionFactory cf = TcpConnectionFactory.create(host, port);
    Connection cnx;
    try {
      cnx = cf.createConnection(user, pass);
      cnx.start();
      new JoramAdmin(cnx, name);
    } catch (JMSException exc) {
      // TODO Auto-generated catch block
      exc.printStackTrace();
    }
  }

  /**
   * Unregisters the MBean.
   * 
   * @param force If true calls System.exit method.
   */
  public synchronized void exit(boolean force) {
    if (force)
      System.exit(0);
    else
      notify();
  }
}
