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
package fr.dyade.aaa.agent;

import java.io.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.EmptyQueueException;

/**
 * Class <code>MessageQueue</code> represents a First-In-First-Out (FIFO)
 * persistent list of Message (source and target agent identifier,
 * notification). It realizes the target end of the communication mechanism.
 * As messages  have a relatively short life span, then the FIFO list of
 * messages is kept in main memory. The list is backed by a persistent image
 * on the disk for reliability needs.<p><hr>
 * Use of stamp information in Message in order to restore the queue from
 * persistent storage at initialization time, so there is no longer need 
 * to save <code>MessageQueue</code> object state.
 */
final class MessageQueue {
  private Logger logmon = null;
  private String logmsg = null;
  private long cpt1, cpt2;

  /**
   * The buffer into which the <code>Message</code> objects are stored
   * in memory.
   */
  private MessageVector data;
  /**
   * The number of validated message in the circular buffer.
   */
  private int validated;

  MessageQueue(String name, boolean persistent) {
    logmon = Debug.getLogger(getClass().getName());
    logmsg = name + ".MessageQueue: ";

    data = new MessageVector(name, persistent);
    validated = 0;
  }

  /**
   * Insert a message in the queue, it should only be used during
   * initialization for restoring the queue state.
   */
  synchronized void insert(Message item) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "insert(" + item + ")");

    int i = 0;
    for (; i<validated; i++) {
      Message msg = data.getMessageAt(i);
      if (item.getStamp() < msg.getStamp()) break;
    }
    data.insertMessageAt(item, i);
    validated += 1;
  }

  /**
   * Pushes a message onto the bottom of this queue. It should only
   * be used during a transaction. The item will be really available
   * after the transaction commit and the queue validate.
   *
   * @param   item   the message to be pushed onto this queue.
   */
  synchronized void push(Message item) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "push(" + item + ")");
    data.addMessage(item);
  }

  /**
   *  Removes the message at the top of this queue. It should only
   * be used during a transaction.
   *
   * @return     The message at the top of this queue.
   * @exception  EmptyQueueException if this queue is empty.
   */
  synchronized Message pop() throws EmptyQueueException {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "pop()");

    if (validated == 0)
      throw new EmptyQueueException();
    
    Message item = data.getMessageAt(0);
    data.removeMessageAt(0);
    validated -= 1;

    return item;
  }

  /**
   * Atomicaly validates all messages pushed in queue during a reaction.
   * It must only be used during a transaction.
   */
  synchronized void validate() {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "validate()");
    validated = data.size();
    notify();
  }

  /**
   * Atomicaly invalidates all messages pushed in queue during a reaction.
   * It must be used during a transaction.
   */
  synchronized void invalidate() {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "invalidate()");
    while (validated != data.size())
      data.removeMessageAt(validated);
  }

  /**
   * Looks at the message at the top of this queue without removing
   * it from the queue. It should never be used during a transaction
   * to avoid dead-lock problems.
   *
   * @return    the message at the top of this queue. 
   * @exception	InterruptedException if another thread has interrupted the
   *		current thread.
   */
  synchronized Message get() throws InterruptedException {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "get()");

    cpt1 += 1; cpt2 += validated;
    if ((cpt1 & 0xFFFFL) == 0L) {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, logmsg + (cpt2/cpt1) + '/' + validated);
      }
    }
    
    while (validated == 0) {
      wait();
    }
    Message item = data.getMessageAt(0);
 
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "get() -> " + item);

   return item;
  }

  /**
   * Looks at the message at the top of this queue without removing
   * it from the queue. It should never be used during a transaction
   * to avoid dead-lock problems. It waits until a message is available
   * or the specified amount of time has elapsed.
   *
   * @param	timeout	the maximum time to wait in milliseconds.
   * @return    	the message at the top of this queue. 
   * @exception	InterruptedException if another thread has interrupted the
   *		current thread.
   * @exception	IllegalArgumentException if the value of timeout is negative.
   */
  synchronized Message get(long timeout) throws InterruptedException {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "get(" + timeout + ")");

    cpt1 += 1; cpt2 += validated;
    if ((cpt1 & 0xFFFFL) == 0L) {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, logmsg + (cpt2/cpt1) + '/' + validated);
      }
    }
    
    Message item = null;
    if ((validated == 0) && (timeout > 0)) wait(timeout);
    if (validated > 0) item = data.getMessageAt(0);

    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "get() -> " + item);

    return item;
  }

  /**
   * Returns the number of messages in this <code>MessageQueue</code>
   * object. Be careful, the result includes messages to be validated.
   *
   * @return the size of the MessageQueue
   */
  synchronized int size(){
    return data.size();
  }

  /**
   * Returns a string representation of this <code>MessageQueue</code>
   * object. Be careful we scan the vector without synchronization, so the
   * result can be incoherent.
   *
   * @return	A string representation of this object. 
   */
  public final String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append('(').append(super.toString()).append(',');
    strbuf.append(validated).append(',');
    for (int i=0; i<data.size(); i++) {
      strbuf.append(data.getMessageAt(i));
    }
    strbuf.append(')');
    
    return strbuf.toString();
  }
}
