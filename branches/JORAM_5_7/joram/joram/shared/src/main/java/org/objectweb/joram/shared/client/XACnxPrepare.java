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
package org.objectweb.joram.shared.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Vector;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * An <code>XACnxPrepare</code> instance is used by an
 * <code>XAConnection</code> for sending messages and acknowledgements to
 * the proxy.
 */
public final class XACnxPrepare extends AbstractJmsRequest {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Transaction branch qualifier. */
  private byte[] bq;

  /** Returns the transaction branch qualifier. */
  public byte[] getBQ() {
    return bq;
  }

  public void setBQ(byte[] bq) {
    this.bq = bq;
  }

  /** Transaction identifier format. */
  private int fi;

  /** Returns the transaction identifier format. */
  public int getFI() {
    return fi;
  }

  public void setFI(int fi) {
    this.fi = fi;
  }

  /** Global transaction identifier. */
  private byte[] gti;

  /** Returns the global transaction identifier. */
  public byte[] getGTI() {
    return gti;
  }

  public void setGTI(byte[] gti) {
    this.gti = gti;
  }

  /** Vector of <code>ProducerMessages</code> instances. */
  private Vector sendings;

  /** Returns the vector of <code>ProducerMessages</code> instances. */
  public Vector getSendings() {
    if (sendings == null)
      sendings = new Vector();
    return sendings;
  }

  public void addProducerMessages(ProducerMessages pm) {
    sendings.addElement(pm);
  }

  /** Vector of <code>SessAckRequest</code> instances. */
  private Vector acks;

  /** Returns the vector of <code>SessAckRequest</code> instances. */
  public Vector getAcks() {
    if (acks == null)
      acks = new Vector();
    return acks;
  }

  public void addSessAckRequest(SessAckRequest sar) {
    acks.addElement(sar);
  }

  protected int getClassId() {
    return XA_CNX_PREPARE;
  }

  /**
   * Constructs an <code>XACnxPrepare</code> instance.
   *
   * @param bq        Transaction branch qualifier.
   * @param fi        Transaction identifier format.
   * @param gti       Global transaction identifier.
   * @param sendings  Vector of <code>ProducerMessages</code> instances.
   * @param acks      Vector of <code>SessAckRequest</code> instances.
   */
  public XACnxPrepare(byte[] bq,
                       int fi, 
                       byte[] gti,
                       Vector sendings,
                       Vector acks) {
    super();
    this.bq = bq;
    this.fi = fi;
    this.gti = gti;
    this.sendings = sendings;
    this.acks = acks;
  }

  public XACnxPrepare() {
    super(null);
    sendings = new Vector();
    acks = new Vector();
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
    StreamUtil.writeTo(bq, os);
    StreamUtil.writeTo(fi, os);
    StreamUtil.writeTo(gti, os);
    if (sendings == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = sendings.size();
      StreamUtil.writeTo(size, os);
      for (int i=0; i<size; i++) {
        ((ProducerMessages) sendings.elementAt(i)).writeTo(os);
      }
    }
    if (acks == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = acks.size();
      StreamUtil.writeTo(size, os);
      for (int i=0; i<size; i++) {
        ((SessAckRequest) acks.elementAt(i)).writeTo(os);
      }
    }
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    bq = StreamUtil.readByteArrayFrom(is);
    fi = StreamUtil.readIntFrom(is);
    gti = StreamUtil.readByteArrayFrom(is);
    int size = StreamUtil.readIntFrom(is);
    if (size == -1) {
      sendings = null;
    } else {
      sendings = new Vector(size);
      for (int i=0; i<size; i++) {
        ProducerMessages pm = new ProducerMessages();
        pm.readFrom(is);
        sendings.addElement(pm);
      }
    }
    size = StreamUtil.readIntFrom(is);
    if (size == -1) {
      acks = null;
    } else {
      acks = new Vector(size);
      for (int i=0; i<size; i++) {
        SessAckRequest ack = new SessAckRequest();
        ack.readFrom(is);
        acks.addElement(ack);
      }
    }
  }
}
