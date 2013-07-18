/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

/**
 * Implements the <code>javax.jms.TextMessage</code> interface.
 */
public final class TextMessage extends Message implements javax.jms.TextMessage {
  /** 
   * Instantiates a bright new <code>TextMessage</code>.
   */
  TextMessage() {
    super();
    momMsg.type = org.objectweb.joram.shared.messages.Message.TEXT;
  }

  /** 
   * Instantiates a <code>TextMessage</code> wrapping a consumed
   * MOM message containing a text.
   *
   * @param session  The consuming session.
   * @param momMsg  The MOM message to wrap.
   */
  TextMessage(Session session,
              org.objectweb.joram.shared.messages.Message momMsg) {
    super(session, momMsg);
  }

  /**
   * API method.
   * Sets a String as the body of the message.
   * 
   * @param text the String containing the message's data.
   *
   * @exception MessageNotWriteableException  When trying to set the text
   *              if the message body is read-only.
   * @exception MessageFormatException If the text serialization fails.
   */
  public void setText(String text) throws MessageNotWriteableException, MessageFormatException {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a text as the message body is read-only.");

    try {
	    momMsg.setText(text);
    } catch (IOException e) {
    	throw new MessageFormatException("The text serialization failed: " + e);
    }
  }

  /**
   * API method.
   * Returns the text body of the message, null if none.
   * 
   * @return the String containing this message's data.
   *
   * @exception JMSException  In case of a problem when getting the body.
   */
  public String getText() throws JMSException {
    try {
	    return momMsg.getText();
    } catch (Exception e) {
    	throw new MessageFormatException("Error while deserializing the text body : " + e);
    }
  }

  @Override
  protected <T> T getEffectiveBody(Class<T> c) throws JMSException {
    return (T) getText();
  }
  
  
}
