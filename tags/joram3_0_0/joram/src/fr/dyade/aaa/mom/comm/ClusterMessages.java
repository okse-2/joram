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
package fr.dyade.aaa.mom.comm;

import fr.dyade.aaa.agent.AgentId;

import java.util.Vector;

/**
 * A <code>ClusterMessages</code> instance is used by a topic for forwarding
 * one or many messages it received from a client to an other topic part of
 * the same cluster.
 */
public class ClusterMessages extends AbstractRequest
{
  /** Identifier of the client which sent the messages. */
  private AgentId from;
  /** Vector holding the messages forwarded by the topic. */
  private Vector messages;


  /**
   * Constructs a <code>ClusterMessages</code> instance.
   *
   * @param from  Identifier of the client which sent the messages.
   * @param req  The client request containing the messages to forward.
   */
  public ClusterMessages(AgentId from, ClientMessages req)
  {
    super(0, null);
    this.from = from;
    messages = req.getMessages();
  }


  /** Returns the identifier of the client which sent the messages. */
  public AgentId getFrom()
  {
    return from;
  }

  /** Returns the vector of the sent messages. */
  public Vector getMessages()
  {
    return messages;
  }
} 