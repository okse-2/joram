/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - ScalAgent Distributed Technologies
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
package monitoring;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Consumes messages from the monitoring topic.
 */
public class Diagnose {

  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("Queries to the monitoring queue...");

    Context ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("MonitoringQueue");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer prod = sess.createProducer(queue);
    MessageConsumer cons = sess.createConsumer(queue);
    cnx.start();

    Message msg1 = sess.createMessage();
    msg1.setStringProperty("Joram#0:type=Destination,name=*",
                           "NbMsgsReceiveSinceCreation,NbMsgsSentToDMQSinceCreation");
    prod.send(msg1);
    System.out.println(" --> Monitoring message sent: " + msg1.getJMSMessageID());
    
    Message msg2 = cons.receive();
    
    System.out.println(" --> Monitoring message received: " + msg2.getJMSMessageID());
    try {
      Monitor.doReport(msg2);
    } catch (JMSException exc) {
      exc.printStackTrace();
    }

    cnx.close();

    System.out.println();
    System.out.println("Consumer closed.");
  }  
}
