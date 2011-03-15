/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2007 ScalAgent Distributed Technologies
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
package joram.client;

import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;


/**
 * Inspired from a bug raised by Nortel.
 * 
 * Launch a producer (loop).
 * Stop the server during the producer's loop.
 * 
 * The producer must be interrupted.
 * The connection is closed by the demultiplexer
 * daemon.
 * A second close is explicitely done to check
 * that the test doesn't hang (a hanging producer
 * is not detected by the test).
 * 
 * 
 * @author feliot
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConnectionClose3 extends TestCase {
  
  public final static int NB_LOOP = 2;
  
  public static void main(String[] args) {
    new ConnectionClose3().run();
  }
  
  private Connection connection;

  private Destination dest;
  
  private Session session;
  
  private MessageProducer producer;
  
  private MessageConsumer consumer;
  
  public void run() {
    try {
      doTest(false);
      doTest(true);   
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      endTest();     
    }
  }
  
  private void doTest(boolean heartBeat) throws Exception {
    for (int i = 0; i < NB_LOOP; i++) {
      startAgentServer();
      connect(heartBeat);

      new Thread() {
        public void run() {
          while (true) {
            try {
              TextMessage msg = session.createTextMessage("hello");
              producer.send(msg);
            } catch (Exception exc) {
		//System.out.println("Expected exception: " + exc);
              assertTrue("unexpected exception: " + exc,
                  exc instanceof javax.jms.JMSException);
              break;
            }
          }
        }
      }.start();

      Thread.sleep(500);

      // System.out.println("Stop #" + i);
      stopAgentServer();
      
      //System.out.println("Close connection");
      connection.close();
    }
  }
  
  private void startAgentServer() throws Exception {
      //System.out.println("Start agent server");
    startAgentServer(
        (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
  }
  
  private void connect(boolean heartBeat) throws Exception {
      //System.out.println("Connect");
    AdminModule.connect("localhost", 2560, "root", "root", 60);

    //System.out.println("create user");
    org.objectweb.joram.client.jms.admin.User user = 
      org.objectweb.joram.client.jms.admin.User
        .create("anonymous", "anonymous", 0);

    //System.out.println("create scheduler queue");
    org.objectweb.joram.client.jms.Queue queue = 
      org.objectweb.joram.client.jms.Queue.create(0);
    queue.setFreeReading();
    queue.setFreeWriting();
    
    AdminModule.disconnect();

    dest = queue;
    
    ConnectionFactory cf = 
      org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
        "localhost", 2560);
    
    if (heartBeat) {
      ((org.objectweb.joram.client.jms.tcp.TcpConnectionFactory) cf)
          .getParameters().cnxPendingTimer = 500;
    }
    
    //System.out.println("create connection");
    connection = cf.createConnection(
        "anonymous", "anonymous");
    
    connection.start();
    
    session = connection.createSession(
      false,
      Session.AUTO_ACKNOWLEDGE);
    
    producer = session.createProducer(dest);
    consumer = session.createConsumer(dest);
    
    //System.out.println("connected");
  }
  
  private void stopAgentServer() throws Exception {
    stopAgentServer((short)0);
    Thread.sleep(5000);
    new File("./s0/lock").delete();
  }
}
