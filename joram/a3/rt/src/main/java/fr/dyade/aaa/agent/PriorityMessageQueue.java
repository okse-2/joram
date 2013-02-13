/*
 * Copyright (C) 2004 - 2011 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

import java.util.LinkedList;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.EmptyQueueException;

//JORAM_PERF_BRANCH
final class PriorityMessageQueue implements MessageQueue {
  private Logger logmon = null;
  private String logmsg = null;
  private long cpt1, cpt2;

  private LinkedList data;

  private int validated;
  
  private boolean persistent;

  PriorityMessageQueue(String name, boolean persistent) {
    this.persistent = persistent;
    logmon = Debug.getLogger(getClass().getName() + '.' + name);
    logmsg = name + ".PriorityMessageQueue: ";
    data = new LinkedList();
    validated = 0;
  }

  /**
   * Insert a message in the queue, it should only be used during
   * initialization for restoring the queue state.
   *
   * @param	item   the message to be pushed onto this queue.
   */
  public synchronized void insert(Message item) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "insert(" + item + ")");

    int i = 0;
    for (; i<validated; i++) {
      Message msg = getMessageAt(i);
      if (item.getStamp() < msg.getStamp()) break;
    }
    insertMessageAt(item, i, false);
    validated += 1;
  }
  
  int highPriorityCount = 0;

  public int getHighPriorityCount() {
    return highPriorityCount;
  }

  /**
   * Pushes a message onto the bottom of this queue. It should only
   * be used during a transaction. The item will be really available
   * after the transaction commit and the queue validate.
   *
   * @param   item   the message to be pushed onto this queue.
   */
  public synchronized void push(Message item) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "push(" + item + ")");
    int priority;
    if (item.not != null) {
      priority = item.not.priority;
    } else {
      priority = 0;
    }
    if (priority == 0) {
      logmon.log(BasicLevel.DEBUG, "push LP: " + data.size() + " HP: " + highPriorityCount);
      if (persistent) {
        data.add(new MessageSoftRef(item));
      } else {
        data.add(item);
      }
    } else {
      logmon.log(BasicLevel.DEBUG, "push HP: " + highPriorityCount + " LP: " + data.size());
      insertMessageAt(item, highPriorityCount, true);
    }
  }

  /**
   * Removes the message at the top of this queue.
   * It must only be used during a transaction.
   *
   * @return     The message at the top of this queue.
   * @exception  EmptyQueueException if this queue is empty.
   */
  public synchronized Message pop() throws EmptyQueueException {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "pop()");

    if (validated == 0)
      throw new EmptyQueueException();
    
    int indexToRemove;
    if (lastGetPriority == 0) {
      indexToRemove = highPriorityCount;
    } else {
      indexToRemove = 0;
    }
    
    Message item = getMessageAt(indexToRemove);
    logmon.log(BasicLevel.DEBUG, logmsg + "pop: " + item);
    removeMessageAt(indexToRemove);
    validated -= 1;
    logmon.log(BasicLevel.DEBUG, logmsg + "data=" + data);
    return item;
  }

  /**
   * Atomically validates all messages pushed in queue during a reaction.
   * It must only be used during a transaction.
   */
  public synchronized void validate() {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "validate()");
    validated = size();
    notify();
  }

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
  public synchronized Message get() throws InterruptedException {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, logmsg + "get()");

      cpt1 += 1; cpt2 += validated;
      if ((cpt1 & 0xFFFFL) == 0L) {
          logmon.log(BasicLevel.DEBUG, logmsg + (cpt2/cpt1) + '/' + validated);
      }
    }
    
    while (validated == 0) {
      wait();
    }
    Message item = getMessageAt(0);
 
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "get() -> " + item);
    lastGetPriority = item.not.priority;
   return item;
  }
  
  private int lastGetPriority;

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
  public synchronized Message get(long timeout) throws InterruptedException {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, logmsg + "get(" + timeout + ")");

      cpt1 += 1; cpt2 += validated;
      if ((cpt1 & 0xFFFFL) == 0L) {
        logmon.log(BasicLevel.DEBUG, logmsg + (cpt2/cpt1) + '/' + validated);
      }
    }
    
    Message item = null;
    if ((validated == 0) && (timeout > 0)) wait(timeout);
    if (validated > 0) item = getMessageAt(0);

    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "get() -> " + item);
    lastGetPriority = item.not.priority;
    return item;
  }

  /**
   * Looks at the first message of this queue where the destination server
   * is the specified one.
   * The message is not removed from the queue. It should never be used during
   * a transaction to avoid dead-lock problems.
   *
   * @param	to	the unique server id.
   * @return    	the corresponding message or null if none . 
   */
  public synchronized Message getMessageTo(short to) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, logmsg + "getFrom(" + to + ")");

      cpt1 += 1; cpt2 += validated;
      if ((cpt1 & 0xFFFFL) == 0L) {
        logmon.log(BasicLevel.DEBUG, logmsg + (cpt2/cpt1) + '/' + validated);
      }
    }
    
    Message item = null;
    for (int i=0; i<validated; i++) {
      Message msg = getMessageAt(i);
      if (msg.getDest() == to) {
        item = msg;
        break;
      }
    }

    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "get() -> " + item);

    return item;
  }

  /**
   * Removes the specified message from the queue if exists.
   *
   * @param	msg	the message to remove.
   */
  synchronized void removeMessage(Message msg) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 logmsg + "removeMessage #" + msg.getStamp());

    for (int i = 0; i<validated; i++) {
      if (getMessageAt(i) ==  msg) {

        if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     logmsg + "removeMessage #" + msg.getStamp() + " -> " + i);

        removeMessageAt(i);
        validated -= 1;
        return;
      }
    }

    logmon.log(BasicLevel.ERROR,
               logmsg + "removeMessage #" + msg.getStamp() + " not found");

    return;
  }

  /**
   *  Removes all messages with a stamp less than the specified one.
   * Be careful with the use of this method, in particular it does not
   * take in account the multiples incoming nodes.
   */
  synchronized int remove(int stamp) {
    if (validated == 0) return 0;
    
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "remove #" + stamp);

    int i = 0;
    for (; i<validated; i++) {
      Message msg = getMessageAt(i);
      if (stamp < msg.getStamp()) break;
    }

    for (int j=0; j<i; j++) {
      removeMessageAt(0);
    }
    validated -= i;
    
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "remove #" + stamp + " ->" +i);

    return i;
  }

  /**
   *  Removes the first messages with a timestamp less than the specified one.
   * Be careful with the use of this method, in particular it does not take in
   * account the multiples incoming nodes.
   */
  synchronized Message removeExpired(long currentTimeMillis) {
    if (validated == 0) return null;
    
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 logmsg + "removeExpired - " + currentTimeMillis);

    for (int i = 0; i<validated; i++) {
      Message msg = getMessageAt(i);
      if ((msg.not != null) && 
          (msg.not.expiration > 0) &&
          (currentTimeMillis >= msg.not.expiration)) {
        removeMessageAt(i);
        validated -= 1;
    
        if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, logmsg + "remove #" + msg.getStamp());

        return msg;
      }
    }

    return null;
  }

  /**
   * Inserts the specified message to this <code>MessageVector</code> at
   * the specified index. Each component in this vector with an index greater
   * or equal to the specified index is shifted upward.
   *
   * @param item	the message to be pushed onto this queue.
   * @param index	where to insert the new message.
   */
  private void insertMessageAt(Message item, int index, boolean hp) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 logmsg + "insertMessageAt(" + item + ", " + index + ")");

    if (persistent) {
      data.add(index, new MessageSoftRef(item));
    } else {
      data.add(index, item);
    }
    if (hp) {
      highPriorityCount++;
    }

    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 logmsg + "insertMessageAt() -> " + this);
  }

  /**
   * Returns the message at the specified index.
   *
   * @param index	the index of the message.
   * @return     	The message at the top of this queue.
   */
  private Message getMessageAt(int index) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "getMessageAt(" + index + ")");
    logmon.log(BasicLevel.DEBUG, logmsg + "data=" + data);
    if (persistent) {
      return ((MessageSoftRef) data.get(index)).loadMessage();
    } else {
      return (Message) data.get(index);
    }
  }

  /**
   * Deletes the message at the specified index.
   *
   * @param index	the index of the message to remove.
   */
  private void removeMessageAt(int index) {
    Object o = data.remove(index);
    logmon.log(BasicLevel.DEBUG, "removed=" + o);
    if (index < highPriorityCount) {
      highPriorityCount -= 1;
    }
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 logmsg + "removeMessageAt(" + index + ") -> " + this);
  }

  /**
   * Returns the number of messages in this vector.
   *
   * @return  the number of messages in this vector.
   */
  public int size() {
    return data.size();
  }

  /**
   * Returns a string representation of this <code>MessageVector</code>
   * object. Be careful we scan the vector without synchronization, so the
   * result can be incoherent.
   *
   * @return	A string representation of this object. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();
    
    strbuf.append('(').append(super.toString());
    strbuf.append(",validated=").append(validated).append(",(");
    strbuf.append("))");
    
    return strbuf.toString();
  }

}
