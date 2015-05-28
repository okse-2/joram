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

/**
 * Interface of a decoder
 */
public interface Decoder {

  /**
   * Decodes a signed short integer. The encoding format
   * depends on the implementation.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  short decodeSignedShort() throws Exception;
  
  /**
   * Decodes an unsigned short integer. The encoding format
   * depends on the implementation.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  short decodeUnsignedShort() throws Exception;
  
  /**
   * Decodes a signed short integer. The encoding format is the
   * 2's complement representation encoded in 2 bytes.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  short decode16() throws Exception;
  
  /**
   * Decodes a signed integer. The encoding format
   * depends on the implementation.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  int decodeSignedInt() throws Exception;
  
  /**
   * Decodes an unsigned integer. The encoding format
   * depends on the implementation.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  int decodeUnsignedInt() throws Exception;
  
  /**
   * Decodes a signed integer. The encoding format is the
   * 2's complement representation encoded in 4 bytes.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  int decode32() throws Exception;
  
  /**
   * Decodes a signed long integer. The encoding format
   * depends on the implementation.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  long decodeSignedLong() throws Exception;
  
  /**
   * Decodes an unsigned long integer. The encoding format
   * depends on the implementation.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  long decodeUnsignedLong() throws Exception;

  /**
   * Decodes a signed long integer. The encoding format is the
   * 2's complement representation encoded in 8 bytes.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  long decode64() throws Exception;
  
  /**
   * Decodes a string that can be null.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  String decodeNullableString() throws Exception;
  
  /**
   * Decodes a string that cannot be null.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  String decodeString() throws Exception;
  
  /**
   * Decodes a string that cannot be null.
   * @param length the length of the string
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  String decodeString(int length) throws Exception;
  
  /**
   * Decodes a byte.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  byte decodeByte() throws Exception;
  
  /**
   * Decodes a byte array that can be null.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  byte[] decodeNullableByteArray() throws Exception;
  
  /**
   * Decodes a byte array that cannot be null.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  byte[] decodeByteArray() throws Exception;
  
  /**
   * Decodes a byte array that cannot be null.
   * @param length the length of the array
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  byte[] decodeByteArray(int length) throws Exception;
  
  /**
   * Decodes a boolean.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  boolean decodeBoolean() throws Exception;
  
  /**
   * Decodes a float.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  float decodeFloat() throws Exception;
  
  /**
   * Decodes a double.
   * @return the decoded value
   * @throws Exception if an error occurs during decoding
   */
  double decodeDouble() throws Exception;
  
}
