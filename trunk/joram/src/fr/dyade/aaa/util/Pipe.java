/*
 * Copyright (C) 2002 SCALAGENT
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
 *
 */
package fr.dyade.aaa.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class Pipe {
  protected String name = null;

  protected int size = 0;

  /**
   * The index of the position in the circular buffer at which the
   * next byte of data will be stored when received from the connected
   * piped output stream. <code>in&lt;0</code> implies the buffer is empty,
   * <code>in==out</code> implies the buffer is full
   */
  protected int in = -1;

  /**
   * The index of the position in the circular buffer at which the next
   * byte of data will be read by this piped input stream.
   */
  protected int out = 0;

  private Logger logmon = null;
  private long cpt1, cpt2;

  public static final int DFLT_BUF_SIZE = 50;
  public static final String DFLT_NAME = "noname";
  public static final String DFLT_DIR = null;

  /**
   * The circular buffer into which incoming data is placed.
   */
  protected Object[] buffer = null;
  
  /**
   * The index of the position in the file buffer at which the next piece
   * of data will be read.
   */
  protected long fbufinptr = 0;
  /**
   * The index of the position in the file buffer at which the next piece
   * of data will be write. <code>in&lt;0</code> implies the buffer is empty.
   */
  protected long fbufoutptr = -1;

  protected RandomAccessFile fbufin = null;
  protected RandomAccessFile fbufout = null;

  /**
   * Creates a <code>Pipe</code> with default size for memory buffer.
   */
  public Pipe() throws IOException {
    this(DFLT_BUF_SIZE, DFLT_NAME, DFLT_DIR);
  }

  public final int getBufferSize() {
    return size;
  }

  public final int getSizeInFile() {
    if (fbufoutptr == -1)
      return -1;
    else
      return (int) (fbufoutptr - fbufinptr);
  }

  /**
   * Creates a <code>Pipe</code> with specified size for in memory buffer.
   *
   * @param	size	the size for in memory buffer.
   */
  public Pipe(int size, String name, String dir) throws IOException {
    this.size = size;
    buffer = new Object[size];

    this.name = name;

    File tmp = null;
    if (dir == null)
      tmp = File.createTempFile(name, "pipe");
    else
      tmp = File.createTempFile(name, "pipe", new File(dir));
    tmp.deleteOnExit();
    fbufin = new RandomAccessFile(tmp, "r");
    fbufout = new RandomAccessFile(tmp, "rw");

    logmon = Debug.getLogger("fr.dyade.aaa.util.Pipe.#" + name);
  }

  public synchronized void write(byte[] msg) throws IOException {
    if ((fbufoutptr == -1) && (in != out)) {
      // Neither the buffer is full, nor a buffer file is also in use.
      // Put the element in the next square.
      if (in < 0) {
        in = 0;
        out = 0;
      }
      buffer[in++] = msg;
      if (in == size) in = 0;
      notify();
    } else {
      // Either there is also a buffer file in use, or the buffer is full
      // Append the element to the buffer file.
      if (fbufoutptr == -1) {
        fbufout.seek(0);
        fbufoutptr = 0;
      }
      fbufout.writeInt(msg.length);
      fbufout.write(msg);
      fbufoutptr += 1;
    }
  }

  public synchronized int read(Object[] buf) throws IOException {
    while (in < 0) {
      // The buffer is empty
      if (fbufoutptr == -1) {
        try {
          wait();
        } catch (InterruptedException ex) {
          throw new java.io.InterruptedIOException();
        }
        continue;
      } else {
        in = 0;
        out = 0;
        int l;

        /* fill in the circular buffer with data in file buffer */
        while ((fbufinptr < fbufoutptr) && (in < size)) {
          l = fbufin.readInt();
          byte[] msg = new byte[l];
          if (fbufin.read(msg) != l)
            throw new IOException("buffer file corrupted");
          buffer[in++] = msg;
          fbufinptr += 1;
        }
        if (fbufinptr == fbufoutptr) {
          // the file buffer is empty
          fbufinptr = 0;
          fbufoutptr = -1;
          fbufout.setLength(0);
          fbufin.seek(0);
        }
        if (in == size) in = 0;
      }
    }


    int idx = 0;
    // There is almost one element!
    do {
      buf[idx++] = buffer[out];
      buffer[out] = null;
      out += 1;
      if (out >= size) out = 0;
    } while ((out != in) && (idx < buf.length));

    cpt1 += 1; cpt2 += idx;
    if ((cpt1 %10000) == 0) {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG,
                   "Pipe.#" + name + ": " +
                   cpt2 + '/' + cpt1 + '/' + in +'/' + out);
      }
    }

    if (out == in) {
      /* now empty */
      in = -1;
    }

    return idx;
  }
}
