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

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * An <code>AbstractJmsReply</code> is sent by a proxy to a Joram client as a 
 * reply to an <code>AbstractJmsRequest</code>.
 */
public abstract class AbstractJmsReply extends AbstractJmsMessage {
  /** Identifier of the replied request. */
  protected int correlationId = -1;

  /** Sets the replied request identifier. */
  public final void setCorrelationId(int correlationId) {
    this.correlationId = correlationId;
  }

  /** Returns the replied request identifier. */
  public final int getCorrelationId() {
    return correlationId;
  }

  /**
   * Constructs an <code>AbstractJmsReply</code>.
   */
  public AbstractJmsReply() {}

  /**
   * Constructs an <code>AbstractJmsReply</code>.
   *
   * @param correlationId  Identifier of the replied request.
   */
  public AbstractJmsReply(int correlationId) {
    this.correlationId = correlationId;
  }

  public final String toString() {
    StringBuffer strbuf = new StringBuffer();
    toString(strbuf);
    return strbuf.toString();
  }

  public void toString(StringBuffer strbuf) {
    strbuf.append('(').append(super.toString());
    strbuf.append("correlationId=").append(correlationId);
    strbuf.append(')');
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
    StreamUtil.writeTo(correlationId, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    correlationId = StreamUtil.readIntFrom(is);
  }
  
  // JORAM_PERF_BRANCH
  public int getAbstractJmsReplyEncodedSize() throws IOException {
    return 4;
  }
}
