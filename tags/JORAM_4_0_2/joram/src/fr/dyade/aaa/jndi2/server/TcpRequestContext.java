/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): David Feliot, Nicolas Tachker
 */
package fr.dyade.aaa.jndi2.server;

import fr.dyade.aaa.jndi2.msg.*;

import java.io.*;
import java.net.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class TcpRequestContext 
    extends RequestContext {
  
  private transient Socket socket;

  private transient ObjectInputStream receiver;

  private transient SerialOutputStream sender;

  private JndiRequest request;

  public TcpRequestContext(Socket socket) throws Exception {
    this.socket = socket;
    sender = 
      new SerialOutputStream(socket.getOutputStream());
    receiver = 
      new ObjectInputStream(socket.getInputStream());      
    request = (JndiRequest)receiver.readObject();
  }

  public JndiRequest getRequest() {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "TcpRequestContext.getRequest()"); 
    return request;
  }

  public void reply(JndiReply reply) {
    if (sender != null) {
      try {
        sender.writeObject(reply);
      } catch (Exception exc) {
        Trace.logger.log(BasicLevel.ERROR, "", exc);
      }
      try {
        receiver.readObject();
      } catch (Exception exc) {
        // Do nothing
      } finally {
        close();
      }
    }
  }
  
  private void close() {
    // Closes the connection
    try {
      socket.getInputStream().close();
    } catch (Exception exc) {}
    try {
      socket.getOutputStream().flush();
      socket.getOutputStream().close();
    } catch (Exception exc) {}
    try {
      socket.close();
    } catch (Exception exc) {}
    socket = null;
    sender = null;
  }

  public String toString() {
    return '(' + super.toString() +
      ",request=" + request + ')';
  }
}
