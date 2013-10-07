/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
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
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Producer/Consumer generating dead messages.
 */
public class DMQClient {
  public static void main(String[] args) throws Exception {
    Context ictx = new InitialContext();
    Queue queue1 = (Queue) ictx.lookup("queue1");
    Queue queue2 = (Queue) ictx.lookup("queue2");
//    Queue queue3 = (Queue) ictx.lookup("queue3");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session prodSession = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Session consSession = cnx.createSession(true, 0);
    
    MessageProducer producer = prodSession.createProducer(null);
    MessageConsumer consumer = consSession.createConsumer(queue1);

    cnx.start();

    // Producing messages with a very short time to live: 20 ms.
    System.out.println("Sends Message1 with a very short time to live");
    TextMessage msg = prodSession.createTextMessage("Message1");
    producer.send(queue1, msg, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, 20);
    
    // Waiting for the message to be expired.
    System.out.println("Waits for the message to be expired");
    Thread.sleep(100);

    msg = (TextMessage) consumer.receiveNoWait();
    System.out.println("receives: " + msg);
    
    // Producing "undeliverable" messages
    System.out.println("Send Message2");   
    msg = prodSession.createTextMessage("Message2");
    producer.send(queue1, msg);
    
    msg = (TextMessage) consumer.receive();
    System.out.println("Receives: " + msg.getText() + " then deny it!");
    consSession.rollback();
    
    msg = (TextMessage) consumer.receive();
    System.out.println("Receives: " + msg.getText() + " then deny it!");
    consSession.rollback();
        
    // Producing "forbidden" messages
    System.out.println("Send Message3");   
    msg = prodSession.createTextMessage("Message3");
    try {
      producer.send(queue2, msg);
    } catch (JMSException exc) {
      System.out.println(exc.getMessage());
    }
    
//    // Producing a message to a deleted destination
//    System.out.println("Send Message4");   
//    msg = prodSession.createTextMessage("Message4");
//    try {
//      producer.send(queue3, msg);
//    } catch (JMSException exc) {
//      System.out.println(exc.getMessage());
//    }

    cnx.close();
  }
}
