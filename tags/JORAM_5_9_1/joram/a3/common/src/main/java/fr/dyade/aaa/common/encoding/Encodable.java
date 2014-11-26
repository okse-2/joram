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
 * This interface has to be provided by every object that
 * needs to be encoded according to the encoding framework.
 */
public interface Encodable {
  
  public static final int BOOLEAN_ENCODED_SIZE = 1;
  
  public static final int BYTE_ENCODED_SIZE = 1;
  
  public static final int SHORT_ENCODED_SIZE = 2;
  
  public static final int INT_ENCODED_SIZE = 4;
  
  public static final int LONG_ENCODED_SIZE = 8;
  
  public static final int FLOAT_ENCODED_SIZE = 4;
  
  public static final int DOUBLE_ENCODED_SIZE = 8;

  /***
   * Returns a unique class identifier.
   * @return a unique class identifier
   */
  int getEncodableClassId();
  
  /**
   * Returns the size of the byte array that results
   * from the encoding of this object.
   * @return the size of the encoded byte array
   * @throws Exception if an error occurs
   */
  int getEncodedSize() throws Exception;
  
  /**
   * Encodes the content of this object
   * @param encoder the encoder to be used for the encoding
   * @throws Exception if an error occurs
   */
  void encode(Encoder encoder) throws Exception;

  /**
   * Decodes the content of this object
   * @param decoder the decoder to be used for the decoding
   * @throws Exception if an error occurs
   */
  void decode(Decoder decoder) throws Exception;

}
