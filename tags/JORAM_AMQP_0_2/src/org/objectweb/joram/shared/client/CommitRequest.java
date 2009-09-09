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
import java.util.Enumeration;
import java.util.Vector;

import org.objectweb.joram.shared.stream.StreamUtil;

public final class CommitRequest extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * List of ProducerMessages
   */
  private Vector producerMessages;
  
  /**
   * List of SessAckRequest
   */
  private Vector ackRequests;
  
  /**
   * Indicates whether the produced messages
   * are asynchronously send or not
   * (without or with an acknowledgement).
   */
  private boolean asyncSend = false;

  protected int getClassId() {
    return COMMIT_REQUEST;
  }
  
  public CommitRequest() {}
  
  public void addProducerMessages(ProducerMessages pm) {
    if (producerMessages == null) producerMessages = new Vector();
    producerMessages.addElement(pm);
  }
  
  public void addAckRequest(SessAckRequest sar) {
    if (ackRequests == null) ackRequests = new Vector();
    ackRequests.addElement(sar);
  }
  
  public Enumeration getProducerMessages() {
    if (producerMessages != null) {
      return producerMessages.elements();
    } else {
      return null;
    }
  }
  
  public Enumeration getAckRequests() {
    if (ackRequests != null) {
      return ackRequests.elements();
    } else {
      return null;
    }
  }
  
  public void setAsyncSend(boolean b) {
    asyncSend = b;
  }
  
  public final boolean getAsyncSend() {
    return asyncSend;
  }
  
  public void toString(StringBuffer strbuf) {
    super.toString(strbuf);
    strbuf.append(",producerMessages=").append(producerMessages);
    strbuf.append(",ackRequests=").append(ackRequests);
    strbuf.append(",asyncSend=").append(asyncSend);
    strbuf.append(')');
  }

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);

    // Serialize the producerMessages Vector
    if (producerMessages == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = producerMessages.size();
      StreamUtil.writeTo(size, os);
      for (int i=0; i<size; i++) {
        ProducerMessages pm = (ProducerMessages) producerMessages.elementAt(i);
        pm.writeTo(os);
      }
    }

    //  Serialize the ackRequests Vector
    if (ackRequests == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = ackRequests.size();
      StreamUtil.writeTo(size, os);
      for (int i=0; i<size; i++) {
        SessAckRequest sar = (SessAckRequest) ackRequests.elementAt(i);
        sar.writeTo(os);
      }
    }

    StreamUtil.writeTo(asyncSend, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);

    // Gets the producerMessages Vector
    int size = StreamUtil.readIntFrom(is);
    if (size == -1) {
      producerMessages = null;
    } else {
      producerMessages = new Vector(size);
      for (int i=0; i<size; i++) {
        ProducerMessages pm = new ProducerMessages();
        pm.readFrom(is);
        producerMessages.addElement(pm);
      }
    }

    //  Gets the ackRequests Vector
    size = StreamUtil.readIntFrom(is);
    if (size == -1) {
      ackRequests = null;
    } else {
      ackRequests = new Vector(size);
      for (int i=0; i<size; i++) {
        SessAckRequest sar = new SessAckRequest();
        sar.readFrom(is);
        ackRequests.addElement(sar);
      }
    }

    asyncSend = StreamUtil.readBooleanFrom(is);
  }
}
