/*
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.common.Debug;

/**
 *  The NTransaction class implements a transactional storage.
 *  For efficiency it uses a file for its transaction journal, the final
 * storage is provided through the Repository interface on filesystem or
 * database.
 * <p>
 * Be Careful, the configuration properties don't work for the transaction component: 
 * these properties are saved in the transaction repository so they can not be used to
 * configure it.
 *
 * @see Transaction
 * @see Repository
 * @see FileRepository
 * @see DBRepository
 * @see MySqlDBRepository
 */
public final class NIOAppendTransaction extends AbstractTransaction implements NIOAppendTransactionMBean {
  
  public static final boolean MULTI_THREAD_DISK_SYNC = true;
  
  /**
   *  Global in memory log initial capacity, by default 4096.
   *  This value can be adjusted for a particular server by setting
   * <code>NTLogMemoryCapacity</code> specific property.
   * <p>
   *  This property can be set only at first launching.
   */
  static int LogMemoryCapacity = 4096;

  /**
   * Returns the initial capacity of global in memory log (by default 4096).
   *
   * @return The initial capacity of global in memory log.
   */
  public final int getLogMemoryCapacity() {
    return LogMemoryCapacity;
  }

  /**
   *  Maximum size of memory log, by default 2048Kb.
   *  This value can be adjusted (Kb) for a particular server by setting
   * <code>NTLogMemorySize</code> specific property.
   * <p>
   *  This property can be set only at first launching.
   */
  static int MaxLogMemorySize = 2048 * Kb;

  /**
   * Returns the maximum size of memory log in Kb, by default 2048Kb.
   *
   * @return The maximum size of memory log in Kb.
   */
  public final int getMaxLogMemorySize() {
    return MaxLogMemorySize / Kb;
  }

  /**
   * Sets the maximum size of memory log in Kb.
   *
   * @param size The maximum size of memory log in Kb.
   */
  public final void setMaxLogMemorySize(int size) {
    if (size > 0)
      MaxLogMemorySize = size * Kb;
  }

  /**
   * Returns the size of memory log in bytes.
   *
   * @return The size of memory log in bytes.
   */
  public final int getLogMemorySize() {
    return logFile.logMemorySize;
  }

  /**
   *  Size of disk log in Mb, by default 16Mb.
   *  This value can be adjusted (Mb) for a particular server by setting
   * <code>NTLogFileSize</code> specific property.
   * <p>
   *  This property can be set only at first launching.
   */
  static int MaxLogFileSize = 16 * Mb;

  /**
   * Returns the maximum size of disk log in Mb, by default 16Mb.
   *
   * @return The maximum size of disk log in Mb.
   */
  public final int getMaxLogFileSize() {
    return MaxLogFileSize/Mb;
  }

  /**
   * Sets the maximum size of disk log in Mb.
   *
   * @param size The maximum size of disk log in Mb.
   */
  public final void setMaxLogFileSize(int size) {
    if (size > 0) MaxLogFileSize = size *Mb;
  }

  /**
   * Returns the size of disk log in Kb.
   *
   * @return The size of disk log in Kb.
   */
  public final int getLogFileSize() {
    return (logFile.getLogFileSize() /Kb);
  }
  
  /** If true every write in the log file is synced to disk. */
  boolean syncOnWrite = false;

  /**
   * @return the syncOnWrite
   */
  public boolean isSyncOnWrite() {
    return syncOnWrite;
  }

  /**
   *  If true use a lock file to avoid multiples activation of Transaction
   * component. This value can be adjusted for a particular server by setting
   * <code>Transaction.UseLockFile</code> specific property.
   * <p>
   *  This property can be set only at first launching.
   */
  boolean useLockFile = true;

  /**
   *  Number of pooled operation, by default 1000.
   *  This value can be adjusted for a particular server by setting
   * <code>NTLogThresholdOperation</code> specific property.
   * <p>
   *  This property can be set only at first launching.
   */
  static int LogThresholdOperation = 1000;

  /**
   * Returns the pool size for <code>operation</code> objects, by default 1000.
   *
   * @return The pool size for <code>operation</code> objects.
   */
  public final int getLogThresholdOperation() {
    return LogThresholdOperation;
  }

  /**
   * Returns the number of commit operation since starting up.
   *
   * @return The number of commit operation.
   */
  public final int getCommitCount() {
    return logFile.commitCount;
  }

  /**
   * Returns the number of garbage operation since starting up.
   *
   * @return The number of garbage operation.
   */
  public final int getGarbageCount() {
    return logFile.garbageCount;
  }

  /**
   * Returns the maximum time between two garbages, 0 if disable.
   *
   * @return The maximum time between two garbages.
   */
  public final int getGarbageDelay() {
    return (int) (logFile.garbageTimeOut /1000L);
  }

  /**
   *  Sets the maximum time between two garbages, 0 to disable the
   * asynchronous garbage mechanism.
   *
   * @param timeout The maximum time between two garbages.
   */
  public final void setGarbageDelay(int timeout) {
    logFile.garbageTimeOut = timeout *1000L;
  }

  /**
   * Returns the status of the garbage thread.
   *
   * @return The status of the garbage thread.
   */
  public final boolean isGarbageRunning() {
    // Currently there is no asynchronous garbage.
    return false;
  }

  private Timer timer = null;
  private GarbageTask task = null;

  /**
   *  Sets asynchronous garbage.
   *
   * @param async 	If true activates the asynchronous garbage,
   *			deactivates otherwise.
   */
  public void garbageAsync(boolean async) {
    if (async) {
      if (task == null) {
        task = new GarbageTask();
      }
    } else {
      if (task != null) task.cancel();
      task = null;
      if (timer != null) timer.cancel();
      timer = null;
    }
  }

  private class GarbageTask extends TimerTask {
    private GarbageTask() {
      if (NIOAppendTransaction.this.timer == null)
        NIOAppendTransaction.this.timer = new Timer();
      if (logFile.garbageTimeOut > 0) {
        try {
          NIOAppendTransaction.this.timer.schedule(this, logFile.garbageTimeOut, logFile.garbageTimeOut);
        } catch (Exception exc) {
          logmon.log(BasicLevel.ERROR,
                     "NTransaction, cannot schedule garbage task ", exc);
        }
      }
    }
    
    /** Method called when the timer expires. */
    public void run() {
      if (logFile.garbageTimeOut > 0) {
        if (System.currentTimeMillis() > (logFile.lastGarbageTime + logFile.garbageTimeOut)) {
          garbage();
        }
      }
    }
  }

  /**
   * Returns the cumulated time of garbage operations since starting up.
   *
   * @return The cumulated time of garbage operations since starting up.
   */
  public long getGarbageTime() {
    return logFile.garbageTime;
  }

  /**
   * Returns the ratio of garbage operations since starting up.
   *
   * @return The ratio of garbage operations since starting up.
   */
  public int getGarbageRatio() {
    return (int) ((logFile.garbageTime *100) / (System.currentTimeMillis() - startTime));
  }

  /**
   *  The Repository classname implementation.
   *  This value can be set for a particular server by setting the
   * <code>NTRepositoryImpl</code> specific property. By default its value
   * is "fr.dyade.aaa.util.FileRepository".
   * <p>
   *  This property can be set only at first launching.
   */
  String repositoryImpl = "fr.dyade.aaa.util.FileRepository";

  /**
   * Returns the Repository classname implementation.
   *
   * @return The Repository classname implementation.
   */
  public String getRepositoryImpl() {
    return repositoryImpl;
  }

  /**
   * Returns the number of save operation to repository.
   *
   * @return The number of save operation to repository.
   */
  public int getNbSavedObjects() {
    return repository.getNbSavedObjects();
  }

  /**
   * Returns the number of delete operation on repository.
   *
   * @return The number of delete operation on repository.
   */
  public int getNbDeletedObjects() {
    return repository.getNbDeletedObjects();
  }

  /**
   * Returns the number of useless delete operation on repository.
   *
   * @return The number of useless delete operation on repository.
   */
  public int getNbBadDeletedObjects() {
    return repository.getNbBadDeletedObjects();
  }

  /**
   * Returns the number of load operation from repository.
   *
   * @return The number of load operation from repository.
   */
  public int getNbLoadedObjects() {
    return repository.getNbLoadedObjects();
  }

  LogFile logFile = null;

  Repository repository = null;

  static final boolean debug = false;

  public NIOAppendTransaction() {}

  /**
   * Tests if the Transaction component is persistent.
   *
   * @return true.
   */
  public boolean isPersistent() {
    return true;
  }

  public final void initRepository() throws IOException {
    LogMemoryCapacity = getInteger("NTLogMemoryCapacity", LogMemoryCapacity).intValue();
    MaxLogFileSize = getInteger("NTLogFileSize", MaxLogFileSize / Mb).intValue() * Mb;
    MaxLogMemorySize = getInteger("NTLogMemorySize", MaxLogMemorySize / Kb).intValue() * Kb;

    LogThresholdOperation = getInteger("NTLogThresholdOperation", LogThresholdOperation).intValue();
    Operation.initPool(LogThresholdOperation);

    try {
      repositoryImpl = System.getProperty("NTRepositoryImpl", repositoryImpl);
      repository = (Repository) Class.forName(repositoryImpl).newInstance();
      repository.init(this, dir);
    } catch (ClassNotFoundException exc) {
      logmon.log(BasicLevel.FATAL,
                 "NTransaction, cannot initializes the repository ", exc);
      throw new IOException(exc.getMessage());
    } catch (InstantiationException exc) {
      logmon.log(BasicLevel.FATAL,
                 "NTransaction, cannot initializes the repository ", exc);
      throw new IOException(exc.getMessage());
    } catch (IllegalAccessException exc) {
      logmon.log(BasicLevel.FATAL,
                 "NTransaction, cannot initializes the repository ", exc);
      throw new IOException(exc.getMessage());
    }
    
    syncOnWrite = getBoolean("NTSyncOnWrite");
    if (getBoolean("NTNoLockFile"))
      logmon.log(BasicLevel.ERROR,
                 "NTransaction, no longer use NTNoLockFile property, use Transaction.UseLockFile.");
    useLockFile = getBoolean("Transaction.UseLockFile");
    
    logFile = new LogFile(dir, repository, useLockFile, syncOnWrite);
    
    // Be careful, setGarbageDelay and garbageAsync use logFile !!
    setGarbageDelay(getInteger("NTGarbageDelay", getGarbageDelay()).intValue());
    garbageAsync(getBoolean("NTAsyncGarbage"));
    
    // JORAM_PERF_BRANCH
    logmon.log(BasicLevel.WARN, "*** Use NIOAppendTransaction ***");
  }

  /**
   * Returns the path of persistence directory.
   *
   * @return The path of persistence directory.
   */
  public String getPersistenceDir() {
    return dir.getPath();
  }

  protected final void setPhase(int newPhase) {
    phase = newPhase;
  }

  /**
   *  Returns an array of strings naming the persistent objects denoted by
   * a name that satisfy the specified prefix. Each string is an object name.
   * 
   * @param prefix	the prefix
   * @return		An array of strings naming the persistent objects
   *		 denoted by a name that satisfy the specified prefix. The
   *		 array will be empty if no names match.
   */
  public synchronized String[] getList(String prefix) {
    String[] list1 = null;
    try {
      list1 = repository.list(prefix);
    } catch (IOException exc) {
      // AF: TODO
    }
    if (list1 == null) list1 = new String[0];
    Object[] list2 = logFile.log.keySet().toArray();
    int nb = list1.length;
    for (int i=0; i<list2.length; i++) {
      if ((list2[i] instanceof String) &&
          (((String) list2[i]).startsWith(prefix))) {
        int j=0;
        for (; j<list1.length; j++) {
          if (list2[i].equals(list1[j])) break;
        }
        if (j<list1.length) {
          // The file is already in the directory list, it must be count
          // at most once.
          if (((Operation) logFile.log.get(list2[i])).type == Operation.DELETE) {
            // The file is deleted in transaction log.
            list1[j] = null;
            nb -= 1;
          }
          list2[i] = null;
        } else if ((((Operation) logFile.log.get(list2[i])).type == Operation.SAVE) ||
            (((Operation) logFile.log.get(list2[i])).type == Operation.CREATE)) {
          // The file is added in transaction log
          nb += 1;
        } else {
          list2[i] = null;
        }
      } else {
        list2[i] = null;
      }
    }
    String[] list = new String[nb];
    for (int i=list1.length-1; i>=0; i--) {
      if (list1[i] != null) list[--nb] = list1[i];
    }
    for (int i=list2.length-1; i>=0; i--) {
      if (list2[i] != null) list[--nb] = (String) list2[i];
    }
        
    return list;
  }

  /**
   *  Save an object state already serialized. The byte array in parameter
   * may be modified so we must duplicate it.
   */
  protected final void saveInLog(byte[] buf,
                                 String dirName, String name,
                                 Hashtable log,
                                 boolean copy,
                                 boolean first) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "NTransaction, saveInLog(" + dirName + '/' + name + ", " + copy + ", " + first + ")");

    Object key = OperationKey.newKey(dirName, name);
    Operation op = null;
    if (first)
      op = Operation.alloc(Operation.CREATE, dirName, name, buf);
    else
      op = Operation.alloc(Operation.SAVE, dirName, name, buf);
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
    // Searches in the log a new value for the object.
    Operation op = (Operation) log.get(key);
    if (op != null) {
      if ((op.type == Operation.SAVE) || (op.type == Operation.CREATE)) {
        return op.value;
      } else if (op.type == Operation.DELETE) {
        // The object was deleted.
        throw new FileNotFoundException();
      }
    }
    return null;
  }

  private final byte[] getFromLog(String dirName, String name) throws IOException {
    // First searches in the logs a new value for the object.
    Object key = OperationKey.newKey(dirName, name);
    byte[] buf = getFromLog(perThreadContext.get().getLog(), key);
    if (buf != null) return buf;
    
    if ((buf = getFromLog(logFile.log, key)) != null) {
      return buf;
    }

    return null;  
  }

  public byte[] loadByteArray(String dirName, String name) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "NTransaction, loadByteArray(" + dirName + '/' + name + ")");

    // First searches in the logs a new value for the object.
    try {
      byte[] buf = getFromLog(dirName, name);
      if (buf != null) return buf;

      // Gets it from disk.      
      return repository.load(dirName, name);
    } catch (FileNotFoundException exc) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "NTransaction, loadByteArray(" + dirName + '/' + name + ") not found");

      return null;
    }
  }
  
  public final void delete(String dirName, String name) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "NTransaction, delete(" + dirName + ", " + name + ")");

    Object key = OperationKey.newKey(dirName, name);

    Hashtable<Object, Operation> log = perThreadContext.get().getLog();
    Operation op = Operation.alloc(Operation.DELETE, dirName, name);
    Operation old = log.put(key, op);
    if (old != null) {
      if (old.type == Operation.CREATE) op.type = Operation.NOOP;
      old.free();
    }
  }
  
  private Object syncLock = new Object();
  
  private List<Runnable> callbacks = new ArrayList<Runnable>();
  
  private boolean synchronizedDisk;

  public final void commit(boolean release) throws IOException {
    commit(null);
  }
  
  private void validateAndRelease() throws IOException {
    for (Runnable callback : callbacks) {
      callback.run();
    }
    callbacks.clear();
    release();
  }
  
  private void syncValidateAndRelease() throws IOException {
    synchronized (syncLock) {
      logFile.sync();
      synchronizedDisk = true;
      validateAndRelease();
      syncLock.notifyAll();
    }
  }
  
  public void commit(Runnable beforeRelease) throws IOException {
    synchronized (this) {
      if (phase != RUN)
        throw new IllegalStateException("Cannot commit.");
      
      synchronizedDisk = false;

      Hashtable<Object, Operation> log = perThreadContext.get().getLog();
      if (!log.isEmpty()) {
        logFile.commit(log);
        log.clear();
      }

      setPhase(COMMIT);

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "NTransaction, committed");
      
      if (beforeRelease != null) {
        callbacks.add(beforeRelease);
      }
      
      if (! syncOnWrite) {
        validateAndRelease();
        return;
      } else if (MULTI_THREAD_DISK_SYNC) {
        if (getWaitingToBegin() == 0) {
          syncValidateAndRelease();
          return;
        } else {
          release();
        }
      } else {
        logFile.sync();
        validateAndRelease();
        return;
      }
    }
    
    synchronized (syncLock) {
      if (! synchronizedDisk) {
        try {
          syncLock.wait();
        } catch (InterruptedException e) {}
      }
    }
  }

  /**
   * Garbage the log file.
   * It waits all transactions termination, then the log file is garbaged
   * and all operations are reported to disk.
   */
  public final synchronized void garbage() {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "NTransaction, garbages");

    while (phase != FREE) {
      // Wait for the transaction subsystem to be free
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }

    setPhase(GARBAGE);
    try {
      logFile.garbage();
    } catch (IOException exc) {
      logmon.log(BasicLevel.WARN, "NTransaction, can't garbage logfile", exc);
    }
    setPhase(FREE);

    if (logmon.isLoggable(BasicLevel.INFO)) {
      logmon.log(BasicLevel.INFO, "NTransaction, garbaged: " + toString());
    }
  }

  /**
   * Stops the transaction module.
   * It waits all transactions termination, then the module is kept
   * in a FREE 'ready to use' state.
   * The log file is garbaged, all operations are reported to disk.
   */
  public synchronized void stop() {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "NTransaction, stops");

    while (phase != FREE) {
      // Wait for the transaction subsystem to be free
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }

    setPhase(FINALIZE);
    try {
      logFile.garbage();
    } catch (IOException exc) {
      logmon.log(BasicLevel.WARN, "NTransaction, can't garbage logfile", exc);
    }
    setPhase(FREE);

    if (logmon.isLoggable(BasicLevel.INFO)) {
      logmon.log(BasicLevel.INFO, "NTransaction, stopped: " + toString());
    }
  }

  /**
   * Close the transaction module.
   * It waits all transactions termination, the module will be initialized
   * anew before reusing it.
   * The log file is garbaged then closed.
   */
  public synchronized void close() {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "NTransaction, closes");

    if (phase == INIT) return;

    while (phase != FREE) {
      // Wait for the transaction subsystem to be free
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }

    setPhase(FINALIZE);
    logFile.stop();
    setPhase(INIT);

    if (logmon.isLoggable(BasicLevel.INFO)) {
      logmon.log(BasicLevel.INFO, "NTransaction, closed: " + toString());
    }
  }

  /**
   *
   */
  static final class LogFile extends ByteArrayOutputStream {
    /**
     * Log of all operations already committed but not reported on disk.
     */
    Hashtable<Object, Operation> log = null;
    /** log file */
    RandomAccessFile logFile = null; 

    /** Current file pointer in log */
    int current = -1;

    /**
     * Returns the size of disk log in bytes.
     *
     * @return The size of disk log in bytes.
     */
    int getLogFileSize() {
      return current;
    }

    /**
     * Number of commit operation since starting up.
     */
    int commitCount = 0;

    /**
     * Number of garbage operation since starting up.
     */
    int garbageCount = 0;

    /**
     * Cumulated time of garbage operations since starting up.
     */
    long garbageTime = 0l;

    /**
     * Date of last garbage.
     */
    long lastGarbageTime = 0L;

    /**
     * Maximum delay between 2 garbages.
     */
    long garbageTimeOut = 0L;

    /** Coherence lock filename */
    static private final String LockPathname = "lock";
    
    /** Coherence lock file */
    private File lockFile = null;

    private Repository repository = null;
    
    private String mode;
    
    // JORAM_PERF_BRANCH
    private long logId;
    
    private boolean syncOnWrite;

    LogFile(File dir, Repository repository,
            boolean useLockFile, boolean syncOnWrite) throws IOException {
      super(4 * Kb);
      this.repository = repository;

      if (useLockFile) {
        lockFile = new File(dir, LockPathname);
        if (! lockFile.createNewFile()) {
          logmon.log(BasicLevel.FATAL,
                     "NTransaction.init(): Either the server is already running, " + 
                     "either you have to remove lock file: " + lockFile.getAbsolutePath());
          throw new IOException("Transaction already running.");
        }
        lockFile.deleteOnExit();
      }

      /*
      if (syncOnWrite)
        mode = "rwd";
      else
      */
      
      // JORAM_PERF_BRANCH: sync should be explicit
      this.syncOnWrite = syncOnWrite;
      mode = "rw";
      
      log = new Hashtable<Object, Operation>(LogMemoryCapacity);

      //  Search for old log file, then apply all committed operation,
      // finally cleans it.
      File logFilePN = new File(dir, "log");
      if ((logFilePN.exists()) && (logFilePN.length() > 0)) {
        logFile = new RandomAccessFile(logFilePN, "r");
        
        // JORAM_PERF_BRANCH
        logId = logFile.readLong();
        
        try {
          int optype = logFile.read();
          commitLoop:
          while (optype == Operation.COMMIT) {
            String dirName;
            String name;

            optype = logFile.read();
 
            while ((optype == Operation.CREATE) ||
                   (optype == Operation.SAVE) ||
                   (optype == Operation.DELETE)) {
              //  Gets all operations of one committed transaction then
              // adds them to specified log.
              dirName = logFile.readUTF();
              if (dirName.length() == 0) dirName = null;
              name = logFile.readUTF();

              Object key = OperationKey.newKey(dirName, name);

              if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           "NTransaction.init(), OPERATION=" + optype + ", " + name);

              Operation op = null;
              if ((optype == Operation.SAVE) || (optype == Operation.CREATE)) {
                byte buf[] = new byte[logFile.readInt()];
                logFile.readFully(buf);
                
                // JORAM_PERF_BRANCH
                long commitLogId = logFile.readLong();
                if (commitLogId != logId) {
                  break commitLoop;
                }
                
                op = Operation.alloc(optype, dirName, name, buf);
                Operation old = log.put(key, op);
                if (old != null) old.free();
              } else {
                // Operation.DELETE
                
                // JORAM_PERF_BRANCH
                long commitLogId = logFile.readLong();
                if (commitLogId != logId) {
                  break commitLoop;
                }
                
                op = Operation.alloc(optype, dirName, name);
                Operation old = log.put(key, op);
                if (old != null) {
                  if (old.type == Operation.CREATE) op.type = Operation.NOOP;
                  old.free();
                }
              }
              
              optype = logFile.read();
            }
            if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         "NTransaction.init(), COMMIT=" + optype);
          }

          if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "NTransaction.init(), END=" + optype + ", " +
                       logFile.getFilePointer());

          // JORAM_PERF_BRANCH
          //if (optype != Operation.END)
          //  throw new IOException("Corrupted transaction log");
        } catch (IOException exc) {
          throw exc;
        } finally {
          logFile.close();
        }

        logFile = new RandomAccessFile(logFilePN, mode);
        garbage();
      } else {
        logFile = new RandomAccessFile(logFilePN, mode);
        logFile.setLength(MaxLogFileSize);

        current = 1;
        // Cleans log file
        logFile.seek(0);
        
        // JORAM_PERF_BRANCH
        //logFile.write(Operation.END);
        
        // JORAM_PERF_BRANCH
        logId = System.currentTimeMillis();
        logFile.writeLong(logId);
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

    void writeInt(int v) {
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
    
    // JORAM_PERF_BRANCH
    void writeLong(long l) {
      int newcount = count +8;
      if (newcount > buf.length) {
        byte newbuf[] = new byte[buf.length << 1];
        System.arraycopy(buf, 0, newbuf, 0, count);
        buf = newbuf;
      }

      buf[count++] = (byte) ((l >>> 56) & 0xFF);
      buf[count++] = (byte) ((l >>> 48) & 0xFF);
      buf[count++] = (byte) ((l >>> 40) & 0xFF);
      buf[count++] = (byte) ((l >>> 32) & 0xFF);
      buf[count++] = (byte) ((l >>> 24) & 0xFF);
      buf[count++] = (byte) ((l >>> 16) & 0xFF);
      buf[count++] = (byte) ((l >>>  8) & 0xFF);
      buf[count++] = (byte) ((l >>>  0) & 0xFF);
    }

    int logMemorySize = 0;

    /**
     * Reports all buffered operations in logs.
     */
    void commit(Hashtable<Object, Operation> ctxlog) throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "NTransaction.LogFile.commit()");

      commitCount += 1;
      
      Operation op = null;
      for (Enumeration<Operation> e = ctxlog.elements(); e.hasMoreElements(); ) {
        op = e.nextElement();
        if (op.type == Operation.NOOP) continue;

//      if (logmon.isLoggable(BasicLevel.DEBUG))
//         if (op.type == Operation.SAVE) {
//           logmon.log(BasicLevel.DEBUG, "NTransaction save " + op.name);
//         } else if (op.type == Operation.CREATE) {
//           logmon.log(BasicLevel.DEBUG, "NTransaction create " + op.name);
//         } else if (op.type == Operation.DELETE) {
//           logmon.log(BasicLevel.DEBUG, "NTransaction delete " + op.name);
//         }
//      }
        
        // JORAM_PERF_BRANCH
        write(Operation.COMMIT);

        // Save the operation to the log on disk
        write(op.type);
        if (op.dirName != null) {
          writeUTF(op.dirName);
        } else {
          write(emptyUTFString);
        }
        writeUTF(op.name);
        if ((op.type == Operation.SAVE) || (op.type == Operation.CREATE)) {
          logMemorySize += op.value.length;
          
          writeInt(op.value.length);
          write(op.value);
        }

        // Reports all committed operation in current log
        Operation old = (Operation) log.put(OperationKey.newKey(op.dirName, op.name), op);
        if (old != null) {
          if ((old.type == Operation.SAVE) || (old.type == Operation.CREATE))
            logMemorySize -= old.value.length;

          if (old.type == Operation.CREATE) {
            if (op.type == Operation.DELETE)
              op.type = Operation.NOOP;
            else if (op.type == Operation.SAVE)
              op.type = Operation.CREATE;
          }
          old.free();
        }
      }
      //JORAM_PERF_BRANCH
      //write(Operation.END);
      writeLong(logId);

      //logFile.seek(current);
      
      // JORAM_PERF_BRANCH
      //logFile.write(buf, 0, count);
      ByteBuffer buffer = ByteBuffer.wrap(buf, 0, count);
      FileChannel channel = logFile.getChannel();
      channel.write(buffer);
      
      // Do not sync here
      
      // JORAM_PERF_BRANCH
      //logFile.seek(current -1);
      //logFile.write(Operation.COMMIT);

      current += (count);
      reset();

      ctxlog.clear();

      if ((current > MaxLogFileSize) || (logMemorySize > MaxLogMemorySize) ||
          ((garbageTimeOut > 0) && (System.currentTimeMillis() > (lastGarbageTime + garbageTimeOut))))
        garbage();
    }
    
    // JORAM_PERF_BRANCH
    void sync() throws IOException {
      FileChannel channel = logFile.getChannel();
      channel.force(false);
    }

    /**
     * Reports all logged operations on disk.
     */
    private final void garbage() throws IOException {
      long start = System.currentTimeMillis();

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "NTransaction.LogFile.garbage() - begin");

      garbageCount += 1;

      Operation op = null;
      for (Enumeration<Operation> e = log.elements(); e.hasMoreElements(); ) {
        op = e.nextElement();

        if ((op.type == Operation.SAVE) || (op.type == Operation.CREATE)) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "NTransaction, LogFile.Save (" + op.dirName + '/' + op.name + ')');

          repository.save(op.dirName, op.name, op.value);
        } else if (op.type == Operation.DELETE) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "NTransaction, LogFile.Delete (" + op.dirName + '/' + op.name + ')');

          repository.delete(op.dirName, op.name);
//           if (!deleted && file.exists())
//             logmon.log(BasicLevel.ERROR,
//                        "NTransaction, can't delete " + file.getCanonicalPath());
        }
        op.free();
      }
      //  Be careful, do not clear log before all modifications are reported
      // to disk, in order to avoid load errors.
      // TODO (AF): Do the repository.commit before the log.clear ?
      log.clear();
      logMemorySize = 0;

      repository.commit();

      current = 7;
      // Cleans log file
      // logFile.seek(0);
      // JORAM_PERF_BRANCH
      //logFile.write(Operation.END);
      
      int fileSize = (int) logFile.length();
      ByteBuffer bb = ByteBuffer.allocate(fileSize);
      
      for (int i = 0; i < fileSize; i++)
      {
         bb.put((byte) 0);
      }

      bb.flip();
      FileChannel channel = logFile.getChannel();
      channel.position(0);
      channel.write(bb);
      logmon.log(BasicLevel.WARN, "*** Log clean: " + fileSize);
      channel.position(0);

      // JORAM_PERF_BRANCH
      logId = System.currentTimeMillis();
      logFile.writeLong(logId);
      
      channel.force(false);
      
      long end = System.currentTimeMillis();
      lastGarbageTime = end;
      garbageTime += end - start;

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "NTransaction.LogFile.garbage() - end: " + (end - start));
    }

    void stop() {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "NTransaction.LogFile, stops");

      try {
        garbage();
        logFile.close();
        repository.close();
      } catch (IOException exc) {
        logmon.log(BasicLevel.WARN,
                   "NTransaction.LogFile, can't close logfile", exc);
      }

      if ((lockFile != null) && (! lockFile.delete())) {
        logmon.log(BasicLevel.FATAL,
                   "NTransaction.LogFile, - can't delete lockfile: " + lockFile.getAbsolutePath());
      }

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "NTransaction.LogFile, stopped.");
    }
  }

  /**
   * Returns a string representation for this object.
   *
   * @return	A string representation of this object. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append('(').append(super.toString());
    strbuf.append(",LogMemorySize=").append(getLogMemorySize());
    strbuf.append(",LogFileSize=").append(getLogFileSize());
    strbuf.append(",CommitCount=").append(getCommitCount());
    strbuf.append(",GarbageCount=").append(getGarbageCount());
    strbuf.append(",GarbageRatio=").append(getGarbageRatio());
    strbuf.append(",NbSavedObjects=").append(getNbSavedObjects());
    strbuf.append(",NbDeletedObjects=").append(getNbDeletedObjects());
    strbuf.append(",NbBadDeletedObjects=").append(getNbBadDeletedObjects());
    strbuf.append(",NbLoadedObjects=").append(getNbLoadedObjects());
    strbuf.append(')');
    
    return strbuf.toString();
  }

  public static void main(String[] args) throws Exception {
    if ("garbage".equals(args[0])) {
      NIOAppendTransaction transaction = new NIOAppendTransaction();
      transaction.init(args[1]);
      transaction.stop();
    } else if ("list".equals(args[0])) {
    } else {
      System.err.println("unknown command: " + args[0]);
    }
  }
}
