/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package com.scalagent.joram.mom.dest.collector;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.util.Properties;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Debug;
import fr.dyade.aaa.agent.WakeUpTask;

/**
 * 
 */
public class CollectorHelper {
  public static Logger logger = Debug.getLogger(CollectorHelper.class.getName());
  public static final long DEFAULT_PERIODE = 60000L;
  
  /**
   * create shared message.
   * 
   * @param type message type.
   * @param body message body.
   * @param prop message properties
   * @param expiration message expiration.
   * @param persistent is message persistent.
   * @param identifier message identifier.
   * @return shared Message.
   */
  public static Message createMessage(
      int type, 
      byte[] body, 
      Properties prop,
      long expiration,
      boolean persistent,
      String identifier) {
    Message msg = new Message();
    // set message type
    if (type > 0)
      msg.type = type;
    else
      msg.type = Message.BYTES;
    
    msg.body = body;
    msg.properties = prop;
    msg.id = identifier;
    
    return msg;
  }
  
  /**
   * create client message.
   * 
   * @param msg the shared message
   * @return ClientMessages.
   */
  public static ClientMessages createClientMessages(Message msg) {
    return new ClientMessages(-1, -1, msg);
  }
  
  /**
   * convert the string period to long and schedule task.
   * 
   * @param task the collector task.
   * @param periodStr the period in ms.
   */
  public static void scheduleTask(WakeUpTask task, String periodStr) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "CollectorHelper.scheduleTask(" + periodStr + ')'); 
    long collectorPeriod = DEFAULT_PERIODE;
    if (periodStr != null)
      collectorPeriod = Long.valueOf(periodStr).longValue();
    // set collector task period
    task.schedule(collectorPeriod);
  }
  
  /**
   * cancel task.
   * 
   * @param task the collector task.
   */
  public static void cancelTask(WakeUpTask task) {
    try {
      // cancel all collector task
      task.cancel();
    } catch (Exception e) {
      //nothing to do
    }
  }
}
