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

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
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
 * Test threshold parameter and test DMQ with message becoming bigger and bigger
 */
public class Test46 extends BaseTest {
  public static void main (String args[]) {
    new Test46().run();
  }
  public void run(){
    try {
      AgentServer.init((short) 0, "./s0", null);
      AgentServer.start();
      Thread.sleep(1000L);

      AdminModule.connect("root", "root", 60);
      User.create("anonymous", "anonymous");
      Queue queue = Queue.create(0);
      DeadMQueue dmq = (DeadMQueue) DeadMQueue.create(0);
      dmq.setFreeReading();
      AdminModule.setDefaultDMQ(0, dmq);
      queue.setFreeReading();
      queue.setFreeWriting();
      queue.setThreshold(2);


      AdminModule.disconnect();
      ConnectionFactory cf =  LocalConnectionFactory.create();

      Connection cnx = cf.createConnection();

      Session sess1 = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons1 = sess1.createConsumer(queue);
      MessageProducer prod1 = sess1.createProducer(queue);

      Session sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons2 = sess2.createConsumer(dmq);
      //       cons2.setMessageListener(new MsgList46());

      cnx.start();

      for (int i=10; i<24; i++) {
        BytesMessage msg = sess1.createBytesMessage();
        msg.setIntProperty("Index", i);
        byte[] content = new byte[2<<i];

        msg.writeBytes(content);
        prod1.send( msg);
        sess1.commit();
        System.out.println("send msg#" + i + " size=" + content.length);

        msg = (BytesMessage) cons1.receive();
        int index = msg.getIntProperty("Index");
        assertEquals(i,index);
        sess1.rollback();
        //System.out.println("rollback msg#" + index);

        msg = (BytesMessage) cons1.receive();
        index = msg.getIntProperty("Index");
        assertEquals(i,index);
        sess1.rollback();
        //System.out.println("rollback msg#" + index);

        msg = (BytesMessage) cons2.receive();
        assertEquals(i,index);
        index = msg.getIntProperty("Index");

        assertEquals(1, msg.getIntProperty("JMS_JORAM_ERRORCOUNT"));
        assertEquals(MessageErrorConstants.UNDELIVERABLE, msg.getIntProperty("JMS_JORAM_ERRORCODE_1"));

        System.out.println("msg#" + index + ", " +
                           msg.getStringProperty("JMS_JORAM_ERRORCAUSE_1") + ", " +
                           msg.getIntProperty("JMSXDeliveryCount"));

        assertEquals(3, msg.getIntProperty("JMSXDeliveryCount"));
      }

      sess1.close();
      sess2.close();
      cnx.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally{
      AgentServer.stop();
      endTest();
    }
  }
}
