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
  /** Logical timestamp information for messages in domain, stamp[0] for
   * messages sent, and stamp[1] for messages received.
   */
  private int[][] stamp;
    
  /** Buffer used to optimise transactions*/
  private byte[] stampBuf = null;

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
    if (modified) {
      AgentServer.transaction.saveByteArray(stampBuf, name);
      modified = false;
    }
  }
    
  /**
   * Restores logical clock information from persistent storage.
   * If servers is null, then we have just to restore the matrix,
   * else it's the first loading at initialization time and we have
   * to restore it.
   */
  void load()throws IOException, ClassNotFoundException {
    // Loads the logical clock.
    stampBuf = AgentServer.transaction.loadByteArray(name);
    if (stampBuf ==  null) {
      // Creates the new stamp, then saves it
      stampBuf = new byte[2*4*servers.length];
      stamp = new int[2][servers.length];
      // Save the servers configuration and the logical time stamp.
      AgentServer.transaction.save(servers, name + "Servers");
      modified = true;
      save();
    } else {
      short[] s = (short[]) AgentServer.transaction.load(serversFN);
      stamp = new int[2][s.length];
      for (int i=0; i<servers.length; i++) {
        stamp[0][i] = ((stampBuf[(i*8)+0] & 0xFF) << 24) +
          ((stampBuf[(i*8)+1] & 0xFF) << 16) +
          ((stampBuf[(i*8)+2] & 0xFF) <<  8) +
          (stampBuf[(i*8)+3] & 0xFF);
        stamp[1][i] = ((stampBuf[(i*8)+4] & 0xFF) << 24) +
          ((stampBuf[(i*8)+5] & 0xFF) << 16) +
          ((stampBuf[(i*8)+6] & 0xFF) <<  8) +
          (stampBuf[(i*8)+7] & 0xFF);
      }
      // Join with the new domain configuration:
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

  synchronized void addServer(String name, short sid) 
    throws IOException {
    // First we have to verify that sid is not already in servers
    int idx = index(sid);
    if (idx >= 0) return;
    idx = -idx -1;
    // Allocates new array for stamp and server
    int[][] newStamp = new int[2][servers.length+1];
    byte[] newStampBuf = new byte[2*4*(servers.length+1)];
    short[] newServers = new short[servers.length+1];
    // Copy old data from stamp and server, let a free room for the new one.
    int j = 0;
    for (int i=0; i<servers.length; i++) {
      if (i == idx) j++;
      newServers[j] = servers[i];
      newStamp[0][j] = stamp[0][i];
      newStamp[1][j] = stamp[1][i];
      j++;
    }
    if (idx > 0)
      System.arraycopy(stampBuf, 0, newStampBuf, 0, idx*8);
    if (idx < servers.length)
      System.arraycopy(stampBuf, idx*8,
                       newStampBuf, (idx+1)*8, (servers.length-idx)*8);

    newServers[idx] = sid;
    newStamp[0][idx] = 0;				// useless
    newStamp[1][idx] = 0;				// useless
    newStampBuf[idx] = 0; newStampBuf[idx+1] = 0;	// useless
    newStampBuf[idx+2] = 0; newStampBuf[idx+3] = 0; 	// useless
    newStampBuf[idx+4] = 0; newStampBuf[idx+5] = 0; 	// useless
    newStampBuf[idx+6] = 0; newStampBuf[idx+7] = 0; 	// useless

    stamp = newStamp;
    stampBuf = newStampBuf;
    servers = newServers;

    // Save the servers configuration and the logical time stamp.
    AgentServer.transaction.save(servers, serversFN);
    modified = true;
    save();
  }

  synchronized void delServer(String name, short sid) 
    throws IOException {
    // First we have to verify that sid is already in servers
    int idx = index(sid);
    if (idx < 0) return;

    int[][] newStamp = new int[2][servers.length-1];
    byte[] newStampBuf = new byte[2*4*(servers.length-1)];
    short[] newServers = new short[servers.length-1];

    int j = 0;
    for (int i=0; i<servers.length; i++) {
      if (sid == servers[i]) {
        idx = i;
        continue;
      }
      newServers[j] = servers[i];
      newStamp[0][j] = stamp[0][i];
      newStamp[1][j] = stamp[1][i];
      j++;
    }
    if (idx > 0)
      System.arraycopy(stampBuf, 0, newStampBuf, 0, idx*8);
    if (idx < (servers.length-1))
      System.arraycopy(stampBuf, (idx+1)*8,
                       newStampBuf, idx*8, (servers.length-idx-1)*8);


    stamp = newStamp;
    stampBuf = newStampBuf;
    servers = newServers;

    // Save the servers configuration and the logical time stamp.
    AgentServer.transaction.save(servers, serversFN);
    modified = true;
    save();
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
  synchronized int testRecvUpdate(Update update) {
    int fromIdx = index(update.getFromId());

    if (update.stamp == (stamp[1][fromIdx] +1)) {
      stamp[1][fromIdx] += 1;
      stampBuf[(fromIdx*8)+4] = (byte)((stamp[1][fromIdx] >>> 24) & 0xFF);
      stampBuf[(fromIdx*8)+5] = (byte)((stamp[1][fromIdx] >>> 16) & 0xFF);
      stampBuf[(fromIdx*8)+6] = (byte)((stamp[1][fromIdx] >>>  8) & 0xFF);
      stampBuf[(fromIdx*8)+7] = (byte)(stamp[1][fromIdx] & 0xFF);
      modified = true;
      return DELIVER;
    } else if (update.stamp > (stamp[1][fromIdx] +1)) {
      return WAIT_TO_DELIVER;
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
  synchronized Update getSendUpdate(short to) {
    int toIdx = index(to);
    Update update = Update.alloc(AgentServer.getServerId(),
                                 to,
                                 ++stamp[0][toIdx]);
    stampBuf[(toIdx*8)+0] = (byte)((stamp[0][toIdx] >>> 24) & 0xFF);
    stampBuf[(toIdx*8)+1] = (byte)((stamp[0][toIdx] >>> 16) & 0xFF);
    stampBuf[(toIdx*8)+2] = (byte)((stamp[0][toIdx] >>>  8) & 0xFF);
    stampBuf[(toIdx*8)+3] = (byte)(stamp[0][toIdx] & 0xFF);
    modified = true;
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
      strBuf.append(stamp[0][i]).append(',').append(stamp[1][i]).append(')');
    }
    strBuf.append(')');

    return strBuf.toString();
  }   
}
