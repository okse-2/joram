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
 * A <code>ConsumerUnsubRequest</code> is sent by a closing temporary
 * <code>MessageConsumer</code> on a topic, or by a <code>Session</code>
 * unsubscribing a durable subscriber.
 */
public class ConsumerUnsubRequest extends AbstractJmsRequest
{
  /**
   * Constructs a <code>ConsumerUnsubRequest</code>.
   *
   * @param subName  The name of the subscription to delete.
   */
  public ConsumerUnsubRequest(String subName)
  {
    super(subName);
  }

  /**
   * Constructs a <code>ConsumerUnsubRequest</code>.
   */
  public ConsumerUnsubRequest()
  {}

  public Hashtable soapCode() {
    return super.soapCode();
  }

  public static Object soapDecode(Hashtable h) {
    ConsumerUnsubRequest req = new ConsumerUnsubRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    return req;
  }
}
