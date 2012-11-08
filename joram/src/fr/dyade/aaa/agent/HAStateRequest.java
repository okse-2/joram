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
import org.jgroups.Address;

/**
 * Message used by a slave component to request the server state.
 */
public class HAStateRequest implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * The requestor address. This address is actually unused as the
   * reply is broadcasted to all components (to keep order with other
   * messages).
   */
  private Address addr = null;

  public HAStateRequest(Address addr) {
    this.addr = addr;
  }

  public Address getAddress() {
    return addr;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();

    buf.append("(HAStateRequest(").append(super.toString());
    buf.append(",addr=").append(addr).append("))");

    return buf.toString();
  }
}
