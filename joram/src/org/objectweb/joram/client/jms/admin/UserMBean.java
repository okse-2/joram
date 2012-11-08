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
 * Initial developer(s): Nicolas Tachker (ScalAgent DT)
 * Contributor(s): 
 */
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;
import java.util.List;
import java.util.Properties;

import javax.jms.JMSException;

public interface UserMBean {

  /** Returns a string view of this <code>User</code> instance. */
  public String toString();

  /** Returns the user name. */
  public String getName();

  /**
   * Removes this user.
   */
  public void delete()
    throws ConnectException, AdminException;

  /**
   * Admin method setting a given dead message queue Id for this user.
   */
  public void setDMQId(String dmqId)
    throws ConnectException, AdminException;

  /**
   * Admin method setting a given value as the threshold for this user.
   */
  public void setThreshold(int thresh)
    throws ConnectException, AdminException;

  public void setNbMaxMsg(String subName, int nbMaxMsg)
    throws ConnectException, AdminException;

  public int getNbMaxMsg(String subName) 
    throws ConnectException, AdminException;

  /** 
   * Returns the dead message queue Id for this user, null if not set.
   */
  public String getDMQId()
    throws ConnectException, AdminException;

  /** 
   * Returns the threshold for this user, -1 if not set.
   */
  public int getThreshold()
    throws ConnectException, AdminException;

  /**
   * Returns the subscriptions owned by a user.
   */
  public List getSubscriptionList() 
    throws ConnectException, AdminException;

  public Subscription[] getSubscriptions()
    throws AdminException, ConnectException;

  /**
   * Returns a subscription.
   */
  public String getSubscriptionString(String subName) 
    throws ConnectException, AdminException;

  public Subscription getSubscription(String subName) 
    throws AdminException, ConnectException;

  public String[] getMessageIds(String subName)
    throws ConnectException, AdminException;

  public String getMessageDigest(String subName,
                                 String msgId) throws AdminException, ConnectException, JMSException;

  public Properties getMessageHeader(String subName,
                                     String msgId) throws ConnectException, AdminException, JMSException;

  public Properties getMessageProperties(String subName,
                                         String msgId) throws ConnectException, AdminException, JMSException;

  public void deleteMessage(String subName, String msgId)
    throws AdminException, ConnectException;

  public void clearSubscription(String subName)
    throws AdminException, ConnectException;

   
  /** Returns the identifier of the user's proxy. */
  public String getProxyId();
}
