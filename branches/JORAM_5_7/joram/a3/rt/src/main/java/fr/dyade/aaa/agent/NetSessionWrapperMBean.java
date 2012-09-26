/*
 * Copyright (C) 2007 - 2008 ScalAgent Distributed Technologies
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

/**
 * JMX interface of a NetSesion component.
 * <p>
 * This interface is defined through a wrapper as the NetSession component
 * is an internal object of PoolNetwork.
 */
public interface NetSessionWrapperMBean {
  /**
   * Gets the server identification of remote host.
   *
   * @return	the server identification of remote host.
   */
  
  public short getRemoteSID();
  /**
   * Gets the maximum number of message sent and non acknowledged.
   * 
   * @return  the maximum number of message sent and non acknowledged.
   */
  public int getMaxMessageInFlow();
  
  /**
   * Sets the maximum number of message sent and non acknowledged.
   * 
   * @param maxMessageInFlow  the maximum number of message sent and non acknowledged.
   */
  public void setMaxMessageInFlow(int maxMessageInFlow);

  /**
   * Tests if the session is connected.
   *
   * @return	true if this session is connected; false otherwise.
   */
  public boolean isRunning();

  /**
   * Gets the number of waiting messages to send for this session.
   *
   * @return	the number of waiting messages.
   */
  public int getNbWaitingMessages();

  /**
   * Returns the number of messages sent since last reboot.
   * 
   * @return  the number of messages sent since last reboot.
   */
  public int getNbMessageSent();

  /**
   * Returns the number of messages received since last reboot.
   * 
   * @return  the number of messages received since last reboot.
   */
  public int getNbMessageReceived();

  /**
   * Returns the number of acknowledge sent since last reboot.
   * 
   * @return  the number of acknowledge sent since last reboot.
   */
  public int getNbAckSent();
  
  /**
   * Returns the time in milliseconds of last message received.
   * 
   * @return the time in milliseconds of last message received.
   */
  public long getLastReceived();
  
  
  /**
   * Returns the number of buffering messages to sent since last reboot.
   *
   * @return  the number of buffering messages to sent since last reboot.
   */
  public int getNbBufferingMessageToSent();
}
