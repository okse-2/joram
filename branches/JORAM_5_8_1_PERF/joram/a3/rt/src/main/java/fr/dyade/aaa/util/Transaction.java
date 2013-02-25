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
 *
 * Initial developer(s): Dyade
 * Contributor(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.util;

import java.io.IOException;
import java.io.Serializable;

/**
 * The Transaction interface defines the API of the atomic storage component.
 */
public interface Transaction {
  final int INIT = 0;	  // Initialization state
  final int FREE = 1;	  // No transaction 
  final int RUN = 2;	  // A transaction is running
  final int COMMIT = 3;	  // A transaction is commiting
  final int ROLLBACK = 4;  // A transaction is aborting
  final int GARBAGE = 5;   // A garbage phase start
  final int FINALIZE = 6;  // During last garbage.

  public static String[] PhaseInfo = {"init", "free",
                                      "run", "commit", "rollback",
                                      "garbage", "finalize"};

  final int Kb = 1024;
  final int Mb = Kb * Kb;

  /**
   * Initializes the atomic storage component.
   * 
   * @param path
   * @throws IOException
   */
  void init(String path) throws IOException;
  
  /**
   * Searches for the property with the specified key in the specific Transaction
   * property list. If the key is not found in this property list, the Configuration
   * property list is then checked.
   * The method returns <code>null</code> if the property is not found.
   *
   * @param   key   the property key.
   * @return  the value corresponding to the specified key value.
   */
  String getProperty(String key);
  
  /**
   * Searches for the property with the specified key in the specific Transaction
   * property list. If the key is not found in this property list, the Configuration
   * property list is then checked. The method returns the default value argument
   * if the property is not found.
   *
   * @param   key            the property key.
   * @param   defaultValue   a default value.
   *
   * @return  the value corresponding to the specified key value.
   */
  String getProperty(String key, String defaultValue);
  
  /**
   * Determines the integer value of the property with the specified name.
   * 
   * @param key
   *          property name.
   * @return the Integer value of the property.
   */
  Integer getInteger(String key);

  /**
   * Determines the integer value of the property with the specified name.
   * 
   * @param key
   *          property name.
   * @param value
   *          a default value.
   * @return the Integer value of the property.
   */
  Integer getInteger(String key, int value);
  
  /**
   * Returns <code>true</code> if and only if the corresponding property exists
   * and is equal to the string {@code "true"}.
   *
   * @param   name   the property name.
   * @return  the <code>boolean</code> value of the property.
   */
  boolean getBoolean(String key);

  /**
   * Returns the transaction state.
   * @return the transaction state.
   */
  int getPhase();
  /**
   * Returns a string representation of the transaction state.
   * @return the string representation of the transaction state.
   */
  String getPhaseInfo();

  /**
   *  Start a transaction validation, the validation phase needs 3 phases: begin, commit
   * and release. The begin ensure the mutual exclusion of the current transaction.
   */
  void begin() throws IOException;

  /**
   * Returns an array of strings naming the objects in the component started by this prefix.
   * 
   * @param prefix
   * @return an array of strings naming the objects in the component started by this prefix.
   */
  String[] getList(String prefix);
  /**
   * Returns true if the component is persistent.
   * @return true if the component is persistent.
   */
  boolean isPersistent();

  /**
   * Register the state of a newly created object in the current transaction.
   * 
   * @param obj   the object to store.
   * @param name  the name of the object.
   */
  void create(Serializable obj, String name) throws IOException;
  /**
   * Register the state of a newly created object in the current transaction.
   * 
   * @param obj     the object to store.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   */
  void create(Serializable obj, String dirName, String name) throws IOException;
  /**
   * Register the modified state of an object in the current transaction.
   * 
   * @param obj     the object to store.
   * @param name    the name of the object.
   */
  void save(Serializable obj, String name) throws IOException;
  /**
   * Register the modified state of an object in the current transaction.
   * 
   * @param obj     the object to store.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   */
  void save(Serializable obj, String dirName, String name) throws IOException;
  /**
   * Register the state of an object in the current transaction.
   * 
   * @param obj     the object to store.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   * @param first   the object is a new one.
   */
  void save(Serializable obj, String dirName, String name, boolean first) throws IOException;
  
  /**
   * Register a new byte array in the current transaction.
   * 
   * @param buf   the byte array to store.
   * @param name  the name of the object.
   */
  void createByteArray(byte[] buf, String name) throws IOException;  
  /**
   * Register a new byte array in the current transaction.
   * 
   * @param buf     the byte array to store.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   */
  void createByteArray(byte[] buf, String dirName, String name) throws IOException;
  /**
   * Register a modified byte array in the current transaction.
   * 
   * @param buf     the byte array to store.
   * @param name    the name of the object.
   */
  void saveByteArray(byte[] buf, String name) throws IOException;
  /**
   * Register a modified byte array in the current transaction.
   * 
   * @param buf     the byte array to store.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   */
  void saveByteArray(byte[] buf, String dirName, String name) throws IOException;
  /**
   * Register a modified byte array in the current transaction.
   * 
   * @param buf     the byte array to store.
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   * @param copy    the byte array can be modified, copy it.
   * @param first   the object is a new one.
   */
  void saveByteArray(byte[] buf, String dirName, String name, boolean copy, boolean first) throws IOException;
  
  /**
   * Load the specified object.
   * 
   * @param name    the name of the object.
   * @return the loaded object.
   */
  Object load(String name) throws IOException, ClassNotFoundException;
  /**
   * Load the specified object.
   * 
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   * @return the loaded object.
   */
  Object load(String dirName, String name) throws IOException, ClassNotFoundException;
  /**
   * Load the specified byte array.
   * 
   * @param name    the name of the object.
   * @return the loaded byte array.
   */
  byte[] loadByteArray(String name) throws IOException, ClassNotFoundException;
  /**
   * Load the specified byte array.
   * 
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   * @return the loaded byte array.
   */
  byte[] loadByteArray(String dirName, String name) throws IOException;
  /**
   * Deletes the specified object.
   * 
   * @param name    the name of the object.
   */
  void delete(String name);
  /**
   * Deletes the specified object.
   * 
   * @param dirName the directory name of the object.
   * @param name    the name of the object.
   */
  void delete(String dirName, String name);

  /**
   * Commit the current transaction.
   * 
   * @param release if true releases the transaction at the end of the commit.
   */
  void commit(boolean release) throws IOException;
  
  // JORAM_PERF_BRANCH
  void commit(Runnable beforeRelease) throws IOException;
  
  /**
   * Release the mutual exclusion.
   */
  void release() throws IOException;

  /**
   * Stops the transaction module.
   * It waits all transactions termination, then the module is kept
   * in a FREE 'ready to use' state.
   */
  void stop();
  
  /**
   * Close the transaction module.
   * It waits all transactions termination, the module will be initialized
   * anew before reusing it.
   */
  void close();
}
