/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
package org.ow2.joram.spring.sample;

import javax.annotation.PostConstruct;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import fr.dyade.aaa.common.Debug;

@Component
public class Producer {

  private static final Logger logger = Debug.getLogger(Producer.class.getName());

  protected static final String MESSAGE_COUNT = "messageCount";

  @Autowired
  private JmsTemplate template = null;
  @Autowired
  private int messageCount = 100;
  @Autowired
  private Destination destination = null;

  /**
   * Generates JMS messages
   */
  @PostConstruct
  public void generateMessages() throws JMSException {
    for (int i = 0; i < messageCount; i++) {
      final int index = i;
      final String text = "Message number is " + i + ".";

      template.send(new MessageCreator() {
        public Message createMessage(Session session) throws JMSException {
          TextMessage message = session.createTextMessage(text);
          message.setIntProperty(MESSAGE_COUNT, index);

          logger.log(BasicLevel.DEBUG, "Sending message: " + text);

          return message;
        }
      });
    }
  }

  /**
   * @return the template
   */
  public JmsTemplate getTemplate() {
    return template;
  }

  /**
   * @param template the template to set
   */
  public void setTemplate(JmsTemplate template) {
    this.template = template;
  }

  /**
   * @return the messageCount
   */
  public int getMessageCount() {
    return messageCount;
  }

  /**
   * @param messageCount the messageCount to set
   */
  public void setMessageCount(int messageCount) {
    this.messageCount = messageCount;
  }

  /**
   * @return the destination
   */
  public Destination getDestination() {
    return destination;
  }

  /**
   * @param destination the destination to set
   */
  public void setDestination(Destination destination) {
    this.destination = destination;
  }
}