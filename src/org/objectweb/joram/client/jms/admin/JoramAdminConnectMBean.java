/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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

/**
 * Allows the creation of JoramAdmin instances connected to servers.
 */
public interface JoramAdminConnectMBean {
  /**
   * Creates an administration connection with default parameters, a JoramAdmin
   * MBean is created and registered in the given domain.
   * 
   * @param name The name of the corresponding JMX domain.
   * 
   * @exception AdminException   If the creation fails.
   * @exception ConnectException if the connection is closed or broken
   */
  public void connect(String name) throws ConnectException, AdminException;
  
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
                      String user, String pass) throws ConnectException, AdminException;
  
  /**
   * Unregisters the MBean.
   * 
   * @param force If true calls System.exit method.
   */
  public void exit(boolean force);
}
