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

import java.util.Hashtable;

public class EncodableFactoryRepository {
  
  private static Hashtable<Integer, EncodableFactory> repository = new Hashtable<Integer, EncodableFactory>();
  
  static {
    repository.put(Encodable.ENCODED_STRING_CLASS_ID, new EncodedString.Factory());
    repository.put(Encodable.SERIALIZABLE_WRAPPER_CLASS_ID, new SerializableWrapper.Factory());
    repository.put(Encodable.STRING_PAIR_CLASS_ID, new StringPair.Factory());
  }
  
  public static EncodableFactory getFactory(Integer classId) {
    return repository.get(classId);
  }
  
  public static void putFactory(Integer classId, EncodableFactory factory) {
    repository.put(classId, factory);
  }
  
}
