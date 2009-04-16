package org.objectweb.joram.mom.amqp.marshalling;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TruncatedInputStream extends FilterInputStream {
  private final long limit;
  private long counter = 0L;
  private long mark = 0L;

  public TruncatedInputStream(InputStream in, long limit) {
    super(in);
    this.limit = limit;
  }

  private static long min(long a, long b) {
    return a > b ? b : a;
  }

  public int available() throws IOException {
    return (int) min(limit - counter, super.available());
  }

  public void mark(int readlimit) {
    super.mark(readlimit);
    mark = counter;
  }

  public int read() throws IOException {
    if (counter < limit) {
      int result = super.read();
      if (result >= 0)
        counter++;
      return result;
    } else {
      return -1;
    }
  }

  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  public int read(byte[] b, int off, int len) throws IOException {

    if (limit > counter) {
      int result = super.read(b, off, (int) min(len, limit - counter));
      if (result > 0)
        counter += result;
      return result;
    } else {
      return -1;
    }
  }

  public void reset() throws IOException {
    super.reset();
    counter = mark;
  }

  public long skip(long n) throws IOException {
    long result = super.skip(min(n, limit - counter));
    counter += result;
    return result;
  }
}
