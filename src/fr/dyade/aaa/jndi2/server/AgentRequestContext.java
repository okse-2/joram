/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): David Feliot, Nicolas Tachker
 */
package fr.dyade.aaa.jndi2.server;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.jndi2.msg.*;

public class AgentRequestContext 
    extends RequestContext {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private JndiRequest request;

  private AgentId from;

  private boolean doReply;

  public AgentRequestContext(JndiRequest request,
                             AgentId from,
                             boolean doReply) {
    this.request = request;
    this.from = from;
    this.doReply = doReply;
  }

  public AgentId getFrom() {
    return from;
  }

  public JndiRequest getRequest() {
    return request;
  }

  public void reply(JndiReply reply) {
    if (doReply) {
      Channel.sendTo(
        from, new JndiReplyNot(request, reply));
    }
  }
}
