/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.amqp;

import java.util.HashMap;

import fr.dyade.aaa.agent.Notification;

public class BindNot extends Notification {
  
  private String queue;
  
  private String routingKey;
  
  private HashMap arguments;

  /**
   * @param queue
   * @param routingKey
   * @param arguments
   */
  public BindNot(String queue, String routingKey, HashMap arguments) {
    super();
    this.queue = queue;
    this.routingKey = routingKey;
    this.arguments = arguments;
  }

  public HashMap getArguments() {
    return arguments;
  }
  public String getQueue() {
    return queue;
  }
  public String getRoutingKey() {
    return routingKey;
  }
}
