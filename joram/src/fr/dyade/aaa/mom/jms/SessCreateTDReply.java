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
 * A <code>SessCreateTDReply</code> is used by a JMS proxy for replying
 * to a <code>SessCreate&lt;TQ/TT&gt;Request</code>.
 */
public class SessCreateTDReply extends AbstractJmsReply
{
  /** The string identifier of the temporary destination agent. */
  private String agentId;

  /**
   * Constructs a <code>SessCreateTDReply</code> instance.
   *
   * @param request  The replied request.
   * @param agentId  String identifier of the destination agent.
   */
  public SessCreateTDReply(AbstractJmsRequest request, String agentId)
  {
    super(request.getRequestId());
    this.agentId = agentId;
  }

  /** Returns the temporary destination's agent identifier. */
  public String getAgentId()
  {
    return agentId;
  }
}
