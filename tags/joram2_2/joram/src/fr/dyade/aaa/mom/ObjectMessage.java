/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */


package fr.dyade.aaa.mom; 
 
import java.io.*;

/** 
 *	a ObjectMessage is as JMS specifications 
 * 
 *	@see	javax.jms.Message 
 */ 
 
public class ObjectMessage extends fr.dyade.aaa.mom.Message implements javax.jms.ObjectMessage{ 
 
  /** the object of the message */ 
  private byte[] obj;
	
  public ObjectMessage() {
    modeReadWrite = READ_WRITE_MODE;
  }
	
  /** @see jms specifications */ 
  public java.io.Serializable getObject() throws javax.jms.JMSException {
    try {
      ByteArrayInputStream bis = new ByteArrayInputStream(obj);
      ObjectInputStream ois = new ObjectInputStream(bis);
      return (java.io.Serializable) ois.readObject();
    } catch (Exception exc) {
      /* can never send a MessageFormatException */
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see jms specifications */ 
  public void setObject(java.io.Serializable obj) throws javax.jms.JMSException {
    try {
      if(modeReadWrite==READ_MODE)
        throw(new javax.jms.MessageNotWriteableException("Message Not Writeable"));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(obj);
      oos.flush();
      this.obj =  bos.toByteArray();
    } catch (javax.jms.JMSException exc) {
      throw(exc);	
    } catch (Exception exc) {
      /* can never send a MessageFormatException */
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** overwrite the methode as jms specified */ 
  public void clearBody() throws javax.jms.JMSException {
    try {
      modeReadWrite = READ_WRITE_MODE;
      obj = null;
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** put the mode in readOnly */
  public void reset() throws javax.jms.JMSException {
    try {
      modeReadWrite = READ_MODE;
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
}
