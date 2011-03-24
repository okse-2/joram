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
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 * Class <code>MessageQueue</code> represents a First-In-First-Out (FIFO)
 * persistent list of Message (source and target agent identifier,
 * notification). It realizes the target end of the communication mechanism.
 * As messages  have a relatively short life span, then the FIFO list of
 * messages is kept in main memory. The list is backed by a persistent image
 * on the disk.<p><hr>
 * Use of stamp information in Message in order to restore the queue from
 * persistent storage at initialization time, so there is no longer need 
 * to save <code>MessageQueue</code> object state.
 */
public final class MessageQueue {
  private Logger logmon = null;
  private String logmsg = null;
  private long cpt1, cpt2;

  /**
   * The <code>Vector</code> into which <code>Message</code> objects are
   * stored in memory.
   */
  private Vector data;
  /** last validated message: last <= data.size(); */
  private int last;

  MessageQueue() {
    data = new Vector();
    last = 0;
    logmon = Debug.getLogger(getClass().getName());
    logmsg = "MessageQueue.#" + hashCode() + ": ";
  }

  /**
   * Insert a message in the queue, it should only be used during
   * initialization for restoring the queue state.
   */
  void insert(Message item) {
    int i = 0;
    for (; i<last; i++) {
      Message msg = (Message) data.elementAt(i);
      if (item.getStamp() < msg.getStamp()) break;
    }
    data.insertElementAt(item, i);
    last += 1;
  }

  /**
   * Pushes a message onto the bottom of this queue. It should only
   * be used during a transaction. The item will be really available
   * after the transaction commit and the queue validate.
   *
   * @param   item   the message to be pushed onto this queue.
   */
  synchronized void push(Message item) {
    data.addElement(item);
  }

  /**
   *  Removes the message at the top of this queue and returns that 
   * message as the value of this function. It should only be used
   * during a transaction.
   *
   * @return     The message at the top of this queue.
   * @exception  EmptyQueueException if this queue is empty.
   */
  synchronized Message pop() throws EmptyQueueException {
    Message item;
    
    if (last == 0)
      throw new EmptyQueueException();
    
    item = (Message) data.elementAt(0);
    data.removeElementAt(0);
    last -= 1;

    return item;
  }

  
  /**
   * Atomicaly validates all messages pushed in queue during a reaction.
   * It must only be used during a transaction.
   */
  synchronized void validate() {
    last = data.size();
    notify();
  }

  /**
   * Atomicaly invalidates all messages pushed in queue during a reaction.
   * It must be used during a transaction.
   */
  synchronized void invalidate() {
    while (last != data.size())
      data.removeElementAt(last);
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
    cpt1 += 1; cpt2 += last;
    if ((cpt1 & 0xFFFFL) == 0L) {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG,
                   logmsg + (cpt2/cpt1) + '/' + last);
      }
    }
    
    while (last == 0) {
      wait();
    }
    return (Message) data.elementAt(0);
  }

  synchronized Message get(long timeout) throws InterruptedException {
    cpt1 += 1; cpt2 += last;
    if ((cpt1 & 0xFFFFL) == 0L) {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG,
                   logmsg + (cpt2/cpt1) + '/' + last);
      }
    }
    
    if ((last == 0) && (timeout > 0)) wait(timeout);
    if (last > 0)
      return (Message) data.elementAt(0);

    return null;
  }

  /**
   * Returns the number of messages in this <code>MessageQueue</code>
   * object. Be careful, the result includes messages to be validated.
   *
   * @return the size of the MessageQueue
   */
  synchronized int size(){
    // AF: May be the result should be last...
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

    strbuf.append("(");
    for (int i=0; i<last; i++) {
      strbuf.append((Message) data.elementAt(i));
    }
    strbuf.append(")");
    
    return strbuf.toString();
  }
}