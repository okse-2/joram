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
package org.objectweb.joram.shared.client;

import java.util.*;

/**
 * An <code>XASessRollback</code> instance is used by an <code>XASession</code>
 * for rolling back the operations performed during a transaction.
 */
public class XASessRollback extends AbstractJmsRequest
{
  /** Identifier of the resource and the rolling back transaction. */
  private String id;
  /** Table holding the identifiers of the messages to deny on queues. */
  private Hashtable qDenyings = null;
  /** Table holding the identifiers of the messages to deny on subs. */
  private Hashtable subDenyings = null;


  /**
   * Constructs an <code>XASessRollback</code> instance.
   *
   * @param id  Identifier of the resource and the rolling back transaction.
   */
  public XASessRollback(String id)
  {
    super(null);
    this.id = id;
  }

  /**
   * Constructs an <code>XASessRollback</code> instance.
   */
  public XASessRollback() {}
 
  /**
   * Adds a vector of denied messages' identifiers.
   *
   * @param target  Name of the queue or of the subscription where denying the
   *          messages.
   * @param ids  Vector of message identifiers.
   * @param queueMode  <code>true</code> if the messages have to be denied on
   *          a queue.
   */
  public void add(String target, Vector ids, boolean queueMode)
  {
    if (queueMode) {
      if (qDenyings == null)
        qDenyings = new Hashtable();
      qDenyings.put(target, ids);
    }
    else {
      if (subDenyings == null)
        subDenyings = new Hashtable();
      subDenyings.put(target, ids);
    }
  }

  /** Sets the identifier. */
  public void setId(String id)
  {
    this.id = id;
  }
  
  
  /** Returns the id of the resource and the rolling back transaction. */
  public String getId()
  {
    return id;
  }

  /** Returns the queues enumeration. */
  public Enumeration getQueues()
  {
    if (qDenyings == null)
      return (new Hashtable()).keys();
    return qDenyings.keys();
  }

  /** Returns the vector of msg identifiers for a given queue. */
  public Vector getQueueIds(String queue)
  {
    if (qDenyings == null)
      return null;
    return (Vector) qDenyings.get(queue);
  }

  /** Returns the subscriptions enumeration. */
  public Enumeration getSubs()
  {
    if (subDenyings == null)
      return (new Hashtable()).keys();
    return subDenyings.keys();
  }
  
  /** Sets the queue denyings table. */
  public void setQDenyings(Hashtable qDenyings)
  {
    this.qDenyings = qDenyings;
  }

  /** Sets the sub denyings table. */
  public void setSubDenyings(Hashtable subDenyings)
  {
    this.subDenyings = subDenyings;
  }

  /** Returns the vector of msg identifiers for a given subscription. */
  public Vector getSubIds(String sub)
  {
    if (subDenyings == null)
      return null;
    return (Vector) subDenyings.get(sub);
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    if (id != null)
      h.put("id",id);
    if (qDenyings != null)
      h.put("qDenyings",qDenyings);
    if (subDenyings != null)
      h.put("subDenyings",subDenyings);
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    XASessRollback req = new XASessRollback();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    req.setId((String) h.get("id"));
    req.setQDenyings((Hashtable) h.get("qDenyings"));
    req.setSubDenyings((Hashtable) h.get("subDenyings"));
    return req;
  }
}
