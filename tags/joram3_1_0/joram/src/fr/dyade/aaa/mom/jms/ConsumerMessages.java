/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.mom.jms;

import fr.dyade.aaa.mom.messages.Message;

import java.util.Vector;

/**
 * A <code>ConsumerMessages</code> is used by a JMS proxy for sending messages
 * to a consumer.
 */
public class ConsumerMessages extends AbstractJmsReply
{
  /** Name of the subscription or the queue the messages come from. */
  private String comingFrom = null;
  /** <code>true</code> if the messages come from a queue. */
  private boolean queueMode;
  /** The vector of messages carried by this reply, if any. */
  private Vector messages = null;

  /**
   * Constructs a <code>ConsumerMessages</code> instance.
   *
   * @param correlationId  See superclass.
   * @param message  Message to the client.
   * @param comingFrom  Name of the queue or the subscription the message
   *          come from.
   * @param queueMode  <code>true</code> if the message come from a queue.
   */
  public ConsumerMessages(String correlationId, Message message,
                          String comingFrom, boolean queueMode)
  {
    super(correlationId);
    messages = new Vector();
    messages.add(message);
    this.comingFrom = comingFrom;
    this.queueMode = queueMode;
  }

  /**
   * Constructs a <code>ConsumerMessages</code> instance.
   *
   * @param correlationId  See superclass.
   * @param messages  The vector of sent messages.
   * @param comingFrom  Name of the queue or the subscription the messages
   *          comes from.
   * @param queueMode  <code>true</code> if the messages come from a queue.
   */
  public ConsumerMessages(String correlationId, Vector messages,
                          String comingFrom, boolean queueMode)
  {
    super(correlationId);
    this.messages = messages;
    this.comingFrom = comingFrom;
    this.queueMode = queueMode;
  }

  /**
   * Constructs a <code>ConsumerMessages</code> instance carrying no
   * message.
   *
   * @param correlationId  See superclass.
   * @param comingFrom  Name of the queue or the subscription the reply
   *          comes from.
   * @param queueMode  <code>true</code> if it replies to a queue consumer.
   */
  public ConsumerMessages(String correlationId, String comingFrom,
                          boolean queueMode)
  {
    super(correlationId);
    this.comingFrom = comingFrom;
    this.queueMode = queueMode;
  }

  /**
   * Returns the name of the queue or the subscription the messages come
   * from.
   */
  public String comesFrom()
  {
    return comingFrom;
  }

  /** Returns <code>true</code> if the messages come from a queue. */
  public boolean getQueueMode()
  {
    return queueMode;
  }

  /** Returns the vector of sent messages. */
  public Vector getMessages()
  {
    if (messages == null)
      return new Vector();
    return messages;
  }

  /** Returns the first sent message. */
  public Message getMessage()
  {
    if (messages == null || messages.isEmpty())
      return null;

    return (Message) messages.elementAt(0);
  }
}
