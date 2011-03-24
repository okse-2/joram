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


package fr.dyade.aaa.joram;

import java.io.*;
import java.util.*;

/**
 * The <code>FifoQueue</code> class represents a First-In-First-Out 
 * (FIFO) list of objects. 
 */
public class FifoQueue extends Vector {
  
  /**
   * Pushes an item onto the bottom of this queue. 
   *
   * @param   item   the item to be pushed onto this queue.
   */
  public synchronized void push(Object item) {
    addElement(item);
    notify();
  }
  
  /**
   * Removes the object at the top of this queue and returns that 
   * object as the value of this function. 
   *
   * @return     The object at the top of this queue.
   */
  public synchronized Object pop() {
    Object obj;
    try {
      obj =elementAt(0);
      removeElementAt(0);
    } catch (Exception e) { return null;}
    return obj;
  }

  /**
   * Looks at the object at the top of this queue without removing it 
   * from the queue. 
   *
   * @return     the object at the top of this queue. 
   */
  public synchronized Object get() {
    while (size() == 0) {
      try {
	wait();
      } catch (InterruptedException e) {}
    }
    return elementAt(0);
  }

  /**
   * Removes all object of this queue 
   */
  public synchronized void remove() {
    removeAllElements();
  }
}