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

import java.util.*;

/**
 * An <code>XASessCommit</code> instance is used by an <code>XASession</code>
 * for commiting the messages and acknowledgements it sent to the proxy.
 */
public class XASessCommit extends AbstractJmsRequest
{
  /** Identifier of the resource and the commiting transaction. */
  private String id;


  /**
   * Constructs an <code>XASessCommit</code> instance.
   *
   * @param id  Identifier of the resource and the commiting transaction.
   */
  public XASessCommit(String id)
  {
    super();
    this.id = id;
  }

  /**
   * Constructs an <code>XASessCommit</code> instance.
   */
  public XASessCommit()
  {}


  /** Sets the identifier. */
  public void setId(String id)
  {
    this.id = id;
  }

  /** Returns the identifier of the resource and the commiting transaction. */
  public String getId()
  {
    return id;
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    if (id != null)
      h.put("id",id);
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    XASessCommit req = new XASessCommit();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    req.setId((String) h.get("id"));
    return req;
  }
}
