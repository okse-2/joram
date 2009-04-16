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
 * 
 */
public final class AdminMessage extends Message {
  boolean RObody;
  
  /** 
   * Instanciates a bright new <code>AdminMessage</code>.
   */
  AdminMessage() {
    super();
    type = ADMIN;
    RObody = false;
  }

  /**
   * Sets an AbstractAdminMessage as the body of the message. 
   * 
   * @param adminMsg  admin message
   * @throws Exception 
   */
  public void setAdminMessage(AbstractAdminMessage adminMsg) throws Exception {
    type = ADMIN;
    OutputXStream out = new OutputXStream();
    AbstractAdminMessage.write(adminMsg, out);
    length = out.size();
    body = new byte[length+4];
    out.toBuffer(body);
  }
  
  /**
   * Get an AbstractAdminMessage as the body of the message. 
   * 
   * @throws Exception 
   */
  public AbstractAdminMessage getAdminMessage() throws Exception {
    if (body == null) return null;
    InputXStream in = new InputXStream(body, body.length);
    AbstractAdminMessage adminMsg = AbstractAdminMessage.read(in);
    return adminMsg;
  }
}
