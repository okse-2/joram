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
 * A <code>ConsumerCloseSubRequest</code> is sent by a closing durable
 * <code>TopicSubscriber</code>.
 */
public class ConsumerCloseSubRequest extends AbstractJmsRequest
{
  /**
   * Constructs a <code>ConsumerCloseSubRequest</code>.
   *
   * @param subName  The name of the closing durable subscription.
   */
  public ConsumerCloseSubRequest(String subName)
  {
    super(subName);
  }

  /**
   * Constructs a <code>ConsumerCloseSubRequest</code>.
   */
  public ConsumerCloseSubRequest()
  {}

  public Hashtable soapCode() {
    return super.soapCode();
  }

  public static Object soapDecode(Hashtable h) {
    ConsumerCloseSubRequest req = new ConsumerCloseSubRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    return req;
  }
}
