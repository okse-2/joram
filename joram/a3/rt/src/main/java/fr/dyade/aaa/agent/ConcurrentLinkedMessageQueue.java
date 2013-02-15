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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.EmptyQueueException;

final class ConcurrentLinkedMessageQueue implements MessageQueue {
 
  private Logger logmon;
  
  private String logmsg;
  
  private ConcurrentLinkedQueue<Message> messages;
  
  private List<Message> notValidated;

  ConcurrentLinkedMessageQueue(String name) {
    logmon = Debug.getLogger(getClass().getName() + '.' + name);
    logmsg = name + ".MessageVector: ";
    messages = new ConcurrentLinkedQueue<Message>();
    notValidated = new ArrayList<Message>();
  }

  /**
   * Insert a message in the queue, it should only be used during
   * initialization for restoring the queue state.
   *
   * @param	item   the message to be pushed onto this queue.
   */
  public void insert(Message item) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "insert(" + item + ")");
    messages.add(item);
  }

  /**
   * Pushes a message onto the bottom of this queue. It should only
   * be used during a transaction. The item will be really available
   * after the transaction commit and the queue validate.
   *
   * @param   item   the message to be pushed onto this queue.
   */
  public void push(Message item) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "push(" + item + ")");
    notValidated.add(item);
  }
  
  // JORAM_PERF_BRANCH
  public void pushAndValidate(Message item) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "pushAndValidate(" + item + ")");
    messages.add(item);
  }

  /**
   * Removes the message at the top of this queue.
   * It must only be used during a transaction.
   *
   * @return     The message at the top of this queue.
   * @exception  EmptyQueueException if this queue is empty.
   */
  public Message pop() throws EmptyQueueException {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "pop()");
    return messages.poll();
  }

  /**
   * Atomically validates all messages pushed in queue during a reaction.
   * It must only be used during a transaction.
   */
  public void validate() {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, logmsg + "validate()");
    messages.addAll(notValidated);
    notValidated.clear();
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
  public Message get() throws InterruptedException {
    throw new RuntimeException("Not implemented");
  }

  public Message get(long timeout) throws InterruptedException {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Returns the number of messages in this vector.
   *
   * @return  the number of messages in this vector.
   */
  public int size() {
    return messages.size() + notValidated.size();
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
    strbuf.append(",messages=").append(messages);
    strbuf.append(",notValidated=").append(notValidated);
    strbuf.append("))");
    
    return strbuf.toString();
  }

}
