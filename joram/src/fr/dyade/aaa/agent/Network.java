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
 * The <code>Network</code> abstract class provides ..
 */
public abstract class Network implements MessageConsumer, NetworkMBean {
  /**
   *  Period of time in ms between two activations of watch-dog thread,
   * default value is 1000L (1 second).
   *  This value can be adjusted for all network components by setting
   * <code>WDActivationPeriod</code> global property or for a particular
   * network by setting <code>\<DomainName\>.WDActivationPeriod</code>
   * specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  long WDActivationPeriod = 1000L;
  /**
   *  Number of try at stage 1, default value is 30.
   *  This value can be adjusted for all network components by setting
   * <code>WDNbRetryLevel1</code> global property or for a particular
   * network by setting <code>\<DomainName\>.WDNbRetryLevel1</code>
   * specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int  WDNbRetryLevel1 = 30;
  /**
   *  Period of time in ms between two connection try at stage 1, default
   * value is WDActivationPeriod divided by 2.
   *  This value can be adjusted for all network components by setting
   * <code>WDRetryPeriod1</code> global property or for a particular
   * network by setting <code>\<DomainName\>.WDRetryPeriod1</code>
   * specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  long WDRetryPeriod1 = WDActivationPeriod/2;
  /**
   *  Number of try at stage 2, default value is 55.
   *  This value can be adjusted for all network components by setting
   * <code>WDNbRetryLevel2</code> global property or for a particular
   * network by setting <code>\<DomainName\>.WDNbRetryLevel2</code>
   * specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  int  WDNbRetryLevel2 = 55;
  /**
   *  Period of time in ms between two connection try at stage 2, default
   * value is 5000L (5 seconds).
   *  This value can be adjusted for all network components by setting
   * <code>WDRetryPeriod2</code> global property or for a particular
   * network by setting <code>\<DomainName\>.WDRetryPeriod2</code>
   * specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  long WDRetryPeriod2 = 5000L;
  /**
   *  Period of time in ms between two connection try at stage 3, default
   * value is 60000L (1 minute).
   *  This value can be adjusted for all network components by setting
   * <code>WDRetryPeriod3</code> global property or for a particular
   * network by setting <code>\<DomainName\>.WDRetryPeriod3</code>
   * specific property.
   * <p>
   *  Theses properties can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  long WDRetryPeriod3 = 60000L;

  protected Logger logmon = null;

  /**
   * Reference to the current network component in order to be used
   * by inner daemon's.
   */
  protected Network network;
  /** The component's name as it appears in logging. */
  protected String name;
  /** The domain name. */
  protected String domain;
  /** The communication port. */
  protected int port;
  /** The <code>MessageVector</code> associated with this network component. */
  protected MessageVector qout;
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
  }

  /**
   * Insert a message in the <code>MessageQueue</code>.
   * This method is used during initialisation to restore the component
   * state from persistent storage.
   *
   * @param msg		the message
   */
  public void insert(Message msg) {
    if (msg.getFromId() == AgentServer.getServerId()) {
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

  /**
   *  Creates a new <code>LogicalClock</code> for this Network component.
   * This method should be defined in subclass depending of implementation
   * message ordering (FIFO, causal, etc.).
   */
  abstract LogicalClock createsLogicalClock(String name, short[] servers);

  /**
   *  Sets the <code>LogicalClock</code> for this Network component.
   * This method is normally used to initialize the clock of a new slave
   * server in HA mode.
   */
  final void setLogicalClock(LogicalClock clock) {
    this.clock = clock;
  }

  /**
   *  Gets the <code>LogicalClock</code> of this Network component.
   * This method is normally used to capture the current clock of the
   * master server in HA mode, then initialize a new slave.
   */
  final LogicalClock getLogicalClock() {
    return clock;
  }

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
    this.name = AgentServer.getName() + '.' + name;

    qout = new MessageVector(this.name,
                            AgentServer.getTransaction().isPersistent());
    waiting = new Vector();

    this.domain = name;
    this.port = port;

    // Get the logging monitor from current server MonologLoggerFactory
    // Be careful, logmon is initialized from name and not this.name !!
    logmon = Debug.getLogger(Debug.A3Network + '.' + name);
    logmon.log(BasicLevel.DEBUG, name + ", initialized");

    WDActivationPeriod = Long.getLong("WDActivationPeriod",
                                      WDActivationPeriod).longValue();
    WDActivationPeriod = Long.getLong(name + ".WDActivationPeriod",
                                      WDActivationPeriod).longValue();

    WDNbRetryLevel1 = Integer.getInteger("WDNbRetryLevel1",
                                         WDNbRetryLevel1).intValue();
    WDNbRetryLevel1 = Integer.getInteger(name + ".WDNbRetryLevel1",
                                         WDNbRetryLevel1).intValue();

    WDRetryPeriod1 = Long.getLong("WDRetryPeriod1",
                                  WDRetryPeriod1).longValue();
    WDRetryPeriod1 = Long.getLong(name + ".WDRetryPeriod1",
                                  WDRetryPeriod1).longValue();

    WDNbRetryLevel2 = Integer.getInteger("WDNbRetryLevel2",
                                         WDNbRetryLevel2).intValue();
    WDNbRetryLevel2 = Integer.getInteger(name + ".WDNbRetryLevel2",
                                         WDNbRetryLevel2).intValue();

    WDRetryPeriod2 = Long.getLong("WDRetryPeriod2",
                                  WDRetryPeriod2).longValue();
    WDRetryPeriod2 = Long.getLong(name + ".WDRetryPeriod2",
                                  WDRetryPeriod2).longValue();

    WDRetryPeriod3 = Long.getLong("WDRetryPeriod3",
                                  WDRetryPeriod3).longValue();
    WDRetryPeriod3 = Long.getLong(name + ".WDRetryPeriod3",
                                  WDRetryPeriod3).longValue();

    // Sorts the array of server ids into ascending numerical order.
    Arrays.sort(servers);
    // then get the logical clock.
    clock = createsLogicalClock(this.name, servers);
    clock.load();
  }

  /**
   * Adds the server sid in the network configuration.
   *
   * @param sid	the unique server id.
   */
  public void addServer(short sid) throws Exception {
    clock.addServer(sid);
  }

  /**
   * Removes the server sid in the network configuration.
   *
   * @param sid	the unique server id.
   */
  public void delServer(short sid) throws Exception {
    clock.delServer(sid);
  }

  /**
   * Reset all information related to server sid in the network configuration.
   *
   * @param sid	the unique server id.
   */
  void resetServer(short sid) throws IOException {
    clock.resetServer(sid);
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
    msg.setUpdate(clock.getSendUpdate(to));
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
   * Updates the network port.
   */
  public void setPort(int port) {
    this.port = port;
  }

  public final int getPort() {
    return port;
  }
}
