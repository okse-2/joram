/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 2003 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.mom.util;

import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.messages.MessageType;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import javax.jms.BytesMessage;
import javax.jms.IllegalStateException;
import javax.jms.MapMessage;
import javax.jms.MessageFormatException;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.JMSException;


/**
 * The <code>MessageConverterModule</code> is a utility class used for
 * converting JORAM MOM messages into foreign JMS messages and foreign
 * JMS messages into JORAM MOM messages.
 */
public class MessageConverterModule
{
  /**
   * Converts a standard JMS message into an
   * <code>org.objectweb.joram.mom.shared.messages.Message</code> instance.
   *
   * @exception javax.jms.MessageFormatException  If a problem occurs while
   *              reading the JMS message or writing the MOM message.
   */
  public static Message convert(javax.jms.Message jmsMessage)
                throws MessageFormatException 
  {
    if (jmsMessage == null) return null;

    Message msg = Message.create();

    try {
      if (jmsMessage instanceof BytesMessage) {
        // Building a Bytes message.
        long length = ((BytesMessage) jmsMessage).getBodyLength();
        byte[] bytes = new byte[(new Long(length)).intValue()];
        ((BytesMessage) jmsMessage).readBytes(bytes);
        msg.setBytes(bytes);
      } else if (jmsMessage instanceof MapMessage) {
        // Building a Map message.
        String name;
        HashMap map = new HashMap();
        for (Enumeration names = ((MapMessage) jmsMessage).getMapNames();
             names.hasMoreElements();) {
          name = (String) names.nextElement();
          map.put(name, ((MapMessage) jmsMessage).getObject(name));
        }
        msg.setMap(map);
      } else if (jmsMessage instanceof ObjectMessage) {
        // Building an Object message.
        msg.setObject(((ObjectMessage) jmsMessage).getObject());
      } else if (jmsMessage instanceof TextMessage) {
        // Building a Text message.
        msg.setText(((TextMessage) jmsMessage).getText());
      } else if (jmsMessage instanceof StreamMessage) {
        // Building a Stream message.
        int length = 0;
        try {
          while (true) {
            ((StreamMessage) jmsMessage).readByte();
            length++;
          }
        } catch (Exception exc) {}
        byte[] bytes = new byte[length];
        ((StreamMessage) jmsMessage).readBytes(bytes);
        msg.setStream(bytes);
      }

      // Setting the CorrelationID field.
      msg.setCorrelationId(jmsMessage.getJMSCorrelationID());
      // Setting the DeliveryMode field.
      msg.setPersistent(jmsMessage.getJMSDeliveryMode() ==
                        javax.jms.DeliveryMode.PERSISTENT);

      // DF: comment this code because it is not 
      // compatible with typed destinations
      // Setting the DestinationID field.
      // javax.jms.Destination dest = jmsMessage.getJMSDestination();
//       if (dest instanceof javax.jms.Queue)
//         msg.setDestination(((javax.jms.Queue) dest).getQueueName(), true);
//       else if (dest instanceof javax.jms.Topic)
//         msg.setDestination(((javax.jms.Topic) dest).getTopicName(), false);



      // Setting the Expiration field.
      msg.setExpiration(jmsMessage.getJMSExpiration());
      // Setting the MessageID field.
      msg.setIdentifier(jmsMessage.getJMSMessageID());
      // Setting the Priority field.
      msg.setPriority(jmsMessage.getJMSPriority());

      // DF: comment this code because it is not 
      // compatible with typed destinations
      //
      // Setting the ReplyTo field.
      // dest = jmsMessage.getJMSReplyTo();
//       if (dest != null && dest instanceof javax.jms.Queue)
//         msg.setReplyTo(((javax.jms.Queue) dest).getQueueName(), true);
//       else if (dest != null && dest instanceof javax.jms.Topic)
//         msg.setReplyTo(((javax.jms.Topic) dest).getTopicName(), false);



      // Setting the Timestamp field.
      msg.setTimestamp(jmsMessage.getJMSTimestamp());
      // Setting the Type field.
      if (jmsMessage.getJMSType() != null)
        msg.setOptionalHeader("JMSType", jmsMessage.getJMSType());
  
      // Setting the properties.
      String name;
      for (Enumeration names = jmsMessage.getPropertyNames();
           names.hasMoreElements();) { 
        name = (String) names.nextElement();
        msg.setObjectProperty(name, jmsMessage.getObjectProperty(name));
      }
    }
    catch (JMSException exc) {
      throw new MessageFormatException("Error while reading the foreign "
                                       + "JMS message: " + exc);
    }
    catch (Exception exc) {
      throw new MessageFormatException("Error while writing the MOM message: "
                                       + exc);
    }
    return msg;
  }

  /**
   * Converts a JORAM MOM message into a JMS message.
   *
   * @exception javax.jms.IllegalStateException  If the module's JMS session
   *              state does not allow to create a JMS message.
   * @exception javax.jms.MessageFormatException  If an error occurs while
   *              constructing the foreign JMS message or reading the MOM
   *              message.
   */
  public static javax.jms.Message 
                convert(javax.jms.Session jmsSession, Message momMessage)
                throws JMSException
  {
    javax.jms.Message jmsMessage = null;

    try {
      // Building a Bytes message.
      if (momMessage.getType() == MessageType.BYTES)
        jmsMessage = jmsSession.createBytesMessage();
      // Building a Map message.
      else if (momMessage.getType() == MessageType.MAP)
        jmsMessage = jmsSession.createMapMessage();
      // Building an Object message.
      else if (momMessage.getType() == MessageType.OBJECT)
        jmsMessage = jmsSession.createObjectMessage();
      // Building a Text message.
      else if (momMessage.getType() == MessageType.TEXT)
        jmsMessage = jmsSession.createTextMessage();
      // Building a Stream message.
      else if (momMessage.getType() == MessageType.STREAM)
        jmsMessage = jmsSession.createStreamMessage();
    }
    // Session is probably closed.
    catch (Exception exc) {
      throw new IllegalStateException("" + exc);
    }

    try {
      // Filling in the Bytes message.
      if (momMessage.getType() == MessageType.BYTES)
        ((BytesMessage) jmsMessage).writeBytes(momMessage.getBytes());
      // Filling in the Map message.
      else if (momMessage.getType() == MessageType.MAP) {
        Map map = momMessage.getMap();
        String name;
        if (map.keySet() != null) {
          for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
            name = (String) it.next();
            ((MapMessage) jmsMessage).setObject(name, map.get(name));
          }
        }
      }
      // Filling in the Object message.
      else if (momMessage.getType() == MessageType.OBJECT) {
        Serializable obj = (Serializable) momMessage.getObject();
        ((ObjectMessage) jmsMessage).setObject(obj);
      }
      // Filling in the Text message.
      else if (momMessage.getType() == MessageType.TEXT)
        ((TextMessage) jmsMessage).setText(momMessage.getText());
      // Filling in the Stream message.
      else if (momMessage.getType() == MessageType.STREAM)
        ((StreamMessage) jmsMessage).writeBytes(momMessage.getStream());

      // Setting the CorrelationID field.
      jmsMessage.setJMSCorrelationID(momMessage.getCorrelationId());
      // Setting the DeliveryMode field.
      if (momMessage.getPersistent())
        jmsMessage.setJMSDeliveryMode(javax.jms.DeliveryMode.PERSISTENT);
      else
        jmsMessage.setJMSDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
      // Setting the Expiration field.
      jmsMessage.setJMSExpiration(momMessage.getExpiration());
      // Setting the Priority field.
      jmsMessage.setJMSPriority(momMessage.getPriority());


      // DF: comment this code because it is not 
      // compatible with typed destinations
      // Setting the ReplyTo field.
      // String destId = momMessage.getReplyToId();
//       if (destId != null) {
//         if (momMessage.replyToQueue())
//           jmsMessage.setJMSReplyTo(jmsSession.createQueue(destId));
//         else
//           jmsMessage.setJMSReplyTo(jmsSession.createTopic(destId));
//       }



      // Setting the Timestamp field.
      jmsMessage.setJMSTimestamp(momMessage.getTimestamp());
      // Setting the Type field.
      if (momMessage.getOptionalHeader("JMSType") != null)
        jmsMessage.setJMSType((String) momMessage.getOptionalHeader("JMSType"));

      // Setting the properties.
      String name;
      for (Enumeration names = momMessage.getPropertyNames();
           names.hasMoreElements();) { 
        name = (String) names.nextElement();
        jmsMessage.setObjectProperty(name, momMessage.getObjectProperty(name));
      }
    }
    catch (JMSException exc) {
      throw new MessageFormatException("Error while writing the foreign "
                                       + "JMS message: " + exc);
    }
    catch (Exception exc) {
      throw new MessageFormatException("Error while reading the MOM message: "
                                       + exc);
    }
    return jmsMessage;
  }
}
