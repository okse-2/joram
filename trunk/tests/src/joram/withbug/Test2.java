/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2004 ScalAgent Distributed Technologies
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
 * Contributor(s):
 */
package joram.withbug;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.BaseTestCase;

class MsgList52 implements MessageListener {
  public void onMessage(Message msg) {
    try {
      int index = msg.getIntProperty("Index");
      if (index == 2) {
        synchronized(Test2.lock) {
          Test2.lock.notify();
        }
        System.out.println("notify");
      }
      System.out.println("receives msg#" + index);
      if (index > 2)
        System.out.println("should not receives msg#" + index);
    } catch(Throwable exc) {
      exc.printStackTrace();
    }
  }
}

public class Test2 extends BaseTestCase {
  static Object lock = null;

  public static void main (String args[]) throws Exception {
    try {
      AgentServer.init((short) 0, "./s0", null);
      AgentServer.start();
      Thread.sleep(1000L);

      lock = new Object();

      AdminModule.connect("root", "root", 60);
      User user = User.create("anonymous", "anonymous");
      Queue queue = Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();
      AdminModule.disconnect();
      ConnectionFactory cf =  LocalConnectionFactory.create();

      Connection cnx = cf.createConnection();

      Session sess1 = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod1 = sess1.createProducer(queue);

      Session sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons2 = sess2.createConsumer(queue);
      cons2.setMessageListener(new MsgList52());

      cnx.start();

      for (int i=0; i<50; i++) {
        Message msg = sess1.createMessage();
        msg.setIntProperty("Index", i);
        prod1.send(msg);
      }
      sess1.commit();
      System.out.println("send msgs");
      sess1.close();

      synchronized(lock) {
        lock.wait();
      }
      System.out.println("cnx close");
      cnx.close();

      Thread.sleep(1000L);
      System.out.println("server stop");
      AgentServer.stop();
    } catch (Exception exc) {
      exc.printStackTrace();
      System.exit(-1);
    }

  }
}
