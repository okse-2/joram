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

import java.util.List;

import org.objectweb.joram.shared.messages.Message;

/**
 * The {@link ReliableTransmitter} is used by an {@link AcquisitionHandler} or
 * an {@link AcquisitionDaemon} to transmit acquired messages to the MOM.
 */
public interface ReliableTransmitter {

  /**
   * Transmits a message to the MOM in a reliable way: message has been
   * persisted when the method returns and therefore can be safely acknowledged.
   * The message ID is used to avoid duplicates if a server crash happens right
   * after transmitting the message and before it has been acknowledged. It can
   * be <code>null</code> if such duplicates are tolerated.
   * 
   * @param message
   *          the message to transmit
   * @param messageId
   *          the unique ID of the transmitted message
   */
  public void transmit(Message message, String messageId);

  /**
   * Transmits a list of messages to the MOM in a reliable way: messages have
   * been persisted when the method returns and therefore can be safely
   * acknowledged. The ID is used to avoid duplicates if a server crash happens
   * right after transmitting the messages and before they have been
   * acknowledged. It can be <code>null</code> if such duplicates are tolerated.
   * 
   * @param messages
   *          the messages to transmit
   * @param messagesId
   *          a unique ID for the list of transmitted messages.
   */
  public void transmit(List messages, String messagesId);

}
