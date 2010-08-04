/*
 * Copyright (C) 2008 ScalAgent Distributed Technologies 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 */
package fr.dyade.aaa.agent;

import java.io.IOException;

/**
 * Class used to recv messages through a stream in a byte array.
 * <p>
 * Be careful this InputStream is not synchronized.
 */
public abstract class ByteArrayMessageInputStream extends MessageInputStream {
  /**
   * Creates a <code>ByteArrayMessageInputStream</code> that uses
   * <code>buf</code> as its buffer array.
   * <p>
   * Be careful, the buffer array is not copied. The initial value of
   * <code>pos</code> is 0 and the initial value of <code>count</code>
   *  is the length of <code>buf</code>.
   *
   * @param   buf   the input buffer.
   */
  public ByteArrayMessageInputStream(byte[] buf) {
    this(buf, 0, buf.length);
  }

  /**
   * Creates <code>ByteArrayMessageInputStream</code> that uses
   * <code>buf</code> as its buffer array.
   * <p>
   * Be careful, the buffer array is not copied. The initial value of
   * <code>pos</code> is <code>offset</code> and the initial value of
   * <code>count</code> is the minimum of <code>offset+length</code> and
   * <code>buf.length</code>.
   *
   * @param   buf      the input buffer.
   * @param   offset   the offset in the buffer of the first byte to read.
   * @param   length   the maximum number of bytes to read from the buffer.
   */
  public ByteArrayMessageInputStream(byte[] buf, int offset, int length) {
    this.buf = buf;
    pos = offset;
    count = Math.min(offset + length, buf.length);
  }

  /**
   * Reads the next byte of data from the input stream. The value byte is
   * returned as an <code>int</code> in the range <code>0</code> to
   * <code>255</code>. If no byte is available because the end of the stream
   * has been reached, the value <code>-1</code> is returned. This method
   * blocks until input data is available, the end of the stream is detected,
   * or an exception is thrown.
   *
   * @return     the next byte of data, or <code>-1</code> if the end of the
   *             stream is reached.
   */
  public final int read() {
    return (pos < count)?(buf[pos++] & 0xff):-1;    
  }

  /**
   * Reads up to <code>len</code> bytes of data from the input stream into
   * an array of bytes.  An attempt is made to read as many as
   * <code>len</code> bytes, but a smaller number may be read.
   * The number of bytes actually read is returned as an integer.
   * <p>
   * This method blocks until input data is available, end of file is
   * detected, or an exception is thrown.
   *
   * @param      b     the buffer into which the data is read.
   * @param      off   the start offset in array <code>b</code>
   *                   at which the data is written.
   * @param      len   the maximum number of bytes to read.
   * @return     the total number of bytes read into the buffer, or
   *             <code>-1</code> if there is no more data because the end of
   *             the stream has been reached.
   * @exception  NullPointerException If <code>b</code> is <code>null</code>.
   * @exception  IndexOutOfBoundsException If <code>off</code> is negative, 
   * <code>len</code> is negative, or <code>len</code> is greater than 
   * <code>b.length - off</code>
   */
  public final int read(byte b[], int off, int len) throws IOException {
    if (b == null)
      throw new NullPointerException();
    if (off < 0 || len < 0 || len > b.length - off)
      throw new IndexOutOfBoundsException();

    if (pos >= count)
      return -1;

    if (pos + len > count)
      len = count - pos;

    if (len <= 0) return 0;

    System.arraycopy(buf, pos, b, off, len);
    pos += len;

    return len;
  }

  /**
   * Reads length bytes of data from the input stream.
   */
  protected final void readFully(int length) throws IOException {
    // Data are fully available in the buffer.
  }
}
