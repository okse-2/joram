/*
 * Copyright (C) 2004 - 2013 ScalAgent Distributed Technologies
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

import java.util.Enumeration;
import java.util.Hashtable;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.EmptyQueueException;

/**
 * Class <code>MessageVector</code> represents a persistent vector of
 * <tt>Message</tt> (source and target agent identifier, notification). 
 * As messages  have a relatively short life span, then the messages
 * are kept in main memory. If possible, the list is backed by a persistent
 * image on the disk for reliability needs. In this case, we can use
 * <tt>SoftReference</tt> to avoid memory overflow.<p><hr>
 * The stamp information in Message is used to restore the queue from
 * persistent storage at initialization time, so there is no longer need 
 * to save <code>MessageVector</code> object state.
 */
final class MessageVector implements MessageQueue {
  private Logger logmon = null;
  private String logmsg = null;
  private long cpt1, cpt2;

  /**
   * The array buffer into which the <code>Message</code> objects are stored
   * in memory. The capacity of this array buffer is at least large enough to
   * contain all the messages of the <code>MessageVector</code>.<p>
   * Messages are stored in a circular way, first one in <tt>data[first]</tt>
   * through <tt>data[(first+count-1)%length]</tt>. Any other array elements
   * are null.
   */
  private Object data[];
  /** The index of the first message in the circular buffer. */
  private int first;
  /**
   * The number of messages in this <tt>MessageVector</tt> object. Components
   * <tt>data[first]</tt> through <tt>data[(first+count-1)%length]</tt> are the
   * actual items.
   */
  private int count;
  /** The number of validated message in this <tt>MessageQueue</tt>. */
  private int validated;

  private boolean persistent;

  MessageVector(String name, boolean persistent) {
    logmon = Debug.getLogger(getClass().getName() + '.' + name);
    logmsg = name + ".MessageVector: ";
    
    if (msgTypesTracking)
      counters = new Hashtable<Class, Counter>();
    
    this.persistent = persistent;
    data = new Object[50];
    first = 0;
    count = 0;
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
    insertMessageAt(item, i);
    validated += 1;
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
    
    if (msgTypesTracking) inc(item.not);
    insertMessageAt(item, count);
  }
  
  /**
   * Pushes and validates a message. This method should not
   * be used during a transaction.
   *
   * @param   item   the message to be pushed and validated
   */
  public synchronized void pushAndValidate(Message item) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "pushAndValidate(" + item + ")");
    
    if (msgTypesTracking) inc(item.not);
    
    insertMessageAt(item, validated);
    validated += 1;
    
    notify();
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
    
    Message item = getMessageAt(0);
    if (msgTypesTracking) dec(item.not);
    removeMessageAt(0);
    validated -= 1;

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
  private void insertMessageAt(Message item, int index) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 logmsg + "insertMessageAt(" + item + ", " + index + ")");

    if (count == data.length) {
      Object newData[] = new Object[data.length *2];
      if ((first + count) < data.length) {
        System.arraycopy(data, first, newData, 0, count);
      } else {
        int j = data.length - first;
        System.arraycopy(data, first, newData, 0, j);
        System.arraycopy(data, 0, newData, j, count - j);
      }
      first = 0;
      data = newData;
    }
    if (index != count) {
      try {
        int srcPos = (first + index)%data.length;
        int destPos = (first + index + 1)%data.length;
        int copyLength = count - index;
        int lastDestPos = (destPos + copyLength - 1)%data.length;
        if (srcPos > lastDestPos) {
          Object lastElement = data[data.length - 1];
          int copyLength1 = data.length - srcPos -1;
          if (copyLength1 > 0) {
            System.arraycopy(data, srcPos, data, destPos, copyLength1);
          }
          int copyLength2 = copyLength - (copyLength1 + 1);
          if (copyLength2 > 0) {
            System.arraycopy(data, 0, data, 1, copyLength2);
          }
          data[0] = lastElement;
        } else {
          System.arraycopy(data, srcPos, data, destPos, copyLength);
        }
        
      } catch (ArrayIndexOutOfBoundsException exc) {
        throw new RuntimeException("insertMessageAt '" + index + "': " + this, exc);
      }
    }
    if (persistent)
      data[(first + index)%data.length] = new MessageSoftRef(item);
    else
      data[(first + index)%data.length] = item;
    count += 1;

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

    int idx = (first + index)%data.length;
    
    if (data[idx] == null) throw new RuntimeException("Null element in: " + this);
    
    if (persistent) {
      return ((MessageSoftRef) data[idx]).loadMessage();
    }
    
    return (Message) data[idx];
  }

  /**
   * Deletes the message at the specified index.
   *
   * @param index	the index of the message to remove.
   */
  private void removeMessageAt(int index) {
    if (index == 0) {
      // It is the first element, just move the start of the list.
      data[first] = null; /* let gc do its work */
      first = (first +1)%data.length;
    } else if (index == (count -1)) {
      // It is the last element, just move the end of the list.
      data[(first + index) %data.length] = null; /* let gc do its work */
    } else if ((first + index) < data.length) {
      // Moves the start of the box to the empty 'box'
      System.arraycopy(data, first,
                       data, first +1, index);
      // Erase the old first 'box'
      data[first] = null; /* let gc do its work */
      // Move the first ptr +1, and decrease counter
      first = (first +1)%data.length;
    } else {
      // Moves the end of the vector -1 to the empty 'box'
      System.arraycopy(data, (first + index)%data.length +1,
                       data, (first + index)%data.length, count - index -1);
      // Erase the old last 'box'
      data[(first + count -1)%data.length] = null; /* let gc do its work */
    }

    // Decrease the counter
    count -= 1;
    // If there is no more element, moves the empty list to the beginning of
    // the vector.
    if (count == 0) first = 0;

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
    return count;
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
    strbuf.append(",first=").append(first);
    strbuf.append(",count=").append(count);
    strbuf.append(",validated=").append(validated).append(",(");
    for (int i=0; i<data.length; i++) {
      strbuf.append(data[i]).append(',');
    }
    strbuf.append("))");
    
    return strbuf.toString();
  }

  /**
   *  Name of property allowing to track the distribution of the types of messages,
   * by default false. If true, for each type of messages the total number of messages
   * sent since the beginning and the number of those waiting is counted.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command or
   * a3servers.xml configuration file.
   */
  public static final String MSG_TYPES_TRACKING = "fr.dyade.aaa.agent.MsgTypesTracking";
  
  /**
   * True if the tracking of the distribution of messages type is allowed.
   */
  private static boolean msgTypesTracking = AgentServer.getBoolean(MSG_TYPES_TRACKING);

  static class Counter {
    int total = 1;
    int live = 1;
  }
  
  Hashtable<Class, Counter> counters = null;

  private void inc(Notification not) {
    if (not == null) return;
    
    Counter counter = counters.get(not.getClass());
    if (counter == null) {
      counters.put(not.getClass(), new Counter());
    } else {
      counter.total += 1;
      counter.live += 1;
    }
  }

  private void dec(Notification not) {
    if (not == null) return;
    
    Counter counter = counters.get(not.getClass());
    counter.live -= 1;
  }

  /**
   * Returns a report about the distribution of messages type in queue.
   */
  public String report() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append("waiting=").append(size()).append('\n');
    for (Enumeration<Class> e = counters.keys(); e.hasMoreElements();){ 
      Class clazz = e.nextElement();
      Counter counter = counters.get(clazz);
      strbuf.append(clazz.getName()).append('=').append(counter.live).append('/').append(counter.total).append('\n');
    }
    return strbuf.toString();
  }
}
