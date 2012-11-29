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

import org.objectweb.joram.client.jms.admin.AdminItf;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import fr.dyade.aaa.common.Debug;

/**
 * Parser to the joram:user xml entry.
 */
public class JoramUserBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  public static final Logger logger = Debug.getLogger(JoramUserBeanDefinitionParser.class.getName());
  
  /* (non-Javadoc)
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
   */
  protected Class getBeanClass(Element element) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramUserBeanDefinitionParser.getBeanClass(" + element + ')');
    return JoramUser.class;
  }

  /**
   * Create a Joram user.
   * Add the user property value to the bean.
   * 
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder)
   */
  protected void doParse(Element element, BeanDefinitionBuilder bean) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramUserBeanDefinitionParser.doParse(" + element + ", " + bean + ')');
    
    // this however is an optional property
    String sid = element.getAttribute("sid");
    if (!StringUtils.hasText(sid)) {
      sid = "0";
    }
    
    // this however is an optional property
    String name = element.getAttribute("name");
    if (!StringUtils.hasText(name)) {
      name = "anonymous";
    }
    // this however is an optional property
    String password = element.getAttribute("password");
    if (!StringUtils.hasText(password)) {
      password = "anonymous";
    }
    
    // this however is an optional property
    String deleteOnStop = element.getAttribute("deleteOnStop");
    if (!StringUtils.hasText(deleteOnStop)) {
      deleteOnStop = "false";
    }

    // joram admin: create the user.
    try {
      AdminItf wrapper = JoramAdmin.getWrapper();
      if (wrapper == null) throw new Exception("JoramUserBeanDefinitionParser:: the admin wrapper is null !");
      
      User user = wrapper.createUser(name, password, Integer.parseInt(sid));
      
      bean.addPropertyValue("user", user);
      bean.addPropertyValue("deleteOnStop", Boolean.parseBoolean(deleteOnStop));
      
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "JoramUserBeanDefinitionParser.doParse", e);
    }
  }
}