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
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import fr.dyade.aaa.common.Debug;

/**
 * This class holds the connection factory (local or tcp).
 */
public class JoramConnectionFactory implements FactoryBean, DisposableBean {
  
  public static final Logger logger = Debug.getLogger(JoramConnectionFactory.class.getName());
  private ConnectionFactory connectionFactory = null;
      
  /**
   * @param connectionFactory the connection factory to set
   */
  public void setConnectionFactory(ConnectionFactory connectionFactory) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramConnectionFactory.setConnectionFactory(" + connectionFactory + ')');
    this.connectionFactory = connectionFactory;
  }

  /* (non-Javadoc)
   * @see org.springframework.beans.factory.FactoryBean#getObject()
   */
  public Object getObject() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramConnectionFactory.getObject : cf = " + connectionFactory);
    return connectionFactory;
  }

  /* (non-Javadoc)
   * @see org.springframework.beans.factory.FactoryBean#getObjectType()
   */
  public Class getObjectType() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramConnectionFactory.getObjectType : cf.class = " + connectionFactory.getClass());
    return connectionFactory.getClass();
  }

  /* (non-Javadoc)
   * @see org.springframework.beans.factory.FactoryBean#isSingleton()
   */
  public boolean isSingleton() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.springframework.beans.factory.DisposableBean#destroy()
   */
  public void destroy() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramConnectionFactory.destroy");
  }
}
