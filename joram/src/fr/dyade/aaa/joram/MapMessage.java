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


  /**
   * Instanciates a <code>MapMessage</code>.
   */
  MapMessage(Session sess)
  {
    super(sess);
    map = new Hashtable();
  }
  

  /** 
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void clearBody() throws JMSException
  {
    map.clear();
  }


  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */
  public void setBoolean(String name, boolean value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");

    map.put(name, new Boolean(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setByte(String name, byte value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");

    map.put(name, new Byte(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void setBytes(String name, byte[] value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");

    map.put(name, value);
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setBytes(String name, byte[] value, int offset, int length)
            throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");

    byte[] buff = new byte[length];
    for (int i = 0; i < length; i++) {
      buff[i] = value[i+offset];
    }
    map.put(name, buff);
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setChar(String name, char value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");

    map.put(name, new Character(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setDouble(String name, double value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");

    map.put(name, new Double(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void setFloat(String name, float value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");

    map.put(name, new Float(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */  
  public void setInt(String name, int value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");

    map.put(name, new Integer(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setLong(String name, long value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");

    map.put(name, new Long(value));
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
   * @exception MessageNotWriteableException  If the message body is read-only.
   */  
  public void setShort(String name, short value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");

    map.put(name, new Short(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void setString(String name, String value) throws JMSException
  {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");

    map.put(name, value);
  }


  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */
  public boolean getBoolean(String name) throws JMSException
  {
    return ConversionHelper.getBoolean(map, name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public byte getByte(String name) throws JMSException
  {
    return ConversionHelper.getByte(map, name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public byte[] getBytes(String name) throws JMSException
  {
    return ConversionHelper.getBytes(map, name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public char getChar(String name) throws JMSException
  {
    return ConversionHelper.getChar(map, name);
  }
 
  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */   
  public double getDouble(String name) throws JMSException
  {
    return ConversionHelper.getDouble(map, name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public float getFloat(String name) throws JMSException
  {
    return ConversionHelper.getFloat(map, name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public int getInt(String name) throws JMSException
  {
    return ConversionHelper.getInt(map, name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public long getLong(String name) throws JMSException
  {
    return ConversionHelper.getLong(map, name);
  }
 
  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
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
    return ConversionHelper.getShort(map, name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */   
  public String getString(String name) throws JMSException
  {
    return ConversionHelper.getString(map, name);
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
   * Method actually serializing the wrapped map into the MOM message.
   *
   * @exception Exception  If an error occurs while serializing.
   */
  protected void prepare() throws Exception
  {
    momMsg.setMap(map);
  }

  /** 
   * Method actually deserializing the MOM body as the wrapped map.
   *
   * @exception Exception  If an error occurs while deserializing.
   */
  protected void restore() throws Exception
  {
    map = momMsg.getMap();
  }
}

