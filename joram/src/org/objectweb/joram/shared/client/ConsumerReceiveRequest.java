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

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * A <code>ConsumerReceiveRequest</code> is sent by a
 * <code>MessageConsumer</code> when requesting a message.
 */
public class ConsumerReceiveRequest extends AbstractJmsRequest
{
  /** The selector for filtering messages on a queue. */
  private String selector;
  /** The time to live value of the request (negative for infinite). */
  private long timeToLive;
  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;

  private boolean receiveAck;

  /**
   * Constructs a <code>ConsumerReceiveRequest</code>.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param selector  The selector for filtering messages, if any.
   * @param timeToLive  Time to live value in milliseconds, negative for
   *          infinite.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  public ConsumerReceiveRequest(String targetName, String selector,
                                long timeToLive, boolean queueMode)
  {
    super(targetName);
    this.selector = selector;
    this.timeToLive = timeToLive;
    this.queueMode = queueMode;
    receiveAck = false;
  }

  /**
   * Constructs a <code>ConsumerReceiveRequest</code>.
   */
  public ConsumerReceiveRequest()
  {}

  public void setReceiveAck(boolean receiveAck) {
    this.receiveAck = receiveAck;
  }

  public final boolean getReceiveAck() {
    return receiveAck;
  }

  /** Sets the selector. */
  public void setSelector(String selector)
  {
    this.selector = selector;
  }

  /** Sets the time to live value. */
  public void setTimeToLive(long timeToLive)
  {
    this.timeToLive = timeToLive;
  }

  /** Sets the target destination type. */
  public void setQueueMode(boolean queueMode)
  {
    this.queueMode = queueMode;
  }

  /** Returns the selector for filtering the messages. */
  public String getSelector()
  {
    return selector;
  }

  /** Returns the time to live value in milliseconds. */
  public long getTimeToLive()
  {
    return timeToLive;
  }

  /** Returns <code>true</code> if the request is destinated to a queue. */
  public boolean getQueueMode()
  {
    return queueMode;
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    if (selector != null)
      h.put("selector",selector);
    h.put("timeToLive",new Long(timeToLive));
    h.put("queueMode",new Boolean(queueMode));
    h.put("receiveAck",new Boolean(receiveAck));
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    ConsumerReceiveRequest req = new ConsumerReceiveRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    req.setSelector((String) h.get("selector"));
    req.setQueueMode(((Boolean) h.get("queueMode")).booleanValue());
    req.setTimeToLive(((Long) h.get("timeToLive")).longValue());
    req.receiveAck = ((Boolean) h.get("receiveAck")).booleanValue();
    return req;
  }
}
