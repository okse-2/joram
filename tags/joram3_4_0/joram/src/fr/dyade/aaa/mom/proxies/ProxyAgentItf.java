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
package fr.dyade.aaa.mom.proxies;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.mom.jms.AbstractJmsReply;

/**
 * The <code>ProxyAgentItf</code> interface defines the methods provided to
 * <code>ProxyImpl</code> objects for actually communicating with MOM
 * destinations and clients.
 * <p>
 * This interface is implemented by proxy agents dedicated to a given
 * communication protocol (as TCP or SOAP).
 *
 * @see fr.dyade.aaa.mom.tcp.JmsProxy
 * @see fr.dyade.aaa.mom.soap.SoapProxy
 */
public interface ProxyAgentItf
{
  /** Returns the proxy's <code>AgentId</code> identifier. */
  public AgentId getAgentId();

  /** Sends a notification to a given agent. */ 
  public void sendNot(AgentId to, Notification not);

  /**
   * Sends an <code>AbstractJmsReply</code> to a given client.
   *
   * @param id  Identifies the client to send the reply to.
   * @param reply  The reply to send to the client.
   */
  public void sendToClient(int id, AbstractJmsReply reply);
}
