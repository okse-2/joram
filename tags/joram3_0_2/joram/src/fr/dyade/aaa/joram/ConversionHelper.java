/*
 * Copyright (C) 2002 - INRIA
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
 */  
package fr.dyade.aaa.joram;

import java.util.Hashtable;

import javax.jms.MessageFormatException;

/**
 * The <code>ConversionHelper</code> class provides the methods for
 * getting the message properties and converting them as authorized by
 * the JMS specification.
 */
class ConversionHelper
{
  static boolean getBoolean(Hashtable props, String name)
               throws MessageFormatException
  {
    if (props == null)
      return Boolean.valueOf(null).booleanValue();

    Object prop = props.get(name);
    if (prop == null)
      return Boolean.valueOf(null).booleanValue();
    else if (prop instanceof Boolean)
      return ((Boolean) prop).booleanValue();
    else if (prop instanceof String)
      return Boolean.valueOf((String) prop).booleanValue();
    else
      throw new MessageFormatException("Type " + prop.getClass().getName()
                                       + " can't be converted to Boolean.");
  }
  
  static byte getByte(Hashtable props, String name)
            throws MessageFormatException
  {
    if (props == null)
      return Byte.valueOf(null).byteValue();

    Object prop = props.get(name);
    if (prop == null)
      return Byte.valueOf(null).byteValue();
    else if (prop instanceof Byte )
      return ((Byte) prop).byteValue();
    else if (prop instanceof String)      
      return Byte.valueOf((String) prop).byteValue();
    else
      throw new MessageFormatException("Type " + prop.getClass().getName() 
                                       + " can't be converted to Byte.");
  }
  
  static short getShort(Hashtable props, String name)
             throws MessageFormatException
  {
    if (props == null)
      return Short.valueOf(null).shortValue();

    Object prop = props.get(name);
    if (prop == null)
      return Short.valueOf(null).shortValue();
    else if (prop instanceof Byte || prop instanceof Short)
      return ((Number) prop).shortValue();
    else if (prop instanceof String)
      return Short.valueOf((String) prop).shortValue();
    else
      throw new MessageFormatException("Type " + prop.getClass().getName() 
                                       + " can't be converted to Short.");
  }
  
  static int getInt(Hashtable props, String name) throws MessageFormatException
  {
    if (props == null)
      return Integer.valueOf(null).intValue();

    Object prop = props.get(name);
    if (prop == null)
      return Integer.valueOf(null).intValue();
    else if (prop instanceof Byte || prop instanceof Short 
             || prop instanceof Integer)
      return ((Number) prop).intValue();
    else if (prop instanceof String)
      return Integer.valueOf((String) prop).intValue();
    else
      throw new MessageFormatException("Type " + prop.getClass().getName() 
                                       + " can't be converted to Integer.");
  }
  
  static long getLong(Hashtable props, String name)
            throws MessageFormatException
  {
    if (props == null)
      return Long.valueOf(null).longValue();

    Object prop = props.get(name);
    if (prop == null)
      return Long.valueOf(null).longValue();
    else if (prop instanceof Byte || prop instanceof Short 
             || prop instanceof Integer || prop instanceof Long)
      return ((Number) prop).longValue();
    else if (prop instanceof String)
      return Long.valueOf((String) prop).longValue();
    else
      throw new MessageFormatException("Type " + prop.getClass().getName() 
                                       + " can't be converted to Long.");
  }

  static float getFloat(Hashtable props, String name)
             throws MessageFormatException
  {
    if (props == null)
      return Float.valueOf(null).floatValue();

    Object prop = props.get(name);
    if (prop == null)
      return Float.valueOf(null).floatValue();
    else if (prop instanceof Float)
      return ((Float) prop).floatValue();
    else if (prop instanceof String)
      return Float.valueOf((String) prop).floatValue();
    else
      throw new MessageFormatException("Type " + prop.getClass().getName() 
                                       + " can't be converted to Float.");
  }
  
  static double getDouble(Hashtable props, String name)
              throws MessageFormatException
  {
    if (props == null)
      return Double.valueOf(null).doubleValue();

    Object prop = props.get(name);
    if (prop == null)
      return Double.valueOf(null).doubleValue();
    else if (prop instanceof Float || prop instanceof Double)
      return ((Number) prop).doubleValue();
    else if (prop instanceof String)
      return Double.valueOf((String) prop).doubleValue();
    else
      throw new MessageFormatException("Type " + prop.getClass().getName() 
                                       + " can't be converted to Double.");
  }

  static String getString(Hashtable props, String name)
  {
    if (props == null)
      return null;

    Object prop = props.get(name);
    if (prop == null)
      return null;
    else if (prop instanceof byte[])
      return new String((byte[]) prop);
    else
      return prop.toString();
  }

  static char getChar(Hashtable map, String name)
            throws MessageFormatException
  {
    Object value = map.get(name);
    if (value == null)
      return ((Character) null).charValue();
    else if (value instanceof Character)
      return ((Character) value).charValue();
    else
      throw new MessageFormatException("Type " + value.getClass().getName() 
                                       + " can't be converted to Character.");
  }

  static byte[] getBytes(Hashtable map, String name)
              throws MessageFormatException
  {
    Object value = map.get(name);
    if (value == null)
      return (byte[]) value;
    else if (value instanceof byte[])
      return (byte[]) value;
    else
      throw new MessageFormatException("Type " + value.getClass().getName() 
                                       + " can't be converted to byte[].");
  }
}  
