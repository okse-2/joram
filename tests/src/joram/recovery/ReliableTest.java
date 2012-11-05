/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent D.T.
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */

package joram.recovery;


import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Test reliability of the optimized version.: fail during receive
 */
public class ReliableTest extends TestCase {

  public static final int LOOP_NB = 10;

  public static void main(String[] args) {
    new ReliableTest().run();
  }

  private Destination dest;

  private ConnectionFactory cf;

  private Connection connection;

  private Session session;

  private MessageProducer producer;

  private MessageConsumer consumer;

  private void startServer() throws Exception {
      //System.out.println("start server");

    startAgentServer(
        (short)0, (File)null, 
        new String[]{
      "-DTransaction=fr.dyade.aaa.util.NTransaction"});

    Thread.sleep(2000);
  }

  private void connect() throws JMSException {
      //System.out.println("connect");

    connection = cf.createConnection(
       "anonymous", "anonymous"); 

    session = connection.createSession(
      false,
      Session.CLIENT_ACKNOWLEDGE);
    
    producer = session.createProducer(dest);

    consumer = session.createConsumer(dest);

    connection.start();
  }

  public void run() {
    try {
      startServer();

      AdminModule.connect("localhost", 2560,
                          "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create(
          "anonymous", "anonymous", 0);

      org.objectweb.joram.client.jms.Queue queue = 
        org.objectweb.joram.client.jms.Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();

      dest = queue;

      cf = org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
        "localhost", 2560);
                 
       connect();
      
       TextMessage msg2 = session.createTextMessage("test2");
       producer.send(msg2);
      
       new Thread() {
         public void run() {
           try {
             Thread.sleep(500);
           } catch (Exception exc) {}
          stopAgentServer((short)0);
         }
       }.start();
      
       msg2 = (TextMessage)consumer.receive();
            
       assertEquals("test2",msg2.getText());
       Thread.sleep(5000);
      
       startServer();
      
       connect();
      
       msg2 = (TextMessage)consumer.receive();
       assertEquals("test2",msg2.getText());
       msg2.acknowledge();
       // System.out.println("msg2 = " + msg2);
       //System.out.println("close");
       connection.close();

     

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}

