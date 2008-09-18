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
package com.scalagent.kjoram.comm;

/**
 * The <code>AbstractNotification</code> class is the superclass of the
 * notifications exchanged by a client agent and a MOM destination agent.
 */
public abstract class AbstractNotification
{
  /**
   * In the case where the client agent is a proxy agent representing, server
   * side, multiple external clients, this field allows the proxy to identify
   * a given client.
   * <p>
   * Keeping this information finally allows the proxy to route a reply to the 
   * correct client.
   * <p>
   * When the client is not a proxy, this field default value is 0.
   */
  private int clientContext = -1;


  /**
   * Constructs an <code>AbstractNotification</code>.
   *
   * @param clientContext  Identifies a client context.
   */
  public AbstractNotification(int clientContext)
  {
    this.clientContext = clientContext;
  }

  /**
   * Constructs an <code>AbstractNotification</code>.
   */
  public AbstractNotification()
  {}


  /** Returns the client context identifier. */
  public int getClientContext()
  {
    return clientContext;
  }
}
