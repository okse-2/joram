/*
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.agent;

public class AgentDeleteRequest extends Notification {

  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  /**
   * the agent identified by 'reply' will receive a DeleteAck
   * when destruction complete
   */
  public AgentId reply;

  /**
   * The extra information is added to the DeleteAck when notifying the reply
   * agent.
   */
  public Object extraInformation;

  public AgentDeleteRequest(AgentId reply) {
    this.reply = reply;
  }

  public AgentDeleteRequest(AgentId reply, Object extraInformation) {
    this.reply = reply;
    this.extraInformation = extraInformation;
  }
}
