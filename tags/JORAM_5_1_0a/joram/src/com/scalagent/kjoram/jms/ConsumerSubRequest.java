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
 * A <code>ConsumerSubRequest</code> is sent by a constructing
 * <code>MessageConsumer</code> destinated to consume messages on a topic.
 */
public class ConsumerSubRequest extends AbstractJmsRequest
{
  /** The subscription's name. */
  private String subName;
  /** The selector for filtering messages. */
  private String selector;
  /**
   * <code>true</code> if the subscriber does not wish to consume messages
   * produced by its connection.
   */
  private boolean noLocal;
  /** <code>true</code> if the subscription is durable. */
  private boolean durable;

  /**
   * Constructs a <code>ConsumerSubRequest</code>.
   *
   * @param topic  The topic identifier the client wishes to subscribe to.
   * @param subName  The subscription's name.
   * @param selector  The selector for filtering messages, if any.
   * @param noLocal  <code>true</code> for not consuming the local messages.
   * @param durable  <code>true</code> for a durable subscription.
   */
  public ConsumerSubRequest(String topic, String subName, String selector,
                            boolean noLocal, boolean durable)
  {
    super(topic);
    this.subName = subName;
    this.selector = selector;
    this.noLocal = noLocal;
    this.durable = durable;
  }

  /**
   * Constructs a <code>ConsumerSubRequest</code>.
   */
  public ConsumerSubRequest()
  {}

  /** Sets the subscription name. */
  public void setSubName(String subName)
  {
    this.subName = subName;
  }

  /** Sets the selector. */
  public void setSelector(String selector)
  {
    this.selector = selector;
  }

  /** Sets the noLocal attribute. */
  public void setNoLocal(boolean noLocal)
  {
    this.noLocal = noLocal;
  }

  /** Sets the durable attribute. */
  public void setDurable(boolean durable)
  {
    this.durable = durable;
  }

  /** Returns the name of the subscription. */
  public String getSubName()
  {
    return subName;
  }

  /** Returns the selector for filtering the messages. */
  public String getSelector()
  {
    return selector;
  }

  /** Returns <code>true</code> for not consuming the local messages. */
  public boolean getNoLocal()
  {
    return noLocal;
  }

  /** Returns <code>true</code> for a durable subscription. */
  public boolean getDurable()
  {
    return durable;
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    if (subName != null)
      h.put("subName",subName);
    if (selector != null)
      h.put("selector",selector);
    h.put("noLocal",new Boolean(noLocal));
    h.put("durable",new Boolean(durable));
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    ConsumerSubRequest req = new ConsumerSubRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    req.setSubName((String) h.get("subName"));
    req.setSelector((String) h.get("selector"));
    req.setNoLocal(((Boolean) h.get("noLocal")).booleanValue());
    req.setDurable(((Boolean) h.get("durable")).booleanValue());
    return req;
  }
}
