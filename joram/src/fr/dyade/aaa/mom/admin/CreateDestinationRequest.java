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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.admin;

import java.util.Properties;

/**
 * A <code>CreateDestinationRequest</code> instance requests the creation of a
 * destination on a given server.
 */
public class CreateDestinationRequest extends AdminRequest
{
  /** Id of the server where deploying the destination. */
  private int serverId;

  /** Name of the class to be instanciated. */
  private String className = null;
 
  /** Properties needed to create destination object. */
  private Properties prop = null;

  /**
   * Constructs a <code>CreateDestinationRequest</code> instance.
   *
   * @param serverId   The id of the server where deploying the destination.
   * @param className  Name of the class to be instanciated.
   */
  public CreateDestinationRequest(int serverId,
                                  String className) {
    this.serverId = serverId;
    this.className = className;
  }

  /** Returns the id of the server where deploying the destination. */
  public int getServerId() {
    return serverId;
  }

  /** Returns the class name of destination (queue, topic, ...). */
  public String getClassName() {
    return className;
  }

  /** Sets the destination properties. */
  public void setProperties(Properties prop) {
    this.prop = prop;
  }

  /** Returns the destination properties. */
  public Properties getProperties() {
    return prop;
  }
}
