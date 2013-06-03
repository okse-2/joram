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
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.ByteBufferDecoder;
import fr.dyade.aaa.common.encoding.ByteBufferEncoder;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encoder;

public class TxLogFileManager {
  
  public static final Logger logmon = Debug.getLogger(TxLogFileManager.class.getName());
  
  public static final String LOG_FILE_PREFIX = "log";
  
  public static final String TMP_SUFFIX = ".tmp";
  
  public static final String COMPACTING_FILE_NAME = "compacting";
  
  /**
   * Ordered according to the logId
   */
  private List<TxLogFile> usedFiles;
  
  /**
   * Ordered according to the logId
   */
  private ConcurrentLinkedQueue<TxLogFile> availableFiles;
  
  /**
   * Ordered according to the logId
   */
  private LinkedBlockingQueue<TxLogFile> initFiles;
  
  private File logDir;
  
  /***
   * Serializes the operations on files.
   * Ensures the logId ordering.
   */
  private ExecutorService fileExecutor;
  
  private FileRecyclingTask fileRecyclingTask;
  
  private FileCreatingTask fileCreatingTask;
  
  private long maxLogId;

  private long maxFileId;
  
  private File compactingFile;
  
  private TxLog txlog;
  
  private int recycledCompactedFileCount;
  
  private int deletedFileCount;
  
  private int newFileCount;
  
  private AtomicInteger fileCount;
  
  public TxLogFileManager(TxLog txlog) {
    this.txlog = txlog;
    
    maxFileId = -1;
    maxLogId = -1;
    
    fileCount = new AtomicInteger();
    
    // Vector: concurrently accessed 
    usedFiles = new Vector<TxLogFile>();
    
    availableFiles = new ConcurrentLinkedQueue<TxLogFile>();
    initFiles = new LinkedBlockingQueue<TxLogFile>();
    
    fileRecyclingTask = new FileRecyclingTask();
    fileCreatingTask = new FileCreatingTask();
    
    fileExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {

      public Thread newThread(Runnable runnable) {
        Thread newThread = new Thread(TxLogFileManager.this.txlog
            .getThreadGroup(), runnable, "TxLogFileManager.Creating");
        return newThread;
      }
    });
    
  }
  
  private TxLogFile newFile(File file) {
    if (txlog.isUseNioFileChannel()) {
      return new NioTxLogFile(file, txlog.isSyncOnWrite());
    } else {
      return new OioTxLogFile(file, txlog.isSyncOnWrite());
    }
  }
  
  public int getUsedFileCount() {
    return usedFiles.size();
  }
  
  public int getInitFileCount() {
    return initFiles.size();
  }
  
  public int getAvailableFileCount() {
    return availableFiles.size();
  }
  
  public int getRecycledCompactedFileCount() {
    return recycledCompactedFileCount;
  }

  public int getDeletedFileCount() {
    return deletedFileCount;
  }

  public int getNewFileCount() {
    return newFileCount;
  }

  public TxLogFile[] getUsedFiles() {
    TxLogFile[] res;
    synchronized (usedFiles) {
      res = new TxLogFile[usedFiles.size()];
      res = usedFiles.toArray(res);
    }
    return res;
  }
  
  public void removeUsedFile(TxLogFile file) {
    usedFiles.remove(file);
  }
  
  public void init() throws Exception { 
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "TxLogFileManager.init()");
    
    logDir = txlog.getLogDirectory();
    
    checkInterruptedCompacting();
    
    String[] fileNames = logDir.list();
    
    if (fileNames.length == 0) {
      // Means that this is the first start
      // TODO: should also check if the first start has been interrupted
      for (int i = 0; i < txlog.getMinFileCount(); i++) {
        createFile();
      }
      return;
    }
    
    List<TxLogFile> orderedFiles = new ArrayList<TxLogFile>(fileNames.length);
    
    for (String fileName : fileNames) {
      File file = new File(logDir, fileName);
      TxLogFile txlogFile = newFile(file);
      txlogFile.open();
      fileCount.incrementAndGet();
      
      try {
        long logId = txlogFile.readLong();
        txlogFile.setLogId(logId);
      } finally {
        txlogFile.close();
      }
      
      if (logmon.isLoggable(BasicLevel.INFO))
        logmon.log(BasicLevel.INFO, "open: " + txlogFile);
      
      int fileNumber = parseFileNumber(fileName);
      if (fileNumber > maxFileId) {
        maxFileId = fileNumber;
      }
      
      orderedFiles.add(txlogFile);
    }
    Collections.sort(orderedFiles, new JournalFileComparator());
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "maxFileId = " + maxFileId);
    
    maxLogId = orderedFiles.get(orderedFiles.size() - 1).getLogId();
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "maxLogId=" + maxLogId);
    
    for (TxLogFile file : orderedFiles) {
      try {
        txlog.init(file);
      } catch (Exception exc) {
        if (logmon.isLoggable(BasicLevel.INFO))
          logmon.log(BasicLevel.INFO, "", exc);
      }
      if (file.isUsed()) {
        usedFiles.add(file);
      } else {
        initFiles.offer(file);
      }
    }
  }
  
  private static int parseFileNumber(String fileName) {
    String fileNumber = fileName.substring(3);
    return Integer.parseInt(fileNumber);
  }
  
  public TxLogFile getLogFile() throws Exception {
    return getLogFile(true);
  }
  
  public TxLogFile getLogFile(boolean waitIfMax) throws Exception {
    TxLogFile initFile = initFiles.poll();
    while (initFile == null) {
      if ((! waitIfMax) ||
          availableFiles.size() + usedFiles.size() < txlog.getMaxFileCount()) {
        fileCreatingTask.execute();
      } else {
        txlog.checkCompacting();
      }
      initFile = initFiles.poll(100, TimeUnit.MILLISECONDS);
    }
    return initFile;
  }
  
  public void close(TxLogFile file) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLogFileManager.close(" + file + ')');
    file.close();
    usedFiles.add(file);
  }

  /**
   * Compacted files are older so they have to be inserted at
   * the beginning of the list.
   * @param files
   */
  public void addCompactedFiles(List<TxLogFile> files) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLogFileManager.addCompactedFiles(" + files + ')');
    usedFiles.addAll(0, files);
  }
  
  public void addAvailableFile(TxLogFile file) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLogFileManager.addAvailableFile(" + file + ')');
    if (txlog.getLogListener() != null) {
      txlog.getLogListener().onAvailableFile(file);
    }
    if (fileCount.get() > txlog.getMinFileCount()) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "Delete file: " + file);
      boolean deleted = file.getFile().delete();
      
      if (! deleted) {
        if (logmon.isLoggable(BasicLevel.FATAL))
          logmon.log(BasicLevel.FATAL, "Failed to delete file: " + file);
        System.exit(1);
      }
      
      deletedFileCount++;
      fileCount.decrementAndGet();
    } else {
      availableFiles.offer(file);
      fileRecyclingTask.execute();
      recycledCompactedFileCount++;
    }
  }
  
  private static int getFileNameEncodedSize(List<TxLogFile> files) {
    int size = 0;
    for (TxLogFile file : files) {
      size += 4;
      size += file.getFile().getName().length();
    }
    return size;
  }
  
  private static void putFileNames(List<TxLogFile> files, Encoder encoder) throws Exception {
    encoder.encodeUnsignedInt(files.size());
    for (TxLogFile file : files) {
      encoder.encodeString(file.getFile().getName());
    }
  }
  
  private static String[] readFileNames(Decoder decoder) throws Exception {
    int size = decoder.decodeUnsignedInt();
    String[] res = new String[size];
    for (int i = 0; i < size; i++) {
      res[i] = decoder.decodeString();
    }
    return res;
  }
  
  public void prepareCompacting(List<TxLogFile> filesToCompact, List<TxLogFile> compactedFiles) throws Exception {
    compactingFile = new File(logDir, COMPACTING_FILE_NAME);
    RandomAccessFile raf = new RandomAccessFile(compactingFile, "rw");
    FileChannel channel = raf.getChannel();
    int encodedSize = 8 + getFileNameEncodedSize(filesToCompact) + getFileNameEncodedSize(compactedFiles);
    ByteBuffer buf = ByteBuffer.allocate(encodedSize);
    Encoder encoder = new ByteBufferEncoder(buf);
    
    putFileNames(filesToCompact, encoder);
    putFileNames(compactedFiles, encoder);
    
    // We have to write in a synchronous way
    channel.write(buf);
    if (txlog.isSyncOnWrite()) {
      channel.force(true);
    }
    raf.close();
  }
  
  public void commitCompacting(final CountDownLatch latch) throws Exception {
    fileExecutor.execute(new Runnable() {
      
      public void run() {
        try {
          compactingFile.delete();
          latch.countDown();
        } catch (Throwable error) {
          if (logmon.isLoggable(BasicLevel.FATAL))
            logmon.log(BasicLevel.FATAL, "Failed to delete file: " + compactingFile, error);
          System.exit(1);
        }
      }
    });
  }
  
  private void deleteTmpFiles () throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLogFileManager.deleteTmpFiles()");
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(final File file, final String name) {
        return name.endsWith(TMP_SUFFIX);
      }
    };
    String[] tmpFileNames = logDir.list(filter);
    for (String tmpFileName : tmpFileNames) {
      File tmpFile = new File(tmpFileName);
      if (tmpFile.exists()) {
        boolean deleted = tmpFile.delete();
        if (! deleted) {
          if (logmon.isLoggable(BasicLevel.FATAL))
            logmon.log(BasicLevel.FATAL, "Could not delete: " + tmpFile.getName());
          System.exit(1);
        }
      }
    }
  }
  
  private void checkInterruptedCompacting() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLogFileManager.checkInterruptedCompacting()");
    File interruptedCompactingFile = new File(logDir, COMPACTING_FILE_NAME);
    if (interruptedCompactingFile.exists()) {
      RandomAccessFile raf = new RandomAccessFile(interruptedCompactingFile,
          "rw");
      ByteBuffer buf = ByteBuffer.allocate((int) raf.length());
      Decoder decoder = new ByteBufferDecoder(buf);

      String[] filesToCompact = readFileNames(decoder);
      String[] compactedFiles = readFileNames(decoder);

      for (String fileToCompact : filesToCompact) {
        File file = new File(logDir, fileToCompact);
        if (file.exists()) {
          file.delete();
        }
      }

      for (String compactedFile : compactedFiles) {
        File file = new File(logDir, compactedFile);
        if (file.exists()) {
          File dest = getRenamedFile(file);
          file.renameTo(dest);
        }
      }

      deleteTmpFiles();
    } else {
      // Delete remaining tmp files (means
      // that a concurrent compacting was running
      // and was interrupted before the compacted files
      // were deleted)
      deleteTmpFiles();
    }
  }
  
  private File getRenamedFile(File file) {
    String newName = file.getName();
    int suffixIndex = newName.indexOf('.');
    if (suffixIndex < 0) {
      logmon.log(BasicLevel.ERROR, "Log file name not correct: " + file.getName());
      return null;
    } else {
      newName = newName.substring(0, suffixIndex);
      File dest = new File(logDir, newName);
      return dest;
    }
  }
  
  private void renameCompactedFile(TxLogFile file) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLogFileManager.renameCompactedFile(" + file + ')');
    File dest = getRenamedFile(file.getFile());
    if (dest != null) {
      if (dest.exists()) {
        logmon.log(BasicLevel.FATAL, "Log file already exists: " + dest);
        System.exit(1);
      } else {
        boolean renamed = file.renameTo(dest);
        if (! renamed) {
          logmon.log(BasicLevel.FATAL, "Log file not renamed: " + file);
          System.exit(1);
        }
      }
    }
  }
  
  public void renameFiles(final List<TxLogFile> filesToRename,
      final CountDownLatch latch) throws Exception {
    fileExecutor.execute(new Runnable() {
      
      public void run() {
        try {
          for (TxLogFile fileToRename : filesToRename) {
            renameCompactedFile(fileToRename);
          }
          latch.countDown();
        } catch (Throwable error) {
          if (logmon.isLoggable(BasicLevel.FATAL))
            logmon.log(BasicLevel.FATAL, "Failed to rename files: " + filesToRename, error);
          System.exit(1);
        }
      }
    });
  }
  
  public TxLogFile getLastUsedFile() {
    if (usedFiles.size() == 0) {
      return null;
    } else {
      return usedFiles.remove(usedFiles.size() - 1);
    }
  }
  
  private void resetFile(TxLogFile file, boolean fill) throws Exception {
    maxLogId += 1;
    file.setLogId(maxLogId);
    file.open();
    file.reset(txlog.getFileSize(), fill);
    file.close();
  }
  
  public void close() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "TxLogFileManager.close()");
    fileExecutor.shutdown();
    fileExecutor.awaitTermination(10, TimeUnit.SECONDS);
    
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "File executor stopped.");
    
    for (TxLogFile tlf : usedFiles) {
      tlf.close();
    }
    
    for (TxLogFile tlf : availableFiles) {
      tlf.close();
    }
    
    for (TxLogFile tlf : initFiles) {
      tlf.close();
    }
  }
  
  @Override
  public String toString() {
    return "TxLogFileManager [usedFiles=" + usedFiles + ", availableFiles="
        + availableFiles + ", initFiles=" + initFiles + ", maxLogId="
        + maxLogId + ", maxFileId=" + maxFileId
        + ", recycledCompactedFileCount=" + recycledCompactedFileCount
        + ", deletedFileCount=" + deletedFileCount + ", newFileCount="
        + newFileCount + "]";
  }

  private class FileRecyclingTask implements Runnable {
    
    private boolean running;

    public void run() {
      try {
        while (true) {
          TxLogFile file = availableFiles.poll();
          if (file == null) {
            synchronized (availableFiles) {
              file = availableFiles.poll();
              if (file == null) {
                running = false;
                return;
              }
            }
          }
          
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG, "Recycle file: " + file);
          
          boolean fill;
          if (txlog.isSyncOnWrite()) {
            fill = true;
          } else {
            fill = false;
          }
          
          resetFile(file, fill);
          
          initFiles.offer(file);
        }
      } catch (Throwable error) {
        if (logmon.isLoggable(BasicLevel.FATAL))
          logmon.log(BasicLevel.FATAL, "", error);
        System.exit(1);
      }
    }
    
    public void execute() {
      synchronized (availableFiles) {
        if (! running) {
          running = true;
          fileExecutor.execute(this);
        }
      }
    }
    
  }
  
  private void createFile() throws Exception {
    maxFileId += 1;
    String fileName = LOG_FILE_PREFIX + maxFileId;
    File file = new File(logDir, fileName);
    TxLogFile logFile = newFile(file);
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "Create file: " + file);
    resetFile(logFile, true);
    
    newFileCount++;
    
    fileCount.incrementAndGet();
    
    initFiles.offer(logFile);
  }
  
  private class FileCreatingTask implements Runnable {

    public void run() {
      try {
        createFile();
      } catch (Exception exc) {
        if (logmon.isLoggable(BasicLevel.ERROR))
          logmon.log(BasicLevel.ERROR, "", exc);
      }
    }
    
    public void execute() {
      fileExecutor.execute(this);
    }
    
  }
  
  private static class JournalFileComparator implements Comparator<TxLogFile> {
    
    public int compare(TxLogFile f1, TxLogFile f2) {
      long id1 = f1.getLogId();
      long id2 = f2.getLogId();
      return id1 < id2 ? -1 : id1 == id2 ? 0 : 1;
    }
    
  }
  
}
