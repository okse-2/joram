/*
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.mail;

public class AddSenderInfo extends org.objectweb.joram.shared.admin.SpecialAdmin {
  public SenderInfo si = null;
  public int index = -1;
  
  public AddSenderInfo(String destId,
                       SenderInfo si,
                       int index) {
    super(destId);
    this.si = si;
    this.index = index;
  }
  
  public AddSenderInfo(String destId,
                       SenderInfo si) {
    this(destId,si,-1);
  }

  public String toString() {
    return "AddSenderInfo (destId=" + getDestId() + 
      ", index=" + index + ", si=" + si + ")";
  }
}
