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

import java.net.ConnectException;
import java.util.List;
import java.util.Properties;

import javax.jms.Destination;

/**
 *
 */
public interface JoramAdminMBean {

  public void exit();

  public void setTimeOutToAbortRequest(long timeOut);

  public long getTimeOutToAbortRequest();
  
  public String getDefaultDMQId(int serverId)
  throws ConnectException, AdminException;
  
  public void setDefaultDMQId(int serverId, String dmqId)
  throws ConnectException, AdminException;

  public String getDefaultDMQId()
    throws ConnectException, AdminException;

  public List getDestinations(int serverId);

  public List getDestinations();

  public List getUsers(int serverId);

  public List getUsers();

  public void createUser(String name, String password)
    throws AdminException;

  public void createUser(String name, String password, int serverId)
    throws AdminException;

  public Destination createQueue(String name)
    throws AdminException;

  public Destination createQueue(int serverId, String name)
    throws AdminException;

  public Destination createQueue(int serverId,
                                 String name,
                                 String className,
                                 Properties prop)
    throws AdminException;

  public Destination createTopic(String name)
    throws AdminException;

  public Destination createTopic(int serverId, String name)
    throws AdminException;

  public Destination createTopic(int serverId,
                                 String name,
                                 String className,
                                 Properties prop)
    throws AdminException;


  /**
   * Export the repository content to an XML file
   * - only the destinations objects are retrieved in this version
   * - xml script format of the admin objects (joramAdmin.xml)
   * @param exportDir target directory where the export file will be put
   * @throws AdminException if an error occurs
   */
  public void exportRepositoryToFile(String exportDir)
    throws AdminException;

  public boolean executeXMLAdminJMX(String path)
    throws Exception;
}
