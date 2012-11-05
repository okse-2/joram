/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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

import fr.dyade.aaa.agent.AgentId;

import java.util.Properties;

/**
 * The <code>AdminDestinationItf</code> interface defines the method needed
 * by the administration topic for creating and initializing a destination
 * agent.
 * <p>
 * For being administerable by the administration topic a destination agent
 * must implement this interface.
 *
 * @see AdminTopicImpl
 */
public interface AdminDestinationItf {
  /** 
   * Initializes the destination and sets the destination properties.
   *
   * @param adminId  	Identifier of the destination administrator.
   * @param prop  	the initial set of properties.
   */
  public void init(AgentId adminId, Properties prop);
}
