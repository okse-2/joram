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

/**
 * An <code>AbstractReply</code> is a reply sent by a destination agent to
 * a client agent.
 */
public abstract class AbstractReply extends AbstractNotification
{
  /**
   * The <code>correlationId</code> field is equal to the request's
   * identifier.
   */
  private int correlationId = -1;


  /**
   * Constructs an <code>AbstractReply</code>.
   *
   * @param clientContext  Client context identifier.
   * @param correlationId  Identifier of the reply.
   */
  public AbstractReply(int clientContext, int correlationId)
  {
    super(clientContext);
    this.correlationId = correlationId;
  }

  /**
   * Constructs an <code>AbstractReply</code>.
   */
  public AbstractReply()
  {}


  /** Returns the reply identifier. */
  public int getCorrelationId()
  {
    return correlationId;
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output
   *	buffer to fill in
   * @return
	<code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(", correlationId=").append(correlationId);
    output.append(')');

    return output;
  }
}
