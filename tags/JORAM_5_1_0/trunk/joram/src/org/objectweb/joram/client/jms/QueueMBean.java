/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms;

import java.util.Properties;

import javax.jms.JMSException;
import java.net.ConnectException;

import org.objectweb.joram.client.jms.admin.AdminException;

public interface QueueMBean extends DestinationMBean {
  public void setNbMaxMsg(int nbMaxMsg)
    throws ConnectException, AdminException;

  public int getNbMaxMsg()
    throws ConnectException, AdminException;

  /**
   * Admin method setting or unsetting the threshold for this queue.
   */
  public void setThreshold(int threshold)
    throws ConnectException, AdminException;

  /** 
   * Monitoring method returning the threshold of this queue, -1 if not set.
   */
  public int getThreshold()
    throws ConnectException, AdminException;
   
  /**
   * Monitoring method returning the number of pending messages on this queue.
   */
  public int getPendingMessages()
    throws ConnectException, AdminException;

  /**
   * Monitoring method returning the number of pending requests on this queue.
   */
  public int getPendingRequests()
    throws ConnectException, AdminException;

  public String[] getMessageIds()
    throws ConnectException, AdminException;

  public String getMessageDigest(String msgId)
    throws AdminException, ConnectException, JMSException;
 
  public Properties getMessageHeader(String msgId)
    throws ConnectException, AdminException, JMSException;
  
  public Properties getMessageProperties(String msgId)
    throws ConnectException, AdminException, JMSException;

  public void deleteMessage(String msgId)
    throws ConnectException, AdminException;

  public void clear()
    throws ConnectException, AdminException;
}
