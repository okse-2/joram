package fr.dyade.aaa.common.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class EncodedString implements Serializable {
  
  private String string;
  
  private byte[] encodedString;
  
  public EncodedString() {}

  public EncodedString(String string) {
    super();
    this.string = string;
  }
  
  public String getString() {
    return string;
  }

  public void writeTo(DataOutputStream os) throws IOException {
    if (encodedString == null) {
      encodedString = string.getBytes();
    }
    os.writeInt(encodedString.length);
    os.write(encodedString);
  }
  
  public void readFrom(DataInputStream is) throws IOException {
    int length = is.readInt();
    encodedString = new byte[length];
    is.readFully(encodedString);
    string = new String(encodedString);
  }
  
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeUTF(string);
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    string = in.readUTF();
  }

  @Override
  public int hashCode() {
    return string.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof EncodedString) {
      return ((EncodedString) obj).string.equals(string);
    } else {
      return string.equals(obj);
    }
  }

  @Override
  public String toString() {
    return "EncodedString [string=" + string + "]";
  }

}
