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
package fr.dyade.aaa.mom; 
  
/** 
 * A <code>MessageAndAck</code> is used to wrap a message
 * stored in a Queue and knows who will acknowledge it.
 * <p>
 * Modified: Frederic Maistre, 01.2001
 *
 * @see  fr.dyade.aaa.mom.Queue
 */ 
public class MessageAndAck implements java.io.Serializable
{ 
  /** The wrapped message. */
  fr.dyade.aaa.mom.Message msg; 
  /** The JMS Session ID from which the message was sent. */
  private String sessionID;
  /** The AgentClient who received the message. */ 
  private fr.dyade.aaa.agent.AgentId agentClient;
  /** 
   * The key identifying the connection through which 
   * the message is sent. 
   */
  private int driversKey;

  public MessageAndAck(fr.dyade.aaa.mom.Message msg) { 
    this.msg = msg; 
    agentClient = null;
    sessionID = null;
  }  


  /** Method setting the parameters. */
  public void setAgentIdentity(fr.dyade.aaa.agent.AgentId agentClient, 
    String sessionID, int driversKey) { 
    this.agentClient = agentClient;
    this.sessionID = sessionID;
    this.driversKey = driversKey;
  }

  /** Method returning the message hold by the MessageAndAck. */  
  public fr.dyade.aaa.mom.Message getMessage() { 
    return msg; 
  } 

  /** Method returning the driversKey. */
  public int getDriversKey() {
    return driversKey;
  } 

  /** 
   * Method returning the AgentClient's id which has 
   * to acknowledge the message.
   */ 
  public fr.dyade.aaa.agent.AgentId getAgentIdentity() { 
    return agentClient; 
  } 

  /** 
   * Method returning the sessionID which has to 
   * acknowledge the message.
   */
  public String getSessionID() {
    return sessionID;
  }

} 
