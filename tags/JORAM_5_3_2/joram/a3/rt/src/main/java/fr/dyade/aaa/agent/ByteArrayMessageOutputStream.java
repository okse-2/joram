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

/**
 * Class used to write messages into a byte array.
 * <p>
 * This OutputStream allows the replacement of the underlying stream and
 * the serialisation of object through an internal ObjectOutputStream.
 * <p>
 * Be careful this OutputStream is not synchronized.
 */
public abstract class ByteArrayMessageOutputStream extends MessageOutputStream {
  /**
   *  Creates a new output stream to write data to an internal byte array
   * with default size.
   *
   * @exception IOException if the internal ObjectOutputStream cannot be
   *		correctly initialized.
   */
  public ByteArrayMessageOutputStream() throws IOException {
    this(8192);
  }

  /**
   *  Creates a new output stream to write data to an internal byte array
   * with specified size.
   *
   * @param size the buffer size.
   * @exception IllegalArgumentException if size is less than 0.
   * @exception IOException if the internal ObjectOutputStream cannot be
   *		correctly initialized.
   */
  public ByteArrayMessageOutputStream(int size) throws IOException {
    super(size);
  }

  /**
   * Writes the specified byte to this output stream. 
   *
   * @param      b   the byte to be written.
   * @exception  IOException  if an I/O error occurs.
   */
  public final void write(int b) throws IOException {
    int newcount = count + 1;
    if (newcount > buf.length) {
      newcount = Math.max(buf.length << 1, newcount);
      byte[] newbuf = new byte[newcount];
      System.arraycopy(buf, 0, newbuf, 0, count);
      buf = newbuf;
    }
    buf[count++] = (byte) b;
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array 
   * starting at offset <code>off</code> to this output stream.
   *
   * @param      b     the data.
   * @param      off   the start offset in the data.
   * @param      len   the number of bytes to write.
   * @exception  IOException  if an I/O error occurs.
   */
  public final void write(byte b[], int off, int len) throws IOException {
    int newcount = count + len;
    if (newcount > buf.length) {
      newcount = Math.max(buf.length << 1, newcount);
      byte[] newbuf = new byte[newcount];
      System.arraycopy(buf, 0, newbuf, 0, count);
      buf = newbuf;
    }
    System.arraycopy(b, off, buf, count, len);
    count += len;
  }

  /**
   * Returns the current size of the buffer.
   *
   * @return  the current size of the buffer.
   */
  public final int size() {
    return count;
  }

  /**
   * Resets this output stream.
   * <p>
   * Set the <code>count</code> field of this output stream to zero,
   * so that all currently accumulated output in the output stream is
   * discarded. The output stream can be used again,  reusing the
   * already allocated buffer space. 
   */
  public final void reset() {
    count = 0;
  }

  /**
   * Writes the complete contents of this byte array output stream to 
   * the specified output stream argument, as if by calling the output 
   * stream's write method using <code>out.write(buf, 0, count)</code>.
   *
   * @param      out   the output stream to which to write the data.
   * @exception  IOException  if an I/O error occurs.
   */
  public final void writeTo(OutputStream out) throws IOException {
    out.write(buf, 0, count);
  }
}
