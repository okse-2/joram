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
package fr.dyade.aaa.agent;

import java.io.*;

import fr.dyade.aaa.util.Pool;

/**
 * List of Matrix clock updates. 
 */
final class Update {
  //  Declares all fileds transient in order to avoid useless
  // description of each during serialization.

  /**
   * Matrix clock line element.
   */
  transient short l;
  /**
   * Matrix clock column element.
   */
  transient short c;
  /**
   * Matrix clock value.
   */
  transient int stamp;
  /**
   * pointer on next element of update list.
   */
  transient Update next;

  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append('(').append(super.toString());
    strbuf.append(',').append(l).append(',')
	.append(c).append(',')
	.append(stamp).append(')');

    return strbuf.toString();
  }

  /**
   * Creates an element in a new empty list.
   * @param	l	Matrix line.
   * @param	c	Matrix column.
   * @param	s	Element stamp. 
   */
  private Update(short l, short c, int s) {
    this.l = l;
    this.c = c;
    this.stamp = s;
    this.next = null;
  }

//   /**
//    * Creates an element and links it after the head.
//    * @param	l	Matrix line.
//    * @param	c	Matrix column.
//    * @param	s	Element stamp. 
//    * @param 	list    The head list element.
//    */
//   private Update(short l, short c, int s, Update list) {
//     this.l = l;
//     this.c = c;
//     this.stamp = s;
//     if (list != null) {
//       this.next = list.next;
//       list.next = this;
//     } else {
//       this.next = null;
//     }
//   }

  /**
   * Return the source server of the message associated with this stamp.
   *
   * @return the source server id.
   */
  public short getFromId() {
    return l;
  }

  /**
   * Return the destination server of the message associated with this stamp.
   *
   * @return the destination server id.
   */
  public short getToId() {
    return c;
  }

  private static Pool pool = null;

  static {
    int size = Integer.getInteger("fr.dyade.aaa.agent.Message$Pool.size", 150).intValue();
    pool = new Pool("Update", size);
  }

  static Update alloc(short l, short c, int s) {
    Update update = null;
    
    try {
      update = (Update) pool.allocElement();
      update.l = l;
      update.c = c;
      update.stamp = s;
      if (update.next != null)
        throw new Error("Update");
    } catch (Exception exc) {
      return new Update(l, c, s);
    }
    return update;
  }

  static Update alloc(short l, short c, int s, Update list) {
    Update update = alloc(l, c, s);

    if (list != null) {
      update.next = list.next;
      list.next = update;
    } else {
      update.next = null;
    }
    return update;
  }

  void free() {
    if (next != null) next.free();
    next = null;
    pool.freeElement(this);
  }
}

