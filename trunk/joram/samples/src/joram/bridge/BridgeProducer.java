/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 - 2010 ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s): 
 */
package bridge;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Produces messages on the foreign destination.
 */
public class BridgeProducer {

  public static void main(String[] args) throws Exception {

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    Destination bridgeDest = (Destination) jndiCtx.lookup("bridgeQueue");
    ConnectionFactory bridgeCF = (ConnectionFactory) jndiCtx.lookup("bridgeCF");
    jndiCtx.close();

    Connection bridgeCnx = bridgeCF.createConnection();
    Session bridgeSess = bridgeCnx.createSession(true, 0);
    MessageProducer bridgeProducer = bridgeSess.createProducer(bridgeDest);

    TextMessage msg = bridgeSess.createTextMessage();

    for (int i = 1; i < 11; i++) {
      msg.setText("Joram message number " + i + " sent through distribution bridge queue.");
      System.out.println("send msg = " + msg.getText());
      bridgeProducer.send(msg);
    }

    bridgeSess.commit();

    bridgeCnx.close();
  }
}
