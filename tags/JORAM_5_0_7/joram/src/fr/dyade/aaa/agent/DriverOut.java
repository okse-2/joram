/*
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 * Output driver.
 */
class DriverOut extends Driver {

  /** Reference to the proxy agent */
  protected ProxyAgent proxy;
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
   * @param proxy	associated proxy agent
   * @param mq		queue of <code>Notification</code> objects to be sent
   * @param out		stream to write notifications to
   */
  DriverOut(int id,
            ProxyAgent proxy,
            Queue mq,
            NotificationOutputStream out) {
    super(id);
    this.proxy = proxy;
    this.mq = mq;
    this.out = out;
    this.name = proxy.getName() + ".DriverOut#" + id;
    // Get the logging monitor using proxy topic
    String classname = getClass().getName();
    logmon = Debug.getLogger(proxy.getLogTopic()+ '.' +
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
        canStop = false;
        if (! proxy.finalizing) {
          logmon.log(BasicLevel.WARN,
                     getName() + ", write failed" + m, exc);
        }
        break mainLoop;
      } catch (InterruptedException exc) {
        canStop = false;
        break mainLoop;
      } finally {
        Thread.interrupted();
        canStop = false;
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
        sendTo(proxy.getId(), new DriverDone(id));

      // In a multi-connections context, flagging the DriverDone
      // notification with the key so that it is known which 
      // DriverOut to close.
      else
        sendTo(proxy.getId(), new DriverDone(id, key));

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
