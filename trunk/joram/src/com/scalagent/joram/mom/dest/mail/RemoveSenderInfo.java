/*
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.mail;

public class RemoveSenderInfo extends org.objectweb.joram.shared.admin.SpecialAdmin {
  public SenderInfo si = null;
  public int index = -1;
  
  public RemoveSenderInfo(String destId,
                          SenderInfo si) {
    super(destId);
    this.si = si;
  }
  
  public RemoveSenderInfo(String destId,
                          int index) {
    super(destId);
    this.index = index;
  }
  
  public String toString() {
    return "RemoveSenderInfo (destId=" + getDestId() + 
      ", index=" + index + ", si=" + si + ")";
  }
}
