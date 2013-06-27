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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.perfs;

import java.util.ArrayList;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Destination;

public class Sender extends BaseTest implements Runnable {
  Connection cnx;
  Destination dest;
  int NbRound;
  int NbMsgPerRound;
  int MsgSize;

  boolean transacted;

  Session sess1, sess2;
  MessageProducer producer;
  
  TemporaryTopic topic;
  MessageConsumer cons;
  MsgListener listener;

  Lock lock;

  boolean MsgTransient;
  boolean isByteMsg = true;

  public Sender(Connection cnx,
                Destination dest,
                int NbRound,
                int NbMsgPerRound,
                int MsgSize,
                Lock lock,
                boolean MsgTransient,
                boolean isByteMsg) throws Exception {
    this.cnx = cnx;
    this.dest = dest;
    this.NbRound = NbRound;
    this.NbMsgPerRound = NbMsgPerRound;
    this.MsgSize = MsgSize;

    this.lock = lock;

    this.MsgTransient = MsgTransient;
    this.isByteMsg = isByteMsg;

    transacted = Boolean.getBoolean("Transacted");

    sess1 = cnx.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
    producer = sess1.createProducer(dest);
    
    sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    topic = sess2.createTemporaryTopic();
    cons = sess2.createConsumer(topic);
    listener = new MsgListener();
    cons.setMessageListener(listener);
  }

  public void start() throws Exception {
    cnx.start();
  }

  long dt1 = 0L;
  long dt2 = 0L;
  ArrayList arrayList = null;

  
  private static String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
  private static int charLength = chars.length();
  
  public static String generateString(int length) {
      StringBuilder  pass = new StringBuilder (charLength);
      for (int x = 0; x < length; x++) {
          int i = (int) (Math.random() * charLength);
          pass.append(chars.charAt(i)).append(" \n");
      }
      return pass.toString();
  }
  
  public static byte[] generateBytes(int MsgSize) {
    byte[] content = new byte[MsgSize];
    for (int i = 0; i< MsgSize; i++)
      content[i] = (byte) (i & 0xFF);
    return content;
  }

  public void run() {

    try {
      long t1 = System.currentTimeMillis();
      arrayList = new ArrayList();
     
      Object content = null;
      if (isByteMsg)
        content = generateBytes(MsgSize);
      else
        content = generateString(Math.round(MsgSize/3)-4);

      long min = Long.MAX_VALUE;
      long max = 0;

      for (int i=0; i<NbRound; i++) {
        long start = System.currentTimeMillis();
        for (int j=0; j<NbMsgPerRound; j++) {
          Message msg = null;
          if (isByteMsg) {
            msg = sess1.createBytesMessage();
          } else {
            msg = sess1.createTextMessage();
          }
          //((org.objectweb.joram.client.jms.TextMessage)msg).setCompressionLevel(Deflater.BEST_COMPRESSION);
          if (MsgTransient) {
            msg.setJMSDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
            producer.setDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
          }
          
          if (isByteMsg)
            ((BytesMessage) msg).writeBytes((byte[])content);
          else
            ((TextMessage) msg).setText((String) content);
          msg.setLongProperty("time", System.currentTimeMillis());
          msg.setIntProperty("index", NbMsgPerRound-j-1);
          msg.setJMSReplyTo(topic);
          producer.send(msg);
          if (transacted && ((j%10) == 9)) sess1.commit();
        }
        long end = System.currentTimeMillis();
        long dt = end - start;
        if (dt < min) min = dt;
        if (max < dt) max = dt;
        dt1 += dt;
        
        listener.fxCtrl(i);
        if (end-start > 0) {
          //System.out.println("== " + ((1000L * (NbMsgPerRound)) / (end-start)) + "msg/s");//NTA tmp
          arrayList.add(new Integer((int) ((1000L * (NbMsgPerRound)) / (end-start))));
        }
      }

      listener.fxCtrl(NbRound);
      long t2 = System.currentTimeMillis();
      dt2 = t2 - t1;

      System.out.println("min=" + min + ", max=" + max);

    } catch (Exception exc) {
      exc.printStackTrace();
      System.exit(-1);
    }


    lock.exit();
  }
  
  public static void main(String args[]) throws Exception {
    String baseclass = "joram.perfs.TcpBaseTest";

    baseclass = System.getProperty("BaseClass", baseclass);

    AdminConnect(baseclass);
    ConnectionFactory cf =  createConnectionFactory(baseclass);
    ((org.objectweb.joram.client.jms.ConnectionFactory)cf).getParameters().noAckedQueue = Boolean.getBoolean("noAckedQueue");
    Connection cnx = cf.createConnection();

    Destination dest = null;

    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

    int NbRound = 10;
    int NbMsgPerRound = 100;
    int MsgSize = 100;

    NbRound = Integer.getInteger("NbRound", NbRound).intValue();
    NbMsgPerRound = Integer.getInteger("NbMsgPerRound", NbMsgPerRound).intValue();
    MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();

    boolean MsgTransient = Boolean.getBoolean("MsgTransient");
    boolean isByteMsg = new Boolean(System.getProperty("isByteMsg", "true"));

    Lock lock = new Lock(1);
    Sender sender = new Sender(cnx, dest,
                               NbRound, NbMsgPerRound, MsgSize,
                               lock, MsgTransient, isByteMsg);
    sender.start();
    sender.run();

    lock.ended();
    System.exit(0);
  }


  /**
   * Implements the <code>javax.jms.MessageListener</code> interface.
   */
  static class MsgListener implements MessageListener {
    int count = 0;
    long last;

    public synchronized long fxCtrl(int round) {
      while (round > count) {
        try {
          wait();
        } catch (InterruptedException exc) {
        }
      }
      return last;
    }

    public synchronized void onMessage(Message msg) {
      try {
        count ++;
        last = msg.getLongProperty("time");
        notify();
      } catch (Throwable exc) {
        exc.printStackTrace();
      }
    }
  }
}
