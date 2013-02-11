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
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import fr.dyade.aaa.common.Debug;

/**
 *
 */
public class JoramNamespaceHandler extends NamespaceHandlerSupport {// implements BundleActivator {
  public static final Logger logmon = Debug.getLogger(JoramNamespaceHandler.class.getName());
  
    /**
     * register the all joram bean definitions.
     * 
     * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
     */
    public void init() {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "JoramNamespaceHandler.init()");
      
        registerBeanDefinitionParser("joramserver", new JoramServerBeanDefinitionParser()); 
        registerBeanDefinitionParser("admin", new JoramAdminBeanDefinitionParser()); 
        registerBeanDefinitionParser("queue", new JoramQueueBeanDefinitionParser()); 
        registerBeanDefinitionParser("topic", new JoramTopicBeanDefinitionParser());
        registerBeanDefinitionParser("user", new JoramUserBeanDefinitionParser()); 
        registerBeanDefinitionParser("tcpConnectionFactory", new JoramTcpConnectionFactoryBeanDefinitionParser()); 
        registerBeanDefinitionParser("localConnectionFactory", new JoramLocalConnectionFactoryBeanDefinitionParser());
        
        registerBeanDefinitionParser("testserver", new JoramTestServerBeanDefinitionParser()); 
    }

//    public void start(BundleContext ctx) throws Exception {
//     init();
//    }
//
//    public void stop(BundleContext ctx) throws Exception {
//      // TODO Auto-generated method stub
//      
//    }
}
