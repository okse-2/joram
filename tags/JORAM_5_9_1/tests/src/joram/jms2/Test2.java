/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): 
 */
package joram.jms2;

import javax.jms.CompletionListener;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Send a NON_PERSISTENT message in asynchronous mode.
 */
public class Test2 extends TestCase implements MessageListener, CompletionListener {
  public static void main(String[] args) {
    new Test2().run();
  }
  
  public void run()  {
    try {
      startAgentServer((short) 0);
      Thread.sleep(1000);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      AdminModule.connect(cf, "root", "root");   
      User.create("anonymous", "anonymous", 0);
      Queue queue = Queue.create("queue");
      queue.setFreeReading();
      queue.setFreeWriting();
      AdminModule.disconnect();

      Connection cnx = cf.createConnection();
      Session session = cnx.createSession();
      MessageProducer producer = session.createProducer(null);
      MessageConsumer consumer = session.createConsumer(queue);
      cnx.start();

      TextMessage msg = session.createTextMessage("message");
      producer.send(queue, msg, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, 0L, this);
      consumer.receive();
      assertTrue("Should complete send", sent);
      
      cnx.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();     
    }
  }

  volatile int nbmsg = 0;
  
  @Override
  public void onMessage(Message message) {
    nbmsg += 1;
  }

  volatile boolean sent = false;
  
  @Override
  public void onCompletion(Message message) {
    System.out.println("sent");
    sent = true;
  }

  @Override
  public void onException(Message message, Exception exc) {
    exc.printStackTrace();
  }
}