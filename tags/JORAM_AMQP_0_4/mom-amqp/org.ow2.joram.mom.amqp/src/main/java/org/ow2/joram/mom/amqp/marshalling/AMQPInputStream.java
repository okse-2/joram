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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.SyntaxErrorException;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.stream.StreamUtil;

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

  public final boolean readBoolean() throws IOException {
    clearBits();
    int bool = StreamUtil.readUnsignedByteFrom(in);
    return bool != 0;
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

  private final Object readFieldValue() throws IOException, SyntaxErrorException {
    Object value = null;
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
    case 'f':
      value = new Float(StreamUtil.readFloatFrom(in));
      break;
    case 'd':
      value = new Double(StreamUtil.readDoubleFrom(in));
      break;
    case 'b':
      value = new Byte(StreamUtil.readByteFrom(in));
      break;
    case 'l':
      value = new Long(StreamUtil.readLongFrom(in));
      break;
    case 't':
      value = new Boolean(readBoolean());
      break;
    case 'V':
      value = null;
      break;
    case 's':
      value = new Boolean(StreamUtil.readShortStringFrom(in));
      break;
    default:
      throw new SyntaxErrorException("Unrecognised object type in table: " + (char) type);
    }
    return value;
  }

  public final Map readTable() throws IOException, SyntaxErrorException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "readTable()");
    }

    clearBits();
    if (in.available() < 1)
      return null;
    long tableLength = StreamUtil.readUnsignedIntFrom(in);
    if (tableLength < 0)
      throw new SyntaxErrorException("Negative table size.");
    Map table = new HashMap();

    InputStream oldIn = in;
    in = new TruncatedInputStream(this.in, tableLength);
    Object value = null;
    while (in.available() > 0) {
      String name = readShortstr();
      value = readFieldValue();
      if (!table.containsKey(name))
        table.put(name, value);
    }

    in = oldIn;
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "readTable -> " + table);
    }
    return table;
  }

  private final Object[] readArray() throws IOException, SyntaxErrorException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "readArray()");
    }

    long arrayLength = StreamUtil.readIntFrom(in);
    if (arrayLength < 0)
      throw new SyntaxErrorException("Negative array size.");

    InputStream oldIn = in;
    in = new TruncatedInputStream(this.in, arrayLength);

    List<Object> list = new ArrayList<Object>();
    while (in.available() > 0) {
      list.add(readFieldValue());
    }

    in = oldIn;

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "readArray -> " + list);
    }
    return list.toArray();
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
