/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.util;

import java.util.Map;

import org.objectweb.joram.mom.messages.Message;

public interface MessageTable {
  
  /**
   * Returns the amount of memory in bytes
   * consumed by this message table.
   * @return amount of memory in bytes
   */
  int getConsumedMemory();
  
  /**
   * Adds a message in this table.
   * @param msg the message to be added
   */
  void put(Message msg);
  
  /**
   * Checks whether the consumed memory
   * is allowed and performs the
   * appropriate actions.
   */
  void checkConsumedMemory();
  
  /**
   * Returns the specified message.
   * @param msgId the identifier of the message to return
   * @return the specified message
   */
  Message get(String msgId);
  
  /**
   * Cleans the table from invalid
   * messages. The detected invalid messages should be
   * removed from the table, deleted if necessary (if persistent), and
   * added to the dead message queue manager.
   * The number of messages to be checked depends
   * on the implementation, i.e. all the messages from the
   * table do not have to be checked.
   * @param currentTime the time from which messages are considered invalid
   * @param dmqManager the dead message queue manager
   * @return the number of invalid messages
   */
  int clean(long currentTime, DMQManager dmqManager);
  
  /**
   * Removes the specified message.
   * @param msgId the identifier of the message to remove
   */
  void remove(String msgId);

  /**
   * Returns the exact number of all the messages 
   * that are contained by this table.
   * @return the number of messages in this table
   */
  int size();
  
  /**
   * Returns a map of messages from this table indexed
   * by their message identifiers.
   * A subset of the messages may be returned
   * depending on the implementation.
   * All the messages from the
   * table do not have to be returned.
   * @return a map of messages from this table.
   */
  Map<String, Message> getMap();
  
}
