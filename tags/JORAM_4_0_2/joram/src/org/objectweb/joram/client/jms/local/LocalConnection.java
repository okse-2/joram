/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
 * Initial developer(s): David Feliot (ScalAgent DT)
 * Contributor(s): 
 */
package org.objectweb.joram.client.jms.local;

import org.objectweb.joram.client.jms.*;
import org.objectweb.joram.client.jms.Connection;
import org.objectweb.joram.mom.proxies.*;
import org.objectweb.joram.shared.client.*;
import org.objectweb.joram.mom.MomTracing;

import fr.dyade.aaa.util.Queue;

import javax.jms.*;

import org.objectweb.util.monolog.api.BasicLevel;

public class LocalConnection 
    implements org.objectweb.joram.client.jms.ConnectionItf {  

  private UserConnection userConnection;

  public LocalConnection(
    String userName, String password) throws JMSException {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "LocalConnection.<init>(" + userName + ')');
    try {
      userConnection = ConnectionManager.openConnection(
        userName, password, 0, null);
    } catch (Exception exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
      throw new JMSException(exc.getMessage());
    }
  }

  public void send(AbstractJmsRequest request) throws javax.jms.IllegalStateException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, 
        "LocalConnection.send(" + request + ')');
    try {
      userConnection.send(request);
    } catch (Exception exc) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(
          BasicLevel.DEBUG, "", exc);
      throw new javax.jms.IllegalStateException(
        exc.getMessage());
    }
  }

  public Driver createDriver(Connection cnx) {
    LocalDriver driver = 
      new LocalDriver(cnx, this);
    driver.setDaemon(true);
    return driver;
  }

  AbstractJmsReply getReply() 
    throws InterruptedException {
    return userConnection.receive();
  }

  public void close() {
    // Nothing to do
  }

  public boolean syncClosure() {
    return false;
  }
}
