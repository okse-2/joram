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


/**
 * Input driver.
 */
class DriverIn extends Driver {

  /** RCS version number of this file: $Revision: 1.1.1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: DriverIn.java,v 1.1.1.1 2000-05-30 11:45:24 tachkeni Exp $";

  /** id of agent to forward notifications to */
  protected AgentId proxy;

  /** stream to read notifications from */
  protected NotificationInputStream in;

  /** number of notifications sent since last sent <code>FlowControl</code> */
  int nbNotSent = 0;

  /** max number of notifications between two <code>FlowControl</code>s */
  int maxNotSent = 0;

  /** number of <code>FlowControl</code>s sent and not received by proxy */
  int nbFlowControl = 0;
  
  /**
   * Constructor.
   *
   * @param id		identifier local to the driver creator
   * @param proxy	id of agent to forward notifications to
   * @param in		stream to read notifications from
   * @param maxNotSent	max number of notifications between <code>FlowControl</code>s
   */
  DriverIn(int id, AgentId proxy, NotificationInputStream in, int maxNotSent) {
    super(id);
    this.maxNotSent = maxNotSent;
    this.proxy = proxy;
    this.in = in;
  }

  /**
   * Constructor with default <code>id</code>.
   *
   * @param proxy	id of agent to forward notifications to
   * @param in		stream to read notifications from
   * @param maxNotSent	max number of notifications between <code>FlowControl</code>s
   */
  DriverIn(AgentId proxy, NotificationInputStream in, int maxNotSent) {
    this(0, proxy, in, maxNotSent);
  }

  /**
   * Constructor with default <code>maxNotSent</code>.
   *
   * @param id		identifier local to the driver creator
   * @param proxy	id of agent to forward notifications to
   * @param in		stream to read notifications from
   */
  DriverIn(int id, AgentId proxy, NotificationInputStream in) {
    this(id, proxy, in, 100);
  }

  /**
   * Constructor with default <code>id</code> and <code>maxNotSent</code>.
   *
   * @param proxy	id of agent to forward notifications to
   * @param in		stream to read notifications from
   */
  DriverIn(AgentId proxy, NotificationInputStream in) {
    this(proxy, in, 100);
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",nbNotSent=" + nbNotSent +
      ",maxNotSent=" + maxNotSent +
      ",nbFlowControl=" + nbFlowControl + ")";
  }

  synchronized void sendFlowControl() throws IOException {
    nbFlowControl += 1;
    if (Debug.driversControl)
      Debug.trace("in driver sendFlowControl#" + nbFlowControl, false);
    sendTo(proxy, new FlowControlNot(id));
    while (nbFlowControl > 1) {
      try { wait(); } catch (InterruptedException e) {}
    }
  }

  synchronized void recvFlowControl(FlowControlNot not) {
    nbFlowControl -= 1;
    if (Debug.driversControl)
      Debug.trace("in driver recvFlowControl#" + nbFlowControl, false);
    notify();
  }

  public void run() {
    Notification m;
    mainLoop:
    while (true) {
      m = null;
      if (nbNotSent > maxNotSent) {
	try {
	  sendFlowControl();
	} catch (IOException exc) {
	  if (Debug.error)
	    Debug.trace("in driver read sendFlowControl", exc);
	  break mainLoop;
	}
	nbNotSent = 0;
      }
      try {
	m = in.readNotification();
      } catch (EOFException exc) {
	// End of input flow.
	break mainLoop;
      } catch (Exception exc) {
	if (Debug.error)
	  Debug.trace("error in " + in + ".readNotification", exc);
	break mainLoop;
      }
      if (m != null) {
	if (Debug.driversData)
	  Debug.trace("in driver read " + m, false);
	try {
	  react(m);
	  nbNotSent += 1;
	} catch (IOException exc) {
	  if (Debug.error)
	    Debug.trace("in driver read " + m, exc);
	  break mainLoop;
	}
      }
    }
  }

  /**
   * Reacts to a notification from the input stream.
   *
   * Base class behaviour forwards the notification to the proxy agent.
   *
   * @param not		notification to react to
   */
  void react(Notification not) throws IOException {
    sendTo(proxy, not);
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
      sendTo(proxy, new DriverDone(id));
    } catch (IOException exc) {
      if (Debug.error)
	Debug.trace("error in reporting end of DriverIn", exc);
    }
  }
}