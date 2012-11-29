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

import java.io.File;
import java.io.FileWriter;

import javax.jms.Connection;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.admin.AdminItf;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;

/**
 * Parser to the joram:testserver xml entry.
 * 
 * <P>This if all in one entry, non configuration files, ...
 * <UL>
 * <LI>Create and start a collocated Joram server on localhost.
 * <LI>Create the Joram administration wrapper
 * <LI>Create the anonymous user
 * </UL>
 *
 * <P>The joram user attributes: 
 * <UL>
 * <LI>sid (optionnal): the server id (default “0”)
 * <LI>port (optionnal): the server port (default “16010”)
 * </UL>
 *<PRE><CODE>
 * <B>A simple example:</B>
 * {@code
 * <!-- lets create an embedded Joram server for test, create a user "anonymous" and create a collocated admin wrapper  -->
 * <joram:testserver id="server" />
 * }</CODE></PRE>
 */
public class JoramTestServerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  public static final Logger logger = Debug.getLogger(JoramTestServerBeanDefinitionParser.class.getName());
  
  /* (non-Javadoc)
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
   */
  protected Class getBeanClass(Element element) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramTestServerBeanDefinitionParser.getBeanClass(" + element + ')');
    return JoramServer.class;
  }

  
  /* (non-Javadoc)
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder)
   */
  protected void doParse(Element element, BeanDefinitionBuilder bean) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramTestServerBeanDefinitionParser.doParse(" + element + ", " + bean + ')');
    
    // this however is an optional property
    String sid = element.getAttribute("sid");
    if (!StringUtils.hasText(sid)) {
      sid = "0";
    }
    
    // this however is an optional property
    String port = element.getAttribute("port");
    if (!StringUtils.hasText(port)) {
      port = "16010";
    }
    
    bean.addPropertyValue("stopServer", "true");

    try {
      createAndStart(Short.parseShort(sid), port);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "JoramTestServerBeanDefinitionParser.doParse", e);
    }
  }
  
  /**
   * @param sid
   * @param port
   * @throws Exception
   */
  public void createAndStart(short sid, String port) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramServerBeanDefinitionParser.createAndStart(" + sid + ')');
    
    File tempDir = File.createTempFile("joram", "");
    tempDir.delete();
    tempDir.deleteOnExit();
    if (tempDir.mkdir()) {
      File a3servers = new File(tempDir, "a3servers.xml");
      FileWriter writer = new FileWriter(a3servers, true);
      writer.write("<?xml version=\"1.0\"?>");
      writer.write("<config>");
      writer.write("<property name=\"Transaction\" value=\"fr.dyade.aaa.util.NullTransaction\" />");
      writer.write("<server id=\"" + sid + "\" name=\"S0\" hostname=\"localhost\">");
      writer.write("<service class=\"org.objectweb.joram.mom.proxies.ConnectionManager\" args=\"root root\"/>");
      writer.write("<service class=\"org.objectweb.joram.mom.proxies.tcp.TcpProxyService\" args=\"" + port + "\"/>");
      writer.write("</server>");
      writer.write("</config>");
      writer.flush();
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramServerBeanDefinitionParser.createAndStart a3servers.xml : " + a3servers.getAbsolutePath());
    } else {
      throw new Exception("can't create the directory : " + tempDir);
    }
    
    System.setProperty(AgentServer.CFG_DIR_PROPERTY, tempDir.getAbsolutePath());
    System.setProperty(Debug.DEBUG_DIR_PROPERTY, tempDir.getAbsolutePath());

    if (AgentServer.getStatus() == AgentServer.Status.STARTED ||
        AgentServer.getStatus() == AgentServer.Status.STARTING)
      return;

    try {
      AgentServer.init(sid, tempDir+"/s0", null);
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, AgentServer.getName() + " initialization failed", exc);
    }

    try {
      String errStr = AgentServer.start();
      if (errStr == null) {
        if (logger.isLoggable(BasicLevel.INFO))
          logger.log(BasicLevel.INFO, AgentServer.getName() + " started: " + AgentServer.OKSTRING);
        
        createAdminWrapper();
        createUser(sid);
      } else {
        if (logger.isLoggable(BasicLevel.INFO)) {
          logger.log(BasicLevel.INFO, AgentServer.getName() + " started: " + AgentServer.ERRORSTRING +
              "\n" + errStr + "\n" + AgentServer.ENDSTRING);
        }
      }
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, AgentServer.getName() + " failed", exc);
    }
  }
  
  /**
   * @throws Exception
   */
  private void createAdminWrapper() throws Exception {
    AdminItf wrapper = JoramAdmin.getWrapper();
    if (wrapper != null) return;

    try {
      ConnectionFactory cf = LocalConnectionFactory.create();

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "createAdminWrapper: cf = " + cf);
      Connection cnx = cf.createConnection("root", "root");
      cnx.start();
     
      wrapper = new org.objectweb.joram.client.jms.admin.JoramAdmin(cnx);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "createAdminWrapper: wrapper = " + wrapper);
      
      JoramAdmin.setWrapper(wrapper);
      
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "createAdminWrapper", e);
      throw e;
    }
  }
  
  /**
   * @param sid
   * @throws Exception
   */
  private void createUser(short sid) throws Exception {
    AdminItf wrapper = JoramAdmin.getWrapper();
    if (wrapper == null) throw new Exception("JoramUserBeanDefinitionParser:: the admin wrapper is null !");
    
    User user = wrapper.createUser("anonymous", "anonymous", sid);
  }

}