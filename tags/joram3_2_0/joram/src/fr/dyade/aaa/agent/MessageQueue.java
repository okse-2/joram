/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package fr.dyade.aaa.agent;

import java.io.*;
import java.util.*;
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
 *
 * @author  Andre Freyssinet
 */
public final class MessageQueue {
  /** RCS version number of this file: $Revision: 1.10 $ */
  public static final String RCS_VERSION="@(#)$Id: MessageQueue.java,v 1.10 2002-10-21 08:41:13 maistrfr Exp $";

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
  }

  /**
   * Insert a message in the queue, it should only be used during
   * initialization for restoring the queue state.
   */
  void insert(Message item) {
    int i = 0;
    for (; i<last; i++) {
      Message msg = (Message) data.elementAt(i);
      if (item.update.stamp < msg.update.stamp) break;
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
    while (last == 0) {
      wait();
    }
    return (Message) data.elementAt(0);
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
