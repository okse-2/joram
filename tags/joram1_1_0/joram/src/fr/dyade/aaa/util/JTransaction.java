/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package fr.dyade.aaa.util;

import java.io.*;
import java.util.*;

class Operation implements Serializable {
  static final int SAVE = 1;
  static final int DELETE = 2;

  int type;
  byte[] value = null;

  Operation(int type) {
    this.type = type;
  }

  Operation(int type, byte[] value) {
    this.type = type;
    this.value = value;
  }
}

public class JTransaction implements Transaction {

public static final String RCS_VERSION="@(#)$Id: JTransaction.java,v 1.3 2000-10-05 15:21:08 tachkeni Exp $"; 

  private File dir = null;

  static private final String LOG = "log";
  private RandomAccessFile logFile = null; 
  private Hashtable log = null;

  // State of the transaction monitor.
  private int phase;

  static private final int INIT = 0;		// Initialization state
  static private final int FREE = 1;		// No transaction 
  static private final int RUN = 2;		// A transaction is running
  static private final int COMMIT = 3;		// A transaction is commiting
  static private final int ROLLBACK = 4;	// A transaction is aborting


  public JTransaction(String path) throws IOException {
    phase = INIT;

    dir = new File(path);
    if (!dir.exists()) dir.mkdir();
    if (!dir.isDirectory())
      throw new FileNotFoundException(path + " is not a directory.");

    // Read the log, then...
    int oldPhase = FREE;
    
    logFile = new RandomAccessFile(new File(dir, LOG), "rw");
    log = new Hashtable();
    if (logFile.length() != 0) {
      logFile.seek(0L);
      oldPhase = logFile.readInt();

      // Test the stop status then complete commit or rollback if needed
      if (oldPhase == COMMIT) {
	int op;
	String name;
	while (!(name = logFile.readUTF()).equals("")) {
	  op = logFile.read();
	  if (op == Operation.SAVE) {
	    byte buf[] = new byte[logFile.readInt()];
	    logFile.readFully(buf);
	    log.put(name, new Operation(Operation.SAVE, buf));
	  } else {
	    log.put(name, new Operation(op));
	  }
	}
      }
      _commit();
    }
    setPhase(FREE);
  }

  public File getDir() {
    return dir;
  }

  private void setPhase(int newPhase) throws IOException {
    logFile.seek(0L);
    logFile.writeInt(newPhase);
    logFile.getFD().sync();
    phase = newPhase;
  }

  public synchronized void begin() throws IOException {
    while (phase != FREE) {
      try {
	wait();
      } catch (InterruptedException exc) {
      }
    }
    // Change the transaction state and save it.
    setPhase(RUN);
  }

  public String[] getList(String prefix) {
    return dir.list(new StartWithFilter(prefix));
  }

  public void save(Serializable obj, String name) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(obj);
    oos.flush();
    byte[] bobj = bos.toByteArray();

    if (phase == RUN) {
      // We are during a transaction put the new state in the log.
      log.put(name, new Operation(Operation.SAVE, bobj));
    } else {
      // Save the new state on the disk.
      FileOutputStream fos = new FileOutputStream(new File(dir, name));
      fos.write(bobj);
      fos.close();
    }
  }

  public Object load(String name) throws IOException, ClassNotFoundException {
    Object obj;

    if (phase == RUN) {
      // first search in the log a new value for the object.
      Operation op = (Operation) log.get(name);
      if (op != null) {
	if (op.type == Operation.SAVE) {
	  ByteArrayInputStream bis = new ByteArrayInputStream(op.value);
	  ObjectInputStream ois = new ObjectInputStream(bis);
	  
	  return ois.readObject();
	} else if (op.type == Operation.DELETE) {
	  // l'objet a *t* d*truit.
	  return null;
	}
      }
    }

    try {
      File file = new File(dir, name);
      FileInputStream fis = new FileInputStream(file);
      
      // I'm not sure we can directly read the object without use
      // a ByteArrayInputStream.
      ObjectInputStream ois = new ObjectInputStream(fis);
      obj = ois.readObject();
      
      fis.close();
    } catch (FileNotFoundException exc) {
      return null;
    }

    return obj;
  }

  public void delete(String name) {
    if (phase == RUN) {
      // We are during a transaction mark the object deleted in the log.
      log.put(name, new Operation(Operation.DELETE));
    } else {
      File file = new File(dir, name);
      file.delete();
    }
  }

  public synchronized void commit() throws IOException {
    if (phase != RUN)
      throw new NotActiveException("Can not commit inexistent transaction.");
    
    // Save the log to disk
    logFile.seek(4L);
    for (Enumeration e = log.keys(); e.hasMoreElements(); ) {
      String name = (String) e.nextElement();
      Operation op = (Operation) log.get(name);

      logFile.writeUTF(name);
      logFile.writeByte(op.type);
      if (op.type == Operation.SAVE) {
	logFile.writeInt(op.value.length);
	logFile.write(op.value);
      }
    }
    logFile.writeUTF("");
    setPhase(COMMIT);
    _commit();
    log.clear();
  }

  private void _commit() throws IOException {    
    for (Enumeration e = log.keys(); e.hasMoreElements(); ) {
      String name = (String) e.nextElement();
      Operation op = (Operation) log.get(name);

      if (op.type == Operation.SAVE) {
	FileOutputStream fos = new FileOutputStream(new File(dir, name));
	fos.write(op.value);
	fos.getFD().sync();
	fos.close();
      } else if (op.type == Operation.DELETE) {
	File file = new File(dir, name);
	file.delete();
      } else {
	throw new InvalidObjectException("Unknow object in log.");
      }
    }
  }

  public synchronized void rollback() throws IOException {
    if (phase != RUN)
      throw new NotActiveException("Can not rollback inexistent transaction.");
    setPhase(ROLLBACK);
    log.clear();
  }

  public synchronized void release() throws IOException {
    if ((phase != COMMIT) && (phase != ROLLBACK))
      throw new NotActiveException("Can not release transaction.");

    // Change the transaction state and save it.
    setPhase(FREE);
    notify();
  }
}
