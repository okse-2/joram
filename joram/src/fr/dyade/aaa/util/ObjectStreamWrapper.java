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
 * There seems to be random errors due to the AIX JIT, when interacting with
 * the garbage collector, in function <code>writeObject</code> of class
 * <code>ObjectOutputStream</code>. These errors have been detected on AIX,
 * with jdk 1.1.6 and 1.1.8, and never on NT. Here are some examples of the
 * exception stacks obtained:
 * <br> java.io.InvalidClassException: Field number too big
 * <br> 	at java.io.ObjectOutputStream.defaultWriteObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.outputObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.writeObject(Compiled Code)
 * <br> java.io.InvalidClassException: Field number too big
 * <br> 	at java.io.ObjectOutputStream.defaultWriteObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.outputObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.writeObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.outputArray(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.writeObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.defaultWriteObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.outputObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.writeObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.defaultWriteObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.outputObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.writeObject(Compiled Code)
 * <br> 	at java.util.Hashtable.writeObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.outputObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.writeObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.defaultWriteObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.outputObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.writeObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.defaultWriteObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.outputObject(Compiled Code)
 * <br> 	at java.io.ObjectOutputStream.writeObject(Compiled Code)
 * <br> java.lang.NoClassDefFoundError
 * <p>
 * The associated input stream receives a <code>WriteAbortedException</code>
 * exception.
 * <p>
 * It seems also that a second call to <code>writeObject</code> after a gc
 * pass usually succeeds. This class provides this work around as two static
 * functions to be called instead of the <code>readObject</code> and
 * <code>writeObject</code> functions. The functions reset and flush the stream
 * after each object is written. They should not be used when the global state
 * of the streams need to be kept for one call to the next.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 */
public final class ObjectStreamWrapper {
public static final String RCS_VERSION="@(#)$Id: ObjectStreamWrapper.java,v 1.11 2004-03-16 10:03:45 fmaistre Exp $";

  /**
   * if <code>true</code>, enables debug tracing code.
   */
  public static final boolean DEBUG = true;

  /**
   * if <code>true</code>, traces caught exceptions.
   * Requires <code>DEBUG</code> to be <code>true</code>.
   * May be set in an agent server using property
   * <code>Debug.var.fr.dyade.aaa.util.ObjectStreamWrapper.dbgException</code>.
   * Default value is <code>false</code>.
   */
  public static boolean dbgException = false;

  /** object used for synchronizing calls to <code>trace</code> */
  static Object lock = new Object();

  /**
   * Traces message.
   * <p>
   * This function is synchronized, allowing for concurrent calls from
   * multiple threads.
   *
   * @param msg		message to trace
   */
  public static void trace(String msg) {
    synchronized (lock) {
      System.out.print(Thread.currentThread());
      System.out.print(" [");
      System.out.print(System.currentTimeMillis());
      System.out.print("] ");
      System.out.println(msg);
    }
  }

  /**
   * Encapsulates a <code>writeObject</code> call.
   * Catches <code>InvalidClassException</code> and
   * <code>NoClassDefFoundError</code> exceptions.
   *
   * @param os		output stream
   * @param obj		object to write
   * @exception IOException
   *	thrown by the underlying stream operations
   */
  public static final void writeObject(ObjectOutputStream os, Object obj)
    throws IOException {
    int retries = 3;
    writeLoop:
    while (true) {
      try {
	os.writeObject(obj);
	os.reset();
	os.flush();
	break writeLoop;
      } catch (InvalidClassException exc) {
	if (retries <= 0) throw exc;
      } catch (NoClassDefFoundError exc) {
	if (retries <= 0) throw exc;
      }
      // tries again
      if (DEBUG && dbgException)
	trace("ObjectStreamWrapper.writeObject: " +
	      "retry (" + retries + ") after exception");
      retries --;
      os.reset();
      Runtime.getRuntime().gc();
    }
  }

  /**
   * Encapsulates a <code>readObject</code> call.
   * Catches <code>WriteAbortedException</code> exceptions.
   *
   * @param is		input stream
   * @return		read object
   * @exception OptionalDataException
   *	thrown by the underlying stream <code>readObject</code> operation
   * @exception ClassNotFoundException
   *	thrown by the underlying stream <code>readObject</code> operation
   * @exception IOException
   *	thrown by the underlying stream operations
   */
  public static final Object readObject(ObjectInputStream is)
    throws OptionalDataException, ClassNotFoundException, IOException {
    Object ret = null;
      readLoop:
    while (true) {
      try {
	ret = is.readObject();
	break readLoop;
      } catch (WriteAbortedException exc) {
	// the stream should be reset by the reset call on the output stream
	if (DEBUG && dbgException)
	  trace("ObjectStreamWrapper.readObject: " +
		"catch " + exc);
      }
    }
    return ret;
  }

  /**
   * Writes an object to a file.
   * Creates a file which contains only the object.
   *
   * @param of		output file
   * @param obj		object to write
   * @param sync	syncs file descriptor when <code>true</code>
   * @exception IOException
   *	thrown by the underlying stream operations
   */
  public static final void writeObject(File of, Object obj, boolean sync)
    throws IOException {
    int retries = 3;
    writeLoop:
    while (true) {
      FileOutputStream fos = null;
      try {
	fos = new FileOutputStream(of);
	ObjectOutputStream oos = new ObjectOutputStream(fos);
	oos.writeObject(obj);
	oos.flush();
	if (sync)
	  fos.getFD().sync();
	break writeLoop;
      } catch (InvalidClassException exc) {
	if (retries <= 0) throw exc;
      } catch (NoClassDefFoundError exc) {
	if (retries <= 0) throw exc;
      } finally {
	if (fos != null) {
	  fos.close();
	  fos = null;
	}
      }
      // tries again
      if (DEBUG && dbgException)
	trace("ObjectStreamWrapper.writeObject(File): " +
	      "retry (" + retries + ") after exception");
      retries --;
      Runtime.getRuntime().gc();
    }
  }

  /**
   * Builds the byte array image of an object, using an
   * <code>ObjectOutputStream</code> object.
   *
   * @param obj		object to encode
   * @param size	initial size of the <code>ByteArrayOutputStream</code>
   *			created internally, undef if not positive
   * @return		byte array image of the object
   * @exception IOException
   *	thrown by the underlying stream operations
   */
  public static final byte[] toByteArray(Object obj, int size)
    throws IOException {

    ByteArrayOutputStream bos;
    if (size > 0)
      bos = new ByteArrayOutputStream(size);
    else
      bos = new ByteArrayOutputStream();

    int retries = 3;
    writeLoop:
    while (true) {
      try {
	ObjectOutputStream oos = new ObjectOutputStream(bos);
	oos.writeObject(obj);
	oos.flush();
	break writeLoop;
      } catch (InvalidClassException exc) {
	if (retries <= 0) throw exc;
      } catch (NoClassDefFoundError exc) {
	if (retries <= 0) throw exc;
      }
      // there seems to be random errors due to the AIX JIT
      // this may also be related to the garbage collector ...
      // tries again
      if (DEBUG && dbgException)
	trace("ObjectStreamWrapper.toByteArray(): " +
	      "retry (" + retries + ") after exception");
      retries --;
      Runtime.getRuntime().gc();
      bos.reset();
    }

    return bos.toByteArray();
  }

  /**
   * Builds the byte array image of an object, using an
   * <code>ObjectOutputStream</code> object.
   *
   * @param obj		object to encode
   * @return		byte array image of the object
   * @exception IOException
   *	thrown by the underlying stream operations
   */
  public static final byte[] toByteArray(Object obj) throws IOException {
    return toByteArray(obj, 0);
  }
}
