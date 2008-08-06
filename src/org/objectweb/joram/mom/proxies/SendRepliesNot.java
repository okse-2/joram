/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * 
 * Created on 3 mai 2006
 */
package org.objectweb.joram.mom.proxies;

import java.util.Enumeration;
import java.util.Vector;

import fr.dyade.aaa.agent.Notification;

/**
 * @author feliot
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SendRepliesNot extends Notification {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private Vector replies;
  
  public SendRepliesNot(Vector v) {
    replies = v;
  }
  
  public Enumeration getReplies() {
    return replies.elements();
  }
  
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    output.append(super.toString(output));
    output.append(",replies=");
    output.append(replies);
    output.append(')');
    return output;
  }
}
