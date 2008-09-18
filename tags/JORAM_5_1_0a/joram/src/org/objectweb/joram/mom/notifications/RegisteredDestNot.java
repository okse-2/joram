/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2006 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.agent.AgentId;

public class RegisteredDestNot 
    extends fr.dyade.aaa.agent.Notification {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private AgentId reply;
  private String name;
  private AgentId dest = null;
  
  public RegisteredDestNot(String name,
                           AgentId reply) {
    this.name = name;
    this.reply = reply;
  }
  
  public final void setDestination(AgentId dest) {
    this.dest = dest;
  }

  public final AgentId getDestination() {
    return dest;
  }
  
  public final String getName() {
    return name;
  }

  public final AgentId getReply() {
    return reply;
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
    output.append(",name=").append(name);
    output.append(",dest=").append(dest);
    output.append(",reply=").append(reply);
    output.append(')');

    return output;
  }
}
