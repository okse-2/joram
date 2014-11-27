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
 * An <code>AbstractReply</code> is sent by a proxy to a Joram client as a 
 * reply to an <code>AbstractJmsRequest</code>.
 */
public abstract class AbstractReply extends AbstractMessage {
  /**
   * Constructs an <code>AbstractReply</code>.
   */
  public AbstractReply() {}

  /**
   * Constructs an <code>AbstractReply</code>.
   *
   * @param correlationId  Identifier of the replied request.
   */
  public AbstractReply(int correlationId) {
    this.correlationId = correlationId;
  }

  /** Identifier of the replied request. */
  protected int correlationId = -1;

  /** Returns the replied request identifier. */
  public final int getCorrelationId() {
    return correlationId;
  }

  public void toString(StringBuffer strbuf) {
    strbuf.append('(');
    strbuf.append("correlationId=").append(correlationId);
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
    os.writeInt(correlationId);
    //throw new IOException("writeTo not implemented for reply");
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputXStream is) throws IOException {
    correlationId = is.readInt();
  }
}
