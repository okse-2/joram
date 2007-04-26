/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s):
 */
package org.objectweb.joram.shared.client;

import java.util.*;

/**
 * An <code>XACnxPrepare</code> instance is used by an
 * <code>XAConnection</code> for sending messages and acknowledgements to
 * the proxy.
 */
public class XACnxPrepare extends AbstractJmsRequest
{
  /** Transaction branch qualifier. */
  private byte[] bq;
  /** Transaction identifier format. */
  private int fi;
  /** Global transaction identifier. */
  private byte[] gti;

  /** Vector of <code>ProducerMessages</code> instances. */
  private Vector sendings;
  /** Vector of <code>SessAckRequest</code> instances. */
  private Vector acks;
  

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
                       Vector acks)
  {
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

  /** Returns the transaction branch qualifier. */
  public byte[] getBQ()
  {
    return bq;
  }

  /** Returns the transaction identifier format. */
  public int getFI()
  {
    return fi;
  }

  /** Returns the global transaction identifier. */
  public byte[] getGTI()
  {
    return gti;
  }

  /** Returns the vector of <code>ProducerMessages</code> instances. */
  public Vector getSendings()
  {
    if (sendings == null)
      sendings = new Vector();
    return sendings;
  }

  /** Returns the vector of <code>SessAckRequest</code> instances. */
  public Vector getAcks()
  {
    if (acks == null)
      acks = new Vector();
    return acks;
  }

  public void setBQ(byte[] bq)
  {
    this.bq = bq;
  }

  public void setFI(int fi)
  {
    this.fi = fi;
  }

  public void setGTI(byte[] gti)
  {
    this.gti = gti;
  }

  public void addProducerMessages(ProducerMessages pm) {
    sendings.addElement(pm);
  }

  public void addSessAckRequest(SessAckRequest sar) {
    acks.addElement(sar);
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    h.put("bq",bq);
    h.put("fi", new Integer(fi));
    h.put("gti", gti);
    int size = sendings.size();
    if (size > 0) {
      Hashtable [] arrayPM = new Hashtable[size];
      for (int i = 0; i<size; i++) {
        ProducerMessages pm = (ProducerMessages) sendings.elementAt(0);
        sendings.removeElementAt(0);
        arrayPM[i] = pm.soapCode();
      }
      if (arrayPM != null)
        h.put("arrayPM",arrayPM);
    }
    size = acks.size();
    if (size > 0) {
      Hashtable [] arraySAR = new Hashtable[size];
      for (int i = 0; i<size; i++) {
        SessAckRequest sar = (SessAckRequest) acks.elementAt(0);
        acks.removeElementAt(0);
        arraySAR[i] = sar.soapCode();
      }
      if (arraySAR != null)
        h.put("arraySAR",arraySAR);
    }
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    XACnxPrepare req = new XACnxPrepare();
    req.setBQ((byte[]) h.get("bq"));
    req.setFI(((Integer) h.get("fi")).intValue());
    req.setGTI((byte[]) h.get("gti"));
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    Map [] arrayPM = (Map []) h.get("arrayPM");
    if (arrayPM != null) {
      for (int i = 0; i<arrayPM.length; i++)
        req.addProducerMessages(
          (ProducerMessages) ProducerMessages.soapDecode((Hashtable) arrayPM[i]));
    }
    Map [] arraySAR = (Map []) h.get("arraySAR");
    if (arraySAR != null) {
      for (int i = 0; i<arraySAR.length; i++)
        req.addSessAckRequest(
          (SessAckRequest) SessAckRequest.soapDecode((Hashtable)arraySAR[i]));
    }
    return req;
  }
}
