/*
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.mail;

public class UpdateSenderInfo extends org.objectweb.joram.shared.admin.SpecialAdmin {
  public SenderInfo oldSi = null;
  public SenderInfo newSi = null;
  
  public UpdateSenderInfo(String destId,
                          SenderInfo oldSi,
                          SenderInfo newSi) {
    super(destId);
    this.oldSi = oldSi;
    this.newSi = newSi;
  }

  public String toString() {
    return "UpdateSenderInfo (destId=" + getDestId() + 
      ", oldSi=" + oldSi + ", newSi=" + newSi + ")";
  }
}
