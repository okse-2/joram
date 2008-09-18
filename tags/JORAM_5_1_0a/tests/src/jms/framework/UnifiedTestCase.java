/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2002 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2002 INRIA
 * Contact: joram-team@objectweb.org
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
 * Initial developer(s): Jeff Mesnil (Inria)
 * Contributor(s): Nicolas Tachker (ScalAgent D.T.)
 */

package jms.framework;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;

import jms.admin.Admin;
import jms.admin.AdminFactory;
import jms.providers.admin.JoramAdmin;


/**
 * Creates convenient Unified JMS 1.1 objects which can be needed for tests.
 * <br />
 * This class defines the setUp and tearDown methods so that JMS administrated
 * objects and other "ready to use" JMS objects (that is to say destinations,
 * sessions, producers and consumers) are available conveniently for the test
 * cases. <br />
 * Classes which want that convenience should extend
 * <code>UnifiedTestCase</code> instead of <code>JMSTestCase</code>.
 * 
 * @since JMS 1.1
 */
public class UnifiedTestCase extends JMSTestCase {

  protected Admin admin;
  //protected InitialContext ctx;
  private static final String CF_NAME = "testCF";
  private static final String TCF_NAME = "testTCF";
  private static final String QCF_NAME = "testQCF";
  private static final String DESTINATION_NAME = "testDestination";
  private static final String QUEUE_NAME = "testQueue";
  private static final String TOPIC_NAME = "testTopic";

  // //////////////////
  // Unified Domain //
  // //////////////////

  /**
   * Destination used by a producer
   */
  protected Destination producerDestination;

  /**
   * Producer
   */
  protected MessageProducer producer;

  /**
   * ConnectionFactory of the producer
   */
  protected ConnectionFactory producerCF;

  /**
   * Connection of the producer
   */
  protected Connection producerConnection;

  /**
   * Session of the producer (non transacted, AUTO_ACKNOWLEDGE)
   */
  protected Session producerSession;

  /**
   * Destination used by a consumer
   */
  protected Destination consumerDestination;

  /**
   * Consumer on destination
   */
  protected MessageConsumer consumer;

  /**
   * ConnectionFactory of the consumer
   */
  protected ConnectionFactory consumerCF;

  /**
   * Connection of the consumer
   */
  protected Connection consumerConnection;

  /**
   * Session of the consumer (non transacted, AUTO_ACKNOWLEDGE)
   */
  protected Session consumerSession;

  // //////////////
  // PTP Domain //
  // //////////////

  /**
   * QueueConnectionFactory
   */
  protected QueueConnectionFactory queueConnectionFactory;

  /**
   * Queue
   */
  protected Queue queue;

  // //////////////////
  // Pub/Sub Domain //
  // //////////////////

  /**
   * TopicConnectionFactory
   */
  protected TopicConnectionFactory topicConnectionFactory;

  /**
   * Topic
   */
  protected Topic topic;

  /**
   * Create all administrated objects connections and sessions ready to use for
   * tests. <br />
   * Start connections.
   */
  protected void setUp() {
    try {
      // Admin step
      // gets the provider administration wrapper...
      if (admin == null) {
        admin = new JoramAdmin();
      }
      // ...and creates administrated objects

      producerCF = admin.createConnectionFactory(CF_NAME);
      // we see destination of the unified domain as a javax.jms.Destination
      // instead of a javax.jms.Queue to be more generic
      producerDestination = (Destination) admin.createQueue(DESTINATION_NAME);
      producerConnection = producerCF.createConnection();
      producerSession = producerConnection.createSession(false,
          Session.AUTO_ACKNOWLEDGE);
      producer = producerSession.createProducer(producerDestination);

      consumerCF = admin.createConnectionFactory(CF_NAME);
      // we see destination of the unified domain as a javax.jms.Destination
      // instead of a javax.jms.Queue to be more generic
      consumerDestination = (Destination) admin.createQueue(DESTINATION_NAME);
      consumerConnection = consumerCF.createConnection();
      consumerSession = consumerConnection.createSession(false,
          Session.AUTO_ACKNOWLEDGE);
      consumer = consumerSession.createConsumer(consumerDestination);

      queueConnectionFactory = admin.createQueueConnectionFactory(QCF_NAME);
      queue = admin.createQueue(QUEUE_NAME);

      topicConnectionFactory = admin.createTopicConnectionFactory(TCF_NAME);
      topic = admin.createTopic(TOPIC_NAME);
      
      producerConnection.start();
      consumerConnection.start();
      // end of client step
    } catch (Exception e) {
      // XXX
      e.printStackTrace();
    }
  }

  /**
   * Close connections and delete administrated objects
   */
  protected void clear() {
    try {
      if (consumerConnection != null)
        consumerConnection.close();
      if (producerConnection != null)
        producerConnection.close();

      if (admin != null) {
        admin.deleteConnectionFactory(CF_NAME);
        admin.deleteQueueConnectionFactory(QCF_NAME);
        admin.deleteTopicConnectionFactory(TCF_NAME);
        admin.deleteQueue(consumerDestination);
        admin.deleteQueue(queue);
        admin.deleteTopic(topic);
        admin.disconnect();
        admin = null;
      }
    } catch (Exception e) {
      // XXX
      e.printStackTrace();
    } finally {
      producerDestination = null;
      producer = null;
      producerCF = null;
      producerSession = null;
      producerConnection = null;

      consumerDestination = null;
      consumer = null;
      consumerCF = null;
      consumerSession = null;
      consumerConnection = null;

      queueConnectionFactory = null;
      queue = null;

      topicConnectionFactory = null;
      topic = null;
    }
  }
}
