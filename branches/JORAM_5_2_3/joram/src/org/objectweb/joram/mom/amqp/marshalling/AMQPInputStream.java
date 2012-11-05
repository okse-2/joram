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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.joram.shared.stream.StreamUtil;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

public class AMQPInputStream {

  public static Logger logger = Debug.getLogger(AMQPInputStream.class.getName());

  private InputStream in;

  /**
   * If we are reading one or more bits, holds the current packed collection of
   * bits
   */
  private int bits;

  /**
   * If we are reading one or more bits, keeps track of which bit position we
   * are reading from
   */
  private int bit;

  private void clearBits() {
    bits = 0;
    bit = 0x100;
  }

  public AMQPInputStream(InputStream inputStream) {
    this.in = inputStream;
  }

  public final String readShortstr() throws IOException {
    clearBits();
    int length = StreamUtil.readUnsignedByteFrom(in);
    return new String(StreamUtil.readByteArrayFrom(in, length), "utf-8");
  }

  public final LongString readLongstr() throws IOException {
    clearBits();
    final long contentLength = StreamUtil.readUnsignedIntFrom(in);
    if (contentLength < Integer.MAX_VALUE) {
      return LongStringHelper.asLongString(StreamUtil.readByteArrayFrom(in, (int) contentLength));
    } else {
      throw new UnsupportedOperationException("Very long strings not currently supported");
    }
  }

  public final int readShort() throws IOException {
    clearBits();
    return StreamUtil.readShortFrom(in);
  }

  public final int readLong() throws IOException {
    clearBits();
    return StreamUtil.readIntFrom(in);
  }

  public final long readLonglong() throws IOException {
    clearBits();
    return StreamUtil.readLongFrom(in);
  }

  public final boolean readBit() throws IOException {
    if (bit > 0x80) {
      bits = StreamUtil.readUnsignedByteFrom(in);
      bit = 0x01;
    }

    boolean result = (bits & bit) != 0;
    bit = bit << 1;
    return result;
  }

  public final Map readTable() throws IOException {
    clearBits();
    if (in.available() < 1)
      return null;
    long tableLength = StreamUtil.readUnsignedIntFrom(in);
    if (tableLength < 1)
      return null;
    Map table = new HashMap();

    InputStream oldIn = in;
    in = new TruncatedInputStream(this.in, tableLength);
    Object value = null;
    while (in.available() > 0) {
      String name = readShortstr();
      int type = StreamUtil.readUnsignedByteFrom(in);
      switch (type) {
      case 'S':
        value = readLongstr();
        break;
      case 'I':
        value = new Integer(StreamUtil.readIntFrom(in));
        break;
      case 'D':
        int scale = StreamUtil.readUnsignedByteFrom(in);
        byte[] unscaled = StreamUtil.readByteArrayFrom(in, 4);
        value = new BigDecimal(new BigInteger(unscaled), scale);
        break;
      case 'T':
        value = readTimestamp();
        break;
      case 'F':
        value = readTable();
        break;

      // Taken from AMQP 0.9.1 spec
      case 'B':
        value = new Byte(StreamUtil.readByteFrom(in));
        break;
      case 'U':
        value = new Short(StreamUtil.readShortFrom(in));
        break;
      case 'L':
        value = new Long(StreamUtil.readLongFrom(in));
        break;
      case 'A':
        value = readArray();
        break;
      default:
        throw new MalformedFrameException("Unrecognised type in table: " + type);
      }

      if (!table.containsKey(name))
        table.put(name, value);
    }

    in = oldIn;
    return table;
  }

  public final Object[] readArray() throws IOException {

    long arrayLength = StreamUtil.readUnsignedIntFrom(in);
    if (arrayLength < 1)
      return null;

    //    DataInputStream in = new DataInputStream(new TruncatedInputStream(in, arrayLength));
    InputStream oldIn = in;
    in = new TruncatedInputStream(this.in, arrayLength);

    int typeCode = StreamUtil.readUnsignedByteFrom(in);
    int count = StreamUtil.readIntFrom(in);

    Object[] value = null;
    switch (typeCode) {
    case 'S':
      value = new LongString[count];
      for (int i = 0; i < count; i++) {
        value[i] = readLongstr();
      }
      break;
    case 'I':
      value = new Integer[count];
      for (int i = 0; i < count; i++) {
        value[i] = new Integer(StreamUtil.readIntFrom(in));
      }
      break;
    case 'U':
      value = new Short[count];
      for (int i = 0; i < count; i++) {
        value[i] = new Short(StreamUtil.readShortFrom(in));
      }
      break;
    case 'B':
      value = new Byte[count];
      for (int i = 0; i < count; i++) {
        value[i] = new Byte(StreamUtil.readByteFrom(in));
      }
      break;
    case 'D':
      value = new BigDecimal[count];
      for (int i = 0; i < count; i++) {
        int scale = StreamUtil.readUnsignedByteFrom(in);
        byte[] unscaled = StreamUtil.readByteArrayFrom(in, 4);
        value[i] = new BigDecimal(new BigInteger(unscaled), scale);
      }
      break;
    case 'T':
      value = new Date[count];
      for (int i = 0; i < count; i++) {
        value[i] = readTimestamp();
      }
      break;
    case 'F':
      value = new HashMap[count];
      for (int i = 0; i < count; i++) {
        value[i] = readTable();
      }
      break;
    case 'L':
      value = new Long[count];
      for (int i = 0; i < count; i++) {
        value[i] = new Long(StreamUtil.readLongFrom(in));
      }
      break;
    case 'A':
      value = new Object[count][];
      for (int i = 0; i < count; i++) {
        value[i] = readArray();
      }
      break;
    default:
      throw new MalformedFrameException("Unrecognised type in table: " + typeCode);
    }

    if (in.available() > 0) {
      throw new IOException("Incorrect array size.");
    }
    in = oldIn;
    
    return value;
  }

  public final int readOctet() throws IOException {
    clearBits();
    return StreamUtil.readUnsignedByteFrom(in);
  }

  public final Date readTimestamp() throws IOException {
    clearBits();
    return new Date(StreamUtil.readLongFrom(in) * 1000);
  }

}
