/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2013 ScalAgent Distributed Technologies
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
package joram.alias;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.DeliveryMode;
//import javax.naming.Context;
//import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test : Use an alias queue with static repartition of messages between two
 * remote destinations. Verify the repartition.
 */
public class AliasInQueueTest1 extends TestCase {

  public static void main(String[] args) {
    new AliasInQueueTest1().run();
  }
  
  int weight1 = 2;
  int weigth2 = 3;
  int nbmsg = 10000;

  public void run() {
    try {
      startAgentServer((short) 0, new String[] { "-DTransaction.UseLockFile=false" });
      startAgentServer((short) 1, new String[] { "-DTransaction.UseLockFile=false" });
      startAgentServer((short) 2, new String[] { "-DTransaction.UseLockFile=false" });
      Thread.sleep(1000L);

      AdminModule.connect("localhost", 16010, "root", "root", 60);

      // Creating access for user anonymous on servers 0, 1 and 2
      User.create("anonymous", "anonymous", 0);
      User.create("anonymous", "anonymous", 1);
      User.create("anonymous", "anonymous", 2);
 
      // Creating the destination on server 1 and 2
      Queue queue1 = Queue.create(1);
      queue1.setFreeWriting();
      queue1.setFreeReading();
      Queue queue2 = Queue.create(2);
      queue2.setFreeWriting();
      queue2.setFreeReading();

      // Create an alias queue on server 0 linking to queue1 and queue2
      Properties props = new Properties();
      props.setProperty("remoteAgentID", queue1.getName() + ';' + queue2.getName());
      Queue aliasQ = Queue.create(0, "org.objectweb.joram.mom.dest.AliasInQueue", props);
      aliasQ.setFreeWriting();
      aliasQ.sendDestinationsWeights(new int[] {weight1, weigth2});
      
      // Creating the connection factories for connecting to the servers 0 and 2:
      ConnectionFactory cf0 = TcpConnectionFactory.create("localhost", 16010);

      ConnectionFactory cf1 = TcpConnectionFactory.create("localhost", 16011);
      ((org.objectweb.joram.client.jms.ConnectionFactory) cf1).getParameters().queueMessageReadMax = 10;
      ((org.objectweb.joram.client.jms.ConnectionFactory) cf1).getParameters().implicitAck = true;

      ConnectionFactory cf2 = TcpConnectionFactory.create("localhost", 16012);
      ((org.objectweb.joram.client.jms.ConnectionFactory) cf2).getParameters().queueMessageReadMax = 10;
      ((org.objectweb.joram.client.jms.ConnectionFactory) cf2).getParameters().implicitAck = true;

//      // Binding the objects in JNDI:
//      javax.naming.Context jndiCtx = new javax.naming.InitialContext();
//      jndiCtx.bind("aliasQ", queue0);
//      jndiCtx.bind("queue1", queue1);
//      jndiCtx.bind("queue2", queue1);
//      jndiCtx.bind("cf0", cf0);
//      jndiCtx.close();

      AdminModule.disconnect();
      System.out.println("Admin closed.");
      Thread.sleep(1000L);

//      Context ictx = new InitialContext();
//      Queue aliasQ = (Queue) ictx.lookup("aliasQ");
//      Queue queue1 = (Queue) ictx.lookup("queue1");
//      Queue queue2 = (Queue) ictx.lookup("queue1");
//      ConnectionFactory cf0 = (ConnectionFactory) ictx.lookup("cf0");
//      ictx.close();

      Connection cnx0 = cf0.createConnection();
      Session sessionP = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = sessionP.createProducer(aliasQ);
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
      cnx0.start();
      
      Connection cnx1 = cf1.createConnection();
      Session sessionC1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer1 = sessionC1.createConsumer(queue1);
      Listener list1 = new Listener("queue1");
      consumer1.setMessageListener(list1);
      cnx1.start();
      
      Connection cnx2 = cf2.createConnection();
      Session sessionC2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer2 = sessionC2.createConsumer(queue2);
      Listener list2 = new Listener("queue2");
      consumer2.setMessageListener(list2);
      cnx2.start();

      // create a producer
      for (int i = 0; i < nbmsg; i++) {
        TextMessage msg = sessionP.createTextMessage();
        msg.setText("Message#" + i);
        producer.send(msg);
//        Thread.sleep(5L);
      }
      cnx0.close();

      int nbtry = 0;
      while (((list1.count + list2.count) != nbmsg) && (nbtry < 30)) {
        Thread.sleep(1000L); nbtry += 1;
      }

      if ((list1.count + list2.count) != nbmsg)
      	Thread.sleep(2000L);
      
      assertEquals(nbmsg, list1.count + list2.count);
      assertEquals((weight1 * nbmsg)/(weight1 + weigth2), list1.count);
      assertEquals((weigth2 * nbmsg)/(weight1 + weigth2), list2.count);

      System.out.println("queue1: " + list1.count + ", queue2: " + list2.count);
      cnx1.close();      
      cnx2.close();      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      killAgentServer((short) 0);
      killAgentServer((short) 1);
      killAgentServer((short) 2);
      endTest();
    }
  }

  class Listener implements MessageListener {
    String name = null;
    int count = 0;

    Listener(String name) {
      this.name = name;
    }

    public void onMessage(Message msg) {
      try {
        if ((count%1000)== 999) System.out.println(name + ':' + (count+1));
        count += 1;
      } catch (Exception exc) {
        exc.printStackTrace();
      }
    }
  }
}
