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
 * Contributor(s):Badolle Fabien (ScalAgent D.T.)
 */
package joram.perfs;

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

class ServerTest5 extends BaseTest {
  static int NbClients = 4;

  static Destination sync = null;
  static ConnectionFactory cf = null;

  public static void main(String[] args) throws Exception {
    new  ServerTest5().run();
  }
  public void run(){
    try{
      startServer();

      AdminConnect("joram.perfs.ColocatedBaseTest");
      sync = createDestination("org.objectweb.joram.client.jms.Queue", "SyncQ");
      cf =  createConnectionFactory("joram.perfs.ColocatedBaseTest");
      User user = User.create("anonymous", "anonymous", 0);
      sync.setFreeReading();
      sync.setFreeWriting();
      org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

      NbClients = Integer.getInteger("NbClients", NbClients).intValue();
      Connection cnx = cf.createConnection();
      
      Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageConsumer cons = sess.createConsumer(sync);
      cnx.start();

      int nbmsg = 0;
      long total = 0L;
      long travel = 0L;

      for (int i=0; i<NbClients; i++) {
        try {
          Message msg = cons.receive();

          nbmsg += msg.getIntProperty("nbmsg");
          total += msg.getLongProperty("total");
          travel += msg.getLongProperty("travel");
        } catch (Throwable exc) {
          exc.printStackTrace();
        }
      }

      writeIntoFile("| Mean time = " + (total*1000L) / nbmsg + "us per msg, " + 
                    ((nbmsg*1000L*NbClients)/total) + "msg/s\n" +
                    "| Mean travel time = " + (travel / (NbClients*1000)) + "ms");


    } catch(Throwable exc){
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      fr.dyade.aaa.agent.AgentServer.stop();
      endTest(); 
    }
  }
}

/**
 *
 *
 */
public class Test5 extends BaseTest {
  public static void main(String[] args) throws Exception {
    new Test5().run(args);
  }

  static int NbCnx = 5;
  static int NbRound = 10;
  static int NbMsgPerRound = 100;
  static int MsgSize = 100;

  static String host = "localhost";
  static int port = 16010;

  static Destination dest = null;
  static Destination sync = null;
  static ConnectionFactory cf = null;

  public void run(String[] args)  {
    try{
      NbCnx = Integer.getInteger("NbCnx", NbCnx).intValue();
      NbRound = Integer.getInteger("NbRound", NbRound).intValue();
      NbMsgPerRound = Integer.getInteger("NbMsgPerRound", NbMsgPerRound).intValue();
      MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();
      String destc = System.getProperty("Destination",
      "org.objectweb.joram.client.jms.Queue");
      host = System.getProperty("hostname", host);
      port = Integer.getInteger("port", port).intValue();
      String baseclass = "joram.perfs.TcpBaseTest";
      baseclass = System.getProperty("BaseClass", baseclass);
      AdminConnect(baseclass);
      dest = createDestination(destc);
      sync = createDestination("org.objectweb.joram.client.jms.Queue", "SyncQ");
      cf =  createConnectionFactory(baseclass);
      User user = User.create("anonymous", "anonymous", 0);
      dest.setFreeReading();
      dest.setFreeWriting();

      org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

      Connection cnx = null;
      Session sess = null;
      MessageProducer producer = null;
      Receiver receiver = null;
      long total = 0L;
      for (int k=0; k<NbCnx; k++) {
        receiver = new Receiver();
        receiver.start();

        long t1 = System.currentTimeMillis();
        try {
          cnx = cf.createConnection();
          sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
          producer = sess.createProducer(dest);

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
            receiver.fxCtrl(i);
          }
        } catch (Exception exc) {
          exc.printStackTrace();
          System.exit(-1);
        } finally {
          producer.close();
          sess.close();
          cnx.close();
          receiver.stop();
        }

        long t2 = System.currentTimeMillis();
        total += (t2 - t1);

        writeIntoFile("| Mean time " + destc + " = " +
                      ((t2-t1)*1000L) / (NbRound*NbMsgPerRound) +
        "us per msg");

      }

      try {
        cnx = cf.createConnection();
        sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
        producer = sess.createProducer(sync);
        cnx.start();

        Message msg = sess.createMessage();
        msg.setIntProperty("nbmsg", NbCnx*NbRound*NbMsgPerRound);
        msg.setLongProperty("total", total);
        msg.setLongProperty("travel", receiver.listener.travel);
        producer.send(msg);
      } catch (Exception exc) {
        exc.printStackTrace();
        System.exit(-1);
      } finally {
        producer.close();
        sess.close();
        cnx.close();
      }

    } catch(Throwable exc){
      exc.printStackTrace();
      error(exc);
    } finally {
      //end
    }
  }

static class Receiver {
    Connection cnx;
    Session sess;
    MessageConsumer cons;
    MsgListener listener;

    public void start() {
      try {
        cnx = cf.createConnection();
        sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
        cons = sess.createConsumer(dest);
         
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
        cons.close();
        sess.close();
        cnx.close();
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

    public synchronized void onMessage(Message msg) {
      try {
        counter += 1;

        if ((counter % NbMsgPerRound) == 1) {
          start = System.currentTimeMillis();
        }

        if ((counter % NbMsgPerRound) == 0) {
          end = System.currentTimeMillis();
        }

        travel += (System.currentTimeMillis() - msg.getLongProperty("time"));

        if ((counter % NbMsgPerRound) == 0) {
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
