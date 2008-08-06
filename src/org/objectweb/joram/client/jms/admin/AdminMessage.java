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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import org.objectweb.joram.client.jms.Message;
import org.objectweb.joram.client.jms.Session;
import org.objectweb.joram.shared.admin.AbstractAdminMessage;

/**
 * @author tachker
 *
 */
public class AdminMessage extends Message {

  /**
   * Instanciates a bright new <code>AdminMessage</code>.
   */
  public AdminMessage() {
    super();
    momMsg.type = momMsg.ADMIN;
  }

  /**
   * Instanciates an <code>AdminMessage</code> wrapping a
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
  public void setAdminMessage(AbstractAdminMessage adminMsg) 
    throws MessageNotWriteableException, MessageFormatException {
    if (RObody)
      throw new MessageNotWriteableException("Can't set an AbstractAdminMessage as the"
                                             + " message body is read-only.");

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
    try {
      return momMsg.getAdminMessage();
    } catch (ClassNotFoundException cnfexc) {
      ByteArrayInputStream bais = null;
      InputStream is = null;
      try {
        // Could not build AbstractAdminMessage: reason could be linked to 
        // class loaders hierarchy in an application server.
        class Specialized_OIS extends ObjectInputStream {
          Specialized_OIS(InputStream is) throws IOException {
            super(is);
          }

          protected Class resolveClass(ObjectStreamClass osc)
            throws IOException, ClassNotFoundException {
            String n = osc.getName();
            return Class.forName(n, false,
                                 Thread.currentThread().getContextClassLoader());
          }
        }
        
        bais = new ByteArrayInputStream(momMsg.body);
        is = new Specialized_OIS(bais);
        return AbstractAdminMessage.read(is);
      } catch (Exception exc) {
        MessageFormatException jE =
          new MessageFormatException("Error while deserializing the wrapped object: " + exc);
        throw jE;
      } finally {
        try {
          is.close();
        } catch (Exception e) {}
        try {
          bais.close();
        } catch (Exception e) {}
      }
    } catch (Exception exc) {
      MessageFormatException jE =
        new MessageFormatException("Error while deserializing the wrapped object: " + exc);
      throw jE;
    }
  }
}
