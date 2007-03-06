/*
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
 */
package fr.dyade.aaa.agent;

import java.io.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.*;

public abstract class AgentDriver extends Driver {

  /** id of associated proxy agent */
  protected AgentId proxy;
  /** queue of <code>Notification</code> objects to be sent */
  protected Queue mq;

  /**
   * Constructor.
   *
   * @param id		internal id of driver
   * @param proxy	Reference to the associated proxy agent
   * @param mq		Queue of <code>Notification</code> objects to be sent
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
  protected abstract void react(Notification not) throws Exception;

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
      logmon.log(BasicLevel.ERROR,
                   getName() + ", error in reporting end", exc);
    }
  }
}
