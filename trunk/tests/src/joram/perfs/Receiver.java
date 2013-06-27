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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Topic;

public class Receiver extends BaseTest implements MessageListener {
  Connection cnx;
  Destination dest;

  boolean transacted;
  boolean durable;
  boolean dupsOk;
  int queueMessageReadMax;
  int topicAckBufferMax;
  boolean implicitAck;
  
  Session sess;
  MessageConsumer cons;
  MessageProducer prod;

  public Receiver(Connection cnx, Destination dest) throws Exception {
    this.cnx = cnx;
    this.dest = dest;

    transacted = Boolean.getBoolean("Transacted");
    durable = Boolean.getBoolean("SubDurable");
    dupsOk = Boolean.getBoolean("dupsOk");
    queueMessageReadMax = Integer.getInteger("queueMessageReadMax", 1).intValue();
    topicAckBufferMax = Integer.getInteger("topicAckBufferMax", 0).intValue();
    implicitAck = Boolean.getBoolean("implicitAck");

    int sessionMode;
    if (dupsOk) {
      sessionMode = Session.DUPS_OK_ACKNOWLEDGE;
    } else {
      sessionMode = Session.AUTO_ACKNOWLEDGE;
    }
    sess = cnx.createSession(transacted, sessionMode);
    
    ((org.objectweb.joram.client.jms.Session)sess).setQueueMessageReadMax(queueMessageReadMax);
    ((org.objectweb.joram.client.jms.Session)sess).setTopicAckBufferMax(topicAckBufferMax);
    ((org.objectweb.joram.client.jms.Session)sess).setImplicitAck(implicitAck);
    
    if (durable && dest instanceof Topic) {
      cons = sess.createDurableSubscriber((Topic)dest, "dursub");
    } else {
      cons = sess.createConsumer(dest);
    }
    prod = sess.createProducer(null);
// Use to test issue with durable subscription
//     prod.setDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);

    cons.setMessageListener(this);
  }

  public void start() throws Exception {
    cnx.start();
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

    Receiver receiver = new Receiver(cnx, dest);
    receiver.start();
  }

  int counter = 0;
  long travel = 0L;

  long start = 0L;
  long last = 0L;

  long t1 = 0L;
  
  public synchronized void onMessage(Message m) {
    boolean excok = false;

    try {
      Message msg = m;

      last = System.currentTimeMillis();
      if (counter == 0) start = t1 = last;

      int index = msg.getIntProperty("index");
      counter += 1;

      if (index == 0) {
        // sends a flow-control message to sender
        javax.jms.Destination sender = msg.getJMSReplyTo();
        Message fx = sess.createMessage();
        fx.setLongProperty("time", last);
        prod.send(sender, fx);
      }

      if (transacted && (((counter%10) == 9) || (index == 0)))
        sess.commit();
      
      if ((counter%10000) == 9999) {
        long x = 10000000L / (last - t1);
        t1 = last;
        System.out.println("#" + ((counter+1)/10000) + " x 10.000 msg -> " + x + " msg/s");
      }

      travel += (last - msg.getLongProperty("time"));
    } catch (IllegalStateException exc) {
      throw exc;
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}

