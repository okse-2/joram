/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.util;

import java.io.*;
import java.util.*;

public class JTransaction implements Transaction {
  public static final String RCS_VERSION="@(#)$Id: JTransaction.java,v 1.13 2003-06-23 13:45:20 fmaistre Exp $";

  public static final String EMPTY_STRING = new String();

  private File dir = null;

  static private final String LOG = "log";
  private RandomAccessFile logFile = null; 
  private Hashtable log = null;

  // SAVE and DELETE should be static attribute of Operation inner class.
  // Unfortunatly it's unsupported in Java 1.1.x.
  static final int SAVE = 1;
  static final int DELETE = 2;

  class Operation implements Serializable {
    String dirName;
    String name;
    int type;
    byte[] value = null;

    Operation(int type, String dirName, String name) {
      this(type, dirName, name, null);
    }

    Operation(int type, String dirName, String name, byte[] value) {
      this.type = type;
      this.dirName = dirName;
      this.name = name;
      this.value = value;
    }
  }

  // State of the transaction monitor.
  private int phase;

  static private final int INIT = 0;		// Initialization state
  static private final int FREE = 1;		// No transaction 
  static private final int RUN = 2;		// A transaction is running
  static private final int COMMIT = 3;		// A transaction is commiting
  static private final int ROLLBACK = 4;	// A transaction is aborting

  public JTransaction() {}

  public void init(String path) throws IOException {
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
          String dirName = logFile.readUTF();
          if (dirName.length() == 0) dirName = null;
          Object key = OperationKey.newKey(dirName, name);
	  op = logFile.read();
	  if (op == SAVE) {
	    byte buf[] = new byte[logFile.readInt()];
	    logFile.readFully(buf);
	    log.put(key, new Operation(SAVE, dirName, name, buf));
	  } else {
	    log.put(key, new Operation(op, dirName, name));
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
    save(obj, null, name);
  }

  public final void save(Serializable obj, String dirName, String name) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(obj);
    oos.flush();
    byte[] bobj = bos.toByteArray();

    if (phase == RUN) {
      // We are during a transaction put the new state in the log.
      Object key = OperationKey.newKey(dirName, name);
      log.put(key, new Operation(SAVE, dirName, name, bobj));
    } else {
      // Save the new state on the disk.
      File file;
      if (dirName == null) {
        file = new File(dir, name);
      } else {
        File parentDir = new File(dir, dirName);
        if (!parentDir.exists()) {
          parentDir.mkdirs();
        }
        file = new File(parentDir, name);
      }
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(bobj);
      fos.close();
    }
  }

  public Object load(String name) throws IOException, ClassNotFoundException {
    return load(null, name);
  }

  public final Object load(String dirName, String name) throws IOException, ClassNotFoundException {
    Object obj;

    if (phase == RUN) {
      // first search in the log a new value for the object.
      Object key = OperationKey.newKey(dirName, name);
      Operation op = (Operation) log.get(key);
      if (op != null) {
	if (op.type == SAVE) {
	  ByteArrayInputStream bis = new ByteArrayInputStream(op.value);
	  ObjectInputStream ois = new ObjectInputStream(bis);
	  
	  return ois.readObject();
	} else if (op.type == DELETE) {
	  // l'objet a *t* d*truit.
	  return null;
	}
      }
    }

    try {
      File file;
      if (dirName == null) {
        file = new File(dir, name);
      } else {
        File parentDir = new File(dir, dirName);
        file = new File(parentDir, name);
      }
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
    delete(null, name);
  }

  public final void delete(String dirName, String name) {
    if (phase == RUN) {
      // We are during a transaction mark the object deleted in the log.
      Object key = OperationKey.newKey(dirName, name);
      log.put(key, new Operation(DELETE, dirName, name));
    } else {
      File file;
      if (dirName == null) {
        file = new File(dir, name);
        file.delete();
      } else {
        File parentDir = new File(dir, dirName);
        file = new File(parentDir, name);
        file.delete();
        deleteDir(parentDir);
      }      
    }
  }

  /**
   * Delete the specified directory if it is empty.
   * Also recursively delete the parent directories if
   * they are empty.
   */
  private void deleteDir(File dir) {
    String[] children = dir.list();
    // children may be null if dir doesn't exist any more.
    if (children != null && 
        children.length == 0) {
      dir.delete();
      if (dir.getAbsolutePath().length() > 
          this.dir.getAbsolutePath().length()) {
        deleteDir(dir.getParentFile());
      }
    }
  }

  public synchronized void commit() throws IOException {
    if (phase != RUN)
      throw new NotActiveException("Can not commit inexistent transaction.");
    
    // Save the log to disk
    logFile.seek(4L);
    for (Enumeration e = log.elements(); e.hasMoreElements(); ) {
      Operation op = (Operation) e.nextElement();      
      logFile.writeUTF(op.name);
      if (op.dirName != null) {
        logFile.writeUTF(op.dirName);
      } else {
        logFile.writeUTF(EMPTY_STRING);
      }
      logFile.writeByte(op.type);
      if (op.type == SAVE) {
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
    for (Enumeration e = log.elements(); e.hasMoreElements(); ) {
      Operation op = (Operation) e.nextElement();

      if (op.type == SAVE) {
        File file;
        if (op.dirName == null) {
          file = new File(dir, op.name);
        } else {
          File parentDir = new File(dir, op.dirName);
          if (!parentDir.exists()) {
            parentDir.mkdirs();
          }
          file = new File(parentDir, op.name);
        }
	FileOutputStream fos = new FileOutputStream(file);
	fos.write(op.value);
	fos.getFD().sync();
	fos.close();
      } else if (op.type == DELETE) {
        File file;
        if (op.dirName == null) {
          file = new File(dir, op.name);
          file.delete();
        } else {
          File parentDir = new File(dir, op.dirName);
          file = new File(parentDir, op.name);
          file.delete();
          deleteDir(parentDir);
        } 
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

  public final void stop() {}

  private static class OperationKey {

    static Object newKey(String dirName, String name) {
      if (dirName == null) {
        return name;
      } else {
        return new OperationKey(dirName, name);
      }
    }

    private String dirName;
    private String name;

    private OperationKey(String dirName,
                        String name) {
      this.dirName = dirName;
      this.name = name;
    }

    public int hashCode() {
      // Should compute a specific one.
      return dirName.hashCode();
    }

    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj instanceof OperationKey) {
        OperationKey opk = (OperationKey)obj;
        if (opk.name.length() != name.length()) return false;
        if (opk.dirName.length() != dirName.length()) return false;
        if (!opk.dirName.equals(dirName)) return false;            
        return opk.name.equals(name);
      }
      return false;
    }
  }
}
