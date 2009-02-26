/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

/**
 * Implements the <code>javax.jms.ObjectMessage</code> interface.
 */
public final class ObjectMessage extends Message implements javax.jms.ObjectMessage {
  /**
   * Instantiates a bright new <code>ObjectMessage</code>.
   */
  ObjectMessage() {
    super();
    momMsg.type = org.objectweb.joram.shared.messages.Message.OBJECT;
  }

  /**
   * Instantiates an <code>ObjectMessage</code> wrapping a
   * consumed MOM message containing an object.
   *
   * @param session  The consuming session.
   * @param momMsg  The MOM message to wrap.
   */
  ObjectMessage(Session session,
                org.objectweb.joram.shared.messages.Message momMsg) {
    super(session, momMsg);
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  When trying to set an object if
   *              the message body is read-only.
   * @exception MessageFormatException        If object serialization fails.
   */
  public void setObject(Serializable obj) throws JMSException {
    if (RObody)
      throw new MessageNotWriteableException("Can't set an object as the message body is read-only.");

    try {
      clearBody();
      momMsg.setObject(obj);
    } catch (Exception exc) {
      throw new MessageFormatException("Object serialization failed: " + exc);
    }
  }

  /**
   * API method.
   * 
   * @exception MessageFormatException  In case of a problem when getting the
   *              body.
   */
  public Serializable getObject() throws MessageFormatException {
    if (momMsg.body == null) return null;

    try {
      return momMsg.getObject();
    } catch (Exception exc) {
      throw new MessageFormatException("Error while deserializing the wrapped object: " + exc);
     }
  }
}
