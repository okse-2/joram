/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest;

import java.util.List;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

public interface QueueMBean extends DestinationMBean {
  
  int getAckRequestNumber();
  
  /**
   * Returns  the threshold value of this queue, -1 if not set.
   *
   * @return the threshold value of this queue; -1 if not set.
   */
  int getThreshold();

  /**
   * Sets or unsets the threshold for this queue.
   *
   * @param threshold The threshold value to be set or -1 for unsetting
   *                  previous value.
   */
  void setThreshold(int threshold);

  /**
   * Returns the number of waiting requests in the queue.
   *
   * @return The number of waiting requests.
   */
  int getWaitingRequestCount();

   /**
    * Removes all request that the expiration time is expired.
    */
   void cleanWaitingRequest();

  /**
   * Returns the number of pending messages in the queue.
   *
   * @return The number of pending messages.
   */
  int getPendingMessageCount();
  
//  /**
//   * Returns the load averages for the last minute.
//   * @return the load averages for the last minute.
//   */
//  float getAverageLoad1();
//
//  /**
//   * Returns the load averages for the past 5 minutes.
//   * @return the load averages for the past 5 minutes.
//   */
//  float getAverageLoad5();
//  
//  /**
//   * Returns the load averages for the past 15 minutes.
//   * @return the load averages for the past 15 minutes.
//   */
//  float getAverageLoad15();
    
   /**
    * Removes all messages that the time-to-live is expired.
    */
   void cleanPendingMessage();

  /**
   * Returns the number of messages delivered and waiting for acknowledge.
   *
   * @return The number of messages delivered.
   */
  int getDeliveredMessageCount();

  /**
   * Returns the maximum number of message for the destination.
   * If the limit is unset the method returns -1.
   *
   * @return the maximum number of message for subscription if set;
   *	     -1 otherwise.
   */
  int getNbMaxMsg();

  /**
   * Sets the maximum number of message for the destination.
   *
   * @param nbMaxMsg the maximum number of message (-1 set no limit).
   */
  void setNbMaxMsg(int nbMaxMsg);

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
