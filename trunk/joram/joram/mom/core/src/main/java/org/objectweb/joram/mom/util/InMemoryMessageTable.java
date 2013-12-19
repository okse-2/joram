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

import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

public class InMemoryMessageTable implements MessageTable {
  
  public static Logger logger = Debug.getLogger(InMemoryMessageTable.class.getName());
  
  private HashMap<String, Message> map;
  
  public InMemoryMessageTable() {
    map = new HashMap<String, Message>();
  }

  public int getConsumedMemory() {
    return -1;
  }

  public void put(Message msg) {
    map.put(msg.getId(), msg);
  }

  public void checkConsumedMemory() {
    // No swap
  }

  public Message get(String msgId) {
    return map.get(msgId);
  }

  public int clean(long currentTime, DMQManager dmqManager) {
    int res = 0;
    for (Iterator<Message> values = map.values().iterator(); values.hasNext();) {
      Message message = values.next();
      if ((message == null) || message.isValid(currentTime))
        continue;

      values.remove();
      if (message.durableAcksCounter > 0)
        message.delete();

      res++;
      dmqManager.addDeadMessage(message.getFullMessage(), MessageErrorConstants.EXPIRED);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "UserAgent expired message " + message.getId());
    }
    return res;
  }

  public void remove(String msgId) {
    map.remove(msgId);
  }

  public int size() {
    return map.size();
  }

  public HashMap<String, Message> getMap() {
    return map;
  }

}
