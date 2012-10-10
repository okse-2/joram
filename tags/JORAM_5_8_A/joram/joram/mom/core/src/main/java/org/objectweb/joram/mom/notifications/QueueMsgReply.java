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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.notifications;

import org.objectweb.joram.shared.messages.Message;

import java.util.*;

/**
 * A <code>QueueMsgReply</code> instance is used by a queue for replying to a
 * <code>ReceiveRequest</code> by sending a message to a client.
 */
public class QueueMsgReply extends AbstractReplyNot {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** The message sent by the queue. */
  private Vector messages;

  /**
   * Constructs a <code>QueueMsgReply</code> instance.
   *
   * @param req  The <code>ReceiveRequest</code> actually replied.
   */
  public QueueMsgReply(ReceiveRequest req) {
    super(req.getClientContext(), req.getRequestId());
    messages = new Vector();
  }

  /** Returns the message wrapped by this reply. */
  public Vector getMessages() {
    return messages;
  }

  public int getSize() {
    return messages.size();
  }

  public void addMessage(Message msg) {
    messages.addElement(msg);
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output 	buffer to fill in
   * @return <code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",messages=").append(messages);
    output.append(')');

    return output;
  }
} 
