/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package com.scalagent.txlog;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodedString;
import fr.dyade.aaa.common.encoding.SerializableWrapper;
import fr.dyade.aaa.common.encoding.StringPair;
import fr.dyade.aaa.util.BaseTransaction;
import fr.dyade.aaa.util.Repository;

public class TxLogTransaction extends BaseTransaction implements TxLogTransactionMBean {
  
  public static final Logger logmon = Debug.getLogger(TxLogTransaction.class.getName());
  
  private TxLog txlog;
  
  protected int phase;
  
  private boolean useLockFile;
  
  private File lockFile;
  
  public TxLogTransaction() {
    super();
  }

  public TxLog getTxlog() {
    return txlog;
  }
  
  protected final void setPhase(int newPhase) {
    phase = newPhase;
  }

  public final void begin() throws IOException {
    try {
      // This is temporary: caused by the current transaction model
      // Need to encode the records out of the
      // transaction lock otherwise the encoding increases
      // the time needed by the transaction to complete.
      txlog.initValueRecords();
    } catch (Exception e) {
      throw new IOException(e);
    }
    
    synchronized (this) {
      while (phase != FREE) {
        try {
          wait();
        } catch (InterruptedException exc) {
        }
      }
      // Change the transaction state.
      setPhase(RUN);
    }
  }

  public synchronized void close() {
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "TxLogTransaction, closes");

    if (phase == INIT) return;

    while (phase != FREE) {
      // Wait for the transaction subsystem to be free
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }

    setPhase(FINALIZE);
    
    try {
      txlog.close();
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
    }
    
    setPhase(INIT);

    if (logmon.isLoggable(BasicLevel.INFO)) {
      logmon.log(BasicLevel.INFO, "TxLogTransaction, closed: " + toString());
    }
  }
  
  public final synchronized void commit(boolean release) throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Illegal phase: " + phase);

    commit(null);

    if (release) {
      setPhase(FREE);
      notify();
    } else {
      setPhase(COMMIT);
    }
  }

  public void commit(Runnable callback) throws IOException {
    try {
      txlog.commit();
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
    
    if (callback != null) {
      callback.run();
    }
  }

  public boolean containsOperations() {
    return txlog.recordsToCommit();
  }
  
  public void create(Encodable object, Encodable objectId) throws IOException {
    Encodable encodable = isEncodable(object);
    if (encodable == null) {
      encodable = new SerializableWrapper((Serializable) object);
    }
    try {
      txlog.create(objectId, encodable);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public void create(Serializable object, String name) throws IOException {
    Encodable encodable = isEncodable(object);
    if (encodable == null) {
      encodable = new SerializableWrapper(object);
    }
    try {
      txlog.create(new EncodedString(name), encodable);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public void create(Serializable object, String dirname, String name)
      throws IOException {
    Encodable encodable = isEncodable(object);
    if (encodable == null) {
      encodable = new SerializableWrapper(object);
    }
    try {
      txlog.create(new StringPair(new EncodedString(dirname), new EncodedString(name)), encodable);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }
  
  public void createByteArray(byte[] bytes, Encodable objectId) throws IOException {
    try {
      txlog.create(objectId, bytes);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public void createByteArray(byte[] bytes, String name) throws IOException {
    try {
      txlog.create(new EncodedString(name), bytes);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public void createByteArray(byte[] bytes, String dirname, String name)
      throws IOException {
    try {
      txlog.create(new StringPair(new EncodedString(dirname),
          new EncodedString(name)), bytes);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }
  
  public void delete(Encodable objectId) {
    txlog.delete(objectId);
  }

  public void delete(String name) {
    txlog.delete(new EncodedString(name));
  }

  public void delete(String dirname, String name) {
    txlog.delete(new StringPair(new EncodedString(dirname), new EncodedString(name)));
  }

  public String[] getList(String prefix) {
    return txlog.getList(prefix);
  }

  public int getPhase() {
    // no phase is managed
    return 0;
  }

  public String getPhaseInfo() {
    // no phase is managed
    return null;
  }
  
  public void init(String path) throws IOException {
    init(path, true);
  }
  
  public boolean getBoolean(String key, boolean defaultValue) {
    String result = getProperty(key);
    if (result != null) {
      if (result.equalsIgnoreCase("true")) {
        return true;
      } else {
        return false;
      }
    } else {
      return defaultValue;
    }
  }

  public void init(String path, boolean start) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLogTransaction.init(" + path + ')');
    phase = INIT;
    
    File dir = new File(path);
    if (! dir.exists()) {
      dir.mkdir();
    }
    
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
    
    useLockFile = getBoolean("Transaction.UseLockFile");
    
    if (useLockFile) {
      File lockFile = new File(dir, "lock");
      if (! lockFile.createNewFile()) {
        logmon.log(BasicLevel.FATAL,
                   "TxLog.init(): Either the server is already running, " + 
                   "either you have to remove lock file " + lockFile.getAbsolutePath());
        throw new IOException("Transaction already running.");
      }
      lockFile.deleteOnExit();
    }
    
    int fileSize = getInteger("Transaction.FileSize", 10).intValue()
        * Mb;
    int minFileCount = getInteger("Transaction.MinFileCount", 10).intValue();
    boolean syncOnWrite = getBoolean("Transaction.SyncOnWrite", true);
    int compactRatio = getInteger("Transaction.CompactRatio", 30);
    int compactCountThreshold = getInteger("Transaction.CompactCountThreshold", 10);
    int minCompactFileCount = getInteger("Transaction.MinCompactFileCount", minFileCount / 2);
    int maxFileCount = getInteger("Transaction.MaxFileCount", 50).intValue();
    boolean synchronousCompact = getBoolean("Transaction.SynchronousCompact");
    int maxSaveCount = getInteger("Transaction.MaxSaveCount", 1000).intValue();
    int compactDelay = getInteger("Transaction.CompactDelay", 30000).intValue();
    
    String encodableFactoryInitClassName = getProperty("Transaction.EncodableFactoryInit");
    if (encodableFactoryInitClassName != null) {
      try {
        Class encodableFactoryInitClass = Class.forName(encodableFactoryInitClassName);
        Method initMethod = encodableFactoryInitClass.getMethod("init");
        initMethod.invoke(null);
      } catch (ClassNotFoundException e) {
        throw new IOException(e);
      } catch (NoSuchMethodException e) {
        throw new IOException(e);
      } catch (SecurityException e) {
        throw new IOException(e);
      } catch (IllegalAccessException e) {
        throw new IOException(e);
      } catch (IllegalArgumentException e) {
        throw new IOException(e);
      } catch (InvocationTargetException e) {
        throw new IOException(e);
      }
      
    }
    
    File logDirectory = new File(dir, "txlog");
    if (! logDirectory.exists()) {
      logDirectory.mkdir();
    }
    
    File repositoryDirectory = new File(dir, "repository");
    if (! repositoryDirectory.exists()) {
      repositoryDirectory.mkdir();
    }
  
    Repository repository;
    try {
      String repositoryImpl = getProperty("Transaction.RepositoryImpl", "fr.dyade.aaa.util.FileRepository");
      repository = (Repository) Class.forName(repositoryImpl).newInstance();
      repository.init(this, repositoryDirectory);
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

    txlog = new TxLog(repository);

    txlog.setFileSize(fileSize);
    txlog.setMinFileCount(minFileCount);
    txlog.setLogDirectory(logDirectory);
    txlog.setSyncOnWrite(syncOnWrite);
    txlog.setCompactRatio(compactRatio);
    txlog.setCompactCountThreshold(compactCountThreshold);
    txlog.setMinCompactFileCount(minCompactFileCount);
    txlog.setMaxFileCount(maxFileCount);
    txlog.setSynchronousCompact(synchronousCompact);
    txlog.setMaxSaveCount(maxSaveCount);
    txlog.setCompactDelay(compactDelay);
    
    txlog.setRepositoryDirectory(repositoryDirectory);

    if (start) {
      try {
        start();
      } catch (Exception e) {
        if (logmon.isLoggable(BasicLevel.ERROR))
          logmon.log(BasicLevel.ERROR, "", e);
        throw new IOException(e);
      }
    }
    
    setPhase(FREE);
  }
  
  public void start() throws Exception {
    txlog.init();
  }

  public boolean isPersistent() {
    return true;
  }
  
  public Encodable load(Encodable objectId) throws IOException, ClassNotFoundException {
    try {
      Encodable value = txlog.load(objectId);
      if (value instanceof SerializableWrapper) {
        SerializableWrapper sw = (SerializableWrapper) value;
        return (Encodable) sw.getValue();
      } else {
        return value;
      }
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public Object load(String name) throws IOException, ClassNotFoundException {
    try {
      Encodable value = txlog.load(new EncodedString(name));
      if (value instanceof SerializableWrapper) {
        SerializableWrapper sw = (SerializableWrapper) value;
        return sw.getValue();
      } else {
        return value;
      }
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public Object load(String dirname, String name) throws IOException,
      ClassNotFoundException {
    try {
      Encodable value = txlog.load(new StringPair(new EncodedString(dirname), new EncodedString(name)));
      if (value instanceof SerializableWrapper) {
        SerializableWrapper sw = (SerializableWrapper) value;
        return sw.getValue();
      } else {
        return value;
      }
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }
  
  public byte[] loadByteArray(Encodable objectId) throws IOException,
      ClassNotFoundException {
    try {
      return txlog.loadEncodedValue(objectId);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public byte[] loadByteArray(String name) throws IOException,
      ClassNotFoundException {
    try {
      return txlog.loadEncodedValue(new EncodedString(name));
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public byte[] loadByteArray(String dirname, String name) throws IOException {
    try {
      return txlog.loadEncodedValue(new StringPair(new EncodedString(dirname), new EncodedString(name)));
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public synchronized void release() throws IOException {
    if ((phase != RUN) && (phase != COMMIT) && (phase != ROLLBACK))
      throw new IllegalStateException("Can not release transaction: " + getPhaseInfo() + '.');

    // Change the transaction state.
    setPhase(FREE);
    // wake-up an eventually user's thread in begin
    notify();
  }
  
  private Encodable isEncodable(Object object) {
    if (object instanceof Encodable) {
      Encodable encodable = (Encodable) object;
      if (encodable.getClassId() >= 0) {
        return encodable;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
  
  public void save(Encodable object, Encodable objectId) throws IOException {
    Encodable encodable = isEncodable(object);
    if (encodable == null) {
      encodable = new SerializableWrapper((Serializable) object);
    }
    try {
      txlog.save(objectId, encodable);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public void save(Serializable object, String name) throws IOException {
    Encodable encodable = isEncodable(object);
    if (encodable == null) {
      encodable = new SerializableWrapper(object);
    }
    try {
      txlog.save(new EncodedString(name), encodable);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public void save(Serializable object, String dirname, String name)
      throws IOException {
    Encodable encodable = isEncodable(object);
    if (encodable == null) {
      encodable = new SerializableWrapper(object);
    }
    try {
      txlog.save(new StringPair(new EncodedString(dirname), new EncodedString(name)), encodable);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public void save(Serializable object, String dirname, String name, boolean first)
      throws IOException {
    if (first) {
      create(object, dirname, name);
    } else {
      save(object, dirname, name);
    }
  }
  
  public void saveByteArray(byte[] bytes, Encodable objectId) throws IOException {
    try {
      txlog.save(objectId, bytes);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public void saveByteArray(byte[] bytes, String name) throws IOException {
    try {
      txlog.save(new EncodedString(name), bytes);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public void saveByteArray(byte[] bytes, String dirname, String name)
      throws IOException {
    try {
      txlog.save(new EncodedString(name), bytes);
    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", e);
      throw new IOException(e);
    }
  }

  public void saveByteArray(byte[] bytes, Encodable objectId,
      boolean copy, boolean first) throws IOException {
    if (copy) {
      byte[] copyBytes = new byte[bytes.length];
      System.arraycopy(bytes, 0, copyBytes, 0, bytes.length);
      bytes = copyBytes;
    }
    if (first) {
      createByteArray(bytes, objectId);
    } else {
      saveByteArray(bytes, objectId);
    }
  }

  public void saveByteArray(byte[] bytes, String dirname, String name,
      boolean copy, boolean first) throws IOException {
    if (copy) {
      byte[] copyBytes = new byte[bytes.length];
      System.arraycopy(bytes, 0, copyBytes, 0, bytes.length);
      bytes = copyBytes;
    }
    if (first) {
      create(bytes, dirname, name);
    } else {
      save(bytes, dirname, name);
    }
  }

  public void stop() {
    close();
    
    if ((lockFile != null) && (! lockFile.delete())) {
      logmon.log(BasicLevel.FATAL,
                 "TxLogTransaction.LogFile, can't delete lockfile: " + lockFile.getAbsolutePath());
    }
  }

  public int getFileSize() {
    return txlog.getFileSize();
  }

  public int getMinFileCount() {
    return txlog.getMinFileCount();
  }

  public boolean isSyncOnWrite() {
    return txlog.isSyncOnWrite();
  }

  public int getCompactCountThreshold() {
    return txlog.getCompactCountThreshold();
  }

  public int getCompactRatio() {
    return txlog.getCompactRatio();
  }
  
  public int getRecycledEmptyFileCount() {
    return txlog.getRecycledEmptyFileCount();
  }
  
  public int getRecycledCompactedFileCount() {
    return txlog.getRecycledCompactedFileCount();
  }

  public int getDeletedFileCount() {
    return txlog.getDeletedFileCount();
  }
  
  public int getNewFileCount() {
    return txlog.getNewFileCount();
  }
  
  public int getMinCompactFileCount() {
    return txlog.getMinCompactFileCount();
  }
  
  public int getMaxFileCount() {
    return txlog.getMaxFileCount();
  }
  
  public int getRecordCount() {
    return txlog.getRecordCount();
  }
  
  public int getWrittenWhileCompactingCount() {
    return txlog.getWrittenWhileCompactingCount();
  }
  
  public int getCompactCount() {
    return txlog.getCompactCount();
  }
  
  public long getCompactDuration() {
    return txlog.getCompactDuration();
  }
  
  public int getFileToCompactCount() {
    return txlog.getFileToCompactCount();
  }
  
  public int getCreateRecordCount() {
    return txlog.getCreateRecordCount();
  }
  
  public int getDeleteRecordCount() {
    return txlog.getDeleteRecordCount();
  }
  
  public long getUsedFileCompactThreshold() {
    return txlog.getUsedFileCompactThreshold();
  }

  public long getUsedFileLiveSize() {
    return txlog.getUsedFileLiveSize();
  }
  
  public boolean isSynchronousCompact() {
    return txlog.isSynchronousCompact();
  }
  
  public int getLiveRecordCount() {
    return txlog.getLiveRecordCount();
  }
  
  public int getLargestRecordEncodedSize() {
    return txlog.getLargestRecordEncodedSize();
  }
  
  public int getMaxSaveCount() {
    return txlog.getMaxSaveCount();
  }
  
  public int getUsedFileCount() {
    return txlog.getUsedFileCount();
  }
  
  public int getInitFileCount() {
    return txlog.getInitFileCount();
  }
  
  public int getAvailableFileCount() {
    return txlog.getAvailableFileCount();
  }
  
  public int getCompactDelay() {
    return txlog.getCompactDelay();
  }

  public void setCompactDelay(int compactDelay) {
    txlog.setCompactDelay(compactDelay);
  }

}
