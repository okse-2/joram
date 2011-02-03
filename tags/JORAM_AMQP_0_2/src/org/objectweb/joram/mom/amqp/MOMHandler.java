/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2008 - 2009 CNES
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
package org.objectweb.joram.mom.amqp;

import java.util.Map;

import org.objectweb.joram.mom.amqp.marshalling.AMQP;

public interface MOMHandler {

  /**
   * Declare a queue
   * @param queueName the name of the queue
   * @param passive true if we are passively declaring a queue (asserting the queue already exists)
   * @param durable true if we are declaring a durable queue (the queue will survive a server restart)
   * @param exclusive true if we are declaring an exclusive queue
   * @param autoDelete true if we are declaring an autodelete queue (server will delete it when no longer in use)
   * @param arguments other properties (construction arguments) for the queue
   * @param channelNumber the channel the request came from
   * @return the name of the queue returned to the client
   * @throws Exception if an error is encountered
   */
  public AMQP.Queue.DeclareOk queueDeclare(String queueName, boolean passive, boolean durable,
      boolean exclusive, boolean autoDelete, Map arguments, int channelNumber) throws Exception;

  /**
   * Delete a queue, without regard for whether it is in use or has messages on it
   * @param queue the name of the queue
   * @param ifUnused true if the queue should be deleted only if not in use
   * @param ifEmpty true if the queue should be deleted only if empty
   * @param channelNumber the channel the request came from
   * @return the number of messages purged
   * @throws Exception if an error is encountered
   */
  public AMQP.Queue.DeleteOk queueDelete(String queue, boolean ifUnused, boolean ifEmpty, boolean nowait,
      int channelNumber) throws Exception;
  
  public AMQP.Queue.PurgeOk queuePurge(String queue, boolean nowait, int channelNumber) throws Exception;
  
  /**
   * Bind a queue to an exchange.
   * @param queue the name of the queue
   * @param exchange the name of the exchange
   * @param nowait 
   * @param routingKey the routing key to use for the binding
   * @param arguments other properties (binding parameters)
   * @param channelNumber the channel the request came from
   * @throws Exception if an error is encountered
   */
  public void queueBind(String queue, String exchange, boolean nowait, String routingKey, Map arguments,
      int channelNumber) throws Exception;
  
  /**
   * Unbinds a queue from an exchange.
   * @param queue the name of the queue
   * @param exchange the name of the exchange
   * @param routingKey the routing key to use for the binding
   * @param arguments other properties (binding parameters)
   * @param channelNumber the channel the request came from
   * @throws Exception if an error is encountered
   */
  public void queueUnbind(String queue, String exchange, String routingKey, Map arguments, int channelNumber)
      throws Exception;

  /**
   * Retrieve a message from a queue.
   * @param queue the name of the queue
   * @param noAck true if no handshake is required
   * @param channelNumber the channel the request came from
   * @throws Exception if an error is encountered
   */
  public void basicGet(String queue, boolean noAck, int channelNumber) throws Exception;

  /**
   * Publish a message
   * @param publishRequest the message to publish with publishing properties
   * @param channelNumber the channel the request came from
   * @throws Exception if an error is encountered
   */
  public void basicPublish(PublishRequest publishRequest, int channelNumber) throws Exception;

  /**
   * Acknowledge one or several received messages.
   * @param deliveryTag the delivery tag
   * @param multiple true if we are acknowledging multiple messages with the same delivery tag
   * @param channelNumber the channel the request came from
   * @throws Exception if an error is encountered
   */
  public void basicAck(long deliveryTag, boolean multiple, int channelNumber) throws Exception;
  
  /**
   * Start a consumer.
   * @param queue the name of the queue
   * @param noAck true if no handshake is required
   * @param consumerTag a client-generated consumer tag to establish context
   * @param noLocal flag set to true unless server local buffering is required
   * @param exclusive true if this is an exclusive consumer
   * @param noWait 
   * @param channelNumber the channel the request came from
   * @return the consumerTag associated with the new consumer
   */
  public AMQP.Basic.ConsumeOk basicConsume(String queue, boolean noAck, String consumerTag, boolean noLocal,
      boolean exclusive, boolean noWait, int channelNumber) throws Exception;
  
  /**
   * Cancel a consumer.
   * @param consumerTag a client -or server- generated consumer tag to establish context
   * @param channelNumber the channel the request came from
   */
  public void basicCancel(String consumerTag, int channelNumber) throws Exception;

  /**
   * Declare an exchange.
   * @param exchange the name of the exchange
   * @param type the exchange type
   * @param passive true if we are passively declaring a exchange (asserting the exchange already exists)
   * @param durable true if we are declaring a durable exchange (the exchange will survive a server restart)
   * @param arguments other properties (construction arguments) for the exchange
   * @param channelNumber the channel the request came from
   */
  public void exchangeDeclare(String exchange, String type, boolean passive, boolean durable, Map arguments,
      int channelNumber) throws Exception;
  
  /**
   * Delete an exchange
   * @param exchange the name of the exchange
   * @param ifUnused true to indicate that the exchange is only to be deleted if it is unused
   * @param channelNumber the channel the request came from
   */
  public void exchangeDelete(String exchangeName, boolean ifUnused, boolean nowait, int channelNumber)
      throws Exception;
  
  /**
   * Gets MOM properties returned to the client on connection start.
   * @return a HashMap with some properties describing the MOM (product, copyright, version...)
   */
  public Map getMOMProperties();

  /**
   * Closes the {@link MOMHandler}
   */
  public void close();

  public void setConsumer(Consumer consumer);

  public void channelClose(int channelNumber) throws Exception;

  public void connectionClose() throws Exception;

  public void channelOpen(int channelNumber) throws Exception;

  public void basicRecover(boolean requeue, int channelNumber);

}