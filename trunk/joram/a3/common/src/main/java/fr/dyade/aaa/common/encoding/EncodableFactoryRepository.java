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

/**
 * Repository for every encodable objects factory.
 * The identifier of a factory must be the concatenation of
 * two short integers: the first one identifies the area of the
 * class; the second one identifies the class.
 * The area of the encodable objects defined in this encoding 
 * framework is 0.
 */
public class EncodableFactoryRepository {
  
  private static final int CLASS_ID_AREA = 0x00000000;
  
  public static final int ENCODED_STRING_CLASS_ID = CLASS_ID_AREA + 0;
  
  public static final int SERIALIZABLE_WRAPPER_CLASS_ID = CLASS_ID_AREA + 1;
  
  public static final int STRING_PAIR_CLASS_ID = CLASS_ID_AREA + 2;
  
  public static final int PROPERTIES_CLASS_ID = CLASS_ID_AREA + 3;
  
  private static Hashtable<Integer, EncodableFactory> repository = new Hashtable<Integer, EncodableFactory>();
  
  static {
    repository.put(EncodableFactoryRepository.ENCODED_STRING_CLASS_ID, new EncodedString.Factory());
    repository.put(EncodableFactoryRepository.SERIALIZABLE_WRAPPER_CLASS_ID, new SerializableWrapper.Factory());
    repository.put(EncodableFactoryRepository.STRING_PAIR_CLASS_ID, new StringPair.Factory());
  }
  
  /**
   * Returns the specified factory. 
   * @param classId the identifier of the factory
   * @return the specified factory
   */
  public static EncodableFactory getFactory(Integer classId) {
    return repository.get(classId);
  }
  
  /**
   * Registers a factory.
   * @param classId the identifier of the factory
   * @param factory the factory to register
   */
  public static void putFactory(Integer classId, EncodableFactory factory) {
    repository.put(classId, factory);
  }
  
}
