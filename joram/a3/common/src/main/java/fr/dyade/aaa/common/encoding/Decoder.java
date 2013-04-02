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

public interface Decoder {

  short decodeSignedShort() throws Exception;
  
  short decodeUnsignedShort() throws Exception;
  
  short decode16() throws Exception;
  
  int decodeSignedInt() throws Exception;
  
  int decodeUnsignedInt() throws Exception;
  
  int decode32() throws Exception;
  
  long decodeUnsignedLong() throws Exception;
  
  long decodeSignedLong() throws Exception;
  
  long decode64() throws Exception;
  
  String decodeNullableString() throws Exception;
  
  String decodeString() throws Exception;
  
  String decodeString(int length) throws Exception;
  
  byte decodeByte() throws Exception;
  
  byte[] decodeNullableByteArray() throws Exception;
  
  byte[] decodeByteArray() throws Exception;
  
  byte[] decodeByteArray(int length) throws Exception;
  
  boolean decodeBoolean() throws Exception;
  
  float decodeFloat() throws Exception;
  
  double decodeDouble() throws Exception;
  
}
