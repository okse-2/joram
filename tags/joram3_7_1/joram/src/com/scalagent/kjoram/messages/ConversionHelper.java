/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Jeff Mesnil (INRIA)
 * Contributor(s): Frederic Maistre (INRIA), Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram.messages;

import com.scalagent.kjoram.excepts.MessageValueException;

/**
 * The <code>ConversionHelper</code> class is used for converting values
 * carried by messages into specified types, if possible.
 */
public class ConversionHelper
{
  /**
   * Gets the boolean value of the given object.
   *
   * @exception MessageValueException  If the given object can't be converted
   *              into a boolean value.
   */
  public static boolean toBoolean(Object value) throws MessageValueException
  {
    if (value == null)
      return Boolean.valueOf(null).booleanValue();

    if (value instanceof Boolean)
      return ((Boolean) value).booleanValue();
    else if (value instanceof String)
      return Boolean.valueOf((String) value).booleanValue();
    else
      throw new MessageValueException("Type " + value.getClass().getName()
                                      + " can't be converted to Boolean.");
  }
 
  /**
   * Gets the byte value of the given object.
   *
   * @exception MessageValueException  If the given object can't be converted
   *              into a byte value.
   */ 
  public static byte toByte(Object value) throws MessageValueException
  {
    if (value == null)
      return Byte.valueOf(null).byteValue();

    if (value instanceof Byte )
      return ((Byte) value).byteValue();
    else if (value instanceof String)      
      return Byte.valueOf((String) value).byteValue();
    else
      throw new MessageValueException("Type " + value.getClass().getName() 
                                      + " can't be converted to Byte.");
  }
 
  /**
   * Gets the short value of the given object.
   *
   * @exception MessageValueException  If the given object can't be converted
   *              into a short value.
   */ 
  public static short toShort(Object value) throws MessageValueException
  {
    if (value == null)
      return Short.valueOf(null).shortValue();

    if (value instanceof Byte)
      return ((Byte) value).shortValue();
    else if (value instanceof Short)
      return ((Short) value).shortValue();
    else if (value instanceof String)
      return Short.valueOf((String) value).shortValue();
    else
      throw new MessageValueException("Type " + value.getClass().getName() 
                                      + " can't be converted to Short.");
  }
 
  /**
   * Gets the int value of the given object.
   *
   * @exception MessageValueException  If the given object can't be converted
   *              into an int value.
   */ 
  public static int toInt(Object value) throws MessageValueException
  {
    if (value == null)
      return Integer.valueOf(null).intValue();

    if (value instanceof Byte)
      return ((Byte) value).intValue();
    else if (value instanceof Short )
      return ((Short) value).intValue();
    else if (value instanceof Integer)
      return ((Integer) value).intValue();
    else if (value instanceof String)
      return Integer.valueOf((String) value).intValue();
    else
      throw new MessageValueException("Type " + value.getClass().getName() 
                                      + " can't be converted to Integer.");
  }
 
  /**
   * Gets the long value of the given object.
   *
   * @exception MessageValueException  If the given object can't be converted
   *              into a long value.
   */ 
  public static long toLong(Object value) throws MessageValueException
  {
    if (value == null)
      return Long.valueOf(null).longValue();

    if (value instanceof Byte)
      return ((Byte) value).longValue();
    else if (value instanceof Short)
      return ((Short) value).longValue();
    else if (value instanceof Integer)
      return ((Integer) value).longValue();
    else if (value instanceof Long)
      return ((Long) value).longValue();
    else if (value instanceof String)
      return Long.valueOf((String) value).longValue();
    else
      throw new MessageValueException("Type " + value.getClass().getName() 
                                      + " can't be converted to Long.");
  }

  /**
   * Gets the float value of the given object.
   *
   * @exception MessageValueException  If the given object can't be converted
   *              into a float value.
   */
  public static float toFloat(Object value) throws MessageValueException
  {
    if (value == null)
      return Float.valueOf(null).floatValue();

    if (value instanceof Float)
      return ((Float) value).floatValue();
    else if (value instanceof String)
      return Float.valueOf((String) value).floatValue();
    else
      throw new MessageValueException("Type " + value.getClass().getName() 
                                      + " can't be converted to Float.");
  }
 
  /**
   * Gets the double value of the given object.
   *
   * @exception MessageValueException  If the given object can't be converted
   *              into a double value.
   */ 
  public static double toDouble(Object value) throws MessageValueException
  {
    if (value == null)
      return Double.valueOf(null).doubleValue();

    if (value instanceof Float)
      return ((Float) value).doubleValue();
    else if (value instanceof Double)
      return ((Double) value).doubleValue();
    else if (value instanceof String)
      return Double.valueOf((String) value).doubleValue();
    else
      throw new MessageValueException("Type " + value.getClass().getName() 
                                      + " can't be converted to Double.");
  }

  /** Gets the String value of the given object. */
  public static String toString(Object value)
  {
    if (value == null)
      return null;

    if (value instanceof byte[])
      return new String((byte[]) value);
    else
      return value.toString();
  }

  /**
   * Gets the char value of the given object.
   *
   * @exception MessageValueException  If the given object can't be converted
   *              into a char value.
   */
  public static char toChar(Object value) throws MessageValueException
  {
    if (value == null)
      return ((Character) null).charValue();
    else if (value instanceof Character)
      return ((Character) value).charValue();
    else
      throw new MessageValueException("Type " + value.getClass().getName() 
                                      + " can't be converted to Character.");
  }

  /**
   * Gets the bytes value of the given object.
   *
   * @exception MessageValueException  If the given object can't be converted
   *              into a bytes array.
   */
  public static byte[] toBytes(Object value) throws MessageValueException
  {
    if (value == null)
      return (byte[]) value;
    else if (value instanceof byte[])
      return (byte[]) value;
    else
      throw new MessageValueException("Type " + value.getClass().getName() 
                                      + " can't be converted to byte[].");
  }
}  
