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

import fr.dyade.aaa.util.*;

public abstract class AgentDriver extends Driver {
  /** RCS version number of this file: $Revision: 1.9 $ */
  public static final String RCS_VERSION="@(#)$Id: AgentDriver.java,v 1.9 2002-03-06 16:50:00 joram Exp $";

  /** id of associated proxy agent */
  protected AgentId proxy;
  /** queue of <code>Notification</code> objects to be sent */
  protected Queue mq;

  /**
   * Constructor.
   *
   * @param proxy	id of associated proxy agent
   * @param mq		queue of <code>Notification</code> objects to be sent
   * @param out		stream to write notifications to
   */
  protected AgentDriver(int id, Agent proxy, Queue mq) {
    super(id);
    this.proxy = proxy.getId();
    this.mq = mq;
    this.name = proxy.getName() + ".AgentDriver#" + id;
    // Get the proxy logging monitor
    logmon = proxy.logmon;
  }

  /**
   * Provides a string image for this object.
   */
  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    output.append(super.toString());
    output.append(",proxy=");
    output.append(proxy);
    output.append(",mq=");
    output.append(mq);
    output.append(")");
    return output.toString();
  }

  public void run() {
    Notification m = null;
    mainLoop:
    while (isRunning) {
      try {
	canStop = true;
	m = (Notification) mq.get();
	if (! isRunning) break mainLoop;
	react(m);
	canStop = false;
      } catch (Exception exc) {
        logmon.log(BasicLevel.ERROR,
                   getName() + ", exception in " + this +
                   ".react(" + m + ")", exc);
	break mainLoop;
      }
      mq.pop();
    }
  }

  public void close() {}

  /**
   * Reacts to notifications from proxy.
   *
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  protected abstract void react(Notification m) throws Exception;

  /**
   * Finalizes the driver.
   *
   * Reports driver end to the proxy agent, with a <code>DriverDone</code>
   * notification.
   *
   * @param not		notification to react to
   */
  protected void end() {
    // report end to proxy
    try {
      sendTo(proxy, new DriverDone(id));
    } catch (IOException exc) {
      logmon.log(BasicLevel.ERROR,
                   getName() + ", error in reporting end", exc);
    }
  }
}
