/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.proxies;

import org.objectweb.joram.shared.client.AbstractJmsMessage;

public class ProxyMessage implements java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  // JORAM_PERF_BRANCH
  //private long id;

  //private long ackId;

  private AbstractJmsMessage obj;

  public ProxyMessage(AbstractJmsMessage obj) {
    // JORAM_PERF_BRANCH
    //this.id = id;
    //this.ackId = ackId;
    this.obj = obj;
  }

  /* JORAM_PERF_BRANCH
  public final long getId() {
    return id;
  }

  public final long getAckId() {
    return ackId;
  }
*/
  public final AbstractJmsMessage getObject() {
    return obj;
  }

  public String toString() {
    return '(' + super.toString() + 
   /* JORAM_PERF_BRANCH
      ",id=" + id + 
      ",ackId=" + ackId + */
      ",obj=" + obj + ')';
  }
}
