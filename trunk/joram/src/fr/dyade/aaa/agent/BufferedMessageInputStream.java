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
import java.io.EOFException;
import java.io.InputStream;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Class used to receive messages through a stream using buffering.
 * <p>
 * Be careful this InputStream is not synchronized.
 */
public abstract class BufferedMessageInputStream extends MessageInputStream {
  /**
   * The underlying input stream to be read. 
   */
  protected InputStream in = null;

  /**
   * Creates a <code>BufferedMessageInputStream</code> that uses a buffer
   * with default size.
   */
  public BufferedMessageInputStream() {
    this(8192);
  }

  /**
   * Creates a <code>BufferedMessageInputStream</code> that uses a buffer
   * with specified size.
   *
   * @param size the buffer size.
   * @exception IllegalArgumentException if size is less than 0.
   */
  public BufferedMessageInputStream(int size) {
    if (size <= 0)
      throw new IllegalArgumentException("Buffer size <= 0");
    buf = new byte[size];
    pos = 0; count = 0;
  }

  /**
   * Resets the stream for a new use.
   * Removes all data.
   */
  protected final void clean() {
    pos = 0; count = 0;
  }

  /**
   * Fills the empty buffer with more data.
   */
  private final void fill() throws IOException {
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "fill()");
    
    pos = 0;
    count = in.read(buf, 0, buf.length);
    if (count < 0)
      count = 0;
    
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "fill() - count=" + count);
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
   * @exception  IOException  if an I/O error occurs.
   */
  public final int read() throws IOException {
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "read()");
    
    if (pos >= count) {
      fill();
      if (pos >= count)
        return -1;
    }
    return buf[pos++] & 0xff;

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
   * @exception  IOException If the first byte cannot be read for any reason
   * other than end of file, or if the input stream has been closed, or if
   * some other I/O error occurs.
   * @exception  NullPointerException If <code>b</code> is <code>null</code>.
   * @exception  IndexOutOfBoundsException If <code>off</code> is negative, 
   * <code>len</code> is negative, or <code>len</code> is greater than 
   * <code>b.length - off</code>
   */
  public final int read(byte b[], int off, int len) throws IOException {
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "read(" + len + ')');
    
    if ((off | len | (off + len) | (b.length - (off + len))) < 0)
      throw new IndexOutOfBoundsException();
    if (len == 0) return 0;

    int n = 0;
    for (;;) {
      int nread = read1(b, off + n, len - n);
      if (nread <= 0) 
        return (n == 0) ? nread : n;
      n += nread;

      if (n >= len)
        return n;
      // if not closed but no bytes available, return
      if (in != null && in.available() <= 0)
        return n;
    }
  }

  private final int read1(byte[] b, int off, int len) throws IOException {
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "read1(" + len + ')');
    
    int avail = count - pos;
    if (avail <= 0) {
      // If the requested length is at least as large as the buffer, do not
      // bother to copy the bytes into the local buffer.
      // In this way buffered streams will cascade harmlessly.
      if (len >= buf.length) {
        if (getLogger().isLoggable(BasicLevel.DEBUG))
          getLogger().log(BasicLevel.DEBUG, "returns read(" + len + ')');
        
        return in.read(b, off, len);
      }
      fill();
      avail = count - pos;
      if (avail <= 0) return -1;
    }
    int cnt = (avail < len) ? avail : len;
    System.arraycopy(buf, pos, b, off, cnt);
    pos += cnt;
    
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "read1() returns " + cnt);
    
    return cnt;
  }

  /**
   * Reads length bytes of data from the input stream. This method returns
   * when length bytes are available or if end of stream is reached.
   */
  protected final void readFully(int length) throws IOException {
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "readFully(" + length + ')');
    
    int valid = count - pos;
    if (valid < length) {
      // There is not enough byte in the buffer
      if (length > buf.length) {
        // Allocate a new buffer then copy valid data.
        byte[] newbuf = new byte[length];
        System.arraycopy(buf, pos, newbuf, 0, valid);
        buf = newbuf; pos = 0; count = valid;
      } else if ((pos + length) > buf.length) {
        // Clear already read data in order to allow the read of next.
        System.arraycopy(buf, pos, buf, 0, valid);
        pos = 0;
        count = valid;
      }

      do {
        if (getLogger().isLoggable(BasicLevel.DEBUG))
          getLogger().log(BasicLevel.DEBUG, "read(" + count + ')');
        
        int nb = in.read(buf, count, buf.length - count);
        if (nb < 0) throw new EOFException();
        count += nb;
      } while (count < length);
    }
    
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "readFully returns - count=" + count);
  }
}
