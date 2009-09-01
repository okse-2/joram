/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
 */
package org.objectweb.joram.mom.proxies;

/**
 * Adds JMX monitoring for a connection manager.
 */
public interface ConnectionManagerMBean {
  
  /**
   * Closes all opened connections.
   */
  public void closeAllConnections();

  /**
   * Deactivates the connection manager. No new connection will be opened.
   */
  public void deactivate();

  /**
   * Activates the connection manager. Creation of new connections will be
   * allowed.
   */
  public void activate();

  /**
   * Tells if the ConnectionManager is active.
   * 
   * @return ConnectionManager's active state.
   */
  public boolean isActivated();

  /**
   * Gets the number of living connections.
   * 
   * @return the number of living connections.
   */
  public int getRunningConnectionsCount();
  
  /**
   * Gets the name of the MBean.
   */
  public String getMBeanName();

}
