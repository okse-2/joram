/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2005 ScalAgent Distributed Technologies
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

import javax.management.openmbean.*;

public interface ProxyImplMBean {
  /**
   * Returns  the period value of this queue, -1 if not set.
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
   * Returns the number of pending messages for an identified subscription.
   * The subscription must be identified by its unique 'symbolic' name.
   *
   * @param subName  The subscription unique name.
   * @return The number of pending message for the subscription.
   */
  int getSubscriptionMessageCount(String subName);

  /**
   * Returns the unique identifier of the topic related to this subscription.
   *
   * @param subName  The subscription unique name.
   * @return the unique identifier of the topic related to this subscription.
   */
  String getSubscriptionTopicId(String subName);

  /**
   * Returns the list of message's identifiers for a subscription.
   * The subscription must be identified by its unique 'symbolic' name.
   *
   * @param subName  The subscription unique name.
   * @return the list of message's identifiers for the subscription.
   */
  String[] getSubscriptionMessageIds(String subName);

  /**
   * Returns the description of a particular pending message in a subscription.
   * The subscription is identified  by its unique name, the message is pointed
   * out through its unique identifier.
   * The description includes the type and priority of the message.
   *
   * @param subName  The subscription unique name.
   * @param msgId    The unique message's identifier.
   * @return the description of the message.
   */
  CompositeDataSupport getSubscriptionMessage(String subName,
                                              String msgId) throws Exception;

  /**
   * Deletes a particular pending message in a subscription.
   * The subscription is identified  by its unique name, the message is pointed
   * out through its unique identifier.
   *
   * @param subName  The subscription unique name.
   * @param msgId    The unique message's identifier.
   */
  void deleteSubscriptionMessage(String subName, String msgId);

  /**
   * Returns the maximum number of message for identified subscription.
   * The subscription is identified  by its unique name, if the limit is unset
   * the method returns -1.
   *
   * @param subName  The subscription unique name.
   * @return the maximum number of message for subscription if set;
   *	     -1 otherwise.
   */
  int getNbMaxMsg(String subName);

  /**
   * Sets the maximum number of message for identified subscription.
   * The subscription is identified  by its unique name.
   *
   * @param subName  The subscription unique name.
   * @param nbMaxMsg the maximum number of message for subscription (-1 set
   *		     no limit).
   */
  void setNbMaxMsg(String subName, int nbMaxMsg);

  /**
   * Returns a string representation of this user's proxy.
   */
  String toString();
}
