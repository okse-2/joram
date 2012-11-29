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

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import fr.dyade.aaa.common.Debug;

/**
 * Parser to the joram:tcpConnectionFactory xml entry.
 * 
 * <P>The joram TCP connection factory attributes:
 * <UL>
 * <LI>host (optionnal): the host of the Joram server  (default “localhost”)
 * <LI>port (optionnal): the port of the Joram server  (default “16010”)
 * </UL>
 *<PRE><CODE>
 * <B>A simple example:</B>
 * {@code
 * <!--  Joram tcp connection factory to use  -->
 * <joram:tcpConnectionFactory id="jmsFactory" 
 *  host="localhost" 
 *  port="16010" />
 * }</CODE></PRE>
 */
public class JoramTcpConnectionFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  public static final Logger logmon = Debug.getLogger(JoramTcpConnectionFactoryBeanDefinitionParser.class.getName());
  
  /* (non-Javadoc)
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
   */
  protected Class getBeanClass(Element element) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "JoramTcpConnectionFactoryBeanDefinitionParser.getBeanClass(" + element + ')');
    logmon.log(BasicLevel.DEBUG, "JoramTcpConnectionFactoryBeanDefinitionParser.getBeanClass name = " + element.getNodeName() + ", value = " + element.getNodeValue());//NTA tmp
    return JoramConnectionFactory.class;
  }

  /**
   * Create a TCP connection factory.
   * Add this connectionFactory property to the bean
   * 
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder)
   */
  protected void doParse(Element element, BeanDefinitionBuilder bean) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "JoramTcpConnectionFactoryBeanDefinitionParser.doParse(" + element + ", " + bean + ')');

    // this however is an optional property
    String host = element.getAttribute("host");
    if (!StringUtils.hasText(host)) {
      bean.addConstructorArgValue("localhost");
    }

    // this however is an optional property
    String port = element.getAttribute("port");
    if (!StringUtils.hasText(host)) {
      bean.addConstructorArgValue(16010);
    }
    
    ConnectionFactory cf = TcpConnectionFactory.create(host, Integer.parseInt(port));
    bean.addPropertyValue("connectionFactory", cf);
  }
}