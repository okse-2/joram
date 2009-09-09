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
package fr.dyade.aaa.util;

import java.io.Serializable;

import fr.dyade.aaa.common.Pool;

/**
 * This class describes an operation in the transaction log.
 */
public class Operation implements Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  static final int SAVE = 1;
  static final int CREATE = 4;
  static final int DELETE = 2;
  static final int NOOP = 5;  // Create then delete
  static final int COMMIT = 3;
  static final int END = 127;
 
  /** Type of the operation. */
  int type;
  /** Relative path of the object if any, null otherwise. */
  String dirName;
  /** Name of the object */
  String name;
  /** Binary representation of the object (only for create and save operation). */
  byte[] value;

  // Actually the value below are only needed for NGTransaction
  
  // Index of the log file recording the operation.
  int logidx;
  // Pointer of the operation in the log file.
  int logptr;

  private Operation(int type, String dirName, String name, byte[] value) {
    this.type = type;
    this.dirName = dirName;
    this.name = name;
    this.value = value;
  }

  /**
   * Returns a string representation for this object.
   *
   * @return  A string representation of this object. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append('(').append(super.toString());
    strbuf.append(",type=").append(type);
    strbuf.append(",dirName=").append(dirName);
    strbuf.append(",name=").append(name);
    strbuf.append(",logidx=").append(logidx);
    strbuf.append(')');
    
    return strbuf.toString();
  }

  static Pool pool = null;

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
