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

import org.objectweb.monolog.api.BasicLevel;
import org.objectweb.monolog.api.Monitor;

/**
 * Input driver.
 */
class DriverIn extends Driver {
  /** RCS version number of this file: $Revision: 1.8 $ */
  public static final String RCS_VERSION="@(#)$Id: DriverIn.java,v 1.8 2002-01-16 12:46:47 joram Exp $";

  /** Proxy this <code>DriverIn<code> belongs to. */
  private ProxyAgent proxy;

  /** Id of proxy this <code>DriverIn</code> belongs to. */
  protected AgentId proxyId;

  /** stream to read notifications from */
  protected NotificationInputStream in;

  /** number of notifications sent since last sent <code>FlowControl</code> */
  int nbNotSent = 0;

  /** max number of notifications between two <code>FlowControl</code>s */
  int maxNotSent = 0;

  /** number of <code>FlowControl</code>s sent and not received by proxy */
  int nbFlowControl = 0;

  /**
   * Identifies this <code>DriverIn</code> among the drivers of a multi
   * connections proxy.
   * If <code>key</code> equals 0, this driver does not belong to a multi
   * connections proxy.
   */
  private int key = 0;

  /**
   * Constructor.
   *
   * @param id		identifier local to the driver creator
   * @param proxy	proxy agent to forward notifications to
   * @param in		stream to read notifications from
   * @param maxNotSent	max number of notifications between
   *			<code>FlowControl</code>s
   */
  DriverIn(int id,
           ProxyAgent proxy,
           NotificationInputStream in,
           int maxNotSent) {
    super(id);
    this.maxNotSent = maxNotSent;
    this.proxy = proxy;
    this.proxyId = proxy.getId();
    this.in = in;
    this.name = proxy.getName() + ".DriverIn#" + id;
    // Get the logging monitor using proxy topic
    String classname = getClass().getName();
    logmon = Debug.getMonitor(proxy.getLogTopic()+ '.' +
      classname.substring(classname.lastIndexOf('.') +1));
  }

  /**
   * Constructor called by a <code>ProxyAgent</code> managing multiple
   * connections.
   * @param id          identifier local to the driver creator
   * @param proxy       proxy agent to forward notifications to
   * @param in          stream to read notifications from
   * @param maxNotSent  max number of notifications between
   *			<code>FlowControl</code>s
   * @param key 	key identifying the connection.
   */
  DriverIn(int id,
           ProxyAgent proxy,
           NotificationInputStream in, 
           int maxNotSent,
           int key) {
    this(id, proxy, in, maxNotSent);
    this.key = key;
  }

  /**
   * Returns name of driver.
   */
  public String getName() {
    return name;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",key=" + key +
      ",nbNotSent=" + nbNotSent +
      ",maxNotSent=" + maxNotSent +
      ",nbFlowControl=" + nbFlowControl + ")";
  }


  synchronized void sendFlowControl() throws IOException {
    nbFlowControl += 1;
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 getName() + ", sendFlowControl#" + nbFlowControl);
    // Single connection context:
    if (key == 0)
      sendTo(proxyId, new FlowControlNot(id));

    // In a multi-connections context, flagging the FlowControlNot
    // notification with the key so that it is known which 
    // DriverIn to control.
    else
      sendTo(proxyId, new FlowControlNot(id, key));
    while (nbFlowControl > 1) {
      try { wait(); } catch (InterruptedException e) {}
    }
  }


  synchronized void recvFlowControl(FlowControlNot not) {
    nbFlowControl -= 1;
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 getName() + ", recvFlowControl#" + nbFlowControl);
    notify();
  }


  public void run() {
    Notification m;
    mainLoop:
    while (isRunning) {
      m = null;
      canStop = true;
      try {
        if (nbNotSent > maxNotSent) {
          try {
            sendFlowControl();
          } catch (IOException exc) {
            logmon.log(BasicLevel.ERROR,
                       getName() + ", error during sendFlowControl", exc);
            break mainLoop;
          }
          nbNotSent = 0;
        }
        m = in.readNotification();
      } catch (EOFException exc) {
        // End of input flow.
        break mainLoop;
      } catch (Exception exc) {
        logmon.log(BasicLevel.WARN,
                   getName() + ", error in readNotification", exc);
        break mainLoop;
      }
      canStop = false;

      if (m != null) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + ", read " + m);

        proxy.getDriverInNotification(key, m);
        nbNotSent += 1;
      }
    }
  }

  /**
   * Finalizes the driver.
   *
   * Reports driver end to the proxy agent, with a <code>DriverDone</code>
   * notification.
   */
  protected void end() {
    // report end to proxy
    try {
      // Single connection context
      if (key == 0)
        sendTo(proxyId, new DriverDone(id));  

      // In a multi-connections context, flagging the DriverDone
      // notification with the key so that it is known which 
      // DriverIn to close.
      else
        sendTo(proxyId, new DriverDone(id, key));
      
    } catch (IOException exc) {
      logmon.log(BasicLevel.ERROR,
                 getName() + ", error in reporting end", exc);
    }
  }

  /**
   * Close the OutputStream.
   */
  public void close() {
    try {
      in.close();
    } catch (Exception exc) {}
    in = null;
  }
}
