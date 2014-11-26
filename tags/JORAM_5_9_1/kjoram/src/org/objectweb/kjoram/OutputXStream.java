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

import java.io.OutputStream;
import java.io.IOException;
import java.util.Vector;

public class OutputXStream extends XStream {
  /** The number of valid bytes in the buffer. */ 
  protected int count;

  /** The buffer where data is stored. */ 
  protected byte buffer[];

  protected void resize(int len) {
    int newcount = count + len;
    if (newcount > buffer.length) {
      int newlength = buffer.length*2;
      if (newcount > newlength) newlength = newcount;

      byte[] newbuf = new byte[newlength];
      System.arraycopy(buffer, 0, newbuf, 0, count);
      buffer = newbuf;
    }
  }

  protected void writeBuffer(byte[] buf) {
    resize(buf.length);
    System.arraycopy(buf, 0, buffer, count, buf.length);
    count += buf.length;
  }

  public OutputXStream() {
    count = 4;
    buffer = new byte[256];
  }

  public void reset() {
    count = 4;
  }

  public int size() {
    return count -4;
  }

  public void writeTo(OutputStream os) throws IOException {
    int len = size();

    buffer[0] = (byte) (len >>>  24);
    buffer[1] = (byte) (len >>>  16);
    buffer[2] = (byte) (len >>>  8);
    buffer[3] = (byte) (len >>>  0);

    try {
    os.write(buffer, 0, count);
    } catch (IOException e) {
      e.printStackTrace();
      throw e;
    }
    reset();
  }

  public void toBuffer(byte[] buf) {
    System.arraycopy(buffer, 4, buf, 0, count-4);
  }

  /**
   * This  method allows to write a boolean to the buffered output stream.
   *
   * @param b 	the boolean to write
   */
  public void writeBoolean(boolean b) {
    if (b)
      writeByte(TRUE);
    else
      writeByte(FALSE);
  }

  /**
   * This  method allows to write a byte to the buffered output stream.
   *
   * @param b 	the byte to write
   */
  public void writeByte(byte b) {
    resize(1);
    buffer[count] = b;
    count += 1;
  }

  /**
   * This  method allows to write a short to the buffered output stream.
   *
   * @param s 	the short to write
   */
  public void writeShort(short s) {
    resize(2);
    buffer[count] = (byte) (s >>>  8);
    buffer[count+1] = (byte) (s >>>  0);
    count += 2;
  }

  /**
   * This  method allows to write an integer to the buffered output stream.
   *
   * @param i 	the integer to write
   */
  public void writeInt(int i) {
    resize(4);
    buffer[count] = (byte) (i >>>  24);
    buffer[count+1] = (byte) (i >>>  16);
    buffer[count+2] = (byte) (i >>>  8);
    buffer[count+3] = (byte) (i >>>  0);
    count += 4;
  }

  /**
   * This  method allows to write a long to the output stream.
   *
   * @param l 	the long to write
   */
  public void writeLong(long l) {
    resize(8);
    buffer[count] = (byte) (l >>>  56);
    buffer[count+1] = (byte) (l >>>  48);
    buffer[count+2] = (byte) (l >>>  40);
    buffer[count+3] = (byte) (l >>>  32);
    buffer[count+4] = (byte) (l >>>  24);
    buffer[count+5] = (byte) (l >>>  16);
    buffer[count+6] = (byte) (l >>>  8);
    buffer[count+7] = (byte) (l >>>  0);
    count += 8;
  }

  /**
   * This  method allows to write a String to the buffered output stream.
   *
   * @param str	the String to write
   */
  public void writeString(String str) {
    if (str == null) {
      writeInt(-1);
    } else if (str.length() == 0) {
      writeInt(0);
    } else {
      byte[] buf = str.getBytes();
      writeInt(buf.length);
      writeBuffer(buf);
    }
  }

  /**
   * This  method allows to write byte array to the buffered output stream.
   *
   * @param tab	the byte array to write
   */
  public void writeByteArray(byte[] tab) {
    if (tab == null) {
      writeInt(-1);
    } else if (tab.length == 0) {
      writeInt(0);
    } else {
      writeInt(tab.length);
      writeBuffer(tab);
    }    
  }

  /**
   * This  method allows to write a float to the buffered output stream.
   *
   * @param f 	the float to write
   */
  public void writeFloat(float f) {
    writeInt(Float.floatToIntBits(f));
  }

  /**
   * This  method allows to write a double to the buffered output stream.
   *
   * @param d 	the double to write
   */
  public void writeDouble(double d) {
     writeLong(Double.doubleToLongBits(d));
  }

  /**
   * This  method allows to write an  object to the buffered output stream.
   *
   * @param obj the object to write
   */
  public void writeObject(Object obj) throws IOException {
    if (obj == null) {
      writeByte(NULL);
    } else if (obj instanceof Boolean) {
      writeByte(BOOLEAN);
      writeBoolean(((Boolean) obj).booleanValue());
    } else if (obj instanceof Byte) {
      writeByte(BYTE);
      writeByte(((Byte) obj).byteValue());
    } else if (obj instanceof Short) {
      writeByte(SHORT);
      writeShort(((Short) obj).shortValue());
    } else if (obj instanceof Integer) {
      writeByte(INT);
      writeInt(((Integer) obj).intValue());
    } else if (obj instanceof Long) {
      writeByte(LONG);
      writeLong(((Long) obj).longValue());
    } else if (obj instanceof Float) {
      writeByte(FLOAT);
      writeFloat(((Float) obj).floatValue());
    } else if (obj instanceof Double) {
      writeByte(DOUBLE);
      writeDouble(((Double) obj).doubleValue());
    } else if (obj instanceof String) {
      writeByte(STRING);
      writeString((String) obj);
    } else if (obj instanceof byte[]) {
      writeByte(BYTEARRAY);
      writeByteArray((byte[]) obj);
    } else {
      throw new IOException("Bad primitive type"); 
    }
  }

  /**
   * This  method allows to write a vector of String objects to the buffered
   * output stream.
   *
   * @param p 	the Properties object to write
   * @exception	a ClassCastException if the vector contains non String object.
   */
  public void writeVectorOfString(Vector v) {
    if (v == null) {
      writeInt(-1);
    } else {
      int size = v.size();
      writeInt(size);
      for (int i=0; i<size; i++) {
        writeString((String) v.elementAt(i));
      }
    }
  }

  /**
   * This  method allows to write a Properties object to the buffered
   * output stream.
   *
   * @param p 	the Properties object to write
   */
  public void writeProperties(Properties p) throws IOException {
    if (p == null) {
      writeInt(-1);
    } else {
      p.writeTo(this);
    }
  }
}
