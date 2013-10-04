/*
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public final class NullTransaction extends BaseTransaction implements NullTransactionMBean {
  protected long startTime = 0L;

  /**
   * Returns the starting time.
   *
   * @return The starting time.
   */
  public long getStartTime() {
    return startTime;
  }
  
  // State of the transaction monitor.
  protected int phase;

  public final int getPhase() {
    return phase;
  }

  public final String getPhaseInfo() {
    return PhaseInfo[phase];
  }

  protected final void setPhase(int newPhase) {
    phase = newPhase;
  }

  public NullTransaction() {}

  /**
   * Tests if the Transaction component is persistent.
   *
   * @return false.
   */
  public boolean isPersistent() {
    return false;
  }

  public void init(String path) throws IOException {
    phase = INIT;

    startTime = System.currentTimeMillis();

    /* The Transaction subsystem is ready */
    setPhase(FREE);
  }

  public File getDir() {
    return null;
  }

  /**
   * Returns the path of persistence directory.
   *
   * @return null.
   */
  public String getPersistenceDir() {
    return null;
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
    return new String[0];
  }

  public final void create(Serializable obj, String name) throws IOException {}

  public final void create(Serializable obj,
                     String dirName, String name) throws IOException {}

  public final void save(Serializable obj, String name) throws IOException {}

  public final void save(Serializable obj,
                         String dirName, String name) throws IOException {}

  public void save(Serializable obj,
                   String dirName, String name,
                   boolean first) throws IOException {}
  
  public final void createByteArray(byte[] buf, String name) throws IOException {}
  
  public final void createByteArray(byte[] buf,
                                  String dirName, String name) throws IOException {}

  public final void saveByteArray(byte[] buf, String name) throws IOException {}
  
  public final void saveByteArray(byte[] buf,
                                  String dirName, String name) throws IOException {}

  public void saveByteArray(byte[] buf,
                            String dirName, String name,
                            boolean copy,
                            boolean first) throws IOException {}

  public final Object load(String name) throws IOException, ClassNotFoundException {
    return null;
  }
  
  public final Object load(String dirName, String name) throws IOException, ClassNotFoundException {
    return null;
  }

  public final byte[] loadByteArray(String name) throws IOException {
    return null;
  }
  
  public byte[] loadByteArray(String dirName, String name) throws IOException {
    return null;
  }
  
  public final void delete(String name) {}

  public void delete(String dirName, String name) {}

  public synchronized void commit(boolean release) throws IOException {
    if (phase != RUN)
      throw new IllegalStateException("Can not commit.");

    setPhase(COMMIT);
    if (release) {
      // Change the transaction state.
      setPhase(FREE);
      // wake-up an eventually user's thread in begin
      notify();
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

  public synchronized final void stop() {
    while (phase != FREE) {
      // Wait for the transaction subsystem to be free
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }
    // Change the transaction state.
    setPhase(FREE);
  }

  public synchronized final void close() {
    if (phase == INIT) return;

    while (phase != FREE) {
      // Wait for the transaction subsystem to be free
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }
    // Change the transaction state.
    setPhase(INIT);
  }
  
  /**
   * Indicates whether some operations have been done in
   * this transaction.
   */
  public boolean containsOperations() {
    return false;
  }
}
