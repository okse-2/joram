/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.messages.ConversionHelper;

/**
 * Implements the <code>javax.jms.MapMessage</code> interface.
 */
public final class MapMessage extends Message implements javax.jms.MapMessage {
  /** The wrapped hashmap. */
  private transient HashMap map;

  /**
   * Instantiates a bright new <code>MapMessage</code>.
   */
  MapMessage() {
    super();
    momMsg.type = org.objectweb.joram.shared.messages.Message.MAP;
    map = new HashMap();
  }

  /**
   * Instantiates a <code>MapMessage</code> wrapping a consumed MOM
   * message containing an hashtable.
   *
   * @param sess  The consuming session.
   * @param momMsg  The MOM message to wrap.
   *
   * @exception MessageFormatException  In case of a problem when getting the
   *              MOM message data.
   */
  MapMessage(Session session,
             org.objectweb.joram.shared.messages.Message momMsg) throws MessageFormatException {
    super(session, momMsg);

    ByteArrayInputStream bais = null;
    ObjectInputStream ois = null;
    try {
      bais = new ByteArrayInputStream(momMsg.body);
      ois = new ObjectInputStream(bais);
      map = (HashMap) ois.readObject();
    } catch (Exception exc) {
      MessageFormatException jE =
        new MessageFormatException("Error while getting the body.");
      jE.setLinkedException(exc);
      throw jE;
    } finally {
      try {
        ois.close();
      } catch (IOException exc) {}
      try {
        bais.close();
      } catch (IOException exc) {}
    }
  }  

  /** 
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void clearBody() throws JMSException {
    super.clearBody();
    map.clear();
  }


  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */
  public void setBoolean(String name, boolean value) throws JMSException {
    setObject(name, new Boolean(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setByte(String name, byte value) throws JMSException {
    setObject(name, new Byte(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void setBytes(String name, byte[] value) throws JMSException {
    setObject(name, value);
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setBytes(String name, byte[] value, int offset, int length) throws JMSException {
    byte[] buff = new byte[length];
    System.arraycopy(value, offset, buff, 0, length);
    setObject(name, buff);
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setChar(String name, char value) throws JMSException {
    setObject(name, new Character(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setDouble(String name, double value) throws JMSException {
    setObject(name, new Double(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void setFloat(String name, float value) throws JMSException {
    setObject(name, new Float(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */  
  public void setInt(String name, int value) throws JMSException {
    setObject(name, new Integer(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setLong(String name, long value) throws JMSException {
    setObject(name, new Long(value));
  }
  
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */  
  public void setShort(String name, short value) throws JMSException {
    setObject(name, new Short(value));
  }
 
  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void setString(String name, String value) throws JMSException {
    setObject(name, value);
  }

  /** 
   * API method.
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   * @exception MessageFormatException  If the value type is invalid.
   */ 
  public void setObject(String name, Object value) throws JMSException {
    if (RObody)
      throw new MessageNotWriteableException("Can't set a value as the message"
                                             + " body is read-only.");

    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid null or empty value name.");

    if (value instanceof Boolean || value instanceof Character ||
        value instanceof Number || value instanceof String ||
        value instanceof byte[] || value == null)
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
  public boolean getBoolean(String name) throws JMSException {
    try {
      return ConversionHelper.toBoolean(map.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public byte getByte(String name) throws JMSException {
    try {
      return ConversionHelper.toByte(map.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public byte[] getBytes(String name) throws JMSException {
    try {
      return ConversionHelper.toBytes(map.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public char getChar(String name) throws JMSException {
    try {
      return ConversionHelper.toChar(map.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }
 
  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */   
  public double getDouble(String name) throws JMSException {
    try {
      return ConversionHelper.toDouble(map.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public float getFloat(String name) throws JMSException {
    try {
      return ConversionHelper.toFloat(map.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public int getInt(String name) throws JMSException {
    try {
      return ConversionHelper.toInt(map.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */  
  public long getLong(String name) throws JMSException {
    try {
      return ConversionHelper.toLong(map.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }
 
  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */   
  public Object getObject(String name) throws JMSException {
    return map.get(name);
  }

  /**
   * API method.
   *
   * @exception MessageFormatException  If the value type is invalid.
   */     
  public short getShort(String name) throws JMSException {
    try {
      return ConversionHelper.toShort(map.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */   
  public String getString(String name) throws JMSException {
    Object value = map.get(name);
    if (value instanceof byte[])
      throw new MessageFormatException("Type " + value.getClass().getName() 
                                       + " can't be converted to String.");
    return ConversionHelper.toString(map.get(name));
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */  
  public boolean itemExists(String name) throws JMSException {
    return map.containsKey(name);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public Enumeration getMapNames() throws JMSException {
    Vector vec = new Vector();
    if (map.keySet() != null) {
      for (Iterator it = map.keySet().iterator(); it.hasNext(); )
        vec.add(it.next());
    }
    return vec.elements();
  }

  /**
   * Method actually preparing the message for sending by transfering the
   * local body into the wrapped MOM message.
   *
   * @exception MessageFormatException  If an error occurs while serializing.
   */
  protected void prepare() throws JMSException {
    super.prepare();

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(map);
      oos.flush();
      momMsg.body = baos.toByteArray();
      oos.close();
      baos.close();
    } catch (IOException exc) {
      MessageFormatException jExc =
        new MessageFormatException("The message body could not be serialized.");
      jExc.setLinkedException(exc);
      throw jExc;
    }
  }
}
