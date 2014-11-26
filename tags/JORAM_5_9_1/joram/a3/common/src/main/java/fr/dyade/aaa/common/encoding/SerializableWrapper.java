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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.Encoder;

/**
 * Encodable object that wraps a serializable object.
 */
public class SerializableWrapper implements Encodable {
  
  private Serializable value;
  
  private byte[] bytes;
  
  public SerializableWrapper() {}

  public SerializableWrapper(Serializable value) {
    super();
    this.value = value;
  }

  public Serializable getValue() {
    return value;
  }

  public int getEncodableClassId() {
    return EncodableFactoryRepository.SERIALIZABLE_WRAPPER_CLASS_ID;
  }

  public int getEncodedSize() throws Exception {
    serialize();
    return bytes.length + 4;
  }
  
  private void serialize() throws Exception {
    if (bytes == null) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      try {
        oos.writeObject(value);
        bytes = baos.toByteArray();
      } finally {
        baos.close();
        oos.close();
      }
    }
  }

  public void encode(Encoder encoder) throws Exception {
    serialize();
    encoder.encodeByteArray(bytes);
  }

  public void decode(Decoder decoder) throws Exception {
    byte[] bytes = decoder.decodeByteArray();
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    ObjectInputStream ois = new ObjectInputStream(bais);
    value = (Serializable) ois.readObject();
  }
  
  public static class Factory implements EncodableFactory {

    public Encodable createEncodable() {
      return new SerializableWrapper();
    }
    
  }

}
