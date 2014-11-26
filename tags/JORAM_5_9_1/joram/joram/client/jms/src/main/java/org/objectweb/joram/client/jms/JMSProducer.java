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
 * Initial developer(s):
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.jms.CompletionListener;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.IllegalStateRuntimeException;
import javax.jms.InvalidDestinationException;
import javax.jms.InvalidDestinationRuntimeException;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageFormatRuntimeException;
import javax.jms.MessageNotWriteableException;
import javax.jms.MessageNotWriteableRuntimeException;

import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import fr.dyade.aaa.common.Debug;

public class JMSProducer implements javax.jms.JMSProducer {
  public static Logger logger = Debug.getLogger(JMSProducer.class.getName());
  
  private CompletionListener completionListener = null;
  String jmsCorrelationID = null;
  private String jmsType = null;
  private MessageProducer messageProducer = null;
  private Map<String, Object> properties; // store properties
  private Destination replyTo = null;
  private Session session = null;

  public JMSProducer(Session session) throws JMSException {
    try {
      this.session = session;
      this.properties= new HashMap<String, Object>();
      messageProducer = new MessageProducer(this.session, null);
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO, "JMSProducer created with [ producer = " + messageProducer +" | session = "+session+" ] ");
    } catch (JMSException e) {
      logger.log(BasicLevel.DEBUG,
          "Unable to instantiate a JMSProducer" + e.getMessage());
      throw new JMSException("Unable to instantiate a JMSProducer");
    }
  }

  public javax.jms.JMSProducer setAsync(CompletionListener completionListener) {
    this.completionListener = completionListener;
    return this;
  }

  public CompletionListener getAsync() {
    return completionListener;
  }

  /**
   * Send message to destination
   *
   * @param destination
   * @param message
   *          message to send
   * @throws JMSException
   *           if error occurs
   */
  private void doSend(Destination destination, Message message) {
    try {
      message = writeCurrentProperties(message);
      if (getAsync() == null) {
        messageProducer.send(destination, message);
      } else {
        messageProducer.send(destination, message, completionListener);
      }
    } catch (MessageNotWriteableException e) {
      throw new MessageNotWriteableRuntimeException(e.getMessage());
    } catch (UnsupportedOperationException e) {
      throw new InvalidDestinationRuntimeException(e.getMessage());
    } catch (MessageFormatException e) {
      throw new MessageFormatRuntimeException(e.getMessage());
    } catch (InvalidDestinationException e) {
      throw new InvalidDestinationRuntimeException(e.getMessage());
    } catch (IllegalStateException e) {
      throw new IllegalStateRuntimeException(e.getMessage());
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage());
    }
  }

  /**
   * API method
   */
  public long getDeliveryDelay() {
    try {
      return messageProducer.getDeliveryDelay();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to get delivery delay");
    }
  }

  /**
   * API method
   */
  public int getDeliveryMode() {
    try {
      return messageProducer.getDeliveryMode();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to get delivery mode");
    }
  }

  /**
   * API method
   */
  public boolean getDisableMessageID() {
    try {
      return messageProducer.getDisableMessageID();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to get disable message id");
    }
  }

  /**
   * API method
   */
  public boolean getDisableMessageTimestamp() {
    try {
      return messageProducer.getDisableMessageTimestamp();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to get disable message timestamp");
    }
  }

  public String getJMSCorrelationID() {
    return jmsCorrelationID;
  }

  public javax.jms.JMSProducer setJMSCorrelationID(String correlationID) {
    jmsCorrelationID = correlationID;
    return this;
  }

  public byte[] getJMSCorrelationIDAsBytes() {
    return jmsCorrelationID.getBytes();
  }

  public javax.jms.JMSProducer setJMSCorrelationIDAsBytes(byte[] correlationID) {
    jmsCorrelationID = ConversionHelper.toString(correlationID);
    return this;
  }

  public Destination getJMSReplyTo() {
    return replyTo;
  }

  public String getJMSType() {
    return jmsType;
  }

  public int getPriority() {
    try {
      return messageProducer.getPriority();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to get priority");
    }
  }

  public Set<String> getPropertyNames() {
    return properties.keySet();
  }

  public long getTimeToLive() {
    try {
      return messageProducer.getTimeToLive();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to get time to live");
    }
  }

  public boolean propertyExists(String name) {
    return properties.containsKey(name);
  }

  /**
   * JMS 2.0 API method.
   */
  public javax.jms.JMSProducer send(Destination destination, byte[] body) {
    javax.jms.BytesMessage message = null;
    try {
      message = session.createBytesMessage();
      message.writeBytes(body);
    } catch (MessageNotWriteableException e) {
      throw new MessageNotWriteableRuntimeException(e.getMessage());
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage());
    }
    doSend(destination, message);
    return this;
  }

  /**
   * JMS 2.0 API method.
   */
  public javax.jms.JMSProducer send(Destination destination, Map<String, Object> body) {
    javax.jms.MapMessage message = null;
    try {
      message = session.createMapMessage();
      for (String key : body.keySet()) {
        message.setObject(key, body.get(key));
      }
    } catch (MessageFormatException e) {
      throw new MessageFormatRuntimeException(e.getMessage());
    } catch (MessageNotWriteableException e) {
      throw new MessageNotWriteableRuntimeException(e.getMessage());
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage());
    }
    doSend(destination, message);
    return this;
  }

  /**
   * JMS 2.0 API method.
   */
  public javax.jms.JMSProducer send(Destination destination, Message message) {
    doSend(destination, message);
    return this;
  }

  /**
   * JMS 2.0 API method.
   */
  public javax.jms.JMSProducer send(Destination destination, Serializable body) {
    javax.jms.ObjectMessage message = null;
    try {
      message = session.createObjectMessage(body);
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage());
    }
    doSend(destination, message);
    return this;
  }

  /**
   * JMS 2.0 API method.
   */
  public javax.jms.JMSProducer send(Destination destination, String body) {
    try {
      TextMessage message = (TextMessage) session.createTextMessage(body);
      doSend(destination, message);
    } catch (JMSException e) {
      String errorMessage = "Unable to send message: " + body + " to "
          + destination+ "using session: "+ session;
      if (e instanceof MessageFormatException) {
        throw new MessageFormatRuntimeException(errorMessage);
      } else if (e instanceof InvalidDestinationException) {
        throw new InvalidDestinationRuntimeException(errorMessage);
      } else {
        throw new JMSRuntimeException(errorMessage);
      }
    }
    return this;
  }

  public javax.jms.JMSProducer setDeliveryDelay(long deliveryDelay) {
    try {
      messageProducer.setDeliveryDelay(deliveryDelay);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to set delivery delay");
    }
    return this;
  }

  public javax.jms.JMSProducer setDeliveryMode(int deliveryMode) {
    try {
      messageProducer.setDeliveryMode(deliveryMode);
      return this;
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to set delivery mode");
    }
  }

  /**
   * API method
   */
  public javax.jms.JMSProducer setDisableMessageID(boolean value) {
    try {
      messageProducer.setDisableMessageID(value);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to set disable message id");
    }
    return this;
  }

  /**
   * API method
   */
  public javax.jms.JMSProducer setDisableMessageTimestamp(boolean value) {
    try {
      messageProducer.setDisableMessageTimestamp(value);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to set disable message timestamp");
    }
    return this;
  }

  public javax.jms.JMSProducer setJMSReplyTo(Destination replyTo) {
    this.replyTo = replyTo;
    return this;
  }

  public javax.jms.JMSProducer setJMSType(String type) {
    this.jmsType = type;
    return this;
  }

  public javax.jms.JMSProducer setPriority(int priority) {
    try {
      messageProducer.setPriority(priority);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to set priority");
    }
    return this;
  }

  public javax.jms.JMSProducer setTimeToLive(long timeToLive) {
    try {
      messageProducer.setTimeToLive(timeToLive);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to set time to live");
    }
    return this;
  }

  /**
   * Writes properties previously stored, in message and sets reply destination
   *
   * @param Message
   *          message
   * @throws JMSException
   *           when property is not assignable to message
   */
  private Message writeCurrentProperties(Message message) throws JMSException {
    if (jmsCorrelationID != null)
      message.setJMSCorrelationID(jmsCorrelationID);
    if (jmsType != null)
      message.setJMSType(jmsType);
    if (replyTo != null)
      message.setJMSReplyTo(replyTo);
    
    for (String key : properties.keySet()) {
//      if (properties.get(key) instanceof Boolean) {
//        message.setBooleanProperty(key,
//            ((Boolean) properties.get(key)).booleanValue());
//      } else if (properties.get(key) instanceof Long) {
//        message.setLongProperty(key, ((Long) properties.get(key)).longValue());
//      } else if (properties.get(key) instanceof Short) {
//        message.setShortProperty(key,
//            ((Short) properties.get(key)).shortValue());
//      } else if (properties.get(key) instanceof Integer) {
//        message.setIntProperty(key, ((Integer) properties.get(key)).intValue());
//      } else if (properties.get(key) instanceof Byte) {
//        message.setLongProperty(key, ((Byte) properties.get(key)).byteValue());
//      } else if (properties.get(key) instanceof Float) {
//        message.setFloatProperty(key,
//            ((Float) properties.get(key)).floatValue());
//      } else if (properties.get(key) instanceof Double) {
//        message.setDoubleProperty(key,
//            ((Double) properties.get(key)).doubleValue());
//      } else if (properties.get(key) instanceof String) {
//        message.setStringProperty(key, (String) properties.get(key));
//      } else {
        message.setObjectProperty(key, properties.get(key));
//      }
    }

    if (replyTo != null)
      message.setJMSReplyTo(replyTo);
    return message;
  }
  
  // Getter/Setter for properties
  
  private final Object doGetProperty(String name) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    return properties.get(name);
  }
  
  public final String getString2Property(String name) throws JMSException {
    return ConversionHelper.toString(doGetProperty(name));
  }

  public javax.jms.JMSProducer clearProperties() {
    properties.clear();
    return this;
  }

  public boolean getBooleanProperty(String name) {
    if (name == null || name.equals(""))
      throw new JMSRuntimeException("Invalid property name: " + name);
    
//    if (!propertyExists(name)) return false;
    
    try {
      return ConversionHelper.toBoolean(properties.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatRuntimeException(mE.getMessage());
    }

//    if (properties.get(name) instanceof Boolean) {
//      return ((Boolean) properties.get(name)).booleanValue();
//    } else {
//      if (!propertyExists(name)) {
//        throw new JMSRuntimeException("Unable to get property associated to: " + name);
//      } else {
//        throw new MessageFormatRuntimeException("Unable to convert property associated to: " + name);
//      }
//    }
  }

  public byte getByteProperty(String name) {
    if (name == null || name.equals(""))
      throw new JMSRuntimeException("Invalid property name: " + name);
    
    try {
      return ConversionHelper.toByte(properties.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatRuntimeException(mE.getMessage());
    }
    
//    if (propertyExists(name) && (properties.get(name) instanceof Byte)) {
//      return ((Byte) properties.get(name)).byteValue();
//    } else {
//      if (!propertyExists(name)) {
//        throw new JMSRuntimeException("unable to get property associated to: " + name);
//      } else {
//        throw new MessageFormatRuntimeException("Unable to convert property associated to: " + name);
//      }
//    }
  }

  public short getShortProperty(String name) {
    if (name == null || name.equals(""))
      throw new JMSRuntimeException("Invalid property name: " + name);
    
    try {
      return ConversionHelper.toShort(properties.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatRuntimeException(mE.getMessage());
    }
//    if (propertyExists(name) && (properties.get(name) instanceof Short)) {
//      return ((Short) properties.get(name)).shortValue();
//    } else {
//      if (!propertyExists(name)) {
//        throw new JMSRuntimeException("unable to get property associated to: " + name);
//      } else {
//        throw new MessageFormatRuntimeException("Unable to convert property associated to: " + name);
//      }
//    }
  }

  public int getIntProperty(String name) {
    if (name == null || name.equals(""))
      throw new JMSRuntimeException("Invalid property name: " + name);
    
    try {
      return ConversionHelper.toInt(properties.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatRuntimeException(mE.getMessage());
    }
//    if (propertyExists(name) && (properties.get(name) instanceof Integer)) {
//      return ((Integer) properties.get(name)).intValue();
//    } else {
//      if (!propertyExists(name)) {
//        throw new JMSRuntimeException("unable to get property associated to: " + name);
//      } else {
//        throw new MessageFormatRuntimeException("Unable to convert property associated to: " + name);
//      }
//    }
  }

  public long getLongProperty(String name) {
    if (name == null || name.equals(""))
      throw new JMSRuntimeException("Invalid property name: " + name);
    
    try {
      return ConversionHelper.toLong(properties.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatRuntimeException(mE.getMessage());
    }
//    if (propertyExists(name) && (properties.get(name) instanceof Long)) {
//      return ((Long) properties.get(name)).longValue();
//    } else {
//      if (!propertyExists(name)) {
//        throw new JMSRuntimeException("unable to get property associated to: " + name);
//      } else {
//        throw new MessageFormatRuntimeException("Unable to convert property associated to: " + name);
//      }
//    }
  }

  public float getFloatProperty(String name) {
    if (name == null || name.equals(""))
      throw new JMSRuntimeException("Invalid property name: " + name);
    
    try {
      return ConversionHelper.toFloat(properties.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatRuntimeException(mE.getMessage());
    }
//    if (propertyExists(name) && (properties.get(name) instanceof Float)) {
//      return ((Float) properties.get(name)).floatValue();
//    } else {
//      if (!propertyExists(name)) {
//        throw new JMSRuntimeException("unable to get property associated to: " + name);
//      } else {
//        throw new MessageFormatRuntimeException("Unable to convert property associated to: " + name);
//      }
//    }
  }

  public double getDoubleProperty(String name) {
    if (name == null || name.equals(""))
      throw new JMSRuntimeException("Invalid property name: " + name);
    
    try {
      return ConversionHelper.toDouble(properties.get(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatRuntimeException(mE.getMessage());
    }
//    if (propertyExists(name) && (properties.get(name) instanceof Double)) {
//      return ((Double) properties.get(name)).doubleValue();
//    } else {
//      if (!propertyExists(name)) {
//        throw new JMSRuntimeException("unable to get property associated to: " + name);
//      } else {
//        throw new MessageFormatRuntimeException("Unable to convert property associated to: " + name);
//      }
//    }
  }

  public String getStringProperty(String name) {
    if (name == null || name.equals(""))
      throw new JMSRuntimeException("Invalid property name: " + name);
    
    return ConversionHelper.toString(properties.get(name));

//    if (propertyExists(name) && (properties.get(name) instanceof String)) {
//      return ((String) properties.get(name)) ;
//    } else {
//      if (!propertyExists(name)) {
//        throw new JMSRuntimeException("unable to get property associated to: " + name);
//      } else {
//        throw new MessageFormatRuntimeException("Unable to convert property associated to: " + name);
//      }
//    }
  }

  public Object getObjectProperty(String name) {
    if (name == null || name.equals(""))
      throw new JMSRuntimeException("Invalid property name: " + name);

    return properties.get(name);
  }

  public javax.jms.JMSProducer setProperty(String name, boolean value) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    properties.put(name, value);
    return this;
  }

  public javax.jms.JMSProducer setProperty(String name, byte value) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);
    
    properties.put(name, value);
    return this;
  }

  public javax.jms.JMSProducer setProperty(String name, double value) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);
    
    properties.put(name, value);
    return this;
  }

  public javax.jms.JMSProducer setProperty(String name, float value) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    properties.put(name, value);
    return this;
  }

  public javax.jms.JMSProducer setProperty(String name, int value) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    properties.put(name, value);
    return this;
  }

  public javax.jms.JMSProducer setProperty(String name, long value) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    properties.put(name, value);
    return this;
  }

  public javax.jms.JMSProducer setProperty(String name, Object value) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    if (value instanceof Boolean ||
        value instanceof Number ||
        value instanceof String) {
      properties.put(name, value);
    } else {
      throw new MessageFormatRuntimeException("Can't set non primitive Java object as a property value.");
    }
    return this;
  }

  public javax.jms.JMSProducer setProperty(String name, short value) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    properties.put(name, value);
    return this;
  }

  public javax.jms.JMSProducer setProperty(String name, String value) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    properties.put(name, value);
    return this;
  }

}
