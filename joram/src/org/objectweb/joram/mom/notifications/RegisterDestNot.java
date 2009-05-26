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

public class RegisterDestNot extends fr.dyade.aaa.agent.Notification {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private AgentId id;
  private String name;
  private String className;
  private byte type;
  
  public RegisterDestNot(AgentId id,
                         String name,
                         String className,
                         byte type) {
    this.id = id;
    this.name = name;
    this.className = className;
    this.type = type;
  }
  
  public final AgentId getId() {
    return id;
  }
  
  public final String getName() {
    return name;
  }
  
  public final String getClassName() {
    return className;
  }
  
  public final byte getType() {
    return type;
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
    output.append(",id=").append(id);
    output.append(",name=").append(name);
    output.append(",className=").append(className);
    output.append(",type=").append(type);
    output.append(')');

    return output;
  }
}
