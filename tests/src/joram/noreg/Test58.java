/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package joram.noreg;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TopicSubscriber;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import framework.TestCase;


/**
 * Try to build producers and consumers with bad queue or topic.
 * The client should get an exception and the server should continue to work.
 */
public class Test58 extends BaseTest {
  public static void main(String[] args) throws Exception {
    new Test58().run(args);
  }

  public void run(String[] args) {
    try{
      writeIntoFile("===================== start test 58 =====================");

      TestCase.startAgentServer((short)0);
      Thread.sleep(2000);

      ConnectionFactory cf = TcpBaseTest.createConnectionFactory();
      AdminModule.connect(cf);
      User.create("anonymous", "anonymous", 0);
      AdminModule.disconnect();

      Connection cnx = cf.createConnection();
      Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

      Topic topic1 = new Topic();
      try {
        MessageConsumer cons = sess.createConsumer(topic1);
        assertTrue("unreachable", false);
      } catch (JMSException exc) {
        assertTrue("Bad exception: " + exc, exc instanceof InvalidDestinationException);
        assertTrue("Bad exception's message: " + exc.getMessage(),
                   exc.getMessage().equals("Undefined (null) destination identifier."));
      }
      
      try {
        MessageProducer prod = sess.createProducer(topic1);
        assertTrue("unreachable", false);
      } catch (JMSException exc) {
        assertTrue("Bad exception: " + exc, exc instanceof InvalidDestinationException);
        assertTrue("Bad exception's message: " + exc.getMessage(),
                   exc.getMessage().equals("Undefined (null) destination identifier."));
      }
      
      try {
        TopicSubscriber sub = sess.createDurableSubscriber(topic1, "sub");
        assertTrue("unreachable", false);
      } catch (JMSException exc) {
        assertTrue("Bad exception: " + exc, exc instanceof InvalidDestinationException);
        assertTrue("Bad exception's message: " + exc.getMessage(),
                   exc.getMessage().equals("Undefined (null) destination identifier."));
      }

      Topic topic2 = new Topic("#0.0-1025");
      try {
        MessageConsumer cons = sess.createConsumer(topic2);
        assertTrue("unreachable", false);
      } catch (JMSException exc) {
        assertTrue("Bad exception: " + exc, exc instanceof InvalidDestinationException);
        assertTrue("Bad exception's message: " + exc.getMessage(),
                   exc.getMessage().startsWith("Bad destination identifier:"));
      }
      
      try {
        MessageProducer prod = sess.createProducer(topic2);
        assertTrue("unreachable", false);
      } catch (JMSException exc) {
        assertTrue("Bad exception: " + exc, exc instanceof InvalidDestinationException);
        assertTrue("Bad exception's message: " + exc.getMessage(),
                   exc.getMessage().startsWith("Bad destination identifier:"));
      }
      
      try {
        TopicSubscriber sub = sess.createDurableSubscriber(topic2, "sub");
        assertTrue("unreachable", false);
      } catch (JMSException exc) {
        assertTrue("Bad exception: " + exc, exc instanceof InvalidDestinationException);
        assertTrue("Bad exception's message: " + exc.getMessage(),
                   exc.getMessage().startsWith("Bad destination identifier:"));
      }

      Queue queue1 = new Queue();
      try {
        MessageConsumer cons = sess.createConsumer(queue1);
        assertTrue("unreachable", false);
      } catch (JMSException exc) {
        assertTrue("Bad exception: " + exc, exc instanceof InvalidDestinationException);
        assertTrue("Bad exception's message: " + exc.getMessage(),
                   exc.getMessage().equals("Undefined (null) destination identifier."));
      }
      
      try {
        MessageProducer prod = sess.createProducer(queue1);
        assertTrue("unreachable", false);
      } catch (JMSException exc) {
        assertTrue("Bad exception: " + exc, exc instanceof InvalidDestinationException);
        assertTrue("Bad exception's message: " + exc.getMessage(),
                   exc.getMessage().equals("Undefined (null) destination identifier."));
      }
      
      try {
        QueueBrowser browser = sess.createBrowser(queue1);
        assertTrue("unreachable", false);
      } catch (JMSException exc) {
        assertTrue("Bad exception: " + exc, exc instanceof InvalidDestinationException);
        assertTrue("Bad exception's message: " + exc.getMessage(),
                   exc.getMessage().equals("Undefined (null) destination identifier."));
      }

      Queue queue2 = new Queue("#0.0-1025");
      try {
        MessageConsumer cons = sess.createConsumer(queue2);
        assertTrue("unreachable", false);
      } catch (JMSException exc) {
        assertTrue("Bad exception: " + exc, exc instanceof InvalidDestinationException);
        assertTrue("Bad exception's message: " + exc.getMessage(),
                   exc.getMessage().startsWith("Bad destination identifier:"));
      }
      
      try {
        MessageProducer prod = sess.createProducer(queue2);
        assertTrue("unreachable", false);
      } catch (JMSException exc) {
        assertTrue("Bad exception: " + exc, exc instanceof InvalidDestinationException);
        assertTrue("Bad exception's message: " + exc.getMessage(),
                   exc.getMessage().startsWith("Bad destination identifier:"));
      }
      
      try {
        QueueBrowser browser = sess.createBrowser(queue2);
        assertTrue("unreachable", false);
      } catch (JMSException exc) {
        assertTrue("Bad exception: " + exc, exc instanceof InvalidDestinationException);
        assertTrue("Bad exception's message: " + exc.getMessage(),
                   exc.getMessage().startsWith("Bad destination identifier:"));
      }
   
      cnx.start();
            
      sess.close();
      cnx.close();
    } catch (JMSException exc) {
      exc.printStackTrace();
      assertTrue("Bad exception message",
                 exc.getMessage().equals("Cannot subscribe to an undefined topic (null)."));
    } catch(Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      TestCase.stopAgentServer((short)0);
      endTest();
    }

    System.exit(0);
  }

}
