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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 * An element of the matrix clock.
 */
class MatClockElt {
  /** RCS version number of this file: $Revision: 1.12 $ */
  public static final String RCS_VERSION="@(#)$Id: MatrixClock.java,v 1.12 2003-03-19 15:16:06 fmaistre Exp $"; 

  /** Element value. */
  int stamp;
  /** Source node of last modification. */
  short node; 
  /**
   * State value when last modified. */
  int status;
}

/**
 * Matrix clock realization. 
 */
class MatrixClock implements Serializable {
  /** RCS version number of this file: $Revision: 1.12 $ */
  public static final String RCS_VERSION="@(#)$Id: MatrixClock.java,v 1.12 2003-03-19 15:16:06 fmaistre Exp $";

  //  Declares all fields transient in order to avoid useless
  // description of each during serialization.

  /** True if the matrix is modified since last save, false otherwise. */
  private transient boolean modified = false;
  /**
   * List of id. for all servers in the domain, this list is sorted and
   * is used as index for status and matrix arrays. Be careful, this array
   * is shared with the <code>Network</code> components (may be it should
   * be saved by this component).
   */
  private transient short[] servers;
  /** The domain name. */
  private transient String name;
  /** Index of local server in status and matrix arrays. */
  private transient int idxLS;
  /** */
  private transient int status[];
  /** */
  private transient MatClockElt matrix[][];

  private transient Logger logmon = null;

 /**
  * The writeObject method is responsible for writing the state of the
  * <code>MatrxiClock</code> object so that the corresponding readObject
  * method can restore it. For performance reasons, all fields are declared
  * transient, and the state of <code>MatClockElt</code> objects are
  * directly handled in this serialization method.
  *
  * @param out	The <code>ObjectOutputStream</code> object.
  */
  private void writeObject(java.io.ObjectOutputStream out)
       throws IOException {
    int size = status.length;
    out.writeInt(size);
    for (int i=0; i<size; i++)
      out.writeInt(status[i]);
    for (int i=0; i<size; i++) {
      for (int j=0; j<size; j++) {
	out.writeInt(matrix[i][j].stamp);
	out.writeShort(matrix[i][j].node);
	out.writeInt(matrix[i][j].status);
      }
    }
  }

 /**
  * The readObject method is responsible for reading from the stream and
  * restoring the <code>MatrxiClock</code> fields.
  *
  * @see	#writeObject
  *
  * @param in	The <code>ObjectInputStream</code> object.
  */
  private void readObject(java.io.ObjectInputStream in)
       throws IOException, ClassNotFoundException {
    int size = in.readInt();
    status = new int[size];
    matrix = new MatClockElt[size][size];
    for (int i=0; i<size; i++)
      status[i] = in.readInt();
    for (int i=0; i<size; i++) {
      for (int j=0; j<size; j++) {
	matrix[i][j] = new MatClockElt();
	matrix[i][j].stamp = in.readInt();
	matrix[i][j].node = in.readShort();
	matrix[i][j].status = in.readInt();
      }
    }
  }

  /**
   * Creates a new matrix clock. Be careful, the list of servers must be
   * sorted into ascending numerical order, this list must be also used in
   * Network component.
   *
   * @param name	Name of domain.
   * @param servers	List of domain's server id.
   */
  private MatrixClock(String name, short[] servers) throws IOException {
    this.name = name;
    this.servers = servers;
    AgentServer.transaction.save(servers, name + "Servers");
    idxLS = index(AgentServer.getServerId());
    status = new int[servers.length];
    matrix = new MatClockElt[servers.length][servers.length];
    // Immediatly allocates all elements
    for (int i=0; i<servers.length; i++)
      for (int j=0; j<servers.length; j++)
	matrix[i][j] = new MatClockElt();
    save(name);
  }
  
  /**
   * Saves the object state on persistent storage.
   *
   * @param name	Name of domain.
   */
  void save(String name) throws IOException {
    if (modified) {
      AgentServer.transaction.save(this, name);
      modified = false;
    }
  }

  /**
   * Restores the object state from the persistent storage. If servers
   * is null, then we have just to restore the matrix, else it's the
   * first loading at initialization time and we have to restore the
   *
   * @param name	Name of domain.
   * @param servers	List of domain's server id.
   * @return		The restored matrix clock.
   */
  static MatrixClock
  load(String name, 
       short[] servers)throws IOException, ClassNotFoundException {
    // Loads the matrix clock.
    MatrixClock mc = (MatrixClock) AgentServer.transaction.load(name);
    if (mc == null) {
      // Creates a new Matrix and save it.
      mc = new MatrixClock(name, servers);
      // Get the logging monitor from current server MonologLoggerFactory
      mc.logmon = Debug.getLogger(Debug.A3Debug + ".MatrixClock." + name);
    } else {
      // Get the logging monitor from current server MonologLoggerFactory
      mc.logmon = Debug.getLogger(Debug.A3Debug + ".MatrixClock." + name);
      // Join with the new domain configuration:
      mc.servers = (short[]) AgentServer.transaction.load(name + "Servers");
      mc.idxLS = mc.index(AgentServer.getServerId());
      if (!Arrays.equals(mc.servers, servers)) {
        mc.logmon.log(BasicLevel.WARN,
                      "MatrixClock." + name + ", updates configuration");
	// TODO: Insert or suppress corresponding elements in matrix...
	throw new IOException("Bad configuration");
      }
    }
    return mc;
  }

  /**
   * Returns the index of the specified server.
   */
  private final int index(short id) {
//   private final int index(short id) throws UnknownServerException {
    int idx = Arrays.binarySearch(servers, id);
//     if (idx < 0)
//       throw new UnknownServerException("Unknow server id. #" + id);
    return idx;
  }

  /**
   *  Adjust matrix clock and status table. It should only be
   * used in testRecvUpdate and getSendUpdate, so there is no
   * need of synchronisation.
   */
//   private void grow(short sid) {
//     int newSize = sid +1;
//     int newStatus[] = new int[newSize];
//     MatClockElt newMatClock[][] = new MatClockElt[newSize][newSize];

//     // Copy matrix clock and status table in the right sized table.
//     int i, j;
//     for (i=0; i<size; i++) {
//       for (j=0; j<size; j++)
// 	newMatClock[i][j] = matClock[i][j];
//       for (; j<newSize; j++)
// 	newMatClock[i][j] = new MatClockElt();
//       newStatus[i] = status[i];
//     }
//     for (; i<newSize; i++) {
//       for (j=0; j<newSize; j++)
// 	newMatClock[i][j] = new MatClockElt();
//       newStatus[i] = 0;
//     }
//     size = newSize;
//     status = newStatus;
//     matClock = newMatClock;
//   }

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
    short from = update.getFromId();
    int fromIdx = index(from);

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentServer#" + AgentServer.getServerId() +
                 ".testRecvUpdate(" + from + ", " + update + ") <" + this + '>');

    // The 1st element of update is always: HM[from, to]
    Update ptr = update;
    if ((matrix[fromIdx][idxLS].stamp +1) < ptr.stamp) {
      // There is other messages from the same node to deliver before this one.
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "MatrixClock." + name +
                   ", testRecvUpdate return WAIT_TO_DELIVER");
      return WAIT_TO_DELIVER;
    } else if ((matrix[fromIdx][idxLS].stamp +1) == ptr.stamp) {
      // Verify that all messages to be delivered to this node and known by
      // by this message are already be delivered.
      ptr = ptr.next;
      while (ptr != null) {
	if ((ptr.c == AgentServer.getServerId()) &&
	    (ptr.stamp > matrix[index(ptr.l)][index(ptr.c)].stamp))
	  break;
	ptr = ptr.next;
      }
    } else {
      // We have already receive this message, we should send a new
      // acknowledge. Be careful: don't put this message in waiting list
      // it's a bug!
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "MatrixClock." + name +
                   ", testRecvUpdate return ALREADY_DELIVERED");
      return ALREADY_DELIVERED;
    }

    if (ptr != null) {
      // The message can not be delivered.
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "MatrixClock." + name +
                   ", testRecvUpdate return WAIT_TO_DELIVER");
      return WAIT_TO_DELIVER;
    } else {
      // The message is ready to be delivered, so updates the matrix clock.
      ptr = update;
      do {
	int idxl = index(ptr.l);
	int idxc = index(ptr.c);
	if (matrix[idxl][idxc].stamp < ptr.stamp) {
	  matrix[idxl][idxc].stamp = ptr.stamp;
	  matrix[idxl][idxc].node = from;
	  matrix[idxl][idxc].status = status[idxLS];
	}
	ptr = ptr.next;
      } while (ptr != null);
      
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "MatrixClock." + name +
                   ", testRecvUpdate return DELIVER <" + this + '>');
      modified = true;
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
    int toIdx = index(to);

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "MatrixClock." + name +
                   ", getSendUpdate(" + to + ") <" + this + '>');

    matrix[idxLS][toIdx].stamp += 1;
    matrix[idxLS][toIdx].status = status[idxLS];
    matrix[idxLS][toIdx].node = to;
    // The 1st element of update is always (from, to, stamp), its property
    // is used in testRecvUpdate.
    Update update = new Update(AgentServer.getServerId(),
			       to,
			       matrix[idxLS][toIdx].stamp);
    if (to != AgentServer.getServerId()) {
      // If the message is remote there is need of matrix clock update.
      for (short i=0; i<matrix.length; i++) {
	for (short j=0; j<matrix[i].length; j++) {
	  if ((matrix[i][j].status > status[toIdx]) &&
	      (matrix[i][j].node != to) &&
	      ((i != idxLS) || (j != toIdx)))
	    new Update(servers[i], servers[j], matrix[i][j].stamp, update);
	}
      }
      status[toIdx] = status[idxLS];
      status[idxLS] += 1;
    }

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "MatrixClock." + name +
                   ", getSendUpdate return " + update + '<' + this + '>');

    modified = true;
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
    return new Update(AgentServer.getServerId(), to,
		      matrix[idxLS][index(to)].stamp);
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
    return new Update(to, AgentServer.getServerId(),
		      matrix[index(to)][idxLS].stamp);
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
    return ((u1.stamp < matrix[index(u1.l)][idxLS].stamp) ||
	    (u2.stamp > matrix[idxLS][index(u2.c)].stamp));
  }

  /**
   * Returns a string representation of this <code>MatrixClock</code> object
   * in the form of a set of entries, enclosed in braces and separated by the
   * String ", " (characters comma and space).
   *
   * @return String representation of this <code>MatrixClock</code> object.
   */
  public String toString() {
    int size = status.length;
    StringBuffer strBuf = new StringBuffer();

    strBuf.append("(");
    for (int i=0; i<size; i++) {
      strBuf.append(servers[i]).append(" ");
    }
    strBuf.append(")\n\n");

    strBuf.append("(");
    for (int i=0; i<size; i++) {
      strBuf.append(status[i]).append(" ");
    }
    strBuf.append(")\n\n");

    for (int i=0; i<size; i++) {
      strBuf.append("(");
      for (int j=0; j<size; j++) {
	strBuf.append("(").append(matrix[i][j].stamp).append(", ");
	strBuf.append(matrix[i][j].node).append(", ");
	strBuf.append(matrix[i][j].status).append(")");
      }
      strBuf.append(")\n");
    }

    return strBuf.toString();
  }
}

