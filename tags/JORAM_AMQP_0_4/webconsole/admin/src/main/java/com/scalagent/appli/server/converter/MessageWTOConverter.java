/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package com.scalagent.appli.server.converter;

import java.util.List;

import org.objectweb.joram.mom.messages.MessageView;

import com.scalagent.appli.shared.MessageWTO;

/**
 * @author Yohann CINTRE
 */
public class MessageWTOConverter {

  /**
   * @param msg
   *          a MessageView containing the message info
   * @return a MessageWTO object created from the MessageView object
   */
  public static MessageWTO getMessageWTO(MessageView msg) {

    MessageWTO result = new MessageWTO(msg.getId(), msg.getExpiration(), msg.getTimestamp(),
        msg.getDeliveryCount(), msg.getPriority(), msg.getText(), msg.getType(), msg.getProperties());

    return result;
  }

  /**
   * @param msgs
   *          a List of MessageView
   * @return an array of MessageWTO
   */
  public static MessageWTO[] getMessageWTOArray(List<MessageView> msgs) {

    try {
      MessageWTO[] newMessagesWTO = new MessageWTO[msgs.size()];

      int i = 0;
      for (MessageView itemMsg : msgs) {
        newMessagesWTO[i] = MessageWTOConverter.getMessageWTO(itemMsg);
        i++;
      }
      return newMessagesWTO;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}