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

import fr.dyade.aaa.agent.AgentId;

/**
 * A <code>SetDMQRequest</code> instance is used by a <b>client</b> agent
 * for notifying a destination or a proxy which dead message queue is
 * attributed to it.
 */
public class SetDMQRequest extends AbstractRequest
{
  /** The dead message queue identifier, <code>null</code> for no DMQ. */
  private AgentId dmqId;


  /**
   * Constructs a <code>SetDMQRequest</code> instance involved in an
   * external client - MOM interaction.
   *
   * @param key  See superclass.
   * @param requestId  See superclass.
   * @param dmqId  The dead message queue identifier, <code>null</code> for
   *          none.
   */
  public SetDMQRequest(int key, String requestId, AgentId dmqId)
  {
    super(key, requestId);
    this.dmqId = dmqId;
  }

  /**
   * Constructs a <code>setDMQRequest</code> instance not involved in an
   * external client - MOM interaction.
   *
   * @param requestId  See superclass.
   * @param dmqId  The dead message queue identifier, <code>null</code> for
   *          none.
   */
  public SetDMQRequest(String requestId, AgentId dmqId)
  {
    this(0, requestId, dmqId);
  }

  
  /**
   * Returns the dead message queue identifier, <code>null</code> for none.
   */
  public AgentId getDmqId()
  {
    return dmqId;
  }
} 
