/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
package org.objectweb.joram.client.connector;


/**
 * A <code>TopicConnectionRequest</code> instance wraps a user connection
 * request for performing PubSub messaging.
 */
public class TopicConnectionRequest
             extends ConnectionRequest
             implements javax.resource.spi.ConnectionRequestInfo
{
  /**
   * Constructs a <code>TopicConnectionRequest</code> instance.
   *
   * @param userName  Name of the user requesting a connection.
   * @param password  Password of the user requesting a connection.
   */
  public TopicConnectionRequest(String userName, String password)
  {
    super(userName, password);
  } 


  /** Returns a code based on the wrapped user identity. */
  public int hashCode()
  {
    return ("PubSub:" + userName).hashCode();
  }
}
