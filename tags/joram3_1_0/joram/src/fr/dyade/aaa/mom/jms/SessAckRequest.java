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
 * A <code>SessAckRequest</code> instance is used by a <code>Session</code>
 * for acknowledging the messages it consumed.
 */
public class SessAckRequest extends AbstractJmsRequest
{
  /** Vector of message identifiers. */
  private Vector ids;
  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;

  /**
   * Constructs a <code>SessAckRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param ids  Vector of acknowledged message identifiers.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  public SessAckRequest(String targetName, Vector ids, boolean queueMode)
  {
    super(targetName);
    this.ids = ids;
    this.queueMode = queueMode;
  }

  /** Returns the vector of acknowledged messages identifiers. */
  public Vector getIds()
  {
    return ids;
  }

  /** Returns <code>true</code> if the request is destinated to a queue. */
  public boolean getQueueMode()
  {
    return queueMode;
  }
}
