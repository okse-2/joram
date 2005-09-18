/*
 * Copyright (C) 2005 ScalAgent Distributed Technologies
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

public interface NTransactionMBean extends TransactionMBean {
  /**
   * Returns the initial capacity of global in memory log (by default 4096).
   *
   * @return The initial capacity of global in memory log.
   */
  int getLogMemoryCapacity();

  /**
   * Returns the size of disk log in Mb, by default 16Mb.
   *
   * @return The size of disk log in Mb.
   */
  int getLogFileSize();

  /**
   * Sets the size of disk log in Mb.
   *
   * @param size The size of disk log in Mb.
   */
  void setLogFileSize(int size);

  /**
   * Returns the pool size for <code>operation</code> objects, by default 1000.
   *
   * @return The pool size for <code>operation</code> objects.
   */
  int getLogThresholdOperation();

  /**
   * Returns the number of commit operation since starting up.
   *
   * @return The number of commit operation.
   */
  public int getCommitCount();

  /**
   * Returns the number of garbage operation since starting up.
   *
   * @return The number of garbage operation.
   */
  public int getGarbageCount();

  /**
   *
   */
  public int getPhase();

  public String getPhaseInfo();
}
