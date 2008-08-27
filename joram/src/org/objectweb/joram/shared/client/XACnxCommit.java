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

import org.objectweb.joram.shared.stream.StreamUtil;

/**
 * An <code>XACnxCommit</code> instance is used by an <code>XAConnection</code>
 * for commiting the messages and acknowledgements it sent to the proxy.
 */
public final class XACnxCommit extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
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

  protected int getClassId() {
    return XA_CNX_COMMIT;
  }

  /**
   * Constructs an <code>XACnxCommit</code> instance.
   *
   * @param bq        Transaction branch qualifier.
   * @param fi        Transaction identifier format.
   * @param gti       Global transaction identifier.
   */
  public XACnxCommit(byte[] bq, int fi, byte[] gti) {
    super();
    this.bq = bq;
    this.fi = fi;
    this.gti = gti;
  }

  /**
   * Constructs an <code>XACnxCommit</code> instance.
   */
  public XACnxCommit() {}

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
  }
}
