/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.proxies;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.objectweb.joram.mom.proxies.tcp.IOControl;
import org.objectweb.joram.mom.proxies.tcp.TcpConnection;
import org.objectweb.joram.shared.client.MomExceptionReply;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

public class QueueWorker implements Runnable {

  /** logger */
  public static Logger logger = Debug.getLogger(QueueWorker.class.getName());

  public ConcurrentLinkedQueue<ProxyMessage> queue = new ConcurrentLinkedQueue<ProxyMessage>();
  public boolean running;

  public TcpConnection tcpConnection;
  public IOControl ioctrl;

  private void handleMessage(ProxyMessage msg) throws Exception {
    if ((msg.getObject() instanceof MomExceptionReply) &&
        (((MomExceptionReply) msg.getObject()).getType() == MomExceptionReply.HBCloseConnection)) {
      // Exception indicating that the connection
      // has been closed by the heart beat task.
      // (see UserAgent)
      new Thread(new Runnable() {
        public void run() {            
          tcpConnection.close();
        }
      }).start();
    } else {
      ioctrl.send(msg);
    }
  }

  public void run() {
    try {
      while (true) {
        ProxyMessage msg = queue.poll();
        if (msg == null) {
          synchronized (queue) {
            msg = queue.poll();
            if (msg == null) {
              running = false;
              return;
            }
          }
        }
        handleMessage(msg);
      }
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, e);
    }
  } 
}
