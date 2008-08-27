/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Vector;

import org.objectweb.joram.shared.messages.Message;

/**
 * A <code>QBrowseReply</code> instance is used by a JMS client proxy for
 * forwarding a <code>BrowseReply</code> destination notification,
 * actually replying to a client <code>QBrowseRequest</code>.
 */
public final class QBrowseReply extends AbstractJmsReply {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** The vector of messages carried by this reply. */
  private Vector messages = null;

  /** Returns the vector of messages carried by this reply. */
  public Vector getMessages() {
    if (messages == null)
      messages = new Vector();
    return messages;
  }

  public void addMessage(Message msg) {
    if (messages == null)
      messages = new Vector();
    messages.addElement(msg);
  }

  protected int getClassId() {
    return QBROWSE_REPLY;
  }

  /**
   * Constructs a <code>QBrowseReply</code>.
   */
  public QBrowseReply(int correlationId, Vector messages) {
    super(correlationId);
    this.messages = messages;
  }

  /**
   * Public no-arg constructor needed by Externalizable.
   */
  public QBrowseReply() {
  }

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    Message.writeVectorTo(messages, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    messages = Message.readVectorFrom(is);
  }
}
