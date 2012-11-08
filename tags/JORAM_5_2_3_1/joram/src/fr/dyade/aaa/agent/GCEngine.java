/*
 * Copyright (C) 2004 - 2005 ScalAgent Distributed Technologies
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

import org.objectweb.util.monolog.api.BasicLevel;

/**
 *  Implementation of Engine that used Group-Commit in order to improve 
 * performance.
 */
final class GCEngine extends Engine {

  int loop = 0;
  int NbMaxLoop = 50;

  GCEngine() throws Exception {
    super();

    NbMaxLoop = AgentServer.getInteger("NbMaxLoop", NbMaxLoop).intValue();
    needToBeCommited = false;
  }

  /**
   * Commit the agent reaction in case of right termination:
   * <ul>
   * <li>suppress the processed notification from message queue, then deletes
   * it ;
   * <li>push all new notifications in qin and qout, and saves them ;
   * <li>saves the agent state ;
   * <li>then commit the transaction to validate all changes.
   * </ul>
   */
  void commit() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, getName() + ", commit reaction");
    }
    loop += 1;

    AgentServer.getTransaction().begin();
    // Suppress the processed notification from message queue ..
    qin.pop();
    // .. then deletes it ..
    msg.delete();
    // .. and frees it.
    msg.free();
    // Dispatch local messages
    for (int i=0; i<mq.size(); ) {
      Message m = (Message) mq.elementAt(i);
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, getName() + ", dispatch: " + m);
      }
      if (m.to.getTo() == AgentServer.getServerId()) {
        // !AF! Need to be synchronized in order to avoid interaction
        // !AF! in stamp handling.
        post(m);
        mq.removeElementAt(i);
      } else {
        i += 1;
      }
    }
    // !AF! It's dangerous to call validate outside of a transaction,
    // !AF! we really need to enclose this code in a begin/release
    validate();

    // Saves the agent state then commit the transaction.
    if (agent != null) agent.save();

    if (needToBeCommited || (qin.size() == 0) || (loop > NbMaxLoop)) {
      if (logmon.isLoggable(BasicLevel.INFO)) {
        logmon.log(BasicLevel.INFO, getName() + ", commit: " + loop);
      }
      loop = 0;

      // Post all notifications temporary keeped in mq in the rigth consumers,
      // then saves changes.
      dispatch();
      AgentServer.getTransaction().commit(false);
      // The transaction has commited, then validate all messages.
      Channel.validate();
    }
    AgentServer.getTransaction().release();
  }
}
