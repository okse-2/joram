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
  int[][] stamp;
    
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
      AgentServer.transaction.save(stamp, name);
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
    stamp = (int[][]) AgentServer.transaction.load(name);
    if (stamp ==  null) {
      // Creates the new stamp, then saves it
      stamp = new int[2][servers.length];
      // Save the servers configuration and the logical time stamp.
      AgentServer.transaction.save(servers, name + "Servers");
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
    newServers[idx] = sid;
    newStamp[0][idx] = 0;
    newStamp[1][idx] = 0;

    stamp = newStamp;
    servers = newServers;

    // Save the servers configuration and the logical time stamp.
    AgentServer.transaction.save(servers, serversFN);
    AgentServer.transaction.save(stamp, name);
  }

  synchronized void delServer(String name, short sid) 
    throws IOException {
    // First we have to verify that sid is already in servers
    int idx = index(sid);
    if (idx < 0) return;

    int[][] newStamp = new int[2][servers.length-1];
    short[] newServers = new short[servers.length-1];

    int j = 0;
    for (int i=0; i<servers.length; i++) {
      if (sid == servers[i]) continue;
      newServers[j] = servers[i];
      newStamp[0][j] = stamp[0][i];
      newStamp[1][j] = stamp[1][i];
      j++;
    }
    stamp = newStamp;
    servers = newServers;

    // Save the servers configuration and the logical time stamp.
    AgentServer.transaction.save(servers, serversFN);
    AgentServer.transaction.save(stamp, name);
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
    modified = true;
    return new Update(AgentServer.getServerId(),
                      to,
                      ++stamp[0][toIdx]);
  }
}
