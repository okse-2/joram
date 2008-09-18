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
 * An <code>AbstractJmsReply</code> is sent by a proxy to a Joram client as a 
 * reply to an <code>AbstractJmsRequest</code>.
 */
public class AbstractJmsReply
{
  /** Identifier of the replied request. */
  protected int correlationId = -1;

  /**
   * Constructs an <code>AbstractJmsReply</code>.
   *
   * @param correlationId  Identifier of the replied request.
   */
  public AbstractJmsReply(int correlationId)
  {
    this.correlationId = correlationId;
  }

  /**
   * Constructs an <code>AbstractJmsReply</code>.
   */
  public AbstractJmsReply()
  {}


  /** Sets the replied request identifier. */
  public void setCorrelationId(int correlationId)
  {
    this.correlationId = correlationId;
  }

  /** Returns the replied request identifier. */
  public int getCorrelationId()
  {
    return correlationId;
  }

  /** Returns the identifier as an hashtable key. */
  public Integer getKey()
  {
    return new Integer(correlationId);
  }

  public Hashtable soapCode() {
    Hashtable h = new Hashtable();
    String className = getClass().getName();
    String end = className.substring(
      className.lastIndexOf((int)'.'),
      className.length());
    h.put("className","org.objectweb.joram.shared.client"+end);
    //h.put("className",getClass().getName());
    h.put("correlationId",getKey());
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    return new AbstractJmsReply(
      ((Integer) h.get("correlationId")).intValue());
  }
}
