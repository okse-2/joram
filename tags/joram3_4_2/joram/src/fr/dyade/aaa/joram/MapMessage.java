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

import fr.dyade.aaa.mom.excepts.MessageValueException;
import fr.dyade.aaa.mom.messages.ConversionHelper;

import java.util.*;

import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

/**
 * Implements the <code>javax.jms.MapMessage</code> interface.
 */
public class MapMessage extends Message implements javax.jms.MapMessage
{
  /** The wrapped hashtable. */
  private Hashtable map;
  /** <code>true</code> if the message body is read-only. */
  private boolean RObody = false;


  /**
   * Instanciates a bright new <code>MapMessage</code>.
   */
  MapMessage()
  {
    super();
    map = new Hashtable();
  }

  /**
   * Instanciates a <code>MapMessage</code> wrapping a consumed MOM
   * message containing an hashtable.
   *
   * @param sess  The consuming session.
   * @param momMsg  The MOM message to wrap.
   *
   * @exception MessageFormatException  In case of a problem when getting the
   *              MOM message data.
   */
  MapMessage(Session sess, fr.dyade.aaa.mom.messages.Message momMsg)
  throws MessageFormatException
  {
    super(sess, momMsg);
    try {
      map = momMsg.getMap();
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
    map.clear();
    RObody = false;
  }


  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */
  public void setBoolean(String name, boolean value) throws JMSException
  {
    setObject(name, new Boolean(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setByte(String name, byte value) throws JMSException
  {
    setObject(name, new Byte(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void setBytes(String name, byte[] value) throws JMSException
  {
    setObject(name, value);
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setBytes(String name, byte[] value, int offset, int length)
              throws JMSException
  {
    byte[] buff = new byte[length];

    for (int i = 0; i < length; i++)
      buff[i] = value[i + offset];

    setObject(name, buff);
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setChar(String name, char value) throws JMSException
  {
    setObject(name, new Character(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setDouble(String name, double value) throws JMSException
  {
    setObject(name, new Double(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void setFloat(String name, float value) throws JMSException
  {
    setObject(name, new Float(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */  
  public void setInt(String name, int value) throws JMSException
  {
    setObject(name, new Integer(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setLong(String name, long value) throws JMSException
  {
    setObject(name, new Long(value));
  }
  
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */  
  public void setShort(String name, short value) throws JMSException
  {
    setObject(name, new Short(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void setString(String name, String value) throws JMSException
  {
    setObject(name, value);
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception MessageFormatException  If the value type is invalid.
   */ 
  public void setObject(String name, Object value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid null or empty value name.");

    if (value == null)
      return;

    if (value instanceof Boolean || value instanceof Character 
        || value instanceof Number || value instanceof String
        || value instanceof byte[])
      map.put(name, value);
    else
      throw new MessageFormatException("Can't set non Java primitive type as"
                                       + " a map value.");
  }
  
  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */
  public boolean getBoolean(String name) throws JMSException
  {
    try {
      return ConversionHelper.toBoolean(map.get(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public byte getByte(String name) throws JMSException
  {
    try {
      return ConversionHelper.toByte(map.get(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public byte[] getBytes(String name) throws JMSException
  {
    try {
      return ConversionHelper.toBytes(map.get(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public char getChar(String name) throws JMSException
  {
    try {
      return ConversionHelper.toChar(map.get(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }
 
  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */   
  public double getDouble(String name) throws JMSException
  {
    try {
      return ConversionHelper.toDouble(map.get(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public float getFloat(String name) throws JMSException
  {
    try {
      return ConversionHelper.toFloat(map.get(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public int getInt(String name) throws JMSException
  {
    try {
      return ConversionHelper.toInt(map.get(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public long getLong(String name) throws JMSException
  {
    try {
      return ConversionHelper.toLong(map.get(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }
 
  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */   
  public Object getObject(String name) throws JMSException
  {
    return map.get(name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */     
  public short getShort(String name) throws JMSException
  {
    try {
      return ConversionHelper.toShort(map.get(name));
    }
    catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */   
  public String getString(String name) throws JMSException
  {
    return ConversionHelper.toString(map.get(name));
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */  
  public boolean itemExists(String name) throws JMSException
  {
    return (map.get(name) != null);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public Enumeration getMapNames() throws JMSException
  {
    return map.keys();
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
    momMsg.setMap(map);
  }
}
