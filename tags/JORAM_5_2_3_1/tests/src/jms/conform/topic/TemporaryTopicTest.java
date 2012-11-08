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

package jms.conform.topic;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.TopicSubscriber;

import jms.framework.PubSubTestCase;
import jms.framework.TestConfig;


/**
 * Test the <code>javax.jms.TemporaryTopic</code> features.
 */
public class TemporaryTopicTest extends PubSubTestCase {

  private TemporaryTopic tempTopic;
  private TopicSubscriber tempSubscriber;

  /**
   * Test a TemporaryTopic
   */
  public void testTemporaryTopic() {
    try {
      // we stop both publisher and subscriber connections
//      publisherConnection.stop();
//      subscriberConnection.stop();

      // we create a temporary topic to receive messages
      tempTopic = subscriberSession.createTemporaryTopic();
      // we recreate the sender because it has been
      // already created with another Destination as parameter
      publisher = publisherSession.createPublisher(tempTopic);
      // we create a temporary subscriber on the temporary topic
      tempSubscriber = subscriberSession.createSubscriber(tempTopic);
      subscriberConnection.start();
      publisherConnection.start();

      TextMessage message = publisherSession.createTextMessage();
      message.setText("testTemporaryTopic");
      publisher.publish(message);

      Message m = tempSubscriber.receive(TestConfig.TIMEOUT);
      assertTrue(m instanceof TextMessage);
      TextMessage msg = (TextMessage) m;
      assertEquals("testTemporaryTopic", msg.getText());
    } catch (JMSException e) {
      fail(e);
    }
  }

  public static void main(String[] args) {
    run(new TemporaryTopicTest());
  }
}
