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

import java.util.Iterator;

public interface MessageIdList {
  
  int size();
  
  String[] toArray(String[] array);
  
  boolean contains(String msgId);
  
  /**
   * Adds a message identifier and indicates whether the identifier should be
   * persistently added or not.
   * 
   * @param msgId
   *          the added message identifier
   * @param persistent
   *          indicates whether the identifier should be persistently added or
   *          not
   */
  void add(String msgId, boolean persistent);
  
  boolean isEmpty();
  
  String remove(int index);
  
  String get(int index);
  
  void remove(String msgId);
  
  /**
   * Adds a message identifier at the psecified index and indicates whether the
   * identifier should be persistently added or not.
   * 
   * @param index
   *          the index of the added message identifier
   * @param msgId
   *          the added message identifier
   * @param persistent
   *          indicates whether the identifier should be persistently added or
   *          not
   */
  void add(int index, String msgId, boolean persistent);
  
  Iterator<String> iterator();
  
  void clear();
  
  /**
   * Saves the list in the persistent storage.
   * @throws Exception
   */
  void save() throws Exception;
  
  /**
   * Deletes the list from the persistent storage.
   */
  void delete();
  
}
