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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class EncodableHelper {
  
  /**
   * Returns the size of an encoded string.
   * Assumes that every character is encoded with one byte.
   * @param s the string to encode
   * @return the size of the encoded string
   */
  public static final int getStringEncodedSize(String s) {
    return Encodable.INT_ENCODED_SIZE + s.length();
  }
  
  /**
   * Returns the size of an encoded string which
   * value may be null.
   * Assumes that every character is encoded with one byte.
   * @param s the string to encode
   * @return the size of the encoded string
   */
  public static final int getNullableStringEncodedSize(String s) {
    int res = Encodable.BYTE_ENCODED_SIZE;
    if (s != null) {
      res += Encodable.INT_ENCODED_SIZE + s.length();
    }
    return res;
  }
  
  public static final int getByteArrayEncodedSize(byte[] byteArray) {
    return Encodable.INT_ENCODED_SIZE + byteArray.length;
  }
  
  public static final int getNullableByteArrayEncodedSize(byte[] byteArray) {
    int res = Encodable.BYTE_ENCODED_SIZE;
    if (byteArray != null) {
      res += Encodable.INT_ENCODED_SIZE + byteArray.length;
    }
    return res;
  }
  
  public static int getEncodedSize(Properties properties) throws Exception {
    int res = Encodable.INT_ENCODED_SIZE;
    Set<Entry<Object, Object>> entries = properties.entrySet();
    Iterator<Entry<Object, Object>> iterator = entries.iterator();
    while (iterator.hasNext()) {
      Entry<Object, Object> entry = iterator.next();
      res += getStringEncodedSize((String) entry.getKey());
      res += getStringEncodedSize((String) entry.getValue());
    }
    return res;
  }
  
  public static void encodeProperties(Properties properties, Encoder encoder) throws Exception {
    encoder.encodeUnsignedInt(properties.size());
    Set<Entry<Object, Object>> entries = properties.entrySet();
    Iterator<Entry<Object, Object>> iterator = entries.iterator();
    while (iterator.hasNext()) {
      Entry<Object, Object> entry = iterator.next();
      encoder.encodeString((String) entry.getKey());
      encoder.encodeString((String) entry.getValue());
    }
  }

  public static Properties decodeProperties(Decoder decoder) throws Exception {
    Properties properties = new Properties();
    int size = decoder.decodeUnsignedInt();
    for (int i = 0; i < size; i++) {
      String key = decoder.decodeString();
      String value = decoder.decodeString();
      properties.put(key, value);
    }
    return properties;
  }
  
}
