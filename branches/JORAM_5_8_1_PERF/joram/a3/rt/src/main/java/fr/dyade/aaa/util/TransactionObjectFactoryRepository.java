package fr.dyade.aaa.util;

import java.util.Hashtable;

public class TransactionObjectFactoryRepository {
  
  private static Hashtable<Integer, TransactionObjectFactory> repository = new Hashtable<Integer, TransactionObjectFactory>();
  
  public static TransactionObjectFactory getFactory(Integer classId) {
    return repository.get(classId);
  }
  
  public static void putFactory(Integer classId, TransactionObjectFactory factory) {
    repository.put(classId, factory);
  }
  
}
