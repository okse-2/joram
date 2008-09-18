/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.kjoram;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Built-in facility for supporting application-defined property values.<br>
 * Property names must not be null, and must not be empty strings. If a
 * property name is set and it is either null or an empty string, an
 * IllegalArgumentException must be thrown.<br>
 * Property values can be boolean, byte, short, int, long, float, double,
 * and String. At this time, properties does nor support conversion between
 * types.
 */
public class Properties {
  private Hashtable hashtable = null;

  public Properties() {
    hashtable = new Hashtable();
  }

  public Properties(int capacity) {
    hashtable = new Hashtable(capacity);
  }

  int size() {
    return hashtable.size();
  }

  boolean isEmpty() {
    return hashtable.isEmpty();
  }

  void clear() {
    hashtable.clear();
  }


  boolean getBooleanProperty(String name) throws MessageFormatException {
    Object value = hashtable.get(name);
    if (value == null) {
      return false;
    } else if (value instanceof Boolean) {
      return ((Boolean) value).booleanValue();
    } else if (value instanceof String) {
      String str = (String) value;
      if (str.equals("true"))
        return true;
      else if (str.equals("false"))
        return false;
    }
    throw new MessageFormatException("Can't converted to Boolean");
  }

  byte getByteProperty(String name) throws MessageFormatException {
    Object value = hashtable.get(name);
    if (value == null) {
      throw new MessageFormatException("Can't converted to byte");
    } else if (value instanceof Byte) {
      return ((Byte) value).byteValue();
    } else if (value instanceof String) {
      return Byte.parseByte((String) value);
    }
    throw new NumberFormatException("Can't converted to byte");
  }

  double getDoubleProperty(String name) throws MessageFormatException {
    Object value = hashtable.get(name);
    if (value == null) {
      throw new MessageFormatException("Can't converted to double");
    } else if (value instanceof Double) {
      return ((Double) value).doubleValue();
    } else if (value instanceof String) {
      return Double.parseDouble((String) value);
    }
    throw new MessageFormatException("Can't converted to double");
  }

  float getFloatProperty(String name) throws MessageFormatException {
    Object value = hashtable.get(name);
    if (value == null) {
      throw new MessageFormatException("Can't converted to float");
    } else if (value instanceof Float) {
      return ((Float) value).floatValue();
    } else if (value instanceof String) {
      return Float.parseFloat((String) value);
    }
    throw new MessageFormatException("Can't converted to float");
  }

  int getIntProperty(String name) throws MessageFormatException {
    Object value = hashtable.get(name);
    if (value == null) {
      throw new MessageFormatException("Can't converted to integer");
    } else if (value instanceof Integer) {
      return ((Integer) value).intValue();
    } else if (value instanceof String) {
      return Integer.parseInt((String) value);
    }
    throw new MessageFormatException("Can't converted to integer");
  }

  long getLongProperty(String name) throws MessageFormatException {
    Object value = hashtable.get(name);
    if (value == null) {
      throw new MessageFormatException("Can't converted to long");
    } else if (value instanceof Long) {
      return ((Long) value).longValue();
    } else if (value instanceof String) {
      return Long.parseLong((String) value);
    }
    throw new MessageFormatException("Can't converted to long");
  }

  short getShortProperty(String name) throws MessageFormatException {
    Object value = hashtable.get(name);
    if (value == null) {
      throw new MessageFormatException("Can't converted to short");
    } else if (value instanceof Short) {
      return ((Short) value).shortValue();
    } else if (value instanceof String) {
      return Short.parseShort((String) value);
    }
    throw new MessageFormatException("Can't converted to short");
  }

  String getStringProperty(String name) throws MessageFormatException {
    Object value = hashtable.get(name);
    if (value == null) {
      return null;
    } else if (value instanceof String) {
      return (String) value;
    } else if (value instanceof Boolean) {
      if (((Boolean) value).booleanValue())
        return "true";
      return "false";
    } else if (value instanceof Byte) {
      return value.toString();
    } else if (value instanceof Double) {
      return value.toString();
    } else if (value instanceof Float) {
      return value.toString();
    } else if (value instanceof Integer) {
      return value.toString();
    } else if (value instanceof Long) {
      return value.toString();
    } else if (value instanceof Short) {
      return value.toString();
    } else {
      throw new MessageFormatException("Unknown type");
    }
  }

  void setBooleanProperty(String name, Boolean value) {
    hashtable.put(name, value);
  }

  void setBooleanProperty(String name, boolean value) {
    setBooleanProperty(name, new Boolean(value));
  }

  void setByteProperty(String name, Byte value) {
    hashtable.put(name, value);
  }

  void setByteProperty(String name, byte value) {
    setByteProperty(name, new Byte(value));
  }

  void setDoubleProperty(String name, Double value) {
   hashtable.put(name, value);
  }

  void setDoubleProperty(String name, double value) {
    setDoubleProperty(name, new Double(value));
  }

  void setFloatProperty(String name, Float value) {
   hashtable.put(name, value);
  }

  void setFloatProperty(String name, float value) {
    setFloatProperty(name, new Float(value));
  }

  void setIntProperty(String name, Integer value) {
   hashtable.put(name, value);
  }

  void setIntProperty(String name, int value) {
    setIntProperty(name, new Integer(value));
  }

  void setLongProperty(String name, Long value) {
   hashtable.put(name, value);
  }

  void setLongProperty(String name, long value) {
    setLongProperty(name, new Long(value));
  }

  void setShortProperty(String name, Short value) {
    hashtable.put(name, value);
  }

  void setShortProperty(String name, short value) {
    setShortProperty(name, new Short(value));
  }

  void setStringProperty(String name, String value) {
    hashtable.put(name, value);
  }

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputXStream os) throws IOException {
    os.writeInt(hashtable.size());
    for (Enumeration e = hashtable.keys(); e.hasMoreElements() ;) {
      String key = (String) e.nextElement();
      os.writeString(key);

      Object value= hashtable.get(key);      
      os.writeObject(value);
    }
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public static Properties readFrom(InputXStream is) throws IOException {
    int count = is.readInt();
    if (count == -1) return null;

    Properties p = new Properties(((4*count)/3) +1);

    String key;
    Object value;
    for (int i=0; i<count; i++) {
      key = is.readString();
      value = is.readObject();
      p.hashtable.put(key, value);
    }

    return p;
  }

  // for clone
  void put(String name, Object value) {
    hashtable.put(name, value);
  }
  
  public Properties clone() {
    Properties clone = new Properties(this.size());
    Enumeration e = hashtable.keys();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      clone.put(key, hashtable.get(key));
    }
    return clone;
  }
}
