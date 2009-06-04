/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): Feliot David  (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.tcp;

import java.io.File;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.IllegalStateException;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;

import framework.TestCase;

/**
 *  Check that a receive on a closed connection (closed by a server stop)
 * raises an IllegalStateException.
 *  The test is launched 2 times with and without "hear beat" timer.
 */
public class Test3 extends TestCase {
  public Test3() {
    super();
  }

  public void run() {
    try {
      startAgentServer((short)0);
      Thread.sleep(1000);

      AdminModule.connect("localhost", 2560, "root", "root", 10);

      User user = User.create("anonymous", "anonymous", 0);

      ConnectionFactory qcf = TcpConnectionFactory.create("localhost", 2560);

      Queue queue = Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();
 
      AdminModule.disconnect();
     
      doTest(qcf, queue);
      Thread.sleep(2000);

      new File("./s0/lock").delete();
      startAgentServer((short)0);
      Thread.sleep(1000);

      ((TcpConnectionFactory)qcf).getParameters().connectingTimer = 5;
      ((TcpConnectionFactory)qcf).getParameters().cnxPendingTimer = 500;

      doTest(qcf, queue);
    } catch (Exception exc) {
      error(exc);
    } finally {
      endTest();     
    }
  }
  
  private void doTest(ConnectionFactory cf, Queue queue) throws Exception {
    Connection cnx = cf.createConnection();
    Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer prod = session.createProducer(queue);
    MessageConsumer cons = session.createConsumer(queue);
    cnx.start();

    TextMessage msg = session.createTextMessage();
    msg.setText("Test");
    prod.send(msg);

    stopAgentServer((short)0);

    // Avoids a bug: synchro between connection
    // error and further receive.
    Thread.sleep(10000);

    IllegalStateException ise = null;
    try {
      msg = (TextMessage) cons.receive();
    } catch (IllegalStateException exc) {
      ise = exc;
    }
    
    assertTrue("Expected IllegalStateException not thrown", ise != null);
  }

  public static void main(String args[]) {
    new Test3().run();
  }
}
