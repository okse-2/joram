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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.Encodable;

public class TxLogFile {
  
  public static final Logger logmon = Debug.getLogger(TxLogFileManager.class.getName());
  
  /**
   * Only contains the log id (8 bytes).
   */
  public static final int FILE_HEADER_LENGTH = 8;
  
  private File file;
  
  private RandomAccessFile raf;
  
  private FileChannel channel;
  
  private long logId;
  
  /**
   * Only counts the size of
   * the records. The other fields
   * are ignored (e.g. fileId, number of records
   * in a transaction).
   */
  private AtomicInteger liveRecordsEncodedSize;
  
  private AtomicInteger liveRecordsCount;
  
  /**
   * Number of files where a record (create or save)
   * is cancelled by a delete record written in this file.
   * These files are older than this file.
   */
  private AtomicInteger deletedFileCount;
  
  /**
   * Files where a delete record cancels
   * a record (create or save) written in this file.
   * This file is older than those files.
   */
  private HashMap<TxLogFile, TxLogFile> deletingFiles;
  
  private long currentFilePointer;
  
  private long fileSize;
  
  private HashMap<Encodable, ValueRecord> liveRecords;
  
  private ReentrantLock ioLock;
  
  public TxLogFile(File file) {
    this.file = file;
    currentFilePointer = 0;
    liveRecordsEncodedSize = new AtomicInteger();
    liveRecordsCount = new AtomicInteger();
    deletedFileCount = new AtomicInteger();
    deletingFiles = new HashMap<TxLogFile, TxLogFile>();
    liveRecords = new HashMap<Encodable, ValueRecord>();
    ioLock = new ReentrantLock();
  }
  
  public boolean isUsed() {
    return (currentFilePointer > FILE_HEADER_LENGTH);
  }
  
  public long getCurrentFilePointer() {
    return currentFilePointer;
  }

  public void setCurrentFilePointer(long currentFilePointer) {
    this.currentFilePointer = currentFilePointer;
  }

  public File getFile() {
    return file;
  }

  public long getLogId() {
    return logId;
  }

  public void setLogId(long logId) {
    this.logId = logId;
  }

  public void open() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, toString() + ".open()");
    if (channel != null) throw new Exception("File already open: " + this);
    doOpen();
  }
  
  private void doOpen() throws IOException {
    raf = new RandomAccessFile(file, "rw");
    channel = raf.getChannel();
    fileSize = channel.size();
  }
  
  public void setCurrentPosition() throws IOException {
    channel.position(currentFilePointer);
  }
  
  public void reset(int size, boolean force, boolean fill) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, toString() + ".reset(" + size + ')');
    ByteBuffer buf;
    if (fill) {
      buf = ByteBuffer.allocate(size);
      buf.putLong(logId);
      int remainingSize = size - buf.position();
      for (int i = 0; i < remainingSize; i++) {
        buf.put(TxLog.RESET_VALUE);
      }
    } else {
      buf = ByteBuffer.allocate(FILE_HEADER_LENGTH);
      buf.putLong(logId);
    }
    
    buf.flip();
    channel.write(buf);
    
    if (force) {
      channel.force(false);
    }
    
    fileSize = channel.size();
    
    currentFilePointer = FILE_HEADER_LENGTH;
    
    liveRecordsEncodedSize.set(0);
    liveRecordsCount.set(0);
    liveRecords.clear();
    
    deletedFileCount.set(0);
    deletingFiles.clear();
  }
  
  public void read(ByteBuffer buf) throws IOException {
    // No need to take the io lock
    channel.read(buf);
  }
  
  public long readLong() throws IOException {
    return raf.readLong();
  }
  
  public boolean remains(int size) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, toString() + ".remains(" + size + ')');
    return (currentFilePointer + size <= fileSize);
  }
  
  public long size() throws Exception {
    if (channel == null) {
      return getFile().length();
    } else {
      return channel.size();
    }
  }
  
  public void write(ByteBuffer buf, boolean sync) throws IOException {
    ioLock.lock();
    try {
      currentFilePointer += channel.write(buf);
      if (sync) {
        channel.force(false);
      }
    } finally {
      ioLock.unlock();
    }
  }
  
  public void loadEncodedValue(ValueRecord valueRecord, boolean resetPosition) throws Exception {
    ioLock.lock();
    try {
      boolean alreadyOpen;
      if (channel == null) {
        doOpen();
        alreadyOpen = false;
      } else {
        alreadyOpen = true;
      }
      channel.position(valueRecord.getFilePointer());
      int byteArraySize = valueRecord.getByteArraySize();
      ByteBuffer buf = ByteBuffer.allocate(byteArraySize);
      int read = 0;
      while (read < byteArraySize) {
        int res = channel.read(buf);
        if (res < 0)
          throw new Exception("Cannot read record: " + valueRecord);
        read += res;
      }
      valueRecord.setByteArray(buf.array());
      if (alreadyOpen) {
        if (resetPosition) {
          channel.position(currentFilePointer);
        }
      } else {
        close();
      }
    } finally {
      ioLock.unlock();
    }
  }
  
  public long getLiveRecordsEncodedSize() {
    return liveRecordsEncodedSize.get();
  }
  
  public void addRecord(ValueRecord record) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, toString() + ".addRecord(" + record + ')');
    liveRecordsEncodedSize.addAndGet(record.getByteArraySize());
    liveRecordsCount.incrementAndGet();
    liveRecords.put(record.getObjectId(), record);
  }
  
  public void removeRecord(ValueRecord record) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, toString() + ".removeRecord(" + record + ')');
    liveRecordsEncodedSize.addAndGet(- record.getByteArraySize());
    if (liveRecordsCount.decrementAndGet() < 0) throw new Exception("Negative live size");
    liveRecords.remove(record.getObjectId());
  }
  
  public ValueRecord[] getLiveRecords() {
    ValueRecord[] res = new ValueRecord[liveRecords.size()];
    Collection<ValueRecord> collection = liveRecords.values();
    return collection.toArray(res);
  }
  
  public void addDeletingFile(TxLogFile file) {
    // No need to synchronize
    if (deletingFiles.get(file) == null) {
      deletingFiles.put(file, file);
      file.incrementDeletedFileCount();
    }
  }
  
  /**
   * Called after a file has been compacted.
   */
  public void clearDeletingFiles() {
    Set<Entry<TxLogFile, TxLogFile>> entries = deletingFiles.entrySet();
    Iterator<Entry<TxLogFile, TxLogFile>> iterator = entries.iterator();
    while (iterator.hasNext()) {
      Entry<TxLogFile, TxLogFile> entry = iterator.next();
      entry.getValue().decrementDeletedFileCount();
    }
    deletingFiles.clear();
  }
  
  // Only called by another file
  private void incrementDeletedFileCount() {
    deletedFileCount.incrementAndGet();
  }
  
  //Only called by another file
  private void decrementDeletedFileCount() {
    deletedFileCount.decrementAndGet();
  }
  
  public int getDeletedFileCount() {
    return deletedFileCount.get();
  }
  
  public int getLiveRecordsCount() {
    return liveRecordsCount.get();
  }
  
  public boolean renameTo(File dest) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "Rename: " + file);
    boolean res = file.renameTo(dest);
    if (res) {
      file = dest;
    }
    return res;
  }
  
  public void close() throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, toString() + ".close()");
    if (channel != null) {
      channel.close();
      raf.close();
      raf = null;
      channel = null;
    }
  }

  @Override
  public String toString() {
    return "TxLogFile [file=" + file + ", logId=" + logId
        + ", deletedFileCount=" + deletedFileCount
        + ", deletingFileCount=" + deletingFiles.size()
        + ", liveRecordsEncodedSize=" + liveRecordsEncodedSize
        + ", liveRecordsCount=" + liveRecordsCount 
        + ", liveRecordsTableSize=" + liveRecords.size()
        + ", currentFilePointer="
        + currentFilePointer + ", fileSize=" + fileSize + "]";
  }

}
