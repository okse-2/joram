/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2009 ScalAgent Distributed Technologies
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
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Test : check use of cluster administered objects.
 */
public class TestQ6 extends TestQBase {

  public static void main(String[] args) {
    new TestQ6().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);
      startAgentServer((short)1);
      startAgentServer((short)2);

      Properties prop = new Properties();
      prop.setProperty("period","100");
      prop.setProperty("producThreshold","10");
      prop.setProperty("consumThreshold","2");
      prop.setProperty("autoEvalThreshold","true");
      prop.setProperty("waitAfterClusterReq","100");
      admin(prop);

      Context  ictx = new InitialContext();
      
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("clusterCF");
      Queue queue = (Queue) ictx.lookup("clusterQueue");

      ictx.close();

      Connection cnx0 = cf.createConnection("user", "pass");
      Session sess0 = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = sess0.createProducer(queue);
      cnx0.start();
      
      Connection cnx1 = cf.createConnection("user", "pass");
      Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer recv = sess1.createConsumer(queue);
      MsgListenerCluster listener = new MsgListenerCluster("recv");
      recv.setMessageListener(listener);
      cnx1.start();

      TextMessage msg = null;
      int i = 0;
      for (; i<50; i++) {
        msg = sess0.createTextMessage("Test number#0." + i);
        producer.send(msg);
      }
      
      int nbTry = 50;
      while ((listener.nbMsg != i) && (nbTry-- > 0))
        Thread.sleep(100);

      assertTrue(listener.nbMsg == i);

      // It could be useful to verify the coherence between the servers used
      // for connection and the server where the sending and receiving queue
      // are located.
      
      cnx0.close();
      cnx1.close();
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
