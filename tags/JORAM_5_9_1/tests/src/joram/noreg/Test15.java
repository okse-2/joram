/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2013 ScalAgent Distributed Technologies
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
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.AgentServer;

class ExcList15 implements ExceptionListener {
  String name = null;
  int nbexc;

  ExcList15(String name) {
    this.name = name;
    nbexc = 0;
  }

  public void onException(JMSException exc) {
    nbexc += 1;
    // System.err.println(name + ": " + exc.getMessage());
  }
}

/**
 *
 */
public class Test15 extends BaseTest {
  static ConnectionFactory cf;
  static Destination dest;

  public static void main(String args[]) throws Exception {
    new Test15().run();
  }

  public void run() {
    try {
      startServer();

      String baseclass = "joram.noreg.ColocatedBaseTest";
      baseclass = System.getProperty("BaseClass", baseclass);

      String destclass = "org.objectweb.joram.client.jms.Queue";
      destclass = System.getProperty("Destination", destclass);

      Thread.sleep(500L);
      cf = createConnectionFactory(baseclass);
      AdminModule.connect(cf);

      User.create("anonymous", "anonymous", 0);
      dest = createDestination(destclass);
      dest.setFreeReading();
      dest.setFreeWriting();

      AdminModule.disconnect();

      Connection cnx2 = cf.createConnection();
      Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = sess2.createProducer(dest);
      cnx2.start();

      Sender15 sender = new Sender15(cnx2, sess2, producer);
      new Thread(sender).start();

      ExcList15 exclst = new ExcList15("Receiver");

      int nb1 = 0;
      int nb2 = 0;
      int idx = 0;
      for (int i = 0; i < 100; i++) {
        // System.out.println("connecting#" + i);
        Connection cnx1 = cf.createConnection();
        cnx1.setExceptionListener(exclst);
        Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer cons = sess1.createConsumer(dest);
        cnx1.start();
        // System.out.println("connected#" + i);
        for (int j = 0; j < 50; j++) {
          Message msg = cons.receive();
          nb2 += 1;
          int index = msg.getIntProperty("index");
          if (index != idx) {
            // System.out.println("recv#" + idx + '/' + index);
            nb1 += 1;
          }
          idx = index + 1;
        }
        // System.out.println("end recv#" + i);
        cnx1.close();
        // System.out.println("closed#" + i);
      }

      sender.stop();
      System.out.println("Test OK: " + exclst.nbexc + ", " + nb1 + ", " + nb2);

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();
    }
  }
}

class Sender15 implements Runnable {
  Connection cnx;
  Session session;
  MessageProducer producer;
  boolean running;

  public Sender15(Connection cnx, Session session, MessageProducer producer)
                                                                            throws Exception {
    this.cnx = cnx;
    this.session = session;
    this.producer = producer;

    running = false;
  }

  public void stop() {
    running = false;
  }

  public void run() {
    try {
      int i = 0;
      running = true;
      while (running) {
        Message msg = session.createMessage();
        msg.setIntProperty("index", i++);
        producer.send(msg);
      }
    } catch (Exception exc) {
      exc.printStackTrace();
    } finally {
      try {
        cnx.close();
      } catch (Exception exc) {
        exc.printStackTrace();
      }
    }
  }
}
