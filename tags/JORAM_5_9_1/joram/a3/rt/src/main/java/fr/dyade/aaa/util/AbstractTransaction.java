/*
 * Copyright (C) 2009 - 2012 ScalAgent Distributed Technologies
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Hashtable;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.common.encoding.ByteBufferDecoder;
import fr.dyade.aaa.common.encoding.ByteBufferEncoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.EncodableFactoryRepository;
import fr.dyade.aaa.common.encoding.SerializableWrapper;

/**
 *  The AbstractTransaction class implements the common part of the Transaction
 * Transaction interface. A transaction implementation only needs to define several
 * methods: saveInLog, loadByteArray, delete and commit.
 *
 * @see Transaction
 */
public abstract class AbstractTransaction extends BaseTransaction {
  
  private static final byte JAVA_SERIALIZATION_TAG = 0x0;
  
  private static final byte ENCODING_TAG = 0x1;
  
  private boolean onlyUseJavaSerialization;
  
  protected long startTime = 0L;

  /**
   * Returns the starting time.
   *
   * @return The starting time.
   */
  public long getStartTime() {
    return startTime;
  }

  public AbstractTransaction() {}
  
  // State of the transaction monitor.
  protected int phase;

  /**
   * Returns the transaction state.
   * @return the transaction state.
   * 
   * @see fr.dyade.aaa.util.Transaction#getPhase()
   */
  public final int getPhase() {
    return phase;
  }

  /**
   * Returns a string representation of the transaction state.
   * @return the string representation of the transaction state.
   * 
   * @see fr.dyade.aaa.util.Transaction#getPhaseInfo()
   */
  public final String getPhaseInfo() {
    return PhaseInfo[phase];
  }

  /**
   * Changes the transaction state.
   * 
   * @param newPhase  the new transaction state.
   * @throws IOException
   */
  protected abstract void setPhase(int newPhase) throws IOException;

  protected File dir = null;

  /**
   *  ThreadLocal variable used to get the log to associate state with each
   * thread. The log contains all operations do by the current thread since
   * the last <code>commit</code>. On commit, its content is added to current
   * log (clog, memory + disk), then it is freed.
   */
  protected ThreadLocal<Context> perThreadContext = null;

  public abstract void initRepository() throws IOException;

  public final void init(String path) throws IOException {
    phase = INIT;

    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "Transaction, init():");

    dir = new File(path);
    if (!dir.exists()) dir.mkdirs();
    if (!dir.isDirectory())
      throw new FileNotFoundException(path + " is not a directory.");

    // Saves the transaction classname in order to prevent use of a
    // different one after restart (see AgentServer.init).
    DataOutputStream ldos = null;
    try {
      File tfc = new File(dir, "TFC");
      if (! tfc.exists()) {
        ldos = new DataOutputStream(new FileOutputStream(tfc));
        ldos.writeUTF(getClass().getName());
        ldos.flush();
      }
    } finally {
      if (ldos != null) ldos.close();
    }

    loadProperties(dir);
    
    onlyUseJavaSerialization = getBoolean("Transaction.OnlyUseJavaSerialization");
    
    initRepository();
    saveProperties(dir);
    
    perThreadContext = new ThreadLocal<Context>() {
      protected synchronized Context initialValue() {
        return new Context();
      }
    };
    
    startTime = System.currentTimeMillis();

    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, "Transaction, initialized " + startTime);

    /* The Transaction subsystem is ready */
    setPhase(FREE);
  }
  
  /**
   *  Start a transaction validation, the validation phase needs 3 phases: begin, commit
   * and release. The begin ensure the mutual exclusion of the current transaction.
   * 
   * @see fr.dyade.aaa.util.Transaction#begin()
   */
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

  public class Context {
    private Hashtable log = null;
    private ByteArrayOutputStream bos = null;
    private ObjectOutputStream oos = null;

    public final Hashtable getLog() {
      return log;
    }
    
    Context() {
      log = new Hashtable<Object, Operation>(15);
      bos = new ByteArrayOutputStream(256);
    }
  }

  /**
   *  The OOS_STREAM_HEADER allows to reset an ObjectOutputStream built on top of
   * a ByteArrayOutputStream.
   */
  static protected final byte[] OOS_STREAM_HEADER = {
    (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF),
    (byte)((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF)
  };

  /**
   * Register the state of a newly created object in the current transaction.
   * 
   * @param obj   the object to store.
   * @param name  the name of the object.
   * 
   * @see fr.dyade.aaa.util.Transaction#create(java.io.Serializable, java.lang.String)
   */
  public final void create(Serializable obj, String name) throws IOException {
    save(obj, null, name, true);
  }

  /**
   * Register the state of a newly created object in the current transaction.
   * 
   * @param obj     the object to store.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   * 
   * @see fr.dyade.aaa.util.Transaction#create(java.io.Serializable, java.lang.String, java.lang.String)
   */
  public final void create(Serializable obj,
                           String dirName, String name) throws IOException {
    save(obj, dirName, name, true);
  }

  /**
   * Register the modified state of an object in the current transaction.
   * 
   * @param obj     the object to store.
   * @param name    the name of the object.
   * 
   * @see fr.dyade.aaa.util.Transaction#save(java.io.Serializable, java.lang.String)
   */
  public final void save(Serializable obj, String name) throws IOException {
    save(obj, null, name, false);
  }


  /**
   * Register the modified state of an object in the current transaction.
   * 
   * @param obj     the object to store.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   * 
   * @see fr.dyade.aaa.util.Transaction#save(java.io.Serializable, java.lang.String, java.lang.String)
   */
  public final void save(Serializable obj,
                         String dirName, String name) throws IOException {
    save(obj, dirName, name, false);
  }
  
  private Encodable isEncodable(Object object) {
    if (object instanceof Encodable) {
      Encodable encodable = (Encodable) object;
      if (encodable.getEncodableClassId() >= 0) {
        return encodable;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
  
  private byte[] serialize(Serializable obj, Context ctx) throws IOException {
    if (ctx.oos == null) {
      ctx.bos.reset();
      if (! onlyUseJavaSerialization) {
        ctx.bos.write(JAVA_SERIALIZATION_TAG);
      }
      ctx.oos = new ObjectOutputStream(ctx.bos);
    } else {
      ctx.oos.reset();
      ctx.bos.reset();
      if (! onlyUseJavaSerialization) {
        ctx.bos.write(JAVA_SERIALIZATION_TAG);
      }
      ctx.bos.write(OOS_STREAM_HEADER, 0, 4);
    }
    
    ctx.oos.writeObject(obj);
    ctx.oos.flush();
    return ctx.bos.toByteArray();
  }

  /**
   * Register the state of an object in the current transaction.
   * 
   * @param obj     the object to store.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   * @param first   the object is a new one.
   * 
   * @see fr.dyade.aaa.util.Transaction#save(java.io.Serializable, java.lang.String, java.lang.String, boolean)
   */
  public final void save(Serializable obj,
                         String dirName, String name,
                         boolean first) throws IOException {
    Context ctx = perThreadContext.get();
    byte[] encodedObject;
    if (onlyUseJavaSerialization) {
      encodedObject = serialize(obj, ctx);
    } else {
      Encodable encodable = isEncodable(obj);
      if (encodable == null) {
        encodedObject = serialize(obj, ctx);
      } else {
        try {
          encodedObject = new byte[Encodable.BYTE_ENCODED_SIZE
              + Encodable.INT_ENCODED_SIZE + encodable.getEncodedSize()];
          ByteBuffer buf = ByteBuffer.wrap(encodedObject);
          ByteBufferEncoder encoder = new ByteBufferEncoder(buf);
          encoder.encodeByte(ENCODING_TAG);
          encoder.encode32(encodable.getEncodableClassId());
          encodable.encode(encoder);
        } catch (Exception exc) {
          throw new IOException(exc);
        }
      }
    }
    
    saveInLog(encodedObject, dirName, name, ctx.log, false, first);
  }

  /**
   * Register a new byte array in the current transaction.
   * 
   * @param buf   the byte array to store.
   * @param name  the name of the object.
   * 
   * @see fr.dyade.aaa.util.Transaction#createByteArray(byte[], java.lang.String)
   */
  public final void createByteArray(byte[] buf, String name) throws IOException {
    saveByteArray(buf, null, name, true, true);
  }

  /**
   * Register a new byte array in the current transaction.
   * 
   * @param buf     the byte array to store.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   * 
   * @see fr.dyade.aaa.util.Transaction#createByteArray(byte[], java.lang.String, java.lang.String)
   */
  public final void createByteArray(byte[] buf,
                                    String dirName, String name) throws IOException {
    saveByteArray(buf, dirName, name, true, true);
  }

  /**
   * Register a modified byte array in the current transaction.
   * 
   * @param buf     the byte array to store.
   * @param name    the name of the object.
   * 
   * @see fr.dyade.aaa.util.Transaction#saveByteArray(byte[], java.lang.String)
   */
  public final void saveByteArray(byte[] buf, String name) throws IOException {
    saveByteArray(buf, null, name, true, false);
  }
  
  /**
   * Register a modified byte array in the current transaction.
   * 
   * @param buf     the byte array to store.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   * 
   * @see fr.dyade.aaa.util.Transaction#saveByteArray(byte[], java.lang.String, java.lang.String)
   */
  public final void saveByteArray(byte[] buf,
                                  String dirName, String name) throws IOException {
    saveByteArray(buf, dirName, name, true, false);
  }
  
  /**
   * Register a modified byte array in the current transaction.
   * 
   * @param buf     the byte array to store.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   * @param copy    the byte array can be modified, copy it.
   * @param first   the object is a new one.
   * 
   * @see fr.dyade.aaa.util.Transaction#saveByteArray(byte[], java.lang.String, java.lang.String, boolean, boolean)
   */
  public final void saveByteArray(byte[] buf,
                                  String dirName, String name,
                                  boolean copy,
                                  boolean first) throws IOException {
    saveInLog(buf, dirName, name, perThreadContext.get().log, copy, first);
  }

  /**
   * Register the specified object in transaction log.
   * 
   * @param buf     the byte array containing the state of the object.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   * @param log     the log to use.
   * @param copy    the byte array can be modified, copy it.
   * @param first   the object is a new one.
   * @throws IOException
   */
  protected abstract void saveInLog(byte[] buf,
                                    String dirName, String name,
                                    Hashtable log,
                                    boolean copy,
                                    boolean first) throws IOException;

  /**
   * Load the specified object.
   * 
   * @param name    the name of the object.
   * @return the loaded object.
   * 
   * @see fr.dyade.aaa.util.Transaction#load(java.lang.String)
   */
  public final Object load(String name) throws IOException, ClassNotFoundException {
    return load(null, name);
  }
  
  /**
   * Load the specified object.
   * 
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   * @return the loaded object.
   * 
   * @see fr.dyade.aaa.util.Transaction#load(java.lang.String, java.lang.String)
   */
  public final Object load(String dirName, String name) throws IOException, ClassNotFoundException {
    byte[] buf = loadByteArray(dirName, name);
    if (buf != null) {
      if (onlyUseJavaSerialization) {
        return deserialize(buf, 0);
      } else if (buf.length > 0) {
        byte tag = buf[0];
        switch (tag) {
        case JAVA_SERIALIZATION_TAG:
          return deserialize(buf, 1);
        case ENCODING_TAG:
          return decode(buf, 1);
        default:
          throw new IOException("Unexpected tag: " + tag);
        }
      } else {
        throw new IOException("Unexpected empty byte array");
      }
    } else {
      return null;
    }
  }
  
  private Object deserialize(byte[] buf, int offset) throws IOException, ClassNotFoundException {
    ByteArrayInputStream bis = new ByteArrayInputStream(buf, offset, buf.length - offset);
    ObjectInputStream ois = new ObjectInputStream(bis);
    try {
      return ois.readObject();
    } finally {
      ois.close();
      bis.close();
    }
  }
  
  private Object decode(byte[] buf, int offset) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.wrap(buf, offset, buf.length - offset);
    ByteBufferDecoder decoder = new ByteBufferDecoder(byteBuffer);
    try {
      int classId = decoder.decode32();
      EncodableFactory factory = EncodableFactoryRepository.getFactory(classId);
      if (factory == null) throw new Exception("Unknown factory: " + classId);
      Encodable encodable = factory.createEncodable();
      encodable.decode(decoder);
      return encodable;
    } catch (Exception exc) {
      throw new IOException(exc);
    }
  }

  /**
   * Load the specified byte array.
   * 
   * @param name    the name of the object.
   * @return the loaded byte array.
   * 
   * @see fr.dyade.aaa.util.Transaction#loadByteArray(java.lang.String)
   */
  public final byte[] loadByteArray(String name) throws IOException {
    return loadByteArray(null, name);
  }

  /**
   * Deletes the specified object.
   * 
   * @param name    the name of the object.
   * 
   * @see fr.dyade.aaa.util.Transaction#delete(java.lang.String)
   */
  public final void delete(String name) {
    delete(null, name);
  }

  /**
   * Release the mutual exclusion.
   * @see fr.dyade.aaa.util.Transaction#release()
   */
  public synchronized void release() throws IOException {
    if ((phase != RUN) && (phase != COMMIT) && (phase != ROLLBACK))
      throw new IllegalStateException("Can not release transaction: " + getPhaseInfo() + '.');

    // Change the transaction state.
    setPhase(FREE);
    // wake-up an eventually user's thread in begin
    notify();
  }
  
  /**
   * Indicates whether some operations have been done in
   * this transaction.
   */
  public boolean containsOperations() {
    return (perThreadContext.get().getLog().size() > 0);
  }
  
  /**
   * Returns the number of operations to be committed.
   * @return the number of operations to be committed.
   */
  public int getOperationCount() {
    return perThreadContext.get().getLog().size();
  }
  
}
