/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s):
 */
package org.objectweb.joram.shared.client;

import java.util.Hashtable;


/**
 * A <code>XACnxRecoverRequest</code> instance is used by an
 * <code>XACnxion</code> for recovering the identifiers of the TM's
 * prepared transactions.
 */
public class XACnxRecoverRequest extends AbstractJmsRequest
{
  /**
   * Constructs a <code>XACnxRecoverRequest</code> instance.
   */
  public XACnxRecoverRequest()
  {}


  public static Object soapDecode(Hashtable h)
  {
    XACnxRecoverRequest req = new XACnxRecoverRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    return req;
  }
}
