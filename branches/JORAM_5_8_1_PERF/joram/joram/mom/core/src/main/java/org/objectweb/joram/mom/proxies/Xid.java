package org.objectweb.joram.mom.proxies;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import fr.dyade.aaa.util.TransactionObject;

/**
 * The <code>Xid</code> internal class is a utility class representing
 * a global transaction identifier.
 */
class Xid implements Serializable, TransactionObject {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  byte[] bq;
  int fi;
  byte[] gti;

  Xid(byte[] bq, int fi, byte[] gti) {
    this.bq = bq;
    this.fi = fi;
    this.gti = gti;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Xid))
      return false;

    Xid other = (Xid) o;

    return java.util.Arrays.equals(bq, other.bq)
           && fi == other.fi
           && java.util.Arrays.equals(gti, other.gti);
  }

  public int hashCode() {
    return (new String(bq) + "-" + new String(gti)).hashCode();
  }

  public int getClassId() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void encodeTransactionObject(DataOutputStream os) throws IOException {
    // TODO Auto-generated method stub
    
  }

  public void decodeTransactionObject(DataInputStream is) throws IOException {
    // TODO Auto-generated method stub
    
  }

}