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
package org.objectweb.joram.mom.notifications;

import org.objectweb.joram.shared.excepts.MomException;

/**
 * An <code>ExceptionReply</code> instance is used by a destination for
 * notifying a client of an exception thrown when processing a request.
 */
public class ExceptionReply extends AbstractReplyNot
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * The <code>MomException</code> which occured when processing the request.
   */
  private MomException except;


  /**
   * Constructs an <code>ExceptionReply</code> instance.
   *
   * @param request  The request that caused the exception.
   * @param except  The exception to send back to the client.
   */
  public ExceptionReply(AbstractRequestNot request, MomException except)
  {
    super(request.getClientContext(), request.getRequestId());
    this.except = except;
  }

  /**
   * Constructs an <code>ExceptionReply</code> instance.
   *
   * @param except  The exception to send back to the client.
   */
  public ExceptionReply(MomException except)
  {
    this.except = except;
  }


  /** Returns the exception wrapped by the reply. */
  public MomException getException()
  {
    return except;
  }
}
