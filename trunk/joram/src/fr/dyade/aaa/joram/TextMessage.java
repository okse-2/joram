/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram;

import fr.dyade.aaa.mom.excepts.*;

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
  TextMessage(Session sess, fr.dyade.aaa.mom.messages.Message momMsg)
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
}
