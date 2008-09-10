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

import java.util.Vector;

/**
 * A <code>BrowseReply</code> instance is used by a queue for replying
 * to a client <code>BrowseRequest</code> by sending a vector of its messages.
 */
public class BrowseReply extends AbstractReply {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** The message contained in the queue. */
  private Message message = null;
  /** The messages contained in the queue. */
  private Vector messages = null;

  /**
   * Constructs a <code>BrowseReply</code> instance.
   *
   * @param req  The <code>BrowseRequest</code> actually replied.
   */
  public BrowseReply(BrowseRequest req) {
    super(req.getClientContext(), req.getRequestId());
  }

  
  /** Adds a message. */
  public void addMessage(Message msg) {
    if (message == null && messages == null)
      message = msg;
    else {
      if (messages == null) {
        messages = new Vector();
        messages.add(message);
        message = null;
      }
      messages.add(msg);
    }
  }

  /** Returns the messages carried by this reply. */
  public Vector getMessages() {
    if (message != null) {
      Vector vec = new Vector();
      vec.add(message);
      return vec;
    }
    return messages;
  }
} 
