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
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.DisposableBean;

import fr.dyade.aaa.common.Debug;

/**
 * this class holds the Joram wrapper admin and the connection
 */
public class JoramAdmin implements DisposableBean {
  
  public static final Logger logger = Debug.getLogger(JoramAdmin.class.getName());
  
  public static AdminItf wrapper = null;
  private static Connection cnx = null;

  /**
   * @return the admin wrapper
   */
  public static AdminItf getWrapper() {
    return wrapper;
  }
  
  /**
   * @param wrapper the admin wrapper to set
   */
  public static void setWrapper(AdminItf wrapper) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramAdmin.setWrapper(" + wrapper + ')');
    JoramAdmin.wrapper = wrapper;
  }
  
  /**
   * @param cnx the admin connection to set
   */
  public static void setConnection(Connection cnx) {
    JoramAdmin.cnx = cnx;
  }

  /** 
   * Close the admin wrapper and the connection.
   * 
   * @see org.springframework.beans.factory.DisposableBean#destroy()
   */
  public void destroy() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JoramAdmin.destroy() wrapper = " + wrapper + ", cnx = " + cnx);
    if (wrapper != null)
      wrapper.close();
    wrapper = null;
    
    try {
    if (cnx != null)
      cnx.close();
    cnx = null;
    } catch (Exception e) { }
  }
}
