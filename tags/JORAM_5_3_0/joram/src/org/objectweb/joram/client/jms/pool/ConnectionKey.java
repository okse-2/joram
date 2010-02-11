/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms.pool;

/**
 * A Key to retrieve Connection in cache.
 */
public class ConnectionKey {
  /** the name of user. */
  private String name;
  /** the password of user. */
  private String pass;

  /**
   * Creates a new ConnectionKey object for user's name and password.
   *  
   * @param name the name of user.
   * @param pass the password of user.
   */
  public ConnectionKey(String name, String pass) {
    this.pass = pass;
    this.name = name;
  }

  
  /**
   * Returns a hash code value for the object.
   * 
   * @return returns the hash code value of the name attribute.
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return name.hashCode();
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   * 
   * @return  returns true if the specified object is a ConnectionKey and
   *          if name and password are equals.
   *          
   * @see java.lang.Object#hashCode()
   */
  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (obj instanceof ConnectionKey) {
      ConnectionKey key = (ConnectionKey) obj;
      if (name.equals(key.name) && pass.equals(key.pass))
        return true;
    }
    
    return false;
  }
}
