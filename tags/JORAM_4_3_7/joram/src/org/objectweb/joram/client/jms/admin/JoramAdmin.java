/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.Iterator;

import javax.jms.Destination;

import fr.dyade.aaa.util.management.MXWrapper;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 *
 */
public class JoramAdmin
  implements JoramAdminMBean {

  public long timeOut = 1000;
  public PlatformAdmin platformAdmin;

  public JoramAdmin() 
    throws UnknownHostException, ConnectException, AdminException {
    platformAdmin = new PlatformAdmin();
    registerMBean();
  }
  
  public JoramAdmin(String hostName,
                    int port,
                    String name,
                    String password,
                    int cnxTimer,
                    String reliableClass)
    throws UnknownHostException, ConnectException, AdminException {
    platformAdmin = new PlatformAdmin(hostName,port,name,password,cnxTimer,reliableClass);
    registerMBean();
  }

  public JoramAdmin(String hostName,
                    int port,
                    String name,
                    String password,
                    int cnxTimer)
    throws UnknownHostException, ConnectException, AdminException {
    platformAdmin = new PlatformAdmin(hostName,port,name,password,cnxTimer);
    registerMBean();
  }

  public JoramAdmin(String name,
                    String password) 
    throws ConnectException, AdminException {
    platformAdmin = new PlatformAdmin(name,password);
    registerMBean();
  }

  public JoramAdmin(javax.jms.TopicConnectionFactory cnxFact, 
                    String name,
                    String password)
    throws ConnectException, AdminException {
    platformAdmin = new PlatformAdmin(cnxFact,name,password);
    registerMBean();
  }

  private void registerMBean() {
    try {
      MXWrapper.registerMBean(this,
                              "joramClient",
                              "type=JoramAdmin");
    } catch (Exception e) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                   "registerMBean",e);
    }
  }

  private void unregisterMBean() {
    try {
      MXWrapper.unregisterMBean("joramClient",
                                "type=JoramAdmin");
    } catch (Exception e) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                   "unregisterMBean",e);
    }
  }

  public PlatformAdmin getPlatformAdmin() {
    return platformAdmin;
  }

  public void exit() {
    platformAdmin.exit();
    unregisterMBean();
  }

  /**
   * wait before abort a request.
   */
  public void setTimeOutToAbortRequest(long timeOut) {
    this.timeOut = timeOut;
  }

  /**
   * wait before abort a request.
   */
  public long getTimeOutToAbortRequest() {
    return timeOut;
  }

  /**
   * Sets a given dead message queue as the default DMQ for a given server
   * (<code>null</code> for unsetting previous DMQ).
   * <p>
   * The request fails if the target server does not belong to the platform.
   * 
   * @param serverId  The identifier of the server.
   * @param dmq  The dmq to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDefaultDMQ(int serverId, DeadMQueue dmq)
    throws ConnectException, AdminException {
    AdminModule.setDefaultDMQ(serverId,dmq);
  }

  /** 
   * Returns the default dead message queue for a given server, null if not
   * set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public DeadMQueue getDefaultDMQ(int serverId)
    throws ConnectException, AdminException {
    return AdminModule.getDefaultDMQ(serverId);
  }

  /** 
   * Returns the default dead message queue for the local server, null if not
   * set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public DeadMQueue getDefaultDMQ()
    throws ConnectException, AdminException {
    return AdminModule.getDefaultDMQ();
  }

  /**
   * Returns the list of all destinations that exist on a given server,
   * or an empty list if none exist.
   */
  public List getDestinations(int serverId) {
    Vector destinations = new Vector();
    try {
      List destList = AdminModule.getDestinations(serverId,timeOut);
      Iterator destIt = destList.iterator();
      while (destIt.hasNext()) {
        org.objectweb.joram.client.jms.Destination dest = 
          (org.objectweb.joram.client.jms.Destination) destIt.next();
        destinations.add(new String("type=" + dest.getType() +
                                    ", name=" + dest.getAdminName() +
                                    ", id=" + dest.getName()));
      }
    } catch (Exception exc) {}
    return destinations;
  }

  /**
   * Returns the list of all destinations that exist on the local server,
   * or an empty list if none exist.
   */
  public List getDestinations() {
    Vector destinations = new Vector();
    
    List list = platformAdmin.getServersIds();
    if (list != null) {
      Iterator it = list.iterator();
      while (it.hasNext()) {
        try {
          Integer sid = (Integer) it.next();
          List destList = AdminModule.getDestinations(sid.intValue(),timeOut);
          Iterator destIt = destList.iterator();
          while (destIt.hasNext()) {
            org.objectweb.joram.client.jms.Destination dest = 
              (org.objectweb.joram.client.jms.Destination) destIt.next();
            destinations.add(new String("type=" + dest.getType() +
                                        ", name=" + dest.getAdminName() +
                                        ", id=" + dest.getName()));
          }
        } catch (Exception exc) {}
      }
    }
    return destinations;
  }

  /**
   * Returns the list of all users that exist on a given server, or an empty
   * list if none exist.
   */
  public List getUsers(int serverId) {
    Vector users = new Vector();
    try {
      List userList = AdminModule.getUsers(serverId,timeOut);
      Iterator userIt = userList.iterator();
      while (userIt.hasNext()) {
        User dest = (User) userIt.next();
        users.add(dest.toString());
      }
    } catch (Exception exc) {}
    return users;
  }

  /**
   * Returns the list of all users that exist on the local server, or an empty
   * list if none exist.
   */
  public List getUsers() {
    Vector users = new Vector();

    List list = platformAdmin.getServersIds();
    if (list != null) {
      Iterator it = list.iterator();
      while (it.hasNext()) {
        try {
          Integer sid = (Integer) it.next();
          List userList = AdminModule.getUsers(sid.intValue(),timeOut);
          Iterator userIt = userList.iterator();
          while (userIt.hasNext()) {
            User dest = (User) userIt.next();
            users.add(dest.toString());
          }
        } catch (Exception exc) {}
      }
    }
    return users;
  }

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @exception AdminException   If the creation fails.
   */
  public void createUser(String name, String password)
    throws AdminException {
    try {
      User.create(name,password);
    } catch (ConnectException exc) {
      throw new AdminException("createUser() failed: admin connection "
                               + "has been lost.");
    }
  }

  /**
   * Creates or retrieves a user on the underlying JORAM server.
   *
   * @exception AdminException   If the creation fails.
   */
  public void createUser(String name, String password, int serverId) 
    throws AdminException {
    try {
      User.create(name,password,serverId);
    } catch (ConnectException exc) {
      throw new AdminException("createUser() failed: admin connection "
                               + "has been lost.");
    }
  }

  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param name       The name of the queue.
   *
   * @exception AdminException   If the creation fails.
   */
  public Destination createQueue(String name)
    throws AdminException {
    try {
      return createQueue(platformAdmin.getLocalServerId(),
                         name,
                         "org.objectweb.joram.mom.dest.Queue",
                         null);
    } catch (ConnectException exc) {
      throw new AdminException("createQueue() failed: admin connection "
                               + "has been lost.");
    }
  }

  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   *
   * @exception AdminException   If the creation fails.
   */
  public Destination createQueue(int serverId, String name)
    throws AdminException {
    return createQueue(serverId, 
                       name, 
                       "org.objectweb.joram.mom.dest.Queue", 
                       null);
  }
  
  /**
   * Creates or retrieves a queue destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Queue</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   * @param className  The queue class name.
   * @param prop       The queue properties.
   *
   * @exception AdminException   If the creation fails.
   */
  public Destination createQueue(int serverId,
                                 String name,
                                 String className,
                                 Properties prop)
    throws AdminException {
    try {
      Queue queue = Queue.create(serverId,
                                 name,
                                 className,
                                 prop); 
      return queue;
    } catch (ConnectException exc) {
      throw new AdminException("createQueue() failed: admin connection "
                               + "has been lost.");
    }
  }


  /**
   * Creates or retrieves a topic destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Topic</code> instance.
   *
   * @exception AdminException   If the creation fails.
   */
  public Destination createTopic(String name)
    throws AdminException {
    try {
      return createTopic(platformAdmin.getLocalServerId(),
                         name,
                         "org.objectweb.joram.mom.dest.Topic",
                         null);
    } catch (ConnectException exc) {
      throw new AdminException("createTopic() failed: admin connection "
                               + "has been lost.");
    }
  }
  
  /**
   * Creates or retrieves a topic destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Topic</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the topic.
   *
   * @exception AdminException   If the creation fails.
   */
  public Destination createTopic(int serverId, String name)
    throws AdminException {
    return createTopic(serverId, 
                       name, 
                       "org.objectweb.joram.mom.dest.Topic", 
                       null);
  }
  
  /**
   * Creates or retrieves a topic destination on the underlying JORAM server,
   * (re)binds the corresponding <code>Topic</code> instance.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the topic.
   * @param className  The topic class name.
   * @param prop       The topic properties.
   *
   * @exception AdminException   If the creation fails.
   */
  public Destination createTopic(int serverId,
                                 String name,
                                 String className,
                                 Properties prop)
    throws AdminException {
    try {
      Topic topic = Topic.create(serverId,
                                 name,
                                 className,
                                 prop);
      return topic;
    } catch (ConnectException exc) {
      throw new AdminException("createTopic() failed: admin connection "
                               + "has been lost.");
    }
  }
}
