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
import java.io.OutputStream;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.BinaryDump;

/**
 * Class used to send messages through a stream using buffering.
 * <p>
 * This OutputStream allows the replacement of the underlying stream and
 * the serialisation of object through an internal ObjectOutputStream.
 * <p>
 * Be careful this OutputStream is not synchronized.
 */
public abstract class BufferedMessageOutputStream extends MessageOutputStream {
  /**
   * The underlying output stream. 
   */
  protected OutputStream out;

  /**
   *  Creates a new output stream to write data to an unspecified 
   * underlying output stream through a buffer with default size.
   *
   * @exception IOException if the internal ObjectOutputStream cannot be
   *		correctly initialized.
   */
  public BufferedMessageOutputStream() throws IOException {
    this(8192);
  }

  /**
   *  Creates a new output stream to write data to an unspecified 
   * underlying output stream through a buffer with the specified size.
   *
   * @param size the buffer size.
   * @exception IllegalArgumentException if size is less than 0.
   * @exception IOException if the internal ObjectOutputStream cannot be
   *		correctly initialized.
   */
  public BufferedMessageOutputStream(int size) throws IOException {
    super(size);
  }

  /**
   * Writes the internal buffer in underlying output stream.
   */
  private final void drain() throws IOException {
    if (getLogger().isLoggable(BasicLevel.DEBUG))
      getLogger().log(BasicLevel.DEBUG, "drain() - count=" + count + ", buf=" + BinaryDump.toHex(buf, 0, count));
    
    if (count > 0) {
      out.write(buf, 0, count);
      count = 0;
    }
  }

  /**
   * Writes the specified byte to this output stream. 
   *
   * @param      b   the byte to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  public final void write(int b) throws IOException {
    if (count >= buf.length)
      drain();
    buf[count++] = (byte) b;
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array 
   * starting at offset <code>off</code> to this output stream.
   * <p>
   * Ordinarily this method stores bytes from the given array into this
   * stream's buffer, flushing the buffer to the underlying output stream as
   * needed.  If the requested length is at least as large as this stream's
   * buffer, however, then this method will flush the buffer and write the
   * bytes directly to the underlying output stream.
   *
   * @param      b     the data.
   * @param      off   the start offset in the data.
   * @param      len   the number of bytes to write.
   * @exception  IOException  if an I/O error occurs.
   */
  public final void write(byte b[], int off, int len) throws IOException {
    if (len >= buf.length) {
      // If the request length exceeds the size of the output buffer, flush
      // the output buffer and then write the data directly.
      drain();
      
      if (getLogger().isLoggable(BasicLevel.DEBUG))
        getLogger().log(BasicLevel.DEBUG, "write(" + len + ')');
      
      out.write(b, off, len);
      return;
    }
    if (len > buf.length - count) {
      drain();
    }
    System.arraycopy(b, off, buf, count, len);
    count += len;
  }

  /**
   * Flushes this output stream.
   * <p>
   * This forces any buffered output bytes to be written out to the underlying
   * output stream. 
   *
   * @exception  IOException  if an I/O error occurs.
   */
  public final void flush() throws IOException {
    drain();
    out.flush();
  }
}
