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
 * Initial developer(s): Freyssinet Andre (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.noreg;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.BaseTestCase;

class ExcList26 implements ExceptionListener {

  private String name = null;
  public boolean excOccurred = false;

  ExcList26(String name) {
    this.name = name;
  }

  public void onException(JMSException exc) {
    System.err.println(name + ": " + exc.getMessage());
    BaseTestCase.assertTrue(exc instanceof javax.jms.IllegalStateException);
    excOccurred = true;
  }
}

class MsgList26 implements MessageListener {
  public void onMessage(Message msg) {
    try {
      System.out.println("onMessage");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}

/**
 *
 */
public class Test26 extends BaseTest {
  static ConnectionFactory cf;
  static Destination dest;
  static Connection cnx1;

  public static void main(String args[]) throws Exception {
    new Test26().run();
  }

  public void run() {
    try {
      startServer();
      Thread.sleep(500L);

      cf = TcpConnectionFactory.create("localhost", 16010);
      AdminModule.connect(cf);

      User.create("anonymous", "anonymous", 0);
      dest = Queue.create();
      dest.setFreeReading();
      dest.setFreeWriting();

      FactoryParameters fp = ((org.objectweb.joram.client.jms.ConnectionFactory) cf).getParameters();
      fp.cnxPendingTimer = 5000;
      fp.connectingTimer = 60;
      AdminModule.disconnect();

      Connection cnx = cf.createConnection();
      ExcList26 excList = new ExcList26("Test26.ExcLst");
      cnx.setExceptionListener(excList);
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer = session.createConsumer(dest);
      consumer.setMessageListener(new MsgList26());
      cnx.start();

      Thread.sleep(1000L);
      AgentServer.stop();
      Thread.sleep(5000L);
      assertFalse(excList.excOccurred);
      
      // Verify we don't hang connectingTimer delay on close
      long time = System.currentTimeMillis();
      cnx.close();
      assertTrue((System.currentTimeMillis() - time) < ((fp.connectingTimer *1000) + (2* fp.cnxPendingTimer)));
      Thread.sleep(2000L);
      assertTrue(excList.excOccurred);
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {

      endTest();
    }
  }
}
