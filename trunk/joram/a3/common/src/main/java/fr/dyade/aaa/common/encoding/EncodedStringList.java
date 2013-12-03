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
import java.util.ArrayList;
import java.util.List;

public class EncodedStringList implements Serializable, Encodable {
  
  private List<EncodedString> list;
  
  public EncodedStringList() {
    list = new ArrayList<EncodedString>();
  }

  public List<EncodedString> getList() {
    return list;
  }
  
  public int getEncodableClassId() {
    return EncodableFactoryRepository.ENCODED_STRING_CLASS_ID;
  }

  public int getEncodedSize() throws Exception {
    int res = 4;
    for (EncodedString str : list) {
      res += str.getEncodedSize();
    }
    return res;
  }

  public void encode(Encoder encoder) throws Exception {
    encoder.encode32(list.size());
    for (EncodedString str : list) {
      str.encode(encoder);
    }
  }

  public void decode(Decoder decoder) throws Exception {
    int l = decoder.decode32();
    list = new ArrayList<EncodedString>(l);
    for (int i = 0; i < l; i++) {
      EncodedString str = new EncodedString();
      list.add(str);
      str.decode(decoder);
    }
  }
  
  public static class Factory implements EncodableFactory {

    public Encodable createEncodable() {
      return new EncodedStringList();
    }
    
  }

}
