/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package joram.connection;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.AgentServer;

/**
 * Test the connection closing.
 */
public class Test1 extends BaseTest {
  ConnectionFactory cf;
  Connection cnx;
  Session session;
  MessageConsumer cons;
  
  public synchronized void close() {
    try {
      //  System.out.println("close");
      if (cnx != null) cnx.close();
      cnx = null;
    } catch (JMSException exc) {
      System.out.println("Error during close: " + exc.getMessage());
    }
  }
  
  public static void main (String args[]) throws Exception {
    new Test1().run();
  }
  
  public void run(){
    try{
      startServer();

      System.out.println("trace1");
      
      String baseclass = "joram.noreg.ColocatedBaseTest";
      baseclass = System.getProperty("BaseClass", baseclass);

      Thread.sleep(500L);

      AdminConnect(baseclass);
      User user = User.create("anonymous", "anonymous", 0);
      Queue queue = Queue.create();
      queue.setFreeReading();
      cf =  createConnectionFactory(baseclass);
      AdminModule.disconnect();

      System.out.println("trace2");

      cnx = cf.createConnection();
      
      session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cons = session.createConsumer(queue);
      
      cnx.start();
      
      Receiver receiver = new Receiver(cons);
      Thread thread = new Thread(receiver);
      thread.start();
      
      Thread.sleep(5000L);
      
      long start = System.currentTimeMillis();
      System.out.println("before close");
      close();
      long end = System.currentTimeMillis();
      System.out.println("after close: " + ((end-start)/1000L));
      
      assertTrue((end - start) < 30000L);
      assertEquals(receiver.msg, null);
    } catch(Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();
    }
  }
}

class Receiver implements Runnable {
  MessageConsumer cons = null;
  Message msg = null;
  
  Receiver(MessageConsumer cons) {
    this.cons = cons;
  }
  
  public void run() {
    try {
      System.out.println("before receive");
      msg = cons.receive(120000L);
    } catch (JMSException exc) {
      exc.printStackTrace();
    }
    System.out.println("Receives: " + msg);
  }
}
