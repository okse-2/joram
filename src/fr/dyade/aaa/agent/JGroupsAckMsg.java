/*
 * Copyright (C) 2004 - France Telecom R&D
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

import java.io.IOException;

/**
 * Message used by the master component to braodcast the ack of a sending
 * message (so each slave can remove the message from sending queue).
 */
public class JGroupsAckMsg implements java.io.Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  String name = null;
  int stamp;
  boolean isPersistent;

  public JGroupsAckMsg(Message message) {
    this.name = message.toStringId();
    this.stamp = message.getStamp();
    this.isPersistent = message.isPersistent();
  }

  /**
   * Deletes the current object in persistent storage.
   */
  void delete()  throws IOException {
    if (isPersistent) {
      AgentServer.getTransaction().delete(name);
    }
  }

  int getStamp() {
    return stamp;
  }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();

    strBuf.append("(").append(super.toString());
    strBuf.append(",name=").append(name);
    strBuf.append(",stamp=").append(stamp);
    strBuf.append(")");

    return strBuf.toString();
  }
}
