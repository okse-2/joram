/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package deadMQueue;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Producer/Consumer generating dead messages.
 */
public class DMQClient {

  static Context ictx = null;

  public static void main(String[] args) throws Exception {
    
    ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("queue");
    
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cnxFact");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session prodSession = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Session consSession = cnx.createSession(true, 0);
    
    MessageProducer qProducer = prodSession.createProducer(queue);
    MessageConsumer qConsumer = consSession.createConsumer(queue);

    cnx.start();

    TextMessage msg = prodSession.createTextMessage();

    // Producing messages with a very short time to live: 1 ms.
    msg.setText("Expiry test");
    qProducer.send(msg, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, 1);
    
    // Waiting for the message to be expired.
    Thread.sleep(100);

    qConsumer.receiveNoWait();

    
    // Producing "undeliverable" messages: 
    msg.setText("Undeliverability test");
    qProducer.send(msg);
    
    qConsumer.receive();
    consSession.rollback();
    
    qConsumer.receive();
    consSession.rollback();
    
    consSession.commit();
    
    cnx.close();
  }
}
