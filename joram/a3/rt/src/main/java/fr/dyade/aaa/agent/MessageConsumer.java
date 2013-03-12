/*
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

import java.io.IOException;
/**
 * The parent interface for all messages consumers.
 * 
 * @see		Engine, Network.
 */
public interface MessageConsumer {
  /**
   * Returns this <code>MessageConsumer</code>'s name.
   *
   * @return this <code>MessageConsumer</code>'s name.
   */
  String getName();

  /**
   * Returns the corresponding domain's name.
   *
   * @return this domain's name.
   */
  String getDomainName();

  /**
   * Insert a message in the <code>MessageQueue</code>.
   * This method is used during initialization to restore the component
   * state from persistent storage.
   *
   * @param msg		the message
   */
  void insert(Message msg);

  /**
   * Saves logical clock information to persistent storage.
   */
  void save() throws IOException;

  /**
   * Restores logical clock information from persistent storage.
   */
  void restore() throws Exception;

  /**
   *  Adds a message in "ready to deliver" list. This method allocates a
   * new time stamp to the message ; be Careful, changing the stamp imply
   * the filename change too.
   */
  void post(Message msg) throws Exception;
  
  // JORAM_PERF_BRANCH
  void postAndValidate(Message msg) throws Exception;
  
  //JORAM_PERF_BRANCH
  void validate(Message msg) throws Exception;
  
  // JORAM_PERF_BRANCH
  void postAndSave(Message msg) throws Exception;

  /**
   * Validates all messages pushed in queue during transaction session.
   */
  void validate();

  /**
   * Causes this component to begin execution.
   *
   * @see stop
   */
  void start() throws Exception;

  /**
   * Forces the component to stop executing.
   *
   * @see start
   */
  void stop();

  /**
   * Deletes the component, removes all persistent data. The component
   * may have been previously stopped, and removed from MessageConsumer
   * list.
   * This operation use Transaction calls, you may use commit to validate it.
   *
   * @see fr.dyade.aaa.util.Transaction 
   */
  void delete() throws IllegalStateException;

  /**
   *  Get this consumer's <code>MessageQueue</code>. Use in administration and
   * debug tasks, should be replaced by a common attribute.
   *
   * @return this <code>MessageConsumer</code>'s queue.
   *
   *JORAM_PERF_BRANCH
  MessageQueue getQueue();*/

  /**
   *  Tests if the component is alive. A <code>MessageConsumer</code> is alive
   * if it has been started and has not yet stopped.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  boolean isRunning();
  
  /**
   * Returns the load averages for the last minute.
   * @return the load averages for the last minute.
   */
  public float getAverageLoad1();

  /**
   * Returns the load averages for the past 5 minutes.
   * @return the load averages for the past 5 minutes.
   */
  public float getAverageLoad5();
  
  /**
   * Returns the load averages for the past 15 minutes.
   * @return the load averages for the past 15 minutes.
   */
  public float getAverageLoad15();
}
