/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2008 CNES
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
package org.objectweb.joram.mom.amqp;

import fr.dyade.aaa.agent.Notification;

/**
 * Notification sent to request an exchange or a queue deletion.
 */
public class DeleteNot extends Notification {
  
  private boolean ifUnused;
  
  // Used only for queue deletion
  private boolean ifEmpty;
  
  public DeleteNot() {
    super();
  }

  public DeleteNot(boolean ifUnused) {
    this.ifUnused = ifUnused;
  }

  public DeleteNot(boolean ifUnused, boolean ifEmpty) {
    this.ifUnused = ifUnused;
    this.ifEmpty = ifEmpty;
  }

  public boolean isIfUnused() {
    return ifUnused;
  }

  public boolean isIfEmpty() {
    return ifEmpty;
  }
  
}
