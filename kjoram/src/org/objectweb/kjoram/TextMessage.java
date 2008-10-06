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
public final class TextMessage extends Message {
  boolean RObody;
  
  /** 
   * Instantiates a bright new <code>TextMessage</code>.
   */
  TextMessage() {
    super();
    type = TEXT;
    RObody = false;
  }

  /**
   * Sets a String as the body of the message.
   *
   * @exception MessageNotWriteableException  When trying to set the text
   *              if the message body is read-only.
   */
  public void setText(String text) throws JoramException {
    if (RObody)
      throw new JoramException("Can't set a text as the message body is read-only.");

    if (text == null) {
      body = null;
    } else {
      body = text.getBytes();
    }
  }

  /**
   * Returns the text body of the message.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getText() throws JoramException {
    if (body == null) {
      return null;
    } else {
      return new String(body);
    }
  }
}
