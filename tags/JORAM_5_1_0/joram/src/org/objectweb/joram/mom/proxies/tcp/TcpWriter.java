/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
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
 * Contributor(s): 
 */
package org.objectweb.joram.mom.proxies.tcp;

import java.io.*;

import org.objectweb.joram.mom.proxies.*;
import org.objectweb.joram.shared.client.MomExceptionReply;

import fr.dyade.aaa.util.*;

import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The activity responsible for getting the replies
 * from the user's proxy and writing them to the
 * socket. 
 */
public class TcpWriter extends Daemon {
  /**
   * The TCP connection that started this writer.
   */
  private TcpConnection tcpConnection;

  private IOControl ioctrl;

  private AckedQueue replyQueue;

  /**
   * Creates a new writer.
   *
   * @param sock the socket where to write
   * @param userConnection the connection 
   * with the user's proxy
   * @param tcpConnection the TCP connection
   */
  public TcpWriter(IOControl ioctrl,
		   AckedQueue replyQueue,
                   TcpConnection tcpConnection) 
    throws IOException {
    super("tcpWriter");
    this.ioctrl = ioctrl;
    this.replyQueue = replyQueue;
    this.tcpConnection = tcpConnection;
    replyQueue.reset();
  }

  public void run() {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,  "TcpWriter.run()");
    try {
      while (running) {
        ProxyMessage msg =  
          (ProxyMessage)replyQueue.get();
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
          // No queue.pop() !
          // Done by the proxy (UserAgent)
        }
      }
    } catch (Exception exc) {
      if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgProxy.log(
          BasicLevel.DEBUG, "", exc);
    }
  }

  protected void shutdown() {
    close();
  }
    
  protected void close() {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "TcpWriter.close()", new Exception());
    if (ioctrl != null)
      ioctrl.close();
    ioctrl = null;
  }
}