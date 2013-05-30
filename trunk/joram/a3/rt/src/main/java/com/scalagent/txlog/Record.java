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

import fr.dyade.aaa.common.encoding.Encodable;

public abstract class Record {
  
  public static final byte CREATE_RECORD = 0;
  public static final byte SAVE_RECORD = 1;
  public static final byte DELETE_RECORD = 2;

  private byte recordType;
  
  private Encodable objectId;
  
  private TxLogFile file;

  public Record(byte recordType, Encodable objectId) {
    super();
    this.recordType = recordType;
    this.objectId = objectId;
  }

  public Encodable getObjectId() {
    return objectId;
  }
  
  public void setObjectId(Encodable objectId) {
    this.objectId = objectId;
  }

  public byte getRecordType() {
    return recordType;
  }
  
  public void setRecordType(byte recordType) throws Exception {
    checkRecordType(recordType);
    this.recordType = recordType;
  }
  
  public TxLogFile getFile() {
    return file;
  }

  public void setFile(TxLogFile file) {
    this.file = file;
  }
  
  public abstract void checkRecordType(byte recordType) throws Exception;

  @Override
  public String toString() {
    return "Record [recordType=" + recordType + ", objectId=" + objectId + 
        ", file=" + file.getFile().getName() + "]";
  }
  
}
