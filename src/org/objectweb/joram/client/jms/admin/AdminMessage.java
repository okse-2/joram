/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms.admin;

import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import org.objectweb.joram.client.jms.Message;
import org.objectweb.joram.client.jms.Session;
import org.objectweb.joram.shared.admin.AbstractAdminMessage;

/**
 * The class AdminMessage allows to send and receive administration messages.
 * It defines a proprietary JMS message type, the body is serialized through a
 * proprietary encoding.
 */
public class AdminMessage extends Message {
  /**
   * Instantiates a bright new <code>AdminMessage</code>.
   */
  public AdminMessage() {
    super();
    momMsg.type = org.objectweb.joram.shared.messages.Message.ADMIN;
  }

  /**
   * Instantiates an <code>AdminMessage</code> wrapping a
   * consumed MOM message containing a Admin message.
   *
   * @param session  The consuming session.
   * @param momMsg  The MOM message to wrap.
   */
  public AdminMessage(Session session,
                      org.objectweb.joram.shared.messages.Message momMsg) {
    super(session, momMsg);
  }
  
  /**
   * Sets an AbstractAdminMessage as the body of the message. 
   * 
   * @param adminMsg  admin message
   * @throws MessageNotWriteableException
   * @throws MessageFormatException
   */
  public void setAdminMessage(AbstractAdminMessage adminMsg) throws MessageNotWriteableException, MessageFormatException {
    if (RObody)
      throw new MessageNotWriteableException("Can't set an AbstractAdminMessage as the message body is read-only.");

    try {
      clearBody();
      momMsg.setAdminMessage(adminMsg);
    } catch (Exception exc) {
      throw new MessageFormatException("Object serialization failed: " + exc);
    }
  }
  
  /**
   * get the AbstractAdminMessage body of the message.
   * 
   * @return AbstractAdminMessage body of the message.
   * @throws MessageFormatException
   */
  public AbstractAdminMessage getAdminMessage() throws MessageFormatException {
    return momMsg.getAdminMessage();
  }
}
