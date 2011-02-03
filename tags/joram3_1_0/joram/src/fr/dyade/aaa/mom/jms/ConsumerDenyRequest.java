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

import java.util.Vector;

/**
 * A <code>ConsumerDenyRequest</code> instance is used by a
 * <code>MessageConsumer</code> for denying a received message.
 */
public class ConsumerDenyRequest extends AbstractJmsRequest
{
  /** Message identifier. */
  private String id;
  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;
  /** <code>true</code> if the request must not be acked by the server. */
  private boolean doNotAck = false;

  /**
   * Constructs a <code>ConsumerDenyRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param id  The message identifier.
   * @param queueMode  <code>true</code> if this request is destinated to
   *          a queue.
   */
  public ConsumerDenyRequest(String targetName, String id, boolean queueMode)
  {
    super(targetName);
    this.id = id;
    this.queueMode = queueMode;
  }

  /**
   * Constructs a <code>ConsumerDenyRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param id  The message identifier.
   * @param queueMode  <code>true</code> if this request is destinated to
   *          a queue.
   * @param doNotAck  <code>true</code> if this request must not be acked by
   *          the server.
   */
  public ConsumerDenyRequest(String targetName, String id, boolean queueMode,
                             boolean doNotAck)
  {
    super(targetName);
    this.id = id;
    this.queueMode = queueMode;
    this.doNotAck = doNotAck;
  }

  /** Returns the denied message identifier. */
  public String getId()
  {
    return id;
  }

  /** Returns <code>true</code> if the request is destinated to a queue. */
  public boolean getQueueMode()
  {
    return queueMode;
  }

  /**
   * Returns <code>true</code> if the request must not be acked by the 
   * server.
   */
  public boolean doNotAck()
  {
    return doNotAck;
  }
}