/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2006 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.connector;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.JoramAdminMBean;

public interface JoramAdapterMBean extends JoramAdminMBean {
  /**
   * Path to the directory containing JORAM's configuration files
   * (<code>a3servers.xml</code>, <code>a3debug.cfg</code>
   * and admin file), needed when starting the collocated JORAM server.
   */
  public java.lang.String getPlatformConfigDir();

  /** <code>true</code> if the JORAM server to start is persistent. */
  public java.lang.Boolean getPersistentPlatform();

  /** Identifier of the JORAM server to start. */
  public Short getServerId();

  /** Name of the JORAM server to start. */
  public java.lang.String getServerName();

  /**
   * Path to the file containing a description of the administered objects to
   * create and bind.
   */
  public java.lang.String getAdminFile();

  public java.lang.String getAdminFileXML();

  public java.lang.String getAdminFileExportXML();

  /**
   * Export the repository content to an XML file with default filename.
   * - only the destinations objects are retrieved in this version
   * - xml script format of the admin objects (joramAdmin.xml)
   * 
   * @param exportDir       target directory where the export file will be put
   * @throws AdminException if an error occurs
   */
  public void exportRepositoryToFile(String exportDir) throws AdminException;

  public java.lang.Boolean getCollocatedServer();

  public java.lang.String getHostName();

  public Integer getServerPort();

  /**
   * Duration in seconds during which connecting is attempted (connecting
   * might take time if the server is temporarily not reachable); the 0 value
   * is set for connecting only once and aborting if connecting failed.
   */
  public java.lang.Integer getConnectingTimer();

  /**
   * Duration in seconds during which a JMS transacted (non XA) session might
   * be pending; above that duration the session is rolled back and closed;
   * the 0 value means "no timer".
   */
  public java.lang.Integer getTxPendingTimer();

  /**
   * Period in milliseconds between two ping requests sent by the client
   * connection to the server; if the server does not receive any ping
   * request during more than 2 * cnxPendingTimer, the connection is
   * considered as dead and processed as required.
   */
  public java.lang.Integer getCnxPendingTimer();

  /**
  * @return the DeleteDurableSubscription that indicates whether the durablesubscription
  * must be deleted at InboundConsumer close time.
  */
  public java.lang.Boolean  getDeleteDurableSubscription();

  public void removeDestination(String name) throws AdminException;
}
