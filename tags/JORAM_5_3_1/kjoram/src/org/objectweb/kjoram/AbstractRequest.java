/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

import java.io.IOException;

/**
 * An <code>AbstractRequest</code> is a request sent by a Joram client
 * to its proxy.
 */
public abstract class AbstractRequest extends AbstractMessage {
  /**
   * Constructs an <code>AbstractRequest</code>.
   */
  public AbstractRequest() {}

  /**
   * Constructs an <code>AbstractRequest</code>.
   *
   * @param target  String identifier of the request target, either a queue
   *          name, or a subscription name.
   */
  public AbstractRequest(String target) {
    this.target = target;
  }

  /** 
   * Identifier of the request. 
   */
  protected volatile int requestId = -1;

  /** 
   * Sets the request identifier. 
   */
  public final void setRequestId(int requestId) {
    this.requestId = requestId;
  }
  
  /** Returns the request identifier. */
  public final synchronized int getRequestId() {
    return requestId;
  }

  /**
   * The request target is either a destination agent name, or a subscription 
   * name.
   */
  protected String target = null;

  public void toString(StringBuffer strbuf) {
    strbuf.append('(');
    strbuf.append("requestId=").append(requestId);
    strbuf.append(",target=").append(target);
    strbuf.append(')');
  }

  // ==================================================
  // Streamable interface
  // ==================================================

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputXStream os) throws IOException {
    os.writeInt(requestId);
    os.writeString(target);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputXStream is) throws IOException {
   // throw new IOException("readFrom not implemented for request");
    requestId = is.readInt();
    target = is.readString();
  }
}
