/*
 * Copyright (C) 2003 - 2004 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package fr.dyade.aaa.agent;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 *  <code>FIFONetwork</code> is a base implementation for network components
 * with a simple FIFO message ordering.
 */
public abstract class FIFONetwork extends StreamNetwork {
  /**
   * Creates a new network component. This simple constructor is required
   * by subclasses.
   */
  public FIFONetwork() {}

  LogicalClock createsLogicalClock(String name, short[] servers) {
    return new SimpleClock(name, servers);
  }

  /**
   * Try to deliver the received message to the right consumer.
   *
   * @param msg		the message.
   */
  protected void deliver(Message msg) throws Exception {
    // Get real from serverId.
    short from = msg.getFromId();

    // Test if the message is really for this node (final destination or
    // router).
    short dest = msg.getToId();
    if (dest != AgentServer.getServerId()) {
      logmon.log(BasicLevel.ERROR,
                 getName() + ", recv bad msg#" + msg.getStamp() +
                 " really to " + dest +
                 " by " + from);
      throw new Exception("recv bad msg#" + msg.getStamp() +
                          " really to " + dest +
                          " by " + from);
    }

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 getName() + ", recv msg#" + msg.getStamp() +
                 " from " + msg.from +
                 " to " + msg.to +
                 " by " + from);

    AgentServer.getServerDesc(from).active = true;
    AgentServer.getServerDesc(from).retry = 0;

    // Start a transaction in order to ensure atomicity of clock updates
    // and queue changes.
    AgentServer.transaction.begin();

    // Test if the message can be delivered then deliver it
    // else put it in the waiting list
    int todo = clock.testRecvUpdate(msg.getUpdate());

    if (todo == LogicalClock.DELIVER) {
      // Deliver the message then try to deliver alls waiting message.
      // Allocate a local time to the message to order it in
      // local queue, and save it.
      Channel.post(msg);

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", deliver msg#" + msg.getStamp());

      Channel.save();
      AgentServer.transaction.commit();
      // then commit and validate the message.
      Channel.validate();
      AgentServer.transaction.release();
    } else {
//    it's an already delivered message, we have just to re-send an
//    aknowledge (see below).
      AgentServer.transaction.commit();
      AgentServer.transaction.release();
    }
  }
}
