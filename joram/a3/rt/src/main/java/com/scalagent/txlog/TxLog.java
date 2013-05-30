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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.ByteBufferDecoder;
import fr.dyade.aaa.common.encoding.ByteBufferEncoder;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.Encoder;
import fr.dyade.aaa.util.Repository;

public class TxLog {
  
  public static final Logger logmon = Debug.getLogger(TxLog.class.getName());
  
  public static final byte RESET_VALUE = 0;
  
  public static final byte COMMIT_TAG = 1;
  
  public static final boolean USE_REPOSITORY = true;
  
  private TxLogFile currentFile;
  
  private TxLogFileManager fileManager;

  private Map<Encodable, Record> records;
  
  private CompactingContext compactingContext;
  
  private boolean compacting;
  
  private Lock commitLock;
  
  private Lock loadLock;
  
  private ThreadLocal<TransactionContext> transactionContext;
  
  private int fileSize;
  
  private int minFileCount;
  
  private int compactRatio;
  
  private int minCompactFileCount;
  
  private boolean syncOnWrite;
  
  private File logDirectory;
  
  private ExecutorService compactingExecutor;
  
  private CompactingTask compactingTask;
  
  private Repository repository;
  
  private int compactCountThreshold;
  
  private int recycledEmptyFileCount;
  
  private int maxFileCount;
  
  private int writtenWhileCompactingCount;
  
  private int compactCount;
  
  private long compactDuration;
  
  private int fileToCompactCount;
  
  private AtomicBoolean compactingTaskRunning;
  
  private int createRecordCount;
  
  private int deleteRecordCount;
  
  private long usedFileCompactThreshold;
  
  private long usedFileLiveSize;
  
  private boolean synchronousCompact;
  
  private int liveRecordCount;
  
  private File repositoryDirectory;
  
  private HashMap<String, String> repositoryCache;
  
  private TxLogListener logListener;
  
  private int largestRecordEncodedSize;
  
  private int maxSaveCount;
  
  private ThreadGroup threadGroup;
  
  private long lastCompactDate;
  
  private int compactDelay;
  
  public TxLog(Repository repository) {
    this.repository = repository;
    commitLock = new ReentrantLock();
    loadLock = new ReentrantLock();
    compactingTaskRunning = new AtomicBoolean();
    threadGroup = new ThreadGroup(this.getClass().getName());
  }
  
  public void init() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLog.init()");
    
    repositoryCache = new HashMap<String, String>();
    String[] allObjectNames = repository.list("");
    for (String objectName : allObjectNames) {
      repositoryCache.put(objectName, objectName);
    }
    
    records = new Hashtable<Encodable, Record>();
    
    compactingExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {

      public Thread newThread(Runnable runnable) {
        Thread newThread = new Thread(threadGroup, runnable, "TxLogFileManager.Compacting");
        return newThread;
      }
    });

    transactionContext = new ThreadLocal<TransactionContext>() {
      protected TransactionContext initialValue() {
        return new TransactionContext();
      }
    };

    compactingTask = new CompactingTask();

    fileManager = new TxLogFileManager(this);
    fileManager.init();

    TxLogFile firstFile = fileManager.getLastUsedFile();

    if (firstFile == null) {
      firstFile = fileManager.getLogFile();
    }
    
    setCurrentFile(firstFile);
    
    // Drop the repository cache
    repositoryCache = null;
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "current file: " + currentFile);
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "records = " + records);
  }
  
  private void setCurrentFile(TxLogFile file) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLog.setCurrentFile(" + file + ')');
    currentFile = file;
    currentFile.open();
    currentFile.setCurrentPosition();
  }
  
  public TxLogFile getCurrentFile() {
    return currentFile;
  }

  public int getFileSize() {
    return fileSize;
  }

  public void setFileSize(int fileSize) {
    this.fileSize = fileSize;
  }

  public int getMinFileCount() {
    return minFileCount;
  }

  public void setMinFileCount(int minFileCount) {
    this.minFileCount = minFileCount;
  }

  public boolean isSyncOnWrite() {
    return syncOnWrite;
  }

  public void setSyncOnWrite(boolean syncOnWrite) {
    this.syncOnWrite = syncOnWrite;
  }

  public int getCompactRatio() {
    return compactRatio;
  }

  public void setCompactRatio(int compactRatio) {
    this.compactRatio = compactRatio;
  }

  public File getLogDirectory() {
    return logDirectory;
  }

  public void setLogDirectory(File logDirectory) {
    this.logDirectory = logDirectory;
  }

  public int getCompactCountThreshold() {
    return compactCountThreshold;
  }

  public int getRecycledEmptyFileCount() {
    return recycledEmptyFileCount;
  }
  
  public int getRecycledCompactedFileCount() {
    return fileManager.getRecycledCompactedFileCount();
  }

  public int getDeletedFileCount() {
    return fileManager.getDeletedFileCount();
  }
  
  public int getNewFileCount() {
    return fileManager.getNewFileCount();
  }

  public void setCompactCountThreshold(int compactCountThreshold) {
    this.compactCountThreshold = compactCountThreshold;
  }

  public int getMinCompactFileCount() {
    return minCompactFileCount;
  }

  public void setMinCompactFileCount(int minCompactFileCount) {
    this.minCompactFileCount = minCompactFileCount;
  }

  public int getMaxFileCount() {
    return maxFileCount;
  }

  public void setMaxFileCount(int maxFileCount) {
    this.maxFileCount = maxFileCount;
  }
  
  public int getRecordCount() {
    return records.size();
  }
  
  public int getWrittenWhileCompactingCount() {
    return writtenWhileCompactingCount;
  }

  public int getFileToCompactCount() {
    return fileToCompactCount;
  }

  public int getCompactCount() {
    return compactCount;
  }

  public long getCompactDuration() {
    return compactDuration;
  }

  public int getCreateRecordCount() {
    return createRecordCount;
  }

  public int getDeleteRecordCount() {
    return deleteRecordCount;
  }

  public long getUsedFileCompactThreshold() {
    return usedFileCompactThreshold;
  }

  public long getUsedFileLiveSize() {
    return usedFileLiveSize;
  }

  public boolean isSynchronousCompact() {
    return synchronousCompact;
  }

  public void setSynchronousCompact(boolean synchronousCompact) {
    this.synchronousCompact = synchronousCompact;
  }

  public File getRepositoryDirectory() {
    return repositoryDirectory;
  }

  public void setRepositoryDirectory(File repositoryDirectory) {
    this.repositoryDirectory = repositoryDirectory;
  }

  public int getLiveRecordCount() {
    return liveRecordCount;
  }

  public TxLogListener getLogListener() {
    return logListener;
  }

  public void setLogListener(TxLogListener logListener) {
    this.logListener = logListener;
  }

  public TxLogFileManager getFileManager() {
    return fileManager;
  }

  public Map<Encodable, Record> getRecords() {
    return records;
  }

  public Repository getRepository() {
    return repository;
  }

  public int getLargestRecordEncodedSize() {
    return largestRecordEncodedSize;
  }

  public int getMaxSaveCount() {
    return maxSaveCount;
  }

  public void setMaxSaveCount(int maxSaveCount) {
    this.maxSaveCount = maxSaveCount;
  }
  
  public int getUsedFileCount() {
    return fileManager.getUsedFileCount();
  }
  
  public int getInitFileCount() {
    return fileManager.getInitFileCount();
  }
  
  public int getAvailableFileCount() {
    return fileManager.getAvailableFileCount();
  }

  public ThreadGroup getThreadGroup() {
    return threadGroup;
  }

  public int getCompactDelay() {
    return compactDelay;
  }

  public void setCompactDelay(int compactDelay) {
    this.compactDelay = compactDelay;
  }

  public void create(Encodable objectId, Encodable value) throws Exception {
    Record record = ValueRecord.newCreateRecord(objectId, value, null);
    transactionContext.get().records.put(record.getObjectId(), record);
  }
  
  public void create(Encodable objectId, byte[] encodedValue) throws Exception{
    Record record = ValueRecord.newCreateRecord(objectId, null, encodedValue);
    transactionContext.get().records.put(record.getObjectId(), record);
  }
  
  public void save(Encodable objectId, Encodable value) throws Exception {
    Record record = ValueRecord.newSaveRecord(objectId, value, null);
    transactionContext.get().records.put(record.getObjectId(), record);
  }
  
  public void save(Encodable objectId, byte[] encodedValue) throws Exception {
    Record record = ValueRecord.newSaveRecord(objectId, null, encodedValue);
    transactionContext.get().records.put(record.getObjectId(), record);
  }
    
  public void delete(Encodable objectId) {
    Record record = new DeleteRecord(objectId);
    transactionContext.get().records.put(record.getObjectId(), record);
  }
  
  private static Encodable load(Encodable objectId, Map<Encodable, Record> recordTable) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLog.load(" + objectId + ')');
    Record record = recordTable.get(objectId);
    if (record != null) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "record = " + record);
      if ((record.getRecordType() == Record.SAVE_RECORD) || (record.getRecordType() == Record.CREATE_RECORD)) {
        return RecordEncodingHelper.resolveValue((ValueRecord) record);
      } else {
        // The object was deleted or removed from the log.
        return null;
      }
    } else {
      return null;
    }
  }
  
  public Encodable load(Encodable objectId) throws Exception {
    try {
      Map<Encodable, Record> currentRecords = transactionContext.get().records;
      Encodable value = load(objectId, currentRecords);
      if (value != null)
        return value;

      loadLock.lock();
      try {
        value = load(objectId, records);
        if (value != null) {
          return value;
        }
      } finally {
        loadLock.unlock();
      }

      byte[] encodedValue = repository.load(null, objectId.toString());
      return RecordEncodingHelper.decodeValue(encodedValue);
    } catch (FileNotFoundException exc) {
      return null;
    }
  }
  
  private byte[] loadEncodedValue(Encodable objectId,
      Map<Encodable, Record> recordTable) throws Exception {
    Record record = recordTable.get(objectId);
    if (record != null) {
      if ((record.getRecordType() == Record.SAVE_RECORD)
          || (record.getRecordType() == Record.CREATE_RECORD)) {
        return RecordEncodingHelper.resolveEncodedValue((ValueRecord) record);
      } else {
        // The object was deleted.
        return null;
      }
    } else {
      return null;
    }
  }
  
  public byte[] loadEncodedValue(Encodable objectId) throws Exception {
    try {
      Map<Encodable, Record> currentRecords = transactionContext.get().records;
      byte[] encodedValue = loadEncodedValue(objectId, currentRecords);
      if (encodedValue != null)
        return encodedValue;

      encodedValue = loadEncodedValue(objectId, records);
      if (encodedValue != null)
        return encodedValue;

      encodedValue = repository.load(null, objectId.toString());
      return encodedValue;
    } catch (FileNotFoundException exc) {
      return null;
    }
  }
  
  public boolean recordsToCommit() {
    Map<Encodable, Record> recordsToCommit = transactionContext.get().records;
    return (recordsToCommit.size() > 0);
  }
  
  /**
   * Enables to pre-encode the objects before the transaction takes the lock.
   * This operation will be removed as soon as the transaction model will
   * be made more efficient.
   */
  public void initValueRecords() throws Exception {
    Map<Encodable, Record> recordsToCommit = transactionContext.get().records;
    if (recordsToCommit.size() == 0) return;
    Set<Entry<Encodable, Record>> entries = recordsToCommit.entrySet();
    Iterator<Entry<Encodable, Record>> iterator = entries.iterator();
    while (iterator.hasNext()) {
      Record record = iterator.next().getValue();
      if (record instanceof ValueRecord) {
        ValueRecord valueRecord = (ValueRecord) record;
        valueRecord.init();
      }
    }
  }
  
  public void commit() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLog.commit()");
    
    Map<Encodable, Record> recordsToCommit = transactionContext.get().records;
    if (recordsToCommit.size() == 0) return;
    
    Set<Entry<Encodable, Record>> entries = recordsToCommit.entrySet();
    
    Iterator<Entry<Encodable, Record>> iterator = entries.iterator();
    int encodedSize = 4; // Number of records in the transaction
    while (iterator.hasNext()) {
      Record record = iterator.next().getValue();
      int recordEncodedSize = RecordEncodingHelper.getEncodedSize(record);
      
      if (recordEncodedSize > largestRecordEncodedSize) {
        largestRecordEncodedSize = recordEncodedSize;
      }
      
      encodedSize += RecordEncodingHelper.getEncodedSize(record);
    }
    encodedSize += 1; // COMMIT tag
    
    ByteBuffer buffer = ByteBuffer.allocate(encodedSize);
    Encoder encoder = new ByteBufferEncoder(buffer);
    
    // Number of records in the transaction (exactly 4 bytes)
    encoder.encode32(entries.size());
    
    commitLock.lock();
    try {
      doCommit(entries, encodedSize, buffer, encoder);
    } finally {
      commitLock.unlock();
    }
    
    iterator = entries.iterator();
    while (iterator.hasNext()) {
      Record record = iterator.next().getValue();
      if (record instanceof ValueRecord) {
        ValueRecord createRecord = (ValueRecord) record;
        // Drop the value
        // This has to be done after the value has been written
        // TODO: a callback should be used if asynchronous
        createRecord.setValue(null);
      }
    }
    recordsToCommit.clear();
  }
     
  private void doCommit(Set<Entry<Encodable, Record>> entries, 
      int encodedSize, ByteBuffer buffer, Encoder encoder) throws Exception {
    checkCurrentFile(encodedSize);
    
    Iterator<Entry<Encodable, Record>> iterator = entries.iterator();
    while (iterator.hasNext()) {
      Record record = iterator.next().getValue();
      
      RecordEncodingHelper.encodeRecord(record, encoder, buffer, currentFile);
      
      if (compacting) {
        if (record.getRecordType() == Record.CREATE_RECORD) {
          putCreateRecord((ValueRecord) record);
        } else {
          Record oldRecord = records.get(record.getObjectId());
          if (oldRecord == null) {
            // Means that there is no old record being compacted
            updateRecord(record);
          } else {
            if (oldRecord.getRecordType() != Record.DELETE_RECORD) {
              // This is a value record
              ValueRecord oldValueRecord = (ValueRecord) oldRecord;
              if (compactingContext.isFileToCompact(oldValueRecord.getFile())) {
                compactingContext.addWrittenWhileCompacting(record);
              } else {
                // the old file is not compacted: update is allowed
                updateRecord(record);
              }
            } else {
              updateRecord(record);
            }
          }
        }
      } else {
        if (record.getRecordType() == Record.CREATE_RECORD) {
          putCreateRecord((ValueRecord) record);
        } else {
          updateRecord(record);
        }
      }
    }
    
    // Commit tag
    buffer.put(COMMIT_TAG);
    
    if (buffer.position() != encodedSize) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "Actual encoded size: " + buffer.position() + " != " + encodedSize);
    }

    // Synchronous write
    // TODO: asynchronous write
    buffer.flip();
    currentFile.write(buffer, syncOnWrite);
  }
  
  private void putCreateRecord(ValueRecord record) throws Exception {
    Record old = records.put(record.getObjectId(), record);
    
    if (old != null) {
      // A 'create' should be the first operation done on an object otherwise
      // the compacting can't be parallel
      throw new Exception("Forbidden to create an existing object: " + old);
    }
    
    createRecordCount++;
    record.addToFile();
  }
  
  private void checkCurrentFile(int size) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLog.checkCurrentFile(" + size + ')');
    if (! currentFile.remains(size)) {
      useNewFile();
      checkCompacting();
    }
  }
  
  public void compact(TxLogFile[] usedFiles, int usedFileCount) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLog.compact()");
    
    commitLock.lock();
    
    List<TxLogFile> filesToCompact;
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "records.size() = " + records.size());
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "records = " + records);
    
    try {

      if (usedFileCount > 1) {
        // Remove the most recent used files (we assume it's not empty enough)
        int lastIndex = usedFiles.length - 1;
        usedFiles[lastIndex] = null;
      }
      
      if (! synchronousCompact) {
        // Check again
        usedFileCount = checkEmptyUsedFiles(usedFiles);
        if (usedFileCount == 0) return;
      }
      
      filesToCompact = new ArrayList<TxLogFile>(usedFileCount);
      for (int i = 0; i < usedFiles.length; i++) {
        TxLogFile usedFile = usedFiles[i];
        if (usedFile != null) {
          filesToCompact.add(usedFile);
          fileManager.removeUsedFile(usedFile);
        }
      }

      fileToCompactCount = filesToCompact.size();

      // filesToCompact are ordered from the oldest to the newest
      // the first logid is the smallest
      compactingContext = new CompactingContext(this, fileManager,
          filesToCompact);
      compacting = true;
      
    } finally {
      commitLock.unlock();
    }
    
    compactCount++;
    
    int liveCount = 0;
    for (TxLogFile lgf : filesToCompact) {
      if (logListener != null) {
        logListener.onFileToCompact(lgf);
      }
      liveCount += lgf.getLiveRecordsCount();
    }
    liveRecordCount = liveCount;

    long start = System.currentTimeMillis();

    // Counts the number of 'save' done in the repository
    int saveCount = 0;

    // It is better to compact the records in the file order (from the oldest
    // to the newest)
    for (TxLogFile lgf : filesToCompact) {
      lgf.open();

      ValueRecord[] fileRecords = lgf.getLiveRecords();

      for (int i = 0; i < fileRecords.length; i++) {
        Record record = fileRecords[i];

        switch (record.getRecordType()) {
        case Record.CREATE_RECORD:
        case Record.SAVE_RECORD:
          ValueRecord valueRecord = (ValueRecord) record;

          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, "Before compacting: " + valueRecord);

          if (valueRecord.getByteArray() == null) {
            // Don't need to decode the value
            // Just load the byte array
            valueRecord.getFile().loadEncodedValue(valueRecord, false);
          }

          // Check if the repository can be updated
          if (USE_REPOSITORY && saveCount < maxSaveCount
              && valueRecord.getCompactCount() > compactCountThreshold) {
            repository.save(null, valueRecord.getObjectId().toString(),
                valueRecord.getByteArray());
            records.remove(valueRecord.getObjectId());
            saveCount++;
            valueRecord.removeFromFile();
          } else {
            valueRecord.incCompactCount();

            // It is like a remove and a re-add
            valueRecord.removeFromFile();

            compactingContext.write(valueRecord);
          }

          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, "After compacting: " + valueRecord);

          // Drop the content to avoid out of memory
          valueRecord.setByteArray(null);

          break;
        case Record.DELETE_RECORD:
          throw new Exception("Unexpected delete record: " + record);
        default:
          throw new Exception("Unexpected record type: " + record);
        }
      }

      lgf.close();
    }

    compactingContext.flush();
    
    List<TxLogFile> compactedFiles = compactingContext.getCompactedFiles();
    
    fileManager.prepareCompacting(filesToCompact, compactedFiles);
    
    compactDuration = System.currentTimeMillis() - start;
    
    commitLock.lock();
    try {
      compacting = false;

      fileManager.addCompactedFiles(compactedFiles);
      
      // Now the save/delete records written during compacting can be added
      // to the records
      List<Record> writtenRecords = compactingContext.getWrittenWhileCompacting();
      for (Record writtenRecord : writtenRecords) {
        updateRecord(writtenRecord);
      }
      writtenWhileCompactingCount = writtenRecords.size();
      writtenRecords.clear();
      
    } finally {
      commitLock.unlock();
    }
    
    for (TxLogFile fileToCompact : filesToCompact) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "Recycle compacted file: " + fileToCompact);
      fileToCompact.close();
      
      // Before recycling we need to clear the list of the deleting files
      // and decrement their counters
      fileToCompact.clearDeletingFiles();
      
      fileManager.addAvailableFile(fileToCompact);
    }
    
    CountDownLatch latch = new CountDownLatch(2);
    
    fileManager.renameFiles(compactedFiles, latch);
    
    fileManager.commitCompacting(latch);
    
    latch.await();
    
    if (logListener != null) {
      logListener.onCompactDone(compactedFiles);
    }
  }
  
  private void updateRecord(Record updateRecord) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLog.updateRecord(" + updateRecord + ')');
    
    Record oldRecord;
    switch (updateRecord.getRecordType()) {
      case Record.SAVE_RECORD:
        oldRecord = records.put(updateRecord.getObjectId(), updateRecord);
        if (oldRecord != null && oldRecord.getRecordType() == Record.CREATE_RECORD) {
          // The object has never been created on disk, the resulting operation
          // is still a creation
          updateRecord.setRecordType(Record.CREATE_RECORD);
        }
        break;
      case Record.DELETE_RECORD:
        // This case should be the most efficient: the deleted object is in the log
        // so we remove it
        deleteRecordCount++;
        oldRecord = (ValueRecord) records.remove(updateRecord.getObjectId());   
        if (USE_REPOSITORY) {
          if (oldRecord == null || oldRecord.getRecordType() == Record.SAVE_RECORD) {
            // This case should happen less frequently: need to keep the delete record
            // as the object is (or may be) in the repository
            
            // TODO: check if the file exists
            repository.delete(null, updateRecord.getObjectId().toString());
          }
        }
        
        if (oldRecord != null
            && updateRecord.getFile() != oldRecord.getFile()) {
          oldRecord.getFile().addDeletingFile(updateRecord.getFile());
        }
        break;
      default:
        throw new Exception("Unexpected record type: " + updateRecord.getRecordType());
    }
    
    if (oldRecord != null && oldRecord.getRecordType() != Record.DELETE_RECORD) {
      // 1- remove the old record
      ((ValueRecord) oldRecord).removeFromFile();
    }
    
    if (updateRecord.getRecordType() != Record.DELETE_RECORD) {
      // 2- add the new record
      ((ValueRecord) updateRecord).addToFile();
    }
  }
  
  private void useNewFile() throws Exception {
    fileManager.close(currentFile);
    setCurrentFile(fileManager.getLogFile());
  }
  
  public void init(TxLogFile file) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLog.init(" + file + ')');
    
    long lastConsistentPosition = 0;
    
    file.open();
    try {
      int fileSize = (int) file.size();
      ByteBuffer buf = ByteBuffer.allocate(fileSize);
      file.read(buf);
      buf.flip();
      Decoder decoder = new ByteBufferDecoder(buf);
      
      long decodedLogId = decoder.decodeUnsignedLong();
      
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "decodedLogId: " + decodedLogId);
      
      lastConsistentPosition = buf.position();
      
      List<Record> committedRecords = new ArrayList<Record>();

      loop: while (buf.hasRemaining()) {
        lastConsistentPosition = buf.position();
            
        // Integer on exactly 4 bytes
        int recordCount = decoder.decode32();
        
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, "recordCount: " + recordCount);
        
        if (recordCount == 0) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, "No more records");
          break loop;
        }
        
        for (int i = 0; i < recordCount; i++) {
          Record record = RecordEncodingHelper.decodeRecord(decoder, buf, file);
          committedRecords.add(record);
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, "Decoded: " + record);
        }

        // Check the file consistency
        byte commitTag = decoder.decodeByte();
        if (commitTag != COMMIT_TAG) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, "Unexpected commit");
          throw new Exception("Unexpected commit tag: " + commitTag);
        } else {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, "Commit");
        }
        
        for (Record record : committedRecords) {
          switch (record.getRecordType()) {
          case Record.CREATE_RECORD:
            // Fix a potential bug if there is a crash during a garbage.
            // A newly created object can be saved in repository then not deleted.
            // Check if the object exists in the repository
            if (repositoryCache.get(record.getObjectId().toString()) != null) {
              // This is actually a save
              record.setRecordType(Record.SAVE_RECORD);
            }

            Record old = records.put(record.getObjectId(), record);
            ValueRecord valueRecord = (ValueRecord) record;
            valueRecord.addToFile();
            if (old != null) {
              throw new Exception("Forbidden to create an existing object: " + old);
            }
            break;
          case Record.SAVE_RECORD:
            old = records.put(record.getObjectId(), record);
            valueRecord = (ValueRecord) record;
            valueRecord.addToFile();
            if (old != null) {
              if (old instanceof ValueRecord) {
                ValueRecord oldValueRecord = (ValueRecord) old;
                oldValueRecord.removeFromFile();
              }
              // else nothing to do
            }
            break;
          case Record.DELETE_RECORD:
            old = records.remove(record.getObjectId());

            if (old == null || old.getRecordType() == Record.SAVE_RECORD) {
              // TODO: should keep the string representation in cache
              String recordName = record.getObjectId().toString();
              if (repositoryCache.get(recordName) != null) {
                // Need to update the repository
                repository.delete(null, recordName);
              }
            }
            
            if (old != null) {
              if (old instanceof ValueRecord) {
                ValueRecord oldValueRecord = (ValueRecord) old;
                oldValueRecord.removeFromFile();
              }
              // else nothing to do
            }
            break;
          default:
            throw new Exception("Unexpected record type: "
                + record.getRecordType());
          }
          
        }
        
        committedRecords.clear();
      }
    } finally {
      // Sets the last consistent position
      file.setCurrentFilePointer(lastConsistentPosition);
      
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "Init file: " + file);
      
      file.close();
    }
  }
  
  public void checkCompacting() throws Exception {
    if (synchronousCompact) {
      doCompactingTask();
    } else {
      if (compactingTaskRunning.compareAndSet(false, true)) {
        compactingExecutor.execute(compactingTask);
      }
    }
  }
  
  public String[] getList(String prefix) {
    String[] list1 = null;
    try {
      list1 = repository.list(prefix);
    } catch (IOException exc) {
      // AF: TODO
    }
    if (list1 == null) list1 = new String[0];
    Object[] list2 = records.keySet().toArray();
    int nb = list1.length;
    for (int i=0; i<list2.length; i++) {
      String key = list2[i].toString();
      if (key.startsWith(prefix)) {
        int j=0;
        for (; j<list1.length; j++) {
          if (key.equals(list1[j])) break;
        }
        if (j<list1.length) {
          // The file is already in the directory list, it must be count
          // at most once.
          if ((records.get(list2[i])).getRecordType() == Record.DELETE_RECORD) {
            // The file is deleted in transaction log.
            list1[j] = null;
            nb -= 1;
          }
          list2[i] = null;
        } else if ((records.get(list2[i]).getRecordType() == Record.SAVE_RECORD) ||
            (records.get(list2[i]).getRecordType() == Record.CREATE_RECORD)) {
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
      if (list2[i] != null) list[--nb] = (String) list2[i].toString(); // TODO: toString already done above
    }

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "getList() -> " + Arrays.toString(list));

    return list;
  }
  
  private static class TransactionContext {
    private Map<Encodable, Record> records = new HashMap<Encodable, Record>();
  }
  
  private int checkEmptyUsedFiles(TxLogFile[] usedFiles) throws Exception {
    int usedFileCount = 0;
    for (int i = 0; i < usedFiles.length; i++) {
      TxLogFile file = usedFiles[i];
      if (file != null) {
        if (file.getLiveRecordsCount() == 0 && file.getDeletedFileCount() == 0) {
          recycledEmptyFileCount++;
          fileManager.removeUsedFile(file);
          usedFiles[i] = null;

          file.close();

          // Before recycling we need to clear the list of the deleting files
          // and decrement their counters
          file.clearDeletingFiles();

          fileManager.addAvailableFile(file);
        } else {
          usedFileCount++;
        }
      }
    }
    return usedFileCount;
  }
  
  public void doCompactingTask() throws Exception {
    TxLogFile[] usedFiles = fileManager.getUsedFiles();
    
    int usedFileCount = usedFiles.length;
    if (usedFileCount == 0) return;
    
    usedFileCount = checkEmptyUsedFiles(usedFiles);
    
    if (usedFileCount == 0) {
      return;
    }
    
    long now = System.currentTimeMillis();
    if ((now - lastCompactDate > compactDelay) ||
        usedFileCount > minCompactFileCount) {
      long totalLiveSize = 0;
      for (TxLogFile file : usedFiles) {
        if (file != null) {
          totalLiveSize += file.getLiveRecordsEncodedSize();
        }
      }
      usedFileLiveSize = totalLiveSize;

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "totalLiveSize=" + totalLiveSize);

      long usedFileBytes = usedFileCount * (long) fileSize;
      usedFileCompactThreshold = (long) (usedFileBytes * compactRatio) / 100;

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "usedFileCompactThreshold="
            + usedFileCompactThreshold);

      if (totalLiveSize < usedFileCompactThreshold) {
        loadLock.lock();
        try {
          lastCompactDate = now;
          compact(usedFiles, usedFileCount);
        } finally {
          loadLock.unlock();
        }
      }
    }
  }
  
  public void close() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLog.close()");
    compactingExecutor.shutdown();
    compactingExecutor.awaitTermination(10, TimeUnit.SECONDS);
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "Compacting executor stopped.");
    
    fileManager.close();
    
    if (currentFile != null) {
      currentFile.close();
    }
  }
  
  @Override
  public String toString() {
    return "TxLog [currentFile=" + currentFile + ", fileManager=" + fileManager
        + ", records=" + records + ", compactingContext=" + compactingContext
        + ", compacting=" + compacting + ", fileSize=" + fileSize
        + ", minFileCount=" + minFileCount + ", compactRatio=" + compactRatio
        + ", compactMinFiles=" + minCompactFileCount + ", syncOnWrite="
        + syncOnWrite + ", compactCountThreshold=" + compactCountThreshold
        + ", recycledEmptyFileCount=" + recycledEmptyFileCount
        + ", maxFileCount=" + maxFileCount + ", writtenWhileCompactingCount="
        + writtenWhileCompactingCount + ", compactCount=" + compactCount
        + ", compactDuration=" + compactDuration + ", fileToCompactCount="
        + fileToCompactCount + ", compactingTaskRunning="
        + compactingTaskRunning + ", createRecordCount=" + createRecordCount
        + ", deleteRecordCount=" + deleteRecordCount
        + ", usedFileCompactThreshold=" + usedFileCompactThreshold
        + ", usedFileLiveSize=" + usedFileLiveSize + ", synchronousCompact="
        + synchronousCompact + ", liveRecordCount=" + liveRecordCount + "]";
  }

  private class CompactingTask implements Runnable {

    public void run() {
      try {
        doCompactingTask();
      } catch (Throwable error) {
        if (logmon.isLoggable(BasicLevel.FATAL))
          logmon.log(BasicLevel.FATAL, "TxLog fatal error: " + this, error);
        System.exit(1);
      } finally {
        compactingTaskRunning.set(false);
      }
    }
    
  }
  
}
