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


/**
 * Class <code>BufferedFilter</code> provides code for filtering a flow
 * of objects, some of them needing further analysis, the others being
 * simply forwarded. The particularity of this class is that incoming
 * objects are handled by a thread, which may also directly forward the
 * objects, while the extra analysis is performed asynchronously by another
 * thread, which forwards itself the possibly modified objects. Both
 * threads need to synchronize to enforce forwarding all objects in the
 * order they were received.
 *
 * Buffered objects are kept in a linked list of <code>BufferedFilterItem</code>
 * objects. This object keeps the list head in the <code>head</code> variable,
 * and the list tail in the <code>tail</code> variable. The tail is always
 * a <code>BufferedFilterItem</code> with a null next field, and is used to
 * <code>append</code> items to the list. The head is the tail when the list
 * is empty.
 *
 * Buffered objects are always organized as series formed of a leading item
 * with the <code>toBeAnalyzed</code> field set to true, followed by a set of
 * items with the <code>toBeAnalyzed</code> field set to false. The latter are
 * stored into the list by the producer which was not able to forward them
 * waiting for the former to be forwarded by the consumer. A serie is
 * identified by a stamp incremented each time a to be analyzed object is
 * appended by the producer. The current stamp is kept in the <code>stamp</code>
 * variable. The <code>done</code> variable is updated by the consumer when it
 * estimates it has completed the treatment and forwarding of a serie. This
 * variable is not always up to date, and some series stamps may be omitted.
 * However This is enough for the producer and the consumer to synchronize.
 *
 * The producer creates objects by calling the function <code>forward</code>
 * for objects to forward, and <code>keep</code> for objects to analyze before
 * forwarding. Objects are analyzed by the producer calling the <code>analyze</code>
 * function, which is actually defined in derived classes. The analyzer may
 * modify the object prior to eventually calling the <code>forward</code>
 * function without parameter to actually forward the possibly modified object.
 * This call should be issued from a separate thread. A call from the original
 * thread may succeed but i am not sure of it. Actual forwarding is executed
 * by calling the <code>doForward</code> function, which is actually defined
 * in derived classes.
 *
 * The code is designed to avoid synchronization cost in standard functionning.
 * When potential conflicts are detected, actual synchronization is done
 * using the lock on this object.
 *
 * The <code>head</code>, <code>tail</code>, <code>stamp</code>, and <code>done</code> variables are declared volatile because
 * they are written by a thread and read by the other without explicit
 * synchronization.
 *
 * WARNING: This synchronization is not correct as long as the strict definition
 * of Java mechanisms is taken. Synchronizing key variables with the volatile
 * keyword has no effect on the private memory level of each thread, meaning
 * that the objects in the queue and the queue items may not be visible in the
 * consumer thread.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		BufferedFilterItem
 */

package fr.dyade.aaa.util;

import java.util.*;


/**
 * Objects are kept in a list of <code>BufferedFilterItem</code> objects,
 * linked by the <code>next</code> field, and terminated by a tail item
 * identified by its null <code>next</code> field. The other fields of
 * the tail item are undefined.
 * The class defines no synchronisation, as the producer and the consumer(s)
 * of the list synchronize at their level.
 * The <code>toBeAnalyzed</code> field notifies wether the associated object
 * needs further analysis before being forwarded. Such an item may be followed
 * by items with a false <code>toBeAnalyzed</code> field, corresponding to
 * objects which the consumer was not able to forward waiting for the initial
 * item to be analyzed and forwarded. The producer and the consumer synchronize
 * to forward those items.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		BufferedFilter
 */
class BufferedFilterItem {

  /* object to forward */
  Object object;
  /* <code>true</code> if object needs prior analysis */
  boolean toBeAnalyzed;
  /* <code>null</code> if tail item */
  BufferedFilterItem next;

  /**
   * Creates a tail item.
   */
  public BufferedFilterItem() {
    object = null;
    next = null;
  }

  /**
   * Appends an item to the list.
   * Fills in the tail item, and creates a new tail item.
   *
   * @param object	object to forward
   * @param toBeAnalyzed <code>true</code> if object needs prior analysis
   */
  public void append(Object object, boolean toBeAnalyzed) {
    if (next != null)
      throw new IllegalStateException("append to no tail item");
    this.object = object;
    this.toBeAnalyzed = toBeAnalyzed;
    next = new BufferedFilterItem();
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",object=" + object +
      ",toBeAnalyzed=" + toBeAnalyzed +
      ",next=" + next + ')';
  }
}


/**
 * This class is used to help a consumer and a producer synchronize
 * when filtering, analyzing, and forwarding objects.
 * <p>
 * Some objects need no analyzis, and are forwarded directly by the producer.
 * Others are queued for the consumer to analyze and forward them. This object
 * ensures that the objects are forwarded in a proper ordering, be they
 * analyzed or not.
 */
public abstract class BufferedFilter {

public static final String RCS_VERSION="@(#)$Id: BufferedFilter.java,v 1.7 2002-03-06 16:58:48 joram Exp $"; 


  /* first item, tail if empty list */
  volatile BufferedFilterItem head;
  /* tail item */
  volatile BufferedFilterItem tail;
  /* incremented by producer when new serie begins */
  volatile int stamp;
  /* minorates stamp of last serie completed by consumer */
  volatile int done;
  /* current serie handled by consumer */
  int current;

  /**
   * Default constructor.
   */
  public BufferedFilter() {
    tail = new BufferedFilterItem();
    head = tail;
    stamp = 0;
    done = 0;
    current = 0;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",stamp=" + stamp +
      ",done=" + done +
      ",current=" + current +
      ",list=" + head + ')';
  }

  /**
   * Checks the status of the queue of objects.
   *
   * @return	<code>true</code> if the queue is empty
   */
  public boolean isEmpty() {
    return (head == tail);
  }

  /**
   * Checks the status of the queue of objects.
   *
   * @return	<code>true</code> if the queue is full
   */
  public boolean isFull() {
    return false;
  }

  /**
   * Adds an object at the end of the queue of objects.
   *
   * @param object	object to forward
   * @param toBeAnalyzed <code>true</code> if object needs prior analysis
   */
  public void push(Object object, boolean toBeAnalyzed) {
    tail.append(object, toBeAnalyzed);
    tail = tail.next;
  }

  /**
   * Gets first element from the queue of objects.
   *
   * @return	first object in the queue
   * @exception NoSuchElementException
   *	when the queue is empty
   */
  public BufferedFilterItem pull() {
    if (isEmpty())
      throw new NoSuchElementException();
    return head;
  }

  /**
   * Gets and removes first element from the queue of objects.
   *
   * @return	first object in the queue
   * @exception NoSuchElementException
   *	when the queue is empty
   */
  public BufferedFilterItem pop() {
    if (isEmpty())
      throw new NoSuchElementException();
    BufferedFilterItem item = head;
    head = item.next;
    return item;
  }

  /**
   * Forwards an object without extra treatment required.
   * Called by the producer.
   * <p>
   * If the list is empty lets the producer thread forward the object.
   * Otherwise append the object to the list and synchronize with the
   * consumer.
   *
   * @param object	object to forward
   * @exception Exception
   *	unspecialized exception
   */
  public void forward(Object object) throws Exception {
    if (isEmpty()) {
      doForward(object);
    } else {
      push(object, false);
      // by the time the item is pushed, the consumer may have stopped
      // and declared the serie completed
      if (done == stamp) {
	System.out.println("synchronize with consumer");
	// need to synchronize with the consumer
	// TODO si le producteur ne va pas assez vite, on tombe toujours
	// dans ce cas. Prevoir un done en deux etapes ?
	BufferedFilterItem left = tail;
	synchronized (this) {
	  if (done == stamp && ! isEmpty()) {
	    System.out.println("producer gained the lock");
	    // producer has gained the lock
	    // "empty" the buffer
	    // the consumer naturally stops with an empty buffer
	    left = head;
	    head = tail;
	  }
	}
	while (left != tail) {
	  doForward(left.object);
	  left = left.next;
	}
      }
    }
  }

  /**
   * Requires an extra treatment before forwarding object.
   * Called by the producer.
   *
   * @param object	object to keep
   * @exception Exception
   *	unspecialized exception
   */
  public void keep(Object object) throws Exception {
    stamp ++;
    push(object, true);
    analyze(object);
  }

  /**
   * Analyzes an object. Upcall to the consumer in the producer thread.
   * The object given as parameter may be modified by the consumer.
   * When the consumer eventually calls <code>forward</code>, the
   * modified object is forwarded.
   *
   * @param object	object to analyze
   * @exception Exception
   *	unspecialized exception
   */
  public abstract void analyze(Object object) throws Exception;

  /**
   * Forwards the head list object, which has the <code>toBeAnalyzed</code>
   * field set to <code>true</code>.
   * Called by the consumer.
   * <p>
   * The head item is read first by <code>pull</code> then removed by
   * <code>pop</code> because there is no synchronization with the producer,
   * which assumes that an item is forwarded as soon as it is no longer in the
   * list.
   *
   * @exception IllegalStateException
   *	when first item in list has no <code>toBeAnalyzed</code> field set
   * @exception Exception
   *	unspecialized exception
   */
  public void forward() throws Exception {
    BufferedFilterItem item = pull();
    if (! item.toBeAnalyzed)
      throw new IllegalStateException("no to be analyzed item in head list");

    // updates serie stamp handled by consumer
    current ++;
  serie:
    while (true) {
      doForward(item.object);
      pop();
      if (isEmpty()) {
	// mark the serie completed
	done = current;
	// items may have been lately produced
	if (isEmpty()) {
	  // consumer actually completed the serie
	  break serie;
	} else {
	  System.out.println("synchronize with producer");
	  // need to synchronize with the producer
	  synchronized (this) {
	    if (isEmpty()) {
	      // producer gained the lock
	      break serie;
	    } else {
	      item = pull();
	      if (item.toBeAnalyzed) {
		// producer gained the lock and pushed next serie first item
		// could set done to current, but it is not necessary
		break serie;
	      }
	      System.out.println("consumer gained the lock");
	      // consumer has gained the lock
	      // mark consumer running again
	      done = current - 1;
	    }
	  }
	}
      } else {
	item = pull();
	if (item.toBeAnalyzed) {
	  // could set done to current, but it is not necessary
	  // this item will be handled by next call to forward
	  break serie;
	}
      }
    }
  }

  /**
   * Actually forwards an object.
   * This function may be called by the producer thread or the
   * consumer thread, but never concurrently.
   *
   * @param object	object to forward
   * @exception Exception
   *	unspecialized exception
   */
  public abstract void doForward(Object object) throws Exception;
}
