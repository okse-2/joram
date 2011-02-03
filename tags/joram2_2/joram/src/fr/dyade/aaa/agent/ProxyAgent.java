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
import java.util.*;

import org.objectweb.monolog.api.BasicLevel;

import fr.dyade.aaa.util.*;

public abstract class ProxyAgent extends Agent {
  /** RCS version number of this file: $Revision: 1.9 $ */
  public static final String RCS_VERSION="@(#)$Id: ProxyAgent.java,v 1.9 2002-01-16 12:46:47 joram Exp $"; 

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
  private transient DriverIn drvIn = null;
  /** manage output stream */
  private transient DriverOut drvOut = null;
  /** manage connection step, optional */
  private transient DriverConnect drvCnx;

  /** <code>true</code> if the proxy manages multiple connections. */
  protected boolean multiConn = false;
  /** 
   * Table holding the <code>DriverMonitor</code> objects, each one holding a
   * connection set (a pair of drivers, a qout, ois, oos, ...).
   * For multi-connections management.
   *
   * @see  DriverMonitor
   */
  protected transient Hashtable driversTable;
  /**
   * Used in multi-connections context for identifying each
   * connection.
   */
  private int driversKey ;

  /**
   * Returns default log topic for proxies. Its method  overriddes
   * the default one in Agent, the resulting logging topic is
   * <code>Debug.A3Proxy</code> dot the real classname.
   */
  protected String getLogTopic() {
    String classname = getClass().getName();
    return Debug.A3Proxy + '.' +
      classname.substring(classname.lastIndexOf('.') +1);
  }

  public ProxyAgent() {
    this(null);
  }

  public ProxyAgent(String n) {
    this(AgentServer.getServerId(), n);
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
      ",multiConn=" + multiConn +
      ",qout=" + qout +
      ",driversKey=" + driversKey + ")";
  }
  
  /**
   * Method setting the <code>ProxyAgent</code> in multiConn mode.
   * To be called immediately after the <code>ProxyAgent</code> instanciation.
   */
  public void setMultiConn() {
    multiConn = true;
  }

  public int getProxyDriversKey() {
    return driversKey;
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

    // In single connection mode, creating qout once:
    if (!multiConn)
      qout = new Queue();
    // In multi connections mode, creating the driversTable and initializing
    // the driversKey:
    else {
      driversKey = 1;
      driversTable = new Hashtable();
    }
    reinitialize();
  }

  /**
   * Reinitializes the agent, that is reconnects its input and output.
   * This function may be called only when all drivers are null
   * if the <code>ProxyAgent</code> manages only one connection at a time.
   * Otherwise, a multiConn <code>ProxyAgent</code> will reinitialize
   * even if the current drivers are not null.
   */
  protected void reinitialize() throws IOException {
    if (drvIn != null || drvOut != null) {
      if (!multiConn) throw new IllegalStateException();
    }

    drvCnx = new DriverConnect(this, blockingCnx, multipleCnx);
    drvCnx.start();

    // If the ProxyAgent manages multi-connections, storing the connection set
    // in a DriverMonitor and putting it in the driversTable.
    if (multiConn) {
      DriverMonitor dMonitor = new DriverMonitor(drvIn, drvOut, qout, ois, oos,
                                                 drvCnx);

      driversTable.put(new Integer(driversKey), dMonitor);
      driversKey++;
    }

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
   * to two created drivers. The streams must be created by the
   * <code>connect</code> function defined in derived classes.
   * <p>
   * If the connection step blocks, this function is executed
   * in a separate thread controled by <code>drvCnx</code> (see
   * <code>Initialize</code>).
   */
  void createDrivers() {
    try {
      connect();
    } catch (EOFException exc) {
      logmon.log(BasicLevel.WARN, "connection closed in createDrivers()", exc);
    } catch (Exception exc) { 
      logmon.log(BasicLevel.ERROR, "error in createDrivers()", exc);
    }
    if (!multiConn) {  
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "connected");
      if (ois != null) {
        drvIn = new DriverIn(DRIVER_IN, this, ois, inFlowControl);
        drvIn.start();
      }
      if (oos != null) {
        drvOut = new DriverOut(DRIVER_OUT, this, qout, oos);
        drvOut.start();
      }
    }
    // If the ProxyAgent is multiConn, creating drvIn and drvOut with
    // the additionnal driversKey parameter and also creating a new qout.
    else {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "connected - driversKey=" + driversKey);
      if (ois != null) {
        drvIn = new DriverIn(DRIVER_IN, this, ois, inFlowControl, driversKey);
        drvIn.start();
      }
      if (oos != null) {
        qout = new Queue(); 
        drvOut = new DriverOut(DRIVER_OUT, this, qout, oos, driversKey);
        drvOut.start();
      }
    }
  }


  /**
   * Stops all drivers (non multiConn mode).
   * May be multiply called.
   */
  protected void stop() {
    if (multiConn) return;

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
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "stopped");
  }

  /**
   * Method stopping the specified connection set (multi-connections mode). 
   *
   * @param drvKey  key identifying the connection set to stop.
   */
  protected void stop(int drvKey) {
    DriverMonitor dMonitor =
      (DriverMonitor) driversTable.get(new Integer(drvKey));

    if (dMonitor != null) {
      if (dMonitor.drvCnx != null) {
        (dMonitor.drvCnx).stop();
        dMonitor.drvCnx = null;
      }
      if (dMonitor.drvIn != null) {
        (dMonitor.drvIn).stop();
        dMonitor.drvIn = null;
      }
      if (dMonitor.drvOut != null) {
        (dMonitor.drvOut).stop();
        dMonitor.drvOut = null;
      }
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "stopped - driversKey=" + driversKey);
    }
  }


  /** Method cleaning DriverOut. Single connection mode only. */
  public void cleanDriverOut() {
    if (! multiConn) {
      if (drvOut != null)
        drvOut.clean();
    }
  }


  /**
   * Method cleaning the <code>DriverOut</code> specified
   * by the key parameter (multi-connections mode).
   *
   * @param  drvKey key identifying the connection set.
   */ 
  public void cleanDriverOut(int drvKey) {
    DriverMonitor dMonitor =
      (DriverMonitor) driversTable.get(new Integer(drvKey));
    if (dMonitor != null) {
      if (dMonitor.drvOut != null)
        (dMonitor.drvOut).clean();
    }
  }


  /**
   * Finalizes this proxy agent execution. Calls <code>disconnect</code>
   * to close the open streams, and <code>stop</code> to stop the drivers.
   *
   * @exception Throwable
   *	unspecialized exception
   */
  protected final void finalize() throws Throwable
  {
    if (! multiConn) {
      if (ois == null && oos == null)
        return;
    }
    else {
      Enumeration keys = driversTable.keys();
      while (keys.hasMoreElements()) {
        Integer key = (Integer) keys.nextElement();
        DriverMonitor dMonitor = (DriverMonitor) driversTable.get(key);

        if (dMonitor != null) {
          if (dMonitor.ois != null || dMonitor.oos != null) {
	    try {
	      (dMonitor.ois).close();
	    } catch (IOException exc) {}
	    try {
	      (dMonitor.oos).close();
	    } catch (IOException exc) {}
  
            stop(key.intValue());
  
            dMonitor.ois = null;
            dMonitor.oos = null;
            dMonitor.qout = null;
          }
        }
      }
      driversTable.clear();
    }

    try {
      disconnect();
    } catch (Exception exc) {
      logmon.log(BasicLevel.WARN, "error in finalize", exc);
    }

    stop();

    qout = null;
    ois = null;
    oos = null;

  }


  /** 
   * Method called by <code>DriverIn</code> instances to forward the 
   * notifications they get. 
   * <p>
   * May be overridden for specific behaviour as long as the proxy state
   * is not modified by the method, because it does not occur within a
   * transaction.
   *
   * @param key  Identifier of the driver calling the method.
   * @param not  Forwarded notification.
   */ 
  void getDriverInNotification(int key, Notification not)
  {
    if (key != 0) 
      sendTo(this.getId(), new DriverNotification(key, not));
    else
      sendTo(this.getId(), not);
  }


  /**
   * Method implementing the <code>ProxyAgent</code> reactions to
   * notifications.
   * Forwards notifications coming from an identified agent onto the outgoing
   * connection.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    int drvKey;
    DriverMonitor dMon;
    DriverIn dIn;
    Queue qo;
    try {
      if (not instanceof DriverDone)
        driverDone((DriverDone) not);
      else if (not instanceof FlowControlNot) {
        // Allowing drvIn to read more data
        drvKey = ((FlowControlNot) not).driverKey;
        // MultiConn proxy: getting the right driverIn to control:
        if (drvKey != 0) {
          dMon = (DriverMonitor) driversTable.get(new Integer(drvKey));
          dIn = dMon.drvIn;
          dIn.recvFlowControl((FlowControlNot) not);
        }
        else
          drvIn.recvFlowControl((FlowControlNot) not);
      }
      // If notification comes from an identified agent:
      else if (! from.equals(this.getId())) {
        // If not is a DriverNotification, pushing it into the right queue out.
        if (not instanceof DriverNotification) {
          drvKey = ((DriverNotification) not).getDriverKey();
          dMon = (DriverMonitor) driversTable.get(new Integer(drvKey));
          qo = dMon.qout;
          qo.push(((DriverNotification) not).getNotification());
        }
        else
          qout.push(not);
      }
      else
        super.react(from, not);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR,
                   "error in " + this + ".react(" + from + ", " + not + ")",
                   exc);
      stop();
      // the proxy agent may restart
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
    if (!multiConn) {
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
    // In case of multiConn, the driver to close is identified in the
    // DriverDone notification.
    else {
      int drvKey = not.getDriverKey();
      DriverMonitor dMonitor = (DriverMonitor)
        driversTable.get(new Integer(drvKey));

      if (dMonitor != null) {
        switch (not.getDriver()) {
          case DRIVER_IN:
	    if (dMonitor.drvIn != null)
	      (dMonitor.drvIn).close();
	    dMonitor.ois = null;
	    dMonitor.drvIn = null;
            break;
          case DRIVER_OUT:
	    if (dMonitor.drvOut != null)
	      (dMonitor.drvOut).close();
	    dMonitor.oos = null;
	    dMonitor.drvOut = null;
            break;
        }
        // When both drivers have been closed, removing the entry
        // corresponding to their pair from the driversTable.
        if (dMonitor.drvIn == null && dMonitor.drvOut == null)
          driversTable.remove(new Integer(drvKey));
      }
    } 
  }

}