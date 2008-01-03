/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.jndi2.server;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.jndi2.msg.*;

public class JndiReplyNot extends Notification {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private JndiRequest request;
  
  private JndiReply reply;

  public JndiReplyNot(JndiRequest request,
                      JndiReply reply) {
    this.request = request;
    this.reply = reply;
  }

  public final JndiRequest getRequest() {
    return request;
  }

  public final JndiReply getReply() {
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
    output.append(",request=").append(request);
    output.append(",reply=").append(reply);
    output.append(')');

    return output;
  }
}
