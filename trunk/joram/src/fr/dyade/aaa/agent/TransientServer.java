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

import fr.dyade.aaa.util.Queue;
import java.io.*;
import java.net.*;


/**
 * A <code>TransientServer</code> agent resides in a transient agent server and
 * ensures the notification delivery from and to the other agent servers.
 * <p>
 * A transient agent server is subordinated to a single persistent agent server.
 * The persistent agent server, declared in the configuration file, hosts a
 * <code>TransientManager</code> agent which the <code>TransientServer</code>
 * agent connects to when it initializes. All incoming and outgoing
 * notifications to and from the transient agent server are conveyed as
 * <code>TransientMessages</code> onto that connection.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		TransientManager
 * @see		TransientMessages
 */
public class TransientServer extends Agent {

public static final String RCS_VERSION="@(#)$Id: TransientServer.java,v 1.1.1.1 2000-05-30 11:45:24 tachkeni Exp $"; 


  static final int DRIVER_IN = 0;
  static final int DRIVER_OUT = 1;

  /** input driver */
  transient TransDriverIn driverIn;
  
  /** output driver */
  transient TransDriverOut driverOut;

  /** communication queue with output driver */
  protected transient Queue qout;


  /**
   * Constructor.
   */
  TransientServer() {
    // agent is "pined" in memory.
    super("TransientServer#" + Server.serverId,
	  true,
	  AgentId.transientProxyId);
    driverIn = null;
    driverOut = null;
    qout = null;
  }


  /**
   * Initializes the transient members of this agent.
   * This function is first called by the factory agent,
   * then by the system each time the agent server is restarted.
   * <p>
   * This function connects to the <code>TransientManager</code> agent.
   * The persistent agent server is assumed to be running at that time.
   *
   * @param firstTime		true when first called by the factory
   *
   * @exception Exception
   *	unspecialized exception
   */
  protected void initialize(boolean firstTime) throws Exception {
    super.initialize(firstTime);
    if (firstTime) {
      // gets the configuration
    } else {
      // recovers after a failure ?
    }
    
    // connects to <code>TransientManager</code> agent
    ServerDesc desc = Server.getServerDesc(Server.getServerId());
    desc = Server.getServerDesc(desc.proxyId.to);
    Socket sock = new Socket(desc.getAddr(), desc.transientPort);
    // sets the input and output flows
    ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
    ObjectInputStream ois =
      new ObjectInputStream(new BufferedInputStream(sock.getInputStream()));
    // sends local configuration
    oos.writeObject(new TransientConfig(Server.getServerId()));

    // creates and starts drivers
    qout = new Queue();
    driverIn = new TransDriverIn(DRIVER_IN, getId(), ois, 100);
    driverOut = new TransDriverOut(DRIVER_OUT, getId(), qout, oos);
    driverIn.start();
    driverOut.start();
  }

  /**
   * Reacts to notifications.
   * <p>
   * This agent mainly receives <code>TransientMessage</code> notifications.
   * The notification may have been forwarded by the <code>Channel</code> when
   * a transient agent of this server communicates with an external agent, or
   * it may come from a persistent agent communicating with a local transient
   * agent. The second case should actually not occur as the notification is
   * completely handled by the <code>TransDriverIn</code> driver.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    try {
      if (not instanceof TransientMessage) {
	forwardMessage((TransientMessage) not);
      } else if (not instanceof FlowControlNot) {
	flowControl((FlowControlNot) not);
      } else if (not instanceof DriverDone) {
	driverDone((DriverDone) not);
      } else {
	super.react(from, not);
      }
    } catch (Exception exc) {
      if (Debug.error)
	Debug.trace("TransientServer: Exception in " +
		    this + ".react(" + from + "," + not + ")",
		    exc);
      stop();
    }
  }

  /**
   * Forwards <code>TransientMessage</code> notifications to the relevant
   * agent, if it is local, or to the <code>Channel</code> object otherwise.
   *
   * @param msg		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  void forwardMessage(TransientMessage msg) throws Exception {
    if (msg.to.to == Server.getServerId()) {
      // forwards to channel, setting actual source agent
      Channel.channel.sendTo(msg.from, msg.to, msg.not);
    } else {
      // forwards to the manager
      driverOut.sendTo(msg);
    }
  }

  /**
   * Controls the notification flow from input drivers.
   *
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  void flowControl(FlowControlNot not) throws Exception {
    if (not.driverId != DRIVER_IN)
      throw new IllegalStateException("unknown driver for " + not);

    // allows the input driver to read more data
    driverIn.recvFlowControl(not);
  }

  /**
   * Reacts to the end of a driver execution.
   * <p>
   * A transient server disconnecting from its manager needs to be stopped and
   * restarted as there is no persistency of the notifications exchanged, and
   * outgoing notifications may be lost.
   *
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  void driverDone(DriverDone not) throws Exception {
    stop();
  }

  /**
   * Stops the transient agent server.
   *
   * @exception Exception
   *	unspecialized exception
   */
  void stop() throws Exception {
    Server.stop();
  }
}
