/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Feliot David  (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.client;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;


/**
 * @author feliot
 *
 * concurrent close consumer and a consumer listener
 */
public class MsgConsUnsubscribe extends TestCase {
  
  public static void main(String[] args) {
    new MsgConsUnsubscribe().run();
  }
  
  private Connection connection;

  private Destination dest;
  
  public void run() {
    try {
      startAgentServer(
        (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});
      
      AdminModule.connect("localhost", 2560, "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = org.objectweb.joram.client.jms.admin.User
          .create("anonymous", "anonymous", 0);

      org.objectweb.joram.client.jms.Queue queue = org.objectweb.joram.client.jms.Queue
          .create(0);
      queue.setFreeReading();
      queue.setFreeWriting();
      
      org.objectweb.joram.client.jms.Topic topic = org.objectweb.joram.client.jms.Topic
          .create(0);
      topic.setFreeReading();
      topic.setFreeWriting();

      dest = topic;
      
      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);
      
      connection = cf.createConnection(
          "anonymous", "anonymous");
      
      connection.start();
      
      Session session = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      
      MessageProducer producer = session.createProducer(dest);

      MessageConsumer consumer = session.createConsumer(dest);

      consumer.setMessageListener(
        new MessageListener() {
            public void onMessage(Message message) {              
              
            }
        });
      
      consumer.close();
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }

}
