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

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;

/**
 * Implements the <code>javax.jms.TextMessage</code> interface.
 */
public class TextMessage extends Message implements javax.jms.TextMessage
{
  /** The wrapped text. */
  private String text = null;
  /** <code>true</code> if the message body is read-only. */
  private boolean RObody = false;

  /** 
   * Instanciates a bright new <code>TextMessage</code>.
   */
  TextMessage()
  {
    super();
  }
  
  /** 
   * Instanciates a <code>TextMessage</code> wrapping a consumed
   * MOM message containing a text.
   *
   * @param sess  The consuming session.
   * @param momMsg  The MOM message to wrap.
   */
  TextMessage(Session sess, org.objectweb.joram.shared.messages.Message momMsg)
  {
    super(sess, momMsg);
    text = momMsg.getText();
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
    text = null;
    RObody = false;
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  When trying to set the text
   *              if the message body is read-only.
   */
  public void setText(String text) throws MessageNotWriteableException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a text as the"
                                             + " message body is read-only.");
    this.text = text;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getText() throws JMSException
  {
    return text;
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
    momMsg.clearBody();
    momMsg.setText(text);
  }

  public String toString() {
    return '(' + super.toString() + 
      ",text=" + text +
      ",RObody=" + RObody + ')';
  }
}
