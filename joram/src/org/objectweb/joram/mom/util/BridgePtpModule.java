/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s):
 */
package org.objectweb.joram.mom.util;

import javax.jms.*;
import javax.jms.IllegalStateException;

import java.util.Vector;


/**
 * The <code>BridgePtpModule</code> class is a bridge module based on the JMS
 * PTP semantics and classes.
 */
public class BridgePtpModule extends BridgeUnifiedModule
{
  /** Specific PTP producer resource. */
  private transient QueueSender sender;


  /** Constructs a <code>BridgePtpModule</code> module. */
  public BridgePtpModule()
  {
    super();
  }

 
  /**
   * Sends a message to the foreign JMS queue.
   *
   * @exception javax.jms.IllegalStateException  If the module's state does
   *              not permit message sendings.
   * @exception javax.jms.MessageFormatException  If the MOM message could not
   *              be converted into a foreign JMS message.
   */
  public void send(org.objectweb.joram.shared.messages.Message message)
              throws JMSException
  {
    if (! usable)
      throw new IllegalStateException(notUsableMessage);

    try {
      sender.send(MessageConverterModule.convert(producerSession, message));
      acknowledge(message);
    }
    catch (javax.jms.MessageFormatException exc) {
      throw exc;
    }
    // Connection failure? Keeping the message for later delivery.
    catch (javax.jms.JMSException exc) {
      qout.add(message);
    }
  }

  /**
   * Opens a connection with the foreign JMS server and creates the
   * JMS PTP resources for interacting with the foreign JMS queue. 
   *
   * @exception JMSException  If the needed JMS resources could not be created.
   */
  protected void doConnect() throws JMSException
  {
    QueueConnectionFactory queueCnxFact = (QueueConnectionFactory) cnxFact;
    Queue queue = (Queue) dest;

    if (userName != null && password != null)
      cnx = queueCnxFact.createQueueConnection(userName, password);
    else
      cnx = queueCnxFact.createQueueConnection();
    cnx.setExceptionListener(this);   

    if (clientID != null)
      cnx.setClientID(clientID);

    producerSession =
      ((QueueConnection) cnx).createQueueSession(false,
                                                 Session.AUTO_ACKNOWLEDGE);
    sender = ((QueueSession) producerSession).createSender(queue);

    consumerSession = ((QueueConnection) cnx).createQueueSession(true, 0);
    consumer = ((QueueSession) consumerSession).createReceiver(queue,
                                                               selector);
  }
}
