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
import fr.dyade.aaa.util.*;

/**
 * An element of the matrix clock.
 * @version	1.1, 11/19/97
 * @author	Andre Freyssinet
 */
class MatClockElt {
  /**
   * Element value.
   */
  int stamp;
  /**
   * Source node of last modification.
   */
  short node; 
  /**
   * State value when last modified.
   */
  int status;
}

/**
 * Matrix clock realization. 
 * @version	1.1, 02/04/98
 * @author	Andre Freyssinet
 */
class MatrixClock implements Serializable {

  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: MatrixClock.java,v 1.3 2000-10-05 15:15:21 tachkeni Exp $";

  //  Declares all fields transient in order to avoid useless
  // description of each during serialization.

  private transient int size;
  private transient int status[];

  private transient MatClockElt matClock[][];

  public String toString() {
    StringBuffer strBuf = new StringBuffer();

    strBuf.append("(");
    for (int i=0; i<size; i++) {
      strBuf.append(status[i]).append(" ");
    }
    strBuf.append(")\n\n");
    for (int i=0; i<size; i++) {
      strBuf.append("(");
      for (int j=0; j<size; j++) {
	strBuf.append("(").append(matClock[i][j].stamp).append(", ");
	strBuf.append(matClock[i][j].node).append(", ");
	strBuf.append(matClock[i][j].status).append(")");
      }
      strBuf.append(")\n");
    }

    return strBuf.toString();
  }

  private void writeObject(java.io.ObjectOutputStream out)
       throws IOException {
    out.writeInt(size);
    for (int i=0; i<size; i++)
      out.writeInt(status[i]);
    for (int i=0; i<size; i++) {
      for (int j=0; j<size; j++) {
	out.writeInt(matClock[i][j].stamp);
	out.writeShort(matClock[i][j].node);
	out.writeInt(matClock[i][j].status);
      }
    }
  }

  private void readObject(java.io.ObjectInputStream in)
       throws IOException, ClassNotFoundException {
    size = in.readInt();
    status = new int[size];
    matClock = new MatClockElt[size][size];
    for (int i=0; i<size; i++)
      status[i] = in.readInt();
    for (int i=0; i<size; i++) {
      for (int j=0; j<size; j++) {
	matClock[i][j] = new MatClockElt();
	matClock[i][j].stamp = in.readInt();
	matClock[i][j].node = in.readShort();
	matClock[i][j].status = in.readInt();
      }
    }
  }

  MatrixClock(short sid) {
    size = sid +1;
    status = new int[size];
    matClock = new MatClockElt[size][size];

    for (int i=0; i<size; i++)
      for (int j=0; j<size; j++)
	matClock[i][j] = new MatClockElt();
  }
  
  /**
   * Saves the object state on persistent storage.
   */
  void save() throws IOException {
    Server.transaction.save(this, "MatrixClock");
  }

  /**
   * Restores the object state from the persistent storage.
   *
   * @return	The restored matrix clock.
   */
  static MatrixClock
  load() throws IOException, ClassNotFoundException {
    return (MatrixClock) Server.transaction.load("MatrixClock");
  }

  /**
   *  Adjust matrix clock and status table. It should only be
   * used in testRecvUpdate and getSendUpdate, so there is no
   * need of synchronisation.
   */
  private void grow(short sid) {
    int newSize = sid +1;
    int newStatus[] = new int[newSize];
    MatClockElt newMatClock[][] = new MatClockElt[newSize][newSize];

    // Copy matrix clock and status table in the right sized table.
    int i, j;
    for (i=0; i<size; i++) {
      for (j=0; j<size; j++)
	newMatClock[i][j] = matClock[i][j];
      for (; j<newSize; j++)
	newMatClock[i][j] = new MatClockElt();
      newStatus[i] = status[i];
    }
    for (; i<newSize; i++) {
      for (j=0; j<newSize; j++)
	newMatClock[i][j] = new MatClockElt();
      newStatus[i] = 0;
    }
    size = newSize;
    status = newStatus;
    matClock = newMatClock;
  }

  /** The message can be delivered. */
  static final int DELIVER = 0;
  /** There is other message in the causal ordering before this.*/
  static final int WAIT_TO_DELIVER = 1;
  /** The message has already been delivered. */
  static final int ALREADY_DELIVERED = 2;

  /**
   *  Test if a received message with the specified clock must be
   * delivered. If the message is ready to be delivered, the method returns
   * <code>DELIVER</code> and the matrix clock is updated. If the message has
   * already been delivered, the method returns <code>ALREADY_DELIVERED</code>,
   * and if other messages are waited before this message the method returns
   * <code>WAIT_TO_DELIVER</code>. In the last two case the matrix clock
   * remains unchanged.
   *
   * @param update	The message matrix clock (list of update).
   * @return		<code>DELIVER</code>, <code>ALREADY_DELIVERED</code>,
   * 			or <code>WAIT_TO_DELIVER</code> code.
   */
  synchronized int testRecvUpdate(Update update) {
    // History: Field from is no longer need, we can use the first
    // element of the update to know the source server.
    short from = update.l;

// AF: I think that now (use of 1st element of update), "from" is always
// the server id. of incoming message.

    // Get real from serverId.
    if (Server.isTransient(from))
      from = Server.transientProxyId(from).to;

    if (Debug.dumpMatrixClock)
      Debug.trace("testRecvUpdate(" + from + ", " + update + ") <" + this + '>', false);

// AF: Actually this code is useless.

    // Update the matrix size if necessary.
    Update ptr = update;
    do {
      if (ptr.l >= size) grow(ptr.l);
      if (ptr.c >= size) grow(ptr.c);
      ptr = ptr.next;
    } while (ptr != null);

    // The 1st element of update is always: HM[from, to]
    ptr = update;
    if ((matClock[from][Server.serverId].stamp +1) < ptr.stamp) {
      // There is other messages from the same node to deliver before this one.
      if (Debug.dumpMatrixClock)
	Debug.trace("testRecvUpdate return WAIT_TO_DELIVER", false);

      return WAIT_TO_DELIVER;
    } else if ((matClock[from][Server.serverId].stamp +1) == ptr.stamp) {
      // Verify that all messages to be delivered to this node and known by
      // by this one are already be delivered.
      ptr = ptr.next;
      while (ptr != null) {
	if ((ptr.c == Server.serverId) && (ptr.stamp > matClock[ptr.l][ptr.c].stamp))
	  break;
	ptr = ptr.next;
      }
    } else {
      // We have already receive this message, we should send a new
      // acknowledge. Be careful: don't put this message in waiting list
      // it's a bug!
      if (Debug.dumpMatrixClock)
	Debug.trace("testRecvUpdate return ALREADY_DELIVERED", false);

      return ALREADY_DELIVERED;
    }

    if (ptr != null) {
      // The message can not be delivered.
      if (Debug.dumpMatrixClock)
	Debug.trace("testRecvUpdate return WAIT_TO_DELIVER", false);

      return WAIT_TO_DELIVER;
    } else {
      // The message is ready to be delivered, so updates the matrix clock.
      ptr = update;
      do {
	if (matClock[ptr.l][ptr.c].stamp < ptr.stamp) {
	  matClock[ptr.l][ptr.c].stamp = ptr.stamp;
	  matClock[ptr.l][ptr.c].node = from;
	  matClock[ptr.l][ptr.c].status = status[Server.serverId];
	}
	ptr = ptr.next;
      } while (ptr != null);
      
      if (Debug.dumpMatrixClock)
	Debug.trace("testRecvUpdate return DELIVER <" + this + '>', false);

      return DELIVER;
    }
  }
    
  /**
   * Computes the matrix clock of a send message. The server's
   * matrix clock is updated.
   *
   * @param to	The identification of receiver.	
   * @return	The message matrix clock (list of update).
   */
  synchronized Update getSendUpdate(short to) {
// AF: Actually this code is useless.

    // Update the matrix size if necessary.
    if (to >= size) grow(to);

    if (Debug.dumpMatrixClock)
      Debug.trace("getSendUpdate(" + to + ") <" + this + '>', false);

    matClock[Server.serverId][to].stamp += 1;
    matClock[Server.serverId][to].status = status[Server.serverId];
    matClock[Server.serverId][to].node = to;
    // The 1st element of update is always (from, to, stamp), its property
    // is used in testRecvUpdate.
    Update update = new Update(Server.serverId, to, matClock[Server.serverId][to].stamp);
    if (to != Server.serverId) {
      // If the message is remote there is need of matrix clock update.
      for (short i=0; i<matClock.length; i++) {
	for (short j=0; j<matClock[i].length; j++) {
	  if ((matClock[i][j].status > status[to]) && (matClock[i][j].node != to) &&
	      ((i != Server.serverId) || (j != to)))
	    new Update(i, j, matClock[i][j].stamp, update);
	}
      }
      status[to] = status[Server.serverId];
      status[Server.serverId] += 1;
    }

    if (Debug.dumpMatrixClock)
      Debug.trace("getSendUpdate return " + update + '<' + this + '>', false);

    return update;
  }

  /**
   * Returns the number of messages sent by local node to this one.<p>
   * <hr>
   * Used for a temporary Netwall fixes in order to detect a partial
   * configuration reinstallation.
   *
   * @param to	The id. of remote server.
   * @return	The number of messages.
   */ 
  synchronized Update getNetU1(short to) {
    if (to >= size) grow(to);
    return new Update(Server.serverId, to,
		      matClock[Server.serverId][to].stamp);
  }

  /**
   * Returns the number of messages received by local node from this one.<p>
   * <hr>
   * Used for a temporary Netwall fixes in order to detect a partial
   * configuration reinstallation.
   *
   * @param to	The id. of remote server.
   * @return	The number of messages.
   */ 
 synchronized Update getNetU2(short to) {
    if (to >= size) grow(to);
    return new Update(to, Server.serverId,
		      matClock[to][Server.serverId].stamp);
  }

  /**
   * Verify the coherency of local matrix clock with the one of remote
   * server. If one of the servers has been restarted after a complete
   * installation, the other one must have a bigger history: the number
   * of messages known as "received" by a node is bigger than the number
   * of messages known as "sent" by the other.<p>
   * In such a case we must refudes the connection.<p>
   * <hr>
   * Used for a temporary Netwall fixes in order to detect a partial
   * configuration reinstallation.
   *
   * @param u1	MC_From[from, local].stamp.
   * @param u2	MC_From[local, from].stamp.
   * @return	true if the matrix clocks of two nodes are coherent.
   */ 
 synchronized boolean testNU(Update u1, Update u2) {
// AF: Actually this code is useless.
    if (u1.l >= size) grow(u1.l);
    if (u2.c >= size) grow(u2.c);

    return ((u1.stamp < matClock[u1.l][Server.serverId].stamp) ||
	    (u2.stamp > matClock[Server.serverId][u2.c].stamp));
  }
}

