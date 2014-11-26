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

import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.Vector;

public class InputXStream extends XStream {
  // The buffer where data is stored. 
  protected byte[] buffer;

  // The number of byte in buffer. 
  protected int count;

  // The index of the next character to read from the buffer.
  protected int pos;

  public InputXStream() {
    buffer = new byte[256];
    count = 0;
    pos = 0;
  }

  public InputXStream(byte[] buffer, int count) {
    this.buffer = buffer;
    this.count = count;
    pos = 0;
 }

  protected void read(InputStream is, int len) throws IOException {
    if (len > buffer.length) buffer = new byte[len];

    int cur = 0;
    int nb = -1;
    do {
      nb = is.read(buffer, cur, len-cur);
      if (nb < 0) throw new EOFException();
      cur += nb;
    } while (cur != len);

    pos = 0;
    count = len;
  }

  public void readFrom(InputStream is) throws IOException {
    read(is, 4);
    int len = readInt();
    read(is, len);
  }

  public void reset() {
    pos = 0;
  }

  public int size() {
    return count;
  }

  protected void check(int len) throws EOFException {
    if (buffer == null) throw new EOFException();
    if (pos + len > count) throw new EOFException();
  }
  /**
   * This method allows to restore a byte from the input stream.
   *
   * @return 	the byte 
   */
  public byte readByte() throws EOFException {
    check(1);
    byte b = (byte) (buffer[pos] & ((byte) 0xff));
    pos += 1;
    return b;
  }

  /**
   * This method allows to restore a boolean from the input stream.
   *
   * @return 	the boolean 
   */
  public boolean readBoolean() throws EOFException {
    byte b = readByte();
    return (b == TRUE);
  }

  /**
   * This method allows to restore a short from the input stream.
   *
   * @return 	the short 
   */
  public short readShort() throws EOFException {
    check(2);
    short s = (short) (((buffer[pos] &0xFF) << 8) | (buffer[pos+1] &0xFF));
    pos += 2;
    return s;
  }

  /**
   * This method allows to restore an integer from the input stream.
   *
   * @return 	the integer 
   */
  public int readInt() throws EOFException {
    check(4);
    int i = (((buffer[pos] &0xFF) << 24) | ((buffer[pos+1] &0xFF) << 16) |
             ((buffer[pos+2] &0xFF) << 8) | (buffer[pos+3] &0xFF));
    pos += 4;
    return i;
  }

  /**
   * This method allows to restore a long from the input stream.
   *
   * @return 	the long 
   */
  public long readLong() throws EOFException {
    check(8);
    long l = ((((long) buffer[0]) &0xFFL) << 56) |
      ((((long) buffer[1]) &0xFFL) << 48) |
      ((((long) buffer[2]) &0xFFL) << 40) |
      ((((long) buffer[3]) &0xFFL) << 32) |
      ((((long) buffer[4]) &0xFFL) << 24) |
      ((((long) buffer[5]) &0xFFL) << 16) |
      ((((long) buffer[6]) &0xFFL) << 8) |
      (((long) buffer[7]) &0xFFL);
    pos += 8;
    return l;
  }

  static final String EMPTY_STRING = "";

  /**
   * This method allows to restore a String from the input stream.
   *
   * @return 	the String object or null
   */
  public String readString() throws IOException {
    int length = readInt();
    if (length == -1) {
      return null;
    } else if (length == 0) {
      return EMPTY_STRING;
    } else if (length > 0) {
      check(length);
      String str =  new String(buffer, pos, length);
      pos += length;
      return str;
    } else {
      throw new IOException("bad string length");
    }
  }

  static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  /**
   * This method allows to restore a byte array from the input stream.
   *
   * @return 	the byte array object or null
   */
  public byte[] readByteArray() throws IOException {
    int length = readInt();
    if (length == -1) {
      return null;
    } else if (length == 0) {
      return EMPTY_BYTE_ARRAY;
    } else if (length > 0) {
      check(length);
      byte[] tab = new byte[length];
      System.arraycopy(buffer, pos, tab, 0, length);
      pos += length;
      return tab;
    } else {
      throw new IOException("bad array length");
    }
  }


  /**
   * This method allows to restore a float from the input stream.
   *
   * @return 	the float 
   */
  public float readFloat() throws EOFException {
    return Float.intBitsToFloat(readInt());
  }

  /**
   * This method allows to restore a double from the input stream.
   *
   * @param is	the stream to read data from in order to restore the object
   * @return 	the double 
   */
  public double readDouble() throws EOFException {
    return Double.longBitsToDouble(readLong());
  }

  /**
   * This method allows to restore an object from the input stream.
   *
   * @return 	the object or null
   */
  public Object readObject() throws IOException {
    byte type = readByte();
    switch (type) {
    case NULL:
      return null;
    case BOOLEAN:
      return new Boolean(readBoolean());
    case BYTE:
      return new Byte(readByte());
    case SHORT:
      return new Short(readShort());
    case INT:
      return new Integer(readInt());
    case LONG:
      return new Long(readLong());
    case FLOAT:
      return new Float(readFloat());
    case DOUBLE:
      return new Double(readDouble());
    case STRING:
      return readString();
    case BYTEARRAY:
      return readByteArray();
    default:
      throw new IOException("Bad primitive type"); 
    }
  }

  /**
   * This method allows to restore a Properties object from the input stream.
   *
   * @return 	the Properties object or null
   */
  public Properties readProperties() throws IOException {
    return Properties.readFrom(this);
  }

  /**
   * This method allows to restore a vector of String objects from the
   * input stream.
   *
   * @return 	the Properties object or null
   */
  public Vector readVectorOfString() throws IOException {
    int size = readInt();
    if (size == -1) {
      return null;
    } else {
      Vector v = new Vector(size);
      for (int i=0; i<size; i++) {
        v.addElement(readString());
      }
      return v;
    }
  }
}
