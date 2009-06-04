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
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Testing:
 * 1- Message consumer receive with a concurrent consumer close
 * 2- Message consumer receive with a concurrent session close
 * 3- Message consumer receive with a concurrent connection close
 */
public class ClientTest2 extends TestCase {

  public static final int LOOP_NB = 100;

  public static void main(String[] args) {
    new ClientTest2().run();
  }

  private Connection connection;
  
  private Session session;

  private Destination dest;

  private MessageConsumer consumer;

  public void run() {
    try {
      startAgentServer(
        (short)0, (File)null, 
        new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

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

      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);

      Message msg = null;

      for (int i = 0; i < 300; i++) {
        
        connection = cf.createConnection(
          "anonymous", "anonymous"); 
        
        session = connection.createSession(
          false,
          Session.AUTO_ACKNOWLEDGE);
        consumer = session.createConsumer(dest);
        
        connection.start();
        
        new Thread() {
          public void run() {
            try {
              consumer.close();
            } catch (JMSException exc) {
              exc.printStackTrace();
            }
          }
        }.start();
        
        try {
          msg = consumer.receive();
        } catch (Exception exc) {
          // May happen if the consumer is already closed.
        }

        assertTrue("Null expected", msg == null);
        //System.out.println("Close consumer OK (" + i + ')');

        consumer = session.createConsumer(dest);
        
        new Thread() {
          public void run() {
            try {
              session.close();
            } catch (JMSException exc) {
              exc.printStackTrace();
            }
          }
        }.start();
        
        try {
          msg = consumer.receive();
        } catch (Exception exc) {
          // May happen if the session is already closed.
        }

        assertTrue("Null expected", msg == null);
        //System.out.println("Close session OK (" + i + ')');
        
        session = connection.createSession(
          false,
          Session.AUTO_ACKNOWLEDGE);
        consumer = session.createConsumer(dest);
	
	new Thread() {
          public void run() {
            try {
              connection.close();
            } catch (JMSException exc) {
              exc.printStackTrace();
            }
          }
        }.start();
        
        try {
          msg = consumer.receive();
        } catch (Exception exc) {
          // May happen if the connection is already closed.
	}
          
        assertTrue("Null expected", msg == null);
        //System.out.println("Close connection OK (" + i + ')');
        
        // Check log file is empty
        File logFile = new File("server.log.0.1");
        assertTrue("Log file not empty: " + logFile, logFile.length() == 0);
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
