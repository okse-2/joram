/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2013 ScalAgent Distributed Technologies
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
package joram.dursub;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Test durable subscription:
 * - Create a durable subscriber with name "dursub1" and clientId "cnx1".
 * - Create another durable subscriber with the same subscription name and verify
 *   that using an already active subscription generate an exception.
 * - Close the session and connection of the active subscriber.
 * - Create a subscriber to the durable subscription and verify that it is ok.
 * - Close the connection.
 * - Create a subscriber to the durable subscription and verify that it is ok.
 */
public class Test1 extends framework.TestCase {
  public static void main(String[] args) throws Exception {
    new Test1().run(args);
  }

  public void run(String[] args) throws Exception {
    try{
      startAgentServer((short)0);
      Thread.sleep(1000L);

      ConnectionFactory cf =  TcpConnectionFactory.create("localhost", 2560);
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 10;
      AdminModule.connect(cf, "root", "root");

      User.create("anonymous", "anonymous", 0);
      
      Topic topic = Topic.create();
      topic.setFreeReading();
      topic.setFreeWriting();

      AdminModule.disconnect();

      Connection cnx1 = cf.createConnection();
      cnx1.setClientID("cnx1");
      cnx1.start();
      
      Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons1= sess1.createDurableSubscriber(topic, "dursub1");

      Connection cnx2 = cf.createConnection();
      JMSException exc = null;
      try {
        cnx2.setClientID("cnx1");
        Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer cons2 = sess2.createDurableSubscriber(topic, "dursub1");
      } catch (JMSException e) {
        exc = e;
      }
      assertTrue(exc != null);

      sess1.close();
      cnx1.close();
      
      cnx2.setClientID("cnx1");
      Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons2 = sess2.createDurableSubscriber(topic, "dursub1");
      
      cnx2.close();

      cnx1 = cf.createConnection();
      cnx1.setClientID("cnx1");
      Session sess3 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons3 = sess3.createDurableSubscriber(topic, "dursub1");
    } catch(Throwable exc){
      exc.printStackTrace();
      error(exc);
    } finally{
      stopAgentServer((short)0);
      endTest(); 
    }
  }
}
