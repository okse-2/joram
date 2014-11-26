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

import javax.jms.Connection;

import org.objectweb.joram.client.jms.admin.AdminItf;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import fr.dyade.aaa.common.Debug;

/**
 * Parser for the joram:admin xml entry.
 * 
 * <P>The joram administration attributes:
 * <UL>
 * <LI>user (optionnal): the name of the Joram root (default “root”)
 * <LI>pass (optionnal): the password of the Joram root (default “root”)
 * <LI>host (optionnal): the host of the Joram server  (default “localhost”)
 * <LI>port (optionnal): the port of the Joram server  (default “16010”)
 * </UL>
 *<PRE><CODE>
 * <B>A simple example:</B>
 * {@code
 * <!--  Joram administration to use  -->
 * <joram:admin id="wrapper" 
 *  user="root" 
 *  pass="root" 
 *  host="localhost" 
 *  port="16010" />
 * }</CODE></PRE>
 */
public class JoramAdminBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  public static final Logger logger = Debug.getLogger(JoramAdminBeanDefinitionParser.class.getName());
  private Connection cnx = null;
  
  /** 
   * @return the Class of JoramAdmin.
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
   */
  protected Class getBeanClass(Element element) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramAdminBeanDefinitionParser.getBeanClass(" + element + ')');
    return JoramAdmin.class;
  }

  /**
   * Parse the joram admin definition, and create the admin wrapper.
   * By default JoramAdmin is collocated. If you set a host / port, the collocated set to false. 
   * Add the wrapper property value to the bean.
   * 
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder)
   */
  protected void doParse(Element element, BeanDefinitionBuilder bean) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramAdminBeanDefinitionParser.doParse(" + element + ", " + bean + ')');
    
    boolean collocated = true;
    
    // this however is an optional property
    String user = element.getAttribute("user");
    if (!StringUtils.hasText(user)) {
      user = "root";
    }
    
    // this however is an optional property
    String pass = element.getAttribute("pass");
    if (!StringUtils.hasText(pass)) {
      pass = "root";
    }

    // this however is an optional property
    String host = element.getAttribute("host");
    if (StringUtils.hasText(host)) {
      collocated = false;
    }

    // this however is an optional property
    String port = element.getAttribute("port");
    if (StringUtils.hasText(port)) {
      collocated = false;
    }
    
    AdminItf wrapper = null;
    try {
      if (collocated)
        wrapper = adminConnect(collocated, user, pass, null, -1);
      else
        wrapper = adminConnect(collocated, user, pass, host, Integer.parseInt(port));
      
      JoramAdmin.setWrapper(wrapper);
      JoramAdmin.setConnection(cnx);
      bean.addPropertyValue("wrapper", wrapper);
      
    } catch (Exception e) {
      // TODO: handle exception
    }
  }
  
  /**
   * Create the wrapper (AdminItf).
   * 
   * @param collocated true if Joram is colocated
   * @param user the admin user name
   * @param pass the admin password
   * @param host the admin host
   * @param port the admin port
   * @return the admin wrapper
   * @throws Exception
   */
  private AdminItf adminConnect(boolean collocated, String user, String pass, String host, int port) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramAdminBeanDefinitionParser.adminConnect(" + collocated + ", " + user + ", " + pass + ", " + host + ", " + port + ')');
    AdminItf wrapper = JoramAdmin.getWrapper();
    if (wrapper != null) return null;

    try {
      org.objectweb.joram.client.jms.ConnectionFactory cf;

      if (collocated)
        cf = LocalConnectionFactory.create();
      else
        cf = TcpConnectionFactory.create(host, port);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramAdmin.adminConnect: cf = " + cf);

      //      if (connectingTimer == 0)
      //        cf.getParameters().connectingTimer = 60;
      //      else
      //        cf.getParameters().connectingTimer = connectingTimer;
      //      cf.setIdentityClassName(identityClass);
      //      cf.setCnxJMXBeanBaseName(jmxRootName+"#"+getName());
      cnx = cf.createConnection(user, pass);
      cnx.start();
     
      wrapper = new org.objectweb.joram.client.jms.admin.JoramAdmin(cnx);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "adminConnect: wrapper = " + wrapper);
      
      return wrapper;
      
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "JoramAdmin.adminConnect", e);
      throw e;
    }
  }
}