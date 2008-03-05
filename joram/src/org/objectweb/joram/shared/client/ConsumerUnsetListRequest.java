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
 * A <code>ConsumerUnsetListRequest</code> is sent by a
 * <code>MessageConsumer</code> which listener is unset.
 */
public class ConsumerUnsetListRequest extends AbstractJmsRequest
{
  /** <code>true</code> if the listener is listening to a queue. */
  private boolean queueMode;
  /**
   * Identifier of the last listener request, cancelled by this
   * request, queue mode only.
   */
  private int cancelledRequestId = -1;


  /**
   * Constructs a <code>ConsumerUnsetListRequest</code>.
   *
   * @param queueMode  <code>true</code> if the listener is listening to a
   *          queue.
   */
  public ConsumerUnsetListRequest(boolean queueMode)
  {
    this.queueMode = queueMode;
  }

  /**
   * Constructs a <code>ConsumerUnsetListRequest</code>.
   */
  public ConsumerUnsetListRequest()
  {}

  
  /** Sets the listener mode (queue or topic listener). */
  public void setQueueMode(boolean queueMode)
  {
    this.queueMode = queueMode;
  }

  /**
   * Sets the identifier of the last listener request, cancelled by this
   * request, queue mode only.
   */
  public void setCancelledRequestId(int cancelledRequestId)
  {
    this.cancelledRequestId = cancelledRequestId;
  }

  /** Returns <code>true</code> for a queue listener. */
  public boolean getQueueMode()
  {
    return queueMode;
  }

  /**
   * Returns the identifier of the last listener request, cancelled by this
   * request, queue mode only.
   */
  public int getCancelledRequestId()
  {
    return cancelledRequestId;
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    h.put("cancelledRequestId",new Integer(cancelledRequestId));
    h.put("queueMode",new Boolean(queueMode));
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    ConsumerUnsetListRequest req = new ConsumerUnsetListRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    req.setCancelledRequestId(
      ((Integer) h.get("cancelledRequestId")).intValue());
    req.setQueueMode(((Boolean) h.get("queueMode")).booleanValue());
    return req;
  }

  public String toString() {
    return '(' + super.toString() + 
      ",queueMode=" + queueMode + 
      ",cancelledRequestId=" + cancelledRequestId + ')';
  }
}
