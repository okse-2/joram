//   The contents of this file are subject to the Mozilla Public License
//   Version 1.1 (the "License"); you may not use this file except in
//   compliance with the License. You may obtain a copy of the License at
//   http://www.mozilla.org/MPL/
//
//   Software distributed under the License is distributed on an "AS IS"
//   basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//   License for the specific language governing rights and limitations
//   under the License.
//
//   The Original Code is RabbitMQ.
//
//   The Initial Developers of the Original Code are LShift Ltd., and
//   Cohesive Financial Technologies LLC.
//
//   Portions created by LShift Ltd. and Cohesive Financial
//   Technologies LLC. are Copyright (C) 2007 LShift Ltd. and Cohesive
//   Financial Technologies LLC.; All Rights Reserved.
//
//   Contributor(s): ______________________________________.
//

package org.objectweb.joram.mom.amqp.marshalling;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.input.ProxyInputStream;

/**
 * Utility stream: proxies another stream, making it appear to be no longer than
 * a preset limit.
 */
public class TruncatedInputStream extends ProxyInputStream {
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
