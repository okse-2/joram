/*
 * Copyright (C) 2002 - INRIA
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.kjoram.ksoap;

import java.util.Hashtable;

import com.scalagent.kjoram.excepts.MessageFormatException;

/**
 * The <code>ConversionHelper</code> class provides the methods for
 * getting the message properties and converting them as authorized by
 * the JMS specification.
 */
class ConversionHelper {
  static boolean getBoolean(Hashtable props, String name)
    throws MessageFormatException {
    if (props == null)
      return false;
    
    Object prop = props.get(name);
    if (prop == null)
      return false;
    else if (prop instanceof Boolean)
      return ((Boolean) prop).booleanValue();
    else if (prop instanceof String)
      return ((String) prop).equals("true");
    else
      throw new MessageFormatException("Type " + prop.getClass().getName()
                                       + " can't be converted to Boolean.");
  }

  static byte getByte(Hashtable props, String name)
    throws MessageFormatException {
    if (props == null)
      return Byte.parseByte(null);
    
    Object prop = props.get(name);
    if (prop == null)
      return Byte.parseByte(null);
    else if (prop instanceof Byte )
      return ((Byte) prop).byteValue();
    else if (prop instanceof String)
      return Byte.parseByte((String) prop);
    else
      throw new MessageFormatException("Type " + prop.getClass().getName()
                                       + " can't be converted to Byte.");
  }
  
  static short getShort(Hashtable props, String name)
    throws MessageFormatException {
    if (props == null)
      return Short.parseShort(null);
    
    Object prop = props.get(name);
    if (prop == null)
      return Short.parseShort(null);
    else if (prop instanceof Byte)
      return Short.parseShort(((Byte) prop).toString());
    else if (prop instanceof Short)
      return ((Short) prop).shortValue();
    else if (prop instanceof String)
      return Short.parseShort((String) prop);
    else
      throw new MessageFormatException("Type " + prop.getClass().getName()
                                       + " can't be converted to Short.");
  }

  static int getInt(Hashtable props, String name) 
    throws MessageFormatException {
    if (props == null)
      return Integer.parseInt(null);

    Object prop = props.get(name);
    if (prop == null)
      return Integer.parseInt(null);
    else if (prop instanceof Byte)
      return Integer.parseInt(((Byte) prop).toString());
    else if (prop instanceof Short)
      return Integer.parseInt(((Short) prop).toString());
    else if (prop instanceof Integer)
      return ((Integer) prop).intValue();
    else if (prop instanceof String)
      return Integer.parseInt((String) prop);
    else
      throw new MessageFormatException("Type " + prop.getClass().getName()
                                       + " can't be converted to Integer.");
  }

  static long getLong(Hashtable props, String name)
    throws MessageFormatException {
    if (props == null)
      return Long.parseLong(null);
    
    Object prop = props.get(name);
    if (prop == null)
      return Long.parseLong(null);
    else if (prop instanceof Byte)
      return Long.parseLong(((Byte) prop).toString());
    else if (prop instanceof Short)
      return Long.parseLong(((Short) prop).toString());
    else if (prop instanceof Integer)
      return Long.parseLong(((Integer) prop).toString());
    else if (prop instanceof Long)
      return ((Long) prop).longValue();
    else if (prop instanceof String)
      return Long.parseLong((String) prop);
    else
      throw new MessageFormatException("Type " + prop.getClass().getName()
                                       + " can't be converted to Long.");
  }


  static String getString(Hashtable props, String name) {
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
    throws MessageFormatException {
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
    throws MessageFormatException {
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
