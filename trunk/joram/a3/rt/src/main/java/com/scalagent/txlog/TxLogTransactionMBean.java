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

public interface TxLogTransactionMBean {
  
  int getFileSize();

  int getMinFileCount();

  boolean isSyncOnWrite();

  int getCompactCountThreshold();

  int getCompactRatio();
  
  int getRecycledEmptyFileCount();
  
  int getRecycledCompactedFileCount();

  int getDeletedFileCount();
  
  int getNewFileCount();
  
  int getMinCompactFileCount();
  
  int getMaxFileCount();
  
  int getRecordCount();
  
  int getWrittenWhileCompactingCount();

  int getCompactCount();
  
  long getCompactDuration();
  
  int getFileToCompactCount();
  
  int getCreateRecordCount();
  
  int getDeleteRecordCount();
  
  long getUsedFileCompactThreshold();

  long getUsedFileLiveSize();
  
  boolean isSynchronousCompact();
  
  int getLiveRecordCount();
  
  int getLargestRecordEncodedSize();
  
  int getMaxSaveCount();
  
  int getUsedFileCount();
  
  int getInitFileCount();
  
  int getAvailableFileCount();
  
  int getCompactDelay();
  
  boolean isUseNioFileChannel();
  
}
