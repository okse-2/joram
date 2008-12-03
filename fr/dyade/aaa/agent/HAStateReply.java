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

import java.io.Serializable;

/**
 * Message used by the master component to send the server state.
 */
public class HAStateReply implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Engine state */
  long now;
  int stamp;
  byte[] agents = null;
  byte[]  messages = null;

  /** Network state */
  private byte[] clock = null;

  public HAStateReply() {
  }

  public void setNetworkStamp(byte[] clock) {
    this.clock = clock;
  }

  public byte[] getNetworkStamp() {
    return clock;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();

    buf.append("(HAStateReply(").append(super.toString());
    buf.append("now=").append(now);
    buf.append(",stamp=").append(stamp);
    buf.append("],clock=").append(clock);
    buf.append("))");

    return buf.toString();
  }
}
