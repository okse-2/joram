/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Alexander Fedorowicz  
 */
package fr.dyade.aaa.joram.admin;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;


/**
 * The <code>MonitorItf</code> interface defines the set of methods provided
 * for monitoring a Joram platform.
 */
public interface MonitorItf
{
  /**
   * Opens a connection dedicated to monitoring with the Joram server
   * which parameters are wrapped by a given
   * <code>TopicConnectionFactory</code>.
   *
   * @param cnxFact  The TopicConnectionFactory to use for connecting.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void connect(javax.jms.TopicConnectionFactory cnxFact,
                      String name,
                      String password)
              throws ConnectException, AdminException;

  /**
   * Opens a connection dedicated to monitoring with the Joram server
   * running on a given host and listening to a given port.
   *
   * @param host  The name or IP address of the host the server is running on.
   * @param port  The number of the port the server is listening to.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server
   *          is attempted.
   *
   * @exception UnknownHostException  If the host is invalid.
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void connect(String hostName, 
                      int port,
                      String name,
                      String password,
                      int cnxTimer)
              throws UnknownHostException, ConnectException, AdminException;

  /**
   * Opens a connection dedicated to monitoring with the Joram server
   * running on the default "locahost" host and listening to the default
   * 16010 port.
   *
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server
   *          is attempted.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is
   *              incorrect.
   */
  public void connect(String name, String password, int cnxTimer)
              throws ConnectException, AdminException;

  /** Closes the monitoring connection. */
  public void disconnect();


  /**
   * Returns the list of the platform's servers' identifiers.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public List getServersIds() throws ConnectException, AdminException;

  /**
   * Returns the list of all <code>javax.jms.Destination</code> that
   * exist on a given server, or an empty list if none exist.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public List getDestinations(int serverId)
         throws ConnectException, AdminException;

  /**
   * Returns the list of all <code>fr.dyade.aaa.joram.admin.User</code> that
   * exist on a given server, or an empty list if none exist.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public List getUsers(int serverId) throws ConnectException, AdminException;

  /**
   * Returns the list of all <code>fr.dyade.aaa.joram.admin.User</code> that
   * have a reading permission on a given destination, or an empty list if no
   * specific readers are set.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public List getReaders(javax.jms.Destination dest)
         throws IllegalArgumentException, ConnectException, AdminException;

  /**
   * Returns the list of all <code>fr.dyade.aaa.joram.admin.User</code> that
   * have a writing permission on a given destination, or an empty list if no
   * specific writers are set.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public List getWriters(javax.jms.Destination dest)
         throws IllegalArgumentException, ConnectException, AdminException;

  /**
   * Returns <code>true</code> if a given destination provides free READ
   * access.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public boolean freelyReadable(javax.jms.Destination dest)
         throws IllegalArgumentException, ConnectException, AdminException;

  /**
   * Returns <code>true</code> if a given destination provides free WRITE
   * access.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public boolean freelyWriteable(javax.jms.Destination dest)
         throws IllegalArgumentException, ConnectException, AdminException;

  /** 
   * Returns the default dead message queue for a given server, null if not
   * set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public DeadMQueue getDefaultDMQ(int serverId)
         throws ConnectException, AdminException;

  /**
   * Returns the default threshold value for a given server, -1 if not set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getDefaultThreshold(int serverId)
         throws ConnectException, AdminException;

  /** 
   * Returns the dead message queue of a given destination, null if not
   * set.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public DeadMQueue getDMQ(javax.jms.Destination dest)
         throws IllegalArgumentException, ConnectException, AdminException;

  /** 
   * Returns the threshold of a given queue, -1 if not set.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getThreshold(javax.jms.Queue dest)
         throws IllegalArgumentException, ConnectException, AdminException;

  /** 
   * Returns the dead message queue of a given user, null if not
   * set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public DeadMQueue getDMQ(User user)
         throws ConnectException, AdminException;

  /** 
   * Returns the threshold of a given user, -1 if not set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getThreshold(User user)
         throws ConnectException, AdminException;

  /**
   * Returns the hierarchical father of a given topic, null if none.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public javax.jms.Topic getHierarchicalFather(javax.jms.Topic dest)
         throws IllegalArgumentException, ConnectException, AdminException;

  /**
   * Returns the list describing the cluster a given topic is part of.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public List getCluster(javax.jms.Topic dest)
         throws IllegalArgumentException, ConnectException, AdminException;

  /**
   * Returns the number of pending messages on a given queue.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getPendingMessages(javax.jms.Queue queue)
         throws IllegalArgumentException, ConnectException, AdminException;

  /**
   * Returns the number of pending requests on a given queue.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getPendingRequests(javax.jms.Queue queue)
         throws IllegalArgumentException, ConnectException, AdminException;

  /**
   * Returns the number of subscriptions on a given topic.
   *
   * @exception IllegalArgumentException  If the destination is not a valid 
   *              JORAM destination.
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getSubscriptions(javax.jms.Topic topic)
         throws IllegalArgumentException, ConnectException, AdminException;
}
