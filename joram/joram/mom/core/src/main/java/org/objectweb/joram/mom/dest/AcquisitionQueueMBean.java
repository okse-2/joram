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
 * JMX interface for the monitoring queue.
 */
public interface AcquisitionQueueMBean extends QueueMBean, AcquisitionMBean {
  /**
   * Returns the number of messages acquired by the acquisition handler.
   * Be careful this counter is reseted at each time the server starts.
   * 
   * @return the number of messages acquired by the acquisition handler.
   */
  long getAcquiredMsgCount();
  /**
   * Returns the number of acquired messages processed by the destination.
   * 
   * @return the number of acquired messages processed by the destination.
   */
  long getHandledMsgCount();
  
  /**
   * Returns the maximum number of acquired messages waiting to be handled by
   * the destination. When the number of messages waiting to be handled is greater
   * the acquisition handler is temporarily stopped.
   * 
   * @return the maximum number of acquired messages waiting to be handled by
   * the destination.
   */
  long getDiffMax();
  /**
   * Returns the minimum threshold of acquired messages waiting to be handled by
   * the destination for restarting the acquisition handler.
   * 
   * @return the minimum threshold of acquired messages waiting to be handled by
   * the destination.
   */
  long getDiffMin();
  
  /**
   * Returns the maximum number of waiting messages in the destination. When the number
   * of waiting messages is greater the acquisition handler is temporarily stopped.
   * 
   * @return the maximum number of waiting messages in the destination.
   */
  long getPendingMax();
  /**
   * Returns the minimum threshold of waiting messages in the destination for restarting
   * the acquisition handler.
   * 
   * @return the minimum threshold of waiting messages in the destination.
   */
  long getPendingMin();
}
