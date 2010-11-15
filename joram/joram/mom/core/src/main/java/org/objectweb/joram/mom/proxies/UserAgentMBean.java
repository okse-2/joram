/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.proxies;


public interface UserAgentMBean {
  /**
   * Returns the name of this queue, or its id if not set.
   *
   * @return the name of this queue; its id if not set.
   */
  String getName();
  
  /**
   * Returns the period value of this queue, -1 if not set.
   *
   * @return the period value of this queue; -1 if not set.
   */
  long getPeriod();

  /**
   * Sets or unsets the period for this queue.
   *
   * @param period The period value to be set or -1 for unsetting previous
   *               value.
   */
  void setPeriod(long period);

  /**
   * Returns the list of subscriptions for this user. Each subscription is
   * identified by its unique 'symbolic' name.
   *
   * @return The list of subscriptions for this user.
   */
  String[] getSubscriptionNames();

  /**
   * Returns a string representation of this user's proxy.
   */
  String toString();
  
  /**
   * Returns the number of erroneous messages forwarded to the DMQ since
   * creation time of this proxy..
   * 
   * @return the number of erroneous messages forwarded to the DMQ.
   */
  long getNbMsgsSentToDMQSinceCreation();
  
  /**
   * Returns the default DMQ for subscription of this user.
   * @return  the default DMQ for subscription of this user.
   */
  String getDMQId();

  /**
   * Returns the default threshold for the subscription of this user.
   * 0 stands for no threshold, -1 for value not set.
   *
   * @return the maximum number of message if set; -1 otherwise.
   */
  int getThreshold();

  /**
   * Sets the default threshold for the subscription of this user.
   * 0 stands for no threshold, -1 for value not set.
   *  
   * @param threshold the threshold to set.
   */
  void setThreshold(int threshold);

  /**
   * Returns the default maximum number of message for the subscription of this user.
   * If the limit is unset the method returns -1.
   *
   * @return the maximum number of message if set; -1 otherwise.
   */
  int getNbMaxMsg();

  /**
   * Sets the maximum number of message for the subscription of this user.
   *
   * @param nbMaxMsg the maximum number of message (-1 set no limit).
   */
  void setNbMaxMsg(int nbMaxMsg);

  /**
   * Deletes this proxy.
   */
  void delete();
}
