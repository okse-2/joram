/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

/**
 * An <code>AdminRequest</code> is a request sent by a client administrator
 * inside a <code>org.objectweb.joram.shared.messages.Message</code> to an
 * <code>org.objectweb.joram.mom.dest.AdminTopic</code> topic for requesting an
 * admin operation.
 */
public abstract class AdminRequest extends AbstractAdminMessage {
  
  protected int getClassId() {
    return ADMIN_REQUEST;
  }
  
  public void toString(StringBuffer strbuf) {
  }
}
