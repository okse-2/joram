/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2009 ScalAgent Distributed Technologies
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

import java.util.List;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

public interface ClientSubscriptionMBean {
  /**
   * Deletes a particular pending message in the subscription. The message is
   * pointed out through its unique identifier.
   * 
   * @param msgId The unique message's identifier.
   */
  void deleteMessage(String msgId);

  /**
   * Sets the maximum number of message for the subscription.
   * 
   * @param nbMaxMsg
   *            the maximum number of message for subscription (-1 set no
   *            limit).
   */
  void setNbMaxMsg(int nbMaxMsg);

  /**
   * Deletes all messages
   */
  public void clear();

  /**
   * Returns the subscription's context identifier.
   */
  public int getContextId();

  /**
   * Returns the identifier of the subscribing request.
   */
  public int getSubRequestId();

  /**
   * Returns the name of the subscription.
   */
  public String getName();

  /**
   * Returns the identifier of the subscription topic.
   */
  public String getTopicIdAsString();

  /**
   * Returns the selector.
   */
  public String getSelector();

  /**
   * Returns <code>true</code> if the subscription is durable.
   */
  public boolean getDurable();

  /**
   * Returns <code>true</code> if the subscription is active.
   */
  public boolean getActive();
  
  /**
   * Returns the threshold above which messages are considered undeliverable
   * because constantly denied.
   * 
   * @return  the threshold if set; -1 otherwise.
   */
  public int getThreshold();

  /**
   * Returns the maximum number of message for the subscription. If the limit is
   * unset the method returns -1.
   * 
   * @return the maximum number of message for subscription if set; -1
   *         otherwise.
   */
  public int getNbMaxMsg();

  /**
   * Returns the number of pending messages for the subscription.
   * 
   * @return The number of pending message for the subscription.
   */
  public int getPendingMessageCount();

  /**
   * Returns the list of message's identifiers for the subscription.
   * 
   * @return the list of message's identifiers for the subscription.
   */
  public String[] getMessageIds();
  
  /**
   * Returns the number of erroneous messages forwarded to the DMQ since
   * creation time of this subscription.
   * 
   * @return the number of erroneous messages forwarded to the DMQ.
   */
  public long getNbMsgsSentToDMQSinceCreation();

  /**
   * Returns the number of messages delivered to the client since creation time
   * of this subscription.
   * 
   * @return the number of delivered messages.
   */
  public long getNbMsgsDeliveredSinceCreation();

  /**
   * Returns the description of a particular pending message.
   * The message is pointed out through its unique identifier.
   * 
   * @param msgId The unique message's identifier.
   * @return the description of the message.
   * 
   * @see org.objectweb.joram.mom.messages.MessageJMXWrapper
   */
  public CompositeData getMessage(String msgId) throws Exception;

  /**
   * Returns the description of all pending messages.
   * 
   * @return the description of the message.
   * 
   * @see org.objectweb.joram.mom.messages.MessageJMXWrapper
   */
  public TabularData getMessages() throws Exception;

  /**
   * Returns the description of all pending messages.
   * 
   * @return the description of the message.
   */
  public List getMessagesView();
//  public CompositeData[] getMessages() throws Exception;
}
