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
 * A <code>GetAdminTopicReply</code> is sent by an administrator proxy for
 * notifying an administrator client of the identifier of the local admin
 * topic.
 */
public class GetAdminTopicReply extends AbstractJmsReply
{
  /** Identifier of the admin topic. */
  private String id;

  /**
   * Constructs a <code>GetAdminTopicReply</code> instance.
   *
   * @param request  The <code>GetAdminTopicRequest</code> being answered.
   * @param id  The identifier of the admin topic.
   */
  public GetAdminTopicReply(GetAdminTopicRequest request, String id)
  {
    super(request.getRequestId());
    this.id = id;
  }

  /**
   * Constructs a <code>GetAdminTopicReply</code> instance.
   */
  public GetAdminTopicReply()
  {}

  /** Sets the identifier of the admin topic. */
  public void setId(String id)
  {
    this.id = id;
  }

  /** Returns the identifier of the admin topic. */
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
    GetAdminTopicReply req = new GetAdminTopicReply();
    req.setCorrelationId(((Integer) h.get("correlationId")).intValue());
    req.setId((String) h.get("id"));
    return req;
  }
}
