/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007-2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.amqp.marshalling;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

public class AMQPStreamUtil {
  public static Logger logger = Debug.getLogger(AMQPStreamUtil.class.getName());
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
  private static final long INT_MASK = 0xffffffff;

  protected static long unsignedExtend(int value) {
    long extended = value;
    return extended & INT_MASK;
  }

  public static final String readShortstr(DataInputStream in)
      throws IOException {
    clearBits();
    byte[] b = new byte[in.readUnsignedByte()];
    in.readFully(b);
    return new String(b, "utf-8");
  }

  public static final LongString readLongstr(DataInputStream in)
      throws IOException {
    clearBits();
    final long contentLength = unsignedExtend(in.readInt());
    if (contentLength < Integer.MAX_VALUE) {
      final byte[] buffer = new byte[(int) contentLength];
      in.readFully(buffer);

      return LongStringHelper.asLongString(buffer);
    } else {
      throw new UnsupportedOperationException("Very long strings not currently supported");
    }
  }

  public static final byte[] readByteArray(DataInputStream in) throws IOException {
    clearBits();
    int size = in.readInt();
    if (size < 1)
      return null;
    byte[] tab = new byte[size];
    //in.readFully(tab);
    for (int i = 0; i < size; i++) {
      tab[i] = in.readByte();
    }
    return tab;
  }
  
  public static final byte[] readByteArray(DataInputStream in, int size) throws IOException {
    clearBits();
    if (size < 1)
      return null;
    byte[] tab = new byte[size];
    //in.readFully(tab);
    for (int i = 0; i < size; i++) {
      tab[i] = in.readByte();
    }
    return tab;
  }
  
  public static final int readShort(DataInputStream in) throws IOException {
    clearBits();
    return in.readShort();
  }

  public static final int readLong(DataInputStream in) throws IOException {
    clearBits();
    return in.readInt();
  }

  public static final long readLonglong(DataInputStream in) throws IOException {
    clearBits();
    return in.readLong();
  }

  public static final byte readByte(DataInputStream in) throws IOException {
    clearBits();
    return in.readByte();
  }

  public static final int readInt(DataInputStream in) throws IOException {
    clearBits();
    return in.readInt();
  }

  
  /** If we are reading one or more bits, holds the current packed collection of bits */
  private static int bits;
  /** If we are reading one or more bits, keeps track of which bit position we are reading from */
  private static int bit;

  /**
   * Private API - resets the bit group accumulator variables when
   * some non-bit argument value is to be read.
   */
  private static void clearBits() {
      bits = 0;
      bit = 0x100;
  }
  
  public static final boolean readBit(DataInputStream in) throws IOException {
    if (bit > 0x80) {
      bits = in.readUnsignedByte();
      bit = 0x01;
  }
  
  boolean result = (bits&bit) != 0;
  bit = bit << 1;
  return result;
  /*
    if (in.read() == 0)
      return true;
    else
      return false;
      */
  }

  public static final Map readTable(DataInputStream in) throws IOException {
    clearBits();
    if (in.available() < 1)
      return null;
    long tableLength = unsignedExtend(in.readInt());
    if (tableLength < 1)
      return null;
    Map table = new HashMap();

    DataInputStream tableIn = new DataInputStream(new TruncatedInputStream(in, tableLength));
    Object value = null;
    while (tableIn.available() > 0) {
      String name = readShortstr(tableIn);
      switch (tableIn.readUnsignedByte()) {
      case 'S':
        value = readLongstr(tableIn);
        break;
      case 'I':
        value = new Integer(tableIn.readInt());
        break;
      case 'D':
        int scale = tableIn.readUnsignedByte();
        byte[] unscaled = new byte[4];
        tableIn.readFully(unscaled);
        value = new BigDecimal(new BigInteger(unscaled), scale);
        break;
      case 'T':
        value = readTimestamp(tableIn);
        break;
      case 'F':
        value = readTable(tableIn);
        break;
      default:
        throw new MalformedFrameException("Unrecognised type in table");
      }

      if (!table.containsKey(name))
        table.put(name, value);
    }

    return table;
  }

  public static final int readOctet(DataInputStream in) throws IOException {
    clearBits();
    return in.readUnsignedByte();
  }

  public static final Date readTimestamp(DataInputStream in) throws IOException {
    clearBits();
    return new Date(in.readLong() * 1000);
  }
  
  // ===========================================================
  // ======   Write
  // ===========================================================
  public static final void writeShortstr(String str, DataOutputStream out)
      throws IOException {
    bitflush(out);
    byte[] bytes = str.getBytes("utf-8");
    out.writeByte(bytes.length);
    out.write(bytes);
  }

  public static final void writeLongstr(LongString str, DataOutputStream out)
      throws IOException {
    bitflush(out);
    writeLong((int) str.length(), out);
    InputStream in = str.getStream();
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    int count = 0;
    int n = 0;
    while (-1 != (n = in.read(buffer))) {
      out.write(buffer, 0, n);
      count += n;
    }
  }

  public static final void writeLongstr(String str, DataOutputStream out)
      throws IOException {
    bitflush(out);
    byte[] bytes = str.getBytes("utf-8");
    writeLong(bytes.length, out);
    out.write(bytes);
  }

  public static final void writeShort(short s, DataOutputStream out)
      throws IOException {
    bitflush(out);
    out.writeShort(s);
  }

  public static final void writeShort(int s, DataOutputStream out)
  throws IOException {
    bitflush(out);
    out.writeShort((short) s);
  }
  
  public static final void writeLong(int l, DataOutputStream out)
      throws IOException {
    bitflush(out);
    out.writeInt(l);
  }

  public static final void writeLonglong(long l, DataOutputStream out)
      throws IOException {
    bitflush(out);
    out.writeLong(l);
  }
  
  public static final void writeByteArray(byte[] byteArray, DataOutputStream out) throws IOException {
    bitflush(out);
    if (byteArray == null) {
      out.writeInt(0);
    } else if (byteArray.length == 0) {
      out.writeInt(0);
    } else {
      out.writeInt(byteArray.length);
      out.write(byteArray);
    }
  }

  public static final void writeByte(byte b, DataOutputStream out) throws IOException {
    bitflush(out);
    out.writeByte(b);
  }

  public static final void writeByte(int i, DataOutputStream out) throws IOException {
    bitflush(out);
    out.writeByte((byte) i);
  }
  
  public static final void writeInt(int i, DataOutputStream out) throws IOException {
    bitflush(out);
    out.writeInt(i);
  }
  
  
  /** When encoding one or more bits, records whether a group of bits is waiting to be written */
  private static boolean needBitFlush = false;
  /** The current group of bits */
  private static byte bitAccumulator;
  /** The current position within the group of bits */
  private static int bitMask;
   
  private static final void bitflush(DataOutputStream out) throws IOException {
    if (needBitFlush) {
      out.writeByte(bitAccumulator);
      needBitFlush = false;
      bitAccumulator = 0;
      bitMask = 1;
    }
  }

  public static final void writeBit(boolean b, DataOutputStream out)
  throws IOException {
    if (bitMask > 0x80) {
      bitflush(out);
    }
    if (b) {
      bitAccumulator |= bitMask;
    } else {
      // um, don't set the bit.
    }
    bitMask = bitMask << 1;
    needBitFlush = true;
    /*
    if (b)
      out.write(0);
    else
      out.write(1);
     */
  }

  public static final void writeTable(Map table, DataOutputStream out)
      throws IOException {
    bitflush(out);
    if (table == null) {
      // Convenience.
      out.writeInt(0);
    } else {
      out.writeInt((int) tableSize(table));
      for (Iterator entries = table.entrySet().iterator(); entries.hasNext();) {
        Map.Entry entry = (Map.Entry) entries.next();
        writeShortstr((String) entry.getKey(), out);
        Object value = entry.getValue();
        if (value instanceof String) {
          writeOctet('S', out);
          writeLongstr((String) value, out);
        } else if (value instanceof LongString) {
          writeOctet('S', out);
          writeLongstr((LongString) value, out);
        } else if (value instanceof Integer) {
          writeOctet('I', out);
          writeLong(((Integer) value).intValue(), out);
        } else if (value instanceof BigDecimal) {
          writeOctet('D', out);
          BigDecimal decimal = (BigDecimal) value;
          writeOctet(decimal.scale(), out);
          BigInteger unscaled = decimal.unscaledValue();
          if (unscaled.bitLength() > 32) /* Integer.SIZE in Java 1.5 */
            throw new IllegalArgumentException(
                "BigDecimal too large to be encoded");
          writeLong(decimal.unscaledValue().intValue(), out);
        } else if (value instanceof Date) {
          writeOctet('T', out);
          writeTimestamp((Date) value, out);
        } else if (value instanceof Map) {
          writeOctet('F', out);
          writeTable((Map) value, out);
        } else {
          throw new IllegalArgumentException("Invalid value type: "
              + value.getClass().getName() + " for key " + entry.getKey());
        }
      }
    }
  }

  public static final void writeOctet(int octet, DataOutputStream out)
      throws IOException {
    bitflush(out);
    out.writeByte(octet);
  }

  public static final void writeOctet(byte octet, DataOutputStream out)
      throws IOException {
    bitflush(out);
    out.writeByte(octet);
  }

  public static final void writeTimestamp(Date timestamp, DataOutputStream out)
      throws IOException {
    bitflush(out);
    // AMQP uses POSIX time_t which is in seconds since the epoc
    writeLonglong(timestamp.getTime() / 1000, out);
  }

  private static int shortStrSize(String str)
      throws UnsupportedEncodingException {
    return str.getBytes("utf-8").length + 1;
  }

  private static int longStrSize(String str) throws UnsupportedEncodingException {
    return str.getBytes("utf-8").length + 4;
  }

  private static long tableSize(Map table) throws UnsupportedEncodingException {
    long acc = 0;
    for (Iterator entries = table.entrySet().iterator(); entries.hasNext();) {
      Map.Entry entry = (Map.Entry) entries.next();
      acc += shortStrSize((String) entry.getKey());
      acc += 1;
      Object value = entry.getValue();
      if (value instanceof String) {
        acc += longStrSize((String) entry.getValue());
      } else if (value instanceof LongString) {
        acc += 4;
        acc += ((LongString) value).length();
      } else if (value instanceof Integer) {
        acc += 4;
      } else if (value instanceof BigDecimal) {
        acc += 5;
      } else if (value instanceof Date || value instanceof Timestamp) {
        acc += 8;
      } else if (value instanceof Map) {
        acc += 4;
        acc += tableSize((Map) value);
      } else {
        throw new IllegalArgumentException("invalid value in table");
      }
    }

    return acc;
  }
}
