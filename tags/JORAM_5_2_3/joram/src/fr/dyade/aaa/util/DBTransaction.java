/*
 * Copyright (C) 2006 - 2008 ScalAgent Distributed Technologies
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Debug;

/**
 *  The DBTransaction class implements a transactionnal storage through
 * a JDBC interface. This class is designed to be specialized for different
 * database implementation. 
 *
 * @see Transaction
 * @see MySQLDBTransaction
 * @see DerbyDBTransaction
 */
public abstract class DBTransaction implements Transaction, DBTransactionMBean {
  // Logging monitor
  protected static Logger logmon = null;

  File dir = null;

  /** Log context associated with each Thread using DBTransaction. */
  private class Context {
    Hashtable log = null;
    ByteArrayOutputStream bos = null;
    ObjectOutputStream oos = null;

    Context() {
      log = new Hashtable(15);
      bos = new ByteArrayOutputStream(256);
    }
  }

  /**
   *  ThreadLocal variable used to get the log to associate state with each
   * thread. The log contains all operations do by the current thread since
   * the last <code>commit</code>. On commit, its content is added to current
   * log (memory + disk), then it is freed.
   */
  private ThreadLocal perThreadContext = null;

  /**
   *  Number of pooled operation, by default 1000.
   *  This value can be adjusted for a particular server by setting
   * <code>DBLogThresholdOperation</code> specific property.
   * <p>
   *  These property can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  static int LogThresholdOperation = 1000;

  /**
   * Returns the pool size for <code>operation</code> objects, by default 1000.
   *
   * @return The pool size for <code>operation</code> objects.
   */
  public int getLogThresholdOperation() {
    return LogThresholdOperation;
  }

  long startTime = 0L;

  /**
   * Returns the starting time.
   *
   * @return The starting time.
   */
  public long getStartTime() {
    return startTime;
  }

  protected Connection conn = null;

  private PreparedStatement insertStmt = null;
  private PreparedStatement updateStmt = null;
  private PreparedStatement deleteStmt = null;

  public DBTransaction() {}

  public void init(String path) throws IOException {
    phase = INIT;

    logmon = Debug.getLogger(Transaction.class.getName());
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "DBTransaction, init()");

    dir = new File(path);
    if (!dir.exists())
      dir.mkdir();
    if (!dir.isDirectory())
      throw new FileNotFoundException(path + " is not a directory.");

    // Saves the transaction classname in order to prevent use of a
    // different one after restart (see AgentServer.init).
    DataOutputStream ldos = null;
    try {
      File tfc = new File(dir, "TFC");
      if (!tfc.exists()) {
        ldos = new DataOutputStream(new FileOutputStream(tfc));
        ldos.writeUTF(getClass().getName());
        ldos.flush();
      }
    } finally {
      if (ldos != null)
        ldos.close();
    }

    initDB();

    try {
      insertStmt = conn.prepareStatement("INSERT INTO JoramDB VALUES (?, ?)");
      updateStmt = conn.prepareStatement("UPDATE JoramDB SET content=? WHERE name=?");
      deleteStmt = conn.prepareStatement("DELETE FROM JoramDB WHERE name=?");
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      throw new IOException(sqle.getMessage());
    }

    perThreadContext = new ThreadLocal() {
        protected synchronized Object initialValue() {
          return new Context();
        }
      };

    startTime = System.currentTimeMillis();

    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "DBTransaction, initialized " + startTime);

    /* The Transaction subsystem is ready */
    setPhase(FREE);
  }

  /**
   * Instantiates the database driver and creates the table if necessary
   * @throws IOException
   */
  protected abstract void initDB() throws IOException;

  public final File getDir() {
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
  private int phase = INIT;
  String phaseInfo = PhaseInfo[phase];

  /**
   *
   */
  public int getPhase() {
    return phase;
  }

  public String getPhaseInfo() {
    return phaseInfo;
  }

  private final void setPhase(int newPhase) {
    phase = newPhase;
    phaseInfo = PhaseInfo[phase];
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

  /**
   *  Returns an array of strings naming the persistent objects denoted by
   * a name that satisfy the specified prefix. Each string is an object name.
   * 
   * @param prefix	the prefix
   * @return		An array of strings naming the persistent objects
   *		 denoted by a name that satisfy the specified prefix. The
   *		 array will be empty if no names match.
   */
  public final synchronized String[] getList(String prefix) {
    try {
      // Creating a statement lets us issue commands against the connection.
      Statement s = conn.createStatement();
      ResultSet rs = s.executeQuery("SELECT name FROM JoramDB WHERE name LIKE '" + prefix + "%'");

      Vector v = new Vector();
      while (rs.next()) {
        v.add(rs.getString(1));
      }
      rs.close();
      s.close();

      String[] result = new String[v.size()];
      result = (String[]) v.toArray(result);

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "DBTransaction, getList: " + v);

      return result;
    } catch (SQLException sqle) {
      // TODO: AF
    }
    return null;
  }

  /**
   * Tests if the Transaction component is persistent.
   *
   * @return true.
   */
  public boolean isPersistent() {
    return true;
  }

  final String fname(String dirName, String name) {
    if (dirName == null) {
      return name;
    } else {
      return new StringBuffer(dirName).append('/').append(name).toString();
    }
  }

  static private final byte[] OOS_STREAM_HEADER = {
    (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF)
  };
  
  public final void create(Serializable obj, String name) throws IOException {
    save(obj, null, name);
  }
  
  public final void create(Serializable obj, String dirName, String name) throws IOException {
    save(obj, dirName, name);
  }

  public void save(Serializable obj,
                   String dirName, String name) throws IOException {
    save(obj, fname(dirName, name));
  }

  public void save(Serializable obj, String name) throws IOException{
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "DBTransaction, save(" + name + ")");

    Context ctx = (Context) perThreadContext.get();
    if (ctx.oos == null) {
      ctx.bos.reset();
      ctx.oos = new ObjectOutputStream(ctx.bos);
    } else {
      ctx.oos.reset();
      ctx.bos.reset();
      ctx.bos.write(OOS_STREAM_HEADER, 0, 4);
    }
    ctx.oos.writeObject(obj);
    ctx.oos.flush();

    saveInLog(ctx.bos.toByteArray(), name, ctx.log, false);
  }

  public void saveByteArray(byte[] buf,
                            String dirName, String name) throws IOException {
    save(buf, fname(dirName, name));
  }

  public void saveByteArray(byte[] buf, String name) throws IOException{
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "DBTransaction, saveByteArray(" + name + ")");

    saveInLog(buf, name, ((Context) perThreadContext.get()).log, true);
  }

  private final void saveInLog(byte[] buf,
                               String name,
                               Hashtable log,
                               boolean copy) throws IOException {
    DBOperation op = DBOperation.alloc(DBOperation.SAVE, name, buf);
    DBOperation old = (DBOperation) log.put(name, op);
    if (copy) {
      if ((old != null) &&
          (old.type == DBOperation.SAVE) &&
          (old.value.length == buf.length)) {
        // reuse old buffer
        op.value = old.value;
      } else {
        // alloc a new one
        op.value = new byte[buf.length];
      }
      System.arraycopy(buf, 0, op.value, 0, buf.length);
    }
    if (old != null) old.free();
  }

  public Object load(String dirName, String name) throws IOException, ClassNotFoundException {
    return load(fname(dirName, name));
  }

  public Object load(String name) throws IOException, ClassNotFoundException {
    byte[] buf = loadByteArray(name);
    if (buf != null) {
      ByteArrayInputStream bis = new ByteArrayInputStream(buf);
      ObjectInputStream ois = new ObjectInputStream(bis);	  
      return ois.readObject();
    }
    return null;
  }

  public byte[] loadByteArray(String dirName, String name) throws IOException {
    return loadByteArray(fname(dirName, name));
  }

  public synchronized byte[] loadByteArray(String name) throws IOException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "DBTransaction, loadByteArray(" + name + ")");

    // Searchs in the log a new value for the object.
    Hashtable log = ((Context) perThreadContext.get()).log;
    DBOperation op = (DBOperation) log.get(name);
    if (op != null) {
      if (op.type == DBOperation.SAVE) {
	return op.value;
      } else if (op.type == DBOperation.DELETE) {
	// The object was deleted.
	return null;
      }
    }

    try {
      // Creating a statement lets us issue commands against the connection.
      Statement s = conn.createStatement();
      //
      ResultSet rs = s.executeQuery("SELECT content FROM JoramDB WHERE name='" + name + "'");

      if (!rs.next()) return null;

       byte[] content = rs.getBytes(1);

       rs.close();
       s.close();

       return content;
    } catch (SQLException sqle) {
      throw new IOException(sqle.getMessage());
    }
  }

  public void delete(String dirName, String name) {
    delete(fname(dirName, name));
  }

  public void delete(String name) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "DBTransaction, delete(" + name + ")");

    Hashtable log = ((Context) perThreadContext.get()).log;
    DBOperation op = DBOperation.alloc(DBOperation.DELETE, name);
    op = (DBOperation) log.put(name, op);
    if (op != null) op.free();
  }

  public final synchronized void commit(boolean release) throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Can not commit.");

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "DBTransaction, commit");
    
    Hashtable log = ((Context) perThreadContext.get()).log;
    if (! log.isEmpty()) {
      DBOperation op = null;
      for (Enumeration e = log.elements(); e.hasMoreElements(); ) {
        op = (DBOperation) e.nextElement();
        if (op.type == DBOperation.SAVE) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "DBTransaction, commit.save (" + op.name + ')');

          try {
            insertStmt.setString(1, op.name);
            insertStmt.setBytes(2, op.value);
            insertStmt.executeUpdate();
          } catch (SQLException sqle1) {
            try {
              updateStmt.setBytes(1, op.value);
              updateStmt.setString(2, op.name);
              updateStmt.executeUpdate();
            } catch (SQLException sqle) {
              throw new IOException(sqle.getMessage());
            }
          }
        } else if (op.type == DBOperation.DELETE) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "DBTransaction, commit.delete (" + op.name + ')');

          try {
            deleteStmt.setString(1, op.name);
            deleteStmt.executeUpdate();
          } catch (SQLException sqle) {
            throw new IOException(sqle.getMessage());
          }
        }
        op.free();
      }
      log.clear();

      try {
        conn.commit();
      } catch (SQLException sqle) {
        throw new IOException(sqle.getMessage());
      }
    }

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "DBTransaction, committed");
    
    if (release) {
      // Change the transaction state and save it.
      setPhase(FREE);
      notify();
    } else {
      setPhase(COMMIT);
    }
  }

  public final synchronized void release() throws IOException {
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
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "DBTransaction, stops");

    while (phase != FREE) {
      // Wait for the transaction subsystem to be free
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }
    setPhase(FINALIZE);

//     server.shutdown();
    try {
//       org.hsqldb.DatabaseManager.closeDatabases(0);
      // Creating a statement lets us issue commands against the connection.
      Statement s = conn.createStatement();
      // .
//       s.execute("CHECKPOINT DEFRAG");
      conn.commit();
    logmon.log(BasicLevel.INFO, "DBTransaction, TBR stop#3");
      s.executeUpdate("SHUTDOWN COMPACT");
    logmon.log(BasicLevel.INFO, "DBTransaction, TBR stop#4");
      s.close();
    logmon.log(BasicLevel.INFO, "DBTransaction, TBR stop#5");
    } catch (SQLException sqle) {
// AF: TODO
//       throw new IOException(sqle.getMessage());
      logmon.log(BasicLevel.ERROR, "DBTransaction, stop#6", sqle);
    } catch (Throwable t) {
      logmon.log(BasicLevel.ERROR, "DBTransaction, stop#7", t);
    } finally {
      logmon.log(BasicLevel.INFO, "DBTransaction, stop#8");
    }
    logmon.log(BasicLevel.INFO, "DBTransaction, TBR stop#9");
    setPhase(FREE);

    if (logmon.isLoggable(BasicLevel.INFO)) {
      logmon.log(BasicLevel.INFO, "NTransaction, stopped: ");
    }
  }

  /**
   * Close the transaction module.
   * It waits all transactions termination, the module will be initialized
   * anew before reusing it.
   */
  public synchronized void close() {
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "DBTransaction, close");

    if (phase == INIT) return;

    while (phase != FREE) {
      // Wait for the transaction subsystem to be free
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }

    setPhase(FINALIZE);
    try {
      // Creating a statement lets us issue commands against the connection.
      Statement s = conn.createStatement();
      // .
      s.execute("SHUTDOWN COMPACT");
      s.close();
    } catch (SQLException sqle) {
// AF: TODO
//       throw new IOException(sqle.getMessage());
      logmon.log(BasicLevel.ERROR, "DBTransaction, close", sqle);
    }
    setPhase(INIT);

    if (logmon.isLoggable(BasicLevel.INFO)) {
      logmon.log(BasicLevel.INFO, "DBTransaction, closed: ");
    }
  }
}

final class DBOperation implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  static final int SAVE = 1;
  static final int DELETE = 2;
  static final int COMMIT = 3;
  static final int END = 127;
 
  int type;
  String name;
  byte[] value;

  private DBOperation(int type, String name, byte[] value) {
    this.type = type;
    this.name = name;
    this.value = value;
  }

  /**
   * Returns a string representation for this object.
   *
   * @return	A string representation of this object. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append('(').append(super.toString());
    strbuf.append(",type=").append(type);
    strbuf.append(",name=").append(name);
    strbuf.append(')');
    
    return strbuf.toString();
  }

  private static Pool pool = null;

  static {
    pool = new Pool("DBTransaction$Operation",
                    Integer.getInteger("DBLogThresholdOperation",
                                       DBTransaction.LogThresholdOperation).intValue());
  }

  static DBOperation alloc(int type, String name) {
    return alloc(type, name, null);
  }

  static DBOperation alloc(int type, String name, byte[] value) {
    DBOperation op = null;
    
    try {
      op = (DBOperation) pool.allocElement();
    } catch (Exception exc) {
      return new DBOperation(type, name, value);
    }
    op.type = type;
    op.name = name;
    op.value = value;

    return op;
  }

  void free() {
    /* to let gc do its work */
    name = null;
    value = null;
    pool.freeElement(this);
  }
}
