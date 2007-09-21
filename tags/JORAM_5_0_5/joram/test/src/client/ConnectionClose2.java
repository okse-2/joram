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
package client;

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
 * @author feliot
 *
 *
 */
public class ConnectionClose2 extends TestCase {
  
  public final static int NB_LOOP = 10;
  
  public static void main(String[] args) {
    new ConnectionClose2().run();
  }
  
  private Connection connection;

  private Destination dest;
  
  private Session session;
  
  private MessageProducer producer;
  
  private MessageConsumer consumer;
  
  public void run() {
    try {
      startAgentServer(
        (short)0, (File)null, 
        new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});
      
      AdminModule.connect("localhost", 2560, "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = org.objectweb.joram.client.jms.admin.User
          .create("anonymous", "anonymous", 0);

      org.objectweb.joram.client.jms.Queue queue = org.objectweb.joram.client.jms.Queue
          .create(0);
      queue.setFreeReading();
      queue.setFreeWriting();
      
      dest = queue;
      
      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);
      ((org.objectweb.joram.client.jms.tcp.TcpConnectionFactory)cf).
        getParameters().cnxPendingTimer = 500;
      
      for (int i = 0; i < NB_LOOP; i++) {
        connection = cf.createConnection("anonymous", "anonymous");

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        producer = session.createProducer(dest);

        consumer = session.createConsumer(dest);

        connection.start();

        new Thread() {
          public void run() {
            while (true) {
              try {
                TextMessage msg = session.createTextMessage("hello");
                producer.send(msg);
              } catch (Exception exc) {
                assertTrue("unexpected exception: " + exc,
                    exc instanceof javax.jms.IllegalStateException);
                break;
              }
            }
          }
        }.start();

        Thread.sleep(500);

        //System.out.println("Close #" + i);
        connection.close();
      }
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }

}
