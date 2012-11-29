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
package org.ow2.joram.spring;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminItf;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import fr.dyade.aaa.common.Debug;

/**
 * Parser to the joram:queue xml entry.
 */
public class JoramQueueBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  public static final Logger logger = Debug.getLogger(JoramQueueBeanDefinitionParser.class.getName());
  
  /* (non-Javadoc)
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
   */
  protected Class getBeanClass(Element element) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramQueueBeanDefinitionParser.getBeanClass(" + element + ')');
    return JoramDestination.class;
  }

  /**
   * Create a Joram queue.
   * Add the destination property value to the bean.
   * 
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder)
   */
  protected void doParse(Element element, BeanDefinitionBuilder bean) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramQueueBeanDefinitionParser.doParse(" + element + ", " + bean + ')');
    
    // this however is an required property
    String name = element.getAttribute("name");
    
    // this however is an optional property
    String sid = element.getAttribute("sid");
    if (!StringUtils.hasText(sid)) {
      sid = "0";
    }
    
    // this however is an optional property
    String dmq = element.getAttribute("dmq");
    
    // this however is an optional property
    String dmqSid = element.getAttribute("dmqSid");
    if (!StringUtils.hasText(dmqSid)) {
      dmqSid = sid;
    }
    
    String deleteOnStop = element.getAttribute("deleteOnStop");
    if (!StringUtils.hasText(deleteOnStop)) {
      deleteOnStop = "false";
    }

    // joram admin: create the queue.
    try {
      AdminItf wrapper = JoramAdmin.getWrapper();
      if (wrapper == null) throw new Exception("JoramQueueBeanDefinitionParser:: the admin wrapper is null !");
      
      Destination queue = wrapper.createQueue(Short.valueOf(sid), name);
      queue.setFreeReading();
      queue.setFreeWriting();
      
      if (StringUtils.hasText(dmq)) {
        Queue deadMQ = (Queue) wrapper.createQueue(Short.valueOf(dmqSid), dmq);
        deadMQ.setFreeReading();
        deadMQ.setFreeWriting();
        // set dead message queue
        queue.setDMQ(deadMQ);
      }
      
      bean.addPropertyValue("destination", queue);
      bean.addPropertyValue("deleteOnStop", Boolean.parseBoolean(deleteOnStop));
      
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "JoramQueueBeanDefinitionParser.doParse", e);
    }
  }
}