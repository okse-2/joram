/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 - 2013 ScalAgent Distributed Technologies
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
package perfs;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Topic;

/**
 * MessageConsumer receiving messages on queue or topic for performance statistics.
 */
public class Consumer implements MessageListener {
  static Destination dest = null;
  static ConnectionFactory cf = null;

  static int NbMsgPerRound = 10000;
  static int NbMaxMessage = -1;
  
  static boolean durable = false;
  static boolean transacted = true;
  static boolean dupsOk = true;
  
  static int queueMessageReadMax = 1000;
  static int topicAckBufferMax = 100;
  static boolean implicitAck = true;

  static Session session = null;

  public static boolean getBoolean(String key, boolean def) {
    String value = System.getProperty(key, Boolean.toString(def));
    return Boolean.parseBoolean(value);
  }

  public static void main (String args[]) throws Exception {
    durable = getBoolean("SubDurable", durable);
    transacted = getBoolean("Transacted", transacted);
    dupsOk = getBoolean("dupsOk", dupsOk);
    
    queueMessageReadMax = Integer.getInteger("queueMessageReadMax", queueMessageReadMax).intValue();
    topicAckBufferMax = Integer.getInteger("topicAckBufferMax", topicAckBufferMax).intValue();
    implicitAck = getBoolean("implicitAck", implicitAck);

    NbMsgPerRound = Integer.getInteger("NbMsgPerRound", NbMsgPerRound).intValue();
    NbMaxMessage = Integer.getInteger("NbMaxMessage", NbMaxMessage).intValue();
    
    InitialContext ictx = new InitialContext();
    Destination dest = (Destination) ictx.lookup(args[0]);
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    System.out.println("Destination: " + (dest.isQueue()?"Queue":"Topic"));
    System.out.println("Subscriber: durable=" + durable + ", dupsOk=" + dupsOk);
    System.out.println("            queueMessageReadMax=" + queueMessageReadMax +
                       ", topicAckBufferMax=" + topicAckBufferMax);
    System.out.println("Transacted=" + transacted);
    System.out.println("Subscriber:       implicitAck=" + implicitAck);

    Connection cnx = cf.createConnection();
    int mode;
    if (dupsOk) {
      mode = Session.DUPS_OK_ACKNOWLEDGE;
    } else {
      mode = Session.AUTO_ACKNOWLEDGE;
    }
    session = cnx.createSession(transacted, mode);
    
    ((org.objectweb.joram.client.jms.Session)session).setQueueMessageReadMax(queueMessageReadMax);
    ((org.objectweb.joram.client.jms.Session)session).setTopicAckBufferMax(topicAckBufferMax);
    ((org.objectweb.joram.client.jms.Session)session).setImplicitAck(implicitAck);
     
    MessageConsumer consumer = null;
    try {
      if (durable && dest instanceof Topic) {
        consumer = session.createDurableSubscriber((Topic)dest, "dursub");
      } else {
        consumer = session.createConsumer(dest);
      }
      Consumer listener = new Consumer();
      consumer.setMessageListener(listener);
      cnx.start();

      if (NbMaxMessage == -1) {
        System.in.read();
      } else {
        do {
          Thread.sleep(1000L);
        } while (listener.counter < NbMaxMessage);
      }
    } finally {
      consumer.close();
      if (durable && dest instanceof Topic) {
        session.unsubscribe("dursub");
      }
    }
    if (transacted) session.commit();
    cnx.close();
  }

  int counter = 0;
  long travel = 0L;

  long start = 0L;
  long last = 0L;

  long t1 = 0L;
  
  public synchronized void onMessage(Message m) {
    try {
      BytesMessage msg = (BytesMessage) m;

      last = System.currentTimeMillis();
      int index = msg.getIntProperty("index");
      if (index == 0) start = t1 = last;

      travel += (last - msg.getLongProperty("time"));
      counter += 1;
      
      if (transacted && (((counter%10) == 9) || (index == 0)))
        session.commit();
      
      if ((counter%NbMsgPerRound) == (NbMsgPerRound -1)) {
        long x = (NbMsgPerRound * 1000L) / (last - t1);
        t1 = last;
        System.out.println("#" + ((counter+1)/NbMsgPerRound) + " x " + NbMsgPerRound + " msg -> " + x + " msg/s " + (travel/counter));
      }

    } catch (IllegalStateException exc) {
      throw exc;
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}
