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
package fr.dyade.aaa.util;

import java.io.*;
import java.util.*;

/**
 * The <code>Queue</code> class implements a First-In-First-Out 
 * (FIFO) list of objects.
 * <p>
 * A queue is for the exclusive use of one single consumer, whereas many
 * producers may access it. It is ready for use after instanciation. A
 * producer may wait for the queue to be empty by calling the
 * <code>stop()</code> method. This method returns when the queue is
 * actually empty, and prohibitis any further call to the <code>push</code>
 * method. To be able to use the queue again, it must be re-started through
 * the <code>start()</code> method.
 */
public class Queue extends Vector {
  /**
   * <code>true</code> if a producer called the <code>stop()</code>
   * method.
   */
  private boolean stopping;

  /**
   * Constructs a <code>Queue</code> instance.
   */
  public Queue() {
    super();
    start();
  }


  /**
   * Pushes an item at the end of this queue. 
   *
   * @param item  The item to be pushed at the end of this queue.
   * @exception  StoppedQueueException  If the queue is stopping or stopped.
   */
  public synchronized void push(Object item) {
    if (stopping)
      throw new StoppedQueueException();

    addElement(item);
    notify();
  }


  /**
   * Removes and returns the object at the top of this queue.
   *
   * @return  The object at the top of this queue.
   * @exception  EmptyQueueException  If the queue is empty.
   */
  public synchronized Object pop() {
    Object obj;
    
    if (size() == 0)
      throw new EmptyQueueException();
    
    obj =elementAt(0);
    removeElementAt(0);

    if (stopping && size() == 0)
      notify();

    return obj;
  }


  /**
   * Waits for an object to be pushed in the queue, and eventually returns
   * it without removing it.
   *
   * @return  The object at the top of this queue. 
   */
  public synchronized Object get() throws InterruptedException {
    while (size() == 0)
      wait();

    return elementAt(0);
  }


  /** Authorizes the use of the queue by producers. */
  public void start() {
    stopping = false;
  }


  /**
   * Stops the queue by returning when it is empty and prohibiting any
   * further producers call to the <code>push</code> method.
   */
  public synchronized void stop() throws InterruptedException {
    stopping = true;
    if (size() != 0) wait();
  }
}
