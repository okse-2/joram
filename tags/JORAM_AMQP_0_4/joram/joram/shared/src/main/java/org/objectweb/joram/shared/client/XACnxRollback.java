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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * An <code>XACnxRollback</code> instance is used by an
 * <code>XAConnection</code> for rolling back the operations performed
 * during a transaction.
 */
public final class XACnxRollback extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Transaction branch qualifier. */
  private byte[] bq;

  public void setBQ(byte[] bq) {
    this.bq = bq;
  }
  
  /** Returns the transaction branch qualifier. */
  public byte[] getBQ() {
    return bq;
  }

  /** Transaction identifier format. */
  private int fi;

  public void setFI(int fi) {
    this.fi = fi;
  }

  /** Returns the transaction identifier format. */
  public int getFI() {
    return fi;
  }

  /** Global transaction identifier. */
  private byte[] gti;

  public void setGTI(byte[] gti) {
    this.gti = gti;
  }

  /** Returns the global transaction identifier. */
  public byte[] getGTI() {
    return gti;
  }

  /** Table holding the identifiers of the messages to deny on queues. */
  private Hashtable qDenyings = null;
  /** Table holding the identifiers of the messages to deny on subs. */
  private Hashtable subDenyings = null;

  /**
   * Adds a vector of denied messages' identifiers.
   *
   * @param target  Name of the queue or of the subscription where denying the
   *          messages.
   * @param ids  Vector of message identifiers.
   * @param queueMode  <code>true</code> if the messages have to be denied on
   *          a queue.
   */
  public void add(String target, Vector ids, boolean queueMode) {
    if (queueMode) {
      if (qDenyings == null)
        qDenyings = new Hashtable();
      qDenyings.put(target, ids);
    } else {
      if (subDenyings == null)
        subDenyings = new Hashtable();
      subDenyings.put(target, ids);
    }
  }


  /** Returns the queues enumeration. */
  public Enumeration getQueues() {
    if (qDenyings == null)
      return (new Hashtable()).keys();
    return qDenyings.keys();
  }

  /** Returns the vector of msg identifiers for a given queue. */
  public Vector getQueueIds(String queue) {
    if (qDenyings == null)
      return null;
    return (Vector) qDenyings.get(queue);
  }

  /** Returns the subscriptions enumeration. */
  public Enumeration getSubs() {
    if (subDenyings == null)
      return (new Hashtable()).keys();
    return subDenyings.keys();
  }
  
  /** Sets the queue denyings table. */
  public void setQDenyings(Hashtable qDenyings) {
    this.qDenyings = qDenyings;
  }

  /** Sets the sub denyings table. */
  public void setSubDenyings(Hashtable subDenyings) {
    this.subDenyings = subDenyings;
  }

  /** Returns the vector of msg identifiers for a given subscription. */
  public Vector getSubIds(String sub) {
    if (subDenyings == null)
      return null;
    return (Vector) subDenyings.get(sub);
  }

  protected int getClassId() {
    return XA_CNX_ROLLBACK;
  }

  /**
   * Constructs an <code>XACnxRollback</code> instance.
   *
   * @param bq        Transaction branch qualifier.
   * @param fi        Transaction identifier format.
   * @param gti       Global transaction identifier.
   */
  public XACnxRollback(byte[] bq, int fi, byte[] gti) {
    super(null);
    this.bq = bq;
    this.fi = fi;
    this.gti = gti;
  }

  /**
   * Constructs an <code>XACnxRollback</code> instance.
   */
  public XACnxRollback() {}

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
    if (qDenyings == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = qDenyings.size();
      StreamUtil.writeTo(size, os);
      for (Enumeration keys = qDenyings.keys(); keys.hasMoreElements(); ) {
        String key = (String) keys.nextElement();
        StreamUtil.writeTo(key, os);
        Vector ids = (Vector) qDenyings.get(key);
        StreamUtil.writeListOfStringTo(ids, os);
      }
    }
    if (subDenyings == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = subDenyings.size();
      StreamUtil.writeTo(size, os);
      for (Enumeration keys = subDenyings.keys(); keys.hasMoreElements(); ) {
        String key = (String) keys.nextElement();
        StreamUtil.writeTo(key, os);
        Vector ids = (Vector) subDenyings.get(key);
        StreamUtil.writeListOfStringTo(ids, os);
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
      qDenyings = null;
    } else {
      qDenyings = new Hashtable(size*4/3);
      for (int i=0; i<size; i++) {
        String target = StreamUtil.readStringFrom(is);
        Vector ids = StreamUtil.readVectorOfStringFrom(is);
        qDenyings.put(target, ids);
      }
    }
    size = StreamUtil.readIntFrom(is);
    if (size == -1) {
      subDenyings = null;
    } else {
      subDenyings = new Hashtable(size*4/3);
      for (int i=0; i<size; i++) {
        String target = StreamUtil.readStringFrom(is);
        Vector ids = StreamUtil.readVectorOfStringFrom(is);
        subDenyings.put(target, ids);
      }
    }
  }
}
