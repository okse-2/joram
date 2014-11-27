/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): Freyssinet Andre (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.noreg;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.shared.MessageErrorConstants;

import fr.dyade.aaa.agent.AgentServer;

/**
 * test use TTL
 *
 */
public class Test31 extends framework.TestCase implements MessageListener{
  public static void main (String args[]) throws Exception {
      new Test31().run();
  }
  public void run(){
    Connection cnx = null;
    try {
      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();

      Thread.sleep(1000L);

      AdminModule.connect("localhost", 16010, "root", "root", 60);

      DeadMQueue dmq = (DeadMQueue) DeadMQueue.create(0);
      dmq.setFreeReading();
      AdminModule.setDefaultDMQ(0, dmq);

      Properties prop = new Properties();
      prop.setProperty("period", "1000");
      Queue dest = Queue.create(0, prop);
      dest.setFreeReading();
      dest.setFreeWriting();
      User user = User.create("anonymous", "anonymous", 0);

      ConnectionFactory cf =  LocalConnectionFactory.create();

      cnx = cf.createConnection();

      Session session1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons = session1.createConsumer(dest);
      MessageProducer prod = session1.createProducer(dest);

      Session session2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons2 = session2.createConsumer(dmq);
      cons2.setMessageListener(this);

      cnx.start();

      Message msg = null;

      //System.out.println("sends msg#1 (ttl=1s): " + new Date());
      prod.setTimeToLive(1000L);
      msg = session1.createMessage();
      msg.setIntProperty("index", 1);
      prod.send(msg);

      Thread.sleep(5000L);

      //System.out.println("sends msg#2 (ttl=60s): " + new Date());
      prod.setTimeToLive(60000L);
      msg = session1.createMessage();
      msg.setIntProperty("index", 2);
      prod.send(msg);

      //System.out.println("sends msg#3 (ttl=1s): " + new Date());
      prod.setTimeToLive(1000L);
      msg = session1.createMessage();
      msg.setIntProperty("index", 3);
      prod.send(msg);

      Thread.sleep(5000L);
      //System.out.println("Messages in queue: " + dest.getPendingMessages() + ", " + new Date());
      assertEquals(1, dest.getPendingMessages());

      //System.out.println("sends msg#4 (ttl=1s): " + new Date());
      prod.setTimeToLive(1000L);
      msg = session1.createMessage();
      msg.setIntProperty("index", 4);
      prod.send(msg);

      Thread.sleep(5000L);

      msg = cons.receive();
      int index = msg.getIntProperty("index");
      //System.out.println("Receives message #" + index + " (2), " + new Date());
      assertEquals(2,index);

      AdminModule.disconnect();
      cnx.close();
    } catch(Throwable exc){
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();
    }
  }

  public synchronized void onMessage(Message msg) {
    try {
      assertEquals(1, msg.getIntProperty("JMS_JORAM_ERRORCOUNT"));
      assertEquals(MessageErrorConstants.EXPIRED, msg.getIntProperty("JMS_JORAM_ERRORCODE_1"));
    } catch (JMSException exc) {
      exc.printStackTrace();
    }
  }
}
