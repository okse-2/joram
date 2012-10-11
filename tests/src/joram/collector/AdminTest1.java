/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2008 - 2010 ScalAgent Distributed Technologies
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
package joram.collector;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Tests receive the LGPL license using the CollectorQueue.
 */
public class AdminTest1 extends TestCase implements MessageListener {

  private int nbReceived;

  public static void main(String[] args) {
    new AdminTest1().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0);
      Thread.sleep(1000);
      
      AdminModule.executeXMLAdmin("joramAdmin.xml");
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      Topic topic = (Topic) ictx.lookup("topic");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session session1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons1 = session1.createConsumer(queue);
      MessageProducer prod1 = session1.createProducer(queue);
      Session session2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons2 = session2.createConsumer(topic);
      cons2.setMessageListener(this);
      cnx.start();
      Thread.sleep(12000);
      
      assertTrue(nbReceived >= 2);
      Message msg1 = cons1.receiveNoWait();
      assertTrue(msg1 == null);
      
      Message msg = session1.createMessage();
      prod1.send(msg);
      Message msg2 = cons1.receive(1000L);
      assertTrue(msg2 != null);
      
      cnx.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();
    }
  }

  public void onMessage(Message message) {
    nbReceived++;
    try {
      assertTrue("http://www.gnu.org/licenses/lgpl.txt".equals(message.getStringProperty("collector.url")));
    } catch (JMSException exc) {
      addError(exc);
    }
  }
}
