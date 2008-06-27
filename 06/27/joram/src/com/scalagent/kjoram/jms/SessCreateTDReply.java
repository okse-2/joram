/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram.jms;

import java.util.Hashtable;
import java.util.Enumeration;

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

  /**
   * Constructs a <code>SessCreateTDReply</code> instance.
   */
  public SessCreateTDReply()
  {}


  /** Sets the destination identifier. */
  public void setAgentId(String agentId)
  {
    this.agentId = agentId;
  }

  /** Returns the temporary destination's agent identifier. */
  public String getAgentId()
  {
    return agentId;
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    if (agentId != null)
      h.put("agentId",agentId);
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    SessCreateTDReply req = new SessCreateTDReply();
    req.setCorrelationId(((Integer) h.get("correlationId")).intValue());
    req.setAgentId((String) h.get("agentId"));
    return req;
  }
}
