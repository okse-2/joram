/*
 * Copyright (C) 2009 - 2012 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.ext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.AbstractTransaction;
import fr.dyade.aaa.util.Operation;
import fr.dyade.aaa.util.OperationKey;
import fr.dyade.aaa.util.Repository;
import fr.dyade.aaa.util.StartWithFilter;
import fr.dyade.aaa.util.Transaction;

/**
 *  The NGTransaction class implements a transactional storage.
 *  For efficiency it uses multiples files for its transaction journal, the final
 * storage is provided through the Repository interface on filesystem or database.
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
public final class NGAsyncTransaction extends AbstractTransaction implements NGAsyncTransactionMBean {
  
  /**
   *  Global in memory log initial capacity, by default 4096.
   *  This value can be adjusted for a particular server by setting
   * <code>Transaction.LogMemoryCapacity</code> specific property.
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
   * Returns the number of operation in the memory log.
   *
   * @return The number of operation in the memory log.
   */
  public int getLogMemorySize() {
    return logManager.mainLog.size();
  }
  
  /**
   *  Maximum size of disk log in Mb, by default 16Mb.
   *  This value can be adjusted (Mb) for a particular server by setting
   * <code>Transaction.MaxLogFileSize</code> specific property.
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
   * Returns the current size of disk log in Kb.
   *
   * @return The size of disk log in Kb.
   */
  public final int getLogFileSize() {
    return (logManager.getLogFileSize() /Kb);
  }
  
  /**
   *  Maximum number of disk log used by the Transaction component, by
   * default 4.
   *  This value can be adjusted for a particular server by setting
   * <code>Transaction.NbLogFile</code> specific property.
   * <p>
   *  This property can be set only at first launching.
   */
  static int nbLogFile = 4;
  
  /**
   * Returns the number of rolled log files.
   * 
   * @return The number of rolled log files.
   */
  public final int getNbLogFiles() {
    return nbLogFile;
  }

  /**
   *  Minimum number of 'live' objects in a disk log before a garbage, by
   * default 64.
   *  This value can be adjusted for a particular server by setting
   * <code>Transaction.minObjInLog</code> specific property.
   * <p>
   *  This property can be set only at first launching.
   */
  static int minObjInLog = 64;
  
  /**
   *  If true every write in the log file is synced to disk, by default
   * false. This value can be adjusted for a particular server by setting
   * <code>Transaction.SyncOnWrite</code> specific property.
   * <p>
   *  This property can be set only at first launching.
   */
  boolean syncOnWrite = false;
  
  /**
   * @return the syncOnWrite value.
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
   * <code>Transaction.LogThresholdOperation</code> specific property.
   * <p>
   *  This property can be set only at first launching.
   */
  int LogThresholdOperation = 1000;

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
    return logManager.commitCount;
  }

  /**
   * Returns the number of garbage operation since starting up.
   *
   * @return The number of garbage operation.
   */
  public final int getGarbageCount() {
    return logManager.garbageCount;
  }

  /**
   * Returns the cumulated time of garbage operations since starting up.
   *
   * @return The cumulated time of garbage operations since starting up.
   */
  public long getGarbageTime() {
    return logManager.garbageTime;
  }

  /**
   * Returns the number of load operation from a log file since last start.
   * 
   * @return The number of load operation from a log file.
   */
  public int getNbLoadedFromLog() {
    return logManager.loadFromLog;
  }
  
  /**
   * Returns the ratio of garbage operations since starting up.
   *
   * @return The ratio of garbage operations since starting up.
   */
  public int getGarbageRatio() {
    return (int) ((logManager.garbageTime *100) / (System.currentTimeMillis() - startTime));
  }

  public void resetGarbageRatio() {
    logManager.garbageTime = 0L;
    startTime = System.currentTimeMillis();
  }
  
  /**
   *  The Repository classname implementation.
   *  This value can be set for a particular server by setting the
   * <code>Transaction.RepositoryImpl</code> specific property. By default its value
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
  
  LogManager logManager = null;

  Repository repository = null;

  public NGAsyncTransaction() {}

  private ScheduledExecutorService executorService;
  
  private long syncTimeout;
  
  private boolean asyncLogWriter;
  
  public final void initRepository() throws IOException {
    LogMemoryCapacity = getInteger("Transaction.LogMemoryCapacity", LogMemoryCapacity).intValue();
    MaxLogFileSize = getInteger("Transaction.MaxLogFileSize", MaxLogFileSize / Mb).intValue() * Mb;
    nbLogFile = getInteger("Transaction.NbLogFile", nbLogFile).intValue();
    minObjInLog = getInteger("Transaction.minObjInLog", minObjInLog).intValue();
    syncTimeout = getInteger("Transaction.syncTimeout", 3000000).intValue();
    asyncLogWriter = getBoolean("Transaction.asyncLogWriter");
    
    LogThresholdOperation = getInteger("Transaction.LogThresholdOperation", LogThresholdOperation).intValue();
    Operation.initPool(LogThresholdOperation);

    if (asyncLogWriter) {
      executorService = Executors.newSingleThreadScheduledExecutor(new LogFileWriterThreadFactory());
      executorService.scheduleWithFixedDelay(logFileWriter, 5, 5, TimeUnit.MILLISECONDS);
    }
    
    try {
      repositoryImpl = getProperty("Transaction.RepositoryImpl", repositoryImpl);
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
    
    syncOnWrite = getBoolean("Transaction.SyncOnWrite");
    useLockFile = getBoolean("Transaction.UseLockFile");

    logManager = new LogManager(dir, repository, useLockFile, syncOnWrite);
  }

  /**
   * Tests if the Transaction component is persistent.
   *
   * @return true.
   */
  public boolean isPersistent() {
    return true;
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
    return logManager.getList(prefix);
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
                 "NGTransaction, saveInLog(" + dirName + '/' + name + ", " + copy + ", " + first + ")");

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

  private final synchronized byte[] getFromLog(String dirName, String name) throws IOException {
    // First searches in the current transaction log a new value for the object.
    Object key = OperationKey.newKey(dirName, name);
    byte[] buf = getFromLog(perThreadContext.get().getLog(), key);
    if (buf != null) return buf;
    
    // Then search in the log files and repository.
    return logManager.load(dirName, name);  
  }


  public byte[] loadByteArray(String dirName, String name) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "NTransaction, loadByteArray(" + dirName + '/' + name + ")");

    // First searches in the logs a new value for the object.
    try {
      return getFromLog(dirName, name);
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

  public final synchronized void commit(boolean release) throws IOException {
    commit(null);
  }
  
  private LogFileWriter logFileWriter = new LogFileWriter();
  
  public void commit(Runnable callback) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "NGAsyncTransaction.commit()");
    try {
      if (phase != RUN)
        throw new IllegalStateException("Cannot commit.");

      Hashtable<Object, Operation> log = perThreadContext.get().getLog();
      if (! log.isEmpty()) {
        if (asyncLogWriter) {
          logManager.commit(log, callback, logFileWriter);
        } else {
          logManager.commit(log, callback, null);
        }
        log.clear();
      } else {
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN, "Should not commit an empty log", new Exception());
        if (callback != null) {
          callback.run();
        }
      }

      setPhase(COMMIT);
      
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "NGAsyncTransaction, to be committed");
    } finally {
      release();
    }
  }
  
  static class LogFileContext {
    LogFile logFile;

    public LogFileContext(LogFile logFile) {
      super();
      this.logFile = logFile;
    }
    
  }
  
  static class LogFileWriteContext extends LogFileContext {
    ByteBuffer byteBuffer;
    Runnable callback;
    
    public LogFileWriteContext(LogFile logFile, ByteBuffer byteBuffer,
        Runnable callback) {
      super(logFile);
      this.byteBuffer = byteBuffer;
      this.callback = callback;
    }

  }
  
  static class LogGarbageContext extends LogFileContext {
    List<Operation> operations;

    public LogGarbageContext(LogFile logFile, List<Operation> operations) {
      super(logFile);
      this.operations = operations;
    }
    
  }
  
  class LogFileWriterThreadFactory implements ThreadFactory {
    
    private int counter;

    public Thread newThread(Runnable runnable) {
      Thread t = new LogFileWriterThread(runnable, counter++);
      return t;
    }
    
  }
 
  class LogFileWriterThread extends Thread {
   
    LogFileWriterThread(Runnable runnable, int id) {
      super(AgentServer.getThreadGroup(), runnable, "LogFileWriter#" + id);
    }
   
  }
  
  /**
   * Number of 'write' between two synchronizations with the disk
   */
  private int writeSyncRatio;

  public int getWriteSyncRatio() {
    return writeSyncRatio;
  }

  class LogFileWriter implements Runnable {
    
    private ConcurrentLinkedQueue<LogFileContext> contexts = 
        new ConcurrentLinkedQueue<LogFileContext>();

    private boolean running;
    
    private LogFile currentLogFile;
    
    private int syncCount;
    
    private int writeCount;
    
    private List<LogFileWriteContext> toValidate = 
        new ArrayList<LogFileWriteContext>();
    
    private long lastSyncDate;
    
    private boolean pendingSync;
    
    private void syncCurrentLog() throws IOException {
      //logmon.log(BasicLevel.DEBUG, "*** sync");
      FileChannel channel = currentLogFile.getChannel();
      if (syncOnWrite) {
        channel.force(false);
      }
      validate();
      pendingSync = false;
      syncCount++;
      writeSyncRatio = writeCount/syncCount;
      lastSyncDate = System.nanoTime();
    }
    
    private void validate() {
      for (LogFileWriteContext ctxToValidate : toValidate) {
        if (ctxToValidate.callback != null) {
          ctxToValidate.callback.run();
        }
      }
      toValidate.clear();
    }
    
    public void run() {
      while (true) {
        LogFileContext ctx = contexts.poll();
        if (ctx == null) {
          if (pendingSync) {
            long now = System.nanoTime();
            if (now - lastSyncDate > syncTimeout) {
              try {
                syncCurrentLog();
              } catch (IOException e) {
                logmon.log(BasicLevel.ERROR, "", e);
              }
            } else {
              //logmon.log(BasicLevel.WARN, "*** no sync");
            }
          }
          synchronized (contexts) {
            ctx = contexts.poll();
            if (ctx == null) {
              running = false;
              //logmon.log(BasicLevel.WARN, "*** exit");
              return;
            }
          }
        }
        
        if (ctx instanceof LogFileWriteContext) {
          if (pendingSync &&
              currentLogFile != ctx.logFile) {
            // The log file has been changed
            // Need to sync the current log
            try {
              syncCurrentLog();
            } catch (IOException e) {
              logmon.log(BasicLevel.ERROR, "", e);
            }
          }
          
          currentLogFile = ctx.logFile;
          
          LogFileWriteContext writeCtx = (LogFileWriteContext) ctx;
          try {
            ByteBuffer buffer = writeCtx.byteBuffer;
            FileChannel channel = currentLogFile.getChannel();
            //logmon.log(BasicLevel.DEBUG, "*** write");
            channel.write(buffer);
            writeCount++;
            pendingSync = true;
            toValidate.add(writeCtx);
          } catch (IOException e) {
            logmon.log(BasicLevel.ERROR, "", e);
          }
        } else {
          try {
            LogGarbageContext garbageCtx = (LogGarbageContext) ctx;
            LogFile garbagedLog = ctx.logFile;
            //logmon.log(BasicLevel.WARN, "*** garbage log: " + garbagedLog);
                
            if (currentLogFile == garbagedLog) {
              // Need to sync the current log
              try {
                syncCurrentLog();
              } catch (IOException e) {
                logmon.log(BasicLevel.ERROR, "", e);
              }
            }
            
            logManager.garbageOperations(garbagedLog, garbageCtx.operations);
          } catch (IOException e) {
            logmon.log(BasicLevel.ERROR, "", e);
          }
        }
      }
    }
    
    public void offer(LogFileContext ctx) {
      synchronized (contexts) {
        //logmon.log(BasicLevel.WARN, "*** offer");
        contexts.offer(ctx);
        if (! running) {
          running = true;
          executorService.execute(this);
        }
      }
    }
    
  }

  /**
   * Stops the transaction module.
   * It waits all transactions termination, then the module is kept
   * in a FREE 'ready to use' state.
   * The log file is garbaged, all operations are reported to disk.
   */
  public synchronized void stop() {
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "NTransaction, stops");

    while (phase != FREE) {
      // Wait for the transaction subsystem to be free
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }

    setPhase(FINALIZE);
//    try {
//      logManager.garbage();
//    } catch (IOException exc) {
//      logmon.log(BasicLevel.WARN, "NTransaction, can't garbage log files", exc);
//    }
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
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "NTransaction, closes");

    if (phase == INIT) return;

    while (phase != FREE) {
      // Wait for the transaction subsystem to be free
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }

    setPhase(FINALIZE);
    logManager.stop();
    setPhase(INIT);

    if (logmon.isLoggable(BasicLevel.INFO)) {
      logmon.log(BasicLevel.INFO, "NTransaction, closed: " + toString());
    }
  }
  

  static class OpenByteArrayOutputStream extends ByteArrayOutputStream {
    
    OpenByteArrayOutputStream(int capacity) {
      super(capacity);
    }
    
    byte[] getBuf() {
      return buf;
    }
    
    void setBuf(byte[] buf) {
      this.buf = buf;
    }
    
    void setCount(int count) {
      this.count = count;
    }
  }
  

  /**
   *  This class manages the memory log of operations and the multiples
   * log files.
   */
  // JORAM_PERF_BRANCH: can't reuse the ByteArrayOutputStream because of asynchronous writes
  static final class LogManager {
    /**
     * Log of all operations already committed but not reported on disk.
     */
    Hashtable<Object, Operation> mainLog = null;
    
    int logidx;
    
    /** log file */
    LogFile[] logFile = null;

    /** Current file pointer in log */
    // JORAM_PERF_BRANCH
    //int current = -1;
    int current = 0;
    
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
     * Number of load from a log file.
     */
    int loadFromLog = 0;
    
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
    long lastGarbageDate = 0L;
    
    /** Coherence lock filename */
    static private final String LockPathname = "lock";
    
    /** Coherence lock file */
    private File lockFile = null;

    private Repository repository = null;

    File dir;
    
    private String mode;
    
    // JORAM_PERF_BRANCH
    private boolean syncOnWrite;
    
    LogManager(File dir, Repository repository,
               boolean useLockFile, boolean syncOnWrite) throws IOException {
      this.repository = repository;
      
      if (useLockFile) {
        lockFile = new File(dir, LockPathname);
        if (! lockFile.createNewFile()) {
          logmon.log(BasicLevel.FATAL,
                     "NTransaction.init(): Either the server is already running, " + 
                     "either you have to remove lock file " + lockFile.getAbsolutePath());
          throw new IOException("Transaction already running.");
        }
        lockFile.deleteOnExit();
      }
      
      // JORAM_PERF_BRANCH
      /*
      if (syncOnWrite)
        mode = "rwd";
      else
      */
      mode = "rw";
      
      // JORAM_PERF_BRANCH: sync should be explicit
      this.syncOnWrite = syncOnWrite;
      mode = "rw";

      mainLog = new Hashtable(LogMemoryCapacity);
      
      long start = System.currentTimeMillis();
      
      logidx = -1;
      logFile = new LogFile[nbLogFile];
      
      this.dir = dir ;
      
      String[] list = dir.list(new StartWithFilter("log#"));
      if (list == null) {
        throw new IOException("NGTransaction error opening " + dir.getAbsolutePath());
      } else if (list.length == 0) {
        logidx = 0;
      } else {
        // Recovery of log files..
        // Be careful, sort the log according to their index.
        int idx[] = new int[list.length];
        for (int i=0; i<list.length; i++) {
          idx[i] = Integer.parseInt(list[i].substring(4));
        }
        Arrays.sort(idx);
        for (int i=0; i<idx.length; i++) {
          //logmon.log(BasicLevel.WARN, "NGTransaction.LogManager, rebuilds index: log#" + idx[i]);
          
          // Fix the log index to the lower index, it is needed if all log files
          // are garbaged.
          if (logidx == -1) logidx = idx[i];
          try {
            LogFile logf = new LogFile(dir, idx[i], mode);
            
            // JORAM_PERF_BRANCH
            logf.logId = logf.readLong();
            //logmon.log(BasicLevel.WARN, "logId=" + logf.logId);
            
            /*
            int optype = logf.read();
            if (optype == Operation.END) {
              // The log is empty
              logf.close();
              continue;
            }
            */
            
            if (logf.logId == 0) {
              // The log is empty (already garbaged)
              logf.close();
              continue;
            }
            
            // The index of current log is the bigger index of log with 'live' operations. 
            logidx = idx[i];
            logFile[logidx%nbLogFile] = logf;
            // current is fixed after the log reading
            
            recordLoop:
            while (true) {
              int recordType = logf.read();
              //logmon.log(BasicLevel.WARN, "commit/end=" + recordType);
              if (recordType != Operation.COMMIT) break recordLoop;
              
              String dirName;
              String name;
              operationLoop:
              while (true) {
                int optype = logFile[logidx%nbLogFile].read();
                if ((optype != Operation.CREATE) &&
                    (optype != Operation.SAVE) &&
                    (optype != Operation.DELETE)) {
                  if (optype == Operation.END) {
                    break operationLoop;
                  } else {
                    break recordLoop;
                  }
                }
                
                //logmon.log(BasicLevel.WARN, "optype=" + optype);
                
                int ptr = (int) logFile[logidx%nbLogFile].getFilePointer() -1;
                logFile[logidx%nbLogFile].logCounter += 1;
                //  Gets all operations of one committed transaction then
                // adds them to specified log.
                dirName = logFile[logidx%nbLogFile].readUTF();
                //logmon.log(BasicLevel.WARN, "dirName=" + dirName);
                if (dirName.length() == 0) dirName = null;
                name = logFile[logidx%nbLogFile].readUTF();
                //logmon.log(BasicLevel.WARN, "name=" + name);

                Object key = OperationKey.newKey(dirName, name);

                byte[] opValue;
                if ((optype == Operation.SAVE) || (optype == Operation.CREATE)) {
                  // TODO (AF): Fix a potential bug if there is a crash during a garbage.
                  // A newly created object can be saved in repository then not deleted.
                  // May be we can test if the corresponding file exists.
                  
                  // JORAM_PERF_BRANCH: not very useful (commented for testing)
                  // optype = Operation.SAVE;
                  
                  // JORAM_PERF_BRANCH: need to avoid to 'getFromLog' in the current log
                  // Simple solution: reload everything
                  // logFile[logidx%nbLogFile].skipBytes(logFile[logidx%nbLogFile].readInt());
                  opValue = new byte[logFile[logidx%nbLogFile].readInt()];
                  logFile[logidx%nbLogFile].readFully(opValue);
                } else {
                  opValue = null;
                }

                if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
                  logmon.log(BasicLevel.DEBUG,
                             "NGTransaction.LogManager, OPERATION=" + optype + ", " + name);
//                           "NGTransaction.LogManager, OPERATION=" + optype + ", " + name + " buf=" + Arrays.toString(buf));
                
                // JORAM_PERF_BRANCH
                long commitLogId = logFile[logidx%nbLogFile].readLong();
                //logmon.log(BasicLevel.WARN, "commitLogId=" + commitLogId);
                if (commitLogId != logf.logId) {
                  //logmon.log(BasicLevel.WARN, "*** STOP ***");
                  break recordLoop;
                }
                
                Operation old = mainLog.get(key);
                //logmon.log(BasicLevel.WARN, "old=" + old);
                if (old != null) {
                  logFile[old.logidx%nbLogFile].logCounter -= 1;

                  // There is 6 different cases:
                  //
                  //   new |
                  // old   |  C  |  S  |  D
                  // ------+-----+-----+-----+
                  //   C   |  C  |  C  | NOP
                  // ------+-----+-----+-----+
                  //   S   |  S  |  S  |  D
                  // ------+-----+-----+-----+
                  //   D   |  S  |  S  |  D
                  //

                  if ((old.type == Operation.CREATE) || (old.type == Operation.SAVE)) {
                    if ((optype == Operation.CREATE) || (optype == Operation.SAVE)) {
                      // The resulting operation is still the same, just change the logidx
                      // and logptr informations.
                      //logmon.log(BasicLevel.WARN, "keep: " + old.type);
                      old.logidx = logidx;
                      old.logptr = ptr;
                    } else {
                      // The operation is a delete
                      if (old.type == Operation.CREATE) {
                        // There is no need to memorize the deletion, the object will be never
                        // created on disk.
                        //logmon.log(BasicLevel.WARN, "NOOP");
                        old.type = Operation.NOOP;
                        mainLog.remove(key);
                        old.free();
                        logFile[logidx%nbLogFile].logCounter -= 1;
                      } else {
                        //logmon.log(BasicLevel.WARN, "DELETE");
                        // The operation is a save, overload it.
                        old.type = Operation.DELETE;
                        old.logidx = logidx;
                        old.logptr = ptr;
                      }
                    }
                  } else if (old.type == Operation.DELETE) {
                    if ((optype == Operation.CREATE) || (optype == Operation.SAVE)) {
                      // The resulting operation is a save 
                      //logmon.log(BasicLevel.WARN, "SAVE");
                      old.type = Operation.SAVE;
                    }
                    old.logidx = logidx;
                    old.logptr = ptr;
                  } 
                } else {
                  //logmon.log(BasicLevel.WARN, "PUT");
                  Operation op = Operation.alloc(optype, dirName, name);
                  op.logidx = logidx;
                  op.logptr = ptr;
                  op.value = opValue;
                  mainLog.put(key, op);
                }
              }
            }
            
            //logmon.log(BasicLevel.WARN, "mainLog=" + mainLog);

            current = (int) logFile[logidx%nbLogFile].getFilePointer();
            if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, "NGTransaction.LogManager, END#" + logidx);

            // JORAM_PERF_BRANCH
            //if (optype != Operation.END)
            //throw new IOException("Corrupted transaction log#" + logidx);
          } catch (IOException exc) {
            throw exc;
          }
        }
        
        logmon.log(BasicLevel.DEBUG,
                   "NGTransaction.LogManager, log=" + Arrays.toString(mainLog.values().toArray()));
      }
      
      if (logFile[logidx%nbLogFile] == null) {
        // Creates a log file
        logFile[logidx%nbLogFile] = new LogFile(dir, logidx, mode);
        logFile[logidx%nbLogFile].setLength(MaxLogFileSize);

        // Initializes the log file
        
         // JORAM_PERF_BRANCH
        // logFile[logidx%nbLogFile].seek(0);
        //logFile[logidx%nbLogFile].write(Operation.END);
        
        // JORAM_PERF_BRANCH
        fillAndReset(logFile[logidx%nbLogFile]);
        current = 0;
      }
      
      logmon.log(BasicLevel.INFO,
                 "NGTransaction.LogManager, ends: " + (System.currentTimeMillis() - start));
    }
    
    /**
     * Reports all buffered operations in logs.
     */
    void commit(Hashtable<Object, Operation> ctxlog, Runnable callback, LogFileWriter logFileWriter) throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "NTransaction.LogFile.commit()");
      
      // Don't need to wait that the LogFileWriter has finished to write in the old logs
      // because the garbage is also done by the LogFileWriter
      /*
      for (int i=0; i<nbLogFile; i++) {
        if (logFile[i] == null) continue;
        if (i == (logidx%nbLogFile)) {
          // Don't garbage the current log
          continue;
        }
        
        if ((logFile[i].logCounter == 0) ||
            (logFile[i].logCounter < minObjInLog)) {
          // The related log file is no longer useful, cleans it in order to speed up the
          // restart after a crash.
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "NTransaction log#" + logFile[i].logidx + " is no longer needed, cleans it.");
          garbage(logFile[i], logFileWriter);
        }
      }
      */

      commitCount += 1;
      
      Set<Entry<Object, Operation>> entries = ctxlog.entrySet();
      
      // Compute the size
      int sizeToAllocate = 0;
      
      // JORAM_PERF_BRANCH
      // Operation.COMMIT
      sizeToAllocate += 1;
      
      Iterator<Entry<Object, Operation>> iterator = entries.iterator();
      while (iterator.hasNext()) {
        Entry<Object, Operation> entry = iterator.next();
        Object key = entry.getKey();
        Operation op = entry.getValue();

        if (op.type == Operation.NOOP)
          continue;

        // op.type
        sizeToAllocate += 1;

        if (op.dirName != null) {
          sizeToAllocate += op.dirName.length();
        }

        sizeToAllocate += op.name.length();

        if ((op.type == Operation.SAVE) || (op.type == Operation.CREATE)) {
          sizeToAllocate += 4;
          sizeToAllocate += op.value.length;
        }
        
        // Log id
        sizeToAllocate += 8;
      }
      
      // Final tag (END)
      sizeToAllocate += 1;
      
      boolean garbageRequired = current + sizeToAllocate > MaxLogFileSize;
      
      if (garbageRequired) {
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          for (int i = 0; i < nbLogFile; i++)
            if (logFile[i] != null)
              logmon.log(BasicLevel.DEBUG, "logCounter[" + logFile[i].logidx
                  + "]=" + logFile[i].logCounter);
          logmon.log(BasicLevel.DEBUG, "log -> " + mainLog.size());
        }

        logidx += 1;
        long logFileId;
        if (logFile[logidx % nbLogFile] != null) {
          logFileId = logFile[logidx % nbLogFile].logId;
          // The log file is an older one, garbage it before using it again.
          garbage(logFile[logidx % nbLogFile], logFileWriter);
        } else {
          logFileId = 0;
        }

        // Creates and initializes a new log file
        // TODO: this new log should have already been created and filled by the LogWriter
        // TODO: may be we should check that the file exists before and wait if not (this should not happen in normal cases)
        logFile[logidx % nbLogFile] = new LogFile(dir, logidx, mode);
        logFile[logidx % nbLogFile].logId = logFileId;
        
        // Not useful as the logs are filled
        //logFile[logidx % nbLogFile].setLength(MaxLogFileSize);

        // Cleans log file (needed only for new log file, already done in
        // garbage).

        // JORAM_PERF_BRANCH
        // logFile[logidx%nbLogFile].seek(0);
        // logFile[logidx%nbLogFile].write(Operation.END);
        // current = 1;
        
        // JORAM_PERF_BRANCH: already done either at creation of the LogManager
        // or during the garbage (concurrent)
        if (logidx < nbLogFile) {
          // These files are not recycled
          fillAndReset(logFile[logidx % nbLogFile]);
        }
        current = 0;
        
        // Need to add the log id
        sizeToAllocate += 8;
      }
      
      // JORAM_PERF_BRANCH: can't reuse the same ByteArrayOutputStream because
      // of asynchronous writer
      OpenByteArrayOutputStream baos = new OpenByteArrayOutputStream(
          sizeToAllocate);
      
      if (garbageRequired) {
        long newLogId = System.currentTimeMillis();
        logFile[logidx % nbLogFile].logId = newLogId;
        writeLong(newLogId, baos);
      }
      
      // JORAM_PERF_BRANCH
      baos.write(Operation.COMMIT);

      iterator = entries.iterator();
      while (iterator.hasNext()) {
        Entry<Object, Operation> entry = iterator.next();
        Object key = entry.getKey();
        Operation op = entry.getValue();

        if (op.type == Operation.NOOP)
          continue;

        // if (logmon.isLoggable(BasicLevel.DEBUG)) {
        // if (op.type == Operation.SAVE) {
        // logmon.log(BasicLevel.DEBUG, "NTransaction save " + op.name);
        // } else if (op.type == Operation.CREATE) {
        // logmon.log(BasicLevel.DEBUG, "NTransaction create " + op.name);
        // } else if (op.type == Operation.DELETE) {
        // logmon.log(BasicLevel.DEBUG, "NTransaction delete " + op.name);
        // } else {
        // logmon.log(BasicLevel.DEBUG, "NTransaction unknown(" + op.type + ") "
        // + op.name);
        // }
        // }

        op.logidx = logidx;
        op.logptr = current + baos.size();

        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, "commit#" + op.logidx + ' ' + op.dirName
              + '/' + op.name + ": " + op.logptr);

        // Save the operation to the log on disk
        baos.write(op.type);
        if (op.dirName != null) {
          writeUTF(op.dirName, baos);
        } else {
          baos.write(emptyUTFString);
        }
        writeUTF(op.name, baos);
        if ((op.type == Operation.SAVE) || (op.type == Operation.CREATE)) {
          writeInt(op.value.length, baos);
          baos.write(op.value);
        }
        // TODO: Use SoftReference ?
        op.value = null;
        
        // JORAM_PERF_BRANCH
        writeLong(logFile[logidx%nbLogFile].logId, baos);

        // Reports all committed operation in current log
        Operation old = mainLog.put(key, op);
        logFile[logidx % nbLogFile].logCounter += 1;

        if (old != null) {
          logFile[old.logidx % nbLogFile].logCounter -= 1;
          
          // There are 6 different cases:
          //
          //   new |
          // old   |  C  |  S  |  D
          // ------+-----+-----+-----+
          //   C   |  C  |  C  | NOP
          // ------+-----+-----+-----+
          //   S   |  S  |  S  |  D
          // ------+-----+-----+-----+
          //   D   |  S  |  S  |  D
          //

          if (old.type == Operation.CREATE) {
            if (op.type == Operation.SAVE) {
              // The object has never been created on disk, the resulting operation
              // is still a creation.
              op.type = Operation.CREATE;
            } else if (op.type == Operation.DELETE) {
              // There is no more need to memorize the deletion the object will be
              // never created on disk.
              op.type = Operation.NOOP;
              mainLog.remove(key);
              op.free();
              logFile[logidx % nbLogFile].logCounter -= 1;
            }
          }
          old.free();
        }
      }

      //JORAM_PERF_BRANCH
      baos.write(Operation.END);

      // JORAM_PERF_BRANCH
      //logFile[logidx%nbLogFile].seek(current);
      //logFile[logidx%nbLogFile].write(buf, 0, count);

      ByteBuffer buffer = ByteBuffer.wrap(baos.getBuf(), 0, baos.size());
      
      current += baos.size();
      ctxlog.clear();

      if (logFileWriter != null) {
        LogFileWriteContext writeContex = new LogFileWriteContext(
          logFile[logidx % nbLogFile], buffer, callback);
        logFileWriter.offer(writeContex);
      } else {
        FileChannel channel = logFile[logidx % nbLogFile].getChannel();
        channel.write(buffer);
        if (syncOnWrite) {
          channel.force(false);
        }
        if (callback != null) {
          callback.run();
        }
      }
      
      //logmon.log(BasicLevel.WARN, "mainLog: " + mainLog);
    }
    
    public byte[] getFromLog(String dirName, String name) throws IOException {
      // First searches in the logs a new value for the object.
      Operation op = mainLog.get(OperationKey.newKey(dirName, name));
      if (op != null) {
        if ((op.type == Operation.SAVE) || (op.type == Operation.CREATE)) {
          // JORAM_PERF_BRANCH
          if (op.value == null) {
            // reads the value from the log file
            return getFromLog(logFile[op.logidx%nbLogFile], op);
          } else {
            return op.value;
          }
        } else if (op.type == Operation.DELETE) {
          // The object was deleted.
          throw new FileNotFoundException();
        }
      }
      
      return null;
    }

    // Can be called by the LogWriter for operations that have been removed
    // from the main log.
    public byte[] getFromLog(LogFile logf, Operation op) throws IOException {
      loadFromLog += 1;
      
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "getFromLog#" + op.logidx + ' ' + op.dirName + '/' + op.name + ": " + op.logptr);
      
      logf.seek(op.logptr);
      int optype = logf.read();    

      String dirName = logf.readUTF();
      if (dirName.length() == 0) dirName = null;
      String name = logf.readUTF();

      byte buf[] = new byte[logf.readInt()];
      logf.readFully(buf);

      return buf;
    }
    
    public byte[] load(String dirName, String name) throws IOException {
      byte[] buf = getFromLog(dirName, name);
      if (buf == null) {
        // Gets it from disk.
        buf = repository.load(dirName, name);
      }
      return buf;
    }
    
    public String[] getList(String prefix) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "getList(" + prefix + ")");
      
      String[] list1 = null;
      try {
        list1 = repository.list(prefix);
      } catch (IOException exc) {
        // AF: TODO
      }
      if (list1 == null) list1 = new String[0];
      Object[] list2 = mainLog.keySet().toArray();
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
            if ((mainLog.get(list2[i])).type == Operation.DELETE) {
              // The file is deleted in transaction log.
              list1[j] = null;
              nb -= 1;
            }
            list2[i] = null;
          } else if ((mainLog.get(list2[i]).type == Operation.SAVE) ||
              (mainLog.get(list2[i]).type == Operation.CREATE)) {
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

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "getList() -> " + Arrays.toString(list));

      return list;
    }

//    /**
//     * Reports all logged operations on disk.
//     */
//    private final void garbage() throws IOException {
//      if (logmon.isLoggable(BasicLevel.DEBUG)) {
//        logmon.log(BasicLevel.DEBUG, "log -> " + log.size());
//        for (int i=0; i<logFile.length; i++) {
//          if (logFile[i] != null)
//            logmon.log(BasicLevel.FATAL, "logCounter[" + logFile[i].logidx + "]=" + logFile[i].logCounter);
//        }
//
//        for (Enumeration<Operation> e = log.elements(); e.hasMoreElements();) {
//          Operation op = e.nextElement();
//          logmon.log(BasicLevel.DEBUG, op);
//        }
//      }
//
//      for (int i=0; i<nbLogFile; i++)
//        garbage(logFile[i]);
//    }
    
    public String logCounters() {
      StringBuffer strbuf = new StringBuffer();
      for (int i=0; i<logFile.length; i++) {
        if (logFile[i] == null) continue;
        strbuf.append("log#").append(logFile[i].logidx).append(" -> ").append(logFile[i].logCounter).append('\n');
      }
      return strbuf.toString();
    }

    public String logContent(int idx) {
      LogFile logf = logFile[idx%nbLogFile];
      if (logf == null) return null;

      StringBuffer strbuf = new StringBuffer();

      strbuf.append("counter=").append(logf.logCounter).append('\n');

      Iterator<Operation> iterator = mainLog.values().iterator();

      try {
        while (true) {
          Operation op = iterator.next();

          if (op.logidx != logf.logidx) continue;

          if (op.type == Operation.SAVE) {
            strbuf.append("SAVE ");
          } else if (op.type == Operation.CREATE) {
            strbuf.append("CREATE ");
          } else if (op.type == Operation.DELETE) {
            strbuf.append("DELETE ");
          } else {
            strbuf.append("OP(").append(op.type).append(") ");
          }
          strbuf.append(op.dirName).append('/').append(op.name).append('\n');
        }
      } catch (NoSuchElementException exc) {}

      return strbuf.toString();
    }
    
    /* JORAM_PERF_BRANCH: not permitted
    private final void garbage(int idx) throws IOException {
      garbage(logFile[idx%nbLogFile]);
    }
    */
    
    /**
     * Reports all 'live' operations of a particular log file in the repository, the
     * log file is then cleaned and closed.
     * 
     * @param logf The log file to garbage.
     * @throws IOException
     */
    private final void garbage(LogFile logf, LogFileWriter logFileWriter) throws IOException {
      //logmon.log(BasicLevel.WARN, "Garbage: log#" + logf);
      //logmon.log(BasicLevel.WARN, "logidx=" + logidx);
      //logmon.log(BasicLevel.WARN, "mainLog=" + mainLog);
      //logmon.log(BasicLevel.WARN, "Log content" + logContent(logf.logidx));
      
      if (logf == null) return;

      garbageCount += 1;
      long start = System.currentTimeMillis();
      
      List<Operation> garbagedOperations = new ArrayList<Operation>();
      if (logf.logCounter > 0) {
        Iterator<Operation> iterator = mainLog.values().iterator();
        while (iterator.hasNext()) {
          Operation op = iterator.next();

          if (op.logidx != logf.logidx)
            continue;
          
          garbagedOperations.add(op);
          
          /* Moved in LogWriter
          if ((op.type == Operation.SAVE) || (op.type == Operation.CREATE)) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, "NTransaction, LogFile.Save ("
                  + op.dirName + '/' + op.name + ')');

            byte buf[] = getFromLog(logf, op);

            repository.save(op.dirName, op.name, buf);
          } else if (op.type == Operation.DELETE) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG, "NTransaction, LogFile.Delete ("
                  + op.dirName + '/' + op.name + ')');

            repository.delete(op.dirName, op.name);
          }
          */
          
          iterator.remove();
          
          // Moved in LogWriter
          //op.free();
        }
        
        // Moved in LogWriter
        // repository.commit();
      }

      // Cleans log file
      // JORAM_PERF_BRANCH
      //logf.seek(0);
      //logf.write(Operation.END);
      
      // JORAM_PERF_BRANCH
      // Moved in LogWriter
      //fillAndReset(logf);
      
      if (logf.logidx == logidx) {
        // If the log file is the current one don't close it ! just reset
        // the file pointer so the log can be used a new.
        
        // JORAM_PERF_BRANCH
        //current = 1;
        current = 8;
      } else {
        /* Moved in LogWriter (see recycleLog)
        // Closes the log file and renames it for future use.
        logf.close();

        // Rename the log for a future use
        logf.renameTo();
        
        */
        
        // Cleans the log file array
        logFile[logf.logidx%nbLogFile] = null;
      }
      
      if (logFileWriter != null) {
        LogGarbageContext garbageContext = new LogGarbageContext(logf, garbagedOperations);
        logFileWriter.offer(garbageContext);
      } else {
        garbageOperations(logf, garbagedOperations);
      }
      
      lastGarbageDate = System.currentTimeMillis();
      garbageTime += lastGarbageDate - start;

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "NTransaction.LogFile.garbage() - end: " + (lastGarbageDate - start));
    }
    
    void garbageOperations(LogFile logf, List<Operation> garbagedOperations) throws IOException {
      for (Operation op : garbagedOperations) {
        if ((op.type == Operation.SAVE) || (op.type == Operation.CREATE)) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, "NTransaction, LogFile.Save ("
                + op.dirName + '/' + op.name + ')');

          byte buf[] = getFromLog(logf, op);

          repository.save(op.dirName, op.name, buf);
        } else if (op.type == Operation.DELETE) {
          if (logmon.isLoggable(BasicLevel.WARN))
            logmon.log(BasicLevel.WARN, "NTransaction, LogFile.Delete ("
                + op.dirName + '/' + op.name + ')');

          repository.delete(op.dirName, op.name);
        }
        op.free();
        repository.commit();
      }
      fillAndReset(logf);
      recycleLog(logf);
    }
    
    // JORAM_PERF_BRANCH
    void fillAndReset(LogFile logFile) throws IOException {
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
      
      if (syncOnWrite) {
        // Also sync the metadata
        channel.force(true);
      }
      
      channel.position(0);
    }
    
    void recycleLog(LogFile logf) throws IOException {
      if (logf.logidx != logidx) {
        // Closes the log file and renames it for future use.
        logf.close();

        // Rename the log for a future use
        logf.renameTo();
      }
    }

    void stop() {
//      try {
//        garbage();
//      } catch (IOException exc) {
//        // TODO Auto-generated catch block
//        exc.printStackTrace();
//      }
      
      if ((lockFile != null) && (! lockFile.delete())) {
        logmon.log(BasicLevel.FATAL,
                   "NTransaction.LogFile, can't delete lockfile: " + lockFile.getAbsolutePath());
      }

      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        for (int i=0; i<logFile.length; i++)
          if (logFile[i] != null)
            logmon.log(BasicLevel.DEBUG, "logCounter[" + i + "]=" + logFile[i].logCounter);
        logmon.log(BasicLevel.DEBUG, "log -> " + mainLog.size());

        for (Enumeration<Operation> e = mainLog.elements(); e.hasMoreElements();) {
          logmon.log(BasicLevel.DEBUG, e.nextElement());
        }
      }
    }
    
    static private final byte[] emptyUTFString = {0, 0};

    void writeUTF(String str, OpenByteArrayOutputStream baos)  {
      int strlen = str.length() ;

      int newcount = baos.size() + strlen +2;
      byte[] buf = baos.getBuf();
      int count = baos.size();
      if (newcount > buf.length) {
        byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
        System.arraycopy(buf, 0, newbuf, 0, count);
        buf = newbuf;
        baos.setBuf(newbuf);
      }

      buf[count++] = (byte) ((strlen >>> 8) & 0xFF);
      buf[count++] = (byte) ((strlen >>> 0) & 0xFF);

      str.getBytes(0, strlen, buf, count);
      baos.setCount(newcount);
    }

    void writeInt(int v, OpenByteArrayOutputStream baos) {
      byte[] buf = baos.getBuf();
      int count = baos.size();
      int newcount = count +4;
      if (newcount > buf.length) {
        byte newbuf[] = new byte[buf.length << 1];
        System.arraycopy(buf, 0, newbuf, 0, count);
        buf = newbuf;
        baos.setBuf(newbuf);
      }

      buf[count++] = (byte) ((v >>> 24) & 0xFF);
      buf[count++] = (byte) ((v >>> 16) & 0xFF);
      buf[count++] = (byte) ((v >>>  8) & 0xFF);
      buf[count++] = (byte) ((v >>>  0) & 0xFF);
      baos.setCount(newcount);
    }
    
 // JORAM_PERF_BRANCH
    void writeLong(long l, OpenByteArrayOutputStream baos) {
      byte[] buf = baos.getBuf();
      int count = baos.size();
      int newcount = count +8;
      if (newcount > buf.length) {
        byte newbuf[] = new byte[buf.length << 1];
        System.arraycopy(buf, 0, newbuf, 0, count);
        buf = newbuf;
        baos.setBuf(newbuf);
      }

      buf[count++] = (byte) ((l >>> 56) & 0xFF);
      buf[count++] = (byte) ((l >>> 48) & 0xFF);
      buf[count++] = (byte) ((l >>> 40) & 0xFF);
      buf[count++] = (byte) ((l >>> 32) & 0xFF);
      buf[count++] = (byte) ((l >>> 24) & 0xFF);
      buf[count++] = (byte) ((l >>> 16) & 0xFF);
      buf[count++] = (byte) ((l >>>  8) & 0xFF);
      buf[count++] = (byte) ((l >>>  0) & 0xFF);
      baos.setCount(newcount);
    }
  }

  public static class LogFile extends RandomAccessFile {
    /** Unique index of this log file */
    int logidx;
    /** Number of valid operation in this log file */
    int logCounter = 0;

    File dir;
    
    /** Maximum index of existing log files */
    static int maxUsedIdx = -1;
    
    // JORAM_PERF_BRANCH
    long logId;
    
    /**
     *  Creates a random access file stream to read from and to write to the file specified
     * by the File argument.
     *  The file is open in "rwd" mode and require that every update to the file's content be
     * written synchronously to the underlying storage device. 
     *  
     * @param logFile the specified file.
     */
    public LogFile(File dir, int logidx, String mode) throws FileNotFoundException {
      super(new File(dir, "log#" + logidx), mode);
      if (logidx > maxUsedIdx) maxUsedIdx = logidx;
      this.logidx = logidx;
      this.dir = dir;
    }

    public void renameTo() {
      maxUsedIdx += 1;
      new File(dir, "log#" + logidx).renameTo(new File(dir, "log#" + maxUsedIdx));
    }

    @Override
    public String toString() {
      return "LogFile [logidx=" + logidx + ", logCounter=" + logCounter
          + ", dir=" + dir + ", logId=" + logId + "]";
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
    strbuf.append(",NbLoadedFromLog=").append(getNbLoadedFromLog());
    strbuf.append(",NbSavedObjects=").append(getNbSavedObjects());
    strbuf.append(",NbDeletedObjects=").append(getNbDeletedObjects());
    strbuf.append(",NbBadDeletedObjects=").append(getNbBadDeletedObjects());
    strbuf.append(",NbLoadedObjects=").append(getNbLoadedObjects());
    strbuf.append(')');
    
    return strbuf.toString();
  }

  public String logCounters() {
    return logManager.logCounters();
  }
  
  public String logContent(int idx) throws IOException {
    begin();
    String res = logManager.logContent(idx);
    commit(true);
    return res;
  }

  /* JORAM_PERF_BRANCH: Not permitted
  public void garbage(int idx) throws IOException {
    begin();
    logManager.garbage(idx);
    commit(true);
  }
  */
}

