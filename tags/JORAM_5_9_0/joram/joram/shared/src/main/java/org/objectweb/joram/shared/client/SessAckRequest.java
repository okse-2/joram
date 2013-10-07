/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.objectweb.joram.shared.messages.Message;

import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.EncodableHelper;
import fr.dyade.aaa.common.encoding.Encoder;
import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>SessAckRequest</code> instance is used by a <code>Session</code>
 * for acknowledging the messages it consumed.
 */
public final class SessAckRequest extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  /** Vector of message identifiers. */
  private Vector<String> ids;

  /** Sets the vector of identifiers. */
  public void setIds(Vector ids) {
    this.ids = ids;
  }

  public void addId(String id) {
    ids.addElement(id);
  }

  /** Returns the vector of acknowledged messages identifiers. */
  public Vector getIds() {
    return ids;
  }

  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;

  /** Sets the target destination type. */
  public void setQueueMode(boolean queueMode) {
    this.queueMode = queueMode;
  }

  /** Returns <code>true</code> if the request is destinated to a queue. */
  public boolean getQueueMode() {
    return queueMode;
  }

  protected int getClassId() {
    return SESS_ACK_REQUEST;
  }

  /**
   * Constructs a <code>SessAckRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param ids  Vector of acknowledged message identifiers.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  public SessAckRequest(String targetName, Vector ids, boolean queueMode) {
    super(targetName);
    this.ids = ids;
    this.queueMode = queueMode;
  }

  /**
   * Constructs a <code>SessAckRequest</code> instance.
   */
  public SessAckRequest() {
    ids = new Vector();
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
    StreamUtil.writeListOfStringTo(ids, os);
    StreamUtil.writeTo(queueMode, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    ids = StreamUtil.readVectorOfStringFrom(is);
    queueMode = StreamUtil.readBooleanFrom(is);
  }
  
  public int getEncodableClassId() {
    // Not defined
    return -1;
  }
  
  public int getEncodedSize() throws Exception {
    int res = super.getEncodedSize()
        + INT_ENCODED_SIZE + BOOLEAN_ENCODED_SIZE;
    for (String id : ids) {
      res += EncodableHelper.getStringEncodedSize(id);
    }
    return res;
  }
  
  public void encode(Encoder encoder) throws Exception {
    super.encode(encoder);
    encoder.encodeUnsignedInt(ids.size());
    for (String id : ids) {
      encoder.encodeString(id);
    }
    encoder.encodeBoolean(queueMode);
  }

  public void decode(Decoder decoder) throws Exception {
    super.decode(decoder);
    int size = decoder.decodeUnsignedInt();
    for (int i = 0; i < size; i++) {
      decoder.decodeString();
    }
    queueMode = decoder.decodeBoolean();
  }
  
}
