/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Debug;

public final class ATransaction implements Transaction, Runnable {
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

  final static int CLEANUP_THRESHOLD_COMMIT = 300;
  final static int CLEANUP_THRESHOLD_OPERATION = 1500;
  final static int CLEANUP_THRESHOLD_SIZE = 2000000;

  int commitCount = 0; // Number of commited transaction in clog.
  int operationCount = 0; // Number of operations reported to clog.
  int cumulativeSize = 0; // Byte amount in clog.

  class Context {
    Hashtable log = null;
    ByteArrayOutputStream bos = null;
    ObjectOutputStream oos = null;

    Context() {
      log = new Hashtable(15);
      bos = new ByteArrayOutputStream(256);
//       try {
//         oos = new ObjectOutputStream(bos);
//       } catch (IOException exc) { }
    }
  }

  static private final byte[] OOS_STREAM_HEADER = {
    (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF)
  };

  /**
   *  ThreadLocal variable used to get the log to associate state with each
   * thread. The log contains all operations do by the current thread since
   * the last <code>commit</code>. On commit, its content is added to current
   * log (clog, memory + disk), then it is freed.
   */
  private ThreadLocal perThreadContext = null;
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

  static private final String LOCK = "lock";
  static private final String LOG = "log";
  static private final String PLOG = "plog";
  private File lockFile = null;
  private File logFilePN = null;
  private File plogFilePN = null;
  private RandomAccessFile logFile = null; 
  protected FileDescriptor logFD = null;
  
  static protected final String LOG_FILE_MODE = "rw";

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
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "ATransaction, init()");

    //  Search for log files: plog then clog, reads it, then apply all
    // committed operation, finally deletes it.
    dir = new File(path);
    if (!dir.exists()) dir.mkdir();
    if (!dir.isDirectory())
      throw new FileNotFoundException(path + " is not a directory.");

    // Saves the transaction classname in order to prevent use of a
    // different one after restart (see AgentServer.init).
    DataOutputStream dos = null;
    try {
      lockFile = new File(dir, LOCK);
      if (! lockFile.createNewFile()) {
        logmon.log(BasicLevel.FATAL,
                   "ATransaction.init(): " +
                   "Either the server is already running, " + 
                   "either you have to remove lock file: " +
                   lockFile.getAbsolutePath());
        throw new IOException("Transaction already running.");
      }
      lockFile.deleteOnExit();
      File tfc = new File(dir, "TFC");
      if (! tfc.exists()) {
        dos = new DataOutputStream(new FileOutputStream(tfc));
        dos.writeUTF(getClass().getName());
        dos.flush();
      }
    } finally {
      if (dos != null) dos.close();
    }

    logFilePN = new File(dir, LOG);
    plogFilePN = new File(dir, PLOG);

    Hashtable tempLog = new Hashtable();
    restart(tempLog, logFilePN);
    restart(tempLog, plogFilePN);
    commit(tempLog);

    plogFilePN.delete();
    logFilePN.delete();

    perThreadContext = new ThreadLocal() {
      protected synchronized Object initialValue() {
        return new Context();
      }
    };

    clog = new Hashtable(CLEANUP_THRESHOLD_OPERATION / 2);
    plog = new Hashtable(CLEANUP_THRESHOLD_OPERATION / 2);
    
    logFile = new RandomAccessFile(logFilePN, LOG_FILE_MODE);
    logFD = logFile.getFD();

    lock = new Object();
    garbage = false;
    gThread = new Thread(this, "TGarbage");
    gThread.start();

    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "ATransaction, initialized");

    /* The Transaction subsystem is ready */
    setPhase(FREE);
  }

  private void restart(Hashtable log, File logFilePN) throws IOException {
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "ATransaction, restart");

    if ((logFilePN.exists()) && (logFilePN.length() > 0)) {
      RandomAccessFile logFile = new RandomAccessFile(logFilePN, "r");
      try {
        Hashtable templog = new Hashtable();
	while (true) {
	  int optype;
          String dirName;
	  String name;
	  while ((optype = logFile.read()) != Operation.COMMIT) {
            //  Gets all operations of one committed transaction then
            // adds them to specified log.
            dirName = logFile.readUTF();
            if (dirName.length() == 0) dirName = null;
	    name = logFile.readUTF();

            Object key = OperationKey.newKey(dirName, name);

            Operation op = null;
	    if (optype == Operation.SAVE) {
	      byte buf[] = new byte[logFile.readInt()];
	      logFile.readFully(buf);
              op = Operation.alloc(optype, dirName, name, buf);
	      op = (Operation) templog.put(key, op);
	    } else {
              op = Operation.alloc(optype, dirName, name);
	      op = (Operation) templog.put(key, op);
	    }
            if (op != null) op.free();
	  }
          //  During this aggregation somes operation object can be lost due
          // to Hashtable collision...
          log.putAll(templog);
          templog.clear();
	}
      } catch (EOFException exc) {
	logFile.close();
      } catch (IOException exc) {
	logFile.close();
	throw exc;
      }
    }
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "ATransaction, started");
  }

  public File getDir() {
    return dir;
  }

  private void setPhase(int newPhase) {
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

  public void save(Serializable obj,
                   String dirName, String name) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "ATransaction, save(" + dirName + ", " + name + ")");

    Context ctx = (Context) perThreadContext.get();
    if (ctx.oos == null) {
      ctx.bos.reset();
      ctx.oos = new ObjectOutputStream(ctx.bos);
    } else {
      ctx.oos.reset();
      ctx.bos.reset();
//       ctx.bos.write((ctx.oos.STREAM_MAGIC >>> 8) & 0xFF);
//       ctx.bos.write((ctx.oos.STREAM_MAGIC >>> 0) & 0xFF);
//       ctx.bos.write((ctx.oos.STREAM_VERSION >>> 8) & 0xFF);
//       ctx.bos.write((ctx.oos.STREAM_VERSION >>> 0) & 0xFF);
      ctx.bos.write(OOS_STREAM_HEADER, 0, 4);
    }
    ctx.oos.writeObject(obj);
    ctx.oos.flush();

    saveInLog(ctx.bos.toByteArray(), dirName, name, ctx.log, false);
  }

  /**
   *  Save an object state already serialized. The byte array keeped in log is
   * a copy, so the original one may be modified.
   */
  public void saveByteArray(byte[] buf, String name) throws IOException {
    saveByteArray(buf, null, name);
  }

  /**
   *  Save an object state already serialized. The byte array keeped in log is
   * a copy, so the original one may be modified.
   */
  public void saveByteArray(byte[] buf,
                            String dirName, String name) throws IOException {
    Context ctx = (Context) perThreadContext.get();
    saveInLog(buf,
              dirName, name,
              ((Context) perThreadContext.get()).log, true);
  }

  private void saveInLog(byte[] buf,
                         String dirName, String name,
                         Hashtable log,
                         boolean copy) throws IOException {
    Object key = OperationKey.newKey(dirName, name);
    Operation op = Operation.alloc(Operation.SAVE, dirName, name, buf);
    Operation old = (Operation) log.put(key, op);
    if (copy) {
      if ((old != null) &&
          (old.type == Operation.SAVE) &&
          (old.value.length == buf.length)) {
        // reuse old buffer
        op.value = old.value;
      } else {
        // alloc a new one
        op.value = new byte[buf.length];
      }
      System.arraycopy(buf, 0, op.value, 0, buf.length);
    }
    if (old != null) old.free();

  }

//   private Object getFromLog(Hashtable log, Object key)
//     throws IOException, ClassNotFoundException {
//     // Searchs in the log a new value for the object.
//     Operation op = (Operation) log.get(key);
//     if (op != null) {
//       if (op.type == Operation.SAVE) {
// 	ByteArrayInputStream bis = new ByteArrayInputStream(op.value);
// 	ObjectInputStream ois = new ObjectInputStream(bis);	  
// 	return ois.readObject();
//       } else if (op.type == Operation.DELETE) {
// 	// The object was deleted.
// 	throw new FileNotFoundException();
//       }
//     }
//     return null;
//   }

  private byte[] getFromLog(Hashtable log, Object key) throws IOException {
    // Searchs in the log a new value for the object.
    Operation op = (Operation) log.get(key);
    if (op != null) {
      if (op.type == Operation.SAVE) {
	return op.value;
      } else if (op.type == Operation.DELETE) {
	// The object was deleted.
	throw new FileNotFoundException();
      }
    }
    return null;
  }

  private byte[] getFromLog(String dirName, String name) throws IOException {
    // First searchs in the logs a new value for the object.
    Object key = OperationKey.newKey(dirName, name);
    byte[] buf = getFromLog(((Context) perThreadContext.get()).log, key);
    if (buf != null) return buf;
    
    if (((buf = getFromLog(clog, key)) != null) ||
        ((buf = getFromLog(plog, key)) != null)) {
      return buf;
    }
    return null;  
  }

  public Object load(String name) throws IOException, ClassNotFoundException {
    return load(null, name);
  }

  public Object load(String dirName, String name) throws IOException, ClassNotFoundException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "ATransaction, load(" + dirName + ", " + name + ")");

    // First searchs in the logs a new value for the object.
    try {
      byte[] buf = getFromLog(dirName, name);
      if (buf != null) {
      	ByteArrayInputStream bis = new ByteArrayInputStream(buf);
	ObjectInputStream ois = new ObjectInputStream(bis);	  
	return ois.readObject();
      }

      // Gets it from disk.      
      File file;
      Object obj;
      if (dirName == null) {
        file = new File(dir, name);
      } else {
        File parentDir = new File(dir, dirName);
        file = new File(parentDir, name);
      }
      FileInputStream fis = new FileInputStream(file);
      ObjectInputStream ois = new ObjectInputStream(fis);
      obj = ois.readObject();
      
      fis.close();
      return obj;
    } catch (FileNotFoundException exc) {
      return null;
    }
  }

  public byte[] loadByteArray(String name) throws IOException {
    return loadByteArray(null, name);
  }

  public byte[] loadByteArray(String dirName, String name) throws IOException {
    // First searchs in the logs a new value for the object.
    try {
      byte[] buf = getFromLog(dirName, name);
      if (buf != null) return buf;

      // Gets it from disk.      
      File file;
      if (dirName == null) {
        file = new File(dir, name);
      } else {
        File parentDir = new File(dir, dirName);
        file = new File(parentDir, name);
      }
      FileInputStream fis = new FileInputStream(file);
      buf = new byte[(int) file.length()];
      for (int nb=0; nb<buf.length; ) {
        int ret = fis.read(buf, nb, buf.length-nb);
        if (ret == -1) throw new EOFException();
        nb += ret;
      }
      fis.close();

      return buf;
    } catch (FileNotFoundException exc) {
      return null;
    }
  }

  public void delete(String name) {
    delete(null, name);
  }
  
  public void delete(String dirName, String name) {
    Object key = OperationKey.newKey(dirName, name);

    Hashtable log = ((Context) perThreadContext.get()).log;
    Operation op = Operation.alloc(Operation.DELETE, dirName, name);
    op = (Operation) log.put(key, op);
    if (op != null) op.free();
  }

  private byte[] emptyUTFString = {0, 0};

  public synchronized void commit() throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Can not commit.");

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "ATransaction, commit");
    
    commitCount += 1; // AF: Monitoring
    Hashtable log = ((Context) perThreadContext.get()).log;
    Operation op = null;
    for (Enumeration e = log.elements(); e.hasMoreElements(); ) {
      op = (Operation) e.nextElement();

      operationCount += 1; // AF: Monitoring
      // Save the log to disk
      logFile.writeByte(op.type);
      if (op.dirName != null) {
        logFile.writeUTF(op.dirName);
      } else {
        logFile.write(emptyUTFString);
      }
      logFile.writeUTF(op.name);
      if (op.type == Operation.SAVE) {
	logFile.writeInt(op.value.length);
	logFile.write(op.value);
	cumulativeSize += op.value.length; // AF: Monitoring
      }

      // Reports all committed operation in clog
      op = (Operation) clog.put(OperationKey.newKey(op.dirName, op.name), op);
      if (op != null) op.free();
    }
    logFile.writeByte(Operation.COMMIT);
    syncLogFile();

    log.clear();

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "ATransaction, committed");

    setPhase(COMMIT);
  }

  protected void syncLogFile() throws IOException {
    logFD.sync();
  }

  public synchronized void rollback() throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Can not rollback.");

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "ATransaction, rollback");

    setPhase(ROLLBACK);
    ((Context) perThreadContext.get()).log.clear();
  }

  public synchronized void release() throws IOException {
    if ((phase != COMMIT) && (phase != ROLLBACK))
      throw new IllegalStateException("Can not release transaction.");

    if (((commitCount > CLEANUP_THRESHOLD_COMMIT) || (operationCount > CLEANUP_THRESHOLD_OPERATION) || (cumulativeSize > CLEANUP_THRESHOLD_SIZE)) && !garbage) {
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
   * Reports all logged operations on disk.
   */
  private void commit(Hashtable log) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
		 "ATransaction, Commit(" + log + ")");

    Operation op = null;
    for (Enumeration e = log.elements(); e.hasMoreElements(); ) {
      op = (Operation) e.nextElement();

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
		     "ATransaction, can't delete " + file.getCanonicalPath());
      }
      op.free();
    }
    //  Be careful, do not clear log before all modifications are reported
    // to disk, in order to avoid load errors.
    log.clear();

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "ATransaction, Committed");

  }

  /**
   * Delete the specified directory if it is empty.
   * Also recursively delete the parent directories if
   * they are empty.
   */
  private final void deleteDir(File dir) {
    // Check the disk state. It may be false
    // according to the transaction log but
    // it doesn't matter because directories
    // are lazily created.
    String[] children = dir.list();
    // children may be null if dir doesn't exist any more.
    if (children != null && children.length == 0) {
      dir.delete();
      if (dir.getAbsolutePath().length() > 
          this.dir.getAbsolutePath().length()) {
        deleteDir(dir.getParentFile());
      }
    }
  }

  public final synchronized void _stop() {
    synchronized (lock) {
      while (phase != FREE) {
        // Wait for the transaction subsystem to be free
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

  public final void stop() {
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "ATransaction, stops");

    _stop();

    try {
      // And waits for this thread to die.
      gThread.join();
    } catch (InterruptedException exc3) {
      if (logmon.isLoggable(BasicLevel.WARN))
        logmon.log(BasicLevel.WARN, "ATransaction, interrupted");
    }
              
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "ATransaction, stopped");
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
          garbage = false;
	}

        wakeup();
      }
    } catch (IOException exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "ATransaction, tgarbage", exc);
      // TODO: ?
      exc.printStackTrace();
    } finally {
      isRunning = false;
      
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "ATransaction, ends");

      try {
        logFile.close();
      } catch (IOException exc) {
        logmon.log(BasicLevel.WARN, "ATransaction, can't close logfile", exc);
      }

      if (! lockFile.delete()) {
        logmon.log(BasicLevel.FATAL,
                   "ATransaction, - can't delete lockfile.");
      }

      if (logmon.isLoggable(BasicLevel.INFO))
        logmon.log(BasicLevel.INFO, "ATransaction, exits.");
    }
  }

  private final void wakeup() throws IOException {
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO,
                 "ATransaction, Wakeup: " + commitCount + ", " +
                 operationCount + ", " + cumulativeSize);
    commitCount = operationCount = cumulativeSize = 0;

    Hashtable templog = plog;
    plog = clog;
    clog = templog;

    logFile.close();
	
    logFilePN.renameTo(plogFilePN);
    logFile = new RandomAccessFile(logFilePN, LOG_FILE_MODE);
    logFD = logFile.getFD();

    _release();
	  
    commit(plog);
    // commit clears the log and frees Operations object.
    plogFilePN.delete();

    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "ATransaction, Wakeup: end");
  }
}

final class Operation implements Serializable {
  static final int SAVE = 1;
  static final int DELETE = 2;
  static final int COMMIT = 3;
  static final int KEY = 4;
 
  int type;
  String dirName;
  String name;
  byte[] value;

  private Operation(int type, String dirName, String name, byte[] value) {
    this.type = type;
    this.dirName = dirName;
    this.name = name;
    this.value = value;
  }

  /**
   * Returns a string representation for this object.
   *
   * @return	A string representation of this object. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append('(').append(super.toString());
    strbuf.append(",type=").append(type);
    strbuf.append(",dirName=").append(dirName);
    strbuf.append(",name=").append(name);
    strbuf.append(')');
    
    return strbuf.toString();
  }

  private static Pool pool = null;

  static {
    pool = new Pool("Atransaction$Operation",
                    ATransaction.CLEANUP_THRESHOLD_OPERATION);
  }

  static Operation alloc(int type, String dirName, String name) {
    return alloc(type, dirName, name, null);
  }

  static Operation alloc(int type,
                         String dirName, String name,
                         byte[] value) {
    Operation op = null;
    
    try {
      op = (Operation) pool.allocElement();
    } catch (Exception exc) {
      return new Operation(type, dirName, name, value);
    }
    op.type = type;
    op.dirName = dirName;
    op.name = name;
    op.value = value;
    return op;
  }

  void free() {
    /* to let gc do its work */
    dirName = null;
    name = null;
    value = null;
    pool.freeElement(this);
  }
}

class OperationKey {
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
