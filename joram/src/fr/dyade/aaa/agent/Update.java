/*
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

/**
 * List of Matrix clock updates. 
 * @version	1.1, 11/19/97
 * @author	Andr* Freyssinet
 */
final class Update implements Serializable {
  /** RCS version number of this file: $Revision: 1.13 $ */
  public static final String RCS_VERSION="@(#)$Id: Update.java,v 1.13 2003-06-23 13:37:51 fmaistre Exp $";

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
    
    Update update = this;
    while (update != null) {
      strbuf.append('(').append(update.l).append(',')
	.append(update.c).append(',')
	.append(update.stamp).append(')');
      update = update.next;
    }

    return strbuf.toString();
  }

// TODO: To delete
//   private void writeObject(java.io.ObjectOutputStream out)
//        throws IOException {
//     out.writeShort(l);
//     out.writeShort(c);
//     out.writeInt(stamp);
//   }

//   private void readObject(java.io.ObjectInputStream in)
//        throws IOException, ClassNotFoundException {
//     l = in.readShort();
//     c = in.readShort();
//     stamp = in.readInt();
//   }

  /**
   * Creates an element in a new empty list.
   * @param	l	Matrix line.
   * @param	c	Matrix column.
   * @param	s	Element stamp. 
   */
  public Update(short l, short c, int s) {
    this.l = l;
    this.c = c;
    this.stamp = s;
    this.next = null;
  }

  /**
   * Creates an element and links it after the head.
   * @param	l	Matrix line.
   * @param	c	Matrix column.
   * @param	s	Element stamp. 
   * @param 	list    The head list element.
   */
  public Update(short l, short c, int s, Update list) {
    this.l = l;
    this.c = c;
    this.stamp = s;
    if (list != null) {
      this.next = list.next;
      list.next = this;
    } else {
      this.next = null;
    }
  }

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
}

