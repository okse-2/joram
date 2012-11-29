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
 * A <code>ConsumerUnsetListRequest</code> is sent by a
 * <code>MessageConsumer</code> which listener is unset.
 */
public final class ConsumerUnsetListRequest extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** <code>true</code> if the listener is listening to a queue. */
  private boolean queueMode;

  /** Sets the listener mode (queue or topic listener). */
  public void setQueueMode(boolean queueMode) {
    this.queueMode = queueMode;
  }

  /** Returns <code>true</code> for a queue listener. */
  public boolean getQueueMode() {
    return queueMode;
  }

  /**
   * Identifier of the last listener request, cancelled by this
   * request, queue mode only.
   */
  private int cancelledRequestId = -1;

  /**
   * Sets the identifier of the last listener request, cancelled by this
   * request, queue mode only.
   */
  public void setCancelledRequestId(int cancelledRequestId) {
    this.cancelledRequestId = cancelledRequestId;
  }

  /**
   * Returns the identifier of the last listener request, cancelled by this
   * request, queue mode only.
   */
  public int getCancelledRequestId() {
    return cancelledRequestId;
  }

  protected int getClassId() {
    return CONSUMER_UNSET_LIST_REQUEST;
  }

  /**
   * Constructs a <code>ConsumerUnsetListRequest</code>.
   *
   * @param queueMode  <code>true</code> if the listener is listening to a
   *          queue.
   */
  public ConsumerUnsetListRequest(boolean queueMode) {
    this.queueMode = queueMode;
  }

  /**
   * Constructs a <code>ConsumerUnsetListRequest</code>.
   */
  public ConsumerUnsetListRequest() {}

  public void toString(StringBuffer strbuf) {
    super.toString(strbuf);
    strbuf.append(",queueMode=").append(queueMode);
    strbuf.append(",cancelledRequestId=").append(cancelledRequestId);
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
    super.writeTo(os);
    StreamUtil.writeTo(queueMode, os);
    StreamUtil.writeTo(cancelledRequestId, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    queueMode = StreamUtil.readBooleanFrom(is);
    cancelledRequestId = StreamUtil.readIntFrom(is);
  }
}
