/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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


package fr.dyade.aaa.util;

import java.io.*;


/**
 * A <code>FlowControlDxOutputStream</code> object works paired with a
 * <code>FlowControlDxInputStream</code> object. They both implement
 * a user level flow control over a duplex stream. They generalize what
 * is done by the classes <code>fr.dyade.aaa.ar.FlowControlInputStream</code> and
 * <code>fr.dyade.aaa.ar.FlowControlOutputStream</code> over a single sided stream.
 * <p>
 * Those classes have been written to circumvent a bug occuring in JDK 1.1.6
 * for AIX. The execution of the garbage collector when a thread is blocked
 * in writing onto a socket output stream due to TCP flow control triggers
 * an IOException in the writing thread with message "interrupted system call".
 * This bug also relates with the JIT, and still occurs in JDK 1.1.8.
 * <p>
 * The classes are used as filters in a stack of input or output streams.
 * Here is an example usage :
 * <code>
 * <br> InputStream in;
 * <br> OutputStream out;
 * <br> FlowControlDxInputStream fcis = new FlowControlDxInputStream(in);
 * <br> FlowControlDxOutputStream fcos = new FlowControlDxInputStream(out);
 * <br> fcos.setControlInputStream(fcis);
 * <br> fcis.setControlOutputStream(fcos);
 * <br> in = fcis;
 * <br> out = fcos;
 * </code>
 * <br>
 * If characters are mostly read one at a time, it is strongly recommended
 * to set a buffered stream ahead.
 * <code>
 * <br> in = new BufferedInputStream(fcis);
 * </code>
 * <p>
 * This class has been primarily designed for separate reading and writing
 * threads. If input and output is to be performed by a single thread, it
 * might be necessary to set a not null value for the <code>regularSize</code>
 * variable in the <code>FlowControlDxInputStream</code> object.
 * <p>
 * The algorithm uses a window of <code>windowSize</code> bytes which is
 * the base unit of data the output stream object writes onto the stream.
 * When the input stream object completes reading of a unit of data it signals
 * the output stream object with a control byte. When the output stream object
 * receives that byte it starts writing the next unit of data.
 * The algorithm starts with the output stream object writing two units of data.
 * <p>
 * Control bytes and data bytes are identified by a byte long header holding
 * a value from <code>0</code> to <code>255</code>. A <code>0</code> value
 * indicates a following control byte, and a not null <code>n</code> value
 * indicates a number of <code>n</code> following data bytes.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		FlowControlDxInputStream
 */
public class FlowControlDxOutputStream extends FilterOutputStream {
public static final String RCS_VERSION="@(#)$Id: FlowControlDxOutputStream.java,v 1.10 2004-02-13 10:26:43 fmaistre Exp $";

  /** control input stream */
  FlowControlDxInputStream control = null;

  /** number of bytes to write between control bytes */
  int windowSize = 0;

  /** number of bytes to write before next control byte */
  int window = 0;

  /** buffered single bytes written */
  protected byte[] sendBuffer = null;
  /** number of valid bytes in <code>sendBuffer</code> */
  protected int sendLength = 0;

  /** synchronization object */
  Object lock = null;

  /** unique id, used for debugging */
  int id = -1;

  /**
   * Constructor.
   *
   * @param out		the underlying output stream
   * @param windowSize	number of bytes to write between control bytes
   */
  public FlowControlDxOutputStream(OutputStream out,
				 int windowSize) {
    super(out);
    this.windowSize = windowSize;
    window = 2 * windowSize;
    sendBuffer = new byte[windowSize];
    lock = new Object();
    if (FlowControlDuplexStream.DEBUG)
      id = FlowControlDuplexStream.newId();
  }

  /**
   * Constructor with default window size value set to
   * <code>FlowControlDuplexStream.WINDOW_SIZE</code>.
   *
   * @param out		the underlying output stream
   */
  public FlowControlDxOutputStream(OutputStream out) {
    this(out, FlowControlDuplexStream.WINDOW_SIZE);
  }

  /**
   * Sets the control input stream. That stream may not be known when
   * this object is created.
   *
   * @param control	the control input stream
   */
  public void setControlInputStream(FlowControlDxInputStream control) {
    this.control = control;
  }

  /**
   * Writes the specified <code>byte</code> to this output stream.
   * <p>
   * This function shares the underlying output stream with the function
   * <code>controlWrite</code> called in another thread. The byte is buffered
   * until a full window is ready, or a flush is called, or the byte array
   * write function is called.
   *
   * @param b	the byte
   */
  public void write(int b) throws IOException {
    if (FlowControlDuplexStream.DEBUG && FlowControlDuplexStream.dbgStreams)
      FlowControlDuplexStream.trace(
	"FlowControlDxOutputStream(" + id + "): write()");
    // there is no need to synchronize with another write function
    sendBuffer[sendLength] = Ubyte.signedValue(b);
    sendLength ++;
    if (sendLength >= window ||
	sendLength >= sendBuffer.length) {
      // calls the byte array write of this class
      int slen = sendLength;
      sendLength = 0;
      write(sendBuffer, 0, slen);
    }
  }

  /**
   * Writes <code>len</code> bytes from the specified <code>byte</code>
   * array starting at offset <code>off</code> to this output stream.
   * <p>
   * This function shares the underlying output stream with the function
   * <code>controlWrite</code> called in another thread.
   *
   * @param b		the data
   * @param off		the start offset in the data
   * @param len		the number of bytes to write
   */
  public void write(byte b[],
		    int off,
		    int len) throws IOException {
    if (FlowControlDuplexStream.DEBUG && FlowControlDuplexStream.dbgStreams)
      FlowControlDuplexStream.trace(
	"FlowControlDxOutputStream(" + id + "): write(" + len + ")");
    if (len <= 0)
      return;

    // first checks locally buffered bytes
    if (sendLength > 0) {
      int slen = sendLength;
      sendLength = 0;
      write(sendBuffer, 0, slen);
    }

    while (len > 0) {
      if (window <= 0) {
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgControl)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxOutputStream(" + id + "): control read");
	flush();
	switch (control.controlRead()) {
	case FlowControlDuplexStream.CTRL_WINDOW:
	  if (FlowControlDuplexStream.DEBUG &&
	      FlowControlDuplexStream.dbgControl)
	    FlowControlDuplexStream.trace(
	      "FlowControlDxOutputStream(" + id + "): control read -> control");
	  window += windowSize;
	  break;
	case -1:
	  // connection closed
	  if (FlowControlDuplexStream.DEBUG &&
	      FlowControlDuplexStream.dbgControl)
	    FlowControlDuplexStream.trace(
	      "FlowControlDxOutputStream(" + id + "): control read -> close");
	  throw new IOException("stream closed");
	}
      }

      int towrite = len;
      if (towrite > window) towrite = window;
      if (towrite > 255) towrite = 255;
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgStreams)
	FlowControlDuplexStream.trace(
	  "FlowControlDxOutputStream(" + id + "): write -> " + towrite);
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxOutputStream(" + id + "): get lock");
      synchronized (lock) {
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgSynchro)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxOutputStream(" + id + "): got lock");
	out.write(towrite);	// regular length header
	out.write(b, off, towrite);
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgSynchro)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxOutputStream(" + id + "): free lock");
      }
      off += towrite;
      len -= towrite;

      window -= towrite;
    }
  }

  /**
   * Writes the specified control byte to this output stream.
   * <p>
   * This function shares the underlying output stream with the functions
   * <code>write</code> called in another thread.
   *
   * @param b	the byte
   */
  public void controlWrite(int b) throws IOException {
    if (FlowControlDuplexStream.DEBUG && FlowControlDuplexStream.dbgControl)
      FlowControlDuplexStream.trace(
	"FlowControlDxOutputStream(" + id + "): controlWrite(" + b + ")");

    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgSynchro)
      FlowControlDuplexStream.trace(
	"FlowControlDxOutputStream(" + id + "): get lock");
    synchronized (lock) {
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxOutputStream(" + id + "): got lock");
      try {
	out.write(0);	// control header
	out.write(b);
      } catch (Exception exc) {
	// the output stream may have been closed
	// while the current thread reads the remaining input bytes
	// ignores this error
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgControl)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxOutputStream(" + id + "): " +
	    "write error " + exc.toString());
      }
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxOutputStream(" + id + "): free lock");
    }
  }

  /**
   * Flushes this output stream and forces any buffered output bytes to be
   * written out.
   */
  public void flush() throws IOException {
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgStreams)
      FlowControlDuplexStream.trace(
	"FlowControlDxOutputStream(" + id + "): flush()");
    if (sendLength > 0) {
      int slen = sendLength;
      sendLength = 0;
      write(sendBuffer, 0, slen);
    }
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgSynchro)
      FlowControlDuplexStream.trace(
	"FlowControlDxOutputStream(" + id + "): get lock");
    synchronized (lock) {
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxOutputStream(" + id + "): got lock");
      out.flush();
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxOutputStream(" + id + "): free lock");
    }
  }

  /**
   * Closes this output stream and releases any system resources associated
   * with the stream.
   */
  public void close() throws IOException {
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgStreams)
      FlowControlDuplexStream.trace(
	"FlowControlDxOutputStream(" + id + "): close()");
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgSynchro)
      FlowControlDuplexStream.trace(
	"FlowControlDxOutputStream(" + id + "): get lock");
    synchronized (lock) {
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxOutputStream(" + id + "): got lock");
      flush();
      out.close();
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxOutputStream(" + id + "): free lock");
    }
  }
}
