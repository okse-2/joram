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
package joram.studycase.odis;

import javax.jms.*;

import fr.dyade.aaa.agent.AgentServer;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

/**
 *
 */
public class Test1 {
  static int NbRound = 10;
  static int NbMsgPerRound = 100;

  static ConnectionFactory cf = null;

  static String host = "localhost";
  static int port = 16010;

  public static void main(String[] args) throws Exception {
    new Test1().run(args);
  }

  protected void AdminConnect() throws Exception {
    AdminModule.collocatedConnect("root", "root");
//     AdminModule.connect(host, port, "root", "root", 60);
  }

  protected ConnectionFactory createConnectionFactory() throws Exception {
    return new org.objectweb.joram.client.jms.local.LocalConnectionFactory();
//     return TcpConnectionFactory.create(host, port);
  }

  // Creating JNDI context.
  javax.naming.Context jndiCtx = null;

  String username = "anonymous";
  String password = "anonymous";

  String acquiringQueueName= "acquiringQueue";
  Queue acquiringQueue = null;

  String rawQueueName= "rawQueue";
  Queue rawQueue = null;

  String processingQueueName= "processingQueue";
  Queue processingQueue = null;

  String prettyTopicName= "prettyTopic";
  Topic prettyTopic = null;

  String eventTopicName= "eventTopic";
  Topic eventTopic = null;

  protected void createDestinations(javax.naming.Context jndiCtx) throws Exception {
    // Connecting to Joram server.
    AdminConnect();

    // Creating default user
    User user = User.create(username, password, 0);

    // Creating acquiringQueue.
    acquiringQueue = AdminTest1.createQueue(acquiringQueueName, jndiCtx);
    // Creating rawQueue.
    rawQueue = AdminTest1.createQueue(rawQueueName, jndiCtx);
    // Creating processingQueue.
    processingQueue = AdminTest1.createQueue(processingQueueName, jndiCtx);
    // Creating prettyTopic.
    prettyTopic = AdminTest1.createTopic(prettyTopicName, jndiCtx);
    // Creating eventTopic.
    eventTopic = AdminTest1.createTopic(eventTopicName, jndiCtx);

    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
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

    host = System.getProperty("hostname", host);
    port = Integer.getInteger("port", port).intValue();

    System.out.println("====================================================");
    System.out.println("Transaction: " + System.getProperty("Transaction"));
    System.out.println("Engine: " + System.getProperty("Engine"));
    System.out.println("====================================================");

    try {
      cf =  createConnectionFactory();
//    jndiCtx = new javax.naming.InitialContext();
      createDestinations(jndiCtx);
      Connection cnx = cf.createConnection();

      Dispatcher dispatcher = new Dispatcher(cnx);
      Filter filter = new Filter(cnx);
      Recorder recorder = new Recorder(cnx);

      cnx.start();

      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(acquiringQueue);

      for (int i=0; i<NbRound; i++) {
        for (int j=0; j<NbMsgPerRound; j++) {
          Message msg = session.createObjectMessage();
          ((ObjectMessage) msg).setObject(null);
          msg.setIntProperty("index", ((i * NbMsgPerRound) + j));
          producer.send(msg);
        }
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      System.exit(-1);
    } finally {
      
    }

    System.exit(0);
  }

  /**
   *
   */
  class Dispatcher implements MessageListener {
    Connection cnx = null;
    Session session = null;
    MessageConsumer acquiringQueueConsumer = null;
    MessageProducer rawQueueProducer = null;
    MessageProducer processingQueueProducer = null;

    Dispatcher(Connection cnx) throws JMSException {
      this.cnx = cnx;      

      try {
        session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
        acquiringQueueConsumer = session.createConsumer(acquiringQueue);
        acquiringQueueConsumer.setMessageListener(this);
        rawQueueProducer = session.createProducer(rawQueue);
        processingQueueProducer = session.createProducer(processingQueue);
      } catch (Exception exc) {
        exc.printStackTrace();
        System.exit(-1);
      }
    }

    public synchronized void onMessage(Message msg) {
      try {
        ObjectMessage om = (ObjectMessage) msg;
        int index = msg.getIntProperty("index");

        Message msg2 = session.createMessage();
        msg2.setIntProperty("index", index);
        
        rawQueueProducer.send(msg2);

        Message msg3 = session.createMessage();
        msg3.setIntProperty("index", index);
 
        processingQueueProducer.send(msg3);
      } catch (Exception exc) {
        exc.printStackTrace();
      }
    }

    void close()  throws JMSException {
      acquiringQueueConsumer.close();
      rawQueueProducer.close();
      processingQueueProducer.close();
      session.close();
    }
  }

  /**
   *
   */
  class Filter implements MessageListener {
    Connection cnx = null;
    Session session = null;
    MessageConsumer processingQueueConsumer = null;
    MessageProducer prettyTopicProducer = null;

    Filter(Connection cnx) throws JMSException {
      this.cnx = cnx;      

      try {
        session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
        processingQueueConsumer = session.createConsumer(processingQueue);
        processingQueueConsumer.setMessageListener(this);
        prettyTopicProducer = session.createProducer(prettyTopic);
      } catch (Exception exc) {
        exc.printStackTrace();
        System.exit(-1);
      }
    }

    int current = 0;
    void filter(Message msg) throws JMSException {
      int index = msg.getIntProperty("index");
      if (index != current)
        System.out.println("filter, bad message:" + index);
      current = index +1;
      if ((current % 100) == 0)
        System.out.println("filter: " + current);
    }

    public synchronized void onMessage(Message msg) {
      try {
        filter(msg);
        prettyTopicProducer.send(msg);
      } catch (Exception exc) {
        exc.printStackTrace();
      }
    }

    void close() throws JMSException {
      processingQueueConsumer.close();
      prettyTopicProducer.close();
      session.close();
    }
  }

  /**
   *
   */
  class Recorder implements MessageListener {
    Connection cnx = null;
    Session session = null;
    MessageConsumer rawQueueConsumer = null;

    Recorder(Connection cnx) throws JMSException {
      this.cnx = cnx;      

      try {
        session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
        rawQueueConsumer = session.createConsumer(rawQueue);
        rawQueueConsumer.setMessageListener(this);
      } catch (Exception exc) {
        exc.printStackTrace();
        System.exit(-1);
      }
    }

    int current = 0;
    void record(Message msg) throws JMSException {
      int index = msg.getIntProperty("index");
      if (index != current)
        System.out.println("recorder, bad message:" + index);
      current = index +1;
      if ((current % 100) == 0)
        System.out.println("recorder: " + current);
    }

    public synchronized void onMessage(Message msg) {
      try {
        record(msg);
      } catch (Exception exc) {
        exc.printStackTrace();
      }
    }

    void close()  throws JMSException {
      rawQueueConsumer.close();
      session.close();
    }
  }

}
