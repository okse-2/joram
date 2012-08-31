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
 * <p>
 * A MapMessage object is used to send a set of name-value pairs. The names are String objects, and
 * the values are primitive Java data types. The names must have a value that is not null, and not an
 * empty string. MapMessage inherits from the Message interface and adds a message body that contains
 * a Map.
 * <p>
 * The primitive types can be read or written explicitly using methods for each type. They may also be
 * read or written generically as objects. For instance, a call to MapMessage.setInt("foo", 6) is equivalent
 * to MapMessage.setObject("foo", new Integer(6)). 
 * <p>
 * When a client receives a MapMessage, it is in read-only mode. If a client attempts to write to the message
 * at this point, a MessageNotWriteableException is thrown. If clearBody is called, the message can now be both
 * read from and written to.
 * <p>
 * MapMessage objects support conversions (see table below). Unsupported conversions must throw a JMSException.
 * The String-to-primitive conversions may throw a runtime exception if the primitive's valueOf() method does not
 * accept it as a valid String representation of the primitive.
 * <p>
 * 
 * <p>
 * A value written as the row type can be read as the column type.
 * <p>
 * 
 * |        | boolean byte short char int long float double String byte[]
 * +--------+-------------------------------------------------------------
 * |boolean |    X                                            X
 * |byte    |          X     X         X   X                  X
 * |short   |                X         X   X                  X
 * |char    |                     X                           X
 * |int     |                          X   X                  X
 * |long    |                              X                  X
 * |float   |                                    X     X      X
 * |double  |                                          X      X
 * |String  |    X     X     X         X   X     X     X      X
 * |byte[]  |                                                        X
 * +----------------------------------------------------------------------
 * <p>
 * Attempting to read a null value as a primitive type must be treated as calling the primitive's corresponding valueOf(String) conversion method with a null value. Since char does not support a String conversion, attempting to read a null value as a char must throw a NullPointerException. 
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
   * Clears out the message body.
   * <p>
   * Calling this method leaves the message body in the same state as an empty body in
   * a newly created message.

   * @exception JMSException  Actually never thrown.
   */
  public void clearBody() throws JMSException {
    super.clearBody();
    map.clear();
  }


  /** 
   * API method.
   * Sets a boolean value with the specified name into the Map.
   * 
   * @param name  the name of the boolean.
   * @param value the boolean value to set in the Map
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */
  public void setBoolean(String name, boolean value) throws JMSException {
    setObject(name, new Boolean(value));
  }
 
  /** 
   * API method.
   * Sets a byte value with the specified name into the Map.
   * 
   * @param name  the name of the byte.
   * @param value the byte value to set in the Map
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setByte(String name, byte value) throws JMSException {
    setObject(name, new Byte(value));
  }
 
  /** 
   * API method.
   * Sets a byte array value with the specified name into the Map.
   * 
   * @param name  the name of the byte array.
   * @param value the byte array value to set in the Map; the array is copied so that
   *              the value for name will not be altered by future modifications
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void setBytes(String name, byte[] value) throws JMSException {
    setObject(name, value);
  }
 
  /** 
   * API method.
   * Sets a portion of a byte array value with the specified name into the Map.
   * 
   * @param name    the name of the byte array.
   * @param value   the byte array value to set in the Map; the array is copied so that
   *                the value for name will not be altered by future modifications.
   * @param offset  the initial offset within the byte array.
   * @param length  the number of bytes to use.
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
   * Sets a char value with the specified name into the Map.
   * 
   * @param name  the name of the char.
   * @param value the char value to set in the Map
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setChar(String name, char value) throws JMSException {
    setObject(name, new Character(value));
  }
 
  /** 
   * API method.
   * Sets a double value with the specified name into the Map.
   * 
   * @param name  the name of the double.
   * @param value the double value to set in the Map
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setDouble(String name, double value) throws JMSException {
    setObject(name, new Double(value));
  }
 
  /** 
   * API method.
   * Sets a float value with the specified name into the Map.
   * 
   * @param name  the name of the float.
   * @param value the float value to set in the Map
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void setFloat(String name, float value) throws JMSException {
    setObject(name, new Float(value));
  }
 
  /** 
   * API method.
   * Sets a int value with the specified name into the Map.
   * 
   * @param name  the name of the int.
   * @param value the int value to set in the Map
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */  
  public void setInt(String name, int value) throws JMSException {
    setObject(name, new Integer(value));
  }
 
  /** 
   * API method.
   * Sets a long value with the specified name into the Map.
   * 
   * @param name  the name of the long.
   * @param value the long value to set in the Map
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */ 
  public void setLong(String name, long value) throws JMSException {
    setObject(name, new Long(value));
  }
  
  /** 
   * API method.
   * Sets a short value with the specified name into the Map.
   * 
   * @param name  the name of the short.
   * @param value the short value to set in the Map
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */  
  public void setShort(String name, short value) throws JMSException {
    setObject(name, new Short(value));
  }
 
  /** 
   * API method.
   * Sets a String value with the specified name into the Map.
   * 
   * @param name  the name of the String.
   * @param value the String value to set in the Map
   *
   * @exception MessageNotWriteableException  If the message body is read-only.
   */   
  public void setString(String name, String value) throws JMSException {
    setObject(name, value);
  }

  /** 
   * API method.
   * Sets an object alue with the specified name into the Map.
   * <p>
   * This method works only for the objectified primitive object types (Integer, Double,
   * Long ...), String objects, and byte arrays.
   * 
   * @param name  the name of the object.
   * @param value the Java object value to set in the Map
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
   * Returns the boolean value to which the specified key is mapped.
   * 
   * @param name the key whose associated value is to be returned.
   * @return the boolean value associated with the specified name.
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
   * Returns the byte value to which the specified key is mapped.
   * 
   * @param name the key whose associated value is to be returned.
   * @return the byte value associated with the specified name.
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
   * Returns the byte array value to which the specified key is mapped.
   * 
   * @param name the key whose associated value is to be returned.
   * @return a copy of the byte array value with the specified name; if there is no
   *         item by this name, a null value is returned.
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
   * Returns the char value to which the specified key is mapped.
   * 
   * @param name the key whose associated value is to be returned.
   * @return the char value associated with the specified name.
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
   * Returns the double value to which the specified key is mapped.
   * 
   * @param name the key whose associated value is to be returned.
   * @return the double value associated with the specified name.
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
   * Returns the float value to which the specified key is mapped.
   * 
   * @param name the key whose associated value is to be returned.
   * @return the float value associated with the specified name.
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
   * Returns the int value to which the specified key is mapped.
   * 
   * @param name the key whose associated value is to be returned.
   * @return the int value associated with the specified name.
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
   * Returns the long value to which the specified key is mapped.
   * 
   * @param name the key whose associated value is to be returned.
   * @return the long value associated with the specified name.
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
   * This method returns in objectified format a java primitive type that had
   * been stored in the Map with the equivalent setObject method call, or its
   * equivalent primitive settype method.
   * 
   * @param name the key whose associated value is to be returned.
   * @return a copy of the Java object value with the specified name, in objectified
   *         format (for example, if the object was set as an int, an Integer is returned);
   *         if there is no item by this name, a null value is returned.
   *
   * @exception JMSException  Actually never thrown.
   */   
  public Object getObject(String name) throws JMSException {
    return map.get(name);
  }

  /**
   * API method.
   * Returns the short value to which the specified key is mapped.
   * 
   * @param name the key whose associated value is to be returned.
   * @return the short value associated with the specified name.
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
   * Returns the String value to which the specified key is mapped.
   * 
   * @param name the key whose associated value is to be returned.
   * @return the String value associated with the specified name; if there is no item
   *         by this name, a null value is returned..
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
   * Returns an Enumeration of all the names in this MapMessage.
   * 
   * @return an enumeration of all the names in this MapMessage.
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
