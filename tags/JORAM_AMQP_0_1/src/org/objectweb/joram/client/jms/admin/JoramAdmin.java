/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2008 ScalAgent Distributed Technologies
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
 * Contributor(s): Benoit Pelletier (Bull SA)
 */
package org.objectweb.joram.client.jms.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.jms.Destination;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.management.MXWrapper;

/**
 *
 */
public class JoramAdmin
  implements JoramAdminMBean {

  public long timeOut = 1000;
  public PlatformAdmin platformAdmin;
  /** <code>true</code> if the underlying a JORAM HA server is defined */
  static boolean isHa = false;


  /**
   * Path to the file containing a description of the exported administered objects (destination)
   */
  private String adminFileExportXML = null;


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
    this(cnxFact, name, password, Identity.SIMPLE_IDENTITY_CLASS);
  }

  public JoramAdmin(javax.jms.TopicConnectionFactory cnxFact,
                    String name,
                    String password,
                    String identityClassName)
    throws ConnectException, AdminException {
    platformAdmin = new PlatformAdmin(cnxFact,name,password,identityClassName);
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
   * Sets a given dead message queue as the default DMQ for a given server
   * (<code>null</code> for unsetting previous DMQ).
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @param serverId  The identifier of the server.
   * @param dmqId  The dmqId (AgentId) to be set as the default one.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDefaultDMQId(int serverId, String dmqId)
    throws ConnectException, AdminException {
    AdminModule.setDefaultDMQId(serverId,dmqId);
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
   * Returns the default dead message queue for a given server, null if not
   * set.
   * <p>
   * The request fails if the target server does not belong to the platform.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public String getDefaultDMQId(int serverId)
    throws ConnectException, AdminException {
    return AdminModule.getDefaultDMQId(serverId);
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
   * Returns the default dead message queue for the local server, null if not
   * set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public String getDefaultDMQId()
    throws ConnectException, AdminException {
    return AdminModule.getDefaultDMQId();
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
      User.create(name, password);
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
      User.create(name, password, serverId, Identity.SIMPLE_IDENTITY_CLASS);
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
  public void createUser(String name, String password, String identityClass) 
  throws AdminException {
    try {
      User.create(name, password, AdminModule.getLocalServerId(), identityClass);
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
  public void createUser(String name, String password, int serverId, String identityClass)
    throws AdminException {
    try {
      User.create(name, password, serverId, identityClass);
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

  public static boolean executeXMLAdmin(String cfgDir,
                                        String cfgFileName)
    throws Exception {
    return AdminModule.executeXMLAdmin(cfgDir, cfgFileName);
  }

  public static boolean executeXMLAdmin(String path)
    throws Exception {
    return AdminModule.executeXMLAdmin(path);
  }

  /**
   * Reload the joramAdmin.xml file
   * @param the path for the joramAdmin file
   * @throws AdminException if an error occurs
   */
  public boolean executeXMLAdminJMX(String path)
    throws Exception {
    throw new Exception("Not implemented yet");

  }


  /**
   * Export the repository content to an XML file
   * - only the destinations objects are retrieved in this version
   * - xml script format of the admin objects (joramAdmin.xml)
   * @param exportDir target directory where the export file will be put
   * @throws AdminException if an error occurs
   */
  public void exportRepositoryToFile(String exportDir) throws AdminException {

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG)) {
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "export repository to " + exportDir.toString());
    }

    StringBuffer strbuf = new StringBuffer();
    int indent = 0;
    strbuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    strbuf.append("<!--\n");
    strbuf.append(" Exported JMS objects : \n");
    strbuf.append(" - destinations : Topic/Queue \n");
    strbuf.append(" The file can be reloaded through the admin interface (joramAdmin.executeXMLAdmin())\n");
    strbuf.append("-->\n");
    strbuf.append("<JoramAdmin>\n");
    indent += 2;

    // Get the srv list
    List srvList = platformAdmin.getServersIds();
    if (srvList != null) {

      // For each server
      Iterator it = srvList.iterator();
      while (it.hasNext()) {
        try {
          Integer sid = (Integer) it.next();

          // Export the JMS destinations
          List destList = AdminModule.getDestinations(sid.intValue(), timeOut);
          Iterator destIt = destList.iterator();
          while (destIt.hasNext()) {
            org.objectweb.joram.client.jms.Destination dest = (org.objectweb.joram.client.jms.Destination) destIt
              .next();

            strbuf.append(dest.toXml(indent, sid.intValue()));
          }

        } catch (Exception exc) {
          throw new AdminException("exportRepositoryToFile() failed - " + exc);
        }
      }

      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG)) {
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "exported objects : \n" + strbuf.toString());
      }
    }

    indent -= 2;
    strbuf.append("</JoramAdmin>");

    // Flush the file in the specified directory
    File exportFile = null;
    FileOutputStream fos = null;

    try {
      exportFile = new File(exportDir, getAdminFileExportXML());
      fos = new FileOutputStream(exportFile);
      fos.write(strbuf.toString().getBytes());
    } catch(Exception ioe) {
      throw new AdminException("exportRepositoryToFile() failed - " + ioe);
    } finally {
      try {
        exportFile = null;
        fos.close();
      } catch (Exception e) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG)) {
          JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Unable to close the file  : " + fos);
        }
      }
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG)) {
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "File : " + exportDir + "/" + getAdminFileExportXML() + " created");
      }
    }
  }


  public String getAdminFileExportXML() {
    return adminFileExportXML;
  }

  public void setAdminFileExportXML(String adminFileExportXML) {
    this.adminFileExportXML = adminFileExportXML;
  }


  public static boolean isHa() {
    return isHa;
  }


  public static void setHa(boolean isHa) {
    JoramAdmin.isHa = isHa;
    AdminModule.setHa(isHa);
  }
}
