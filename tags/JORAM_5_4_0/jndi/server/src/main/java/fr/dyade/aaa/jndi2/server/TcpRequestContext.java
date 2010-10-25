/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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

import java.net.*;

import org.objectweb.util.monolog.api.BasicLevel;

public class TcpRequestContext extends RequestContext {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  private transient IOControl ioCtrl;

  private JndiRequest request;

  public TcpRequestContext(Socket socket) throws Exception {
    ioCtrl = new IOControl(socket);
    request = (JndiRequest)ioCtrl.readObject();
  }

  public JndiRequest getRequest() {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "TcpRequestContext.getRequest()"); 
    return request;
  }

  public void reply(JndiReply reply) {
    if (ioCtrl == null) {
      Trace.logger.log(BasicLevel.WARN, "TcpRequestContext.reply(" + reply + ") ioCtrl is null");
      return;
    }
      
    try {
      ioCtrl.writeObject(reply);
    } catch (Exception exc) {
      Trace.logger.log(BasicLevel.ERROR, "TcpRequestContext.reply(" + reply + ")", exc);
    } finally {
      ioCtrl.close();
    }
  }
  
  public String toString() {
    return '(' + super.toString() + ",request=" + request + ')';
  }
}
