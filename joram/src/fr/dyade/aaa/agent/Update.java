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

/**
 * List of Matrix clock updates. 
 * @version	1.1, 11/19/97
 * @author	Andr* Freyssinet
 */
final class Update implements Serializable {

  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: Update.java,v 1.3 2000-10-05 15:15:25 tachkeni Exp $";

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
    String strBuf;
    
    strBuf = "[" + l + ", " + c + ", " + stamp + "] -> " + next;

    return strBuf;
  }

  private void writeObject(java.io.ObjectOutputStream out)
       throws IOException {
    out.writeShort(l);
    out.writeShort(c);
    out.writeInt(stamp);
  }

  private void readObject(java.io.ObjectInputStream in)
       throws IOException, ClassNotFoundException {
    l = in.readShort();
    c = in.readShort();
    stamp = in.readInt();
  }

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
}

