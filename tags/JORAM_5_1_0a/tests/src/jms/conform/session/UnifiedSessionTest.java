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

package jms.conform.session;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import jms.conform.connection.ConnectionTest;
import jms.framework.UnifiedTestCase;


/**
 * Test unified JMS 1.1 sessions. <br />
 * See JMS 1.1 specifications
 * 
 * @since JMS 1.1
 */
public class UnifiedSessionTest extends UnifiedTestCase {

  /**
   * QueueConnection
   */
  protected QueueConnection queueConnection;

  /**
   * QueueSession (non transacted, AUTO_ACKNOWLEDGE)
   */
  protected QueueSession queueSession;

  /**
   * TopicConnection
   */
  protected TopicConnection topicConnection;

  /**
   * TopicSession (non transacted, AUTO_ACKNOWLEDGE)
   */
  protected TopicSession topicSession;

  /**
   * Test that a call to <code>createDurableConnectionConsumer()</code> method
   * on a <code>QueueConnection</code> throws a
   * <code>javax.jms.IllegalStateException</code>. (see JMS 1.1 specs, table
   * 4-1).
   * 
   * @since JMS 1.1
   */
  public void testCreateDurableConnectionConsumerOnQueueConnection() {
    try {
      queueConnection.createDurableConnectionConsumer(topic,
          "subscriptionName", "", (ServerSessionPool) null, 1);
      fail("Should throw a javax.jms.IllegalStateException");
    } catch (javax.jms.IllegalStateException e) {
    } catch (JMSException e) {
      fail("Should throw a javax.jms.IllegalStateException, not a " + e);
    }
  }

  /**
   * Test that a call to <code>createDurableSubscriber()</code> method on a
   * <code>QueueSession</code> throws a
   * <code>javax.jms.IllegalStateException</code>. (see JMS 1.1 specs, table
   * 4-1).
   * 
   * @since JMS 1.1
   */
  public void testCreateDurableSubscriberOnQueueSession() {
    try {
      queueSession.createDurableSubscriber(topic, "subscriptionName");
      fail("Should throw a javax.jms.IllegalStateException");
    } catch (javax.jms.IllegalStateException e) {
    } catch (JMSException e) {
      fail("Should throw a javax.jms.IllegalStateException, not a " + e);
    }
  }

  /**
   * Test that a call to <code>createTemporaryTopic()</code> method on a
   * <code>QueueSession</code> throws a
   * <code>javax.jms.IllegalStateException</code>. (see JMS 1.1 specs, table
   * 4-1).
   * 
   * @since JMS 1.1
   */
  public void testCreateTemporaryTopicOnQueueSession() {
    try {
      TemporaryTopic tempTopic = queueSession.createTemporaryTopic();
      fail("Should throw a javax.jms.IllegalStateException");
    } catch (javax.jms.IllegalStateException e) {
    } catch (JMSException e) {
      fail("Should throw a javax.jms.IllegalStateException, not a " + e);
    }
  }

  /**
   * Test that a call to <code>createTopic()</code> method on a
   * <code>QueueSession</code> throws a
   * <code>javax.jms.IllegalStateException</code>. (see JMS 1.1 specs, table
   * 4-1).
   * 
   * @since JMS 1.1
   */
  public void testCreateTopicOnQueueSession() {
    try {
      Topic tempTopic = queueSession.createTopic("topic_name");
      fail("Should throw a javax.jms.IllegalStateException");
    } catch (javax.jms.IllegalStateException e) {
    } catch (JMSException e) {
      fail("Should throw a javax.jms.IllegalStateException, not a " + e);
    }
  }

  /**
   * Test that a call to <code>unsubscribe()</code> method on a
   * <code>QueueSession</code> throws a
   * <code>javax.jms.IllegalStateException</code>. (see JMS 1.1 specs, table
   * 4-1).
   * 
   * @since JMS 1.1
   */
  public void testUnsubscribeOnQueueSession() {
    try {
      queueSession.unsubscribe("subscriptionName");
      fail("Should throw a javax.jms.IllegalStateException");
    } catch (javax.jms.IllegalStateException e) {
    } catch (JMSException e) {
      fail("Should throw a javax.jms.IllegalStateException, not a " + e);
    }
  }

  /**
   * Test that a call to <code>createBrowser()</code> method on a
   * <code>TopicSession</code> throws a
   * <code>javax.jms.IllegalStateException</code>. (see JMS 1.1 specs, table
   * 4-1).
   * 
   * @since JMS 1.1
   */
  public void testCreateBrowserOnTopicSession() {
    try {
      QueueBrowser queueBrowser = topicSession.createBrowser(queue);
      fail("Should throw a javax.jms.IllegalStateException");
    } catch (javax.jms.IllegalStateException e) {
    } catch (JMSException e) {
      fail("Should throw a javax.jms.IllegalStateException, not a " + e);
    }
  }

  /**
   * Test that a call to <code>createQueue()</code> method on a
   * <code>TopicSession</code> throws a
   * <code>javax.jms.IllegalStateException</code>. (see JMS 1.1 specs, table
   * 4-1).
   * 
   * @since JMS 1.1
   */
  public void testCreateQueueOnTopicSession() {
    try {
      Queue tempQueue = topicSession.createQueue("queue_name");
      fail("Should throw a javax.jms.IllegalStateException");
    } catch (javax.jms.IllegalStateException e) {
    } catch (JMSException e) {
      fail("Should throw a javax.jms.IllegalStateException, not a " + e);
    }
  }

  /**
   * Test that a call to <code>createTemporaryQueue()</code> method on a
   * <code>TopicSession</code> throws a
   * <code>javax.jms.IllegalStateException</code>. (see JMS 1.1 specs, table
   * 4-1).
   * 
   * @since JMS 1.1
   */
  public void testCreateTemporaryQueueOnTopicSession() {
    try {
      TemporaryQueue tempQueue = topicSession.createTemporaryQueue();
      fail("Should throw a javax.jms.IllegalStateException");
    } catch (javax.jms.IllegalStateException e) {
    } catch (JMSException e) {
      fail("Should throw a javax.jms.IllegalStateException, not a " + e);
    }
  }

  public void setUp() {
    super.setUp();
    try {
      queueConnection = queueConnectionFactory.createQueueConnection();
      queueSession = queueConnection.createQueueSession(false,
          Session.AUTO_ACKNOWLEDGE);
      topicConnection = topicConnectionFactory.createTopicConnection();
      topicSession = topicConnection.createTopicSession(false,
          Session.AUTO_ACKNOWLEDGE);

      queueConnection.start();
      topicConnection.start();
    } catch (Exception e) {
      // XXX
      e.printStackTrace();
    }
  }

  public void tearDown() {
    try {
      queueConnection.close();
      topicConnection.close();
    } catch (Exception e) {
      // XXX
      e.printStackTrace();
    } finally {
      queueConnection = null;
      queueSession = null;
      topicConnection = null;
      topicSession = null;
      super.tearDown();
    }
  }

  public static void main(String[] args) {
    run(new UnifiedSessionTest());
  }
}
