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
package fr.dyade.aaa.jndi2.distributed;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.common.Strings;
import fr.dyade.aaa.jndi2.impl.NamingContextInfo;

public class InitJndiServerNot extends Notification {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private AgentId[] jndiServerIds;

  private NamingContextInfo[] contexts;
  
  /**
   * Indicates whether this notification is
   * a request (spontaneously sent)
   * or a reply to a request.
   */
  private boolean isRequest;

  public InitJndiServerNot(AgentId[] jndiServerIds,
                           NamingContextInfo[] contexts,
                           boolean isRequest) {
    this.jndiServerIds = jndiServerIds;
    this.contexts = contexts;
    this.isRequest = isRequest;
  }

  public final AgentId[] getJndiServerIds() {
    return jndiServerIds;
  }

  public final NamingContextInfo[] getContexts() {
    return contexts;
  }
  
  public final boolean isRequest() {
    return isRequest;
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output
   *	buffer to fill in
   * @return
	<code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",jndiServerIds=");
    Strings.toString(output, jndiServerIds);
    output.append(",contexts=");
    Strings.toString(output, contexts);
    output.append(",isRequest=" + isRequest);
    output.append(')');
    return output;
  }
}
