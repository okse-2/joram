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
package org.objectweb.joram.mom.proxies.tcp;

import java.util.Date;

/**
 * Adds JMX monitoring for a tcp connection.
 */
public interface TcpConnectionMBean {

  /**
   * Gets the socket address used by the connection.
   * 
   * @return the connection's socket address.
   */
  public String getAddress();
  
  /**
   * Gets connected user's name.
   * 
   * @return the name of the connected user.
   */
  public String getUserName();

  /**
   * Gets connection creation date.
   * 
   * @return the date of creation of the connection.
   */
  public Date getCreationDate();

  /**
   * Closes the connection and unregisters the MBean.
   */
  public void close();

  /**
   * Gets the number of requests sent on the connection.
   * 
   * @return the number of requests sent on the connection.
   */
  public long getSentCount();

  /**
   * Gets the number of replies received on the connection.
   * 
   * @return the number of replies received on the connection.
   */
  public long getReceivedCount();
  
  /**
   * Gets the AckedQueue size.
   * 
   * @return the size of AckedQueue.
   */
  public int getAckedQueueSize();
  
  /**
   * Gets the QueueWorker size.
   * 
   * @return the size of QueueWorker.
   */
  public int getQueueWorkerSize();
  
  /**
   * Gets the ReaderQueue size.
   * 
   * @return the size of ReaderQueue.
   */
  public int getReaderQueueSize();
}
