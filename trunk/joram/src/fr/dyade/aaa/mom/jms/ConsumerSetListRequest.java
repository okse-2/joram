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

/**
 * A <code>ConsumerSetListRequest</code> is sent by a
 * <code>MessageConsumer</code> on which a message listener is set.
 */
public class ConsumerSetListRequest extends AbstractJmsRequest
{
  /** Selector for filtering messages on a queue. */
  private String selector;
  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;

  /**
   * Constructs an <code>ConsumerSetListRequest</code>.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param selector  Selector for filtering messages.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  public ConsumerSetListRequest(String targetName, String selector, 
                                boolean queueMode)
  {
    super(targetName);
    this.selector = selector;
    this.queueMode = queueMode;
  }


  /** Returns the selector for filtering messages. */
  public String getSelector()
  {
    return selector;
  }

  /** Returns <code>true</code> if the request is destinated to a queue. */
  public boolean getQueueMode()
  {
    return queueMode;
  }
}
