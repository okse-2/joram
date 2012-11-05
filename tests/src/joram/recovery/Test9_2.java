/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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
package joram.recovery;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 * Test recovery : start 2 server. queue on server 1. User attach on server 0. 
 * kill server 1. send message. restart server 1. and receive with the other user 
 * use some message
 */
public class Test9_2 extends framework.TestCase {
  static int MsgSize = 100;
  static int NbMsg = 1000; // Must be a multiple of 10

  public static void main (String args[]) throws Exception {
    new Test9_2().run();
  }

  public void run(){
    ConnectionFactory cf0, cf1;
    Destination dest;

    try{
      long t1, t2a, t2b;

      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();
      startAgentServer((short) 1);
      Thread.sleep(5000L);

      AdminModule.collocatedConnect("root", "root");

      User user = User.create("anonymous", "anonymous", 0);
      User user1 = User.create("anonymous", "anonymous", 1);

      dest = Queue.create(1);
      dest.setFreeReading();
      dest.setFreeWriting();

      cf0 = new LocalConnectionFactory();
      cf1 = TcpConnectionFactory.create("localhost", 16011);

      AdminModule.disconnect();
      System.out.println("Administration done");

      Thread.sleep(2000L);
      stopAgentServer((short) 1);
      System.out.println("Server #1 stopped");
      Thread.sleep(2000L);

      Connection cnx = cf0.createConnection();
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(dest);
      cnx.start();

      MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();

      byte[] content = new byte[MsgSize];
      for (int i = 0; i< MsgSize; i++)
        content[i] = (byte) (i & 0xFF);

      System.out.println("Send messages");
      t1= System.currentTimeMillis();
      t2a= 0;
      t2b= t1;
      for (int i=0; i<NbMsg; i++) {
        BytesMessage msg = session.createBytesMessage();
        msg.setIntProperty("index", i);
        msg.writeBytes(content);
        producer.send(msg);

        if ((i == 0) || (i% (NbMsg /10)) == ((NbMsg /10) -1)) {
          t2a = System.currentTimeMillis();

          System.out.println("send #" + i +
                             " - time=" +  ((t2a - t1)/(i +1)) +
                             " / " + ((t2a - t2b)/(NbMsg /10)));
          t2b = t2a;
        }
      }
      System.out.println("Messages sent");
      session.close();
      cnx.close();

      Thread.sleep(5000L);

      startAgentServer((short) 1);
      System.out.println("Server #1 started");

      cnx = cf1.createConnection();
      session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageConsumer cons = session.createConsumer(dest);
      cnx.start();

      t1= System.currentTimeMillis();
      t2a= 0;
      t2b= t1;
      for (int i=0; i<NbMsg; i++) {
        BytesMessage msg =(BytesMessage) cons.receive();
        assertEquals(i, msg.getIntProperty("index"));

        if ((i == 0) || (i% (NbMsg /10)) == ((NbMsg /10) -1)) {
          t2a = System.currentTimeMillis();
          if (i == 0) {
            System.out.println("receive #0 - time=" + ((t2a - t1)/(i +1)));
          } else {
            System.out.println("receive #" + i +
                               " - time=" +  ((t2a - t1)/(i +1)) +
                               " / " + ((t2a - t2b)/(NbMsg /10)));
            t2b = t2a;
          }
        }
      }

    } catch(Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 1);
      AgentServer.stop();
      endTest();
    }
  }
}
