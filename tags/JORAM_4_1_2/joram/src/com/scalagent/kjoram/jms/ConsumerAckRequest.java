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

/**
 * A <code>ConsumerAckRequest</code> instance is used by a
 * <code>MessageConsumer</code> for acknowledging a received message.
 */
public class ConsumerAckRequest extends AbstractJmsRequest
{
  /** Message identifier. */
  private String id;
  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;

  /**
   * Constructs a <code>ConsumerAckRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param id  The message identifier.
   * @param queueMode  <code>true</code> if this request is destinated to
   *          a queue.
   */
  public ConsumerAckRequest(String targetName, String id, boolean queueMode)
  {
    super(targetName);
    this.id = id;
    this.queueMode = queueMode;
  }

  /**
   * Constructs a <code>ConsumerAckRequest</code> instance.
   */
  public ConsumerAckRequest()
  {}

  /** Sets the acknowledged message identifier. */
  public void setId(String id)
  {
    this.id = id;
  }

  /** Sets the target destination type. */
  public void setQueueMode(boolean queueMode)
  {
    this.queueMode = queueMode;
  }

  /** Returns the acknowledged message identifier. */
  public String getId()
  {
    return id;
  }

  /** Returns <code>true</code> if the request is destinated to a queue. */
  public boolean getQueueMode()
  {
    return queueMode;
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    if (id != null)
      h.put("id",id);
    h.put("queueMode",new Boolean(queueMode));
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    ConsumerAckRequest req = new ConsumerAckRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    req.setId((String) h.get("id"));
    req.setQueueMode(((Boolean) h.get("queueMode")).booleanValue());
    return req;
  }
}
