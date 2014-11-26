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

import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import fr.dyade.aaa.common.Debug;

/**
 * This class holds the Joram user.
 * If set the user can be deleted on destroy.
 */
public class JoramUser implements FactoryBean, DisposableBean {
  
  public static final Logger logger = Debug.getLogger(JoramUser.class.getName());
  
  private User user = null;
  private boolean deleteOnStop = false;
  
  /**
   * @param user the user to set
   */
  public void setUser(User user) {
  if (logger.isLoggable(BasicLevel.DEBUG))
    logger.log(BasicLevel.DEBUG, "JoramUser.setDestination(" + user + ')');
    this.user = user;
  }

  /**
   * @return the user
   * 
   * @see org.springframework.beans.factory.FactoryBean#getObject()
   */
  public Object getObject() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramUser.getObject : dest = " + user);
    return user;
  }

  /* (non-Javadoc)
   * @see org.springframework.beans.factory.FactoryBean#getObjectType()
   */
  public Class getObjectType() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramUser.getObjectType : dest.class = " + user.getClass());
    return user.getClass();
  }

  /* (non-Javadoc)
   * @see org.springframework.beans.factory.FactoryBean#isSingleton()
   */
  public boolean isSingleton() {
    return true;
  }

  /**
   * delete the user if the deleteOnStop is set.
   *  
   * @see org.springframework.beans.factory.DisposableBean#destroy()
   */
  public void destroy() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramUser.destroy : " + user + " delete. (deleteOnStop = " + deleteOnStop + ')');
    if (user != null && deleteOnStop)
      user.delete();
  }

  /**
   * @return the deleteOnStop
   */
  public boolean isDeleteOnStop() {
    return deleteOnStop;
  }

  /**
   * @param deleteOnStop the deleteOnStop to set
   */
  public void setDeleteOnStop(boolean deleteOnStop) {
    this.deleteOnStop = deleteOnStop;
  }
}
