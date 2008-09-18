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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Consumes messages from the queue and from the topic.
 */
public class Consumer {

  public static void main(String[] args) throws Exception {

    System.out.println();
    System.out.println("Listens to the queue...");

    Context ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("queue");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer recv = sess.createConsumer(queue);

    cnx.start();

    for (int i = 0; i < 10; i++) {
      Message message = recv.receive();
      System.out.println(message);
    }
    
    cnx.close();

    System.out.println();
    System.out.println("Consumer closed.");
  }

  static class MsgListener implements MessageListener {
    
    public void onMessage(Message msg) {
      try {
        System.out.println(((TextMessage) msg).getText());
      } catch (JMSException jE) {
        System.err.println("Exception in listener: " + jE);
      }
    }
    
  }

}
