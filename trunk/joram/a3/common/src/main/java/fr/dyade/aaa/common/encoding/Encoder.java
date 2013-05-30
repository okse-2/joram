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

  /**
   * Encodes a boolean.
   * @param bool the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeBoolean(boolean bool) throws Exception;
  
  /**
   * Encodes a byte.
   * @param b the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeByte(byte b) throws Exception;
  
  /**
   * Encodes a signed short integer. The encoding format
   * depends on the implementation.
   * @param s the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeSignedShort(short s) throws Exception;
  
  /**
   * Encodes an unsigned short integer. The encoding format
   * depends on the implementation.
   * @param s the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeUnsignedShort(short s) throws Exception;
  
  /**
   * Encodes a signed short integer. The encoding format is the
   * 2's complement representation encoded in 2 bytes.
   * @param s the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encode16(short s) throws Exception;
  
  /**
   * Encodes a signed integer. The encoding format
   * depends on the implementation.
   * @param i the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeSignedInt(int i) throws Exception;
  
  /**
   * Encodes an unsigned integer. The encoding format
   * depends on the implementation.
   * @param i the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeUnsignedInt(int i) throws Exception;
  
  /**
   * Encodes a signed integer. The encoding format is the
   * 2's complement representation encoded in 4 bytes.
   * @param i the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encode32(int i) throws Exception;
  
  /**
   * Encodes a signed long integer. The encoding format
   * depends on the implementation.
   * @param l the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeSignedLong(long l) throws Exception;
  
  /**
   * Encodes an unsigned long integer. The encoding format
   * depends on the implementation.
   * @param l the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeUnsignedLong(long l) throws Exception;
  
  /**
   * Encodes a signed long integer. The encoding format is the
   * 2's complement representation encoded in 8 bytes.
   * @param l the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encode64(long l) throws Exception;
  
  /**
   * Encodes a string that can be null.
   * @param str the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeNullableString(String str) throws Exception;

  /**
   * Encodes a string that cannot be null.
   * @param str the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeString(String str) throws Exception;
  
  /**
   * Encodes a byte array that can be null.
   * @param b the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeNullableByteArray(byte[] b) throws Exception;
  
  /**
   * Encodes a byte array that cannot be null.
   * @param b the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeByteArray(byte[] b) throws Exception;
  
  /**
   * Encodes a byte array that can be null.
   * @param b the value to encode
   * @param offset the starting index in array b
   * @param length the number of bytes to be encoded
   * @throws Exception if an error occurs during encoding
   */
  void encodeNullableByteArray(byte[] b, int offset, int length) throws Exception;
    
  /**
   * Encodes a byte array that cannot be null.
   * @param b the value to encode
   * @param offset the starting index in array b
   * @param length the number of bytes to be encoded
   * @throws Exception if an error occurs during encoding
   */
  void encodeByteArray(byte[] b, int offset, int length) throws Exception;
  
  /**
   * Encodes a float.
   * @param f the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeFloat(float f) throws Exception;
  
  /**
   * Encodes a double.
   * @param d the value to encode
   * @throws Exception if an error occurs during encoding
   */
  void encodeDouble(double d) throws Exception;
  
}
