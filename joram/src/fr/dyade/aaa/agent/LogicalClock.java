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

abstract class LogicalClock implements Serializable {
  protected String name = null;
  /**
   * List of id. for all servers in the domain, this list is sorted and
   * is used as index for status and matrix arrays. Be careful, this array
   * is shared with the <code>Network</code> components (may be it should
   * be saved by this component).
   */
  protected short[] servers;
  /** Filename for servers storage */
  transient protected String serversFN = null;

  /** True if the timestamp is modified since last save. */
  transient protected boolean modified = false;

  /** The message can be delivered. */
  static final int DELIVER = 0;
  /** There is other message in the causal ordering before this.*/
  static final int WAIT_TO_DELIVER = 1;
  /** The message has already been delivered. */
  static final int ALREADY_DELIVERED = 2;
 
  transient protected Logger logmon = null;
   
  /**
   * Creates a new logical clock. Be careful, the list of servers must be
   * sorted into ascending numerical order, this list must be also used in
   * Network component.
   *
   * @param name	Name of domain.
   * @param servers	List of domain's server id.
   */
  protected LogicalClock(String name, short[] servers) {
    this.name = name;
    this.servers = servers;
    this.serversFN = name + "Servers";
    // Get the logging monitor from current server MonologLoggerFactory
    this.logmon = Debug.getLogger(Debug.A3Debug + ".LogicalClock." + name);
  }

  /**
   * Saves logical clock information to persistent storage.
   */
  abstract void save() throws IOException;
    
  /**
   * Restores logical clock information from persistent storage.
   * If servers is null, then we have just to restore the matrix,
   * else it's the first loading at initialization time and we have
   * to restore it.
   */
  abstract void load()throws IOException, ClassNotFoundException;

  abstract void addServer(String name, short sid) 
    throws IOException;

  abstract void delServer(String name, short sid) 
    throws IOException;

  /**
   * Returns the index of the specified server.
   */
  protected final int index(short id) {
    int idx = Arrays.binarySearch(servers, id);
    return idx;
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
  abstract int testRecvUpdate(Update update);

  /**
   * Computes the matrix clock of a send message. The server's
   * matrix clock is updated.
   *
   * @param to	The identification of receiver.	
   * @return	The message matrix clock (list of update).
   */
  abstract Update getSendUpdate(short to);
}
