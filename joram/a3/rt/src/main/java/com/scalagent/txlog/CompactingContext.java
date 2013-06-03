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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.ByteBufferEncoder;
import fr.dyade.aaa.common.encoding.Encoder;

public class CompactingContext {
  
  public static final Logger logmon = Debug.getLogger(CompactingContext.class.getName());
  
  private long nextLogId;
  
  private TxLogFile currentFile;
  
  private ByteBuffer currentBuffer;
  
  private Encoder currentEncoder;
  
  private int currentRecordCount;
  
  private List<TxLogFile> compactedFiles;
  
  private List<Record> writtenWhileCompacting;
  
  private TxLog txlog;
  
  private TxLogFileManager fileManager;
  
  private List<TxLogFile> filesToCompact;
  
  public CompactingContext(TxLog txlog, TxLogFileManager fileManager,
      List<TxLogFile> filesToCompact) {
    super();
    this.txlog = txlog;
    this.fileManager = fileManager;
    this.filesToCompact = filesToCompact;
    
    writtenWhileCompacting = new ArrayList<Record>();

    // Vector: accessed concurrently by compactor and committer (isFileToCompact)
    compactedFiles = new Vector<TxLogFile>();

    nextLogId = filesToCompact.get(0).getLogId();

    currentRecordCount = 0;
  }
  
  public int getWrittenWhileCompactingCount() {
    return writtenWhileCompacting.size();
  }
  
  public boolean isFileToCompact(TxLogFile file) {
    return (compactedFiles.contains(file) ||
        filesToCompact.contains(file));
  }
  
  public void flush() throws Exception {
    if (currentFile != null) {
      // synchronous write
      currentFile.open();
      currentFile.setCurrentPosition();
      
      // write the final commit tag at the end
      currentEncoder.encodeByte(TxLog.COMMIT_TAG);
      int finalPosition = currentBuffer.position();

      // write the number of records on 4 bytes
      // after the logid
      currentBuffer.position(8);
      currentEncoder.encode32(currentRecordCount);
      
      currentBuffer.position(finalPosition);
      currentBuffer.flip();
      currentFile.write(currentBuffer);
      
      currentFile.close();
      
    }
    currentBuffer = null;
    currentFile = null;
  }
  
  private void openFile() throws Exception {
    flush();

    currentBuffer = ByteBuffer.allocate(txlog.getFileSize());
    currentEncoder = new ByteBufferEncoder(currentBuffer);
    currentRecordCount = 0;

    currentFile = fileManager.getLogFile(false);
    
    currentFile.close();
    
    File dest = new File(txlog.getLogDirectory(), 
        currentFile.getFile().getName()
        + TxLogFileManager.TMP_SUFFIX);
    if (dest.exists()) {
      logmon.log(BasicLevel.FATAL, "Tmp file already exists: " + dest);
      System.exit(1);
    }
    
    boolean renamed = currentFile.renameTo(dest);
    while (! renamed) {
      logmon.log(BasicLevel.FATAL, "Log file not renamed: " + currentFile);
      System.exit(1);
    }
    
    // We need to update the log id
    currentFile.setLogId(nextLogId);
    currentFile.setCurrentFilePointer(0);

    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "CompactedFile: " + currentFile);

    compactedFiles.add(currentFile);
    
    currentEncoder.encodeUnsignedLong(nextLogId);
    
    // Leave enough space to write the number of records
    currentBuffer.position(currentBuffer.position() + 4);
    
    nextLogId++;
  }

  public void write(ValueRecord valueRecord) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "CompactingContext.write(" + valueRecord + ')');
    if (currentFile == null) {
      openFile();
    } else {
      int requiredSize = currentBuffer.position() + 
          RecordEncodingHelper.getEncodedSize(valueRecord) + 1; // Commit tag
      if (requiredSize > currentBuffer.capacity()) {
        openFile();
      }
    }

    RecordEncodingHelper.encodeRecord(valueRecord, currentEncoder, currentBuffer,
        currentFile);
    
    valueRecord.addToFile();
    currentRecordCount++;
  }
  
  public List<Record> getWrittenWhileCompacting() {
    return writtenWhileCompacting;
  }
  
  public void addWrittenWhileCompacting(Record record) {
    writtenWhileCompacting.add(record);
  }
  
  public List<TxLogFile> getCompactedFiles() {
     return compactedFiles;
  }

  @Override
  public String toString() {
    return "CompactingContext [nextLogId=" + nextLogId + ", currentFile="
        + currentFile + ", compactedFiles=" + compactedFiles
        + ", writtenWhileCompacting=" + writtenWhileCompacting
        + ", filesToCompact=" + filesToCompact + "]";
  }

}
