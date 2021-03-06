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
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

import fr.dyade.aaa.common.Debug;

/**
 * Parser to the joram:localConnectionFactory xml entry.
 * 
 * <P>The joram local connection factory attributes:
 * <UL> 
 * <LI>no attribute for this entry.
 * </UL>
 * <PRE><CODE>
 * <B>A simple example:</B>
 * {@code
 * <!--  Joram local connection factory to use -->
 * <joram:localConnectionFactory id="LCF"/></PRE>
 * }</CODE></PRE>
 */
public class JoramLocalConnectionFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  public static final Logger logmon = Debug.getLogger(JoramLocalConnectionFactoryBeanDefinitionParser.class.getName());
  
  /* (non-Javadoc)
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
   */
  protected Class getBeanClass(Element element) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "JoramLocalConnectionFactoryBeanDefinitionParser.getBeanClass(" + element + ')');
    return JoramConnectionFactory.class;
  }

  /**
   * Create a local connection factory.
   * Add the connectionFactory property value to the bean.
   * 
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder)
   */
  protected void doParse(Element element, BeanDefinitionBuilder bean) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "JoramLocalConnectionFactoryBeanDefinitionParser.doParse(" + element + ", " + bean + ')');

    ConnectionFactory cf = LocalConnectionFactory.create();
    bean.addPropertyValue("connectionFactory", cf);
  }
}