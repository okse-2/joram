/*
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

import java.io.*;

/**
 * This notification is used to ask aa agent creation to a remote
 * agent factory.
 * @author  Andr* Freyssinet
 * @version 1.0, 12/10/97
 */
public class AgentCreateRequest extends Notification {
  /** RCS version number of this file: $Revision: 1.13 $ */
  public static final String RCS_VERSION="@(#)$Id: AgentCreateRequest.java,v 1.13 2003-06-23 13:37:51 fmaistre Exp $"; 


  static final long serialVersionUID = -873215263839213534L;

  /** Id. of agent to reply to */
  public AgentId reply;
  /** Id. of agent to deploy. Used in case of unrecoverable error to report
   * the creation status.
   */
  public AgentId deploy;
  /** Serialized state of the agent */
  public byte agentState[];

  public AgentCreateRequest(Agent agent) throws IOException {
    this(agent, null);
  }

  public AgentCreateRequest(Agent agent, AgentId reply) throws IOException {
    super();
    
    this.reply = reply;
    this.deploy = agent.getId();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(agent);
    oos.flush();
    agentState = bos.toByteArray();
  }
}
