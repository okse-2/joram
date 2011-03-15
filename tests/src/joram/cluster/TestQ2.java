/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 - 2009 ScalAgent Distributed Technologies
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
package joram.cluster;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;

/**
 * Configure a producer on the 3 queues of the cluster and a consumer only on the first 2 queues.
 * Verify that each message sent is received (message on the third queues are forwarded to the others).
 */
public class TestQ2 extends TestQBase {

  public static void main(String[] args) {
    new TestQ2().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);
      startAgentServer((short)1);
      startAgentServer((short)2);

      Properties prop = new Properties();
      prop.setProperty("period","1000");
      prop.setProperty("producThreshold","5");
      prop.setProperty("consumThreshold","2");
      prop.setProperty("autoEvalThreshold","false");
      prop.setProperty("waitAfterClusterReq","100");
      admin(prop);

      Context  ictx = new InitialContext();

      Queue queue0 = (Queue) ictx.lookup("queue0");
      Queue queue1 = (Queue) ictx.lookup("queue1");
      Queue queue2 = (Queue) ictx.lookup("queue2");

      ConnectionFactory cf0 = (ConnectionFactory) ictx.lookup("cf0");
      ConnectionFactory cf1 = (ConnectionFactory) ictx.lookup("cf1");
      ConnectionFactory cf2 = (ConnectionFactory) ictx.lookup("cf2");

      ictx.close();

      Connection cnx0a = cf0.createConnection("user", "pass");
      Session sess0 = cnx0a.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer recv0 = sess0.createConsumer(queue0);
      MsgListenerCluster listener0 = new MsgListenerCluster("recv0");
      recv0.setMessageListener(listener0);
      cnx0a.start();

      Connection cnx1a = cf1.createConnection("user", "pass");
      Session sess1 = cnx1a.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer recv1 = sess1.createConsumer(queue1);
      MsgListenerCluster listener1 = new MsgListenerCluster("recv1");
      recv1.setMessageListener(listener1);
      cnx1a.start();

      Connection cnx0b = cf0.createConnection("user", "pass");
      Session sess0b = cnx0b.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer0 = sess0b.createProducer(queue0);
      cnx0b.start();

      Connection cnx1b = cf1.createConnection("user", "pass");
      Session sess1b = cnx1b.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer1 = sess1b.createProducer(queue1);
      cnx1b.start();

      Connection cnx2b = cf2.createConnection("user", "pass");
      Session sess2b = cnx2b.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer2 = sess2b.createProducer(queue2);
      cnx2b.start();

      TextMessage msg = null;
      int i = 0;
      for (; i<100; i++) {
        msg = sess0b.createTextMessage("Test number#0." + i);
        producer0.send(msg);

        msg = sess1b.createTextMessage("Test number#1." + i);
        producer1.send(msg);
        
        msg = sess2b.createTextMessage("Test number#2." + i);
        producer2.send(msg);
      }

      int nbTry = 50;
      while (((listener0.nbMsg + listener1.nbMsg) != (3*i)) && (nbTry-- > 0))
        Thread.sleep(100);

      System.out.println("listener0/listener1 = " + listener0.nbMsg + " / " + listener1.nbMsg);
      assertTrue((listener0.nbMsg + listener1.nbMsg) == (3*i));

      cnx0a.close();
      cnx1a.close();
      cnx0b.close();
      cnx1b.close();
      cnx2b.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      stopAgentServer((short)1);
      stopAgentServer((short)2);
      endTest(); 
    }
  }

  class MsgListenerCluster implements MessageListener {
    String ident = null;
    public int nbMsg;

    public MsgListenerCluster(String ident) {
      nbMsg=0;
      this.ident = ident;
    }

    public void onMessage(Message msg) {
      nbMsg++;
    }
  }
}
