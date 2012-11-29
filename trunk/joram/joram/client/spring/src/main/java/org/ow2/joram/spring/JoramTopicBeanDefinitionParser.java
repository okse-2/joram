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
 * Parser to the joram:topic xml entry.
 * 
 * <P>The joram topic attributes:
 * <UL>
 * <LI>name (required): the topic name.
 * <LI>sid (optionnal): the server id where create the topic (default “0”)
 * <LI>dmq (optionnal): the dead message queue name
 * <LI>dmqSid (optionnal): the dmq server id (default “sid”)
 * <LI>deleteOnStop (optionnal): if true the topic can be delete on destroy (default “false”)
 * </UL>
 * <PRE><CODE>
 * <B>A simple example:</B>
 * {@code
 * <!--  Joram topic destination to use  -->
 * <joram:topic id="destination" 
 *  name="myTopic" 
 *  dmq="DMQ" />
 * }</CODE></PRE>
 */
public class JoramTopicBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  public static final Logger logger = Debug.getLogger(JoramTopicBeanDefinitionParser.class.getName());
  
  /* (non-Javadoc)
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
   */
  protected Class getBeanClass(Element element) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramTopicBeanDefinitionParser.getBeanClass(" + element + ')');
    return JoramDestination.class;
  }

  /**
   * Create a Joram topic.
   * Add the destination property value to the bean.
   * 
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder)
   */
  protected void doParse(Element element, BeanDefinitionBuilder bean) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramTopicBeanDefinitionParser.doParse(" + element + ", " + bean + ')');
    
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

    // joram admin: create the topic.
    try {
      AdminItf wrapper = JoramAdmin.getWrapper();
      if (wrapper == null) throw new Exception("JoramTopicBeanDefinitionParser:: the admin wrapper is null !");
      
      Destination topic = wrapper.createTopic(Short.valueOf(sid), name);
      topic.setFreeReading();
      topic.setFreeWriting();
      
      if (StringUtils.hasText(dmq)) {
        Queue deadMQ = (Queue) wrapper.createQueue(Short.valueOf(dmqSid), dmq);
        deadMQ.setFreeReading();
        deadMQ.setFreeWriting();
        // set dead message queue
        topic.setDMQ(deadMQ);
      }
      
      bean.addPropertyValue("destination", topic);
      bean.addPropertyValue("deleteOnStop", Boolean.parseBoolean(deleteOnStop));
      
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "JoramTopicBeanDefinitionParser.doParse", e);
    }
  }
}