/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 CNES
 * Copyright (C) 2006 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s):
 */
#ifndef XQUEUE_H
#define XQUEUE_H

#include "Vector.H"
#include "Synchronized.H"
#include "XoramException.H"

class StoppedQueueException : public Exception {
 public:
  StoppedQueueException() : Exception() {}
  StoppedQueueException(char* msg) : Exception(msg) {}
};

class EmptyQueueException : public Exception {
 public:
  EmptyQueueException() : Exception() {}
  EmptyQueueException(char* msg) : Exception(msg) {}
};

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
template<class T> class XQueue : Synchronized {
 private:
  /**
   * <code>true</code> if a producer called the <code>stop()</code>
   * method.
   */
  boolean stopping;
  Vector<T>* queue;

 public:
  /**  Constructs a <code>Queue</code> instance. */
  XQueue() {
    queue = new Vector<T>();
    start();
  }

  /**
   * Pushes an item at the end of this queue. 
   *
   * @param item  The item to be pushed at the end of this queue.
   * @exception  StoppedQueueException  If the queue is stopping or stopped.
   */
  void push(T* item) {
    sync_begin();   
    if (stopping) throw new StoppedQueueException();
    queue->addElement(item);
    notify();
    sync_end();
  }

  /**
   * Removes and returns the object at the top of this queue.
   *
   * @return  The object at the top of this queue.
   * @exception  EmptyQueueException  If the queue is empty.
   */
  T* pop() {
    sync_begin();   
    T* obj;
    
    if (queue->size() == 0) throw new EmptyQueueException();
    obj = queue->removeElementAt(0);
    if (stopping && queue->size() == 0) notifyAll();
    sync_end();

    return obj;
  }

  /**
   * Waits for an object to be pushed in the queue, and eventually returns
   * it without removing it.
   *
   * @return  The object at the top of this queue. 
   */
   T* get() {
    sync_begin();   
    while (queue->size() == 0) wait();
    sync_end();

    return queue->elementAt(0);
  }

  /** Authorizes the use of the queue by producers. */
   void start() {
    stopping = false;
  }

  /**
   * Stops the queue by returning when it is empty and prohibiting any
   * further producers call to the <code>push</code> method.
   */
  void stop() {
    sync_begin();   
    stopping = true;
    if (queue->size() != 0) wait();
    sync_end();
  }
};

#endif /* XQUEUE_H */
