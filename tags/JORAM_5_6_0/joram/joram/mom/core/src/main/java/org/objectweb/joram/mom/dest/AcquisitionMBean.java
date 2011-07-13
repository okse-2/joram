/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest;

/**
 * JMX interface for the acquisition monitoring.
 */
public interface AcquisitionMBean {

  /**
   * Returns true if the messages produced are persistent.
   * 
   * @return true if the messages produced are persistent.
   */
  public boolean isMessagePersistent();

  /**
   * Sets the DeliveryMode value for the produced messages. If the parameter is
   * true the messages produced are persistent.
   * 
   * @param isPersistent
   *          if true the messages produced are persistent.
   */
  public void setMessagePersistent(boolean isPersistent);

  /**
   * Returns the priority of produced messages.
   * 
   * @return the priority of produced messages.
   */
  public int getPriority();

  /**
   * Sets the priority of produced messages.
   * 
   * @param priority
   *          the priority to set.
   */
  public void setPriority(int priority);

  /**
   * Returns the expiration value for produced messages.
   * 
   * @return the expiration value for produced messages.
   */
  public long getExpiration();

  /**
   * Sets the expiration value for produced messages.
   * 
   * @param expiration
   *          the expiration to set.
   */
  public void setExpiration(long expiration);

  /**
   * Returns the acquisition handler class name.
   * 
   * @return the acquisition handler class name.
   */
  public String getAcquisitionClassName();

  /**
   * Returns the acquisition period.
   * 
   * @return the acquisition period.
   */
  public long getAcquisitionPeriod();

}
