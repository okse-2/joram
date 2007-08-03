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

import fr.dyade.aaa.util.Strings;

/**
 * A <code>ConsumerSetListRequest</code> is sent by a
 * <code>MessageConsumer</code> on which a message listener is set.
 */
public class ConsumerSetListRequest extends AbstractJmsRequest
{
  /** Selector for filtering messages on a queue. */
  private String selector;

  /** <code>true</code> if the request is destinated to a queue. */
  private boolean queueMode;
  
  private String[] msgIdsToAck;

  private int msgCount;

  /**
   * Constructs a <code>ConsumerSetListRequest</code>.
   *
   * @param targetName  Name of the target queue or subscription.
   * @param selector  Selector for filtering messages.
   * @param queueMode  <code>true</code> if this request is destinated to a
   *          queue.
   */
  public ConsumerSetListRequest(String targetName, 
                                String selector, 
                                boolean queueMode,
                                String[] msgIdsToAck,
                                int msgCount)
  {
    super(targetName);
    this.selector = selector;
    this.queueMode = queueMode;
    this.msgIdsToAck = msgIdsToAck;
    this.msgCount = msgCount;
  }

  /**
   * Constructs a <code>ConsumerSetListRequest</code>.
   */
  public ConsumerSetListRequest()
  {}

  /** Sets the selector. */
  public void setSelector(String selector)
  {
    this.selector = selector;
  }

  /** Sets the target destination type. */
  public void setQueueMode(boolean queueMode)
  {
    this.queueMode = queueMode;
  }

  /** Returns the selector for filtering messages. */
  public String getSelector()
  {
    return selector;
  }

  /** Returns <code>true</code> if the request is destinated to a queue. */
  public boolean getQueueMode()
  {
    return queueMode;
  }

  public final String[] getMessageIdsToAck() {
    return msgIdsToAck;
  }

  public final int getMessageCount() {
    return msgCount;
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    if (selector != null)
      h.put("selector",selector);
    h.put("queueMode",new Boolean(queueMode));
    if (msgIdsToAck != null)
      h.put("msgIdsToAck", msgIdsToAck);
    h.put("msgCount", new Integer(msgCount));
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    ConsumerSetListRequest req = new ConsumerSetListRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    req.setSelector((String) h.get("selector"));
    req.setQueueMode(((Boolean) h.get("queueMode")).booleanValue());
    req.msgIdsToAck = (String[]) h.get("msgIdsToAck");
    req.msgCount = ((Integer) h.get("msgCount")).intValue();
    return req;
  }

  public String toString() {
    return '(' + super.toString() +
      ",selector=" + selector + 
      ",queueMode=" + queueMode +
      ",msgIdsToAck=" + Strings.toString(msgIdsToAck) + 
      ",msgCount=" + msgCount + ')';
  }
}
