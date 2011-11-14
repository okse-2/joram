/*
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

/**
 * JMX interface of the PoolNetwork component.
 * This interface only reports global indicators of the component, each
 * NetSession has its own JMX interface.
 */
public interface PoolNetworkMBean extends NetworkMBean {
  /**
   * Gets the maximum number of active session.
   *
   * @return	the maximum number of active session.
   */
  public int getNbMaxActiveSession();

  /**
   * Gets the number of active session.
   *
   * @return	the number of active session.
   */
  public int getNbActiveSession();

  /**
   * Gets the maximum idle period permitted before reseting the connection.
   *
   * @return	the number of waiting messages.
   */
  public long getIdleTimeout();

  /**
   * Sets the maximum idle period permitted before reseting the connection.
   *
   * @param the maximum idle period permitted before reseting the connection.
   */
  public void setIdleTimeout(long idleTimeout);
  
  /**
   * Returns if the stream between servers are compressed or not.
   *
   * @return  true if the streams between servers are compressed, false
   *               otherwise.
   */
  public boolean getCompressedFlows();
}
