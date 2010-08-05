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
 * A <code>SubscribeRequest</code> instance is used by a client agent
 * for subscribing to a topic.
 */
public class SubscribeRequest extends AbstractRequestNot {
  /** */
  private static final long serialVersionUID = 1L;
  
  /**
   * Selector for filtering messages, null or empty string for no selection.
   */
  private String selector;
  
  /** asynchronous subscription request. */
  private boolean asyncSub = false;

  /**
   * Constructs a <code>SubscribeRequest</code> instance. 
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Request identifier.
   * @param selector  Selector expression for filtering messages, null or 
   *          empty string for no selection.
   * @param asyncSub true if asynchronous subscription.
   */
  public SubscribeRequest(int clientContext, int requestId, String selector, boolean asyncSub) {
    super(clientContext, requestId);
    this.selector = selector;
    this.asyncSub = asyncSub;
  }

  /**
   * 
   * @return true for asynchronous subscription request.
   */
  public boolean isAsyncSub() {
    return asyncSub;
  }

  /** Returns the selector. */
  public String getSelector() {
    return selector;
  }
} 
