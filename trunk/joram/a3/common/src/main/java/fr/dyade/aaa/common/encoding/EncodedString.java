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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Encodable object that wraps a string.
 * It also serves as a cache for the encoded string.
 */
public class EncodedString implements Serializable, Encodable {
  
  private String string;
  
  private byte[] encodedString;
  
  public EncodedString() {}

  public EncodedString(String string) {
    super();
    this.string = string;
  }
  
  public String getString() {
    return string;
  }

  public void writeTo(DataOutputStream os) throws IOException {
    if (encodedString == null) {
      encodedString = string.getBytes();
    }
    os.writeInt(encodedString.length);
    os.write(encodedString);
  }
  
  public void readFrom(DataInputStream is) throws IOException {
    int length = is.readInt();
    encodedString = new byte[length];
    is.readFully(encodedString);
    string = new String(encodedString);
  }
  
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeUTF(string);
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    string = in.readUTF();
  }

  @Override
  public int hashCode() {
    return string.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof EncodedString) {
      return ((EncodedString) obj).string.equals(string);
    } else {
      return string.equals(obj);
    }
  }

  @Override
  public String toString() {
    return string;
  }

  public int getClassId() {
    return EncodableFactoryRepository.ENCODED_STRING_CLASS_ID;
  }

  public int getEncodedSize() {
    return string.length() + 4;
  }

  public void encode(Encoder encoder) throws Exception {
    if (encodedString == null) {
      encodedString = string.getBytes();
    }
    encoder.encodeByteArray(encodedString);
  }

  public void decode(Decoder decoder) throws Exception {
    encodedString = decoder.decodeByteArray();
    string = new String(encodedString);
  }
  
  public static class Factory implements EncodableFactory {

    public Encodable createEncodable() {
      return new EncodedString();
    }
    
  }

}
