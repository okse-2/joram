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
 * A <code>ConnectionRequest</code> instance wraps a user connection
 * request for performing unified messaging.
 */
public class ConnectionRequest
             implements javax.resource.spi.ConnectionRequestInfo
{
  /** 
   * Identification of the user requesting a connection to the
   * underlying JORAM server.
   */
  protected String userName;
  /**
   * Password of the user requesting a connection to the
   * underlying JORAM server.
   */
  protected String password;


  /**
   * Constructs a <code>ConnectionRequest</code> instance.
   *
   * @param userName  Name of the user requesting a connection.
   * @param password  Password of the user requesting a connection.
   */
  public ConnectionRequest(String userName, String password)
  {
    this.userName = userName;
    this.password = password;
  } 


  /**
   * Returns the identification of the user requesting a connection to the
   * underlying JORAM server.
   */
  public String getUserName()
  {
    return userName;
  }

  /**
   * Returns the password of the user requesting a connection to the
   * underlying JORAM server.
   */
  public String getPassword()
  {
    return password;
  }


  /**
   * Compares <code>ConnectionRequest</code> instances wrapped users
   * identities.
   */
  public boolean equals(Object o)
  {
    if (! (o instanceof ConnectionRequest))
      return false;

    ConnectionRequest other = (ConnectionRequest) o;
    return this.getClass().getName().equals(o.getClass().getName())
           && other.getUserName().equals(userName);
  }

  /** Returns a code based on the wrapped user identity. */
  public int hashCode()
  {
    return ("Unified:" + userName).hashCode();
  }
}
