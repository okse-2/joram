/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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

public class NullTransaction implements Transaction {
  // State of the transaction monitor.
  private int phase;

  static private final int INIT = 0;	  // Initialization state
  static private final int FREE = 1;	  // No transaction 
  static private final int RUN = 2;	  // A transaction is running
  static private final int COMMIT = 3;	  // A transaction is commiting
  static private final int ROLLBACK = 4;  // A transaction is aborting
  static private final int GARBAGE = 5;	  // A garbage phase start
  static private final int FINALIZE = 6;  // During last garbage.

  private final void setPhase(int newPhase) {
    phase = newPhase;
  }

  public NullTransaction() {}

  public boolean isPersistent() {
    return false;
  }

  public void init(String path) throws IOException {
    phase = INIT;

    /* The Transaction subsystem is ready */
    setPhase(FREE);
  }

  public File getDir() {
    return null;
  }

  public synchronized void begin() throws IOException {
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
    return new String[0];
  }

  public void save(Serializable obj, String name) throws IOException {}
  
  public void saveByteArray(byte[] buf, String name) throws IOException {}

  public Object load(String name) throws IOException, ClassNotFoundException {
    return null;
  }
  
  public byte[] loadByteArray(String name) throws IOException, ClassNotFoundException {
    return null;
  }

  public void delete(String name) {}

  public void save(Serializable obj, String dirName, String name) throws IOException {}
  
  public void saveByteArray(byte[] buf, String dirName, String name) throws IOException {}

  public Object load(String dirName, String name) throws IOException, ClassNotFoundException {
    return null;
  }
  
  public byte[] loadByteArray(String dirName, String name) throws IOException {
    return null;
  }

  public void delete(String dirName, String name) {}

  public void commit() throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Can not commit.");

    setPhase(COMMIT);
  }

  public void rollback() throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Can not rollback.");

    setPhase(ROLLBACK);
  }

  public synchronized void release() throws IOException {
    if ((phase != COMMIT) && (phase != ROLLBACK))
      throw new IllegalStateException("Can not release transaction.");

    // Change the transaction state.
    setPhase(FREE);
    // wake-up an eventually user's thread in begin
    notify();
  }

  public synchronized final void stop() {
    while (phase != FREE) {
      // Wait for the transaction subsystem to be free
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }
    // Change the transaction state.
    setPhase(FINALIZE);
  }
}
