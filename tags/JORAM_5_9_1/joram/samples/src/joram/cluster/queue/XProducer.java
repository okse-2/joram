/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 - 2014 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package cluster.queue;

import javax.jms.*;
import javax.naming.*;

/**
 * Produces messages on the cluster queue.
 */
public class XProducer implements Runnable {
  static Context ictx = null;
  
  static int NbClient = 1;
  static int Round = 3000;
  static int NbMsgPerRound = 10;
  static int MsgSize = 500;
  static int mps = 100;

  static Queue dest = null;
  static ConnectionFactory cf = null;

  static boolean MsgTransient = false;
  static boolean SwapAllowed = false;
  static boolean transacted = false;
  static boolean asyncSend = false;

  static String location;
  
  public static boolean getBoolean(String key, boolean def) {
    String value = System.getProperty(key, Boolean.toString(def));
    return Boolean.parseBoolean(value);
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1)
     throw new Exception("Bad number of argument");

    ictx = new InitialContext();
    try {
      if (args[0].equals("-")) {
        // Choose a connection factory and the associated topic depending of
        // the location property.
        cf = (ConnectionFactory) ictx.lookup("clusterCF");
        dest = (Queue) ictx.lookup("clusterQueue");
      } else {
        cf = (ConnectionFactory) ictx.lookup("cf" + args[0]);
        dest = (Queue) ictx.lookup("queue" + args[0]);
        System.setProperty("location", "server" + args[0]);
      }
    } finally {
      ictx.close();
    }

    NbClient = Integer.getInteger("NbClient", NbClient).intValue();
    Round = Integer.getInteger("Round", Round).intValue();
    NbMsgPerRound = Integer.getInteger("NbMsgPerRound", NbMsgPerRound).intValue();
    MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();
    mps = Integer.getInteger("mps", mps).intValue();

    MsgTransient = getBoolean("MsgTransient", MsgTransient);
    SwapAllowed = getBoolean("SwapAllowed", SwapAllowed);
    transacted = getBoolean("Transacted", transacted);
    asyncSend = getBoolean("asyncSend", asyncSend);
    
    System.out.println("Message: MsgTransient=" + MsgTransient);
    System.out.println("Message: SwapAllowed=" + SwapAllowed);
    System.out.println("Transacted=" + transacted);
    System.out.println("asyncSend=" + asyncSend);
    System.out.println("NbMsg=" + (Round*NbMsgPerRound) + ", MsgSize=" + MsgSize);
    
    ((org.objectweb.joram.client.jms.ConnectionFactory) cf).getParameters().asyncSend = asyncSend;

    location = System.getProperty("location");
    if (location != null)
      System.out.println("Sends messages on queue on " + location);
    
    new XProducer().run();
  }
  
  public void run() {
    try {
      Connection cnx = cf.createConnection();
      Session session = cnx.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(dest);
      if (MsgTransient) {
        producer.setDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
      }

      byte[] content = new byte[MsgSize];
      for (int i = 0; i< MsgSize; i++)
        content[i] = (byte) (i & 0xFF);

      long dtx = 0;
      long start = System.currentTimeMillis();
      for (int i=0; i<(Round*NbMsgPerRound); i++) {
        TextMessage msg = session.createTextMessage();
        msg.setStringProperty("location", location);
        if (SwapAllowed) {
          msg.setBooleanProperty("JMS_JORAM_SWAPALLOWED", true);
        }
        msg.setText("location " + location +" : Test number " + i);
        msg.setLongProperty("time", System.currentTimeMillis());
        msg.setIntProperty("index", i);
        producer.send(msg);

        if (transacted && ((i%10) == 9)) session.commit();

        if ((i%mps) == (mps-1)) {
          long dtx1 = (i * 1000L) / mps;
          long dtx2 = System.currentTimeMillis() - start;
          if (dtx1 > (dtx2 + 20)) {
            dtx += (dtx1 - dtx2);
            Thread.sleep(dtx1 - dtx2);
          }
          System.out.println("sent=" + i + ", mps=" + ((((long) i) * 1000L)/dtx2));
        }
      }
      long end = System.currentTimeMillis();
      long dt = end - start;

      System.out.println("----------------------------------------------------");
      System.out.println("| sender dt=" +  ((dt *1000L)/(Round*NbMsgPerRound)) + "us -> " +
          ((1000L * (Round*NbMsgPerRound)) / (dt)) + "msg/s");
      System.out.println("| sender wait=" + dtx + "ms");

      cnx.close();
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }
}
