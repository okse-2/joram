/*
 * Copyright (C) 2003 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.agent;

import java.io.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

final class SimpleClock extends LogicalClock {
  /** Logical timestamp information for messages in domain, stamp[idxLS)]
   * for messages sent, and stamp[index(id] for messages received.
   */
  private int[] stamp;
  /** Buffer used to optimise transactions*/
  private byte[] stampBuf = null;
  /** */
  private int[] bootTS = null;
  /**
   * Creates a new logical clock. Be careful, the list of servers must be
   * sorted into ascending numerical order, this list must be also used in
   * Network component.
   *
   * @param name	Name of domain.
   * @param servers	List of domain's server id.
   */
  SimpleClock(String name, short[] servers) {
    super(name, servers);
  }

  /**
   * Saves logical clock information to persistent storage.
   *
   * @param name	Name of domain.
   */
  void save() throws IOException {
  }
    
  /**
   * Restores logical clock information from persistent storage.
   * If servers is null, then we have just to restore the matrix,
   * else it's the first loading at initialization time and we have
   * to restore it.
   */
  void load() throws IOException, ClassNotFoundException {
    sid = AgentServer.getServerId();
    idxLS = index(sid);
    // Loads the logical clock.
    stampBuf = AgentServer.transaction.loadByteArray(name);
    if (stampBuf ==  null) {
      // Creates the new stamp array and the boot time stamp,
      stampBuf = new byte[4*servers.length];
      stamp = new int[servers.length];
      bootTS = new int[servers.length];
      // Then initializes them
      for (int i=0; i<servers.length; i++) {
        if (i != idxLS) {
          stamp[i] = -1;
          bootTS[i] = -1;
        } else {
          stamp[i] = 0;
          bootTS[i] = (int) (System.currentTimeMillis() /1000L);
        }
      }
      // Save the servers configuration and the logical time stamp.
      AgentServer.transaction.save(servers, serversFN);
      AgentServer.transaction.save(bootTS, bootTSFN);
      AgentServer.transaction.saveByteArray(stampBuf, name);
    } else {
      // Loads the domain configurations
      short[] s = (short[]) AgentServer.transaction.load(serversFN);
      bootTS = (int[]) AgentServer.transaction.load(bootTSFN);
      stamp = new int[s.length];
      for (int i=0; i<servers.length; i++) {
        stamp[i] = ((stampBuf[(i*4)+0] & 0xFF) << 24) +
          ((stampBuf[(i*4)+1] & 0xFF) << 16) +
          ((stampBuf[(i*4)+2] & 0xFF) <<  8) +
          (stampBuf[(i*4)+3] & 0xFF);
      }
      // Joins with the new domain configuration:
      if ((servers != null) && !Arrays.equals(servers, s)) {
        for (int i=0; i<servers.length; i++)
          logmon.log(BasicLevel.DEBUG,
                     "servers[" + i + "]=" + servers[i]);
        for (int i=0; i<s.length; i++)
          logmon.log(BasicLevel.DEBUG,
                     "servers[" + i + "]=" + s[i]);

        throw new IOException("Network configuration changed");
      }
    }
  }

  /**
   * Adds the specified server in the logical clock.
   *
   * @param id	the unique server id.
   */
  synchronized void addServer(short id) 
    throws IOException {
    // First we have to verify that id is not already in servers
    int idx = index(id);
    if (idx >= 0) return;
    idx = -idx -1;
    // Allocates new array for stamp and server
    int[] newStamp = new int[servers.length+1];
    byte[] newStampBuf = new byte[4*(servers.length+1)];
    int[] newBootTS = new int[servers.length+1];
    short[] newServers = new short[servers.length+1];
    // Copy old data from stamp and server, let a free room for the new one.
    int j = 0;
    for (int i=0; i<servers.length; i++) {
      if (i == idx) j++;
      newServers[j] = servers[i];
      newBootTS[j] = bootTS[i];
      newStamp[j] = stamp[i];
      j++;
    }
    if (idx > 0)
      System.arraycopy(stampBuf, 0, newStampBuf, 0, idx*4);
    if (idx < servers.length)
      System.arraycopy(stampBuf, idx*4,
                       newStampBuf, (idx+1)*4, (servers.length-idx)*4);

    newServers[idx] = id;
    newBootTS[idx] = -1;
    newStamp[idx] = -1;		// useless
    newStampBuf[idx] = 0;	// useless
    newStampBuf[idx+1] = 0;	// useless
    newStampBuf[idx+2] = 0; 	// useless
    newStampBuf[idx+3] = 0; 	// useless

    stamp = newStamp;
    stampBuf = newStampBuf;
    servers = newServers;
    // be careful, set again the index of local server.
    idxLS = index(sid);

    // Save the servers configuration and the logical time stamp.
    AgentServer.transaction.save(servers, serversFN);
    AgentServer.transaction.save(bootTS, bootTSFN);
    AgentServer.transaction.saveByteArray(stampBuf, name);
  }

  /**
   * Removes the specified server in the logical clock.
   *
   * @param id	the unique server id.
   */
  synchronized void delServer(short id) 
    throws IOException {
    // First we have to verify that id is already in servers
    int idx = index(id);
    if (idx < 0) return;

    int[] newStamp = new int[servers.length-1];
    byte[] newStampBuf = new byte[4*(servers.length-1)];
    int[] newBootTS = new int[servers.length-1];
    short[] newServers = new short[servers.length-1];

    int j = 0;
    for (int i=0; i<servers.length; i++) {
      if (id == servers[i]) {
        idx = i;
        continue;
      }
      newServers[j] = servers[i];
      newBootTS[j] = bootTS[i];
      newStamp[j] = stamp[i];
      j++;
    }
    if (idx > 0)
      System.arraycopy(stampBuf, 0, newStampBuf, 0, idx*4);
    if (idx < (servers.length-1))
      System.arraycopy(stampBuf, (idx+1)*4,
                       newStampBuf, idx*4, (servers.length-idx-1)*4);

    stamp = newStamp;
    stampBuf = newStampBuf;
    servers = newServers;
    // be careful, set again the index of local server.
    idxLS = index(sid);

    // Save the servers configuration and the logical time stamp.
    AgentServer.transaction.save(servers, serversFN);
    AgentServer.transaction.save(bootTS, bootTSFN);
    AgentServer.transaction.saveByteArray(stampBuf, name);
  }

  /**
   * Reset all information related to the specified server in the
   * logical clock.
   *
   * @param id	the unique server id.
   */
  synchronized void resetServer(short id) throws IOException {
    // First we have to verify that id is already in servers
    int idx = index(id);
    if (idx < 0) return;

    // TODO...

    // Save the servers configuration and the logical time stamp.
    AgentServer.transaction.save(servers, serversFN);
    AgentServer.transaction.save(bootTS, bootTSFN);
    AgentServer.transaction.saveByteArray(stampBuf, name);
  }

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
  synchronized int testRecvUpdate(Update update) throws IOException {
    int fromIdx = index(update.getFromId());

    if (update.getBootTS() != bootTS[fromIdx]) {
      bootTS[fromIdx] = update.getBootTS();
      stamp[fromIdx] = -1;
      AgentServer.transaction.save(bootTS, bootTSFN);
    }
    if (update.stamp > stamp[fromIdx]) {
      stamp[fromIdx] = update.stamp;
      stampBuf[(fromIdx*4)+0] = (byte)((stamp[fromIdx] >>> 24) & 0xFF);
      stampBuf[(fromIdx*4)+1] = (byte)((stamp[fromIdx] >>> 16) & 0xFF);
      stampBuf[(fromIdx*4)+2] = (byte)((stamp[fromIdx] >>>  8) & 0xFF);
      stampBuf[(fromIdx*4)+3] = (byte)(stamp[fromIdx] & 0xFF);
      AgentServer.transaction.saveByteArray(stampBuf, name);
      return DELIVER;
    }
    return ALREADY_DELIVERED;
  }

  /**
   * Computes the matrix clock of a send message. The server's
   * matrix clock is updated.
   *
   * @param to	The identification of receiver.	
   * @return	The message matrix clock (list of update).
   */
  synchronized Update getSendUpdate(short to) throws IOException {
    Update update = Update.alloc(sid, to, ++stamp[idxLS]);
    update.setBootTS(bootTS[idxLS]);
    stampBuf[(idxLS*4)+0] = (byte)((stamp[idxLS] >>> 24) & 0xFF);
    stampBuf[(idxLS*4)+1] = (byte)((stamp[idxLS] >>> 16) & 0xFF);
    stampBuf[(idxLS*4)+2] = (byte)((stamp[idxLS] >>>  8) & 0xFF);
    stampBuf[(idxLS*4)+3] = (byte)(stamp[idxLS] & 0xFF);
    AgentServer.transaction.saveByteArray(stampBuf, name);
    return update;
  }

  /**
   * Returns a string representation of this <code>SimpleClock</code> object
   * in the form of a set of entries, enclosed in braces and separated by the
   * String ", " (characters comma and space).
   *
   * @return String representation of this <code>SimpleClock</code> object.
   */
  public String toString() {
    StringBuffer strBuf = new StringBuffer();

    strBuf.append('(').append(super.toString()).append(',');
    for (int i=0; i<servers.length; i++) {
      strBuf.append('(').append(servers[i]).append(',');
      strBuf.append(stamp[i]).append(')');
    }
    strBuf.append(')');

    return strBuf.toString();
  }   
}
