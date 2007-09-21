/*
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
   * Gets the WDActivationPeriod value.
   *
   * @return the WDActivationPeriod value
   */
  public long getWDActivationPeriod() {
    return WDActivationPeriod;
  }

  /**
   * Sets the WDActivationPeriod value.
   *
   * @param WDActivationPeriod	the WDActivationPeriod value
   */
  public void setWDActivationPeriod(long WDActivationPeriod) {
    this.WDActivationPeriod = WDActivationPeriod;
  }

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
   * Gets the WDNbRetryLevel1 value.
   *
   * @return the WDNbRetryLevel1 value
   */
  public int getWDNbRetryLevel1() {
    return WDNbRetryLevel1;
  }

  /**
   * Sets the WDNbRetryLevel1 value.
   *
   * @param WDNbRetryLevel1	the WDNbRetryLevel1 value
   */
  public void setWDNbRetryLevel1(int WDNbRetryLevel1) {
    this.WDNbRetryLevel1 = WDNbRetryLevel1;
  }

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
   * Gets the WDRetryPeriod1 value.
   *
   * @return the WDRetryPeriod1 value
   */
  public long getWDRetryPeriod1() {
    return WDRetryPeriod1;
  }

  /**
   * Sets the WDRetryPeriod1 value.
   *
   * @param WDRetryPeriod1	the WDRetryPeriod1 value
   */
  public void setWDRetryPeriod1(long WDRetryPeriod1) {
    this.WDRetryPeriod1 = WDRetryPeriod1;
  }

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
   * Gets the WDNbRetryLevel2 value.
   *
   * @return the WDNbRetryLevel2 value
   */
  public int getWDNbRetryLevel2() {
    return WDNbRetryLevel2;
  }

  /**
   * Sets the WDNbRetryLevel2 value.
   *
   * @param WDNbRetryLevel2	the WDNbRetryLevel2 value
   */
  public void setWDNbRetryLevel2(int WDNbRetryLevel2) {
    this.WDNbRetryLevel2 = WDNbRetryLevel2;
  }

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
   * Gets the WDRetryPeriod2 value.
   *
   * @return the WDRetryPeriod2 value
   */
  public long getWDRetryPeriod2() {
    return WDRetryPeriod2;
  }

  /**
   * Sets the WDRetryPeriod2 value.
   *
   * @param WDRetryPeriod2	the WDRetryPeriod2 value
   */
  public void setWDRetryPeriod2(long WDRetryPeriod2) {
    this.WDRetryPeriod2 = WDRetryPeriod2;
  }

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

  /**
   * Gets the WDRetryPeriod3 value.
   *
   * @return the WDRetryPeriod3 value
   */
  public long getWDRetryPeriod3() {
    return WDRetryPeriod3;
  }

  /**
   * Sets the WDRetryPeriod3 value.
   *
   * @param WDRetryPeriod3	the WDRetryPeriod3 value
   */
  public void setWDRetryPeriod3(long WDRetryPeriod3) {
    this.WDRetryPeriod3 = WDRetryPeriod3;
  }

  /**
   * Gets the number of waiting messages in this engine.
   *
   *  return	the number of waiting messages.
   */
  public int getNbWaitingMessages() {
    return qout.size();
  }

  protected Logger logmon = null;

  /** Id. of local server. */
  protected short sid;
  /** Index of local server in status and matrix arrays. */
  protected int idxLS;
  /**
   * List of id. for all servers in the domain, this list is sorted and
   * is used as index for internal tables.
   */
  protected short[] servers;
  /** Filename for servers storage */
  transient protected String serversFN = null;
  /** Logical timestamp information for messages in domain, stamp[idxLS)]
   * for messages sent, and stamp[index(id] for messages received.
   */
  private int[] stamp;
  /** Buffer used to optimise transactions*/
  private byte[] stampbuf = null;
  /** */
  private int[] bootTS = null;
  /** Filename for boot time stamp storage */
  transient protected String bootTSFN = null;
 
  /** The component's name as it appears in logging. */
  protected String name;
  /** The domain name. */
  protected String domain;
  /** The communication port. */
  protected int port;
  /** The <code>MessageVector</code> associated with this network component. */
  protected MessageVector qout;

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
    if (qout != null) strbuf.append(",qout=").append(qout.size());
    if (servers != null) {
      for (int i=0; i<servers.length; i++) {
        strbuf.append(",(").append(servers[i]).append(',');
        strbuf.append(stamp[i]).append(')');
      }
    }
    strbuf.append(")");

    return strbuf.toString();
  }

  /**
   * Creates a new network component. This simple constructor is required in
   * order to use <code>Class.newInstance()</code> method during configuration.
   * The configuration of component is then done by <code>init</code> method.
   */
  public Network() {
  }

  /**
   * Insert a message in the <code>MessageQueue</code>.
   * This method is used during initialisation to restore the component
   * state from persistent storage.
   *
   * @param msg		the message
   */
  public void insert(Message msg) {
    qout.insert(msg);
  }

  /**
   * Saves information to persistent storage.
   */
  public void save() throws IOException {}

  /**
   * Restores component's information from persistent storage.
   * If it is the first load, initializes it.
   */
  public void restore() throws Exception {
    sid = AgentServer.getServerId();
    idxLS = index(sid);
    // Loads the logical clock.
    stampbuf = AgentServer.getTransaction().loadByteArray(name);
    if (stampbuf ==  null) {
      // Creates the new stamp array and the boot time stamp,
      stampbuf = new byte[4*servers.length];
      stamp = new int[servers.length];
      bootTS = new int[servers.length];
      // Then initializes them
      for (int i=0; i<servers.length; i++) {
        if (i != idxLS) {
          stamp[i] = -1;
          bootTS[i] = -1;
        } else {
          stamp[i] = 0;
          bootTS[i] = (int) (System.currentTimeMillis() /1000L);
        }
      }
      // Save the servers configuration and the logical time stamp.
      AgentServer.getTransaction().save(servers, serversFN);
      AgentServer.getTransaction().save(bootTS, bootTSFN);
      AgentServer.getTransaction().saveByteArray(stampbuf, name);
    } else {
      // Loads the domain configurations
      short[] s = (short[]) AgentServer.getTransaction().load(serversFN);
      bootTS = (int[]) AgentServer.getTransaction().load(bootTSFN);
      stamp = new int[s.length];
      for (int i=0; i<stamp.length; i++) {
        stamp[i] = ((stampbuf[(i*4)+0] & 0xFF) << 24) +
          ((stampbuf[(i*4)+1] & 0xFF) << 16) +
          ((stampbuf[(i*4)+2] & 0xFF) <<  8) +
          (stampbuf[(i*4)+3] & 0xFF);
      }
      // Joins with the new domain configuration:
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

    this.domain = name;
    this.port = port;

    // Get the logging monitor from current server MonologLoggerFactory
    // Be careful, logmon is initialized from name and not this.name !!
    logmon = Debug.getLogger(Debug.A3Network + '.' + name);
    logmon.log(BasicLevel.INFO, name + ", initialized");

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

    this.servers = servers;
    this.serversFN = name + "Servers";
    this.bootTSFN = name + "BootTS";

    restore();
  }

  /**
   * Adds the server sid in the network configuration.
   *
   * @param id	the unique server id.
   */
  synchronized void addServer(short id) throws Exception {
    // First we have to verify that id is not already in servers
    int idx = index(id);
    if (idx >= 0) return;

    idx = -idx -1;
    // Allocates new array for stamp and server
    int[] newStamp = new int[servers.length+1];
    byte[] newStampBuf = new byte[4*(servers.length+1)];
    int[] newBootTS = new int[servers.length+1];
    short[] newServers = new short[servers.length+1];
    // Copy old data from stamp and server, let a free room for the new one.
    int j = 0;
    for (int i=0; i<servers.length; i++) {
      if (i == idx) j++;
      newServers[j] = servers[i];
      newBootTS[j] = bootTS[i];
      newStamp[j] = stamp[i];
      j++;
    }
    if (idx > 0)
      System.arraycopy(stampbuf, 0, newStampBuf, 0, idx*4);
    if (idx < servers.length)
      System.arraycopy(stampbuf, idx*4,
                       newStampBuf, (idx+1)*4, (servers.length-idx)*4);

    newServers[idx] = id;
    newBootTS[idx] = -1;
    newStamp[idx] = -1;		// useless
    newStampBuf[idx] = 0;	// useless
    newStampBuf[idx+1] = 0;	// useless
    newStampBuf[idx+2] = 0; 	// useless
    newStampBuf[idx+3] = 0; 	// useless

    stamp = newStamp;
    stampbuf = newStampBuf;
    servers = newServers;
    bootTS = newBootTS;
    // be careful, set again the index of local server.
    idxLS = index(sid);

    // Save the servers configuration and the logical time stamp.
    AgentServer.getTransaction().save(servers, serversFN);
    AgentServer.getTransaction().save(bootTS, bootTSFN);
    AgentServer.getTransaction().saveByteArray(stampbuf, name);
  }

  /**
   * Removes the server sid in the network configuration.
   *
   * @param id	the unique server id.
   */
  synchronized void delServer(short id) throws Exception {
    // First we have to verify that id is already in servers
    int idx = index(id);
    if (idx < 0) return;

    int[] newStamp = new int[servers.length-1];
    byte[] newStampBuf = new byte[4*(servers.length-1)];
    int[] newBootTS = new int[servers.length-1];
    short[] newServers = new short[servers.length-1];

    int j = 0;
    for (int i=0; i<servers.length; i++) {
      if (id == servers[i]) {
        idx = i;
        continue;
      }
      newServers[j] = servers[i];
      newBootTS[j] = bootTS[i];
      newStamp[j] = stamp[i];
      j++;
    }
    if (idx > 0)
      System.arraycopy(stampbuf, 0, newStampBuf, 0, idx*4);
    if (idx < (servers.length-1))
      System.arraycopy(stampbuf, (idx+1)*4,
                       newStampBuf, idx*4, (servers.length-idx-1)*4);

    stamp = newStamp;
    stampbuf = newStampBuf;
    servers = newServers;
    bootTS = newBootTS;
    // be careful, set again the index of local server.
    idxLS = index(sid);

    // Save the servers configuration and the logical time stamp.
    AgentServer.getTransaction().save(servers, serversFN);
    AgentServer.getTransaction().save(bootTS, bootTSFN);
    AgentServer.getTransaction().saveByteArray(stampbuf, name);
  }

  /**
   * Reset all information related to server sid in the network configuration.
   *
   * @param id	the unique server id.
   */
  synchronized void resetServer(short id, int boot) throws IOException {
    // First we have to verify that id is already in servers
    int idx = index(id);
    if (idx < 0) return;

    // TODO...

    // Save the servers configuration and the logical time stamp.
    AgentServer.getTransaction().save(servers, serversFN);
    AgentServer.getTransaction().save(bootTS, bootTSFN);
    AgentServer.getTransaction().saveByteArray(stampbuf, name);
  }

  /**
   *  Adds a message in "ready to deliver" list. This method allocates a
   * new time stamp to the message ; be Careful, changing the stamp imply
   * the filename change too.
   */
  public void post(Message msg) throws Exception {
    if ((msg.not.expiration > 0) &&
        (msg.not.expiration < System.currentTimeMillis())) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ": removes expired notification " +
                   msg.from + ", " + msg.not);
      return;
    }

    short to = AgentServer.getServerDesc(msg.to.to).gateway;
    // Allocates a new timestamp. Be careful, if the message needs to be
    // routed we have to use the next destination in timestamp generation.

    msg.source = AgentServer.getServerId();
    msg.dest = to;
    msg.stamp = getSendUpdate(to);

    // Saves the message.
    msg.save();
    // Push it in "ready to deliver" queue.
    qout.push(msg);
  }

  /**
   *  Returns the index in internal table of the specified server.
   * The servers array must be ordered.
   *
   * @param id	the unique server id.
   */
  protected final int index(short id) {
    int idx = Arrays.binarySearch(servers, id);
    return idx;
  }

  protected final byte[] getStamp() {
    return stampbuf;
  }

  protected final void setStamp(byte[] stampbuf) {
    this.stampbuf = stampbuf;
    stamp = new int[servers.length];
    for (int i=0; i<stamp.length; i++) {
      stamp[i] = ((stampbuf[(i*4)+0] & 0xFF) << 24) +
        ((stampbuf[(i*4)+1] & 0xFF) << 16) +
        ((stampbuf[(i*4)+2] & 0xFF) <<  8) +
        (stampbuf[(i*4)+3] & 0xFF);
    }    
  }

  private void updateStamp(int idx, int update) throws IOException {
    stamp[idx] = update;
    stampbuf[(idx*4)+0] = (byte)((update >>> 24) & 0xFF);
    stampbuf[(idx*4)+1] = (byte)((update >>> 16) & 0xFF);
    stampbuf[(idx*4)+2] = (byte)((update >>>  8) & 0xFF);
    stampbuf[(idx*4)+3] = (byte)(update & 0xFF);
    AgentServer.getTransaction().saveByteArray(stampbuf, name);
  }

  /** The message can be delivered. */
  static final int DELIVER = 0;
//   /**
//    *  There is other message in the causal ordering before this one.
//    * This cannot happened with a FIFO ordering.
//    */
//   static final int WAIT_TO_DELIVER = 1;
  /** The message has already been delivered. */
  static final int ALREADY_DELIVERED = 2;

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
  private synchronized int testRecvUpdate(short source, int update) throws IOException {
    int fromIdx = index(source);

    if (update > stamp[fromIdx]) {
      updateStamp(fromIdx, update);
      return DELIVER;
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
  private synchronized int getSendUpdate(short to) throws IOException {
    int update =  stamp[idxLS] +1;
    updateStamp(idxLS, update);
    return update;
  }

  final int getBootTS() {
    return bootTS[idxLS];
  }

  final void testBootTS(short source, int boot) throws IOException {
    int fromIdx = index(source);

    if (boot != bootTS[fromIdx]) {
      if (logmon.isLoggable(BasicLevel.WARN))
        logmon.log(BasicLevel.WARN,
                   getName() + ", reset stamp #" + source + ", "
                   + bootTS[fromIdx] + " -> " + boot);

      bootTS[fromIdx] = boot;
      AgentServer.getTransaction().save(bootTS, bootTSFN);
      updateStamp(fromIdx, -1);
    }
  }

//   int last = -1;

  /**
   * Try to deliver the received message to the right consumer.
   *
   * @param msg		the message.
   */
  protected void deliver(Message msg) throws Exception {
    // Get real from serverId.
    short source = msg.getSource();

    // Test if the message is really for this node (final destination or
    // router).
    short dest = msg.getDest();
    if (dest != AgentServer.getServerId()) {
      logmon.log(BasicLevel.ERROR,
                 getName() + ", recv bad msg#" + msg.getStamp() +
                 " really to " + dest +
                 " by " + source);
      throw new Exception("recv bad msg#" + msg.getStamp() +
                          " really to " + dest +
                          " by " + source);
    }

//     if ((last != -1) && (msg.getStamp() != (last +1)))
//       logmon.log(BasicLevel.FATAL,
//                  getName() + ", recv msg#" + msg.getStamp() + " should be #" + (last +1));
//     last = msg.getStamp();

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 getName() + ", recv msg#" + msg.getStamp() +
                 " from " + msg.from +
                 " to " + msg.to +
                 " by " + source);

    ServerDesc desc = AgentServer.getServerDesc(source);
    if (! desc.active) {
      desc.active = true;
      desc.retry = 0;
    }

    // Start a transaction in order to ensure atomicity of clock updates
    // and queue changes.
    AgentServer.getTransaction().begin();

    // Test if the message can be delivered then deliver it
    // else put it in the waiting list
    int todo = testRecvUpdate(source, msg.getStamp());

    if (todo == DELIVER) {
      // Deliver the message then try to deliver alls waiting message.
      // Allocate a local time to the message to order it in
      // local queue, and save it.
      Channel.post(msg);

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", deliver msg#" + msg.getStamp());

      Channel.save();
      AgentServer.getTransaction().commit(false);
      // then commit and validate the message.
      Channel.validate();
      AgentServer.getTransaction().release();
    } else {
//    it's an already delivered message, we have just to re-send an
//    aknowledge (see below).
      AgentServer.getTransaction().commit(true);
    }
  }

  /**
   * Deletes the component, removes all persistent datas. The component
   * may have been previously stopped, and removed from MessageConsumer
   * list.
   * This operation use Transaction calls, you may use commit to validate it.
   *
   * @see fr.dyade.aaa.util.Transaction 
   */
  public void delete() throws IllegalStateException {
    if (isRunning()) throw new IllegalStateException();

    AgentServer.getTransaction().delete(serversFN);
    AgentServer.getTransaction().delete(bootTSFN);
    AgentServer.getTransaction().delete(name);
  }

  /**
   * Validates all messages pushed in queue during transaction session.
   */
  public void validate() {
    qout.validate();
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
