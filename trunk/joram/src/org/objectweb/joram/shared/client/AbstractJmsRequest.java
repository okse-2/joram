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
 * An <code>AbstractJmsRequest</code> is a request sent by a Joram client
 * to its proxy.
 */
public class AbstractJmsRequest implements java.io.Serializable
{
  /** 
   * Identifier of the request. 
   * Declared volatile to allow a thread that is not the
   * thread sending the request to get the identifier
   * in order to cancel it during a close.
   */
  private volatile int requestId = -1;

  /**
   * The request target is either a destination agent name, or a subscription 
   * name.
   */
  protected String target = null;


  /**
   * Constructs an <code>AbstractJmsRequest</code>.
   *
   * @param target  String identifier of the request target, either a queue
   *          name, or a subscription name.
   */
  public AbstractJmsRequest(String target)
  {
    this.target = target;
  }

  /**
   * Constructs an <code>AbstractJmsRequest</code>.
   */
  public AbstractJmsRequest()
  {}


  /** 
   * Sets the request identifier. 
   */
  public void setRequestId(int requestId)
  {
    this.requestId = requestId;
  }

   /** Sets the request target name. */
  public void setTarget(String target)
  {
    this.target = target;
  }
  
  /** Returns the request identifier. */
  public synchronized int getRequestId()
  {
    return requestId;
  }

  /** Returns the request target name.  */
  public String getTarget()
  {
    return target;
  }

  /** Returns the identifier as an hashtable key. */
  public Integer getKey()
  {
    return new Integer(requestId);
  }

  public Hashtable soapCode() {
    Hashtable h = new Hashtable();
    h.put("className",getClass().getName());
    h.put("requestId",getKey());
    if (target != null)
      h.put("target",target);
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    AbstractJmsRequest req = 
      new AbstractJmsRequest((String) h.get("target"));
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    return req;
  }

  public String toString() {
    return '(' + super.toString() +
      ",requestId=" + requestId + 
      ",target=" + target + ')';
  }
}
