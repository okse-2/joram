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
 * Contributor(s): 
 */
package org.objectweb.joram.client.jms;

import org.objectweb.joram.client.jms.admin.AdminException;

import java.net.ConnectException;
import java.util.List;
import java.util.Hashtable;
import javax.jms.JMSException;

public interface DestinationMBean {
  public String getName();
  public String getAdminName();
  public String getType();

  /**
   * Returns <code>true</code> if the destination is a queue.
   */
  public boolean isQueue();

  /**
   * Administration method removing this destination from the platform.
   */
  public void delete() throws ConnectException, AdminException, JMSException;

  /**
   * Administration method setting a given user as a reader on this destination.
   * 
   * @param proxyId The unique identification of the user's proxy.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void addReader(String proxyId) throws ConnectException, AdminException;

  /**
   * Administration method setting a given user as a writer on this destination.
   * 
   * @param proxyId The unique identification of the user's proxy.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void addWriter(String proxyId) throws ConnectException, AdminException;

  /**
   * Administration method unsetting a given user as a reader on this destination.
   * 
   * @param proxyId The unique identification of the user's proxy.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void removeReader(String proxyId) throws ConnectException, AdminException;

  /**
   * Administration method unsetting a given user as a writer on this destination.
   * 
   * @param proxyId The unique identification of the user's proxy.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void removeWriter(String proxyId) throws ConnectException, AdminException;

  /**
   * Monitoring method returning the list of all users that have a reading
   * permission on this destination, or an empty list if no specific readers
   * are set.
   */
  public List getReaderList() throws ConnectException, AdminException;

  /**
   * Monitoring method returning the list of all users that have a writing
   * permission on this destination, or an empty list if no specific writers
   * are set.
   */
  public List getWriterList() throws ConnectException, AdminException;

  /**
   * Monitoring method returning <code>true</code> if this destination
   * provides free READ access.
   */
  public boolean isFreelyReadable() throws ConnectException, AdminException;

  /**
   * Monitoring method returning <code>true</code> if this destination
   * provides free WRITE access.
   */
  public boolean isFreelyWriteable() throws ConnectException, AdminException;

  /**
   * Administration method (un)setting free reading access to this destination.
   */
  public void setFreelyReadable(boolean b) throws ConnectException, AdminException;

  /**
   * Administration method (un)setting free writing access to this destination.
   */
  public void setFreelyWriteable(boolean b) throws ConnectException, AdminException;

  /** 
   * Monitoring method returning the dead message queue id of this destination,
   * null if not set.
   */
  public String getDMQId() throws ConnectException, AdminException;
  
  /**
   * Admininistration method setting or unsetting a dead message queue for this
   * destination.
   */
  public void setDMQId(String dmqId) throws ConnectException, AdminException;

  /**
   * Return a set of statistic values from the destination.
   * Be careful this method is deprecated and should be removed in future version,
   * use getStatistics method in replacement.
   * @deprecated
   */
  public Hashtable getStatistic() throws ConnectException, AdminException;

  /**
   * Return a set of statistic values from the destination.
   */
  public Hashtable getStatistics() throws ConnectException, AdminException;
}
