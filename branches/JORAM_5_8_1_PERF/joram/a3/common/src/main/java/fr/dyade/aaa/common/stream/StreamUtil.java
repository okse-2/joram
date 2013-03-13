/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2010 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.common.stream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;


public final class StreamUtil {
  // Per-thread buffer for conversion
  private static ThreadLocal perThreadBuffer = new ThreadLocal() {
    protected synchronized Object initialValue() {
      return new byte[8];
    }
  };

  // Be careful, the buffer will be reused..
  // JORAM_PERF_BRANCH: public method
  public static byte[] readFully(int length, InputStream is) throws IOException {
    int count = 0;
    byte[] buf = (byte[]) (perThreadBuffer.get());
    if (length > buf.length) buf = new byte[length];
    
    int nb = -1;
    do {
      nb = is.read(buf, count, length-count);
      if (nb < 0) throw new EOFException();
      count += nb;
    } while (count != length);
    return buf;
  }

  private static void readFully(byte[] buf, InputStream is) throws IOException {
    int count = 0;
    
    int nb = -1;
    do {
      nb = is.read(buf, count, buf.length-count);
      if (nb < 0) throw new EOFException();
      count += nb;
    } while (count != buf.length);
  }

  public static final int TRUE = 0;
  public static final int FALSE = 1;

  /**
   * This  method allows to write a boolean to the output stream.
   *
   * @param b 	the boolean to write
   * @param os 	the stream to write the object to
   */
  public static void writeTo(boolean b, OutputStream os) throws IOException {
    if (b)
      os.write(TRUE);
    else
      os.write(FALSE);
  }

  /**
   * This method allows to restore a boolean from the input stream.
   *
   * @param is	the stream to read data from in order to restore the object
   * @return 	the boolean 
   */
  public static boolean readBooleanFrom(InputStream is) throws IOException {
    if (is.read() == TRUE)
      return true;
    return false;
  }

  /**
   * This  method allows to write a byte to the output stream.
   *
   * @param b 	the byte to write
   * @param os 	the stream to write the object to
   */
  public static void writeTo(byte b, OutputStream os) throws IOException {
    os.write(b);
  }

  /**
   * This method allows to restore a byte from the input stream.
   *
   * @param is	the stream to read data from in order to restore the object
   * @return 	the byte 
   */
  public static byte readByteFrom(InputStream is) throws IOException {
    return (byte) is.read();
  }
  
  /**
   * This method allows to restore a byte from the input stream.
   *
   * @param is  the stream to read data from in order to restore the object
   * @return    the byte 
   */
  public static int readUnsignedByteFrom(InputStream is) throws IOException {
    return is.read();
  }

  /**
   * This  method allows to write a short to the output stream.
   *
   * @param s 	the short to write
   * @param os 	the stream to write the object to
   */
  public static void writeTo(short s, OutputStream os) throws IOException {
    byte[] buf = (byte[]) (perThreadBuffer.get());
    buf[0] = (byte) (s >>>  8);
    buf[1] = (byte) (s >>>  0);
    os.write(buf, 0, 2);
  }

  /**
   * This method allows to restore a short from the input stream.
   *
   * @param is	the stream to read data from in order to restore the object
   * @return 	the short 
   */
  public static short readShortFrom(InputStream is) throws IOException {
    byte[] buf = readFully(2, is);
    return (short) (((buf[0] &0xFF) << 8) | (buf[1] &0xFF));
  }

  /**
   * This  method allows to write an integer to the output stream.
   *
   * @param i 	the integer to write
   * @param os 	the stream to write the object to
   */
  public static void writeTo(int i, OutputStream os) throws IOException {
    byte[] buf = (byte[]) (perThreadBuffer.get());
    buf[0] = (byte) (i >>>  24);
    buf[1] = (byte) (i >>>  16);
    buf[2] = (byte) (i >>>  8);
    buf[3] = (byte) (i >>>  0);
    os.write(buf, 0, 4);
  }

  /**
   * This method allows to restore an integer from the input stream.
   *
   * @param is	the stream to read data from in order to restore the object
   * @return 	the integer 
   */
  public static int readIntFrom(InputStream is) throws IOException {
    byte[] buf = readFully(4, is);
    return (((buf[0] &0xFF) << 24) | ((buf[1] &0xFF) << 16) |
            ((buf[2] &0xFF) << 8) | (buf[3] &0xFF));
  }
  
  /**
   * This method allows to restore an integer from the input stream.
   *
   * @param is  the stream to read data from in order to restore the object
   * @return    the integer 
   */
  public static long readUnsignedIntFrom(InputStream is) throws IOException {
    byte[] buf = readFully(4, is);
    return (((buf[0] & 0xFFL) << 24) | ((buf[1] & 0xFF) << 16) |
            ((buf[2] &0xFF) << 8) | (buf[3] &0xFF));
  }

  /**
   * This  method allows to write a long to the output stream.
   *
   * @param l 	the long to write
   * @param os 	the stream to write the object to
   */
  public static void writeTo(long l, OutputStream os) throws IOException {
    byte[] buf = (byte[]) (perThreadBuffer.get());
    buf[0] = (byte) (l >>>  56);
    buf[1] = (byte) (l >>>  48);
    buf[2] = (byte) (l >>>  40);
    buf[3] = (byte) (l >>>  32);
    buf[4] = (byte) (l >>>  24);
    buf[5] = (byte) (l >>>  16);
    buf[6] = (byte) (l >>>  8);
    buf[7] = (byte) (l >>>  0);
    os.write(buf, 0, 8);
  }

  /**
   * This method allows to restore a long from the input stream.
   *
   * @param is	the stream to read data from in order to restore the object
   * @return 	the long 
   */
  public static long readLongFrom(InputStream is) throws IOException {
    byte[] buf = readFully(8, is);    
    return ((buf[0] & 0xFFL) << 56) | ((buf[1] & 0xFFL) << 48) | ((buf[2] & 0xFFL) << 40)
        | ((buf[3] & 0xFFL) << 32) | ((buf[4] & 0xFFL) << 24) | ((buf[5] & 0xFFL) << 16)
        | ((buf[6] & 0xFFL) << 8) | (buf[7] & 0xFFL);
  }

  /**
   * This  method allows to write a float to the output stream.
   *
   * @param f 	the float to write
   * @param os 	the stream to write the object to
   */
  public static void writeTo(float f, OutputStream os) throws IOException {
    writeTo(Float.floatToIntBits(f), os);
  }

  /**
   * This method allows to restore a float from the input stream.
   *
   * @param is	the stream to read data from in order to restore the object
   * @return 	the float 
   */
  public static float readFloatFrom(InputStream is) throws IOException {
    return Float.intBitsToFloat(readIntFrom(is));
  }

  /**
   * This  method allows to write a double to the output stream.
   *
   * @param d 	the double to write
   * @param os 	the stream to write the object to
   */
  public static void writeTo(double d, OutputStream os) throws IOException {
    writeTo(Double.doubleToLongBits(d), os);
  }

  /**
   * This method allows to restore a double from the input stream.
   *
   * @param is	the stream to read data from in order to restore the object
   * @return 	the double 
   */
  public static double readDoubleFrom(InputStream is) throws IOException {
    return Double.longBitsToDouble(readLongFrom(is));
  }

  /**
   * This  method allows to write a String to the output stream.
   *
   * @param str	the String to write
   * @param os 	the stream to write the object to
   */
  public static void writeTo(String str, OutputStream os) throws IOException {
    if (str == null) {
      writeTo(-1, os);
    } else if (str.length() == 0) {
      writeTo(0, os);
    } else {
      byte[] buf = str.getBytes();
      writeTo(buf.length, os);
      os.write(buf);
    }
  }

  static final String EMPTY_STRING = "";

  /**
   * This method allows to restore a String from the input stream.
   *
   * @param is	the stream to read data from in order to restore the object
   * @return 	the String object or null
   */
  public static String readStringFrom(InputStream is) throws IOException {
    int length = readIntFrom(is);
    if (length == -1) {
      return null;
    } else if (length == 0) {
      return EMPTY_STRING;
    } else if (length > 0) {
      byte[] tab = readFully(length, is);
      return new String(tab, 0, length);
    } else {
      throw new IOException("bad string length");
    }
  }
  
  /**
   * This method allows to restore a short String from the input stream. The
   * maximum length for a short String is 255 characters.
   * 
   * @param is
   *          the stream to read data from in order to restore the object
   * @return the String object or null
   */
  public static String readShortStringFrom(InputStream is) throws IOException {
    int length = readUnsignedByteFrom(is);
    if (length == -1) {
      return null;
    } else if (length == 0) {
      return EMPTY_STRING;
    } else if (length > 0) {
      byte[] tab = readFully(length, is);
      return new String(tab, 0, length);
    } else {
      throw new IOException("bad short string length");
    }
  }

  static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  /**
   * This  method allows to write byte array to the output stream.
   *
   * @param tab	the byte array to write
   * @param os 	the stream to write the object to
   */
  public static void writeTo(byte[] tab, OutputStream os) throws IOException {
    if (tab == null) {
      writeTo(-1, os);
    } else if (tab.length == 0) {
      writeTo(0, os);
    } else {
      writeTo(tab.length, os);
      os.write(tab);
    }    
  }

  /**
   * This method allows to restore a byte array from the input stream.
   *
   * @param is	the stream to read data from in order to restore the object
   * @return 	the byte array object or null
   */
  public static byte[] readByteArrayFrom(InputStream is) throws IOException {
    int length = readIntFrom(is);
    return readByteArrayFrom(is, length);
  }

  /**
   * This method allows to restore a byte array from the input stream.
   * 
   * @param is
   *          the stream to read data from in order to restore the object
   * @param length
   *          the length of bytes to read
   * @return the byte array object or null
   */
  public static byte[] readByteArrayFrom(InputStream is, int length) throws IOException {
    if (length == -1) {
      return null;
    } else if (length == 0) {
      return EMPTY_BYTE_ARRAY;
    } else if (length > 0) {
      byte[] tab = new byte[length];
      readFully(tab, is);
      return tab;
    } else {
      throw new IOException("bad array length");
    }
  }

  static final byte NULL = -1;
  static final byte BOOLEAN = 1;
  static final byte BYTE = 2;
  static final byte SHORT = 3;
  static final byte INT = 4;
  static final byte LONG = 5;
  static final byte FLOAT = 6;
  static final byte DOUBLE = 7;
  static final byte STRING = 8;
  static final byte BYTEARRAY = 9;

  /**
   * This  method allows to write an  object to the output stream.
   *
   * @param obj the object to write
   * @param os 	the stream to write the object to
   */
  public static void writeObjectTo(Object obj, OutputStream os) throws IOException {
    if (obj == null) {
      writeTo(NULL, os);
    } else if (obj instanceof Boolean) {
      writeTo(BOOLEAN, os);
      writeTo(((Boolean) obj).booleanValue(), os);
    } else if (obj instanceof Byte) {
      writeTo(BYTE, os);
      writeTo(((Byte) obj).byteValue(), os);
    } else if (obj instanceof Short) {
      writeTo(SHORT, os);
      writeTo(((Short) obj).shortValue(), os);
    } else if (obj instanceof Integer) {
      writeTo(INT, os);
      writeTo(((Integer) obj).intValue(), os);
    } else if (obj instanceof Long) {
      writeTo(LONG, os);
      writeTo(((Long) obj).longValue(), os);
    } else if (obj instanceof Float) {
      writeTo(FLOAT, os);
      writeTo(((Float) obj).floatValue(), os);
    } else if (obj instanceof Double) {
      writeTo(DOUBLE, os);
      writeTo(((Double) obj).doubleValue(), os);
    } else if (obj instanceof String) {
      writeTo(STRING, os);
      writeTo((String) obj, os);
    } else if (obj instanceof byte[]) {
      writeTo(BYTEARRAY, os);
      writeTo((byte[]) obj, os);
    } else {
      throw new InvalidClassException("Bad primitive type"); 
    }
  }
  
  // JORAM_PERF_BRANCH
  public static int getEncodedSize(Object obj) throws IOException {
    int size = 0;
    if (obj == null) {
      size += 1;
    } else if (obj instanceof Boolean) {
      size += 1 + 1;
    } else if (obj instanceof Byte) {
      size += 1 + 1;
    } else if (obj instanceof Short) {
      size += 1 + 2;
    } else if (obj instanceof Integer) {
      size += 1 + 4;
    } else if (obj instanceof Long) {
      size += 1 + 8;
    } else if (obj instanceof Float) {
      size += 1 + 4;
    } else if (obj instanceof Double) {
      size += 1 + 8;
    } else if (obj instanceof String) {
      size += 1 + 4 + ((String) obj).length();
    } else if (obj instanceof byte[]) {
      size += 1 + 4 + ((byte[]) obj).length;
    } else {
      throw new InvalidClassException("Bad primitive type"); 
    }
    return size;
  }

  /**
   * This method allows to restore an object from the input stream.
   *
   * @param is	the stream to read data from in order to restore the object
   * @return 	the object or null
   */
  public static Object readObjectFrom(InputStream is) throws IOException {
    byte type = readByteFrom(is);
    switch (type) {
    case NULL:
      return null;
    case BOOLEAN:
      return new Boolean(readBooleanFrom(is));
    case BYTE:
      return new Byte(readByteFrom(is));
    case SHORT:
      return new Short(readShortFrom(is));
    case INT:
      return new Integer(readIntFrom(is));
    case LONG:
      return new Long(readLongFrom(is));
    case FLOAT:
      return new Float(readFloatFrom(is));
    case DOUBLE:
      return new Double(readDoubleFrom(is));
    case STRING:
      return readStringFrom(is);
    case BYTEARRAY:
      return readByteArrayFrom(is);
    default:
      throw new InvalidClassException("Bad primitive type"); 
    }
  }

  /**
   * This  method allows to write a Properties object to the output stream.
   *
   * @param p 	the Properties object to write
   * @param os 	the stream to write the object to
   */
  public static void writeTo(Properties p, OutputStream os) throws IOException {
    if (p == null) {
      writeTo(-1, os);
    } else {
      p.writeTo(os);
    }
  }
  
  // JORAM_PERF_BRANCH
  public static int getEncodedSize(Properties p) throws IOException {
    int size = 0;
    if (p == null) {
      size += 4;
    } else {
      size += p.getEncodedSize();
    }
    return size;
  }

  /**
   * This method allows to restore a Properties object from the input stream.
   *
   * @param is	the stream to read data from in order to restore the object
   * @return 	the Properties object or null
   */
  public static Properties readPropertiesFrom(InputStream is) throws IOException {
      return Properties.readFrom(is);
  }

  /**
   * This method allows to write a generic list of String objects to the
   * output stream.
   * 
   * @param v the List object to write
   * @param os the stream to write the object to
   */
  public static void writeListOfStringTo(List v, OutputStream os) throws IOException {
    if (v == null) {
      writeTo(-1, os);
    } else {
      int size = v.size();
      writeTo(size, os);
      for (int i = 0; i < size; i++) {
        writeTo((String) v.get(i), os);
      }
    }
  }

  /**
   * This method allows to restore a vector of String objects from the
   * input stream.
   * 
   * @param is the stream to read data from in order to restore the object
   * @return the Vector object or null
   */
  public static Vector readVectorOfStringFrom(InputStream is) throws IOException {
    int size = readIntFrom(is);
    if (size == -1) return null;

    Vector v = new Vector(size);
    for (int i=0; i<size; i++) {
      v.addElement(readStringFrom(is));
    }
    return v;
  }
  
  // JORAM_PERF_BRANCH
  public static int getEncodedSize(Vector v) throws IOException {
    int size = 0;
    size += 4;
    for (int i=0; i < v.size(); i++) {
      size += 4 + ((String) v.get(i)).length();
    }
    return size;
  }

  /**
   * This method allows to restore a list of String objects from the
   * input stream.
   * 
   * @param is the stream to read data from in order to restore the object
   * @return the ArrayList object or null
   */
  public static ArrayList readArrayListOfStringFrom(InputStream is) throws IOException {
    int size = readIntFrom(is);
    if (size == -1)
      return null;

    ArrayList v = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      v.add(readStringFrom(is));
    }
    return v;
  }

  /**
   * This method allows to write a String array to the output stream.
   * 
   * @param array the String array to write
   * @param os the stream to write to
   */
  public static void writeArrayOfStringTo(String[] array, OutputStream os) throws IOException {
    if (array == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = array.length;
      StreamUtil.writeTo(size, os);
      for (int i=0; i< size; i++) {
        StreamUtil.writeTo(array[i], os);
      }
    }
  }
  
  /**
   * This method allows to restore a String array from the input stream.
   *
   * @param is  the stream to read data from in order to restore the String array
   * @return    the String array or null
   */
  public static String[] readArrayOfStringFrom(InputStream is) throws IOException {
    int size = StreamUtil.readIntFrom(is);
    String[] array = null;
    if (size != -1) {
      array = new String[size];
      for (int i=0; i< size; i++) {
        array[i] = StreamUtil.readStringFrom(is);
      }
    }
    return array;
  }

  /**
   * This  method allows to write a int array to the output stream.
   *
   * @param array   the int array to write
   * @param os      the stream to write to
   */
  public static void writeArrayOfIntTo(int[] array, OutputStream os) throws IOException {
    if (array == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = array.length;
      StreamUtil.writeTo(size, os);
      for (int i=0; i< size; i++) {
        StreamUtil.writeTo(array[i], os);
      }
    }
  }
  
  /**
   * This method allows to restore a int array from the input stream.
   *
   * @param is  the stream to read data from in order to restore the int array
   * @return    the int array or null
   */
  public static int[] readArrayOfIntFrom(InputStream is) throws IOException {
    int size = StreamUtil.readIntFrom(is);
    int[] array = null;
    if (size != -1) {
      array = new int[size];
      for (int i=0; i< size; i++) {
        array[i] = StreamUtil.readIntFrom(is);
      }
    }
    return array;
  }
  
  /**
   * This  method allows to write a boolean array to the output stream.
   *
   * @param array   the boolean array to write
   * @param os      the stream to write to
   */
  public static void writeArrayOfBooleanTo(boolean[] array, OutputStream os) throws IOException {
    if (array == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = array.length;
      StreamUtil.writeTo(size, os);
      for (int i=0; i< size; i++) {
        StreamUtil.writeTo(array[i], os);
      }
    }
  }
  
  /**
   * This method allows to restore a boolean array from the input stream.
   *
   * @param is  the stream to read data from in order to restore the boolean array
   * @return    the boolean array or null
   */
  public static boolean[] readArrayOfBooleanFrom(InputStream is) throws IOException {
    int size = StreamUtil.readIntFrom(is);
    boolean[] array = null;
    if (size != -1) {
      array = new boolean[size];
      for (int i=0; i< size; i++) {
        array[i] = StreamUtil.readBooleanFrom(is);
      }
    }
    return array;
  }
  
  /**
   * This  method allows to write a java.util.Properties object to the output stream.
   *
   * @param p   the java.util.Properties object to write
   * @param os  the stream to write the object to
   */
  public static void writeTo(java.util.Properties p, OutputStream os) throws IOException {
    if (p == null) {
      writeTo(-1, os);
    } else {
      writeTo(p.size(), os);
      for (Enumeration keys = p.keys(); keys.hasMoreElements(); ) {
        String key = (String) keys.nextElement();
        StreamUtil.writeTo(key, os);
        String value = p.getProperty(key);
        StreamUtil.writeTo(value, os);
      }
    }
  }

  /**
   * This method allows to restore a java.util.Properties object from the input stream.
   *
   * @param is  the stream to read data from in order to restore the object
   * @return  the java.util.Properties object or null
   */
  public static java.util.Properties readJPropertiesFrom(InputStream is) throws IOException {
    int size = StreamUtil.readIntFrom(is);
    java.util.Properties prop = null;
    if (size != -1) {
      prop = new java.util.Properties();
      String key;
      String value;
      for (int i=0; i< size; i++) {
        key = StreamUtil.readStringFrom(is);
        value = StreamUtil.readStringFrom(is);
        prop.put(key, value);
      }
    }
    return prop;
  }

}
