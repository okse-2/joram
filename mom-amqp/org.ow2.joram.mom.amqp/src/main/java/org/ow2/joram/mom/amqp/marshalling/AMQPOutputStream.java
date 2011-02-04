/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2011 ScalAgent Distributed Technologies
 * Copyright (C) 2008 - 2009 CNES
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
package org.ow2.joram.mom.amqp.marshalling;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.stream.StreamUtil;

public class AMQPOutputStream {

  public static Logger logger = Debug.getLogger(AMQPOutputStream.class.getName());

  private final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  private boolean needBitFlush = false;

  private byte bitAccumulator;

  private int bitMask = 1;

  private OutputStream out;

  public AMQPOutputStream(OutputStream outputStream) {
    this.out = outputStream;
  }
  
  public final void writeShortstr(String str) throws IOException {
    bitflush();
    if (str == null) {
      StreamUtil.writeTo((byte) 0, out);
    } else {
      byte[] bytes = str.getBytes("utf-8");
      StreamUtil.writeTo((byte) bytes.length, out);
      out.write(bytes);
    }
  }

  public final void writeLongstr(LongString str) throws IOException {
    bitflush();
    if (str == null) {
      writeLong(0);
    } else {
      writeLong((int) str.length());
      InputStream in = str.getStream();
      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
      int count = 0;
      int n = 0;
      while (-1 != (n = in.read(buffer))) {
        out.write(buffer, 0, n);
        count += n;
      }
    }

  }

  public final void writeShort(short s) throws IOException {
    bitflush();
    StreamUtil.writeTo(s, out);
  }

  public final void writeShort(int s) throws IOException {
    bitflush();
    StreamUtil.writeTo((short) s, out);
  }

  public final void writeLong(int l) throws IOException {
    bitflush();
    StreamUtil.writeTo(l, out);
  }

  public final void writeLonglong(long l) throws IOException {
    bitflush();
    StreamUtil.writeTo(l, out);
  }

  public final void writeByte(byte b) throws IOException {
    bitflush();
    StreamUtil.writeTo(b, out);
  }
  
  private final void bitflush() throws IOException {
    if (needBitFlush) {
      StreamUtil.writeTo(bitAccumulator, out);
      needBitFlush = false;
      bitAccumulator = 0;
      bitMask = 1;
    }
  }

  public final void writeBit(boolean b) throws IOException {
    if (bitMask > 0x80) {
      bitflush();
    }
    if (b) {
      bitAccumulator |= bitMask;
    }
    bitMask = bitMask << 1;
    needBitFlush = true;
  }

  public final void writeBoolean(boolean b) throws IOException {
    bitflush();
    int bool = b ? 1 : 0;
    StreamUtil.writeTo((byte) bool, out);
  }

  public final void writeTable(Map table) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "writeTable -> " + table);
    }
    bitflush();
    if (table == null) {
      StreamUtil.writeTo(0, out);
    } else {
      StreamUtil.writeTo((int) tableSize(table), out);
      for (Iterator entries = table.entrySet().iterator(); entries.hasNext();) {
        Map.Entry entry = (Map.Entry) entries.next();
        writeShortstr((String) entry.getKey());
        writeFieldValue(entry.getValue());
      }
    }
  }

  private void writeFieldValue(Object value) throws IOException {
    if (value == null) {
      writeOctet('V');
    } else if (value instanceof String) {
      writeOctet('s');
      writeShortstr((String) value);
    } else if (value instanceof LongString) {
      writeOctet('S');
      writeLongstr((LongString) value);
    } else if (value instanceof Integer) {
      writeOctet('I');
      writeLong(((Integer) value).intValue());
    } else if (value instanceof Short) {
      writeOctet('U');
      writeShort(((Short) value).shortValue());
    } else if (value instanceof Byte) {
      writeOctet('b');
      writeByte(((Byte) value).byteValue());
    } else if (value instanceof BigDecimal) {
      writeOctet('D');
      BigDecimal decimal = (BigDecimal) value;
      writeOctet(decimal.scale());
      BigInteger unscaled = decimal.unscaledValue();
      if (unscaled.bitLength() > 32) /* Integer.SIZE in Java 1.5 */
        throw new IllegalArgumentException("BigDecimal too large to be encoded");
      writeLong(decimal.unscaledValue().intValue());
    } else if (value instanceof Date) {
      writeOctet('T');
      writeTimestamp((Date) value);
    } else if (value instanceof Boolean) {
      writeOctet('t');
      writeBoolean(((Boolean) value).booleanValue());
    } else if (value instanceof Map) {
      writeOctet('F');
      writeTable((Map) value);
    } else if (value instanceof Long) {
      writeOctet('l');
      writeLonglong(((Long) value).longValue());
    } else if (value instanceof Double) {
      writeOctet('d');
      writeDouble(((Double) value).doubleValue());
    } else if (value instanceof Float) {
      writeOctet('f');
      writeFloat(((Float) value).floatValue());
    } else if (value instanceof Object[]) {
      writeOctet('A');
      writeArray((Object[]) value);
    } else {
      throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
    }
  }

  private void writeArray(Object[] array) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "writeArray -> " + Arrays.toString(array));
    }
    StreamUtil.writeTo(arraySize(array), out);

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "arrayLength -> " + arraySize(array));
    }

    for (int i = 0; i < array.length; i++) {
      writeFieldValue(array[i]);
    }
  }

  public final void writeFloat(float floatValue) throws IOException {
    bitflush();
    StreamUtil.writeTo(floatValue, out);
  }

  public final void writeDouble(double doubleValue) throws IOException {
    bitflush();
    StreamUtil.writeTo(doubleValue, out);
  }

  public final void writeOctet(int octet) throws IOException {
    bitflush();
    StreamUtil.writeTo((byte) octet, out);
  }

  public final void writeOctet(byte octet) throws IOException {
    bitflush();
    StreamUtil.writeTo(octet, out);
  }

  public final void writeTimestamp(Date timestamp) throws IOException {
    bitflush();
    // AMQP uses POSIX time_t which is in seconds since the epoch
    writeLonglong(timestamp.getTime() / 1000);
  }

  private int shortStrSize(String str) throws UnsupportedEncodingException {
    return str.getBytes("utf-8").length + 1;
  }

  private long tableSize(Map table) throws UnsupportedEncodingException {
    long acc = 0;
    for (Iterator entries = table.entrySet().iterator(); entries.hasNext();) {
      Map.Entry entry = (Map.Entry) entries.next();
      acc += shortStrSize((String) entry.getKey()); // key name
      acc += fieldValueSize(entry.getValue()); // key value
    }
    return acc;
  }

  private long fieldValueSize(Object value) throws UnsupportedEncodingException {
    long size = 1; // field value type
    if (value == null) {
      // 0
    } else if (value instanceof Byte || value instanceof Boolean) {
      size++;
    } else if (value instanceof String) {
      size += shortStrSize((String) value);
    } else if (value instanceof LongString) {
      size += 4;
      size += ((LongString) value).length();
    } else if (value instanceof Integer || value instanceof Float) {
      size += 4;
    } else if (value instanceof Short) {
      size += 2;
    } else if (value instanceof BigDecimal) {
      size += 5;
    } else if (value instanceof Long || value instanceof Date || value instanceof Timestamp
        || value instanceof Double) {
      size += 8;
    } else if (value instanceof Map) {
      size += 4;
      size += tableSize((Map) value);
    } else if (value instanceof Object[]) {
      size += 4;
      size += arraySize((Object[]) value);
    } else {
      throw new IllegalArgumentException("Invalid value in table: " + value.getClass().getName());
    }
    return size;
  }

  private int arraySize(Object[] value) throws UnsupportedEncodingException {
    int acc = 0;
    for (int i = 0; i < value.length; i++) {
      acc += fieldValueSize(value[i]);
    }
    return acc;
  }
}
