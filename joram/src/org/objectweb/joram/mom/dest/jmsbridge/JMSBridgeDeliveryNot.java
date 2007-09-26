/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
 * Copyright (C) 2007 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package org.objectweb.joram.mom.dest.jmsbridge;

import org.objectweb.joram.shared.messages.Message;


/**
 * A <code>BridgeDeliveryNot</code> notification carries a message obtained
 * by a JMS module from a foreign JMS server.
 */
public class JMSBridgeDeliveryNot extends fr.dyade.aaa.agent.Notification {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Message obtained from the foreign JMS server. */
  private Message message;


  /**
   * Constructs a <code>BridgeDeliveryNot</code> wrapping a given message
   * obtained from a foreign JMS server.
   */
  public JMSBridgeDeliveryNot(Message message) {
    this.message = message;
  }


  /** Returns the message obtained from the foreign JMS server. */
  public Message getMessage() {
    return message;
  }
}
