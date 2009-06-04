/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2009 ScalAgent Distributed Technologies
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
package joram.perfs;

import java.lang.reflect.Method;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.util.Configuration;

/**
 *
 */
public abstract class Test1 {
  static int NbRound = 100;
  static int NbMsgPerRound = 100;
  static int MsgSize = 100;

  static Destination dest = null;
  static ConnectionFactory cf = null;

  static String host = "localhost";
  static int port = 16010;

  static long dt1 = 0L;
  static long dt2 = 0L;
  static long dt3 = 0L;

  abstract protected void AdminConnect() throws Exception;
  abstract protected ConnectionFactory createConnectionFactory() throws Exception;

  protected Destination createDestination(String classname) throws Exception {
    Class c = Class.forName(classname);
    Method m = c.getMethod("create", new Class[]{int.class});
    return (Destination) m.invoke(null, new Object[]{new Integer(0)});
  }

  protected void startServer() throws Exception {
    AgentServer.init((short) 0, "./s0", null);
    AgentServer.start();

    Thread.sleep(1000L);
  }

  public void run(String[] args) throws Exception {
    if (! Boolean.getBoolean("ServerOutside"))
      startServer();

    NbRound = Integer.getInteger("NbRound", NbRound).intValue();
    NbMsgPerRound = Integer.getInteger("NbMsgPerRound", NbMsgPerRound).intValue();
    MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();

    String destc = System.getProperty("Destination",
                                      "org.objectweb.joram.client.jms.Queue");

    host = System.getProperty("hostname", host);
    port = Integer.getInteger("port", port).intValue();

    System.out.println("====================================================");
    System.out.println("Transaction: " + Configuration.getProperty("Transaction"));
    System.out.println("Engine: " + Configuration.getProperty("Engine"));
    System.out.println("Destination: " + destc);
    System.out.println("NbRound: " + NbRound);
    System.out.println("NbMsgPerRound: " + NbMsgPerRound);
    System.out.println("MsgSize: " + MsgSize);
    System.out.println("====================================================");

    AdminConnect();

    dest = createDestination(destc);
    cf =  createConnectionFactory();

    User user = User.create("anonymous", "anonymous", 0);

    dest.setFreeReading();
    dest.setFreeWriting();

    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

    Receiver receiver = new Receiver();
    receiver.start();

    long t1 = System.currentTimeMillis();
    try {
      Connection cnx = cf.createConnection();
      Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = sess.createProducer(dest);

      byte[] content = new byte[MsgSize];
      for (int i = 0; i< MsgSize; i++)
        content[i] = (byte) (i & 0xFF);

      for (int i=0; i<NbRound; i++) {
        long start = System.currentTimeMillis();
        for (int j=0; j<NbMsgPerRound; j++) {
          BytesMessage msg = sess.createBytesMessage();
          msg.writeBytes(content);
          msg.setLongProperty("time", System.currentTimeMillis());
          producer.send(msg);
        }
        long end = System.currentTimeMillis();
        dt1 += (end-start);
        receiver.fxCtrl(i);
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      System.exit(-1);
    }

    receiver.stop();
    long t2 = System.currentTimeMillis();

    System.out.println("====================================================");
    System.out.println("| sender dt(us)=" + 
                       ((dt1 *1000L)/(NbMsgPerRound * NbRound)));
    System.out.println("| receiver dt(us)=" +
                       ((dt2 *1000L)/(NbMsgPerRound * NbRound)));
    System.out.println("| Mean travel time (ms)=" +
                       (dt3/(NbMsgPerRound * NbRound)));
    System.out.println("| Mean time = " +
                       ((t2-t1)*1000L) / (NbRound*NbMsgPerRound) +
                       "us per msg, " +
                       ((1000L * (NbRound*NbMsgPerRound)) / (t2-t1)) +
                       "msg/s");
    System.out.println("====================================================");
    System.exit(0);
  }

  static class Receiver {
    MsgListener listener;
    Connection cnx;

    public void start() {
      try {
        cnx = cf.createConnection();
        Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer cons = sess.createConsumer(dest);
         
        listener = new MsgListener();
        cons.setMessageListener(listener);

        cnx.start();
      } catch (Exception exc) {
        exc.printStackTrace();
        System.exit(-1);
      }
    }

    public void fxCtrl(int round) {
      try {
        synchronized(listener) {
          if ((listener.counter != -1) &&
              (listener.counter < (round * NbMsgPerRound)))
            listener.wait();
        }
      } catch (InterruptedException exc) {
      }
    }

    public void stop() {
      try {
        synchronized(listener) {
          if (listener.counter != -1)
            listener.wait();
        }
        cnx.close();
        System.out.println("Consumer terminated");
      } catch (Exception exc) {
        exc.printStackTrace();
        System.exit(-1);
      }
    }
  }

  /**
   * Implements the <code>javax.jms.MessageListener</code> interface.
   */
  static class MsgListener implements MessageListener {
    int counter = 0;
    private long travel = 0L;

    private long start = 0L;
    private long end = 0L;

    public synchronized void onMessage(Message m) {
      try {
        BytesMessage msg = (BytesMessage) m;
        counter += 1;

        if ((counter % NbMsgPerRound) == 1) {
          start = System.currentTimeMillis();
        }

        if ((counter % NbMsgPerRound) == 0) {
          end = System.currentTimeMillis();
          dt2 += (end-start);
        }

        travel += (System.currentTimeMillis() - msg.getLongProperty("time"));

//         byte[] content = new byte[(int) msg.getBodyLength()];
//         System.out.println("size: " + content.length);
//         System.out.println("size: " + msg.readBytes(content));
//         for (int i = 0; i< content.length; i++)
//           if (content[i] != (byte) (i & 0xFF))
//             System.out.println("bad " + i);

        if ((counter % NbMsgPerRound) == 0) {
          dt3 += travel;
          travel = 0;

          if ((counter % NbMsgPerRound) == 0) {
            if (counter == (NbRound * NbMsgPerRound))
              counter = -1;
            this.notify();
          }
        }
      } catch (Exception exc) {
        exc.printStackTrace();
      }
    }
  }
}
