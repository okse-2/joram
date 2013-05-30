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

public class ValueRecord extends Record {
  
  public static ValueRecord newCreateRecord(Encodable objectId, Encodable value, byte[] encodedValue) throws Exception {
    return new ValueRecord(Record.CREATE_RECORD, objectId, value, encodedValue);
  }
  
  public static ValueRecord newSaveRecord(Encodable objectId, Encodable value, byte[] encodedValue) throws Exception {
    return new ValueRecord(Record.SAVE_RECORD, objectId, value, encodedValue);
  }
  
  private long filePointer;
  
  private int byteArraySize;

  private Encodable value;
  
  /**
   * A byte array (as a value) or the following encoded result:
   * class id + encodable
   */
  private byte[] byteArray;
  
  private int compactCount;
  
  private ValueRecord(byte recordType, Encodable objectId, Encodable value, byte[] byteArray) {
    super(recordType, objectId);
    this.value = value;
    this.byteArray = byteArray;
    if (byteArray == null) {
      byteArraySize = -1;
    } else {
      byteArraySize = byteArray.length;
    }
  }
  
  public void init() throws Exception {
    if (byteArraySize < 0) {
      if (value != null) {
        int valueEncodedSize = value.getEncodedSize();
        // Class id + encodable
        byteArraySize = 4 + valueEncodedSize;
      } else {
        byteArraySize = byteArray.length;
      }
    }
    // else already initialized
  }

  public int getCompactCount() {
    return compactCount;
  }

  public void incCompactCount() {
    compactCount++;
  }

  public long getFilePointer() {
    return filePointer;
  }

  public void setFilePointer(long filePointer) {
    this.filePointer = filePointer;
  }
  
  public Encodable getValue() {
    return value;
  }

  public void setValue(Encodable value) {
    this.value = value;
  }

  public byte[] getByteArray() {
    return byteArray;
  }

  public int getByteArraySize() throws Exception {
    if (byteArraySize < 0) throw new Exception("Byte array size not available for: " + this);
    return byteArraySize;
  }

  public void setByteArraySize(int byteArraySize) {
    this.byteArraySize = byteArraySize;
  }

  public void setByteArray(byte[] encodedValue) {
    this.byteArray = encodedValue;
  }

  public void checkRecordType(byte recordType) throws Exception {
    if (recordType == DELETE_RECORD) throw new Exception("Forbidden");
  }
  
  public void addToFile() throws Exception {
    getFile().addRecord(this);
  }
  
  public void removeFromFile() throws Exception {
    getFile().removeRecord(this);
  }

  @Override
  public String toString() {
    return "ValueRecord [filePointer="
        + filePointer 
        + ", byteArraySize=" + byteArraySize 
        + ", value=" + value
        + ", compactCount=" + compactCount
        + ", toString()=" + super.toString() + "]";
  }

}
