/*
 * Copyright (C) 2001 - 2013 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

import fr.dyade.aaa.common.EmptyQueueException;

/**
 * Interface <code>MessageQueue</code> represents a First-In-First-Out (FIFO)
 * persistent list of Message (source and target agent identifier,
 * notification).
 */
interface MessageQueue {
  /**
   * Insert a message in the queue, it should only be used during
   * initialization for restoring the queue state.
   *
   * @param	item   the message to be pushed onto this queue.
   */
  public void insert(Message item);

  /**
   * Pushes a message onto the bottom of this queue. It should only
   * be used during a transaction. The item will be really available
   * after the transaction commit and the queue validate.
   *
   * @param   item   the message to be pushed onto this queue.
   */
  public void push(Message item);
  
  /**
   * Pushes a message and validates it at the same time.
   * 
   * @param   item   the message to be pushed and validated
   */
  public void pushAndValidate(Message item);

  /**
   * Removes the message at the top of this queue.
   * It must only be used during a transaction.
   *
   * @return     The message at the top of this queue.
   * @exception  EmptyQueueException if this queue is empty.
   */
  public Message pop() throws EmptyQueueException;

  /**
   * Atomically validates all messages pushed in queue during a reaction.
   * It must only be used during a transaction.
   */
  public void validate();

  /**
   * Looks at the message at the top of this queue without removing
   * it from the queue.
   * It should never be used during a transaction to avoid dead-lock
   * problems.
   *
   * @return    the message at the top of this queue. 
   * @exception	InterruptedException if another thread has interrupted the
   *		current thread.
   */
  public Message get() throws InterruptedException ;

  /**
   * Looks at the message at the top of this queue without removing
   * it from the queue. It waits until a message is available or the
   * specified amount of time has elapsed.
   * It should never be used during a transaction to avoid dead-lock
   * problems.
   *
   * @param	timeout	the maximum time to wait in milliseconds.
   * @return    	the message at the top of this queue. 
   * @exception	InterruptedException if another thread has interrupted the
   *		current thread.
   * @exception	IllegalArgumentException if the value of timeout is negative.
   */
  public Message get(long timeout) throws InterruptedException;

  /**
   * Returns the number of messages in this <code>MessageQueue</code>
   * object. Be careful, the result includes messages to be validated.
   *
   * @return the size of the MessageQueue
   */
  public int size();
  
  /**
   * Returns a report about the distribution of messages type in queue.
   */
  public String report();
}
