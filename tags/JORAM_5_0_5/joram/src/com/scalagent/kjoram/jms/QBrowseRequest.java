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
 * A <code>QBrowseRequest</code> instance is sent by a 
 * <code>QueueBrowser</code> when requesting an enumeration.
 */
public class QBrowseRequest extends AbstractJmsRequest
{
  /** The selector for filtering messages. */
  private String selector;

  /**
   * Constructs a <code>QBrowseRequest</code> instance.
   *
   * @param to  Name of the queue to browse. 
   * @param selector  The selector for filtering messages, if any.
   */
  public QBrowseRequest(String to, String selector)
  {
    super(to);
    this.selector = selector;
  }

  /**
   * Constructs a <code>QBrowseRequest</code> instance.
   */
  public QBrowseRequest()
  {}

  /** Sets the selector. */
  public void setSelector(String selector)
  {
    this.selector = selector;
  }

  /** Returns the selector for filtering the messages. */
  public String getSelector()
  {
    return selector;
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    if (selector != null)
      h.put("selector",selector);
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    QBrowseRequest req = new QBrowseRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    req.setSelector((String) h.get("selector"));
    return req;
  }
}
