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

import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.dyade.aaa.common.Debug;

@Component
public class Consumer implements MessageListener {

  private static final Logger logger = Debug.getLogger(Consumer.class.getName());

  @Autowired
  private JmsTemplate template = null;
  
  /**
   * Implementation of <code>MessageListener</code>.
   */
  public void onMessage(Message message) {
    try {
      int messageCount = message.getIntProperty(Producer.MESSAGE_COUNT);

      if (message instanceof TextMessage) {
        TextMessage tm = (TextMessage) message;
        String msg = tm.getText();
        logger.log(BasicLevel.DEBUG, "Processed message "+ msg);
      }
    } catch (JMSException e) {
      logger.log(BasicLevel.ERROR, e.getMessage(), e);
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
}