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
import java.util.Vector;

abstract class Network implements MessageConsumer {
  /** RCS version number of this file: $Revision: 1.4 $ */
  public static final String RCS_VERSION="@(#)$Id: Network.java,v 1.4 2001-05-04 14:54:51 tachkeni Exp $";

  /** Time between two activation of watch-dog thread (default 10 seconds) */
  final static long WDActivationPeriod = 10000L; // 10 seconds
  /** Number of try at stage 1 (default 6 times) */
  final static int  WDNbRetryLevel1 = 6;
  /** Time between two sending at stage 1 (default 15 seconds) */
  final static long WDRetryPeriod1 = 15000L;	 // 15 seconds
  /** Number of try at stage 2 (default 12 times) */
  final static int  WDNbRetryLevel2 = 12;
  /** Time between two sending at stage 2 (default 2 minutes) */
  final static long WDRetryPeriod2 = 120000L;	 // 2 minutes
  /** time between two sending at stage 3 (default 30 minutes) */
  final static long WDRetryPeriod3 = 1800000L;	 // 30 minutes

  /**
   * Reference to the current network component in order to be used
   * by inner daemon's.
   */
  Network network;
  /** The domain name. */
  String name;
  /** The communication port. */
  int port;
  /** The <code>MessageQueue</code> associated with this network component. */
  MessageQueue qout;
  /**
   * List of id. for all servers in the domain, this list is sorted.
   * Be careful, this array is shared with the <code>MatrixClock</code>
   * components.
   */
  short[] servers;
  /** The matrix clock associated to this network component. */
  MatrixClock mclock;
  /**
   * The waiting list: it contains all messages that waiting to be delivered.
   */
  Vector waiting;

  /**
   * Returns this session's name.
   *
   * @return this session's name.
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns a string representation of this consumer.
   *
   * @return	A string representation of this consumer. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append(getName()).append("[")
      .append(getClass().getName()).append("]\t")
      .append("qout=[").append(qout.size()).append("]");

    return strbuf.toString();
  }

  /**
   * Creates a new network component. This simple constructor is required in
   * order to use <code>Class.newInstance()</code> method during configuration.
   * The configuration of component is then done by <code>init</code> method.
   */
  Network() {
    network = this;
    qout = new MessageQueue();
    waiting = new Vector();
  }

  /**
   * Insert a message in the <code>MessageQueue</code>.
   * This method is used during initialisation to restore the component
   * state from persistent storage.
   *
   * @param msg		the message
   */
  public final void insert(Message msg) {
    if (msg.update.getFromId() == AgentServer.getServerId()) {
      // The update has been locally generated, the message is ready to
      // deliver, we have to insert it in the queue.
      qout.insert(msg);
    } else {
      // The update has been generated on a remote server. If the message
      // don't have a local update, It is waiting to be delivered. So we
      // have to insert it in waiting list.
      addRecvMessage(msg);
    }
  }

  /**
   * Adds a message in waiting list. This method is used to retain messages
   * that cannot be delivered now. Each message in this list is evaluated
   * (see <code>MatrixClock.testRecvUpdate()</code>) each time a new message
   * succeed.
   * <p><hr>
   * This method is also used to fill the waiting list during initialisation.
   *
   * @param msg		the message
   */
  final void addRecvMessage(Message msg) {
    waiting.addElement(msg);
  }

  /**
   * Initializes a new network component. This method is used in order to
   * easily creates and configure a Network component from a class name.
   * So we can use the <code>Class.newInstance()</code> method for create
   * (whitout any parameter) the component, then we can initialize it with
   * this method.<br>
   * This method initializes the logical clock for the domain.
   *
   * @param name	The domain name.
   * @param port	The listen port.
   * @param servers	The list of servers directly accessible from this
   *			network interface.
   */
  public void init(String name, int port, short[] servers) throws Exception {
    this.name = name;
    this.port = port;
    this.servers = servers;
    // Sorts the array of server ids into ascending numerical order.
    java.util.Arrays.sort(servers);
    // then get the logical clock.
    mclock = MatrixClock.load(name, servers);
  }

  /**
   * Saves logical clock information to persistent storage.
   */
  public void save() throws IOException {
    mclock.save(name);
  }

  /**
   * Restores logical clock information from persistent storage.
   */
  public void restore() throws Exception {
    mclock = MatrixClock.load(name, null);
  }

  /**
   * Validates all messages pushed in queue during transaction session.
   */
  public void validate() {
    qout.validate();
  }

  /**
   * Invalidates all messages pushed in queue during transaction session.
   */
  public void invalidate() {
    qout.invalidate();
  }

  public MessageQueue getQueue() {
    return qout;
  }

  /**
   * Returns the index of the specified server.
   */
  final int index(short id) {
    return java.util.Arrays.binarySearch(servers, id);
  }

  /**
   * Try to deliver the received message to the right consumer.
   *
   * @param msg		the message.
   */
  public void deliver(Message msg) throws IOException {
    // Get real from serverId.
    short from = msg.update.getFromId();

    if (Debug.debug && Debug.message)
      Debug.trace(getName() + ": recv msg#" + msg.update.stamp +
		  " from " + msg.from +
		  " to " + msg.to +
		  " by " + from, false);

    AgentServer.getServerDesc(from).active = true;
    AgentServer.getServerDesc(from).retry = 0;

    // Test if the message can be delivered then deliver it
    // else put it in the waiting list
    int todo = mclock.testRecvUpdate(msg.update);
    if (todo == MatrixClock.DELIVER) {
      // Deliver the message then try to deliver alls waiting message.
      AgentServer.transaction.begin();
      // Allocate a local time to the message to order it in
      // local queue, and save it.
      Channel.post(msg);

      if (Debug.debug && Debug.message)
	Debug.trace(getName() + ": deliver msg#" + msg.update.stamp, false);
      scanlist:
      while (true) {
	for (int i=0; i<waiting.size(); i++) {
	  Message tmpMsg = (Message) waiting.elementAt(i);
	  if (mclock.testRecvUpdate(tmpMsg.update) == MatrixClock.DELIVER) {
	    // Be Careful, changing the stamp imply the filename
	    // change !! So we have to delete the old file.
	    tmpMsg.delete();
	    //  Deliver the message, then delete it from list.
	    Channel.post(tmpMsg);
	    waiting.removeElementAt(i);

	    if (Debug.debug && Debug.message)
	      Debug.trace(getName() + ": deliver msg#" + tmpMsg.update.stamp,
			  false);

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
    } else if (todo == MatrixClock.WAIT_TO_DELIVER) {
      AgentServer.transaction.begin();
      // Insert in a waiting list.
      msg.save();
      waiting.addElement(msg);
      AgentServer.transaction.commit();
      AgentServer.transaction.release();
      
      if (Debug.debug && Debug.message)
	Debug.trace(getName() + ": block msg#" + msg.update.stamp, false);
    } // else {
//    it's an already delivered message, we have just to re-send an
//    aknowledge (see below).
//  }
  }

  /**
   *  Adds a message in "ready to deliver" list. This method allocates a
   * new timestamp to the message ; be Careful, changing the stamp imply
   * the filename change too.
   *
   * @param msg		the message.
   */
  public void post(Message msg) throws IOException {
    // Allocates a new timestamp. Be careful, if the message needs to be
    // routed we have to use the next destination in timestamp generation.
    msg.update = mclock.getSendUpdate(AgentServer.servers[msg.to.to].gateway);
    // Saves the message.
    msg.save();
    // Push it in "ready to deliver" queue.
    qout.push(msg);
  }

  /**
   * Wakes up the watch-dog thread.
   */
  public abstract void wakeup();
}
