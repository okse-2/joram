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

import java.util.Enumeration;
import java.util.Vector;

/**
 * An <code>AcknowledgeRequest</code> instance is used by a client agent
 * for acknowledging one or many messages on a queue.
 */
public class AcknowledgeRequest extends AbstractRequestNot
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Message identifier. */
  private String msgId = null;
  /** Vector of message identifiers. */
  private Vector msgIds = null;

   
  /**
   * Constructs an <code>AcknowledgeRequest</code> instance.
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Request identifier.
   * @param msgIds  Vector of message identifiers.
   */
  public AcknowledgeRequest(int clientContext, int requestId, Vector msgIds)
  {
    super(clientContext, requestId);
    this.msgIds = msgIds;
  }

  /**
   * Constructs an <code>AcknowledgeRequest</code> instance.
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Request identifier.
   * @param msgId  Message identifier.
   */
  public AcknowledgeRequest(int clientContext, int requestId, String msgId)
  {
    super(clientContext, requestId);
    this.msgId = msgId;
  }


  /** Returns the acknowledged messages' identifiers. */
  public Enumeration getIds()
  {
    if (msgIds == null) {
      msgIds = new Vector();
      if (msgId != null)
        msgIds.add(msgId);
    }
    return msgIds.elements();
  }
} 
