/*
 * Copyright (C) 2004 - 2005 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
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

import java.util.*;
import java.io.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import org.javagroups.MembershipListener;
import org.javagroups.MessageListener;
import org.javagroups.Message;
import org.javagroups.Channel;
import org.javagroups.JChannel;
import org.javagroups.Address;
import org.javagroups.View;
import org.javagroups.blocks.*;
import org.javagroups.util.Util;
import org.javagroups.log.Trace;
import org.javagroups.ChannelException;
import org.javagroups.ChannelClosedException;
import org.javagroups.ChannelNotConnectedException;

/**
 *  Implementation of JGroups in order to improve HA.
 */
final class JGroups 
  implements MembershipListener, MessageListener {
  
  static Logger logmon = null;

  private int nbClusterExpected = 2;
  boolean coordinator = false;
  private Channel channel;
  private Address myAddr = null;
  private Address coordinatorAddr = null;
  private String channelName = null;
  HAEngine engine = null;
  SimpleNetwork network = null; // AF: to replace with HANetwork
  Object lock;

  JGroups() throws Exception {
    // Get the logging monitor from current server MonologLoggerFactory
    logmon = Debug.getLogger(Debug.JGroups);
    logmon.log(BasicLevel.DEBUG, "JGroups created.");

    nbClusterExpected = AgentServer.getInteger("nbClusterExpected", nbClusterExpected).intValue();
  }

  void init(String name) throws Exception {
    channelName = "HAJGroups." + name;

    lock = new Object();

    state = STARTING;

    String addr = System.getProperty("JGroups.MCastAddr", "224.0.0.35");
    String port = System.getProperty("JGroups.MCastPort", "25566");
      
    String props = System.getProperty(
      "JGroupsProps",
      "UDP(mcast_addr=" + addr + 
      ";mcast_port=" + port + ";ip_ttl=32;" +
      "mcast_send_buf_size=150000;mcast_recv_buf_size=80000):" +
      "PING(timeout=2000;num_initial_members=3):" +
      "MERGE2(min_interval=5000;max_interval=10000):" +
      "FD_SOCK:" +
      "VERIFY_SUSPECT(timeout=1500):" +
      "pbcast.NAKACK(gc_lag=50;retransmit_timeout=300,600,1200,2400,4800):" +
      "UNICAST(timeout=5000):" +
      "pbcast.STABLE(desired_avg_gossip=20000):" +
      "FRAG(frag_size=4096;down_thread=false;up_thread=false):" +
      "pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;" +
      "shun=false;print_local_addr=true)");

    channel = new JChannel(props);
    channel.connect(channelName);
    
    new PullPushAdapter(channel, 
                        (MessageListener) this, 
                        (MembershipListener) this);
    myAddr = channel.getLocalAddress();
  }

  void disconnect() {
    channel.disconnect();
  }
  
  void connect() throws ChannelException, ChannelClosedException {
    if (!channel.isConnected()) channel.connect(channelName);
  }

  void startConsAndServ() {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,"start service and comsumer");

    // Use another thread to start services and network in order
    // to avoid dead-lock during send.
    Thread t = new Thread() {
      public void run() {
        try {
          ServiceManager.start();
        } catch (Exception exc) {
          logmon.log(BasicLevel.WARN, "services start failed.", exc);
        }
        try {
          AgentServer.startConsumers();
        } catch (Throwable exc) {
          logmon.log(BasicLevel.WARN, "consumer start failed.", exc);
        }
      }
    };
    t.setDaemon(true);
    t.start();
  }

  void send(Serializable message) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,"JGroups send(" + message + ")");

    byte[] buf = null;
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(message);
      buf = bos.toByteArray();
      oos.flush();
    } catch(Exception e) {
      logmon.log(BasicLevel.ERROR,"JGroups send message",e);
      throw e;
    }
    if (buf == null) return;
    Message msg = new Message(null, null, buf);
    synchronized (lock) {
      channel.send(msg);
      lock.wait();
    }
  }
  
  void sendTo(Address dst, Serializable obj) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,"JGroups sendTo(" + dst + "," + obj + ")");
    channel.send(dst,myAddr,obj);
  }

  Address getCoordinatorAddr() {
    return coordinatorAddr;
  }

  void setEngine(HAEngine engine) {
    logmon.log(BasicLevel.DEBUG, "setEngine");
    this.engine = engine;
  }

  void setNetWork(SimpleNetwork network) {
    this.network = network;
  }

  boolean isCoordinator() {
    return coordinator;
  }

  int state = NONE;
  final static int NONE = -11;
  final static int STARTING = 1;
  final static int INITIALIZING = 2;
  final static int RUNNING = 3;

  /* ----- MessageListener interface ----- */
  public void receive(Message msg) {
    try {
      Object obj = Util.objectFromByteBuffer(msg.getBuffer());
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG," receive obj = " + obj + 
                   "\nmsg.getSrc =" + msg.getSrc() + 
                   "\nmsg.getDest =" + msg.getDest() + 
                   "\nmyAddr = " + myAddr);
      
      if (myAddr.equals(msg.getSrc())) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,"jgroups, I am the sender.");
        if ((obj instanceof fr.dyade.aaa.agent.Message) ||
            (obj instanceof JGroupsAckMsg) ||
            (obj instanceof HAStateReply)) {
          synchronized (lock) {
            lock.notify();
          }
        }
        return;
      }

      if (obj instanceof HAStateRequest && coordinator) {
        HAStateRequest req = (HAStateRequest) obj;
        engine.requestor.add(req.getAddress());
      } else if (obj instanceof HAStateReply) {
        if (state != INITIALIZING) return;

        HAStateReply reply = (HAStateReply) obj;
        //  Services are already first initialized on master server, we
        // have just to start them in startConsAndServ.
        ServiceDesc services[] = ServiceManager.getServices();
        if (services != null) {
          for (int i = 0; i < services.length; i++)
            services[i].initialized = true;
        }
        // Synchronizes network's logical clock
        if (network != null)
          network.setStamp(reply.getNetworkStamp());
        // Sets engine's state (
        engine.setState(reply);
        state = RUNNING;
      } else if (obj instanceof fr.dyade.aaa.agent.Message) {
        if (state != RUNNING) return;

        fr.dyade.aaa.agent.Message m = (fr.dyade.aaa.agent.Message) obj;
        if ((network != null) &&
            (m.from.getTo() != AgentServer.getServerId())) {
          network.deliver(m);
        } else {
          engine.receiveFromJGroups(m);
        }
      } else if (obj instanceof JGroupsAckMsg && network != null) {
        if (state != RUNNING) return;

        network.ackMsg((JGroupsAckMsg) obj);
      }
    } catch(Exception exc) {
      logmon.log(BasicLevel.ERROR,
                 "JGroups part receive msg = " + msg, exc);
    }
  }

  public byte[] getState() {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,"=== MessageListener getState");
    return null;
  }
  
  public void setState(byte[] state) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,"=== MessageListener setState");
  }
  /* ----------------- End of Interface MessageListener --------------- */

  /* ------------ Interface MembershipListener ------------- */

  public void viewAccepted(View view) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,"==== viewAccepted: " + view);

    // Eventually get new coordinator address
    Vector mbrs = view.getMembers();
    coordinatorAddr = (Address) mbrs.elementAt(0);

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "JGroups setView: " + coordinator + ", " + state); 

    if (coordinator) {
      // Test that the server is always master.
      if (! coordinatorAddr.equals(myAddr)) {
        logmon.log(BasicLevel.FATAL, "Bad view for coordinator");
        throw new RuntimeException("Bad view for coordinator");
      }
      return;
    }

    if ((state != RUNNING) && (! coordinatorAddr.equals(myAddr))) {
      // Ask current state to the new coordinator.    
      try {      
        sendTo(coordinatorAddr,new HAStateRequest(myAddr));
        state = INITIALIZING;
      } catch (Exception exc) {
        logmon.log(BasicLevel.ERROR,"JGroups sendTo()",exc);
      }
    }

    if ((mbrs.size() >= nbClusterExpected) &&
        coordinatorAddr.equals(myAddr)) {
      // This server is the new master !
      coordinator = true;
      // Starts the service
      startConsAndServ();
      // If not already done set state to RUNNING (this can be
      // happen for the 1st master).
      state = RUNNING;
    }

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "JGroups setView: " + coordinator + ", " + state); 
  }
  
  public void suspect(Address suspected_mbr) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,"==== suspect(): " + suspected_mbr);
  }
  
  public void block() {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,"==== block()");
  }
  /* -------------------- End of Interface MembershipListener ----------------- */
}