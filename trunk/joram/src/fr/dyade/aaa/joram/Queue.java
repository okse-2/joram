/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
 */
package fr.dyade.aaa.joram;

import fr.dyade.aaa.joram.admin.AdministeredObject;

import java.util.Vector;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Implements the <code>javax.jms.Queue</code> interface.
 */
public class Queue extends Destination implements javax.jms.Queue
{
  /** 
   * Constructs a queue.
   *
   * @param agentId  Identifier of the queue agent.
   */
  public Queue(String agentId)
  {
    super(agentId);
  }

  /** 
   * Constructs an empty queue.
   */
  public Queue()
  {}

  /** Returns a String image of the queue. */
  public String toString()
  {
    return "Queue:" + agentId;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getQueueName() throws JMSException
  {
    return agentId;
  }


  /**
   * Decodes a <code>Queue</code> which traveled through the SOAP protocol.
   */ 
  public Object decode(Hashtable h) {
    return new Queue((String) h.get("agentId"));
  }
}
