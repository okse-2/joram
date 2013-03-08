package fr.dyade.aaa.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface TransactionObject {
  
  public static final int CLASS_ID_AREA = 0x0000;
  public static final int NOTIFICATION_CLASS_ID = CLASS_ID_AREA + 0;
  
  public int getClassId();
  
  public void encodeTransactionObject(DataOutputStream os) throws IOException;
  
  public void decodeTransactionObject(DataInputStream is) throws IOException;

}
