/*
 * Copyright (C) 2004 - 2006 ScalAgent Distributed Technologies
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

public final class NTransaction implements Transaction, NTransactionMBean {
  // Logging monitor
  private static Logger logmon = null;

  /**
   *  Global in memory log initial capacity, by default 4096.
   *  This value can be adjusted for a particular server by setting
   * <code>NTLogMemoryCapacity</code> specific property.
   * <p>
   *  These property can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  static int LogMemoryCapacity = 4096;

  /**
   * Returns the initial capacity of global in memory log (by default 4096).
   *
   * @return The initial capacity of global in memory log.
   */
  public int getLogMemoryCapacity() {
    return LogMemoryCapacity;
  }

  /**
   *  Maximum size of memory log, by default 2048Kb.
   *  This value can be adjusted (Kb) for a particular server by setting
   * <code>NTLogMemorySize</code> specific property.
   * <p>
   *  These property can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  static int MaxLogMemorySize = 2048 * Kb;

  /**
   * Returns the maximum size of memory log in Kb, by default 2048Kb.
   *
   * @return The maximum size of memory log in Kb.
   */
  public int getMaxLogMemorySize() {
    return MaxLogMemorySize/Mb;
  }

  /**
   * Sets the maximum size of memory log in Kb.
   *
   * @param size The maximum size of memory log in Kb.
   */
  public void setMaxLogMemorySize(int size) {
    if (size > 0) MaxLogMemorySize = size *Mb;
  }

  /**
   * Returns the size of memory log in byte.
   *
   * @return The size of memory log in byte.
   */
  public int getLogMemorySize() {
    return logFile.logMemorySize;
  }

  /**
   *  Size of disk log in Mb, by default 16Mb.
   *  This value can be adjusted (Mb) for a particular server by setting
   * <code>NTLogFileSize</code> specific property.
   * <p>
   *  These property can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  static int LogFileSize = 16 * Mb;

  /**
   * Returns the size of disk log in Mb, by default 16Mb.
   *
   * @return The size of disk log in Mb.
   */
  public int getLogFileSize() {
    return LogFileSize/Mb;
  }

  /**
   * Sets the size of disk log in Mb.
   *
   * @param size The size of disk log in Mb.
   */
  public void setLogFileSize(int size) {
    if (size > 0) LogFileSize = size *Mb;
  }

  /**
   *  Number of pooled operation, by default 1000.
   *  This value can be adjusted for a particular server by setting
   * <code>NTLogThresholdOperation</code> specific property.
   * <p>
   *  These property can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  static int LogThresholdOperation = 1000;

  /**
   * Returns the pool size for <code>operation</code> objects, by default 1000.
   *
   * @return The pool size for <code>operation</code> objects.
   */
  public int getLogThresholdOperation() {
    return LogThresholdOperation;
  }

  /**
   * Returns the number of commit operation since starting up.
   *
   * @return The number of commit operation.
   */
  public int getCommitCount() {
    return logFile.commitCount;
  }

  /**
   * Returns the number of garbage operation since starting up.
   *
   * @return The number of garbage operation.
   */
  public int getGarbageCount() {
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

  private Timer timer = null;
  private GarbageTask task = null;

  /**
   *  Sets asynchronous garbage.
   *
   * @param async 	If true activates the asynchronous garbage,
   *			deasctivates otherwise.
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
      if (timer == null) timer = new Timer();
      if (logFile.garbageTimeOut > 0) {
        try {
          timer.schedule(this, logFile.garbageTimeOut);
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
        try {
          timer.schedule(this, logFile.garbageTimeOut);
        } catch (Exception exc) {
          logmon.log(BasicLevel.ERROR,
                     "NTransaction, cannot schedule garbage task ", exc);
        }
      }
    }
  }

  long startTime = 0L;

  /**
   * Returns the starting time.
   *
   * @return The starting time.
   */
  public long getStartTime() {
    return startTime;
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
   *  These property can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
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

  Repository repository = null;

  /**
   *  ThreadLocal variable used to get the log to associate state with each
   * thread. The log contains all operations do by the current thread since
   * the last <code>commit</code>. On commit, its content is added to current
   * log (memory + disk), then it is freed.
   */
  private ThreadLocal perThreadContext = null;

  static final boolean debug = false;

  public NTransaction() {}

  /**
   * Tests if the Transaction component is persistent.
   *
   * @return true.
   */
  public boolean isPersistent() {
    return true;
  }

  public final void init(String path) throws IOException {
    phase = INIT;

    logmon = Debug.getLogger("fr.dyade.aaa.util.Transaction");
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "NTransaction, init()");

    LogMemoryCapacity = Integer.getInteger("NTLogMemoryCapacity",
                                           LogMemoryCapacity).intValue();
    LogFileSize = Integer.getInteger("NTLogFileSize",
                                     LogFileSize /Mb).intValue() *Mb;
    MaxLogMemorySize = Integer.getInteger("NTLogMemorySize",
                                          MaxLogMemorySize /Kb).intValue() *Kb;

    dir = new File(path);
    if (!dir.exists()) dir.mkdir();
    if (!dir.isDirectory())
      throw new FileNotFoundException(path + " is not a directory.");

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

    try {
      repositoryImpl = System.getProperty("NTRepositoryImpl", repositoryImpl);
      repository = (Repository) Class.forName(repositoryImpl).newInstance();
      repository.init(dir);
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
    logFile = new LogFile(dir, repository);

    perThreadContext = new ThreadLocal() {
        protected synchronized Object initialValue() {
          return new Context();
        }
      };
    
    // Be careful, setGarbageDelay and garbageAsync use logFile !!
    setGarbageDelay(Integer.getInteger("NTGarbageDelay",
                                       getGarbageDelay()).intValue());
    garbageAsync(Boolean.getBoolean("NTAsyncGarbage"));

    startTime = System.currentTimeMillis();

    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "NTransaction, initialized " + startTime);

    /* The Transaction subsystem is ready */
    setPhase(FREE);
  }

  public final File getDir() {
    return dir;
  }

  /**
   * Returns the path of persistence directory.
   *
   * @return The path of persistence directory.
   */
  public String getPersistenceDir() {
    return dir.getPath();
  }

  // State of the transaction monitor.
  private int phase = INIT;
  String phaseInfo = PhaseInfo[phase];

  /**
   *
   */
  public int getPhase() {
    return phase;
  }

  public String getPhaseInfo() {
    return phaseInfo;
  }

  private final void setPhase(int newPhase) {
    phase = newPhase;
    phaseInfo = PhaseInfo[phase];
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
        } else if (((Operation) logFile.log.get(list2[i])).type == Operation.SAVE) {
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

  public Object load(String dirName, String name) throws IOException, ClassNotFoundException {
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
      return repository.loadobj(dirName, name);
    } catch (FileNotFoundException exc) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "NTransaction, load(" + dirName + ", " + name + ") NOT FOUND");

      return null;
    }
  }

  public final byte[] loadByteArray(String name) throws IOException {
    return loadByteArray(null, name);
  }


  public byte[] loadByteArray(String dirName, String name) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "NTransaction, loadByteArray(" + dirName + ", " + name + ")");

    // First searchs in the logs a new value for the object.
    try {
      byte[] buf = getFromLog(dirName, name);
      if (buf != null) return buf;

      // Gets it from disk.      
      return repository.load(dirName, name);
    } catch (FileNotFoundException exc) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "NTransaction, loadByteArray(" + dirName + ", " + name + ") NOT FOUND");

      return null;
    }
  }

  public final void delete(String name) {
    delete(null, name);
  }
  
  public final void delete(String dirName, String name) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "NTransaction, delete(" + dirName + ", " + name + ")");

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
    if ((phase != RUN) && (phase != COMMIT) && (phase != ROLLBACK))
      throw new IllegalStateException("Can not release transaction.");

    // Change the transaction state.
    setPhase(FREE);
    // wake-up an eventually user's thread in begin
    notify();
  }

  /**
   * Garbage the log file.
   * It waits all transactions termination, then the log file is garbaged
   * and all operations are reported to disk.
   */
  public final synchronized void garbage() {
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "NTransaction, stops");

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
      logmon.log(BasicLevel.INFO,
                 "NTransaction, stopped: " +
                 "garbage=" + logFile.garbageCount + ", " +
                 "commit=" + logFile.commitCount + ", " +
                 "ratio=" + getGarbageRatio());
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
    try {
      logFile.garbage();
    } catch (IOException exc) {
      logmon.log(BasicLevel.WARN, "NTransaction, can't garbage logfile", exc);
    }
    setPhase(FREE);

    if (logmon.isLoggable(BasicLevel.INFO)) {
      logmon.log(BasicLevel.INFO,
                 "NTransaction, stopped: " +
                 "garbage=" + logFile.garbageCount + ", " +
                 "commit=" + logFile.commitCount + ", " +
                 "ratio=" + getGarbageRatio());
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
      logmon.log(BasicLevel.INFO, "NTransaction, stops");

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
      logmon.log(BasicLevel.INFO,
                 "NTransaction, closed: " +
                 "garbage=" + logFile.garbageCount + ", " +
                 "commit=" + logFile.commitCount + ", " +
                 "ratio=" + getGarbageRatio());
    }
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

    /** Root directory of transaction storage */
    private File dir = null;
    /** Coherency lock filename */
    static private final String LockPathname = "lock";
    /** Coherency lock file */
    private File lockFile = null;

    private Repository repository = null;

    LogFile(File dir, Repository repository) throws IOException {
      super(4 * Kb);
      this.dir = dir;
      this.repository = repository;

      boolean nolock = Boolean.getBoolean("NTNoLockFile");
      if (! nolock) {
        lockFile = new File(dir, LockPathname);
        if (! lockFile.createNewFile()) {
          logmon.log(BasicLevel.FATAL,
                     "NTransaction.init(): " +
                     "Either the server is already running, " + 
                     "either you have to remove lock file: " +
                     lockFile.getAbsolutePath());
          throw new IOException("Transaction already running.");
        }
        lockFile.deleteOnExit();
      }

      //  Search for old log file, then apply all committed operation,
      // finally cleans it.
      log = new Hashtable(LogMemoryCapacity);

      
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
        logFile.setLength(LogFileSize);

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

    int logMemorySize = 0;

    /**
     * Reports all buffered operations in logs.
     */
    void commit(Hashtable ctxlog) throws IOException {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "NTransaction.LogFile.commit()");

      commitCount += 1;
      
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
          logMemorySize += op.value.length;

          writeInt(op.value.length);
          write(op.value);
        }

        // Reports all committed operation in clog
        op = (Operation) log.put(OperationKey.newKey(op.dirName, op.name), op);
        if (op != null) {
          if (op.type == Operation.SAVE)
            logMemorySize -= op.value.length;

          op.free();
        }
      }
      write(Operation.END);

      // AF: Is it needed? Normally the file pointer should already set at
      // the right position... To be verified, but not really a big cost.
      logFile.seek(current);
      logFile.write(buf, 0, count);

      // AF: May be we can avoid this second synchronous write, using a
      // marker: determination d'un marqueur lié au log courant (date en
      // millis par exemple), ecriture du marqueur au debut du log, puis
      // ecriture du marqueur apres chaque Operation.COMMIT.
      logFile.seek(current -1);
      logFile.write(Operation.COMMIT);

      current += (count);
      reset();

      ctxlog.clear();

      if ((current > LogFileSize) || (logMemorySize > MaxLogMemorySize) ||
          ((garbageTimeOut > 0) && (System.currentTimeMillis() > (lastGarbageTime + garbageTimeOut))))
        garbage();
    }

    /**
     * Reports all logged operations on disk.
     */
    private final void garbage() throws IOException {
      long start = System.currentTimeMillis();

      if (logmon.isLoggable(BasicLevel.INFO))
        logmon.log(BasicLevel.INFO,
                   "NTransaction.LogFile.garbage() - begin");

      garbageCount += 1;

      Operation op = null;
      for (Enumeration e = log.elements(); e.hasMoreElements(); ) {
        op = (Operation) e.nextElement();

        if (op.type == Operation.SAVE) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "NTransaction, LogFile.Save (" + op.dirName + ',' + op.name + ')');

          repository.save(op.dirName, op.name, op.value);
        } else if (op.type == Operation.DELETE) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "NTransaction, LogFile.Delete (" + op.dirName + ',' + op.name + ')');

          repository.delete(op.dirName, op.name);
//           if (!deleted && file.exists())
//             logmon.log(BasicLevel.ERROR,
//                        "NTransaction, can't delete " + file.getCanonicalPath());
        }
        op.free();
      }
      //  Be careful, do not clear log before all modifications are reported
      // to disk, in order to avoid load errors.
      log.clear();
      logMemorySize = 0;

      repository.commit();

      current = 1;
      // Cleans log file
      logFile.seek(0);
      logFile.write(Operation.END);

      long end = System.currentTimeMillis();
      lastGarbageTime = end;
      garbageTime += end - start;

      if (logmon.isLoggable(BasicLevel.INFO))
        logmon.log(BasicLevel.INFO,
                   "NTransaction.LogFile.garbage() - end: " + (end - start));
    }

    void stop() {
      if (logmon.isLoggable(BasicLevel.INFO))
        logmon.log(BasicLevel.INFO, "NTransaction, stops");

      try {
        garbage();
        logFile.close();
        repository.close();
      } catch (IOException exc) {
        logmon.log(BasicLevel.WARN, "NTransaction, can't close logfile", exc);
      }

      if ((lockFile != null) && (! lockFile.delete())) {
        logmon.log(BasicLevel.FATAL,
                   "NTransaction, - can't delete lockfile: " +
                   lockFile.getAbsolutePath());
      }

      if (logmon.isLoggable(BasicLevel.INFO))
        logmon.log(BasicLevel.INFO, "NTransaction, exits.");
    }
  }

  public static void main(String[] args) throws Exception {
    if ("garbage".equals(args[0])) {
      NTransaction transaction = new NTransaction();
      transaction.init(args[1]);
      transaction.stop();
    } else if ("list".equals(args[0])) {
    } else {
      System.err.println("unknown command: " + args[0]);
    }
  }
}

final class Operation implements Serializable {
  static final int SAVE = 1;
  static final int DELETE = 2;
  static final int COMMIT = 3;
  static final int END = 127;
 
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
    pool = new Pool("NTransaction$Operation",
                    Integer.getInteger("NTLogThresholdOperation",
                                       NTransaction.LogThresholdOperation).intValue());
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

final class OperationKey {
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
