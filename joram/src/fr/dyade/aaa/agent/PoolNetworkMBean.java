/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.agent;

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
   * Returns the maximum number of free senders in the pool.
   *
   * @return  the number of free senders in the pool.
   */
  public int getNbMaxFreeSender();

  /**
   * Gets the number of waiting messages to send for this session.
   *
   * @return	the number of waiting messages.
   */
  public long getIdleTimeout();

  public void setIdleTimeout(long idleTimeout);
}
