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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;

/**
 * Parser for the joram:joramserver xml entry.
 * 
 * <P>The joram server attributes:
 * <UL>
 *  <LI>sid (optionnal): the server id value (default 0)
 *  <LI>persistent (optionnal): true if the Joram server if persistent (default false)
 *  <LI>pathToConf (optionnal): the path to the configuration (a3servers.xml, ...)  (default “.”)
 *  <LI>storage (optionnal): the path to the persistent directory (default “s+sid”)
 *  <LI>stopServer (optionnal): if true the server stop on destroy (default false)
 * </UL>
 * <PRE><CODE>
 * <B>A simple example:</B>
 * {@code
 * <!--  lets create an embedded Joram server -->
 * <joram:joramserver id="server" 
 *    sid="0" 
 *    persistent="true" 
 *    pathToConf="/JONAS_BASE/conf" 
 *    storage="/JONAS_BASE/work/s0" 
 *    stopServer="false"/>
 * }</CODE></PRE>
 */
public class JoramServerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  public static final Logger logmon = Debug.getLogger(JoramServerBeanDefinitionParser.class.getName());
  
  /* (non-Javadoc)
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
   */
  protected Class getBeanClass(Element element) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "JoramServerBeanDefinitionParser.getBeanClass(" + element + ')');
    return JoramServer.class;
  }

  /**
   * Parse the joram server definition, and create and start the collocated server.
   * 
   * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionBuilder)
   */
  protected void doParse(Element element, BeanDefinitionBuilder bean) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "JoramServerBeanDefinitionParser.doParse(" + element + ", " + bean + ')');
    
    // this however is an optional property
    String sid = element.getAttribute("sid");
    if (!StringUtils.hasText(sid)) {
      sid = "0";
    }

    // this however is an optional property
    String pathToConf = element.getAttribute("pathToConf");
    if (!StringUtils.hasText(pathToConf)) {
      pathToConf = ".";
    }

    // this however is an optional property
    String storage = element.getAttribute("storage");
    if (!StringUtils.hasText(storage)) {
      storage = "s"+sid;
    }

    // this however is an optional property
//    String collocated = element.getAttribute("collocated");
//    if (!StringUtils.hasText(collocated)) {
//      collocated ="true";
//    }

    // this however is an optional property
    String persistent = element.getAttribute("persistent");
    if (!StringUtils.hasText(persistent)) {
      persistent = "false";
    }
    
    // this however is an optional property
    String stopServer = element.getAttribute("stopServer");
    if (!StringUtils.hasText(stopServer)) {
      stopServer = "false";
    }
    
    bean.addPropertyValue("stopServer", Boolean.parseBoolean(stopServer));
    

    createAndStart(Short.parseShort(sid), pathToConf, storage, Boolean.parseBoolean(persistent));
  }
  
  /**
   * Create and start a collocated Joram server.
   * 
   * @param sid the server id
   * @param pathToConf the path to the configuration (a3servers.xml, ...)
   * @param storage the path to the persistent directory
   * @param persistent true if the Joram server if persistent
   */
  public void createAndStart(short sid, String pathToConf, String storage, boolean persistent) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "JoramServerBeanDefinitionParser.createAndStart(" + sid + ", " + pathToConf + ", " + storage + ", " + persistent + ')');
    
    System.setProperty(AgentServer.CFG_DIR_PROPERTY, pathToConf);
    System.setProperty(Debug.DEBUG_DIR_PROPERTY, pathToConf);
    if (!persistent)
      System.setProperty("Transaction", "fr.dyade.aaa.util.NullTransaction");
//    else
      //TODO

    if (AgentServer.getStatus() == AgentServer.Status.STARTED ||
        AgentServer.getStatus() == AgentServer.Status.STARTING)
      return;

    try {
      AgentServer.init(sid, storage, null);
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, AgentServer.getName() + " initialization failed", exc);
    }

    try {
      String errStr = AgentServer.start();
      if (errStr == null) {
        if (logmon.isLoggable(BasicLevel.INFO))
          logmon.log(BasicLevel.INFO, AgentServer.getName() + " started: " + AgentServer.OKSTRING);
      } else {
        if (logmon.isLoggable(BasicLevel.INFO)) {
          logmon.log(BasicLevel.INFO, AgentServer.getName() + " started: " + AgentServer.ERRORSTRING +
              "\n" + errStr + "\n" + AgentServer.ENDSTRING);
        }
      }
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, AgentServer.getName() + " failed", exc);
    }

  }
}