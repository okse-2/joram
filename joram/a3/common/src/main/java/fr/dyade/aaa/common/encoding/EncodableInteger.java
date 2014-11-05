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

import java.io.Serializable;

/**
 * Encodable object that wraps an integer.
 */
public class EncodableInteger implements Encodable, Serializable {
  
  private int value;
  
  public EncodableInteger() {}

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
  
  public int incrementAndGet() {
    return ++value;
  }
  
  public int getAndIncrement() {
    return value++;
  }

  public int getEncodableClassId() {
    return EncodableFactoryRepository.ENCODABLE_INTEGER_CLASS_ID;
  }

  public int getEncodedSize() throws Exception {
    return 4;
  }

  public void encode(Encoder encoder) throws Exception {
    encoder.encode32(value);
  }

  public void decode(Decoder decoder) throws Exception {
    value = decoder.decode32();
  }
  
  @Override
  public String toString() {
    return "EncodableInteger [value=" + value + "]";
  }

  public static class Factory implements EncodableFactory {

    public Encodable createEncodable() {
      return new EncodableInteger();
    }
    
  }

}
