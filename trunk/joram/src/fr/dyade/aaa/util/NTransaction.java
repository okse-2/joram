/*
 * Copyright (C) 2004 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package fr.dyade.aaa.util;

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Debug;

public final class NTransaction implements Transaction {
  // Logging monitor
  private static Logger logmon = null;

  // State of the transaction monitor.
  private int phase;

  static private final int INIT = 0;	  // Initialization state
  static private final int FREE = 1;	  // No transaction 
  static private final int RUN = 2;	  // A transaction is running
  static private final int COMMIT = 3;	  // A transaction is commiting
  static private final int ROLLBACK = 4;  // A transaction is aborting
  static private final int FINALIZE = 5;  // During last garbage.

  final static int Kb = 1024;
  final static int Mb = Kb * Kb;

  final static int CLEANUP_THRESHOLD_OPERATION = 8192;
  final static int CLEANUP_THRESHOLD_SIZE = 16 * Mb;

  /** Log context associated with each Thread using NTransaction. */
  private class Context {
    Hashtable log = null;
    ByteArrayOutputStream bos = null;
    ObjectOutputStream oos = null;

    Context() {
      log = new Hashtable(15);
      bos = new ByteArrayOutputStream(256);
    }
  }

  File dir = null;

  LogFile logFile = null;

  /**
   *  ThreadLocal variable used to get the log to associate state with each
   * thread. The log contains all operations do by the current thread since
   * the last <code>commit</code>. On commit, its content is added to current
   * log (memory + disk), then it is freed.
   */
  private ThreadLocal perThreadContext = null;

  static final boolean debug = false;

  public NTransaction() {}

  public boolean isPersistent() {
    return true;
  }

  public final void init(String path) throws IOException {
    phase = INIT;

    logmon = Debug.getLogger(Debug.A3Debug + ".Transaction");
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "NTransaction, init()");

    dir = new File(path);
    if (!dir.exists()) dir.mkdir();
    if (!dir.isDirectory())
      throw new FileNotFoundException(path + " is not a directory.");

    logFile = new LogFile(dir);

    // Saves the transaction classname in order to prevent use of a
    // different one after restart (see AgentServer.init).
    DataOutputStream ldos = null;
    try {
      File tfc = new File(dir, "TFC");
      if (! tfc.exists()) {
        ldos = new DataOutputStream(new FileOutputStream(tfc));
        ldos.writeUTF(getClass().getName());
        ldos.flush();
      }
    } finally {
      if (ldos != null) ldos.close();
    }

    perThreadContext = new ThreadLocal() {
        protected synchronized Object initialValue() {
          return new Context();
        }
      };

    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "NTransaction, initialized");

    /* The Transaction subsystem is ready */
    setPhase(FREE);
  }

  public final File getDir() {
    return dir;
  }

  private final void setPhase(int newPhase) {
    phase = newPhase;
  }


  public final synchronized void begin() throws IOException {
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
  public final String[] getList(String prefix) {
    return dir.list(new StartWithFilter(prefix));
  }

  public final void save(Serializable obj, String name) throws IOException {
    save(obj, null, name);
  }

  static private final byte[] OOS_STREAM_HEADER = {
    (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF)
  };

  public final void save(Serializable obj,
                         String dirName, String name) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "NTransaction, save(" + dirName + ", " + name + ")");

    Context ctx = (Context) perThreadContext.get();
    if (ctx.oos == null) {
      ctx.bos.reset();
      ctx.oos = new ObjectOutputStream(ctx.bos);
    } else {
      ctx.oos.reset();
      ctx.bos.reset();
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
  public final void saveByteArray(byte[] buf, String name) throws IOException {
    saveByteArray(buf, null, name);
  }

  /**
   *  Save an object state already serialized. The byte array keeped in log is
   * a copy, so the original one may be modified.
   */
  public final void saveByteArray(byte[] buf,
                                  String dirName, String name) throws IOException {
    Context ctx = (Context) perThreadContext.get();
    saveInLog(buf,
              dirName, name,
              ((Context) perThreadContext.get()).log, true);
  }

  private final void saveInLog(byte[] buf,
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

  private final byte[] getFromLog(Hashtable log, Object key) throws IOException {
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

  private final byte[] getFromLog(String dirName, String name) throws IOException {
    // First searchs in the logs a new value for the object.
    Object key = OperationKey.newKey(dirName, name);
    byte[] buf = getFromLog(((Context) perThreadContext.get()).log, key);
    if (buf != null) return buf;
    
    if ((buf = getFromLog(logFile.log, key)) != null) {
      return buf;
    }

    return null;  
  }

  public final Object load(String name) throws IOException, ClassNotFoundException {
    return load(null, name);
  }

  public final Object load(String dirName, String name) throws IOException, ClassNotFoundException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "NTransaction, load(" + dirName + ", " + name + ")");

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

  public final byte[] loadByteArray(String name) throws IOException {
    return loadByteArray(null, name);
  }


  public final byte[] loadByteArray(String dirName, String name) throws IOException {
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

  public final void delete(String name) {
    delete(null, name);
  }
  
  public final void delete(String dirName, String name) {
    Object key = OperationKey.newKey(dirName, name);

    Hashtable log = ((Context) perThreadContext.get()).log;
    Operation op = Operation.alloc(Operation.DELETE, dirName, name);
    op = (Operation) log.put(key, op);
    if (op != null) op.free();
  }

  public final synchronized void commit() throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Can not commit.");

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "NTransaction, commit");
    
    Hashtable log = ((Context) perThreadContext.get()).log;
    if (! log.isEmpty()) {
      logFile.commit(log);
      log.clear();
    }

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "NTransaction, committed");

    setPhase(COMMIT);
  }

  public final synchronized void rollback() throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Can not rollback.");

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "NTransaction, rollback");

    setPhase(ROLLBACK);
    ((Context) perThreadContext.get()).log.clear();
  }

  public final synchronized void release() throws IOException {
    if ((phase != COMMIT) && (phase != ROLLBACK))
      throw new IllegalStateException("Can not release transaction.");

    // Change the transaction state.
    setPhase(FREE);
    // wake-up an eventually user's thread in begin
    notify();
  }

  public final synchronized void stop() {
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "NTransaction, stops");

    while (phase != FREE) {
      // Wait for the transaction subsystem to be free
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }
    // Change the transaction state.
    setPhase(FINALIZE);
    
    logFile.stop();

    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "NTransaction, stopped");
  }

  /**
   *
   */
  static final class LogFile extends ByteArrayOutputStream {
    /**
     * Log of all operations already commited but not reported on disk.
     */
    Hashtable log = null;
    /** log file */
    RandomAccessFile logFile = null; 

    int current = -1;

    /** Root directory of transaction storage */
    private File dir = null;
    /** Coherency lock file */
    static private final String LOCK = "lock";

    private File lockFile = null;

    LogFile(File dir) throws IOException {
      super(1 * Kb);
      this.dir = dir;

      lockFile = new File(dir, LOCK);
      if (! lockFile.createNewFile()) {
        logmon.log(BasicLevel.FATAL,
                   "NTransaction.init(): " +
                   "Either the server is already running, " + 
                   "either you have to remove lock file: " +
                   lockFile.getAbsolutePath());
        throw new IOException("Transaction already running.");
      }
      lockFile.deleteOnExit();

      //  Search for old log file, then apply all committed operation,
      // finally cleans it.
      log = new Hashtable(CLEANUP_THRESHOLD_OPERATION);

      
      File logFilePN = new File(dir, "log");
      if ((logFilePN.exists()) && (logFilePN.length() > 0)) {
        logFile = new RandomAccessFile(logFilePN, "r");
        try {
          int optype = logFile.read();
          while (optype == Operation.COMMIT) {
            String dirName;
            String name;

            optype = logFile.read();
 
            while ((optype == Operation.SAVE) ||
                   (optype == Operation.DELETE)) {
              //  Gets all operations of one committed transaction then
              // adds them to specified log.
              dirName = logFile.readUTF();
              if (dirName.length() == 0) dirName = null;
              name = logFile.readUTF();

              Object key = OperationKey.newKey(dirName, name);

              if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           "NTransaction.init(), OPERATION=" +
                           optype + ", " + name);

              Operation op = null;
              if (optype == Operation.SAVE) {
                byte buf[] = new byte[logFile.readInt()];
                logFile.readFully(buf);
                op = Operation.alloc(optype, dirName, name, buf);
                op = (Operation) log.put(key, op);
              } else {
                op = Operation.alloc(optype, dirName, name);
                op = (Operation) log.put(key, op);
              }
              if (op != null) op.free();

              optype = logFile.read();
            }
            if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         "NTransaction.init(), COMMIT=" + optype);
          };

          if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "NTransaction.init(), END=" + optype + ", " +
                       logFile.getFilePointer());

          if (optype != Operation.END) System.exit(-1);
        } catch (IOException exc) {
          throw exc;
        } finally {
          logFile.close();
        }

        logFile = new RandomAccessFile(logFilePN, "rwd");
        garbage();
      } else {
        logFile = new RandomAccessFile(logFilePN, "rwd");
        logFile.setLength(CLEANUP_THRESHOLD_SIZE);

        current = 1;
        // Cleans log file
        logFile.seek(0);
        logFile.write(Operation.END);
      }
    }

    static private final byte[] emptyUTFString = {0, 0};

    void writeUTF(String str)  {
      int strlen = str.length() ;

      int newcount = count + strlen +2;
      if (newcount > buf.length) {
        byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
        System.arraycopy(buf, 0, newbuf, 0, count);
        buf = newbuf;
      }

      buf[count++] = (byte) ((strlen >>> 8) & 0xFF);
      buf[count++] = (byte) ((strlen >>> 0) & 0xFF);

      str.getBytes(0, strlen, buf, count);
      count = newcount;
    }

    void writeInt(int v) throws IOException {
      int newcount = count +4;
      if (newcount > buf.length) {
        byte newbuf[] = new byte[buf.length << 1];
        System.arraycopy(buf, 0, newbuf, 0, count);
        buf = newbuf;
      }

      buf[count++] = (byte) ((v >>> 24) & 0xFF);
      buf[count++] = (byte) ((v >>> 16) & 0xFF);
      buf[count++] = (byte) ((v >>>  8) & 0xFF);
      buf[count++] = (byte) ((v >>>  0) & 0xFF);
    }

    /**
     * Reports all buffered operations in logs.
     */
    void commit(Hashtable ctxlog) throws IOException {
      Operation op = null;
      for (Enumeration e = ctxlog.elements(); e.hasMoreElements(); ) {
        op = (Operation) e.nextElement();

        // Save the log to disk
        write(op.type);
        if (op.dirName != null) {
          writeUTF(op.dirName);
        } else {
          write(emptyUTFString);
        }
        writeUTF(op.name);
        if (op.type == Operation.SAVE) {
          writeInt(op.value.length);
          write(op.value);
        }

        // Reports all committed operation in clog
        op = (Operation) log.put(OperationKey.newKey(op.dirName, op.name), op);
        if (op != null) op.free();
      }
      write(Operation.END);

      logFile.seek(current);
      logFile.write(buf);

      logFile.seek(current -1);
      logFile.write(Operation.COMMIT);

      current += (count);
      reset();

      ctxlog.clear();

      if (current > CLEANUP_THRESHOLD_SIZE) garbage();
    }

    /**
     * Reports all logged operations on disk.
     */
    private final void garbage() throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "NTransaction.garbage()");

      Operation op = null;
      for (Enumeration e = log.elements(); e.hasMoreElements(); ) {
        op = (Operation) e.nextElement();

        if (op.type == Operation.SAVE) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "NTransaction, Save (" + op.dirName + ',' + op.name + ')');
        
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
                       "NTransaction, Delete (" + op.dirName + ',' + op.name + ')');

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
                       "NTransaction, can't delete " + file.getCanonicalPath());
        }
        op.free();
      }
      //  Be careful, do not clear log before all modifications are reported
      // to disk, in order to avoid load errors.
      log.clear();

      current = 1;
      // Cleans log file
      logFile.seek(0);
      logFile.write(Operation.END);

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "NTransaction, garbaged");
    }

    /**
     * Delete the specified directory if it is empty.
     * Also recursively delete the parent directories if they are empty.
     */
    private final void deleteDir(File dir) {
      // Check the disk state. It may be false according to the transaction
      // log but it doesn't matter because directories are lazily created.
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

    void stop() {
      if (logmon.isLoggable(BasicLevel.INFO))
        logmon.log(BasicLevel.INFO, "NTransaction, stops");

      try {
        garbage();
        logFile.close();
      } catch (IOException exc) {
        logmon.log(BasicLevel.WARN, "NTransaction, can't close logfile", exc);
      }

      if (! lockFile.delete()) {
        logmon.log(BasicLevel.FATAL,
                   "NTransaction, - can't delete lockfile: " +
                   lockFile.getAbsolutePath());
      }

      if (logmon.isLoggable(BasicLevel.INFO))
        logmon.log(BasicLevel.INFO, "NTransaction, exits.");
    }
  }
}
