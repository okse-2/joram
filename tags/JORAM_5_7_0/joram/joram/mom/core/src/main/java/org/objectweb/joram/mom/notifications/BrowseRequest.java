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
 * A <code>BrowseRequest</code> instance is used by a client agent for 
 * requesting a "view" of the messages on a queue, without actually consuming
 * them.
 */
public class BrowseRequest extends AbstractRequestNot
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * String selector for filtering messages, null or empty for no selection.
   */
  private String selector;


  /**
   * Constructs a <code>BrowseRequest</code> instance. 
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Request identifier.
   * @param selector  Selector expression for filtering messages, null or empty
   *          for no selection.
   */
  public BrowseRequest(int clientContext, int requestId, String selector)
  {
    super(clientContext, requestId);
    this.selector = selector;
  }


  /** Returns the selector of the request. */
  public String getSelector()
  {
    return selector;
  }
} 
