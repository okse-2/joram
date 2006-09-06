/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s):
 */
package org.objectweb.joram.client.jms;

import org.objectweb.joram.shared.excepts.*;

import java.io.*;

import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

/**
 * Implements the <code>javax.jms.ObjectMessage</code> interface.
 */
public class ObjectMessage extends Message implements javax.jms.ObjectMessage
{
  /**
   * The object still coded as a bytes array (decoding will occur during
   * the <code>getObject()</code> call.
   */
  private byte[] codedObject = null;

  /** <code>true</code> if the message body is read-only. */
  private boolean RObody = false;


  /**
   * Instanciates a bright new <code>ObjectMessage</code>.
   */
  ObjectMessage()
  {
    super();
  }

  /**
   * Instanciates an <code>ObjectMessage</code> wrapping a
   * consumed MOM message containing an object.
   *
   * @param sess  The consuming session.
   * @param momMsg  The MOM message to wrap.
   */
  ObjectMessage(Session sess,
                org.objectweb.joram.shared.messages.Message momMsg)
  {
    super(sess, momMsg);
    codedObject = momMsg.getBytes();
    RObody = true;
  }


  /** 
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void clearBody() throws JMSException
  {
    super.clearBody();
    codedObject = null;
    RObody = false;
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  When trying to set an object if
   *              the message body is read-only.
   * @exception MessageFormatException        If object serialization fails.
   */
  public void setObject(Serializable obj) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set an object as the"
                                             + " message body is read-only.");
    try {
      momMsg.clearBody();
      momMsg.setObject(obj);
      codedObject = momMsg.getBytes();
    }
    catch (Exception exc) {
      throw new MessageFormatException("Object serialization failed: " + exc);
    }
  }

  /**
   * API method.
   * 
   * @exception MessageFormatException  In case of a problem when getting the
   *              body.
   */
  public Serializable getObject() throws MessageFormatException
  {
    if (codedObject == null)
      return null;

    try {
      try {
        ByteArrayInputStream bais = new ByteArrayInputStream(codedObject);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (Serializable) ois.readObject();
      } catch (ClassNotFoundException exc) {
        // Could not build serialized object: reason could be linked to 
        // class loaders hierarchy in an application server.
        class Specialized_OIS extends ObjectInputStream {
          Specialized_OIS(InputStream is) throws IOException {
            super(is);
          }

          protected Class resolveClass(ObjectStreamClass osc)
                    throws IOException, ClassNotFoundException {
            String n = osc.getName();
            return Class.forName(n, false, Thread.currentThread().getContextClassLoader());
          }
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(codedObject);
        ObjectInputStream ois = new Specialized_OIS(bais);
        return (Serializable) ois.readObject(); 
      }
    } catch (Exception exc) {
      MessageFormatException jE =
        new MessageFormatException("Error while deserializing the wrapped " 
                                   + "object: " + exc);
      throw jE;
    }
  }

  /**
   * Method actually preparing the message for sending by transfering the
   * local body into the wrapped MOM message.
   *
   * @exception Exception  If an error occurs while serializing.
   */
  protected void prepare() throws Exception
  {
    super.prepare();
  }

  public String toString() {
    return '(' + super.toString() +
      ",codedObject=" + codedObject + ')';
  }
}
