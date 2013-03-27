package fr.dyade.aaa.common.encoding;

import java.util.Hashtable;

public class EncodableFactoryRepository {
  
  private static Hashtable<Integer, EncodableFactory> repository = new Hashtable<Integer, EncodableFactory>();
  
  public static EncodableFactory getFactory(Integer classId) {
    return repository.get(classId);
  }
  
  public static void putFactory(Integer classId, EncodableFactory factory) {
    repository.put(classId, factory);
  }
  
}
