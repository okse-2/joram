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
 * A <code>ConsumerReceiveRequest</code> is sent by a
 * <code>MessageConsumer</code> when requesting a message.
 */
public class ConsumerReceiveRequest extends AbstractJmsRequest
{
  /** The selector for filtering messages on a queue. */
  private String selector;
  /** The time to live value of the request (negative for infinite). */
  private long timeToLive;
  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;


  /**
   * Constructs a <code>ConsumerReceiveRequest</code>.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param selector  The selector for filtering messages, if any.
   * @param timeToLive  Time to live value in milliseconds, negative for
   *          infinite.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  public ConsumerReceiveRequest(String targetName, String selector,
                                long timeToLive, boolean queueMode)
  {
    super(targetName);
    this.selector = selector;
    this.timeToLive = timeToLive;
    this.queueMode = queueMode;
  }

  /** Returns the selector for filtering the messages. */
  public String getSelector()
  {
    return selector;
  }

  /** Returns the time to live value in milliseconds. */
  public long getTimeToLive()
  {
    return timeToLive;
  }

  /** Returns <code>true</code> if the request is destinated to a queue. */
  public boolean getQueueMode()
  {
    return queueMode;
  }
}
