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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 * An element of the matrix clock.
 */
class MatClockElt implements Serializable {
  /** Element value. */
  int stamp;
  /** Source node of last modification. */
  short node; 
  /** State value when last modified. */
  int status;

  private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
    out.writeInt(stamp);
    out.writeShort(node);
    out.writeInt(status);
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
    stamp = in.readInt();
    node = in.readShort();
    status = in.readInt();
  }
}

/**
 * Matrix clock realization. 
 */
class MatrixClock extends LogicalClock {
  /** Index of local server in status and matrix arrays. */
  private int idxLS;
  /** */
  private int status[];
  /** Filename for status storage */
  private String statusFN = null;
  /** */
  private MatClockElt matrix[][];

//  /**
//   * The writeObject method is responsible for writing the state of the
//   * <code>MatrxiClock</code> object so that the corresponding readObject
//   * method can restore it. For performance reasons, all fields are declared
//   * transient, and the state of <code>MatClockElt</code> objects are
//   * directly handled in this serialization method.
//   *
//   * @param out	The <code>ObjectOutputStream</code> object.
//   */
//   private void writeObject(java.io.ObjectOutputStream out)
//        throws IOException {
//     int size = status.length;
//     out.writeInt(size);
//     for (int i=0; i<size; i++)
//       out.writeInt(status[i]);
//     for (int i=0; i<size; i++) {
//       for (int j=0; j<size; j++) {
// 	out.writeInt(matrix[i][j].stamp);
// 	out.writeShort(matrix[i][j].node);
// 	out.writeInt(matrix[i][j].status);
//       }
//     }
//   }

//  /**
//   * The readObject method is responsible for reading from the stream and
//   * restoring the <code>MatrxiClock</code> fields.
//   *
//   * @see	#writeObject
//   *
//   * @param in	The <code>ObjectInputStream</code> object.
//   */
//   private void readObject(java.io.ObjectInputStream in)
//        throws IOException, ClassNotFoundException {
//     int size = in.readInt();
//     status = new int[size];
//     matrix = new MatClockElt[size][size];
//     for (int i=0; i<size; i++)
//       status[i] = in.readInt();
//     for (int i=0; i<size; i++) {
//       for (int j=0; j<size; j++) {
// 	matrix[i][j] = new MatClockElt();
// 	matrix[i][j].stamp = in.readInt();
// 	matrix[i][j].node = in.readShort();
// 	matrix[i][j].status = in.readInt();
//       }
//     }
//   }

  /**
   * Creates a new matrix clock. Be careful, the list of servers must be
   * sorted into ascending numerical order, this list must be also used in
   * Network component.
   *
   * @param name	Name of domain.
   * @param servers	List of domain's server id.
   */
  MatrixClock(String name, short[] servers) {
    super(name, servers);
    statusFN = name + "Status";
  }
  
  /**
   * Saves the object state on persistent storage.
   *
   * @param name	Name of domain.
   */
  void save() throws IOException {
    if (modified) {
      // Save the Clock information.
      AgentServer.transaction.save(status, statusFN);
      AgentServer.transaction.save(matrix, name);
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
  void load()throws IOException, ClassNotFoundException {
    // Loads the matrix clock.
    matrix = (MatClockElt[][]) AgentServer.transaction.load(name);
    if (matrix == null) {
      // Creates the new stamp, then saves it
      status = new int[servers.length];
      matrix = new MatClockElt[servers.length][servers.length];
      // Immediatly allocates all elements
      for (int i=0; i<servers.length; i++)
        for (int j=0; j<servers.length; j++)
          matrix[i][j] = new MatClockElt();
      // Save the servers configuration and clock.
      AgentServer.transaction.save(servers, serversFN);
      modified = true;
      save();
    } else {
      // Join with the new domain configuration:
      short[] s = (short[]) AgentServer.transaction.load(serversFN);
	if ((servers != null) && !Arrays.equals(servers, s)) {
        for (int i=0; i<servers.length; i++)
          logmon.log(BasicLevel.DEBUG,
                     "servers[" + i + "]=" + servers[i]);
        for (int i=0; i<s.length; i++)
          logmon.log(BasicLevel.DEBUG,
                     "servers[" + i + "]=" + s[i]);
        
	throw new IOException("Network configuration changed");
      }
      status = (int[]) AgentServer.transaction.load(statusFN);
    }
    idxLS = index(AgentServer.getServerId());
  }

  synchronized void addServer(String name, short sid) throws IOException {}

  synchronized void delServer(String name, short sid) throws IOException {}

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
    Update update = Update.alloc(AgentServer.getServerId(),
                                 to,
                                 matrix[idxLS][toIdx].stamp);
    if (to != AgentServer.getServerId()) {
      // If the message is remote there is need of matrix clock update.
      for (short i=0; i<matrix.length; i++) {
	for (short j=0; j<matrix[i].length; j++) {
	  if ((matrix[i][j].status > status[toIdx]) &&
	      (matrix[i][j].node != to) &&
	      ((i != idxLS) || (j != toIdx)))
	    Update.alloc(servers[i], servers[j], matrix[i][j].stamp, update);
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

