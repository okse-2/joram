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

import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;

import jms.admin.Admin;
import jms.admin.AdminFactory;
import jms.providers.admin.JoramAdmin;


/**
 * Creates convenient JMS Publish/Subscribe objects which can be needed for
 * tests. <br />
 * This class defines the setUp and tearDown methods so that JMS administrated
 * objects and other "ready to use" Pub/Sub objects (that is to say topics,
 * sessions, publishers and subscribers) are available conveniently for the test
 * cases. <br />
 * Classes which want that convenience should extend <code>PubSubTestCase</code>
 * instead of <code>JMSTestCase</code>.
 * 
 */
public class PubSubTestCase extends JMSTestCase {

  private Admin admin;
  //private InitialContext ctx;
  private static final String TCF_NAME = "testTCF";
  private static final String TOPIC_NAME = "testTopic";

  /**
   * Topic used by a publisher
   */
  protected Topic publisherTopic;

  /**
   * Publisher on queue
   */
  protected TopicPublisher publisher;

  /**
   * TopicConnectionFactory of the publisher
   */
  protected TopicConnectionFactory publisherTCF;

  /**
   * TopicConnection of the publisher
   */
  protected TopicConnection publisherConnection;

  /**
   * TopicSession of the publisher (non transacted, AUTO_ACKNOWLEDGE)
   */
  protected TopicSession publisherSession;

  /**
   * Topic used by a subscriber
   */
  protected Topic subscriberTopic;

  /**
   * Subscriber on queue
   */
  protected TopicSubscriber subscriber;

  /**
   * TopicConnectionFactory of the subscriber
   */
  protected TopicConnectionFactory subscriberTCF;

  /**
   * TopicConnection of the subscriber
   */
  protected TopicConnection subscriberConnection;

  /**
   * TopicSession of the subscriber (non transacted, AUTO_ACKNOWLEDGE)
   */
  protected TopicSession subscriberSession;

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
      
      if (publisherTCF == null)
        publisherTCF = admin.createTopicConnectionFactory(TCF_NAME);
      if (publisherTopic == null)
        publisherTopic = admin.createTopic(TOPIC_NAME);

      publisherConnection = publisherTCF.createTopicConnection();
      if (publisherConnection.getClientID() == null) {
        publisherConnection.setClientID("publisherConnection");
      }

      publisherSession = publisherConnection.createTopicSession(false,
          Session.AUTO_ACKNOWLEDGE);
      publisher = publisherSession.createPublisher(publisherTopic);

      if (subscriberTCF == null)
        subscriberTCF = admin.createTopicConnectionFactory(TCF_NAME);
      if (subscriberTopic == null)
        subscriberTopic = admin.createTopic(TOPIC_NAME);
      
      subscriberConnection = subscriberTCF.createTopicConnection();
      if (subscriberConnection.getClientID() == null) {
        subscriberConnection.setClientID("subscriberConnection");
      }

      subscriberSession = subscriberConnection.createTopicSession(false,
          Session.AUTO_ACKNOWLEDGE);
      subscriber = subscriberSession.createSubscriber(subscriberTopic);

      publisherConnection.start();
      subscriberConnection.start();
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
      if (publisherConnection != null)
        publisherConnection.close();
      if (subscriberConnection != null)
        subscriberConnection.close();

      if (admin != null) {
        admin.deleteTopicConnectionFactory(TCF_NAME);
        admin.deleteTopic(subscriberTopic);
        admin.disconnect();
        admin = null;
      }
    } catch (Exception e) {
      // XXX 
      e.printStackTrace();
    } finally {
      publisherTopic = null;
      publisher = null;
      publisherTCF = null;
      publisherSession = null;
      publisherConnection = null;

      subscriberTopic = null;
      subscriber = null;
      subscriberTCF = null;
      subscriberSession = null;
      subscriberConnection = null;
    }
  }

}
