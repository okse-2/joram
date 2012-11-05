/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2008 CNES
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.joram.shared.stream.StreamUtil;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

public class AMQPOutputStream {

  public static Logger logger = Debug.getLogger(AMQPOutputStream.class.getName());

  private final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  private boolean needBitFlush = false;

  private byte bitAccumulator;

  private int bitMask;

  private OutputStream out;

  public AMQPOutputStream(OutputStream outputStream) {
    this.out = outputStream;
  }
  
  public final void writeShortstr(String str) throws IOException {
    bitflush();
    byte[] bytes = str.getBytes("utf-8");
    StreamUtil.writeTo((byte) bytes.length, out);
    out.write(bytes);
  }

  public final void writeLongstr(LongString str) throws IOException {
    bitflush();
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

  public final void writeLongstr(String str) throws IOException {
    bitflush();
    byte[] bytes = str.getBytes("utf-8");
    writeLong(bytes.length);
    out.write(bytes);
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

  public final void writeTable(Map table) throws IOException {
    bitflush();
    if (table == null) {
      StreamUtil.writeTo(0, out);
    } else {
      StreamUtil.writeTo((int) tableSize(table), out);
      for (Iterator entries = table.entrySet().iterator(); entries.hasNext();) {
        Map.Entry entry = (Map.Entry) entries.next();
        writeShortstr((String) entry.getKey());
        Object value = entry.getValue();
        if (value instanceof String) {
          writeOctet('S');
          writeLongstr((String) value);
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
          writeOctet('B');
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
        } else if (value instanceof Map) {
          writeOctet('F');
          writeTable((Map) value);
        } else if (value instanceof Long) {
          writeOctet('L');
          writeLonglong(((Long) value).longValue());
        } else if (value instanceof Object[]) {
          writeOctet('A');
          writeArray((Object[]) value);
        } else {
          throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName()
              + " for key " + entry.getKey());
        }
      }
    }
  }

  private void writeArray(Object[] value) throws IOException {
    int len = value.length;

    StreamUtil.writeTo(arraySize(value), out);

    if (value instanceof String[]) {
      writeOctet('S');
      StreamUtil.writeTo(len, out);
      for (int i = 0; i < len; i++) {
        writeLongstr((String) value[i]);
      }
    } else if (value instanceof LongString[]) {
      writeOctet('S');
      StreamUtil.writeTo(len, out);
      for (int i = 0; i < len; i++) {
        writeLongstr((LongString) value[i]);
      }
    } else if (value instanceof Integer[]) {
      writeOctet('I');
      StreamUtil.writeTo(len, out);
      for (int i = 0; i < len; i++) {
        writeLong(((Integer) value[i]).intValue());
      }
    } else if (value instanceof Short[]) {
      writeOctet('U');
      StreamUtil.writeTo(len, out);
      for (int i = 0; i < len; i++) {
        writeShort(((Short) value[i]).shortValue());
      }
    } else if (value instanceof Byte[]) {
      writeOctet('B');
      StreamUtil.writeTo(len, out);
      for (int i = 0; i < len; i++) {
        writeByte(((Byte) value[i]).byteValue());
      }
    } else if (value instanceof BigDecimal[]) {
      writeOctet('D');
      StreamUtil.writeTo(len, out);
      for (int i = 0; i < len; i++) {
        BigDecimal decimal = (BigDecimal) value[i];
        StreamUtil.writeTo((byte) decimal.scale(), out);
        BigInteger unscaled = decimal.unscaledValue();
        if (unscaled.bitLength() > 32) /* Integer.SIZE in Java 1.5 */
          throw new IllegalArgumentException("BigDecimal too large to be encoded");
        StreamUtil.writeTo(decimal.unscaledValue().intValue(), out);
      }
    } else if (value instanceof Date[]) {
      writeOctet('T');
      StreamUtil.writeTo(len, out);
      for (int i = 0; i < len; i++) {
        writeTimestamp((Date) value[i]);
      }
    } else if (value instanceof Long[]) {
      writeOctet('L');
      StreamUtil.writeTo(len, out);
      for (int i = 0; i < len; i++) {
        writeLonglong(((Long) value[i]).longValue());
      }
    } else if (value instanceof Map[]) {
      writeOctet('F');
      StreamUtil.writeTo(len, out);
      for (int i = 0; i < len; i++) {
        writeTable((Map) value[i]);
      }
    } else if (value instanceof Object[][]) {
      writeOctet('A');
      StreamUtil.writeTo(len, out);
      for (int i = 0; i < len; i++) {
        writeArray((Object[]) value[i]);
      }
    } else {
      throw new IllegalArgumentException("invalid value in table: " + value.getClass().getName());
    }
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
    // AMQP uses POSIX time_t which is in seconds since the epoc
    writeLonglong(timestamp.getTime() / 1000);
  }

  private int shortStrSize(String str) throws UnsupportedEncodingException {
    return str.getBytes("utf-8").length + 1;
  }

  private int longStrSize(String str) throws UnsupportedEncodingException {
    return str.getBytes("utf-8").length + 4;
  }

  private long tableSize(Map table) throws UnsupportedEncodingException {
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
      } else if (value instanceof Short) {
        acc += 2;
      } else if (value instanceof BigDecimal) {
        acc += 5;
      } else if (value instanceof Long || value instanceof Date || value instanceof Timestamp) {
        acc += 8;
      } else if (value instanceof Map) {
        acc += 4;
        acc += tableSize((Map) value);
      } else if (value instanceof Object[]) {
        acc += 4;
        acc += arraySize((Object[]) value);
      } else {
        throw new IllegalArgumentException("invalid value in table");
      }
    }

    return acc;
  }

  private int arraySize(Object[] value) throws UnsupportedEncodingException {
    int acc = 0;
    acc += 1;
    acc += 4;
    int len = value.length;
    if (value instanceof String[]) {
      for (int i = 0; i < len; i++) {
        acc += longStrSize((String) value[i]);
      }
    } else if (value instanceof LongString[]) {
      for (int i = 0; i < len; i++) {
        acc += 4;
        acc += ((LongString) value[i]).length();
      }
    } else if (value instanceof Integer[]) {
      acc += 4 * len;
    } else if (value instanceof Short[]) {
      acc += 2 * len;
    } else if (value instanceof Byte[]) {
      acc += len;
    } else if (value instanceof BigDecimal[]) {
      acc += 5 * len;
    } else if (value instanceof Long[] || value instanceof Date[] || value instanceof Timestamp[]) {
      acc += 8 * len;
    } else if (value instanceof Map[]) {
      for (int i = 0; i < len; i++) {
        acc += 4;
        acc += tableSize((Map) value[i]);
      }
    } else if (value instanceof Object[][]) {
      acc += arraySize(value);
    } else {
      throw new IllegalArgumentException("invalid value in table: " + value.getClass().getName());
    }
    return acc;
  }
}
