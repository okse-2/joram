/*
 * Copyright (C) 2001 - 2011 ScalAgent Distributed Technologies
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *  The JTransaction class implements a transactionnal storage.
 *  This implementation is simple but inefficient, its main advantage is
 * the compatibility with old JDK.
 *
 * @see Transaction
 */
public final class JTransaction extends BaseTransaction implements JTransactionMBean {
  protected long startTime = 0L;

  /**
   * Returns the starting time.
   *
   * @return The starting time.
   */
  public long getStartTime() {
    return startTime;
  }

  public static final String EMPTY_STRING = new String();

  /**
   *  Number of pooled operation, by default 100.
   *  This value can be adjusted for a particular server by setting
   * <code>LogThresholdOperation</code> specific property.
   * <p>
   *  These property can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  static int LogThresholdOperation = 100;

  private File dir = null;

  static private final String LOG = "log";
  private RandomAccessFile logFile = null; 
  private Hashtable log = null;

  public JTransaction() {}

  /**
   * Tests if the Transaction component is persistent.
   *
   * @return true.
   */
  public boolean isPersistent() {
    return true;
  }

  public void init(String path) throws IOException {
    phase = INIT;

    dir = new File(path);
    if (!dir.exists()) dir.mkdir();
    if (!dir.isDirectory())
      throw new FileNotFoundException(path + " is not a directory.");

    // Saves the transaction classname in order to prevent use of a
    // different one after restart (see AgentServer.init).
    DataOutputStream dos = null;
    try {
      File tfc = new File(dir, "TFC");
      if (! tfc.exists()) {
        dos = new DataOutputStream(new FileOutputStream(tfc));
        dos.writeUTF(getClass().getName());
        dos.flush();
      }
    } finally {
      if (dos != null) dos.close();
    }

    loadProperties(dir);
    LogThresholdOperation = getInteger("LogThresholdOperation", LogThresholdOperation).intValue();
    Operation.initPool(LogThresholdOperation);
    saveProperties(dir);
    
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
          if (op == Operation.SAVE) {
            byte buf[] = new byte[logFile.readInt()];
            logFile.readFully(buf);
            log.put(key, Operation.alloc(Operation.SAVE, dirName, name, buf));
          } else {
            log.put(key, Operation.alloc(op, dirName, name));
          }
        }
      }
      _commit();
    }
    
    startTime = System.currentTimeMillis();

    setPhase(FREE);
  }

  public File getDir() {
    return dir;
  }

  /**
   * Returns the path of persistence directory.
   *
   * @return The path of persistence directory.
   */
  public String getPersistenceDir() {
    return dir.getPath();
  }
  
  // State of the transaction monitor.
  protected int phase;

  public final int getPhase() {
    return phase;
  }

  public final String getPhaseInfo() {
    return PhaseInfo[phase];
  }

  protected void setPhase(int newPhase) throws IOException {
    logFile.seek(0L);
    logFile.writeInt(newPhase);
    logFile.getFD().sync();
    phase = newPhase;
  }

  public final synchronized void begin() throws IOException {
    while (phase != FREE) {
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }
    // Change the transaction state.
    setPhase(RUN);
  }

  public String[] getList(String prefix) {
    return dir.list(new StartWithFilter(prefix));
  }

  public final void create(Serializable obj, String name) throws IOException {
    save(obj, null, name, true);
  }

  public final void create(Serializable obj,
                     String dirName, String name) throws IOException {
    save(obj, dirName, name, true);
  }

  public final void save(Serializable obj, String name) throws IOException {
    save(obj, null, name, false);
  }

  public final void save(Serializable obj,
                         String dirName, String name) throws IOException {
    save(obj, dirName, name, false);
  }

  public void save(Serializable obj,
                   String dirName, String name,
                   boolean first) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(obj);
    oos.flush();
    saveByteArray(bos.toByteArray(), dirName, name);
  }

  public final void createByteArray(byte[] buf, String name) throws IOException {
    saveByteArray(buf, null, name, true, true);
  }
  
  public final void createByteArray(byte[] buf,
                                  String dirName, String name) throws IOException {
    saveByteArray(buf, dirName, name, true, true);
  }

  public final void saveByteArray(byte[] buf, String name) throws IOException {
    saveByteArray(buf, null, name, true, false);
  }
  
  public final void saveByteArray(byte[] buf,
                                  String dirName, String name) throws IOException {
    saveByteArray(buf, dirName, name, true, false);
  }

  public void saveByteArray(byte[] buf,
                            String dirName, String name,
                            boolean copy,
                            boolean first) throws IOException {
    if (phase == RUN) {
      // We are during a transaction put the new state in the log.
      Object key = OperationKey.newKey(dirName, name);
      log.put(key, Operation.alloc(Operation.SAVE, dirName, name, buf));
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
      fos.write(buf);
      fos.close();
    }
  }

  public final Object load(String name) throws IOException, ClassNotFoundException {
    return load(null, name);
  }
  
  public final Object load(String dirName, String name) throws IOException, ClassNotFoundException {
    byte[] buf = loadByteArray(dirName, name);
    if (buf != null) {
      ByteArrayInputStream bis = new ByteArrayInputStream(buf);
      ObjectInputStream ois = new ObjectInputStream(bis);
      try {
      	return ois.readObject();
      } finally {
        ois.close();
        bis.close();
      }
    }
    
    return null;
  }

  public final byte[] loadByteArray(String name) throws IOException {
    return loadByteArray(null, name);
  }

  public byte[] loadByteArray(String dirName, String name) throws IOException {
    if (phase == RUN) {
      // first search in the log a new value for the object.
      Object key = OperationKey.newKey(dirName, name);
      Operation op = (Operation) log.get(key);
      if (op != null) {
        if (op.type == Operation.SAVE) {
          return op.value;
        } else if (op.type == Operation.DELETE) {
          // The object is no longer alive
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
      byte[] buf = new byte[(int) file.length()];
      for (int nb=0; nb<buf.length; ) {
        int ret = fis.read(buf, nb, buf.length-nb);
        if (ret == -1) throw new EOFException();
        nb += ret;
      }
      fis.close();

      return buf;
    } catch (FileNotFoundException exc) {
      return null;
    }
  }

  public final void delete(String name) {
    delete(null, name);
  }

  public void delete(String dirName, String name) {
    if (phase == RUN) {
      // We are during a transaction mark the object deleted in the log.
      Object key = OperationKey.newKey(dirName, name);
      log.put(key, Operation.alloc(Operation.DELETE, dirName, name));
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
   * Also recursively delete the parent directories if they are empty.
   */
  private void deleteDir(File dir) {
    String[] children = dir.list();
    // children may be null if dir doesn't exist any more.
    if (children != null && children.length == 0) {
      dir.delete();
      if (dir.getAbsolutePath().length() > this.dir.getAbsolutePath().length()) {
        deleteDir(dir.getParentFile());
      }
    }
  }

  public synchronized void commit(boolean release) throws IOException {
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
      if (op.type == Operation.SAVE) {
        logFile.writeInt(op.value.length);
        logFile.write(op.value);
      }
    }
    logFile.writeUTF("");
    setPhase(COMMIT);
    _commit();
    log.clear();

    if (release) {
      // Change the transaction state and save it.
      setPhase(FREE);
      notify();
    }
  }

  private void _commit() throws IOException {    
    for (Enumeration e = log.elements(); e.hasMoreElements(); ) {
      Operation op = (Operation) e.nextElement();

      if (op.type == Operation.SAVE) {
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
      } else if (op.type == Operation.DELETE) {
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

  public synchronized void release() throws IOException {
    if ((phase != RUN) && (phase != COMMIT) && (phase != ROLLBACK))
      throw new IllegalStateException("Can not release transaction.");

    // Change the transaction state.
    setPhase(FREE);
    // wake-up an eventually user's thread in begin
    notify();
  }

  /**
   * Stops the transaction module.
   * It waits all transactions termination, then the module is kept
   * in a FREE 'ready to use' state.
   */
  public synchronized void stop() {
    while (phase != FREE) {
      try {
        // Wait for the transaction subsystem to be free
        wait();
      } catch (InterruptedException exc) {
      }
    }
  }

  /**
   * Close the transaction module.
   * It waits all transactions termination, the module will be initialized
   * anew before reusing it.
   */
  public synchronized void close() {
    stop();

    try {
      setPhase(FINALIZE);
      logFile.close();
      setPhase(INIT);
    } catch (IOException exc) {
    }
  }
  
  /**
   * Indicates whether some operations have been done in
   * this transaction.
   */
  public boolean containsOperations() {
    return log.size() > 0;
  }
}
