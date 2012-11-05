/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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

import java.io.*;

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

  void init(String path) throws IOException;

  /**
   *
   */
  public int getPhase();

  public String getPhaseInfo();

  void begin() throws IOException;

  File getDir();
  String[] getList(String prefix);

  boolean isPersistent();

  void create(Serializable obj, String name) throws IOException;
  void save(Serializable obj, String name) throws IOException;
  void saveByteArray(byte[] buf, String name) throws IOException;
  Object load(String name) throws IOException, ClassNotFoundException;
  byte[] loadByteArray(String name) throws IOException, ClassNotFoundException;
  void delete(String name);

  void create(Serializable obj, String dirName, String name) throws IOException;
  void save(Serializable obj, String dirName, String name) throws IOException;
  void saveByteArray(byte[] buf, String dirName, String name) throws IOException;
  Object load(String dirName, String name) throws IOException, ClassNotFoundException;
  byte[] loadByteArray(String dirName, String name) throws IOException;
  void delete(String dirName, String name);

  void commit(boolean release) throws IOException;
//   void rollback() throws IOException;

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
