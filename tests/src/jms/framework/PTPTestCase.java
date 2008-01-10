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

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;

import jms.admin.Admin;
import jms.providers.admin.JoramAdmin;


/**
 * Creates convenient Point to Point JMS objects which can be needed for tests.
 * <br />
 * This class defines the setUp and tearDown methods so that JMS administrated
 * objects and other "ready to use" PTP objects (that is to say queues,
 * sessions, senders and receviers) are available conveniently for the test
 * cases. <br />
 * Classes which want that convenience should extend <code>PTPTestCase</code>
 * instead of <code>JMSTestCase</code>.
 * 
 */
public class PTPTestCase extends JMSTestCase {

  private Admin admin;
  private static final String QCF_NAME = "testQCF";
  private static final String QUEUE_NAME = "testQueue";

  /**
   * Queue used by a sender
   */
  protected Queue senderQueue;

  /**
   * Sender on queue
   */
  protected QueueSender sender;

  /**
   * QueueConnectionFactory of the sender
   */
  protected QueueConnectionFactory senderQCF;

  /**
   * QueueConnection of the sender
   */
  protected QueueConnection senderConnection;

  /**
   * QueueSession of the sender (non transacted, AUTO_ACKNOWLEDGE)
   */
  protected QueueSession senderSession;

  /**
   * Queue used by a receiver
   */
  protected Queue receiverQueue;

  /**
   * Receiver on queue
   */
  protected QueueReceiver receiver;

  /**
   * QueueConnectionFactory of the receiver
   */
  protected QueueConnectionFactory receiverQCF;

  /**
   * QueueConnection of the receiver
   */
  protected QueueConnection receiverConnection;

  /**
   * QueueSession of the receiver (non transacted, AUTO_ACKNOWLEDGE)
   */
  protected QueueSession receiverSession;

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

      if (senderQCF == null)
        senderQCF = admin.createQueueConnectionFactory(QCF_NAME);
      if (senderQueue == null)
        senderQueue = admin.createQueue(QUEUE_NAME);

      senderConnection = senderQCF.createQueueConnection();
      senderSession = senderConnection.createQueueSession(false,
          Session.AUTO_ACKNOWLEDGE);
      sender = senderSession.createSender(senderQueue);

      if (receiverQCF == null)
        receiverQCF = admin.createQueueConnectionFactory(QCF_NAME);
      if (receiverQueue == null)
        receiverQueue = admin.createQueue(QUEUE_NAME);
      
      receiverConnection = receiverQCF.createQueueConnection();
      receiverSession = receiverConnection.createQueueSession(false,
          Session.AUTO_ACKNOWLEDGE);
      receiver = receiverSession.createReceiver(receiverQueue);

      senderConnection.start();
      receiverConnection.start();
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
      if (senderConnection != null)
        senderConnection.close();

      if (receiverConnection != null)
        receiverConnection.close();

      if (admin != null) {
        admin.deleteQueueConnectionFactory(QCF_NAME);
        admin.deleteQueue(senderQueue);
        admin.disconnect();
        admin = null;
      }
    } catch (Exception e) {
      // XXX 
      e.printStackTrace();
    } finally {
      senderQueue = null;
      sender = null;
      senderQCF = null;
      senderSession = null;
      senderConnection = null;

      receiverQueue = null;
      receiver = null;
      receiverQCF = null;
      receiverSession = null;
      receiverConnection = null;
    }
  }

  public Admin getJoramAdmin() {
    return admin;
  }
}
