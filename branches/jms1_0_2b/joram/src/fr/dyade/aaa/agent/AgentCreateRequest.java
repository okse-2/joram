/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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
  /** RCS version number of this file: $Revision: 1.9 $ */
  public static final String RCS_VERSION="@(#)$Id: AgentCreateRequest.java,v 1.9 2002-03-26 16:08:39 joram Exp $"; 

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
