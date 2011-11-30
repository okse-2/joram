/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
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
 * Contributor(s):
 */
package org.objectweb.joram.mom.dest.jmsbridge;


/**
 * A <code>BridgeAckNot</code> notification carries the identifier of a
 * message successfully delivered by a JMS module to a foreign JMS server.
 */
@Deprecated
public class JMSBridgeAckNot extends fr.dyade.aaa.agent.Notification {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * Identifier of the message successfully delivered to the foreign
   * JMS server.
   */
  private String id;


  /**
   * Constructs a <code>BridgeAckNot</code> wrapping the identifier of a
   * message successfully delivered to the foreign JMS server.
   */
  public JMSBridgeAckNot(String id) {
    this.id = id;
  }


  /**
   * Returns the identifier of the message successfully delivered to
   * the foreign JMS server.
   */
  public String getIdentifier() {
    return id;
  }
}
