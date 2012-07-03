/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - ScalAgent Distributed Technologies
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
package org.ow2.joram.shell.mom.commands;

public interface MOMCommands {
  
  /**
   * List items of the given category
   * @param category Category of item to list: destination, queue, topic, user
   */
  public void list(String[] category);

  /**
   * Creates a new destination
   * @param args parameters of the command
   */
  public void create(String[] args);
  
   /**
   * Delete a destination or a user
   * @param args {category, name}
   */
  public void delete(String[] args);

  /**
   * Add a new user to the servers
   * @param args parameters of the command
   */
  public void addUser(String[] args);

  /**
   * Check the pending count of a queue
   * @param args parameters of the command
   */
  public void queueLoad(String[] args);
  
  /**
   * Check  the load of a subscription
   * @param args parameters of the command
   */
  public void subscriptionLoad(String[] args);
  
  /**
   * Display info about the JORAM server
   * @param args ???
   */
  public void info(String[] args);
  
  /**
   * Display messages in queue
   * @param args {queue name, message range}
   */
  public void lsMsg(String[] args);
 
}
