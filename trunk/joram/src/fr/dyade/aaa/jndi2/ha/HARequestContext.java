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
 * Initial developer(s): David Feliot
 */
package fr.dyade.aaa.jndi2.ha;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.jndi2.msg.IOControl;
import fr.dyade.aaa.jndi2.msg.JndiReply;
import fr.dyade.aaa.jndi2.msg.JndiRequest;
import fr.dyade.aaa.jndi2.server.RequestContext;
import fr.dyade.aaa.jndi2.server.Trace;

public class HARequestContext extends RequestContext {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  private transient IOControl ioCtrl;

  private JndiRequest request;

  private JndiReply reply;

  private int id;

  //  private transient HARequestManager manager;

  HARequestContext(IOControl ioCtrl,
                   int id) throws Exception {
    this.ioCtrl = ioCtrl;
    this.id = id;
    request = (JndiRequest)ioCtrl.readObject();
  }

  public final int getId() {
    return id;
  }

  void recover(HARequestContext ctx) {
    ioCtrl = ctx.ioCtrl;
  }

  final JndiReply getReply() {
    return reply;
  }

//   final Socket getSocket() {
//     return ioCtrl.getSocket();
//   }

  public final JndiRequest getRequest() {
    return request;
  }

  public void reply(JndiReply reply) {
    this.reply = reply;
    if (ioCtrl != null) {
      try {
        ioCtrl.writeObject(reply);
      } catch (Exception exc) {
        Trace.logger.log(BasicLevel.ERROR, "", exc);
      } finally {
        ioCtrl.close();
      }
    }
  }

  //  private void close() {
  //    // Closes the connection
  //    ioCtrl.close();
  //    if (manager != null)
  //      manager.removeContext(id);
  //  }
  
  public String toString() {
    return '(' + super.toString() +
      ",request=" + request + ')';
  }
}
