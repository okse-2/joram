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
package joram.studycase.itgw;

import java.lang.reflect.Method;

import javax.jms.*;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

class Consumer implements MessageListener {
  static Destination dest = null;

  static Destination createDestination(String classname,
                                       String name,
                                       int sid) throws Exception {
    Class c = Class.forName(classname);
    Method m = c.getMethod("create",
                           new Class[]{int.class,
                                       java.lang.String.class});
    return (Destination) m.invoke(null, new Object[]{new Integer(sid), name});
  }

  public static void main(String[] args) throws Exception {
    new Consumer().run(args);
  }

  Connection cnx = null;
  Session sess = null;
  MessageProducer producer = null;
  MessageConsumer cons = null;

  public void run(String[] args) throws Exception {
    AgentServer.init((short) 0, "./s0", null);
    AgentServer.start();

    String destc = System.getProperty("Destination",
                                      "org.objectweb.joram.client.jms.Queue");

    Thread.sleep(1000L);

    AdminModule.collocatedConnect("root", "root");

    dest = createDestination(destc, "queue", 0);

    User user = User.create("anonymous", "anonymous", 0);

    dest.setFreeReading();
    dest.setFreeWriting();
    
    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

    try {
      cnx = new LocalConnectionFactory().createConnection();
      sess = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
      producer = sess.createProducer(null);
      cons = sess.createConsumer(dest);
      cons.setMessageListener(this);

      cnx.start();
    } catch (Exception exc) {
      exc.printStackTrace();
      System.exit(-1);
    }
  }

  int counter = 0;

  private long start = 0L;
  private long end = 0L;

  public synchronized void onMessage(Message msg) {
    try {
      if (counter == 0)
        start = System.currentTimeMillis();

      counter += 1;
//       if ((counter % 10) == 0)
//         sess.commit();
      
      int idx = msg.getIntProperty("index");
      if (idx == 0) {
        end = System.currentTimeMillis();
        Destination reply = (Destination) msg.getJMSReplyTo();
        Message msg2 = sess.createMessage();
        producer.send(reply, msg2);
        sess.commit();
        System.out.println(" " + counter + " " + ((1000 * counter) / (end - start)));
      }
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }
}
