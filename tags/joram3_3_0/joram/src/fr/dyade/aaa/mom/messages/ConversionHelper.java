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
 * Initial developer(s): Jeff Mesnil (INRIA)
 * Contributor(s): Frederic Maistre (INRIA)
 */
package fr.dyade.aaa.mom.messages;

import fr.dyade.aaa.mom.excepts.MessageValueException;

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

    if (value instanceof Byte || value instanceof Short)
      return ((Number) value).shortValue();
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

    if (value instanceof Byte || value instanceof Short 
        || value instanceof Integer)
      return ((Number) value).intValue();
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

    if (value instanceof Byte || value instanceof Short 
        || value instanceof Integer || value instanceof Long)
      return ((Number) value).longValue();
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

    if (value instanceof Float || value instanceof Double)
      return ((Number) value).doubleValue();
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
