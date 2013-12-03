/**
 * (C) 2013 ScalAgent Distributed Technologies
 * All rights reserved
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
