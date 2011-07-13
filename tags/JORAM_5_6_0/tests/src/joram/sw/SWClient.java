/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package joram.sw;

import java.util.Timer;
import java.util.TimerTask;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;

/**
 * This test reproduces a user's scenario:
 *  - Multiples Joram's servers connected by a PoolNetwork component.
 *  - On each server there is a topic, the 5 topics are clustered.
 *  - Each server is colocated with a client defining:
 *    - A producer sending quickly 1.000 messages on the topic, then waiting 120 seconds, and so on..
 *    - A consumer receiving all messages sent to the clustered topic.
 * The test will verify that all messages are received in a correct order.
 */
public class SWClient extends TestCase implements MessageListener {
  int sid;
  int nbmsg = 0;
  
  // Be careful, this period must be bigger than the time needed for a round.
  // Otherwise messages from different rounds are getting muddled.
  static long period = 90000;
  
  static int msgPerLoop = 1000;
  static int nbLoop = 5;
  
  static Object lock = null;
  
  public static void main(String[] args) throws Exception {
    SWClient client = new SWClient();
    client.sid = Integer.parseInt(args[0]);
    client.run();
  }

  public void run() {
    try {
      fr.dyade.aaa.agent.AgentServer.init(new String[] { "" + sid, "s" + sid });
      fr.dyade.aaa.agent.AgentServer.start();

      Thread.sleep(1000);
      
      AdminModule.collocatedConnect("root", "root");
      
      User user = User.create("anonymous", "anonymous");
      
      Topic local = Topic.create("Topic" + sid);
      local.setFreeReading();
      local.setFreeWriting();

      if (sid != 0) {
        Topic root = Topic.create(0, "Topic" + 0);
        root.addClusteredTopic(local);
      }

      Thread.sleep(5000);

//      List<Topic> cluster = local.getClusterFellows();
//      for (int i=0; i<cluster.size(); i++) {
//        Topic element = cluster.get(i);
//        System.out.println("#" + sid + " - " + element);
//      }
//
//      Thread.sleep(5000);
      
      LocalConnectionFactory cf = new LocalConnectionFactory();
      Connection cnx = cf.createConnection("anonymous", "anonymous");
      Session sessp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageProducer producer = sessp.createProducer(local);
      MessageConsumer consumer = sessc.createConsumer(local);
      consumer.setMessageListener(this);

      cnx.start();

      lock = new Object();
      
      Timer timer = new Timer();
      SendTask task = new SendTask(sessp, producer);
      timer.scheduleAtFixedRate(task, 5000, period);
      
      synchronized(lock) {
        while (current < nbLoop) {
          try {
            lock.wait();
          } catch (Exception exc) {}
        }
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest(); 
    }
  }

  int current = -1;
  long start, end;
  long dt = 0;
  
  // Be careful, this array doesn't allow the use of a server with sid greater than 9.
  int idx[] = {-1, -1, -1, -1, -1,-1, -1, -1, -1, -1};
  
  /**
   * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
   */
  public void onMessage(Message m) {
    try {
      TextMessage msg = (TextMessage) m;
      int m_sid = msg.getIntProperty("sid");
      int m_loop = msg.getIntProperty("loop");
      int m_idx = msg.getIntProperty("idx");
      
      long time = System.currentTimeMillis();
      
      if (m_loop != current) { // Start a new round
        if (current != -1) {
          System.out.println("receiver#" + sid + '.' + current + "(" + m_sid + ", " + m_idx + ") - " + (end/1000) + ' ' + nbmsg + '/' + (end -start));
          dt += (end - start);
        }
        current = m_loop;
        synchronized(lock) {
          if (current == nbLoop) lock.notify();
        }
        start = time;
      }
      end = time;
      
      if ((idx[m_sid] != m_idx) && (m_idx != 0))
          System.out.println("receiver#" + sid + '.' + current + "(" + m_sid + ", " + m_idx + ") should be " + idx[sid]);
      idx[m_sid] = m_idx + 1;
      
      nbmsg += 1;
    } catch (JMSException exc) {
      exc.printStackTrace();
    }
  }

  class SendTask extends TimerTask {
    long dt = 0;
    int loop = 0;
    
    Session session;
    MessageProducer producer;
    
    SendTask(Session session, MessageProducer producer) {
      this.session = session;
      this.producer = producer;
    }
    
    public void send(int idx) throws JMSException {
      TextMessage msg = session.createTextMessage("Server#" + sid + " updated " + loop + '.' + idx);
      msg.setIntProperty("sid", sid);
      msg.setIntProperty("loop", loop);
      msg.setIntProperty("idx", idx);
      producer.send(msg);
    }
    
    public void run() {
      long start, end;
      
      try {
        if (loop < nbLoop) {
          start = System.currentTimeMillis();
          for (int idx=0; idx<msgPerLoop; idx++)
            send(idx);
          loop += 1;
          end = System.currentTimeMillis();
          System.out.println("sender#" + sid + '.' + loop + " - " + (end/1000) + ' ' + msgPerLoop + '/' + (end -start));
          dt += (end - start);
        } else {
          // just send a unique message to display the statistics of previous round
          send(0);
        }
      } catch (JMSException exc) {
        exc.printStackTrace();
      }
    }
  }
}

