/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package org.ow2.joram.admin;

import org.objectweb.joram.mom.dest.QueueImplMBean;
import org.objectweb.joram.mom.dest.TopicImplMBean;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.ProxyImplMBean;

public interface JoramAdmin {

  public boolean connect(String login, String password);

  public void start(AdminListener listener);

  public void stop();

  public void disconnect();

  /**
   * Create a message on a queue in JORAM
   * 
   * @param queueName
   *          Name of the queue containing the message
   * @param id
   *          Id of the message
   * @param expiration
   *          Expiration date of the message
   * @param timestamp
   *          Timestamp of the message
   * @param priority
   *          Priority of the message
   * @param text
   *          Text of the message
   * @param type
   *          Type of the message
   * @return Create successful
   */
  public boolean createNewMessage(String queueName, String id, long expiration, long timestamp, int priority,
      String text, int type);

  /**
   * Edit a message on a queue in JORAM
   * 
   * @param queueName
   *          Name of the queue containing the message
   * @param id
   *          Id of the message
   * @param expiration
   *          Expiration date of the message
   * @param timestamp
   *          Timestamp of the message
   * @param priority
   *          Priority of the message
   * @param text
   *          Text of the message
   * @param type
   *          Type of the message
   * @return Edit successful
   */
  public boolean editMessage(String queueName, String id, long expiration, long timestamp, int priority,
      String text, int type);

  /**
   * Delete a message in a given subscription.
   * 
   * @param sub
   *          The subscription where the message will be deleted.
   * @param msgId
   *          ID of the message to delete
   * @return suppression Delete Successful
   */
  public boolean deleteSubscriptionMessage(ClientSubscriptionMBean sub, String msgId);

  /**
   * Create a topic on JORAM
   * 
   * @param name
   *          Name of the topic
   * @param DMQ
   *          DMQ Id of the topic
   * @param destination
   *          Destination of the topic
   * @param period
   *          Period of the topic
   * @param freeReading
   *          FreeReading of the topic
   * @param freeWriting
   *          FreeWriting of the topic
   * @return Create successful
   */
  public boolean createNewTopic(String name, String DMQ, String destination, long period,
      boolean freeReading, boolean freeWriting);

  /**
   * Edit a topic on JORAM
   * 
   * @param name
   *          Name of the topic
   * @param DMQ
   *          DMQ Id of the topic
   * @param destination
   *          Destination of the topic
   * @param period
   *          Period of the topic
   * @param freeReading
   *          FreeReading of the topic
   * @param freeWriting
   *          FreeWriting of the topic
   * @return Create successful
   */
  public boolean editTopic(TopicImplMBean topic, String DMQ, String destination, long period,
      boolean freeReading, boolean freeWriting);

  /**
   * Delete a topic in JORAM
   * 
   * @param topic
   *          The topic to delete
   * @return Delete successful
   */
  public boolean deleteTopic(TopicImplMBean topic);

  /**
   * Create a user on JORAM
   * 
   * @param name
   *          Name of the user
   * @param password
   *          User's password
   * @param period
   *          Period of the user
   * @return Create successful
   */
  public boolean createNewUser(String name, String password, long period);

  /**
   * Edit a user on JORAM
   * 
   * @param name
   *          Name of the user
   * @param period
   *          Period of the user
   * @return Edit successful
   */
  public boolean editUser(ProxyImplMBean user, String password, long period);

  /**
   * Delete a user on JORAM
   * 
   * @param user
   *          User to delete
   * @return Delete successful
   */
  public boolean deleteUser(ProxyImplMBean user);

  /**
   * Create a queue on JORAM
   * 
   * @param name
   *          Name of the queue
   * @param DMQ
   *          DMQ Id of the queue
   * @param destination
   *          Destination Id of the queue
   * @param period
   *          Period of the queue
   * @param threshold
   *          Threshold of the queue
   * @param nbMaxMsg
   *          Maximum messages of the queue
   * @param freeReading
   *          Is the queue FreeReading
   * @param freeWriting
   *          Is the queue FreeWriting
   * @return Create successful
   */
  public boolean createNewQueue(String name, String DMQ, String destination, long period, int threshold,
      int nbMaxMsg, boolean freeReading, boolean freeWriting);

  /**
   * Edit a queue on JORAM
   * 
   * @param name
   *          Name of the queue
   * @param DMQ
   *          DMQ Id of the queue
   * @param destination
   *          Destination Id of the queue
   * @param period
   *          Period of the queue
   * @param threshold
   *          Threshold of the queue
   * @param nbMaxMsg
   *          Maximum messages of the queue
   * @param freeReading
   *          Is the queue FreeReading
   * @param freeWriting
   *          Is the queue FreeWriting
   * @return Edit successful
   */
  public boolean editQueue(QueueImplMBean queue, String DMQ, String destination, long period, int threshold,
      int nbMaxMsg, boolean freeReading, boolean freeWriting);

  /**
   * Delete a Queue on JORAM
   * 
   * @param queue
   *          The queue to delete
   * @return Delete successful
   */
  public boolean deleteQueue(QueueImplMBean queue);

  /**
   * Clear the waiting requests for a queue on JORAM
   * 
   * @param queueName
   *          Name of the queue
   * @return Clear successful
   */
  public boolean cleanWaitingRequest(QueueImplMBean queue);

  /**
   * Clear the pending messages for a queue on JORAM
   * 
   * @param queueName
   *          Name of the queue
   * @return Clear successful
   */
  public boolean cleanPendingMessage(QueueImplMBean queue);

  /**
   * Create a subscription on JORAM
   * 
   * @param name
   *          Name of the subscription
   * @param nbMaxMsg
   *          Maximum messages on the subscription
   * @param context
   *          Context Id of the subscription
   * @param selector
   *          Selector of the subscription
   * @param subRequest
   *          SubRequest of the subscription
   * @param active
   *          Is the subscription active
   * @param durable
   *          Is the subscription durable
   * @return Create successful
   */
  public boolean createNewSubscription(String name, int nbMaxMsg, int context, String selector,
      int subRequest, boolean active, boolean durable);

  /**
   * Edit a subscription on JORAM
   * 
   * @param sub
   *          The subscription to edit
   * @param nbMaxMsg
   *          Maximum messages on the subscription
   * @param context
   *          Context Id of the subscription
   * @param selector
   *          Selector of the subscription
   * @param subRequest
   *          SubRequest of the subscription
   * @param active
   *          Is the subscription active
   * @param durable
   *          Is the subscription durable
   * @return Edit successful
   */
  public boolean editSubscription(ClientSubscriptionMBean sub, int nbMaxMsg, int context, String selector,
      int subRequest, boolean active, boolean durable);

  /**
   * Delete a Queue in JORAM
   * 
   * @param subscriptionName
   *          Name of the subscription to delete
   * @return Delete successful
   */
  public boolean deleteSubscription(String subscriptionName);

  public float[] getInfos();

}