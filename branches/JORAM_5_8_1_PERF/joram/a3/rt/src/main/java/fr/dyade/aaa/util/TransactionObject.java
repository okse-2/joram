package fr.dyade.aaa.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface TransactionObject {
  
  public int getClassId();
  
  public void encodeTransactionObject(DataOutputStream os) throws IOException;
  
  public void decodeTransactionObject(DataInputStream os) throws IOException;

}
