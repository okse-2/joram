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
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram.jms;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A <code>SessAckRequest</code> instance is used by a <code>Session</code>
 * for acknowledging the messages it consumed.
 */
public class SessAckRequest extends AbstractJmsRequest
{
  /** Vector of message identifiers. */
  private Vector ids;
  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;

  /**
   * Constructs a <code>SessAckRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param ids  Vector of acknowledged message identifiers.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  public SessAckRequest(String targetName, Vector ids, boolean queueMode)
  {
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

  /** Sets the vector of identifiers. */
  public void setIds(Vector ids)
  {
    this.ids = ids;
  }

  public void addId(String id) {
    ids.addElement(id);
  }

  /** Sets the target destination type. */
  public void setQueueMode(boolean queueMode)
  {
    this.queueMode = queueMode;
  }

  /** Returns the vector of acknowledged messages identifiers. */
  public Vector getIds()
  {
    return ids;
  }

  /** Returns <code>true</code> if the request is destinated to a queue. */
  public boolean getQueueMode()
  {
    return queueMode;
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    h.put("queueMode",new Boolean(queueMode));
    // Coding and adding the messages into a array:
    int size = ids.size();
    if (size > 0) {
      Vector arrayId = new Vector();
      for (int i = 0; i<size; i++) {
        arrayId.insertElementAt((String) ids.elementAt(0),i);
        ids.removeElementAt(0);
      }
      if (arrayId != null)
        h.put("arrayId",arrayId);
    }
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    SessAckRequest req = new SessAckRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    req.setQueueMode(((Boolean) h.get("queueMode")).booleanValue());
    Vector arrayId = (Vector) h.get("arrayId");
    if (arrayId != null) {
      for (int i = 0; i<arrayId.size(); i++)
        req.addId((String) arrayId.elementAt(i));
    }
    return req;
  }
}
