package org.objectweb.joram.mom.amqp.marshalling;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class LongStringHelper {

  public static class ByteArrayLongString implements LongString {

    byte[] bytes;

    public ByteArrayLongString(byte[] bytes) {
      this.bytes = bytes;
    }

    public boolean equals(Object o) {
      if (o instanceof LongString) {
        LongString other = (LongString) o;
        return Arrays.equals(this.bytes, other.getBytes());
      }

      return false;
    }

    public int hashCode() {
      return Arrays.hashCode(this.bytes);
    }

    public byte[] getBytes() {
      return bytes;
    }

    public InputStream getStream() throws IOException {
      return new ByteArrayInputStream(bytes);
    }

    public long length() {
      return bytes.length;
    }

    public String toString() {
      try {
        return new String(bytes, "utf-8");
      } catch (UnsupportedEncodingException e) {
        throw new Error("utf-8 encoding support required");
      }
    }
  }

  public static LongString asLongString(String s) {
    try {
      return new ByteArrayLongString(s.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e) {
      throw new Error("utf-8 encoding support required");
    }
  }

  public static LongString asLongString(byte[] bytes) {
    return new ByteArrayLongString(bytes);
  }
}
