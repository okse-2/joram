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
 * Contributor(s): David Feliot (ScalAgent DT)
 */
package org.objectweb.joram.mom.proxies;

import org.objectweb.joram.shared.client.AbstractJmsRequest;


/**
 * A <code>RequestNotification</code> is a notification used by a proxy for
 * carrying a client request.
 */
class RequestNotification extends fr.dyade.aaa.agent.Notification
{
  /** Identifier of the context within which the request has been received. */
  int id;
  /** Request sent by the client. */
  AbstractJmsRequest request;

  /**
   * Constructs a <code>RequestNotification</code> instance.
   *
   * @param id  Identifier of the context within which the request has
   *          been received.
   * @param request  Request sent by the client.
   */
  RequestNotification(int id, AbstractJmsRequest request)
  {
    this.id = id;
    this.request = request;
  }

  public String toString() {
    return '(' + super.toString() +
      ",id=" + id + 
      ",request=" + request + ')';
  }
}
