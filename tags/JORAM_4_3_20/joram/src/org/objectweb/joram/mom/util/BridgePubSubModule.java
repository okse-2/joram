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
 * The <code>BridgePubSubModule</code> class is a bridge module based on the
 * JMS Publish/Subscribe semantics and classes.
 */
public class BridgePubSubModule extends BridgeUnifiedModule
{
  /** Specific Pub/Sub producer resource. */
  private transient TopicPublisher publisher;


  /** Constructs a <code>BridgePubSubModule</code> module. */
  public BridgePubSubModule()
  {
    super();
  }

 
  /**
   * Sends a message to the foreign JMS topic.
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
      publisher.publish(MessageConverterModule.convert(producerSession,
                                                       message));
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
   * JMS Pub/Sub resources for interacting with the foreign JMS topic. 
   *
   * @exception JMSException  If the needed JMS resources could not be created.
   */
  protected void doConnect() throws JMSException
  {
    TopicConnectionFactory topicCnxFact = (TopicConnectionFactory) cnxFact;
    Topic topic = (Topic) dest;

    if (userName != null && password != null)
      cnx = topicCnxFact.createTopicConnection(userName, password);
    else
      cnx = topicCnxFact.createTopicConnection();
    cnx.setExceptionListener(this);

    if (clientID != null)
      cnx.setClientID(clientID);

    producerSession =
      ((TopicConnection) cnx).createTopicSession(false,
                                                 Session.AUTO_ACKNOWLEDGE);
    publisher = ((TopicSession) producerSession).createPublisher(topic);

    consumerSession = ((TopicConnection) cnx).createTopicSession(true, 0);
    consumer = consumerSession.createDurableSubscriber((Topic) dest,
                                                       agentId.toString(),
                                                       selector,
                                                       false);
  }
}
