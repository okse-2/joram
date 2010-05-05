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
 * Test :check producThreshold property
 */
public class TestQ4 extends TestQBase {

  public static void main(String[] args) {
    new TestQ4().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);
      startAgentServer((short)1);
      startAgentServer((short)2);

      Properties prop = new Properties();
      prop.setProperty("period","60000");
      prop.setProperty("producThreshold","5");
      prop.setProperty("consumThreshold","2");
      prop.setProperty("autoEvalThreshold","false");
      prop.setProperty("waitAfterClusterReq","100");
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
      Session sess0b = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer0 = sess0b.createProducer(queue0);
      cnx0.start();

      Connection cnx1 = cf1.createConnection("user", "pass");
      Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer recv1 = sess1.createConsumer(queue1);
      MsgListenerCluster listener1 = new MsgListenerCluster("recv1");
      recv1.setMessageListener(listener1);
      cnx1.start();

      Connection cnx2 = cf2.createConnection("user", "pass");
      Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer recv2 = sess2.createConsumer(queue2);
      MsgListenerCluster listener2 = new MsgListenerCluster("recv2");
      recv2.setMessageListener(listener2);
      cnx2.start();

      TextMessage msg = null;
      int i = 0;
      for (; i<6; i++) {
        msg = sess0b.createTextMessage("Test number#0." + i);
        producer0.send(msg);
      }
      
      Thread.sleep(5000);
      
      int nbTry = 50;
      while (((listener1.nbMsg + listener2.nbMsg) == 0) && (nbTry-- > 0))
        Thread.sleep(100);

      System.out.println("listener1/listener2 = " + listener1.nbMsg + " / " + listener2.nbMsg);
      assertTrue((listener1.nbMsg + listener2.nbMsg) > 0);

      cnx0.close();
      cnx1.close();
      cnx2.close();
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