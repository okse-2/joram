/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
 * Contributor(s):
 */
package fr.dyade.aaa.mom.proxies;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.mom.jms.AbstractJmsReply;


/**
 * The <code>ProxyAgentItf</code> interface defines the methods which must be
 * provided by a proxy agent hosting a <code>ProxyImpl</code> instance.
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
   * @param id  Identifies the context within which the sending occurs.
   * @param reply  The reply to send to the client.
   */
  public void sendToClient(int id, AbstractJmsReply reply);
}
