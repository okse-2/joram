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

import fr.dyade.aaa.agent.AgentServer;

public abstract class MessageIdListFactory {
  
  public static final String MESSAGE_ID_LIST_FACTORY_CLASS =
      "org.objectweb.joram.mom.util.message.id.list.factory.class";
  
  public static MessageIdListFactory newFactory() {
    String factoryClassName = AgentServer
        .getProperty(MESSAGE_ID_LIST_FACTORY_CLASS);
    if (factoryClassName == null) {
      return new MessageIdListImplFactory();
    } else {
      try {
        Class factoryClass = null;
        if (factoryClass == null) {
          factoryClass = (Class) Class.forName(factoryClassName);
        }
        return (MessageIdListFactory) factoryClass.newInstance();
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    }
  }
  
  /**
   * Creates a new MessageIdList with the specified identifier.
   * @param listId
   * @return
   */
  public abstract MessageIdList createMessageIdList(String listId);
  
  /**
   * Loads the specified MessageIdList from persistent storage.
   * @param listId
   * @return
   * @throws Exception
   */
  public abstract MessageIdList loadMessageIdList(String listId) throws Exception;

}
