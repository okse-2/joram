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

import java.io.*;

import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

/**
 * Implements the <code>javax.jms.ObjectMessage</code> interface.
 */
public class ObjectMessage extends Message implements javax.jms.ObjectMessage
{
  /** The wrapped object. */
  private Serializable object = null;
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
   *
   * @exception MessageFormatException  In case of a problem when getting the
   *              MOM message data.
   */
  ObjectMessage(Session sess, fr.dyade.aaa.mom.messages.Message momMsg)
  throws MessageFormatException
  {
    super(sess, momMsg);
    try {
      object = (Serializable) momMsg.getObject();
    }
    catch (Exception exc) {
      MessageFormatException jE =
        new MessageFormatException("Error while getting the body.");
      jE.setLinkedException(exc);
      throw jE;
    }
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
    object = null;
    RObody = false;
  }

  /**
   * API method.
   *
   * @exception MessageNotWriteableException  When trying to set an object if
   *              the message body is read-only.
   */
  public void setObject(Serializable obj) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set an object as the"
                                             + " message body is read-only.");
    this.object = obj;
  }

  /**
   * API method.
   * 
   * @exception JMSException  Actually never thrown.
   */
  public Serializable getObject() throws MessageFormatException
  {
    return object;
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
    momMsg.setObject(object);
  }
}
