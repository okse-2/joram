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
 *
 * Initial developer(s): Dyade
 * Contributor(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Arrays;

/**
 *  <code>CausalNetwork</code> is a base implementation for network components
 * with a causal message ordering.
 */
public abstract class CausalNetwork extends StreamNetwork {
  /**
   * Creates a new network component. This simple constructor is required
   * by subclasses.
   */
  public CausalNetwork() {}


  LogicalClock createsLogicalClock(String name, short[] servers) {
    return new MatrixClock(name, servers);
  }

  /**
   * Try to deliver the received message to the right consumer.
   *
   * @param msg		the message.
   */
  public void deliver(Message msg) throws Exception {
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

    // Test if the message can be delivered then deliver it
    // else put it in the waiting list
    int todo = clock.testRecvUpdate(msg.getUpdate());
    if (todo == LogicalClock.DELIVER) {
      // Deliver the message then try to deliver alls waiting message.
      AgentServer.transaction.begin();
      // Allocate a local time to the message to order it in
      // local queue, and save it.
      Channel.post(msg);

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", deliver msg#" + msg.getStamp());
      scanlist:
      while (true) {
	for (int i=0; i<waiting.size(); i++) {
	  Message tmpMsg = (Message) waiting.elementAt(i);
	  if (clock.testRecvUpdate(tmpMsg.getUpdate()) == LogicalClock.DELIVER) {
	    // Be Careful, changing the stamp imply the filename
	    // change !! So we have to delete the old file.
	    tmpMsg.delete();
	    //  Deliver the message, then delete it from list.
	    Channel.post(tmpMsg);
	    waiting.removeElementAt(i);

            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         getName() + ",	 deliver msg#" + tmpMsg.getStamp());

	    // logical time has changed we have to rescan the list.
	    continue scanlist;
	  }
	}
	//  We have scan the entire list without deliver any message
	// so we leave the loop.
	break scanlist;
      }
      Channel.save();
      AgentServer.transaction.commit();
      // then commit and validate the message.
      Channel.validate();
      AgentServer.transaction.release();
    } else if (todo == LogicalClock.WAIT_TO_DELIVER) {
      AgentServer.transaction.begin();
      // Insert in a waiting list.
      msg.save();
      waiting.addElement(msg);
      AgentServer.transaction.commit();
      AgentServer.transaction.release();
      
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", block msg#" + msg.getStamp());
    } else {
//    it's an already delivered message, we have just to re-send an
//    aknowledge (see below).
    }
  }
}
