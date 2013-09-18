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
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Test durable subscription:
 * - Creates a durable subscriber with name "dursub1" then close the session
 * of the active subscriber.
 * - Sends a message to the topic.
 * - Creates a second durable subscription.
 * - Verify that the message is received only on the first subscription.
 * - Sends a second message and verify that it is received on the 2 subscriptions.
 */
public class Test4 extends framework.TestCase {
  public static void main(String[] args) throws Exception {
    new Test4().run(args);
  }

  public void run(String[] args) throws Exception {
    try{
      startAgentServer((short)0);
      Thread.sleep(1000L);

      ConnectionFactory cf =  TcpConnectionFactory.create("localhost", 2560);
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 10;
      ((TcpConnectionFactory) cf).getParameters().clientID = "Test4";
      AdminModule.connect(cf, "root", "root");

      User.create("anonymous", "anonymous", 0);
      
      Topic topic = Topic.create();
      topic.setFreeReading();
      topic.setFreeWriting();

      AdminModule.disconnect();

      Connection cnx1 = cf.createConnection();
      cnx1.start();
      
      Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons1= sess1.createDurableSubscriber(topic, "dursub1");
      sess1.close();
      
      Session sess2 = cnx1.createSession(false,Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod = sess2.createProducer(topic);
      Message msg = sess2.createMessage();
      prod.send(msg);

      Session sess3 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons3 = sess3.createDurableSubscriber(topic, "dursub2");

      sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cons1= sess1.createDurableSubscriber(topic, "dursub1");
      
      Message msg1 = cons1.receive(5000L);
      assertTrue(msg1 != null);
      if (msg1 != null)
        assertEquals(msg.getJMSMessageID(), msg1.getJMSMessageID());
      
      Message msg2 = cons1.receiveNoWait();
      assertTrue(msg2 == null);

      msg = sess2.createMessage();
      prod.send(msg);

      msg1 = cons1.receive(5000L);
      assertTrue(msg1 != null);
      if (msg1 != null)
        assertEquals(msg.getJMSMessageID(), msg1.getJMSMessageID());

      msg2 = cons3.receive(5000L);
      assertTrue(msg2 != null);
      if (msg2 != null)
        assertEquals(msg.getJMSMessageID(), msg2.getJMSMessageID());
      
      cnx1.close();
    } catch(Throwable exc){
      exc.printStackTrace();
      error(exc);
    } finally{
      stopAgentServer((short)0);
      endTest(); 
    }
  }
}
