/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Arrays;

public abstract class Network implements MessageConsumer, NetworkMBean {
  /** Time between two activation of watch-dog thread */
  final static long WDActivationPeriod = 1000L;
  /** Number of try at stage 1 */
  final static int  WDNbRetryLevel1 = 30;
  /** Time between two sending at stage 1 */
  final static long WDRetryPeriod1 = WDActivationPeriod;
  /** Number of try at stage 2 */
  final static int  WDNbRetryLevel2 = 55;
  /** Time between two sending at stage 2 */
  final static long WDRetryPeriod2 = 5000L;
  /** time between two sending at stage 3 */
  final static long WDRetryPeriod3 = 60000L;

  protected Logger logmon = null;

  /**
   * Reference to the current network component in order to be used
   * by inner daemon's.
   */
  protected Network network;
  /** The component's name. */
  protected String name;
  /** The domain name. */
  protected String domain;
  /** The communication port. */
  protected int port;
  /** The <code>MessageQueue</code> associated with this network component. */
  protected MessageQueue qout;
  /** The logical clock associated to this network component. */
  protected LogicalClock clock;
  /**
   * The waiting list: it contains all messages that waiting to be delivered.
   */
  protected Vector waiting;

  /**
   * Returns this session's name.
   *
   * @return this session's name.
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns the corresponding domain's name.
   *
   * @return this domain's name.
   */
  public final String getDomainName() {
    return domain;
  }

  /**
   * Returns a string representation of this consumer.
   *
   * @return	A string representation of this consumer. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append("(").append(super.toString());
    strbuf.append(",name=").append(getName());
    strbuf.append(",qout=").append(qout.size());
    strbuf.append(")");

    return strbuf.toString();
  }

  /**
   * Creates a new network component. This simple constructor is required in
   * order to use <code>Class.newInstance()</code> method during configuration.
   * The configuration of component is then done by <code>init</code> method.
   */
  public Network() {
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
  public void insert(Message msg) {
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
   * (see <code>deliver()</code>) each time a new message succeed.
   * <p><hr>
   * This method is also used to fill the waiting list during initialisation.
   *
   * @param msg		the message
   */
  final void addRecvMessage(Message msg) {
    waiting.addElement(msg);
  }

  abstract public LogicalClock
      createsLogicalClock(String name,
                          short[] servers);

  /**
   * Saves logical clock information to persistent storage.
   */
  public void save() throws IOException {
    clock.save();
  }

  /**
   * Restores logical clock information from persistent storage.
   */
  public void restore() throws Exception {
    clock = createsLogicalClock(name, null);
    clock.load();
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
    this.name = "AgentServer#" + AgentServer.getServerId() + '.' + name;
    this.domain = name;
    this.port = port;

    // Get the logging monitor from current server MonologLoggerFactory
    // Be careful, logmon is initialized from name and not this.name !!
    logmon = Debug.getLogger(Debug.A3Network + '.' + name);
    logmon.log(BasicLevel.DEBUG, name + ", initialized");

    // Sorts the array of server ids into ascending numerical order.
    Arrays.sort(servers);
    // then get the logical clock.
    clock = createsLogicalClock(this.name, servers);
    clock.load();
  }

  public void addServer(short sid) throws Exception {
    clock.addServer(name, sid);
  }

  public void delServer(short sid) throws Exception {
    clock.delServer(name, sid);
  }

  /**
   *  Adds a message in "ready to deliver" list. This method allocates a
   * new time stamp to the message ; be Careful, changing the stamp imply
   * the filename change too.
   */
  public void post(Message msg) throws Exception {
    short to = AgentServer.getServerDesc(msg.to.to).gateway;
    // Allocates a new timestamp. Be careful, if the message needs to be
    // routed we have to use the next destination in timestamp generation.
    msg.update = clock.getSendUpdate(to);
    // Saves the message.
    msg.save();
    // Push it in "ready to deliver" queue.
    qout.push(msg);
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
   * Wakes up the watch-dog thread.
   */
  public abstract void wakeup();
}
