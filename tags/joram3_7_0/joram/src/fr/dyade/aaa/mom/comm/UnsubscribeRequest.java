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
package fr.dyade.aaa.mom.comm;

/**
 * An <code>UnsubscribeRequest</code> instance is used by a client agent
 * for unsubscribing to a topic.
 */
public class UnsubscribeRequest extends AbstractRequest
{
  /**
   * Constructs an <code>UnsubscribeRequest</code> instance.
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Request identifier.
   */
  public UnsubscribeRequest(int clientContext, int requestId)
  {
    super(clientContext, requestId);
  }
}
