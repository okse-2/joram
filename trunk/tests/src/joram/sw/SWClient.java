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

import java.util.List;
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
 *
 */
public class SWClient extends TestCase implements MessageListener {
  int sid;
  int nbmsg = 0;
  
  static long period = 300000;
  static int msgPerLoop = 1000;
  static int nbLoop = 500;
  
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

      List<Topic> cluster = local.getClusterFellows();
      for (int i=0; i<cluster.size(); i++) {
        Topic element = cluster.get(i);
        System.out.println("#" + sid + " - " + element);
      }
      
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
  
  /**
   * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
   */
  public void onMessage(Message m) {
    try {
      TextMessage msg = (TextMessage) m;
      int loop = msg.getIntProperty("loop");
      int idx = msg.getIntProperty("idx");
      
      long time = System.currentTimeMillis();
      if (loop != current) {
        if (current != -1) {
          System.out.println("receiver#" + sid + '.' + current + " - " + (end/1000) + ' ' + nbmsg + '/' + (end -start));
          dt += (end - start);
        }
        current = loop;
        synchronized(lock) {
          if (current == nbLoop) lock.notify();
        }
        start = time;
      }
      end = time;
      
//      System.out.println("#" + sid + " - " + msg.getText());
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
    
    public void run() {
      long start, end;
      TextMessage msg;
      
      try {
        start = System.currentTimeMillis();
        for (int idx=0; idx<msgPerLoop; idx++) {
          msg = session.createTextMessage("Server#" + sid + " updated " + loop + '.' + idx);
          msg.setIntProperty("sid", sid);
          msg.setIntProperty("loop", loop);
          msg.setIntProperty("idx", idx);
          producer.send(msg);
        }
        loop += 1;
        end = System.currentTimeMillis();
        System.out.println("sender#" + sid + '.' + loop + " - " + (end/1000) + ' ' + msgPerLoop + '/' + (end -start));
        dt += (end - start);
      } catch (JMSException exc) {
        exc.printStackTrace();
      }
    }
  }
}

