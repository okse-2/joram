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

import java.util.Vector;

/**
 * A <code>SessDenyRequest</code> instance is used by a <code>Session</code>
 * for denying the messages it consumed.
 */
public class SessDenyRequest extends AbstractJmsRequest
{
  /** Vector of message identifiers. */
  private Vector ids;
  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode; 
  /** <code>true</code> if the request must not be acked by the server. */
  private boolean doNotAck = false;

  /**
   * Constructs a <code>SessDenyRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param ids  Vector of denied message identifiers.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  public SessDenyRequest(String targetName, Vector ids, boolean queueMode)
  {
    super(targetName);
    this.ids = ids;
    this.queueMode = queueMode;
  }

  /**
   * Constructs a <code>SessDenyRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param ids  Vector of denied message identifiers.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   * @param doNotAck  <code>true</code> if this request must not be acked by
   *          the server.
   */
  public SessDenyRequest(String targetName, Vector ids, boolean queueMode,
                         boolean doNotAck)
  {
    super(targetName);
    this.ids = ids;
    this.queueMode = queueMode;
    this.doNotAck = doNotAck;
  }

  /**
   * Constructs a <code>SessDenyRequest</code> instance.
   */
  public SessDenyRequest()
  {}

  /** Sets the vector of identifiers. */
  public void setIds(Vector ids)
  {
    this.ids = ids;
  }

  /** Sets the target destination type. */
  public void setQueueMode(boolean queueMode)
  {
    this.queueMode = queueMode;
  }

  /** Sets the server ack policy. */
  public void setDoNotAck(boolean doNotAck)
  {
    this.doNotAck = doNotAck;
  }

  /** Returns the vector of denyed messages identifiers. */
  public Vector getIds()
  {
    return ids;
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
  public boolean getDoNotAck()
  {
    return doNotAck;
  }
}
