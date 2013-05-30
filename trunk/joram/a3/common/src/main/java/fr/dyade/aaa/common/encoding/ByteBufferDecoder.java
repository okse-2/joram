/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.common.encoding;

import java.nio.ByteBuffer;

/**
 * Decoder using a byte buffer.
 */
public class ByteBufferDecoder implements Decoder {
  
  private ByteBuffer buf;

  public ByteBufferDecoder(ByteBuffer buf) {
    super();
    this.buf = buf;
  }

  public short decodeSignedShort() throws Exception {
    return buf.getShort();
  }

  public short decodeUnsignedShort() throws Exception {
    return buf.getShort();
  }
  
  public short decode16() throws Exception {
    return buf.getShort();
  }

  public int decodeSignedInt() throws Exception {
    return buf.getInt();
  }

  public int decodeUnsignedInt() throws Exception {
    return buf.getInt();
  }
  
  public int decode32() throws Exception {
    return buf.getInt();
  }

  public long decodeUnsignedLong() throws Exception {
    return buf.getLong();
  }

  public long decodeSignedLong() throws Exception {
    return buf.getLong();
  }
  
  public long decode64() throws Exception {
    return buf.getLong();
  }
  
  private boolean isNull() throws Exception {
    return decodeBoolean();
  }

  public String decodeNullableString() throws Exception {
    if (isNull()) return null;
    return decodeString();
  }

  public String decodeString() throws Exception {
    int length = buf.getInt();
    return decodeString(length);
  }

  public String decodeString(int length) throws Exception {
    String s;
    if (buf.hasArray()) {
      s = new String(buf.array(), buf.position(), length);
      buf.position(buf.position() + length);
    } else {
      byte[] bytes = new byte[length];
      buf.get(bytes);
      s = new String(bytes);  
    }
    return s;
  }

  public byte decodeByte() throws Exception {
    return buf.get();
  }

  public byte[] decodeNullableByteArray() throws Exception {
    if (isNull()) return null;
    return decodeByteArray();
  }

  public byte[] decodeByteArray() throws Exception {
    int length = buf.getInt();
    return decodeByteArray(length);
  }

  public byte[] decodeByteArray(int length) throws Exception {
    byte[] bytes = new byte[length];
    buf.get(bytes);
    return bytes;
  }

  public boolean decodeBoolean() throws Exception {
    byte b = buf.get();
    return (b != 0);
  }

  public float decodeFloat() throws Exception {
    return buf.getFloat();
  }

  public double decodeDouble() throws Exception {
    return buf.getDouble();
  }

}
