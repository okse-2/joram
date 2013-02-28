/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.ext;

import java.io.IOException;

import fr.dyade.aaa.util.TransactionMBean;

/**
 *
 */
public interface NGAsyncTransactionMBean extends TransactionMBean {

  /**
   * Returns the initial capacity of global in memory log (by default 4096).
   *
   * @return The initial capacity of global in memory log.
   */
  int getLogMemoryCapacity();

  /**
   * Returns the number of operation in the memory log.
   *
   * @return The number of operation in the memory log.
   */
  public int getLogMemorySize();

  /**
   * Returns the maximum size of disk log in Mb, by default 16Mb.
   *
   * @return The maximum size of disk log in Mb.
   */
  int getMaxLogFileSize();

  /**
   * Sets the maximum size of disk log in Mb.
   *
   * @param size The maximum size of disk log in Mb.
   */
  void setMaxLogFileSize(int size);

  /**
   * Returns the size of disk log in Mb.
   *
   * @return The size of disk log in Mb.
   */
  int getLogFileSize();

  /**
   * Returns the number of rolled log files.
   * 
   * @return The number of rolled log files.
   */
  int getNbLogFiles();
  
  /**
   * Returns true if every write in the log file is synced to disk.
   * @return true if every write in the log file is synced to disk.
   */
   boolean isSyncOnWrite();

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
   * Returns the number of load operation from a log file since last start.
   * 
   * @return The number of load operation from a log file.
   */
  public int getNbLoadedFromLog();

  /**
   * Returns the cumulated time of garbage operations.
   *
   * @return The cumulated time of garbage operations.
   */
  public long getGarbageTime();

  /**
   * Returns the ratio of garbage operations since starting up.
   *
   * @return The ratio of garbage operations since starting up.
   */
  public int getGarbageRatio();

//  /**
//   * Returns the status of the garbage thread.
//   *
//   * @return The status of the garbage thread.
//   */
//  boolean isGarbageRunning();
//
//  /**
//   * Garbage the log file.
//   */
//  public void garbage();


  public String getRepositoryImpl();

  /**
   * Returns the number of save operation to repository.
   *
   * @return The number of save operation to repository.
   */
  public int getNbSavedObjects();

  /**
   * Returns the number of delete operation on repository.
   *
   * @return The number of delete operation on repository.
   */
  public int getNbDeletedObjects();

  /**
   * Returns the number of useless delete operation on repository.
   *
   * @return The number of useless delete operation on repository.
   */
  public int getNbBadDeletedObjects();

  /**
   * Returns the number of load operation from repository.
   *
   * @return The number of load operation from repository.
   */
  public int getNbLoadedObjects();
  
  public String logCounters();
  
  public String logContent(int idx) throws IOException;
  
  /* Not permitted
  public void garbage(int idx) throws IOException;
  */
}
