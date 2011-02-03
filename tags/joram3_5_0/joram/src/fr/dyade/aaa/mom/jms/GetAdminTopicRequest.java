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
package fr.dyade.aaa.mom.jms;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * A <code>GetAdminTopicRequest</code> is sent by a session for retrieving
 * the identifier of the local admin topic.
 */
public class GetAdminTopicRequest extends AbstractJmsRequest
{
  /** Constructs a <code>GetAdminTopicRequest</code> instance. */
  public GetAdminTopicRequest()
  {
    super(null);
  }

  public Hashtable soapCode() {
    return super.soapCode();
  }

  public static Object soapDecode(Hashtable h) {
    GetAdminTopicRequest req = new GetAdminTopicRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    return req;
  }
}
