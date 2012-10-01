/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies 
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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;


public final class Pool {
  int elementCount = 0;
  Object[] elementData =  null;

  private Logger logmon = null;
  private long cpt1, cpt2, alloc, free, min, max;

  private String name;
  private String logmsg;

  public Pool(String name, int capacity) {
    this.name = name;
    this.logmsg = name + ".Pool: ";
    elementData = new Object[capacity];
    logmon = Debug.getLogger(getClass().getName() + '.' + getName());
    logmon.log(BasicLevel.INFO, logmsg + capacity);
  }

  public String getName() {
    return name;
  }

  public final synchronized void freeElement(Object obj) {
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, logmsg + "freeElement " + obj);
    }
    // If there is enough free element, let the gc get this element. 
    if (elementCount == elementData.length) {
      free += 1;
      return;
    }
    elementData[elementCount] = obj;
    elementCount += 1;

    if (elementCount > max) max = elementCount;
  }

  public final synchronized Object allocElement() throws Exception {
    if (elementCount == 0) {
      alloc += 1;

      if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, logmsg + "allocElement <new>");

      throw new Exception();
    }
    elementCount -= 1;
    Object obj = elementData[elementCount];
    elementData[elementCount] = null; /* to let gc do its work */

    if (elementCount < min) min = elementCount;
    cpt1 += 1; cpt2 += elementCount;
    if ((cpt1 & 0xFFFFFL) == 0L) {
      if (logmon.isLoggable(BasicLevel.INFO)) {
        logmon.log(BasicLevel.INFO,
                   logmsg + (cpt2/cpt1) + '/' + elementCount +
                   ", " + min + '/' + max + ", " + alloc + ", " + free);
        alloc = 0; free = 0; min = elementData.length; max = 0;
      }
    }
    
    if (Debug.debug && logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG,
                 logmsg + "allocElement " + obj);
    }

    return obj;
  }
}
