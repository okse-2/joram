/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
package fr.dyade.aaa.mom.jms;

import java.util.*;

/**
 * An <code>XASessPrepare</code> instance is used by an <code>XASession</code>
 * for sending messages and acknowledgements to the proxy.
 */
public class XASessPrepare extends AbstractJmsRequest
{
  /** Identifier of the resource and the preparing transaction. */
  private String id;
  /** Vector of <code>ProducerMessages</code> instances. */
  private Vector sendings;
  /** Vector of <code>SessAckRequest</code> instances. */
  private Vector acks;
  

  /**
   * Constructs an <code>XASessPrepare</code> instance.
   *
   * @param id  Identifier of the resource and the preparing transaction.
   * @param sendings  Vector of <code>ProducerMessages</code> instances.
   * @param acks  Vector of <code>SessAckRequest</code> instances.
   */
  public XASessPrepare(String id, Vector sendings, Vector acks)
  {
    super();
    this.id = id;
    this.sendings = sendings;
    this.acks = acks;
  }

  public XASessPrepare() {
    super(null);
    sendings = new Vector();
    acks = new Vector();
  }

  /** Returns the identifier of the resource and the commiting transaction. */
  public String getId()
  {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /** Returns the vector of <code>ProducerMessages</code> instances. */
  public Vector getSendings()
  {
    return sendings;
  }

  /** Returns the vector of <code>SessAckRequest</code> instances. */
  public Vector getAcks()
  {
    return acks;
  }

  public void addProducerMessages(ProducerMessages pm) {
    sendings.addElement(pm);
  }

  public void addSessAckRequest(SessAckRequest sar) {
    acks.addElement(sar);
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    if (id != null)
      h.put("id",id);
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
    XASessPrepare req = new XASessPrepare();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    req.setId((String) h.get("id"));
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
