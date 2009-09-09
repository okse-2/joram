/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 - ScalAgent DT
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
public class ForeignProducer {
  public static void main(String[] args) throws Exception {
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
    Destination foreignDest = (Destination) jndiCtx.lookup("foreignQueue");
    ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF");
    jndiCtx.close();

    Connection foreignCnx = foreignCF.createConnection();
    Session foreignSess = foreignCnx.createSession(true, 0);
    MessageProducer foreignSender = foreignSess.createProducer(foreignDest);

    TextMessage foreignMsg = foreignSess.createTextMessage();

    for (int i = 1; i < 11; i++) {
      foreignMsg.setText("Foreign message number " + i);
      System.out.println("send msg = " + foreignMsg.getText());
      foreignSender.send(foreignMsg);
    }

    foreignSess.commit();
    
    
    foreignCnx.close();
  }
}
