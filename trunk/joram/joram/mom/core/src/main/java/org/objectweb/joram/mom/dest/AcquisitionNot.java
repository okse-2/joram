/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2012 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package org.objectweb.joram.mom.dest;

import org.objectweb.joram.mom.notifications.ClientMessages;

import fr.dyade.aaa.agent.Notification;

public class AcquisitionNot extends Notification {
  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  private ClientMessages acquiredMessages;

  private String id;

  public AcquisitionNot(ClientMessages acquiredMessages, boolean persistent, String id) {
    this.acquiredMessages = acquiredMessages;
    this.persistent = persistent;
    this.id = id;
  }

  public ClientMessages getAcquiredMessages() {
    return acquiredMessages;
  }

  public String getId() {
    return id;
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   * 
   * @param output buffer to fill in
   * @return <code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",id=").append(id);
    output.append(",acquiredMessages=").append(acquiredMessages);
    output.append(')');

    return output;
  }
}
