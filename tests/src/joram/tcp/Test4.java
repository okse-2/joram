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
 *  Check that the onException method is called when the connection is closed
 * (closed by a server stop).
 *  The test is launched 2 times: with and without "hear beat" timer.
 */
public class Test4 extends TestCase {
  public Test4() {
    super();
  }

  public void run() {
    try {
      startAgentServer((short) 0);
      Thread.sleep(1000);

      AdminModule.connect("localhost", 2560, "root", "root", 10);

      User user = User.create("anonymous", "anonymous", 0);

      ConnectionFactory tcf = TcpConnectionFactory.create("localhost", 2560);
      
      Topic topic = Topic.create(0);
      topic.setFreeReading();
      topic.setFreeWriting();

      AdminModule.disconnect();
      
      doTest(tcf, topic);
      Thread.sleep(2000);
      
      new File("./s0/lock").delete();
      startAgentServer((short)0);
      Thread.sleep(1000);

      ((TcpConnectionFactory)tcf).getParameters().connectingTimer = 4;
      ((TcpConnectionFactory)tcf).getParameters().cnxPendingTimer = 500;
      
      doTest(tcf, topic);
    } catch (Exception exc) {
      stopAgentServer((short)0);
      error(exc);
    } finally {
      endTest();     
    }
  }

  private volatile JMSException expectedException;

  private void doTest(ConnectionFactory cf, Topic topic) throws Exception {
    expectedException = null;
    
    //  Topic pub/sub
    Connection cnx = cf.createConnection();
    Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer prod = session.createProducer(topic);
    MessageConsumer cons = session.createConsumer(topic);
    cnx.start();

    cnx.setExceptionListener(new ExceptionListener() {
      public void onException(JMSException exception) {
        System.out.println("onException(" + exception + ')');
        expectedException = exception;
      }
    });
   
    stopAgentServer((short)0);

    // Wait for the connection failure detection
    Thread.sleep(5000);
    if (expectedException == null)
      Thread.sleep(5000);
    
    assertTrue("onException not called with " + cf, 
        expectedException instanceof javax.jms.JMSException);
    
    prod.close();
    
    // Currently fails, bug ?
    // tsub.close();
    
    session.close();
    cnx.close(); 


  }

  public static void main(String args[]) {
    new Test4().run();
  }
}
