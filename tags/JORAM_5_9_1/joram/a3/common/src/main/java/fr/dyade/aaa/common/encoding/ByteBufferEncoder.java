/*
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.common.encoding;

import java.nio.ByteBuffer;

/**
 * Encoder using a byte buffer.
 */
public class ByteBufferEncoder implements Encoder {
  
  private ByteBuffer buf;

  public ByteBufferEncoder(ByteBuffer buf) {
    super();
    this.buf = buf;
  }

  public void encodeBoolean(boolean bool) throws Exception {
    buf.put(bool ? (byte) 1 : 0);
  }

  public void encodeByte(byte b) throws Exception {
    buf.put(b);
  }

  public void encodeSignedShort(short s) throws Exception {
    buf.putShort(s);
  }

  public void encodeUnsignedShort(short s) throws Exception {
    buf.putShort(s);
  }
  
  public void encode16(short s) throws Exception {
    buf.putShort(s);
  }

  public void encodeSignedInt(int i) throws Exception {
    buf.putInt(i);
  }

  public void encodeUnsignedInt(int i) throws Exception {
    buf.putInt(i);
  }
  
  public void encode32(int i) throws Exception {
    buf.putInt(i);
  }

  public void encodeSignedLong(long l) throws Exception {
    buf.putLong(l);
  }

  public void encodeUnsignedLong(long l) throws Exception {
    buf.putLong(l);
  }
  
  public void encode64(long l) throws Exception {
    buf.putLong(l);
  }
  
  private void encodeNullFlag(Object o) throws Exception {
    if (o == null) {
      encodeBoolean(true);
    } else {
      encodeBoolean(false);
    }
  }

  public void encodeNullableString(String str) throws Exception {
    encodeNullFlag(str);
    if (str != null) {
      encodeString(str);
    }
  }

  public void encodeString(String str) throws Exception {
    byte[] bytes = str.getBytes();
    buf.putInt(bytes.length);
    buf.put(bytes);
  }

  public void encodeNullableByteArray(byte[] tab) throws Exception {
    encodeNullFlag(tab);
    if (tab != null) {
      encodeByteArray(tab);
    }
  }

  public void encodeByteArray(byte[] tab) throws Exception {
    buf.putInt(tab.length);
    buf.put(tab);
  }

  public void encodeNullableByteArray(byte[] tab, int offset, int length)
      throws Exception {
    encodeNullFlag(tab);
    if (tab != null) {
      encodeByteArray(tab, offset, length);
    }
  }

  public void encodeByteArray(byte[] tab, int offset, int length)
      throws Exception {
    buf.putInt(length);
    buf.put(tab, offset, length);
  }

  public void encodeFloat(float f) throws Exception {
    buf.putFloat(f);
  }

  public void encodeDouble(double d) throws Exception {
    buf.putDouble(d);
  }

}
