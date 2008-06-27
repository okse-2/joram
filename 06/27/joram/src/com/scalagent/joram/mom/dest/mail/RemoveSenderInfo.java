/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s): 
 */
package com.scalagent.joram.mom.dest.mail;

public class RemoveSenderInfo extends org.objectweb.joram.shared.admin.SpecialAdmin {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public SenderInfo si = null;
  public int index = -1;
  
  public RemoveSenderInfo(String destId, SenderInfo si) {
    super(destId);
    this.si = si;
  }
  
  public RemoveSenderInfo(String destId, int index) {
    super(destId);
    this.index = index;
  }
  
  public String toString() {
    return "RemoveSenderInfo (destId=" + getDestId() + 
      ", index=" + index + ", si=" + si + ")";
  }
}
