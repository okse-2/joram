/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.joram;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;

/**
 * Implements the <code>javax.jms.ObjectMessage</code> interface.
 */
public class ObjectMessage extends Message implements javax.jms.ObjectMessage
{
  /** The wrapped object. */
  private Serializable obj = null;
 
 
  /**
   * Instanciates an empty <code>ObjectMessage</code>.
   */
  ObjectMessage(Session sess)
  {
    super(sess);
  }


  /**
   * Instanciates an <code>ObjectMessage</code> wrapping a given object.
   */
  ObjectMessage(Session sess, Serializable obj)
  {
    super(sess);
    this.obj = obj;
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
      throw new MessageNotWriteableException("Can't set an object on a"
                                             + " read-only message.");
    this.obj = obj;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public Serializable getObject() throws JMSException
  {
    return obj;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void clearBody() throws JMSException
  {
    super.clearBody();
    obj = null;
  }

  /**
   * Method actually serializing the wrapped object into the MOM message.
   *
   * @exception Exception  If an error occurs while serializing.
   */
  protected void prepare() throws Exception
  {
    momMsg.setObject(obj);
  }

  /** 
   * Method actually deserializing the MOM body as the wrapped object.
   *
   * @exception Exception  If an error occurs while deserializing.
   */
  protected void restore() throws Exception
  {
    obj = (Serializable) momMsg.getObject();
  }
}
