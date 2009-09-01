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

public interface HttpNetworkMBean extends NetworkMBean {
  /**
   * Gets the proxyhost value.
   *
   * @return the proxyhost value
   */
  String getProxyhost();
  
  /**
   * Gets the proxyport value.
   *
   * @return the proxyport value
   */
  long getProxyport();

  /**
   * Gets the activationPeriod value.
   *
   * @return the activationPeriod value
   */
  long getActivationPeriod();

  /**
   * Sets the activationPeriod value.
   *
   * @param activationPeriod	the activationPeriod value
   */
  void setActivationPeriod(long activationPeriod);

  /**
   * Gets the NbDaemon value.
   *
   * @return the NbDaemon value
   */
  public long getNbDaemon();
}
