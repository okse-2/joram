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
import java.net.*;


/**
 * A <code>TransientManager</code> agent is responsible for handling
 * communications with all transient agent servers associated with the local
 * persistent agent server. It uses separate <code>TransientMonitor</code>
 * objects to manage each server.
 * <p>
 * A <code>TransientManager</code> agent is created when the configuration file
 * read by the local agent server indicates a dedicated port number for it. The
 * agent creates a server socket listening on that port so that depending
 * transient agent server may connect to it. The transient server first sends a
 * <code>TransientConfig</code> notification onto the created connection,
 * identifying itself for this agent.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		TransientMonitor
 * @see		TransientConfig
 */
public class TransientManager extends Agent {

public static final String RCS_VERSION="@(#)$Id: TransientManager.java,v 1.3 2000-10-05 15:15:24 tachkeni Exp $"; 


  static final int CONNECTOR_ID = -1;
  static final int INPUT_ID = 0;
  static final int OUTPUT_ID = 1;


  /** driver waiting for transient agent servers connections */
  transient TransientConnector connector;

  /** list of transient agent servers managed by this agent */
  TransientMonitor[] monitors;


  /**
   * Constructor.
   */
  public TransientManager() {
    // agent is "pined" in memory.
    super("TransientManager#" + Server.serverId,
	  true,
	  AgentId.transientProxyId);
    connector = null;
    monitors = null;
  }


  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    output.append(super.toString());
    output.append(",monitors=");
    if (monitors == null) {
      output.append(monitors);
    } else {
      output.append("(");
      output.append(monitors.length);
      for (int i = 0; i < monitors.length; i ++) {
	output.append(",");
	output.append(monitors[i]);
      }
      output.append(")");
    }
    output.append(")");
    return output.toString();
  }

  /**
   * Finds the <code>TransientMonitor</code> object from a server id.
   */
  TransientMonitor getMonitor(short serverId) {
    for (int i = monitors.length; i-- > 0;) {
      if (monitors[i].serverId == serverId)
	return monitors[i];
    }

    return null;
  }

  /**
   * Finds the <code>TransientMonitor</code> object from a driver id.
   * <p>
   * Monitor drivers use the first ids from 0, input drivers using even numbers
   * and output drivers using odd ones.
   */
  TransientMonitor getMonitor(int driverId) {
    if (driverId < 0 ||
	driverId >= (monitors.length * 2))
      return null;

    return monitors[driverId / 2];
  }

  /**
   * Finds the driver id from the monitor and the driver direction.
   *
   * @exception Exception
   *	unspecialized exception
   */
  int getDriverId(TransientMonitor monitor, int direction) throws Exception {
    int idx = monitors.length;
    while (idx-- > 0) {
      if (monitors[idx] == monitor)
	break;
    }
    if (idx < 0)
      throw new IllegalStateException("bad monitor");

    return (idx * 2) + direction;
  }

  /**
   * Initializes the transient members of this agent.
   * This function is first called by the factory agent,
   * then by the system each time the agent server is restarted.
   *
   * This function is not declared final so that derived classes
   * may change their reload policy.
   *
   * @param firstTime		true when first called by the factory
   *
   * @exception Exception
   *	unspecialized exception
   */
  protected void initialize(boolean firstTime) throws Exception {
    super.initialize(firstTime);

    // gets the listening port number for transient agent server connection
    // this is a persistent server
    ServerDesc desc = Server.getServerDesc(Server.getServerId());
    int transientPort = desc.transientPort;
    if (transientPort == -1)
      throw new IllegalStateException("no transient server for this monitor");

    if (firstTime) {
      // finds the number of transient servers depending on this agent
      int num = 0;
      for (int i = Server.transientServers.length; i-- > 0;)
	if (Server.transientServers[i].proxyId.equals(id))
	  num ++;
      if (num == 0)
	throw new IllegalStateException("no transient server for this monitor");

      // initializes monitors
      monitors = new TransientMonitor[num];
      try {
	for (int i = Server.transientServers.length; i-- > 0;) {
	  if (Server.transientServers[i].proxyId.equals(id)) {
	    num --;
	    monitors[num] = new TransientMonitor(Server.transientServers[i].sid);
	  }
	}
      } catch (Exception exc) {
	// when the catch instruction is here, no exception is raised
	// when it misses, an ArrayIndexOutOfBoundsException exception
	// may be raised with value 805457943;
	// the bug occurs when executing on acores a program compiled on dyade.
	throw exc;
      }
    } else {
      // recovers after a failure ?
    }
    
    // starts the driver waiting for connections
    connector = new TransientConnector(CONNECTOR_ID, this, transientPort);
    connector.start();
  }

  /**
   * Reacts to notifications.
   * <p>
   * This agent mainly receives <code>TransientMessage</code> notifications.
   * The notification may have been forwarded by the <code>Channel</code> when
   * a persistent agent communicates with a transient one, or it may come from
   * a transient agent communicating outside of its server. Anyway this agent
   * analyzes the actual target of the notification and forwards it to the
   * relevant <code>TransientMonitor</code> object when the target is a known
   * transient agent, or to the <code>Channel</code> object otherwise.
   * <p>
   * Notifications coming from transient agent servers should not reach this
   * agent as they are handled by the <code>TransDriverIn</code> driver of the
   * associated <code>TransientMonitor</code> object.
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
      // no exception is expected
      if (Debug.error)
	Debug.trace("TransientManager: Exception in " +
		    this + ".react(" + from + "," + not + ")",
		    exc);
      // stops everything
      stop();
    }
  }

  /**
   * Forwards <code>TransientMessage</code> notifications to the relevant
   * <code>TransientMonitor</code> object if known, or to the
   * <code>Channel</code> object otherwise.
   *
   * @param msg		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  void forwardMessage(TransientMessage msg) throws Exception {
    TransientMonitor monitor = getMonitor(msg.to.to);
    if (monitor != null) {
      // target agent is a transient one
      monitor.sendTo(msg);
    } else {
      // forwards to channel, setting actual source agent
      Channel.channel.sendTo(msg.from, msg.to, msg.not);
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
    // allows the input driver of the right monitor to read more data
    getMonitor(not.driverId).driverIn.recvFlowControl(not);
  }

  /**
   * Reacts to the end of a driver execution.
   * <p>
   * Makes the relevant <code>Monitor</code> object react to the notification.
   *
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  void driverDone(DriverDone not) throws Exception {
    getMonitor(not.getDriver()).driverDone(not.getDriver() % 2);
  }

  /**
   * Stops everything.
   *
   * @exception Exception
   *	unspecialized exception
   */
  void stop() throws Exception {
    for (int i = monitors.length; i-- > 0;) {
      if (monitors[i] != null) {
	monitors[i].stop();
      }
    }
  }
}


/**
 * Derived <code>Driver</code> class used by <code>TransientManager</code> for
 * waiting for transient agent servers connections.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		TransientManager
 */
class TransientConnector extends Driver {

  /** associated manager */
  protected TransientManager manager = null;

  /** listening port number */
  int port;

  /**
   * Constructor.
   *
   * @param id		identifier local to the driver creator
   * @param manager	associated manager
   * @param port	listening port number
   */
  TransientConnector(int id, TransientManager manager, int port) {
    super(id);
    this.manager = manager;
    this.port = port;
  }

  public void run() throws Exception {
    // creates a server socket listening on configured port
    ServerSocket listen = new ServerSocket(port);

    main_loop:
    while (true) {
      // waits for a transient server to connect
      Socket sock = listen.accept();

      try {
	if (Debug.drivers)
	  Debug.trace("TransientConnector accept server", false);

	// sets the input and output flows
	ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
	ObjectInputStream ois =
	  new ObjectInputStream(new BufferedInputStream(sock.getInputStream()));

	// gets the server configuration
	TransientConfig config = (TransientConfig) ois.readObject();

	// finds the server monitor from its id
	TransientMonitor monitor = manager.getMonitor(config.serverId);
	if (monitor == null) {
	  Debug.trace("unknown server: " + config, false);
	  sock.close();
	  continue main_loop;
	}

	// sets the input and output streams with the server
	monitor.ois = ois;
	monitor.oos = oos;
	monitor.oos.flush();

	// starts the monitor drivers
	// could warn the manager if its persistent state needs to be modified
	// sendTo(manager.getId(), config);
	monitor.createDrivers(manager.getDriverId(monitor, TransientManager.INPUT_ID),
			      manager.getDriverId(monitor, TransientManager.OUTPUT_ID),
			      manager.getId());

	if (Debug.drivers)
	  Debug.trace("TransientConnector handle server " + config, false);
      } catch (Exception exc) {
	Debug.trace("error in connecting server", exc);
	sock.close();
      }
    }
  }
}
