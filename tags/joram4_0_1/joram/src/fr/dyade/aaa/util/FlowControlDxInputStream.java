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
 * A <code>FlowControlDxInputStream</code> object works paired with a
 * <code>FlowControlDxOutputStream</code> object. They both implement
 * a user level flow control over a duplex stream. They generalize what
 * is done by the classes <code>fr.dyade.aaa.ar.FlowControlInputStream</code> and
 * <code>fr.dyade.aaa.ar.FlowControlOutputStream</code> over a single sided stream.
 * <p>
 * See class <code>FlowControlDxOutputStream</code> for a description of
 * the algorithm.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		FlowControlDxOutputStream
 */
public class FlowControlDxInputStream extends FilterInputStream {
public static final String RCS_VERSION="@(#)$Id: FlowControlDxInputStream.java,v 1.11 2004-03-16 10:03:45 fmaistre Exp $";

  /** control output stream */
  FlowControlDxOutputStream control = null;

  /** number of bytes to read between control bytes */
  int windowSize = 0;

  /**
   * number of bytes to read before next control byte
   * <p>
   * This variable is modified only by the owner of the current read operation.
   */
  int window = 0;

  /* default value for <code>owner</code> */
  protected static final int OWNER_NONE = 0;
  /* <code>owner</code> value indicating a regular read in progress */
  protected static final int OWNER_REGULAR = 1;
  /*
   * <code>owner</code> value indicating a control read in progress,
   * header not yet read
   */
  protected static final int OWNER_CONTROL = 2;
  /**
   * owner of the current read operation
   * <br> <code>OWNER_NONE</code> when no read is in progress,
   * <br> <code>OWNER_CONTROL</code> when a control read is in progress,
   * <br> <code>OWNER_REGULAR</code> when a regular read is in progress.
   * <p>
   * This variable is modified in a block synchronized by the <code>lock</code>
   * object.
   */
  protected int owner = OWNER_NONE;

  /**
   * status of the current regular read operation
   * <br> <code>0</code> when the header has not yet been read,
   * <br> the number of regular bytes to read otherwise.
   * <p>
   * This variable is modified only by the owner of the current read operation.
   */
  protected int readStatus = 0;

  /**
   * number of read control bytes not yet handled by <code>controlRead</code>.
   * There is only one type of control byte, yet, so a counter is sufficient.
   * <p>
   * This variable is modified in a block synchronized by the <code>lock</code>
   * object.
   */
  protected int controlPending = 0;

  /**
   * array of regular bytes not yet handled by the <code>read</code> functions,
   * buffered by <code>controlRead</code> to reach the control bytes.
   * This variable is handled as a circular buffer, indexed by the variables
   * <code>regularOff</code> and <code>regularLen</code>, and which size is
   * defined by <code>regularSize</code>.
   * <p>
   * If reading and writing is performed by a single thread, writing may block
   * waiting for a control byte which is not the first byte to read from input.
   */
  protected byte[] regularPending = null;
  /** size of buffer <code>regularPending</code> */
  protected int regularSize = 0;
  /** offset of first pending regular byte in <code>regularPending</code> */
  protected int regularOff = 0;
  /** number of pending regular bytes in <code>regularPending</code> */
  protected int regularLen = 0;
  
  /**
   * synchronization object for modifying <code>owner</code> and
   * <code>controlPending</code>
   */
  protected Object lock = null;

  /** unique id, used for debugging */
  int id = -1;

  /**
   * Constructor.
   *
   * @param in		the underlying input stream
   * @param windowSize	number of bytes to read between control bytes
   * @param regularSize	number of regular bytes to buffer while reading
   *			control bytes
   */
  public FlowControlDxInputStream(InputStream in,
				  int windowSize,
				  int regularSize) {
    super(in);
    this.windowSize = windowSize;
    this.regularSize = regularSize;
    window = windowSize;
    lock = new Object();
    if (regularSize > 0)
      regularPending = new byte[regularSize];
    if (FlowControlDuplexStream.DEBUG)
      id = FlowControlDuplexStream.newId();
  }

  /**
   * Constructor with default regular buffer size value set to <code>0</code>.
   *
   * @param in		the underlying input stream
   * @param windowSize	number of bytes to read between control bytes
   */
  public FlowControlDxInputStream(InputStream in,
				  int windowSize) {
    this(in, windowSize, 0);
  }

  /**
   * Constructor with default window size value set to
   * <code>FlowControlDuplexStream.WINDOW_SIZE</code>, and default
   * regular buffer size value set to <code>0</code>.
   *
   * @param in		the underlying input stream
   */
  public FlowControlDxInputStream(InputStream in) {
    this(in, FlowControlDuplexStream.WINDOW_SIZE);
  }

  /**
   * Sets the control output stream. That stream may not be known when
   * this object is created.
   *
   * @param control	the control output stream
   */
  public void setControlOutputStream(FlowControlDxOutputStream control) {
    this.control = control;
  }

  /**
   * Reads the next byte of data from this input stream. The value byte is
   * returned as an <code>int</code> in the range <code>0</code> to
   * <code>255</code>. If no byte is available because the end of the
   * stream has been reached, the value <code>-1</code> is returned. This
   * method blocks until input data is available, the end of the stream is
   * detected, or an exception is thrown.
   * <p>
   * This function shares the underlying input stream with the function
   * <code>controlRead</code> called in another thread.
   *
   * @return		the next byte of data, or <code>-1</code> if the end
   *			of the stream is reached
   */
  public int read() throws IOException {
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgStreams)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): read()");

    int value = -2;
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgSynchro)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): get lock");
    synchronized (lock) {
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): got lock");

      // first checks for regularPending
      if (regularLen > 0) {
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgStreams)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxInputStream(" + id + "): regularLen=" + regularLen);
	value = regularPending[regularOff];
	regularOff ++;
	if (regularOff == regularSize)
	  regularOff = 0;
	regularLen --;
      } else {
	// sets owner
	owner_loop:
	while (true) {
	  switch (owner) {
	  case OWNER_NONE:
	    owner = OWNER_REGULAR;
	    break owner_loop;
	  case OWNER_REGULAR:
	    break owner_loop;
	  case OWNER_CONTROL:
	    /*
	     * this should never occur as owner moves to and from
	     * OWNER_CONTROL in a critical section
	     */
	    throw new IllegalStateException("OWNER_CONTROL in read");
	  default:
	    throw new IllegalStateException(
	      "Illegal regular read owner: " + owner);
	  }
	}
      }
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): free lock");
    }

    if (value == -2) {
      status_block:
      if (readStatus <= 0) {
	while (true) {
	  // reads the header byte
	  readStatus = in.read();
	  switch (readStatus) {
	  case -1:
	    value = -1;
	    break status_block;
	  case 0:
	    // control byte
	    // it must be read here as there is no guarantee that controlRead
	    // will be called soon
	    if (FlowControlDuplexStream.DEBUG &&
		FlowControlDuplexStream.dbgControl)
	      FlowControlDuplexStream.trace(
		"FlowControlDxInputStream(" + id + "): read control");
	    int cbyte = in.read();
	    if (FlowControlDuplexStream.DEBUG &&
		FlowControlDuplexStream.dbgControl)
	      FlowControlDuplexStream.trace(
		"FlowControlDxInputStream(" + id + "): " +
		"read control -> " + cbyte);
	    switch (cbyte) {
	    case -1:
	      value = -1;
	      break status_block;
	    case FlowControlDuplexStream.CTRL_WINDOW:
	      if (FlowControlDuplexStream.DEBUG &&
		  FlowControlDuplexStream.dbgSynchro)
		FlowControlDuplexStream.trace(
		  "FlowControlDxInputStream(" + id + "): get lock");
	      synchronized (lock) {
		if (FlowControlDuplexStream.DEBUG &&
		    FlowControlDuplexStream.dbgSynchro)
		  FlowControlDuplexStream.trace(
		    "FlowControlDxInputStream(" + id + "): got lock");
		controlPending ++;
		if (FlowControlDuplexStream.DEBUG &&
		    FlowControlDuplexStream.dbgSynchro)
		  FlowControlDuplexStream.trace(
		    "FlowControlDxInputStream(" + id + "): notify lock");
		lock.notify();
		if (FlowControlDuplexStream.DEBUG &&
		    FlowControlDuplexStream.dbgSynchro)
		  FlowControlDuplexStream.trace(
		    "FlowControlDxInputStream(" + id + "): free lock");
	      }
	      break;
	    default:
	      throw new IllegalStateException(
		"Illegal control byte: " + cbyte);
	    }
	    readStatus = 0;
	    break;
	  default:
	    break status_block;
	  }
	}
      }

      if (value == -2) {
	// regular byte to read
	value = in.read();

	if (value >= 0) {
	  readStatus --;
	  windowDecr(1);
	}
      }
    }

    if (value >= 0) {
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): get lock");
      synchronized (lock) {
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgSynchro)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxInputStream(" + id + "): got lock");
	owner = OWNER_NONE;
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgSynchro)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxInputStream(" + id + "): notify lock");
	lock.notify();
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgSynchro)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxInputStream(" + id + "): free lock");
      }
    }

    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgStreams)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): read -> " + value);

    return value;
  }

  /**
   * Reads up to <code>len</code> bytes of data from this input stream into
   * an array of bytes. This method blocks until some input is available.
   * <p>
   * This function shares the underlying input stream with the function
   * <code>controlRead</code> called in another thread.
   *
   * @param b		the buffer into which the data is read
   * @param off		the start offset of the data
   * @param len		the maximum number of bytes read
   * @return		the total number of bytes read into the buffer,
   *			or <code>-1</code> if there is no more data because the
   *			end of the stream has been reached
   */
  public int read(byte b[],
		  int off,
		  int len) throws IOException {
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgStreams)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): read(" + len + ")");

    int value = -2;
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgSynchro)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): get lock");
    synchronized (lock) {
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): got lock");

      // first checks for regularPending
      if (regularLen > 0) {
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgStreams)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxInputStream(" + id + "): regularLen=" + regularLen);
	if (regularLen < len) len = regularLen;
	int left = regularSize - regularOff;
	if (len <= left) {
	  System.arraycopy(regularPending, regularOff, b, off, len);
	  regularOff += len;
	} else {
	  System.arraycopy(regularPending, regularOff, b, off, left);
	  regularOff = len - left;
	  System.arraycopy(regularPending, 0, b, off + left, regularOff);
	}
	regularLen -= len;
	if (regularLen == 0)
	  regularOff = 0;
	value = len;
      } else {
	// sets owner
	owner_loop:
	while (true) {
	  switch (owner) {
	  case OWNER_NONE:
	    owner = OWNER_REGULAR;
	    break owner_loop;
	  case OWNER_REGULAR:
	    break owner_loop;
	  case OWNER_CONTROL:
	    /*
	     * this should never occur as owner moves to and from
	     * OWNER_CONTROL in a critical section
	     */
	    throw new IllegalStateException("OWNER_CONTROL in read");
	  default:
	    throw new IllegalStateException(
	      "Illegal regular read owner: " + owner);
	  }
	}
      }
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): free lock");
    }

    if (value == -2) {
      status_block:
      if (readStatus <= 0) {
	while (true) {
	  // reads the header byte
	  readStatus = in.read();
	  switch (readStatus) {
	  case -1:
	    value = -1;
	    break status_block;
	  case 0:
	    // control byte
	    // it must be read here as there is no guarantee that controlRead
	    // will be called soon
	    if (FlowControlDuplexStream.DEBUG &&
		FlowControlDuplexStream.dbgControl)
	      FlowControlDuplexStream.trace(
		"FlowControlDxInputStream(" + id + "): read control");
	    int cbyte = in.read();
	    if (FlowControlDuplexStream.DEBUG &&
		FlowControlDuplexStream.dbgControl)
	      FlowControlDuplexStream.trace(
		"FlowControlDxInputStream(" + id + "): " +
		"read control -> " + cbyte);
	    switch (cbyte) {
	    case -1:
	      value = -1;
	      break status_block;
	    case FlowControlDuplexStream.CTRL_WINDOW:
	      if (FlowControlDuplexStream.DEBUG &&
		  FlowControlDuplexStream.dbgSynchro)
		FlowControlDuplexStream.trace(
		  "FlowControlDxInputStream(" + id + "): get lock");
	      synchronized (lock) {
		if (FlowControlDuplexStream.DEBUG &&
		    FlowControlDuplexStream.dbgSynchro)
		  FlowControlDuplexStream.trace(
		    "FlowControlDxInputStream(" + id + "): got lock");
		controlPending ++;
		if (FlowControlDuplexStream.DEBUG &&
		    FlowControlDuplexStream.dbgSynchro)
		  FlowControlDuplexStream.trace(
		    "FlowControlDxInputStream(" + id + "): notify lock");
		lock.notify();
		if (FlowControlDuplexStream.DEBUG &&
		    FlowControlDuplexStream.dbgSynchro)
		  FlowControlDuplexStream.trace(
		    "FlowControlDxInputStream(" + id + "): free lock");
	      }
	      break;
	    default:
	      throw new IllegalStateException(
		"Illegal control byte: " + cbyte);
	    }
	    readStatus = 0;
	    break;
	  default:
	    break status_block;
	  }
	}
      }

      if (value == -2) {
	// regular bytes to read
	if (readStatus < len)
	  len = readStatus;
	value = doRead(b, off, len);

	if (value >= 0) {
	  readStatus -= value;
	  windowDecr(value);
	}
      }
    }

    if (value >= 0) {
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): get lock");
      synchronized (lock) {
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgSynchro)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxInputStream(" + id + "): got lock");
	owner = OWNER_NONE;
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgSynchro)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxInputStream(" + id + "): notify lock");
	lock.notify();
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgSynchro)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxInputStream(" + id + "): free lock");
      }
    }

    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgStreams)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): read -> " + value);

    return value;
  }

  /**
   * Skips over and discards <code>n</code> bytes of data from the input stream.
   * The <code>skip</code> method may, for a variety of reasons, end up skipping
   * over some smaller number of bytes, possibly <code>0</code>. The actual
   * number of bytes skipped is returned.
   * <p>
   * This function shares the underlying input stream with the function
   * <code>controlRead</code> called in another thread.
   *
   * @param n		the number of bytes to be skipped
   * @return		the actual number of bytes skipped		
   */
  public long skip(long n) throws IOException {
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgStreams)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): skip(" + n + ")");

    long value = 0;

    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgSynchro)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): get lock");
    synchronized (lock) {
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): got lock");

      // first checks for regularPending
      if (regularLen > 0) {
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgStreams)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxInputStream(" + id + "): regularLen=" + regularLen);
	if (regularLen <= n) {
	  value = regularLen;
	  n -= regularLen;
	  regularOff = 0;
	  regularLen = 0;
	} else {
	  value = n;
	  regularOff += n;
	  if (regularOff >= regularSize)
	    regularOff -= regularSize;
	  regularLen -= n;
	  n = 0;
	}
      }

      if (n > 0) {
	// sets owner
	owner_loop:
	while (true) {
	  switch (owner) {
	  case OWNER_NONE:
	    owner = OWNER_REGULAR;
	    break owner_loop;
	  case OWNER_REGULAR:
	    break owner_loop;
	  case OWNER_CONTROL:
	    /*
	     * this should never occur as owner moves to and from
	     * OWNER_CONTROL in a critical section
	     */
	    throw new IllegalStateException("OWNER_CONTROL in read");
	  default:
	    throw new IllegalStateException(
	      "Illegal regular read owner: " + owner);
	  }
	}
      }
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): free lock");
    }

    skip_loop:
    while (n > 0) {
      status_loop:
      while (readStatus <= 0) {
	// reads the header byte
	readStatus = in.read();
	switch (readStatus) {
	case -1:
	  break skip_loop;
	case 0:
	  // control byte
	  // it must be read here as there is no guarantee that controlRead
	  // will be called soon
	  if (FlowControlDuplexStream.DEBUG &&
	      FlowControlDuplexStream.dbgControl)
	    FlowControlDuplexStream.trace(
	      "FlowControlDxInputStream(" + id + "): read control");
	  int cbyte = in.read();
	  if (FlowControlDuplexStream.DEBUG &&
	      FlowControlDuplexStream.dbgControl)
	    FlowControlDuplexStream.trace(
	      "FlowControlDxInputStream(" + id + "): " +
	      "read control -> " + cbyte);
	  switch (cbyte) {
	  case -1:
	    break skip_loop;
	  case FlowControlDuplexStream.CTRL_WINDOW:
	    if (FlowControlDuplexStream.DEBUG &&
		FlowControlDuplexStream.dbgSynchro)
	      FlowControlDuplexStream.trace(
		"FlowControlDxInputStream(" + id + "): get lock");
	    synchronized (lock) {
	      if (FlowControlDuplexStream.DEBUG &&
		  FlowControlDuplexStream.dbgSynchro)
		FlowControlDuplexStream.trace(
		  "FlowControlDxInputStream(" + id + "): got lock");
	      controlPending ++;
	      if (FlowControlDuplexStream.DEBUG &&
		  FlowControlDuplexStream.dbgSynchro)
		FlowControlDuplexStream.trace(
		  "FlowControlDxInputStream(" + id + "): notify lock");
	      lock.notify();
	      if (FlowControlDuplexStream.DEBUG &&
		  FlowControlDuplexStream.dbgSynchro)
		FlowControlDuplexStream.trace(
		  "FlowControlDxInputStream(" + id + "): free lock");
	    }
	    break;
	  default:
	    throw new IllegalStateException(
	      "Illegal control byte: " + cbyte);
	  }
	  readStatus = 0;
	  break;
	default:
	  break status_loop;
	}
      }

      // regular bytes to read
      long toskip = n;
      if (window < toskip) toskip = window;
      if (readStatus < toskip) toskip = readStatus;
      int skipped = (int) in.skip(toskip);
      value += skipped;
      readStatus -= skipped;
      n -= toskip;

      // in this call both input and output locks are hold
      // this is not a problem as this does not occur during writing
      windowDecr(skipped);
    }

    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgSynchro)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): get lock");
    synchronized (lock) {
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): got lock");
      owner = OWNER_NONE;
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): notify lock");
      lock.notify();
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): free lock");
    }

    return value;
  }

  /**
   * Actually reads up to <code>len</code> bytes of data from this input stream
   * into an array of bytes. This method blocks until some input is available.
   * <p>
   * This function is used in both input thread (regular <code>read</code>
   * functions) and output thread (function <code>controlRead</code>).
   * It is not synchronized by itself, only by the client code.
   *
   * @param b		the buffer into which the data is read
   * @param off		the start offset of the data
   * @param len		the maximum number of bytes read
   * @return		the total number of bytes read into the buffer,
   *			or <code>-1</code> if there is no more data because the
   *			end of the stream has been reached
   */
  protected final int doRead(byte b[],
			     int off,
			     int len) throws IOException {
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgStreams)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): in.read(" + len + ")");
    int value = in.read(b, off, len);
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgStreams)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): in.read -> " + value);
    return value;
  }

  /**
   * Decrements the window value.
   * <p>
   * This function is used in both input thread (regular <code>read</code> functions)
   * and output thread (function <code>controlRead</code>). It is not synchronized
   * by itself, only by the client code.
   *
   * @param value	decrement value
   */
  protected final void windowDecr(int value) throws IOException {
    window -= value;
    if (window <= 0) {
      while (window <= 0) {
	if (FlowControlDuplexStream.DEBUG &&
	    FlowControlDuplexStream.dbgControl)
	  FlowControlDuplexStream.trace(
	    "FlowControlDxInputStream(" + id + "): control write");
	control.controlWrite(FlowControlDuplexStream.CTRL_WINDOW);
	window += windowSize;
      }
      control.flush();
    }
  }

  /**
   * Reads the next control byte from this input stream. The value byte is
   * returned as an <code>int</code> in the range <code>0</code> to
   * <code>255</code>. If no byte is available because the end of the
   * stream has been reached, the value <code>-1</code> is returned. This
   * method blocks until input data is available, the end of the stream is
   * detected, or an exception is thrown.
   * <p>
   * This function shares the underlying input stream with the functions
   * <code>read</code> called in another thread.
   *
   * @return		the next control byte, or <code>-1</code> if the end
   *			of the stream is reached
   */
  public int controlRead() throws IOException {
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgControl)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): controlRead()");

    int value = -1;
    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgSynchro)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): get lock");
    synchronized (lock) {
      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): got lock");

      control_loop:
      while (true) {
	// first checks for controlPending
	if (controlPending > 0) {
	  if (FlowControlDuplexStream.DEBUG &&
	      FlowControlDuplexStream.dbgControl)
	    FlowControlDuplexStream.trace(
	      "FlowControlDxInputStream(" + id + "): " +
	      "controlPending=" + controlPending);
	  controlPending --;
	  value = FlowControlDuplexStream.CTRL_WINDOW;
	  break control_loop;
	}

	boolean wait = false;

	// sets owner
	switch (owner) {
	case OWNER_REGULAR:
	  wait = true;
	  break;
	default:
	  throw new IllegalStateException(
	    "Illegal control read owner: " + owner);
	case OWNER_NONE:
	  owner = OWNER_CONTROL;

	  // keeps the lock as a regular read has nothing to do without lock

	  if (readStatus <= 0) {
	    // reads the header byte
	    readStatus = in.read();
	    switch (readStatus) {
	    case -1:
	      value = -1;
	      owner = OWNER_NONE;
	      break control_loop;
	    case 0:
	      // control byte to read
	      value = in.read();
	      owner = OWNER_NONE;
	      break control_loop;
	    default:
	      // regular byte to read
	      break;
	    }
	  }

	  if ((regularSize - regularLen) >= readStatus) {
	    if (FlowControlDuplexStream.DEBUG &&
		FlowControlDuplexStream.dbgStreams)
	      FlowControlDuplexStream.trace(
		"FlowControlDxInputStream(" + id + "): " +
		"buffer regular read(" + readStatus + ")");
	    // there is enough room to read the regular bytes
	    int start = regularOff + regularLen;
	    if (start >= regularSize)
	      start -= regularSize;
	    int len = regularSize - start;
	    if (readStatus < len) len = readStatus;
	    while (readStatus > 0) {
	      while (len > 0) {
		int read = doRead(regularPending, start, len);
		if (read == -1) {
		  value = -1;
		  owner = OWNER_NONE;
		  break control_loop;
		}
		readStatus -= read;
		windowDecr(read);
		len -= read;
		start += read;
		regularLen += read;
	      }
	      start = 0;
	      len = readStatus;
	    }
	    owner = OWNER_NONE;
	  } else {
	    // lets a regular read function read the bytes
	    if (FlowControlDuplexStream.DEBUG &&
		FlowControlDuplexStream.dbgControl)
	      FlowControlDuplexStream.trace(
		"FlowControlDxInputStream(" + id + "): " +
		"buffer full (" + regularLen + "/" + regularSize + "), " +
		"regular read " + readStatus);
	    owner = OWNER_NONE;
	    wait = true;
	  }
	  break;
	}

	if (wait) {
	  try {
	    if (FlowControlDuplexStream.DEBUG &&
		FlowControlDuplexStream.dbgSynchro)
	      FlowControlDuplexStream.trace(
		"FlowControlDxInputStream(" + id + "): wait lock");
	    lock.wait();
	    if (FlowControlDuplexStream.DEBUG &&
		FlowControlDuplexStream.dbgSynchro)
	      FlowControlDuplexStream.trace(
		"FlowControlDxInputStream(" + id + "): resume lock");
	  } catch (InterruptedException e) {}
	}
      }

      if (FlowControlDuplexStream.DEBUG &&
	  FlowControlDuplexStream.dbgSynchro)
	FlowControlDuplexStream.trace(
	  "FlowControlDxInputStream(" + id + "): free lock");
    }

    if (FlowControlDuplexStream.DEBUG &&
	FlowControlDuplexStream.dbgControl)
      FlowControlDuplexStream.trace(
	"FlowControlDxInputStream(" + id + "): controlRead -> " + value);

    return value;
  }

  /**
   * Tests if this input stream supports the mark and reset methods.
   *
   * @return	<code>true</code> if this stream type supports the
   *		<code>mark</code> and <code>reset</code> method;
   *		<code>false</code> otherwise
   */
  public boolean markSupported() {
    return false;
  }
}
