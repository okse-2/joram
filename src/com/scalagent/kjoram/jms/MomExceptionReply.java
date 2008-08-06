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

import com.scalagent.kjoram.excepts.MomException;
import java.util.Hashtable;
import java.util.Enumeration;


/**
 * A <code>MomExceptionReply</code> instance is used by a JMS client proxy
 * to send a <code>MomException</code> back to a JMS client.
 */
public class MomExceptionReply extends AbstractJmsReply
{
  /** The wrapped exception. */
  private MomException momExcept;

  /**
   * Constructs a <code>MomExceptionReply</code> instance.
   *
   * @param correlationId  Identifier of the failed request.
   * @param momExcept  The resulting exception.
   */
  public MomExceptionReply(int correlationId, MomException momExcept)
  {
    super(correlationId);
    this.momExcept = momExcept;
  }

  /**
   * Constructs a <code>MomExceptionReply</code> instance.
   *
   * @param momExcept  The exception to wrap.
   */
  public MomExceptionReply(MomException momExcept)
  {
    this.momExcept = momExcept;
  }


  /** Returns the exception wrapped by this reply. */
  public MomException getException()
  {
    return momExcept;
  }

  /**
   * Transforms this reply into a hashtable of primitive values that can
   * be vehiculated through the SOAP protocol.
   */
  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    if (momExcept != null)
      h.put("momExcept",momExcept.getMessage());
    return h;
  }

  /** 
   * Transforms a hashtable of primitive values into a
   * <code>MomExceptionReply</code> reply.
   */
  public static Object soapDecode(Hashtable h) {
    return new MomExceptionReply(
      ((Integer) h.get("correlationId")).intValue(),
      new MomException((String) h.get("momExcept")));
  }
}
