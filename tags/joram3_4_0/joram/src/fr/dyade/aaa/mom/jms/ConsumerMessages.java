/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
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
    this.comingFrom = comingFrom;
    this.queueMode = queueMode;
    if (message != null) {
      messages = new Vector();
      messages.add(message);
    }
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

  /**
   * Transforms this reply into a vector of primitive values that can
   * be vehiculated through the SOAP protocol.
   */
  public Vector soapCode()
  {
    Vector vec = new Vector();

    vec.add("ConsumerMessages");

    // Coding the reply fields:
    vec.add(getCorrelationId());
    vec.add(comingFrom);
    vec.add(new Boolean(queueMode));
    
    // Coding and adding the messages into a vector, if any:
    if (messages != null && ! messages.isEmpty()) {
      Vector msgs = new Vector();
      while (! messages.isEmpty())
        msgs.add(((Message) messages.remove(0)).soapCode());

      vec.add(msgs);
    }
    return vec;
  }

  /** 
   * Transforms a vector of primitive values into a
   * <code>ConsumerMessages</code> reply.
   */
  public static ConsumerMessages soapDecode(Vector vec)
  {
    vec.remove(0);

    String correlationId = (String) vec.remove(0);
    String comingFrom = (String) vec.remove(0);
    boolean queueMode = ((Boolean) vec.remove(0)).booleanValue();

    // If the ConsumerMessages does not carry any message:
    if (vec.isEmpty())
      return new ConsumerMessages(correlationId, comingFrom, queueMode);

    // Else, decoding the message(s):
    Vector codedMsgs = (Vector) vec.remove(0);
    Vector decodedMsgs = new Vector();
    while (! codedMsgs.isEmpty())
      decodedMsgs.add(Message.soapDecode((Vector) codedMsgs.remove(0)));

    return new ConsumerMessages(correlationId, decodedMsgs, comingFrom,
                                queueMode);
  } 
}
