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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 * The <code>TransactionEngine</code> class provides a persistent
 * implementation of Engine.
 */
final class TransactionEngine extends Engine {
  /** Logical timestamp information for messages in "local" domain. */
  private int stamp;

  /** Buffer used to optimise */
  private byte[] stampBuf = null;

  /** True if the timestamp is modified since last save. */
  private boolean modified = false;

  TransactionEngine() throws Exception {
    super();

    restore();
    if (modified) save();
  }

  /**
   * Saves logical clock information to persistent storage.
   */
  public void save() throws IOException {
    if (modified) {
      stampBuf[0] = (byte)((stamp >>> 24) & 0xFF);
      stampBuf[1] = (byte)((stamp >>> 16) & 0xFF);
      stampBuf[2] = (byte)((stamp >>>  8) & 0xFF);
      stampBuf[3] = (byte)(stamp & 0xFF);
      AgentServer.transaction.saveByteArray(stampBuf, getName());
      modified = false;
    }
  }

  /**
   * Restores logical clock information from persistent storage.
   */
  public void restore() throws Exception {
    stampBuf = AgentServer.transaction.loadByteArray(getName());
    if (stampBuf == null) {
      stamp = 0;
      stampBuf = new byte[4];
      modified = true;
    } else {
      stamp = ((stampBuf[0] & 0xFF) << 24) +
        ((stampBuf[1] & 0xFF) << 16) +
        ((stampBuf[2] & 0xFF) <<  8) +
        (stampBuf[3] & 0xFF);
      modified = false;
    }
  }

  /**
   *  Adds a message in "ready to deliver" list. This method allocates a
   * new time stamp to the message ; be Careful, changing the stamp imply
   * the filename change too.
   */
  public void post(Message msg) throws Exception {
    modified = true;

    msg.source = AgentServer.getServerId();
    msg.dest = AgentServer.getServerId();
    msg.stamp = ++stamp;
    msg.save();

    qin.push(msg);
  }

  /**
   * Commit the agent reaction in case of rigth termination:<ul>
   * <li>suppress the processed notification from message queue,
   * then deletes it ;
   * <li>push all new notifications in qin and qout, and saves them ;
   * <li>saves the agent state ;
   * <li>then commit the transaction to validate all changes.
   * </ul>
   */
  void commit() throws Exception {
    AgentServer.transaction.begin();
    // Suppress the processed notification from message queue ..
    qin.pop();
    // .. then deletes it ..
    msg.delete();
    // .. and frees it.
    msg.free();
    // Post all notifications temporary keeped in mq in the rigth consumers,
    // then saves changes.
    dispatch();
    // Saves the agent state then commit the transaction.
    if (agent != null) agent.save();
    AgentServer.transaction.commit();
    // The transaction has commited, then validate all messages.
    Channel.validate();
    AgentServer.transaction.release();
  }

  /**
   * Abort the agent reaction in case of error during execution. In case
   * of unrecoverable error during the reaction we have to rollback:<ul>
   * <li>reload the previous state of agent ;
   * <li>remove the failed notification ;
   * <li>clean the Channel queue of all pushed notifications ;
   * <li>send an error notification to the sender ;
   * <li>then commit the transaction to validate all changes.
   * </ul>
   */
  void abort(Exception exc) throws Exception {
    AgentServer.transaction.begin();
    // Reload the state of agent.
    try {
      agent = reload(msg.to);
    } catch (Exception exc2) {
      logmon.log(BasicLevel.ERROR,
                 getName() + ", can't reload Agent" + msg.to, exc2);
      throw new Exception("Can't reload Agent" + msg.to);
    }

    // Remove the failed notification ..
    qin.pop();
    // .. then deletes it ..
    msg.delete();
    // .. and frees it.
    msg.free();
    // Clean the Channel queue of all pushed notifications.
    clean();
    // Send an error notification to client agent.
    push(AgentId.localId,
         msg.from,
         new ExceptionNotification(msg.to, msg.not, exc));
    dispatch();
    AgentServer.transaction.commit();
    // The transaction has commited, then validate all messages.
    Channel.validate();
    AgentServer.transaction.release();
  }
}
