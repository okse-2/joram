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
 * Test : check consumThreshold property
 */
public class TestQ5 extends TestQBase {

  public static void main(String[] args) {
    new TestQ5().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);
      startAgentServer((short)1);
      startAgentServer((short)2);

      Properties prop = new Properties();
      prop.setProperty("period","60000");
      prop.setProperty("producThreshold","10");
      prop.setProperty("consumThreshold","1");
      prop.setProperty("autoEvalThreshold","false");
      prop.setProperty("waitAfterClusterReq","10");
      admin(prop);

      Context  ictx = new InitialContext();

      ConnectionFactory cf0 = (ConnectionFactory) ictx.lookup("cf0");
      ConnectionFactory cf1 = (ConnectionFactory) ictx.lookup("cf1");
      ConnectionFactory cf2 = (ConnectionFactory) ictx.lookup("cf2");
      
      Queue queue0 = (Queue) ictx.lookup("queue0");
      Queue queue1 = (Queue) ictx.lookup("queue1");
      Queue queue2 = (Queue) ictx.lookup("queue2");

      ictx.close();

      Connection cnx0 = cf0.createConnection("user", "pass");
      Session sess0 = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer0 = sess0.createProducer(queue0);
      cnx0.start();

      Connection cnx1 = cf1.createConnection("user", "pass");
      Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer1 = sess1.createProducer(queue1);
      cnx1.start();

      Connection cnx2a = cf2.createConnection("user", "pass");
      Session sess2a = cnx2a.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer recv2a = sess2a.createConsumer(queue2);
      MsgListenerCluster listener2a = new MsgListenerCluster("recv2a");
      recv2a.setMessageListener(listener2a);
      cnx2a.start();

      Connection cnx2b = cf2.createConnection("user", "pass");
      Session sess2b = cnx2b.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer recv2b = sess2b.createConsumer(queue2);
      MsgListenerCluster listener2b = new MsgListenerCluster("recv2b");
      recv2b.setMessageListener(listener2b);
      cnx2b.start();

      TextMessage msg = null;
      int i = 0;
      for (; i<5; i++) {
        msg = sess0.createTextMessage("Test number#0." + i);
        producer0.send(msg);

        msg = sess1.createTextMessage("Test number#1." + i);
        producer1.send(msg);
      }
      
      Thread.sleep(5000);
      
      int nbTry = 50;
      while (((listener2a.nbMsg + listener2b.nbMsg) == 0) && (nbTry-- > 0))
        Thread.sleep(100);

      System.out.println("listener2a/listener2b = " + listener2a.nbMsg + " / " + listener2b.nbMsg);
      assertTrue((listener2a.nbMsg + listener2b.nbMsg) != 0);

      cnx0.close();
      cnx1.close();
      cnx2a.close();
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
