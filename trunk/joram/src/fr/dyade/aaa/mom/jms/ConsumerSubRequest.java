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

/**
 * A <code>ConsumerSubRequest</code> is sent by a constructing
 * <code>MessageConsumer</code> destinated to consume messages on a topic.
 */
public class ConsumerSubRequest extends AbstractJmsRequest
{
  /** The subscription's name. */
  private String subName;
  /** The selector for filtering messages. */
  private String selector;
  /**
   * <code>true</code> if the subscriber does not wish to consume messages
   * produced by its connection.
   */
  private boolean noLocal;
  /** <code>true</code> if the subscription is durable. */
  private boolean durable;

  /**
   * Constructs a <code>ConsumerSubRequest</code>.
   *
   * @param topic  The topic identifier the client wishes to subscribe to.
   * @param subName  The subscription's name.
   * @param selector  The selector for filtering messages, if any.
   * @param noLocal  <code>true</code> for not consuming the local messages.
   * @param durable  <code>true</code> for a durable subscription.
   */
  public ConsumerSubRequest(String topic, String subName, String selector,
                            boolean noLocal, boolean durable)
  {
    super(topic);
    this.subName = subName;
    this.selector = selector;
    this.noLocal = noLocal;
    this.durable = durable;
  }

  /** Returns the name of the subscription. */
  public String getSubName()
  {
    return subName;
  }

  /** Returns the selector for filtering the messages. */
  public String getSelector()
  {
    return selector;
  }

  /** Returns <code>true</code> for not consuming the local messages. */
  public boolean getNoLocal()
  {
    return noLocal;
  }

  /** Returns <code>true</code> for a durable subscription. */
  public boolean getDurable()
  {
    return durable;
  }
}
