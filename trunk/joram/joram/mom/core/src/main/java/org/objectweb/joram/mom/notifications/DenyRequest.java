/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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
 * A <code>DenyRequest</code> instance is used by a client agent
 * for denying one or many messages on a queue.
 */
public class DenyRequest extends AbstractRequestNot {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** Message identifier. */
  private String msgId = null;
  /** Vector of message identifiers. */
  private Vector msgIds = null;

  /** true if the message has already been delivered.  */
  private boolean redelivered = false;
  
  /**
   * @return the redelivered
   */
  public boolean isRedelivered() {
    return redelivered;
  }

  /**
   * @param redelivered the redelivered to set
   */
  public void setRedelivered(boolean redelivered) {
    this.redelivered = redelivered;
  }

  /**
   * Constructs an <code>DenyRequest</code> instance.
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Request identifier.
   * @param msgIds  Vector of message identifiers.
   */
  public DenyRequest(int clientContext, int requestId, Vector msgIds)
  {
    super(clientContext, requestId);
    this.msgIds = msgIds;
  }

  /**
   * Constructs an <code>DenyRequest</code> instance.
   *
   * @param clientContext Identifies a client context.
   * @param requestId     Request identifier.
   * @param msgId         Message identifier.
   */
  public DenyRequest(int clientContext, int requestId, String msgId) {
    super(clientContext, requestId);
    this.msgId = msgId;
  }

  /**
   * Constructs an <code>DenyRequest</code> instance.
   *
   * @param clientContext  Identifies a client context.
   */
  public DenyRequest(int clientContext) {
    super(clientContext, -1);
  }


  /** Returns the denied messages' identifiers. */
  public Enumeration getIds() {
    if (msgIds == null) {
      msgIds = new Vector();
      if (msgId != null)
        msgIds.add(msgId);
    }
    return msgIds.elements();
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
    output.append(",msgId=").append(msgId);
    output.append(",msgIds=").append(msgIds);
    output.append(",isRedelivered=").append(redelivered);
    output.append(')');

    return output;
  }
} 


