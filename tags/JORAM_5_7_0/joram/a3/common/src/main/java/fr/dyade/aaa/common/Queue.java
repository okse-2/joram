/*
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.common;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;


/**
 * The <code>Queue</code> class implements a First-In-First-Out 
 * (FIFO) list of objects.
 * <p>
 * A queue is for the exclusive use of one single consumer, whereas many
 * producers may access it. It is ready for use after instantiation. A
 * producer may wait for the queue to be empty by calling the
 * <code>stop()</code> method. This method returns when the queue is
 * actually empty, and prohibits any further call to the <code>push</code>
 * method. To be able to use the queue again, it must be re-started through
 * the <code>start()</code> method.
 */
public class Queue implements Serializable {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * The list holding queue elements.
   */
  private List elements = new Vector();

  /**
   * <code>true</code> if a producer called the <code>stop()</code>
   * method.
   */
  private boolean stopping;

  /**
   * <code>true</code> if the queue has been closed.
   */
  private boolean closed;

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

    elements.add(item);
    notify();
  }

  /**
   * Waits for an object to be pushed in the queue, and eventually returns
   * it without removing it.
   *
   * @return  The object at the top of this queue. 
   */
  public synchronized Object get() throws InterruptedException {
    while (size() == 0 && !closed)
      wait();
    if (closed)
      throw new InterruptedException();

    return elements.get(0);
  }

  /**
   * Removes and returns the object at the top of this queue.
   *
   * @return  The object at the top of this queue.
   * @exception  EmptyQueueException  If the queue is empty.
   */
  public synchronized Object pop() {
    if (size() == 0)
      throw new EmptyQueueException();
    
    Object obj = elements.get(0);
    elements.remove(0);

    if (stopping && size() == 0)
      notify();

    return obj;
  }
  
  /**
   * Waits for an object to be pushed in the queue, then removes and returns
   * the object at the top of this queue.
   *
   * @return  The object at the top of this queue. 
   */
  public synchronized Object getAndPop() throws InterruptedException {
    while (size() == 0 && !closed)
      wait();
    if (closed)
      throw new InterruptedException();

    Object obj = elements.get(0);
    elements.remove(0);

    if (stopping && size() == 0)
      notify();

    return obj;
  }

  /** Authorizes the use of the queue by producers. */
  public void start() {
    stopping = false;
    closed = false;
  }

  /**
   * Stops the queue by returning when it is empty and prohibiting any
   * further producers call to the <code>push</code> method.
   */
  public synchronized void stop() throws InterruptedException {
    stopping = true;
    if (size() != 0) wait();
  }

  /**
   * Closes the queue. Interrupts all threads blocked on {@link #get()} with an
   * {@link InterruptedException}.
   */
  public synchronized void close() {
    stopping = true;
    closed = true;
    notifyAll();
  }

  /**
   * Returns true if this queue contains no elements.
   */
  public boolean isEmpty() {
    return elements.isEmpty();
  }

  /**
   * Removes all of the elements from this queue.
   */
  public void clear() {
    elements.clear();
  }

  /**
   * Returns the number of elements in this list.
   */
  public int size() {
    return elements.size();
  }

}
