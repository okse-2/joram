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



import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Testing:
 * - concurrent close of a session while
 * receiving. Check that there are no messages
 * loss.
 */
public class ClientTest11 extends TestCase {

  public static void main(String[] args) {
    new ClientTest11().run();
  }

  private volatile Session recSession;

  public void run() {
    try {
      startAgentServer(
        (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

      AdminModule.connect("localhost", 2560,
                          "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create(
          "anonymous", "anonymous", 0);

      org.objectweb.joram.client.jms.Queue queue = 
        org.objectweb.joram.client.jms.Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();

      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);
      
      Connection connection = cf.createConnection(
        "anonymous", "anonymous");

      Session sendSession = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = sendSession.createProducer(queue);

      connection.start();

      for (int i = 0; i < 100; i++) {
        TextMessage msg = sendSession.createTextMessage("msg#" + i);
        producer.send(msg);
      }
      
      for (int j = 0; j < 5; j++) {
        recSession = connection.createSession(
          false,
          Session.CLIENT_ACKNOWLEDGE);
        MessageConsumer consumer = recSession.createConsumer(queue);
        new Thread() {
            public void run() {
              try {
                Thread.sleep(20);
                recSession.close();
              } catch (Exception exc) {
                exc.printStackTrace();
              }
            }
          }.start();
        
        for (int i = 0; i < 100; i++) {
          TextMessage msg = null;
          try {
            msg = (TextMessage)consumer.receive();
          } catch (Exception exc) {}
          if (msg != null) {
	      //System.out.println("Received " + msg.getText());
          } else {
	      //System.out.println("Session closed (" + j + ')');
            break;
          }
        }

        // Wait for the previous session to be closed
        // (it is asynchronously closed).
        Thread.sleep(500);
        
        System.out.println("Check message loss");
        recSession = connection.createSession(
          false,
          Session.CLIENT_ACKNOWLEDGE);
        consumer = recSession.createConsumer(queue);
        for (int i = 0; i < 100; i++) {
          TextMessage msg = (TextMessage)consumer.receive();
	  assertTrue(msg != null);
	   if (! msg.getText().equals("msg#" + i)) {
            String errorMsg = "Missing message: #" + i + 
              " (" +  msg.getText() + ')';
            System.out.println(errorMsg);
            error(new Exception(errorMsg));
            break;
	    }
        }
        recSession.close();
      }

      connection.close();
      AdminModule.disconnect();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}
