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

package jms.conform.queue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueReceiver;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import jms.conform.connection.ConnectionTest;
import jms.framework.PTPTestCase;
import jms.framework.TestConfig;


/**
 * Test the <code>javax.jms.TemporaryQueue</code> features.
 */
public class TemporaryQueueTest extends PTPTestCase {

  private TemporaryQueue tempQueue;
  private QueueReceiver tempReceiver;

  /**
   * Test a TemporaryQueue
   */
  public void testTemporaryQueue() {
    try {
      // we stop both sender and receiver connections
      senderConnection.stop();
      receiverConnection.stop();
      // we create a temporary queue to receive messages
      tempQueue = receiverSession.createTemporaryQueue();
      // we recreate the sender because it has been
      // already created with a Destination as parameter
      sender = senderSession.createSender(null);
      // we create a receiver on the temporary queue
      tempReceiver = receiverSession.createReceiver(tempQueue);
      receiverConnection.start();
      senderConnection.start();

      TextMessage message = senderSession.createTextMessage();
      message.setText("testTemporaryQueue");
      sender.send(tempQueue, message);

      Message m = tempReceiver.receive(TestConfig.TIMEOUT);
      assertTrue(m instanceof TextMessage);
      TextMessage msg = (TextMessage) m;
      assertEquals("testTemporaryQueue", msg.getText());
    } catch (JMSException e) {
      fail(e);
    }
  }

  public static void main(String[] args) {
    run(new TemporaryQueueTest());
  }
}
