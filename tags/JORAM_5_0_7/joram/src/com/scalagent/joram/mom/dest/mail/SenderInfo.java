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

public class SenderInfo implements java.io.Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public String smtpServer;
  public String to;
  public String cc;
  public String bcc;
  public String from;
  public String subject;
  public String selector;
  
  public SenderInfo(String smtpServer,
                    String to,
                    String cc,
                    String bcc,
                    String from,
                    String subject,
                    String selector) {
    this.smtpServer = smtpServer;
    this.to = to;
    this.cc = cc;
    this.bcc = bcc;
    this.from = from;
    this.subject = subject;
    this.selector = selector;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("(smtp=");
    sb.append(smtpServer);
    sb.append(",to=");
    sb.append(to);
    sb.append(",cc=");
    sb.append(cc);
    sb.append(",bcc=");
    sb.append(bcc);
    sb.append(",from=");
    sb.append(from);
    sb.append(",subject=");
    sb.append(subject);
    sb.append(",selector=");
    sb.append(selector);
    sb.append(")");
    return sb.toString();
  }

  public boolean equals(Object obj) {
    if (! (obj instanceof SenderInfo))
      return false;
    else {
      SenderInfo si = (SenderInfo) obj;
      boolean b = true;
      if ((smtpServer != null) && (si.smtpServer != null))
        b &= smtpServer.equals(si.smtpServer);
      if ((to != null) && (si.to != null))
        b &= to.equals(si.to);
      if ((cc != null) && (si.cc != null))
        b &= cc.equals(si.cc);
      if ((bcc != null) && (si.bcc != null))
        b &= bcc.equals(si.bcc);
      if ((from != null) && (si.from != null))
        b &= from.equals(si.from);
      if ((subject != null) && (si.subject != null))
        b &= subject.equals(si.subject);
      if ((selector != null) && (si.selector != null))
        b &= selector.equals(si.selector);
      return b;
    }
  }
}
