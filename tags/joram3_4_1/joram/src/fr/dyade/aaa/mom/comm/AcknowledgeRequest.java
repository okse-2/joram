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
package fr.dyade.aaa.mom.comm;

import java.util.Vector;

/**
 * An <code>AcknowledgeRequest</code> instance is used by a <b>client</b> agent
 * for acknowledging one or many messages on a queue.
 */
public class AcknowledgeRequest extends AbstractRequest
{
  /** Vector of identifiers of the messages to acknowledge. */
  private Vector msgIds;
   
  /**
   * Constructs an instance of <code>AcknowledgeRequest</code> involved in
   * an external client - MOM communication.
   *
   * @param key  See superclass.
   * @param requestId  See superclass.
   * @param msgIds  Vector of message identifiers.
   */
  public AcknowledgeRequest(int key, String requestId, Vector msgIds)
  {
    super(key, requestId);
    this.msgIds = msgIds;
  }

  /**
   * Constructs an <code>AcknowledgeRequest</code> not involved in an external
   * client - MOM communication.
   *
   * @param requestId  See superclass.
   * @param msgIds  Vector of message identifiers.
   */
  public AcknowledgeRequest(String requestId, Vector msgIds)
  {
    this(0, requestId, msgIds);
  }


  /** Returns the vector of message identifiers. */
  public Vector getMsgIds()
  {
    return msgIds;
  }
} 