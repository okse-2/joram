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

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * A <code>ConsumerDenyRequest</code> instance is used by a
 * <code>MessageConsumer</code> for denying a received message.
 */
public class ConsumerDenyRequest extends AbstractJmsRequest
{
  /** Message identifier. */
  private String id;
  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;
  /** <code>true</code> if the request must not be acked by the server. */
  private boolean doNotAck = false;

  /**
   * Constructs a <code>ConsumerDenyRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param id  The message identifier.
   * @param queueMode  <code>true</code> if this request is destinated to
   *          a queue.
   */
  public ConsumerDenyRequest(String targetName, String id, boolean queueMode)
  {
    super(targetName);
    this.id = id;
    this.queueMode = queueMode;
  }

  /**
   * Constructs a <code>ConsumerDenyRequest</code> instance.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param id  The message identifier.
   * @param queueMode  <code>true</code> if this request is destinated to
   *          a queue.
   * @param doNotAck  <code>true</code> if this request must not be acked by
   *          the server.
   */
  public ConsumerDenyRequest(String targetName, String id, boolean queueMode,
                             boolean doNotAck)
  {
    super(targetName);
    this.id = id;
    this.queueMode = queueMode;
    this.doNotAck = doNotAck;
  }

  /**
   * Constructs a <code>ConsumerDenyRequest</code> instance.
   */
  public ConsumerDenyRequest()
  {}

  /** Sets the denied message identifier. */
  public void setId(String id)
  {
    this.id = id;
  }

  /** Sets the target destination type. */
  public void setQueueMode(boolean queueMode)
  {
    this.queueMode = queueMode;
  }

  /** Sets the server ack policy. */
  public void setDoNotAck(boolean doNotAck)
  {
    this.doNotAck = doNotAck;
  }

  /** Returns the denied message identifier. */
  public String getId()
  {
    return id;
  }

  /** Returns <code>true</code> if the request is destinated to a queue. */
  public boolean getQueueMode()
  {
    return queueMode;
  }

  /**
   * Returns <code>true</code> if the request must not be acked by the 
   * server.
   */
  public boolean getDoNotAck()
  {
    return doNotAck;
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    if (id != null)
      h.put("id",id);
    h.put("queueMode",new Boolean(queueMode));
    h.put("doNotAck",new Boolean(doNotAck));
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    ConsumerDenyRequest req = new ConsumerDenyRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    req.setId((String) h.get("id"));
    req.setQueueMode(((Boolean) h.get("queueMode")).booleanValue());
    req.setDoNotAck(((Boolean) h.get("doNotAck")).booleanValue());
    return req;
  }
}
