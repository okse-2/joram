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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Debug;

public final class ATransaction implements Transaction, Runnable {
  public static final String RCS_VERSION="@(#)$Id: ATransaction.java,v 1.10 2002-12-11 11:27:00 maistrfr Exp $";

  public static final String EMPTY_STRING = new String();

  // State of the transaction monitor.
  private int phase;

  private static Logger logmon = null;

  static private final int INIT = 0;	  // Initialization state
  static private final int FREE = 1;	  // No transaction 
  static private final int RUN = 2;	  // A transaction is running
  static private final int COMMIT = 3;	  // A transaction is commiting
  static private final int ROLLBACK = 4;  // A transaction is aborting
  static private final int GARBAGE = 5;	  // A garbage phase start
  static private final int FINALIZE = 6;  // During last garbage.

  class Operation implements Serializable {
    static final int SAVE = 1;
    static final int DELETE = 2;
    static final int COMMIT = 3;
 
    int type;
    String dirName;
    String name;
    byte[] value;

    Operation(int type, String dirName, String name) {
      this(type, dirName, name, null);
    }

    Operation(int type, String dirName, String name, byte[] value) {
      this.type = type;
      this.dirName = dirName;
      this.name = name;
      this.value = value;
    }
  }

//   class DbgHashtable extends Hashtable {
//     DbgHashtable() {
//       super();
//       System.out.println("#" + fr.dyade.aaa.agent.AgentServer.getServerId() + " initialize context" + " [" + Thread.currentThread() + "]");
//     }
//     protected void finalize() throws Throwable {
//       System.out.println("#" + fr.dyade.aaa.agent.AgentServer.getServerId() + " finalize context " + this + " [" + Thread.currentThread() + "]");
//     }
//   }

  class Context extends ThreadLocal {
    protected Object initialValue() {
      return new Hashtable(15);
    }
  }
  /**
   *  ThreadLocal variable used to get the log to associate state with each
   * thread. The log contains all operations do by the current thread since
   * the last <code>commit</code>. On commit, its content is added to current
   * log (clog, memory + disk), then it is freed.
   */
  private Context ctx = null;
//   /**
//    *  Log of all operations do by the current transaction. Its log is
//    * only used by the "users" Thread during the transaction. On commit, its
//    * content is added to current log (clog, memory + disk), then it is
//    * freed.
//    */
//   private Hashtable log = null;

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

  public ATransaction() {}

  public void init(String path) throws IOException {
    phase = INIT;

    logmon = Debug.getLogger(Debug.A3Debug + ".Transaction");

    /*  Search for log files: plog then clog, reads it, then apply all
     * committed operation, finally deletes it.
     */
    dir = new File(path);
    if (!dir.exists()) dir.mkdir();
    if (!dir.isDirectory())
      throw new FileNotFoundException(path + " is not a directory.");

    restart(PLOG);
    restart(LOG);

//     log = new Hashtable(20);
    ctx = new Context();
    clog = new Hashtable(400);
    // the object created here will never be used.
    plog = new Hashtable(15);

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
          String dirName;
	  String name;
	  while ((op = logFile.read()) != Operation.COMMIT) {
            dirName = logFile.readUTF();
            if (dirName.length() == 0) dirName = null;
	    name = logFile.readUTF();

            Object key = OperationKey.newKey(dirName, name);

	    if (op == Operation.SAVE) {
	      byte buf[] = new byte[logFile.readInt()];
	      logFile.readFully(buf);
	      log.put(key, new Operation(op, dirName, name, buf));
	    } else {
	      log.put(key, new Operation(op, dirName, name));
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
    phase = newPhase;
  }


  public synchronized void begin() throws IOException {
    while (phase != FREE) {
      try {
	wait();
      } catch (InterruptedException exc) {
      }
    }
    // Change the transaction state.
    setPhase(RUN);
  }

  // Be careful: only used in Server 
  public String[] getList(String prefix) {
    return dir.list(new StartWithFilter(prefix));
  }

  public void save(Serializable obj, String name) throws IOException {
    save(obj, null, name);
  }

  public final void save(Serializable obj, String dirName, String name) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(obj);
    oos.flush();

    Hashtable log = (Hashtable) ctx.get();
    // We are during a transaction put the new state in the log.
    Object key = OperationKey.newKey(dirName, name);
    log.put(key, new Operation(Operation.SAVE,
                               dirName,
                               name,
                               bos.toByteArray()));
  }

  private final Object getFromLog(Hashtable log, Object key)
    throws IOException, ClassNotFoundException {
    // Searchs in the log a new value for the object.
    Operation op = (Operation) log.get(key);
    if (op != null) {
      if (op.type == Operation.SAVE) {
	ByteArrayInputStream bis = new ByteArrayInputStream(op.value);
	ObjectInputStream ois = new ObjectInputStream(bis);	  
	return ois.readObject();
      } else if (op.type == Operation.DELETE) {
	// The object was deleted.
	throw new FileNotFoundException();
      }
    }
    return null;
  }

  public Object load(String name) throws IOException, ClassNotFoundException {
    return load(null, name);
  }

  public final Object load(String dirName, String name) throws IOException, ClassNotFoundException {
    Object key = OperationKey.newKey(dirName, name);
    Object obj;

    // First searchs in the logs a new value for the object.
    Hashtable log = (Hashtable) ctx.get();
    try {
      if ((obj = getFromLog(log, key)) != null)
        return obj;

      if (((obj = getFromLog(clog, key)) != null) ||
	  ((obj = getFromLog(plog, key)) != null)) {
        return obj;
      }

      // Gets it from disk.      
      File file;
      if (dirName == null) {
        file = new File(dir, name);
      } else {
        File parentDir = new File(dir, dirName);
        file = new File(parentDir, name);
      }
      FileInputStream fis = new FileInputStream(file);
      
      // I'm not sure we can directly read the object without use
      // a ByteArrayInputStream.
      ObjectInputStream ois = new ObjectInputStream(fis);
      obj = ois.readObject();
      
      fis.close();
      return obj;
    } catch (FileNotFoundException exc) {
      return null;
    }
  }

  public void delete(String name) {
    delete(null, name);
  }
  
  public final void delete(String dirName, String name) {
    Object key = OperationKey.newKey(dirName, name);

    // We are during a transaction mark the object deleted in the log.
    Hashtable log = (Hashtable) ctx.get();
    log.put(key, new Operation(Operation.DELETE, dirName, name));
  }

  int nbc = 0; // Number of commited transaction in clog.
  int nbo = 0; // Number of operations reported to clog.
  int nbs = 0; // Byte amount in clog.

  public synchronized void commit() throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Can not commit.");

    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO,
		 "ATransaction, Commit() - begin");
    
    nbc += 1; // AF: Monitoring
    Hashtable log = (Hashtable) ctx.get();
    Enumeration keys = log.keys();
    for (Enumeration e = log.elements(); e.hasMoreElements(); ) {
      Operation op = (Operation) e.nextElement();
      Object key = keys.nextElement();
      nbo += 1; // AF: Monitoring

      // Reports all committed operation in clog
      clog.put(key, op);

      // Save the log to disk
      logFile.writeByte(op.type);
      if (op.dirName != null) {
        logFile.writeUTF(op.dirName);
      } else {
        logFile.writeUTF(EMPTY_STRING);
      }
      logFile.writeUTF(op.name);
      if (op.type == Operation.SAVE) {
	logFile.writeInt(op.value.length);
	logFile.write(op.value);
	nbs += op.value.length; // AF: Monitoring
      }
    }
    logFile.writeByte(Operation.COMMIT);
    logFD.sync();

    log.clear();

    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO,
		 "ATransaction, Commit() - end");

    setPhase(COMMIT);
  }

  public synchronized void rollback() throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Can not rollback.");
    setPhase(ROLLBACK);
    Hashtable log = (Hashtable) ctx.get();
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
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO,
		 "ATransaction, Commit(" + log + ") - begin");

    // Reports all operation on disk...
    for (Enumeration e = log.elements(); e.hasMoreElements(); ) {
      Operation op = (Operation) e.nextElement();
      if (op.type == Operation.SAVE) {
	if (logmon.isLoggable(BasicLevel.DEBUG))
	  logmon.log(BasicLevel.DEBUG,
		     "ATransaction, Save (" + op.dirName + ',' + op.name + ')');
        
        File file;
        if (op.dirName == null) {
          file = new File(dir, op.name);
        } else {
          File parentDir = new File(dir, op.dirName);
          if (!parentDir.exists()) {
            parentDir.mkdirs();
          }
          file = new File(parentDir, op.name);
        }

	FileOutputStream fos = new FileOutputStream(file);
	fos.write(op.value);
	fos.getFD().sync();
	fos.close();
      } else if (op.type == Operation.DELETE) {
	if (logmon.isLoggable(BasicLevel.DEBUG))
	  logmon.log(BasicLevel.DEBUG,
		     "ATransaction, Delete (" + op.dirName + ',' + op.name + ')');

        File file;
        boolean deleted;
        if (op.dirName == null) {
          file = new File(dir, op.name);
          deleted = file.delete();
        } else {
          File parentDir = new File(dir, op.dirName);
          file = new File(parentDir, op.name);
          deleted = file.delete();
          deleteDir(parentDir);
        }

	if (!deleted && file.exists())
          logmon.log(BasicLevel.ERROR,
		     "ATransaction, can't delete " +
		     file.getCanonicalPath());
      }
    }

    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO,
		 "ATransaction, Commit(" + log + ") - end");

  }

  /**
   * Delete the specified directory if it is empty.
   * Also recursively delete the parent directories if
   * they are empty.
   */
  private void deleteDir(File dir) {
    // Check the disk state. It may be false
    // according to the transaction log but
    // it doesn't matter because directories
    // are lazily created.
    String[] children = dir.list();
    // children may be null if dir doesn't exist any more.
    if (children != null && 
        children.length == 0) {
      dir.delete();
      if (dir.getAbsolutePath().length() > 
          this.dir.getAbsolutePath().length()) {
        deleteDir(dir.getParentFile());
      }
    }
  }

  public final synchronized void stop() {
    synchronized (lock) {
      while (phase != FREE) {
	// Waits the transaction subsystem is free.
	try {
	  wait();
	} catch (InterruptedException exc) {
	}
      }
      // Change the transaction state.
      setPhase(FINALIZE);
      isRunning =  false;
      garbage = true;
      // Wake-up the garbage thread.
      lock.notify();
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

	if (logmon.isLoggable(BasicLevel.INFO))
	  logmon.log(BasicLevel.INFO,
		     "ATransaction, Wakeup: " + nbc + ", " + nbo + ", " + nbs);
	nbc = nbo = nbs = 0;

	plog = clog;
	clog = new Hashtable(400);

	logFile.close();
	
	new File(dir, LOG).renameTo(new File(dir, PLOG));
	logFile = new RandomAccessFile(new File(dir, LOG), "rw");
	logFD = logFile.getFD();

	_release();
	  
	commit(plog);
	// plog will be cleared by next garbage.
	new File(dir, PLOG).delete();

	if (logmon.isLoggable(BasicLevel.INFO))
	  logmon.log(BasicLevel.INFO,
		     "ATransaction, Wakeup: end");
      }
    } catch (IOException exc) {
      // TODO: ?
      exc.printStackTrace();
    } finally {
      isRunning = false;

      if (logmon.isLoggable(BasicLevel.INFO))
	  logmon.log(BasicLevel.INFO,
		     "ATransaction,  exits.");
    }
  }

  private static class OperationKey {

    static Object newKey(String dirName, String name) {
      if (dirName == null) {
        return name;
      } else {
        return new OperationKey(dirName, name);
      }
    }

    private String dirName;
    private String name;

    private OperationKey(String dirName,
                        String name) {
      this.dirName = dirName;
      this.name = name;
    }

    public int hashCode() {
      // Should compute a specific one.
      return dirName.hashCode();
    }

    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj instanceof OperationKey) {
        OperationKey opk = (OperationKey)obj;
        if (opk.name.length() != name.length()) return false;
        if (opk.dirName.length() != dirName.length()) return false;
        if (!opk.dirName.equals(dirName)) return false;            
        return opk.name.equals(name);
      }
      return false;
    }
  }
}
