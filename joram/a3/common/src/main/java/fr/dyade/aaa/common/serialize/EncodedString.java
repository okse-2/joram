package fr.dyade.aaa.common.serialize;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EncodedString {
  
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

  @Override
  public int hashCode() {
    return string.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return string.equals(obj);
  }

}
