/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package fr.dyade.aaa.util;

import java.io.*;
import java.util.*;

final class TOperation implements Serializable {
public static final String RCS_VERSION="@(#)$Id: ATransaction.java,v 1.1.1.1 2000-05-30 11:45:21 tachkeni Exp $";
  static final int SAVE = 1;
  static final int DELETE = 2;
  static final int COMMIT = 3;
 
  int type;
  String name;
  byte[] value;

  TOperation(int type, String name) {
    this.type = type;
    this.name = name;
  }

  TOperation(int type, String name, byte[] value) {
    this.type = type;
    this.name = name;
    this.value = value;
  }
}

public final class ATransaction implements Transaction, Runnable {
  // State of the transaction monitor.
  private int phase;

  static private final int INIT = 0;	  // Initialization state
  static private final int FREE = 1;	  // No transaction 
  static private final int RUN = 2;	  // A transaction is running
  static private final int COMMIT = 3;	  // A transaction is commiting
  static private final int ROLLBACK = 4;  // A transaction is aborting
  static private final int GARBAGE = 5;	  // A garbage phase start
  static private final int FINALIZE = 6;  // During last garbage.

  /**
   *  Log of all operations do by the current transaction. Its log is
   * only used by the "users" Thread during the transaction. On commit, its
   * content is added to current log (clog, memory + disk), then it is
   * freed.
   */
  private Hashtable log = null;
  /**
   *  Log of all operations already commited but not reported on disk
   * by the "garbage" Thread. On event (at least previous log plog must
   * be empty), it is moved to plog.
   */
  private Hashtable clog = null;
  /**
   *  Log currently used by "garbage" Thread, its thread reports all
   * operation it contents on disk, then it deletes it.
   */
  private Hashtable plog = null;

  private File dir = null;

  static private final String LOG = "log";
  static private final String PLOG = "plog";
  private RandomAccessFile logFile = null; 
  private FileDescriptor logFD = null;
  
  // State of the garbage.
  private boolean garbage;
  private Object lock = null;
  private boolean isRunning;

  private Thread gThread = null;

  static final boolean debug = false;

  static final void trace(String BPT) {
    if (debug) {
      System.out.println(BPT + " [" + Thread.currentThread() + "]");
      new Throwable().printStackTrace();
    }
  }

  public ATransaction(String path) throws IOException {
    phase = INIT;

    /*  Search for log files: plog then clog, reads it, then apply all
     * committed operation, finally deletes it.
     */
    dir = new File(path);
    if (!dir.exists()) dir.mkdir();
    if (!dir.isDirectory())
      throw new FileNotFoundException(path + " is not a directory.");

    restart(PLOG);
    restart(LOG);

    log = new Hashtable(20);
    clog = new Hashtable(151);
    // the object created here will never be used.
    plog = new Hashtable(11);

    logFile = new RandomAccessFile(new File(dir, LOG), "rw");
    logFD = logFile.getFD();

    lock = new Object();
    garbage = false;
    gThread = new Thread(this, "TGarbage");
    gThread.start();

    /* The Transaction subsystem is ready */
    setPhase(FREE);
  }

  private void restart(String logname) throws IOException {
    File file = new File(dir, logname);
    if ((file.exists()) && (file.length() > 0)) {
      RandomAccessFile logFile = new RandomAccessFile(file, "r");
      Hashtable log = new Hashtable();
      try {
	while (true) {
	  int op;
	  String name;
	  while ((op = logFile.read()) != TOperation.COMMIT) {
	    name = logFile.readUTF();
	    if (op == TOperation.SAVE) {
	      byte buf[] = new byte[logFile.readInt()];
	      logFile.readFully(buf);
	      log.put(name, new TOperation(op, name, buf));
	    } else {
	      log.put(name, new TOperation(op, name));
	    }
	  }
	  commit(log);
	}
      } catch (EOFException exc) {
	logFile.close();
	file.delete();
      } catch (IOException exc) {
	logFile.close();
	throw exc;
      }
    }
  }

  public File getDir() {
    return dir;
  }

  private final void setPhase(int newPhase) {
    trace("setPhase -> " + newPhase);
    phase = newPhase;
  }


  public synchronized void begin() throws IOException {
    trace("begin1");
    while (phase != FREE) {
      try {
	wait();
      } catch (InterruptedException exc) {
      }
    }
    // Change the transaction state.
    setPhase(RUN);
    trace("begin2");
  }

  // Be careful: only used in Server 
  public String[] getList(String prefix) {
    return dir.list(new StartWithFilter(prefix));
  }

  public void save(Serializable obj, String name) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(obj);
    oos.flush();

    if (phase == RUN) {
      // We are during a transaction put the new state in the log.
      log.put(name, new TOperation(TOperation.SAVE,
				   name,
				   bos.toByteArray()));
    } else {
      // Save the new state on the disk.
//       FileOutputStream fos = new FileOutputStream(new File(dir, name));
//       fos.write(bobj);
//       fos.close();
      clog.put(name, new TOperation(TOperation.SAVE,
				    name,
				    bos.toByteArray()));
    }
  }

  private final Object getFromLog(Hashtable log, String name)
    throws IOException, ClassNotFoundException {
    // Searchs in the log a new value for the object.
    TOperation op = (TOperation) log.get(name);
    if (op != null) {
      if (op.type == TOperation.SAVE) {
	ByteArrayInputStream bis = new ByteArrayInputStream(op.value);
	ObjectInputStream ois = new ObjectInputStream(bis);
	  
	return ois.readObject();
      } else if (op.type == TOperation.DELETE) {
	// l'objet a *t* d*truit.
	return null;
      }
    }
    return null;
  }

  public Object load(String name) throws IOException, ClassNotFoundException {
    Object obj;

    // First searchs in the logs a new value for the object.
    if ((phase == RUN) && ((obj = getFromLog(log, name)) != null)) {
      return obj;
    }

    if (((obj = getFromLog(clog, name)) != null) ||
	((obj = getFromLog(plog, name)) != null)) {
      return obj;
    }

    // Gets it from disk.
    try {
      File file = new File(dir, name);
      FileInputStream fis = new FileInputStream(file);
      
      // I'm not sure we can directly read the object without use
      // a ByteArrayInputStream.
      ObjectInputStream ois = new ObjectInputStream(fis);
      obj = ois.readObject();
      
      fis.close();
    } catch (FileNotFoundException exc) {
      return null;
    }

    return obj;
  }

  public void delete(String name) {
    if (phase != RUN) {
      throw new IllegalStateException("Can not delete object " +
				      name +
				      " outside of a transaction.");
    }
     
    // We are during a transaction mark the object deleted in the log.
    log.put(name, new TOperation(TOperation.DELETE, name));
  }

  int nbc = 0; // Number of commited transaction in clog.
  int nbo = 0; // Number of operations reported to clog.
  int nbs = 0; // Byte amount in clog.

  public synchronized void commit() throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Can not commit.");
    
    nbc += 1; // AF: Monitoring
    for (Enumeration e = log.elements(); e.hasMoreElements(); ) {
      TOperation op = (TOperation) e.nextElement();
      nbo += 1; // AF: Monitoring

      // Reports all committed operation in clog
      clog.put(op.name, op);

      // Save the log to disk
      logFile.writeByte(op.type);
      logFile.writeUTF(op.name);
      if (op.type == TOperation.SAVE) {
	logFile.writeInt(op.value.length);
	logFile.write(op.value);
	nbs += op.value.length; // AF: Monitoring
      }
    }
    logFile.writeByte(TOperation.COMMIT);
    logFD.sync();

    log.clear();

    setPhase(COMMIT);
  }

  public synchronized void rollback() throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Can not rollback.");
    setPhase(ROLLBACK);
    log.clear();
  }

  private final static int NBC = 100;
  private final static int NBO = 300;
  private final static int NBS = 100000;

  public synchronized void release() throws IOException {
    if ((phase != COMMIT) && (phase != ROLLBACK))
      throw new IllegalStateException("Can not release transaction.");

    if (((nbc > NBC) || (nbo > NBO) || (nbs > NBS)) && !garbage) {
      synchronized (lock) {
	// wake-up the garbage thread
	garbage = true;
	// Change the transaction state.
	setPhase(GARBAGE);
	lock.notify();
      }
    } else {
      _release();
    }
  }

  private synchronized void _release() {
    // Change the transaction state.
    setPhase(FREE);
    // wake-up an eventually user's thread in begin
    notify();
  }

  /**
   * Reports all operations in log on disk.
   */
  private void commit(Hashtable log) throws IOException { 
    // Reports all operation on disk...
    for (Enumeration e = log.elements(); e.hasMoreElements(); ) {
      TOperation op = (TOperation) e.nextElement();

      if (op.type == TOperation.SAVE) {
	trace("Save " + op.name);
	FileOutputStream fos = new FileOutputStream(new File(dir, op.name));
	fos.write(op.value);
	fos.getFD().sync();
	fos.close();
      } else if (op.type == TOperation.DELETE) {
	trace("Delete " + op.name);
	new File(dir, op.name).delete();
      }
    }
  }

  public final synchronized void stop() {
    // Waits the transaction subsystem is free.
    synchronized (lock) {
      while (phase != FREE) {
	try {
	  wait();
	} catch (InterruptedException exc) {
	}

	// Change the transaction state.
	setPhase(FINALIZE);
	isRunning =  false;

	garbage = true;
	lock.notify();
      }
    }
  }

  public void run() {
    if (isRunning) return;
    isRunning = true;

    try {
      /*  If isRunning is false and garbage is true, the stop is arrived
       * during the previous garbage phase, so we have to garbaged the
       * last transactions. Normally, it should never happened because
       * we wait "Phase == Free" in stop().
       */
      while (isRunning || garbage) {
	synchronized (lock) {
	  while (! garbage) {
	    try {
	      lock.wait();
	    } catch (InterruptedException exc) {}
	  }
	}

	garbage = false;

	trace("Wakeup: " + nbc + ", " + nbo + ", " + nbs);
	nbc = nbo = nbs = 0;

	plog = clog;
	clog = new Hashtable();

	logFile.close();
	
	new File(dir, LOG).renameTo(new File(dir, PLOG));
	logFile = new RandomAccessFile(new File(dir, LOG), "rw");
	logFD = logFile.getFD();

	_release();
	  
	commit(plog);
	// plog will be cleared by next garbage.
	new File(dir, PLOG).delete();

	trace("Wakeup: end");
      }
    } catch (IOException exc) {
      // TODO: ?
      trace(this.toString() + " exits.");
      isRunning = false;
      exc.printStackTrace();
    }
  }
}
