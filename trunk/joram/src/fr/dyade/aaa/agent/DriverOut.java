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


class DriverOut extends Driver {

  /** RCS version number of this file: $Revision: 1.5 $ */
  public static final String RCS_VERSION="@(#)$Id: DriverOut.java,v 1.5 2000-10-20 13:56:13 tachkeni Exp $";

  /** id of associated proxy agent */
  protected AgentId proxy;
  /** queue of <code>Notification</code> objects to be sent */
  protected Queue mq;
  /** stream to write notifications to */
  protected NotificationOutputStream out;

  /**
   * Constructor.
   *
   * @param id		identifier local to the driver creator
   * @param proxy	id of associated proxy agent
   * @param mq		queue of <code>Notification</code> objects to be sent
   * @param out		stream to write notifications to
   */
  DriverOut(int id, AgentId proxy, Queue mq, NotificationOutputStream out) {
    super(id);
    this.proxy = proxy;
    this.mq = mq;
    this.out = out;
  }

  /**
   * Constructor with default id.
   *
   * @param proxy	id of associated proxy agent
   * @param mq		queue of <code>Notification</code> objects to be sent
   * @param out		stream to write notifications to
   */
  DriverOut(AgentId proxy, Queue mq, NotificationOutputStream out) {
    this(0, proxy, mq, out);
  }

  public void run() {
    Notification m = null;
    mainLoop:
    while (isRunning) {
	try {
	    canStop = true;
	    m = (Notification) mq.get();
	    if (! isRunning)
		break;
	    if (Debug.driversData)
		Debug.trace("out driver write " + m, false);
	    canStop = false;
	    out.writeNotification(m);
	} catch (IOException exc) {
	    if (Debug.error)
		Debug.trace("out driver write " + m, exc);
	    break mainLoop;
	} catch (InterruptedException exc) {
	    if (Debug.error)
		Debug.trace("out driver write " + m, exc);
	    break mainLoop;
	}

	mq.pop();
    }
  }
    
  /**
   * Close the OutputStream.
   */
    public void close() {
    try {
      out.close();
    } catch (Exception exc) {}
    out = null;
  }

  /**
   * Sends a notification on the output stream.
   *
   * @param not		notification to send
   */
  void sendTo(Notification not) {
    mq.push(not);
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
	Debug.trace("error in reporting end of DriverOut", exc);
    }
  }
    /** remove all elements of queue */
    protected void clean() {
	mq.removeAllElements();
    }
}
