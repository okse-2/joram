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

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>XACnxRecoverReply</code> replies to a
 * <code>XACnxRecoverRequest</code> and carries transaction identifiers.
 */
public final class XACnxRecoverReply extends AbstractJmsReply {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Branch qualifiers. */
  private Vector bqs;

  public void setBQS(Vector bqs) {
    this.bqs = bqs;
  }

  /** Format identifiers. */
  private Vector fis;

  public void setFIS(Vector fis) {
    this.fis = fis;
  }

  /** Global transaction identifiers. */
  private Vector gtis;

  public void setGTIS(Vector gtis) {
    this.gtis = gtis;
  }

  protected int getClassId() {
    return XA_CNX_RECOVER_REPLY;
  }

  /**
   * Constructs a <code>XACnxRecoverReply</code> instance. 
   *
   * @param req   The replied request.
   * @param bqs   Branch qualifiers.
   * @param fis   Format identifiers.
   * @param gtis  Global transaction identifiers.
   */
  public XACnxRecoverReply(XACnxRecoverRequest req,
                           Vector bqs,
                           Vector fis,
                           Vector gtis) {
    super(req.getRequestId());
    this.bqs = bqs;
    this.fis = fis;
    this.gtis = gtis;
  }

  /**
   * Constructs a <code>XACnxRecoverReply</code> instance. 
   */
  public XACnxRecoverReply() {}

  /** Returns the number of transaction identifiers. */
  public int getSize() {
    return bqs.size();
  }

  /** Returns a branch qualifier. */
  public byte[] getBranchQualifier(int index) {
    return (byte[]) bqs.get(index);
  }

  /** Returns a format identifier. */
  public int getFormatId(int index) {
    return ((Integer) fis.get(index)).intValue();
  }

  /** Returns a global transaction identifier. */
  public byte[] getGlobalTransactionId(int index) {
    return (byte[]) gtis.get(index);
  }

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  private static void writeVectorOfByteArrayTo(Vector v, OutputStream os) throws IOException {
    if (v == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = v.size();
      StreamUtil.writeTo(size, os);
      for (int i=0; i<size; i++) {
        StreamUtil.writeTo((byte []) v.elementAt(i), os);
      }
    }
  }

  private static Vector readVectorOfByteArrayFrom(InputStream is) throws IOException {
    int size = StreamUtil.readIntFrom(is);
    if (size == -1) return null;

    Vector v = new Vector(size);
    for (int i=0; i<size; i++) {
      v.addElement(StreamUtil.readByteArrayFrom(is));
    }
    return v;
  }

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    writeVectorOfByteArrayTo(bqs, os);
    if (fis == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = fis.size();
      StreamUtil.writeTo(size, os);
      for (int i=0; i<size; i++) {
        StreamUtil.writeTo(((Integer) fis.elementAt(i)).intValue(), os);
      }
    }
    writeVectorOfByteArrayTo(gtis, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    bqs = readVectorOfByteArrayFrom(is);
    int size = StreamUtil.readIntFrom(is);
    if (size == -1) {
      fis = null;
    } else {
      fis = new Vector(size);
      for (int i=0; i<size; i++) {
        fis.addElement(new Integer(StreamUtil.readIntFrom(is)));
      }
    }
    gtis = readVectorOfByteArrayFrom(is);
  }
}
