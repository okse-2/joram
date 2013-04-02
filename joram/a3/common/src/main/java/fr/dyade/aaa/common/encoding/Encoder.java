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

public interface Encoder {

  void encodeBoolean(boolean bool) throws Exception;
  
  void encodeByte(byte b) throws Exception;
  
  void encodeSignedShort(short s) throws Exception;
  
  void encodeUnsignedShort(short s) throws Exception;
  
  void encode16(short s) throws Exception;
  
  void encodeSignedInt(int i) throws Exception;
  
  void encodeUnsignedInt(int i) throws Exception;
  
  void encode32(int i) throws Exception;
  
  void encodeSignedLong(long l) throws Exception;
  
  void encodeUnsignedLong(long l) throws Exception;
  
  void encode64(long l) throws Exception;
  
  void encodeNullableString(String str) throws Exception;
  
  void encodeString(String str) throws Exception;
  
  void encodeNullableByteArray(byte[] tab) throws Exception;
  
  void encodeByteArray(byte[] tab) throws Exception;
  
  void encodeNullableByteArray(byte[] tab, int offset, int length) throws Exception;
    
  void encodeByteArray(byte[] tab, int offset, int length) throws Exception;
  
  void encodeFloat(float f) throws Exception;
  
  void encodeDouble(double d) throws Exception;
  
}
