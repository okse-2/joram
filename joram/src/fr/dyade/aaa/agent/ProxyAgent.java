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
import fr.dyade.aaa.util.*;

public abstract class ProxyAgent extends Agent {

public static final String RCS_VERSION="@(#)$Id: ProxyAgent.java,v 1.4 2000-10-05 15:15:22 tachkeni Exp $"; 


  public static final int DRIVER_IN = 1;
  public static final int DRIVER_OUT = 2;

  /** true if connect may block */
  protected boolean blockingCnx = true;
  /** true if proxy may handle multiple connection */
  protected boolean multipleCnx = false;
  /** flow control in driver in */
  protected int inFlowControl = 20;
  /** communication with drvOut */
  protected transient Queue qout;

  /** manage input stream */
  private transient DriverIn drvIn;
  /** manage output stream */
  private transient DriverOut drvOut;
  /** manage connection step, optional */
  private transient DriverConnect drvCnx;

  public ProxyAgent() {
    this(null);
  }

  public ProxyAgent(String n) {
    this(Server.serverId, n);
  }

  public ProxyAgent(short to, String n) {
    // ProxyAgent is "pined" in memory.
    super(to, n, true);
    drvIn = null;
    drvOut = null;
    drvCnx = null;
  }

  /*
   * Constructor used to build Well Known Services agents.
   *
   * @param name	symbolic name
   * @param stamp	well known stamp
   */
  public ProxyAgent(String name, int stamp) {
    super(name, true, stamp);
    drvIn = null;
    drvOut = null;
    drvCnx = null;
  }


  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",blockingCnx=" + blockingCnx +
      ",multipleCnx=" + multipleCnx +
      ",inFlowControl=" + inFlowControl +
      ",qout=" + qout + ")";
  }

  /**
   * Initializes the transient members of this agent.
   * This function is first called by the factory agent,
   * then by the system each time the agent server is restarted.
   * <p>
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
    qout = new Queue();
    reinitialize();
  }

  /**
   * Reinitializes the agent, that is reconnects its input and output.
   * This function may be called only when all drivers are null.
   */
  protected void reinitialize() throws IOException {
    if (drvIn != null || drvOut != null)
      throw new IllegalStateException();
    drvCnx = new DriverConnect(this, blockingCnx, multipleCnx);
    drvCnx.start();
  }

  /** input stream, created by subclass during connect */
  protected transient NotificationInputStream ois = null;
  /** output stream, created by subclass during connect */
  protected transient NotificationOutputStream oos = null;

  /**
   * Initializes the connection with the outside, up to creating
   * the input and output streams <code>ois</code> and <code>oos</code>.
   *
   * @exception Exception
   *	unspecialized exception
   */
  public abstract void connect() throws Exception;

  /**
   * Closes the connection with the outside.
   *
   * @exception Exception
   *	unspecialized exception
   */
  public abstract void disconnect() throws Exception;

  /**
   * Connects the streams provided by the user to this proxy agent
   * via two created drivers. The streams must be created by the
   * <code>connect</code> function defined in derived classes.
   * <p>
   * If the connection step may block, then this function is executed
   * in a separate thread controled by <code>drvCnx</code> (see
   * <code>Initialize</code>).
   */
  void createDrivers() {
    try {
      connect();
    } catch (Exception exc) {
      Debug.trace(toString() + ".createDrivers()", exc);
    }

    if (ois != null) {
      drvIn = new DriverIn(DRIVER_IN, getId(), ois, inFlowControl);
      drvIn.start();
    }
    if (oos != null) {
      drvOut = new DriverOut(DRIVER_OUT, getId(), qout, oos);
      drvOut.start();
    }
  }

  /**
   * Stops all drivers.
   * May be multiply called.
   */
  protected void stop() {
    if (drvCnx != null) {
      drvCnx.stop();
      drvCnx = null;
    }
    if (drvIn != null) {
      drvIn.stop();
      drvIn = null;
    }
    if (drvOut != null) {
      drvOut.stop();
      drvOut = null;
    }
  }

    public void cleanDriverOut() {
	drvOut.clean();
    }

  /**
   * Finalizes this proxy agent execution. Calls <code>disconnect</code>
   * to close the open streams, and <code>stop</code> to stop the drivers.
   *
   * @exception Throwable
   *	unspecialized exception
   */
  protected final void finalize() throws Throwable {
    if (ois == null && oos == null)
      return;

    try {
      disconnect();
    } catch (Exception exc) {
      Debug.trace(toString() + ".finalize(): ", exc);
    }

    stop();

    qout = null;
    
    ois = null;
    oos = null;
  }

  /**
   * Reacts to notifications.
   * Assumes notifications from nullId come from drvIn; let derive classes
   * handle them. Forwards notifications coming from an identified agent
   * onto the outgoing connection.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    try {
      if (not instanceof DriverDone) {
	driverDone((DriverDone) not);
      } else if (not instanceof FlowControlNot) {
	// Received a FlowControlNot then allows "in driver" to read more data.
	drvIn.recvFlowControl((FlowControlNot) not);
      } else if (! from.isNullId()) {
	qout.push(not);
      } else {
	super.react(from, not);
      }
    } catch (Exception exc) {
      if ((Debug.drivers) || (Debug.error))
	Debug.trace("ProxyAgent: Exception in " +
		    this + ".react(" + from + "," + not + ")",
		    exc);
      stop();
      // the proxy agent may eventually restart
    }
  }

  /**
   * Reacts to end of driver execution.
   * <p>
   * This is the end of the driver thread, however the thread resources
   * may not have been released. This is why <code>close</code> is called
   * on the notification streams, which requires from the stream classes
   * to cope with a call to <code>close</code> when some resources may have
   * been released.
   */
  protected void driverDone(DriverDone not) throws IOException {
    switch (not.getDriver()) {
    case DRIVER_IN:
      try {
	ois.close();
      } catch (Exception e) {}
      ois = null;
      drvIn = null;
      break;
    case DRIVER_OUT:
	try {
	    oos.close();
	} catch (Exception e) {}
	oos = null;
	drvOut = null;
	break;
    }
  }
}

class DriverConnect extends Driver {

  protected ProxyAgent proxy = null;
  protected boolean blockingCnx;
  protected boolean multipleCnx;

  DriverConnect(ProxyAgent proxy,
		boolean blockingCnx,
		boolean multipleCnx) {
    this.proxy = proxy;
    this.blockingCnx = blockingCnx;
    this.multipleCnx = multipleCnx;
  }

  public void start() {
    if (! blockingCnx) {
      run();
    } else {
      super.start();
    }
  }

  public void run() {
    if (Debug.drivers)
      Debug.trace("cnx driver start", false);
    do {
      proxy.createDrivers();
    } while (multipleCnx);
  }
}
