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

/**
 * Output driver.
 */
class DriverOut extends Driver {
  /** RCS version number of this file: $Revision: 1.10 $ */
  public static final String RCS_VERSION="@(#)$Id: DriverOut.java,v 1.10 2002-01-16 12:46:47 joram Exp $";

  /** id of associated proxy agent */
  protected AgentId proxy;
  /** queue of <code>Notification</code> objects to be sent */
  protected Queue mq;
  /** stream to write notifications to */
  protected NotificationOutputStream out;

  /** Identifies the <code>DriverOut</code> in a multi-connections context. */
  private int key = 0;
 
  /**
   * Constructor.
   *
   * @param id		identifier local to the driver creator
   * @param proxy	id of associated proxy agent
   * @param mq		queue of <code>Notification</code> objects to be sent
   * @param out		stream to write notifications to
   */
  DriverOut(int id,
            ProxyAgent proxy,
            Queue mq,
            NotificationOutputStream out) {
    super(id);
    this.proxy = proxy.getId();
    this.mq = mq;
    this.out = out;
    this.name = proxy.getName() + ".DriverOut#" + id;
    // Get the logging monitor using proxy topic
    String classname = getClass().getName();
    logmon = Debug.getMonitor(proxy.getLogTopic()+ '.' +
      classname.substring(classname.lastIndexOf('.') +1));
  }

  /**
   * Constructor called by a <code>ProxyAgent</code> managing multiple
   * connections.
   *
   * @param id      identifier local to the driver creator
   * @param proxy   associated proxy agent
   * @param mq      queue of <code>Notification</code> objects to be sent
   * @param out     stream to write notifications to
   * @param key     key identifying the connection.
   */
  DriverOut(int id,
            ProxyAgent proxy,
            Queue mq,
            NotificationOutputStream out,
            int key)
  {
    this(id, proxy, mq, out);
    this.key = key;
  }

  /**
   * Returns name of driver.
   */
  public String getName() {
    return name;
  }

  public void run() {
    Notification m = null;
    mainLoop:
    while (isRunning) {
      try {
        canStop = true;
        m = (Notification) mq.get();
        if (! isRunning) break;
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + ", write: " + m);
        canStop = false;
        out.writeNotification(m);
      } catch (IOException exc) {
        logmon.log(BasicLevel.WARN,
                   getName() + ", write failed" + m, exc);
        break mainLoop;
      } catch (InterruptedException exc) {
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
      // Single connection context.
      if (key == 0)
        sendTo(proxy, new DriverDone(id));

      // In a multi-connections context, flagging the DriverDone
      // notification with the key so that it is known which 
      // DriverOut to close.
      else
        sendTo(proxy, new DriverDone(id, key));

    } catch (IOException exc) {
      logmon.log(BasicLevel.ERROR,
                       getName() + ", error in reporting end", exc);
    }
  }

  /** remove all elements of queue */
  protected void clean() {
    mq.removeAllElements();
  }
}
