/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import org.objectweb.joram.client.jms.admin.AdminMessage;
import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

/**
 * Implements the <code>javax.jms.Message</code> interface.
 * <p>
 * A Joram message encapsulates a proprietary message which is also used
 * for effective MOM transport facility.
 */
public class Message implements javax.jms.Message {
  /** logger */
  public static Logger logger = Debug.getLogger(Message.class.getName());

  protected org.objectweb.joram.shared.messages.Message momMsg;

  /**
   * Constructs a bright new <code>Message</code>.
   */
  protected Message() {
    momMsg = new org.objectweb.joram.shared.messages.Message();
  }

  public static Message wrapMomMessage(Session session,
                                       org.objectweb.joram.shared.messages.Message momMsg) throws JMSException {
    switch (momMsg.type) {
    case org.objectweb.joram.shared.messages.Message.SIMPLE:
      return new Message(session, momMsg);
    case org.objectweb.joram.shared.messages.Message.TEXT:
      return new TextMessage(session, momMsg);
    case org.objectweb.joram.shared.messages.Message.OBJECT:
      return new ObjectMessage(session, momMsg);
    case org.objectweb.joram.shared.messages.Message.MAP:
      return new MapMessage(session, momMsg);
    case org.objectweb.joram.shared.messages.Message.STREAM:
      return new StreamMessage(session, momMsg);
    case org.objectweb.joram.shared.messages.Message.BYTES:
      return new BytesMessage(session, momMsg);
    case org.objectweb.joram.shared.messages.Message.ADMIN:
      return new AdminMessage(session, momMsg);
    default:
      throw new JMSException("Unknow message type: " + momMsg.type);
    }
  }

  /**
   * If the message is actually consumed, the session that consumes it, 
   * <code>null</code> otherwise.
   */
  protected transient Session session = null;

  /**
   *  The JMSDestination field. This field is only use with non Joram
   * destination.
   */
  protected transient javax.jms.Destination jmsDest = null;

  /**
   * Instantiates a <code>Message</code> wrapping a consumed
   * MOM simple message.
   *
   * @param session  The consuming session.
   * @param momMsg  The MOM message to wrap.
   */
  protected Message(Session session,
                    org.objectweb.joram.shared.messages.Message momMsg) {
    this.session = session;
    this.momMsg = momMsg;
    setReadOnly();
  } 

  /**
   * API method.
   *
   * @exception IllegalStateException  If the session is closed.
   * @exception JMSException  If the acknowledgement fails for any other
   *              reason.
   */
  public void acknowledge() throws JMSException {
    if ((session == null) ||
        session.getTransacted() ||
        (session.getAcknowledgeMode() != javax.jms.Session.CLIENT_ACKNOWLEDGE))
      return;
    session.acknowledge();
  }

  /** <code>true</code> if the message body is read-only. */
  protected boolean RObody = false; 

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void clearBody() throws JMSException {
    momMsg.body = null;
    RObody = false;
  }

  /** set message read-only */
  public void setReadOnly() {
    propertiesRO = true;
    RObody = true;
  }

  /**
   * Returns the message identifier.
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final String getJMSMessageID() throws JMSException {
    return momMsg.id;
  }
 
  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void setJMSMessageID(String id) throws JMSException {
    momMsg.id = id;
  }
 
  /**
   * Returns the message priority.
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final int getJMSPriority() throws JMSException {
    return momMsg.priority;
  }
   
  /**
   * API method.
   *
   * @exception JMSException  If the priority value is incorrect.
   */
  public final void setJMSPriority(int priority) throws JMSException {
    if (priority >= 0 && priority <= 9)
      momMsg.priority = priority;
    else
      throw new JMSException("Priority of "+ priority +" is not valid"
                             + " (should be an integer between 0 and 9).");
  }

  /**
   * Returns the message destination.
   * This field is set by <code>Session.send()</code>, it can be overloaded
   * for received messages.
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final javax.jms.Destination getJMSDestination() throws JMSException {
    if (jmsDest != null) return jmsDest;

    if (momMsg.toId != null) {
      try {
        return Destination.newInstance(momMsg.toId, null, momMsg.toType);
      } catch (Exception exc) {
        // The destination name is unknown
        throw new JMSException(exc.getMessage());
      }
    }
    return null;
  }

  /**
   * Set the message destination.
   * This field is set when message is sent. This method can be used to
   * change the value for a message that has been received.
   * API method.
   *
   * @exception JMSException  If the destination id not a Joram's one.
   */
  public final void setJMSDestination(javax.jms.Destination dest) throws JMSException {
    jmsDest = dest;
    if (dest == null) {
      momMsg.setDestination(null, null);
    } else if (dest instanceof org.objectweb.joram.client.jms.Destination) {
      Destination d = (org.objectweb.joram.client.jms.Destination) dest;
      momMsg.toId = d.getName();
      momMsg.toType = d.getType();
    }
  }

  /**
   * Returns the message expiration time.
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final long getJMSExpiration() throws JMSException {
    return momMsg.expiration;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void setJMSExpiration(long expiration) throws JMSException {
    if (expiration >= 0)
      momMsg.expiration = expiration;
  }

  /**
   * Gets an indication of whether this message is being redelivered.
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final boolean getJMSRedelivered() throws JMSException {
    return momMsg.redelivered;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void setJMSRedelivered(boolean redelivered) throws JMSException {
    momMsg.redelivered = redelivered;
  }

  /**
   * Gets the Destination object to which a reply to this message should
   * be sent.
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final javax.jms.Destination getJMSReplyTo() throws JMSException {
    if (momMsg.replyToId != null) {
      // The destination name is unknown
      try {
        return Destination.newInstance(momMsg.replyToId, null, momMsg.replyToType);
      } catch (Exception exc) {
        throw new JMSException(exc.getMessage());
      }
    }
    return null;
  }

  /**
   * API method.
   *
   * @exception JMSException  If the destination id not a Joram's one.
   */
  public final void setJMSReplyTo(javax.jms.Destination replyTo) throws JMSException {
    try {
      Destination d = (org.objectweb.joram.client.jms.Destination) replyTo;
      momMsg.replyToId = d.getName();
      momMsg.replyToType = d.getType();
    } catch (NullPointerException npe) {
      momMsg.replyToId = null;
      momMsg.replyToType = null;
    } catch (ClassCastException cce) {
      throw new JMSException("Destination is not Joram compatible.");
    }
  }

  /**
   * Returns the message time stamp.
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final long getJMSTimestamp() throws JMSException {
    return momMsg.timestamp;
  }
  
  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void setJMSTimestamp(long timestamp) throws JMSException {
    momMsg.timestamp = timestamp;
  }

  /**
   * Returns the message correlation identifier.
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final String getJMSCorrelationID() throws JMSException {
    return momMsg.correlationId;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */ 
  public final void setJMSCorrelationID(String correlationID) throws JMSException {
    momMsg.correlationId = correlationID;
  }
  
  /**
   * API method.
   *
   * @exception MessageFormatException  In case of a problem while retrieving
   *              the field. 
   */
  public final byte[] getJMSCorrelationIDAsBytes() throws JMSException {
    try {
      return ConversionHelper.toBytes(momMsg.correlationId);
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }
  
  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void setJMSCorrelationIDAsBytes(byte[] correlationID) {
    momMsg.correlationId = ConversionHelper.toString(correlationID);
  }

  /**
   * Returns <code>true</code> if the message is persistent.
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final int getJMSDeliveryMode() throws JMSException {
    if (momMsg.persistent) 
      return javax.jms.DeliveryMode.PERSISTENT;
    else
      return javax.jms.DeliveryMode.NON_PERSISTENT;
  }

  /**
   * API method.
   *
   * @exception JMSException  If the delivery mode is incorrect.
   */
  public final void setJMSDeliveryMode(int deliveryMode) throws JMSException {
    if (deliveryMode != javax.jms.DeliveryMode.PERSISTENT &&
        deliveryMode != javax.jms.DeliveryMode.NON_PERSISTENT)
      throw new JMSException("Invalid delivery mode.");

    momMsg.persistent = (deliveryMode == javax.jms.DeliveryMode.PERSISTENT);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final String getJMSType() throws JMSException {
    return ConversionHelper.toString(momMsg.getOptionalHeader("JMSType"));
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void setJMSType(String type) throws JMSException {
    momMsg.setOptionalHeader("JMSType", type);
  }

  /**
   *  Copies all of the mappings from the optionalHeader of this message to
   * the specified hashtable. These mappings will replace any mappings that
   * this Hashtable had for any of the keys currently in the optional header.
   */
  public void getOptionalHeader(Hashtable h) {
    if (momMsg.optionalHeader == null) return;
    momMsg.optionalHeader.copyInto(h);
  }

  // =========================================================
  // API part about properties
  // =========================================================

  /** <code>true</code> if the properties are read-only. */
  public boolean propertiesRO = false;

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void clearProperties() throws JMSException {
    propertiesRO = false;
    if (momMsg.properties == null) return;

    momMsg.properties.clear();
    momMsg.properties = null;
  } 

  /**
   * Resets the read-only flag, in order to allow the modification
   * of message properties.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void resetPropertiesRO() throws JMSException {
    propertiesRO = false;
  } 

  /**
   * API method.
   *
   * @param name  The property name.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final boolean propertyExists(String name) throws JMSException {
    if (momMsg.properties == null)
      return false;

    return momMsg.properties.containsKey(name);
  }

  /**
   *  Copies all of the mappings from the properties of this message to
   * the specified hashtable. These mappings will replace any mappings that
   * this Hashtable had for any of the keys currently in the properties.
   */
  public void getProperties(Hashtable h) {
    if (momMsg.properties == null) return;
    momMsg.properties.copyInto(h);
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final Enumeration getPropertyNames() throws JMSException {
    if (momMsg.properties == null)
      return (new Hashtable()).keys();

    return momMsg.properties.keys();
  }

  /**
   * API method.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public final void setBooleanProperty(String name, boolean value) throws JMSException {
    doSetProperty(name, new Boolean(value));
  }

  /**
   * API method.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public final void setByteProperty(String name, byte value) throws JMSException {
    doSetProperty(name, new Byte(value));
  }

  /**
   * API method.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public final void setShortProperty(String name, short value) throws JMSException {
    doSetProperty(name, new Short(value));
  }

  /**
   * API method.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public final void setIntProperty(String name, int value) throws JMSException {
    doSetProperty(name, new Integer(value));
  }

  /**
   * API method.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public final void setLongProperty(String name, long value) throws JMSException {
    doSetProperty(name, new Long(value));
  }

  /**
   * API method.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public final void setFloatProperty(String name, float value) throws JMSException {
    doSetProperty(name, new Float(value));
  }

  /**
   * API method.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public final void setDoubleProperty(String name, double value) throws JMSException {
    doSetProperty(name, new Double(value));
  }

  /**
   * API method.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid.
   */
  public final void setStringProperty(String name, String value) throws JMSException {
    doSetProperty(name, value);
  }

  /**
   * API method, sets a property value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException
   * 	If the message properties are read-only.
   * @exception MessageFormatException
   *	If the value is not a Java primitive object.
   * @exception IllegalArgumentException
   *	If the key name is illegal (null or empty string).
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the property name is invalid, or if the
   *              		object is invalid.
   */
  public final void setObjectProperty(String name, Object value) throws JMSException {
    if (value instanceof Boolean ||
        value instanceof Number ||
        value instanceof String) {
      doSetProperty(name, value);
    } else {
      throw new MessageFormatException("Can't set non primitive Java object as a property value.");
    }
  }
 
  /**
   * Method actually setting a new property.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception MessageNotWriteableException  If the message is read-only.
   * @exception JMSException  If the name is invalid.
   * @exception IllegalArgumentException  If the name string is null or empty.
   */
  private final void doSetProperty(String name, Object value) throws JMSException {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    if (name.startsWith("JMSX")) {
      if (name.equals("JMSXGroupID")) {
        momMsg.setOptionalHeader(name, ConversionHelper.toString(value));
      } else if (name.equals("JMSXGroupSeq")) {
        try {
          momMsg.setOptionalHeader(name,
                                   new Integer(ConversionHelper.toInt(value)));
        } catch (MessageValueException mE) {
          throw new MessageFormatException(mE.getMessage());
        }
      } else {
        throw new JMSException("Property names with prefix 'JMSX' are reserved.");
      }
    } else if (name.startsWith("JMS")) {
      throw new JMSException("Property names with prefix 'JMS' are  reserved.");
    } else if (name.equalsIgnoreCase("NULL") ||
               name.equalsIgnoreCase("TRUE") ||
               name.equalsIgnoreCase("FALSE") ||
               name.equalsIgnoreCase("NOT") ||
               name.equalsIgnoreCase("AND") ||
               name.equalsIgnoreCase("OR") ||
               name.equalsIgnoreCase("BETWEEN") ||
               name.equalsIgnoreCase("LIKE") ||
               name.equalsIgnoreCase("IN") ||
               name.equalsIgnoreCase("IS") ||
               name.equalsIgnoreCase("ESCAPE")) {
      throw new JMSException("Invalid property name cannot use SQL terminal: " + name);
    } else {
      if (propertiesRO)
        throw new MessageNotWriteableException("Can't set property as the message properties are READ-ONLY.");

      momMsg.setProperty(name, value);
    }
  }

  /**
   * API method.
   *
   * @param name  The property name.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public final boolean getBooleanProperty(String name) throws JMSException {
    try {
      return ConversionHelper.toBoolean(doGetProperty(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }
  
  /**
   * API method.
   *
   * @param name  The property name.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public final byte getByteProperty(String name) throws JMSException {
    try {
      return ConversionHelper.toByte(doGetProperty(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @param name  The property name.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public final short getShortProperty(String name) throws JMSException {
    try {
      return ConversionHelper.toShort(doGetProperty(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @param name  The property name.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public final int getIntProperty(String name) throws JMSException {
    try {
      return ConversionHelper.toInt(doGetProperty(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @param name  The property name.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public final long getLongProperty(String name) throws JMSException {
    try {
      return ConversionHelper.toLong(doGetProperty(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @param name  The property name.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public final float getFloatProperty(String name) throws JMSException {
    try {
      return ConversionHelper.toFloat(doGetProperty(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @param name  The property name.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public final double getDoubleProperty(String name) throws JMSException {
    try {
      return ConversionHelper.toDouble(doGetProperty(name));
    } catch (MessageValueException mE) {
      throw new MessageFormatException(mE.getMessage());
    }
  }

  /**
   * API method.
   *
   * @param name  The property name.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public final String getStringProperty(String name) throws JMSException {
      return ConversionHelper.toString(doGetProperty(name));
  }

  /**
   * API method.
   *
   * @param name  The property name.
   *
   * @exception JMSException  If the name is invalid.
   */
  public final Object getObjectProperty(String name) throws JMSException {
    return doGetProperty(name);
  }

  /**
   * Method actually getting a property.
   *
   * @param name  The property name.
   */
  private final Object doGetProperty(String name) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    if (name.startsWith("JMSX")) {
      if (name.equals("JMSXDeliveryCount"))
        return new Integer(momMsg.deliveryCount);
      else
        return momMsg.getOptionalHeader(name);
    } else {
      return momMsg.getProperty(name);
    }
  }

  /**
   * Converts a non-Joram JMS message into a Joram message.
   *
   * @exception JMSException  If an error occurs while building the message.
   */
  static public Message convertJMSMessage(javax.jms.Message jmsMsg) throws JMSException {
    Message joramMsg = null;
    if (jmsMsg instanceof javax.jms.TextMessage) {
      joramMsg = new TextMessage();
      ((javax.jms.TextMessage) joramMsg).setText(((javax.jms.TextMessage) jmsMsg).getText());
    } else if (jmsMsg instanceof javax.jms.ObjectMessage) {
      joramMsg = new ObjectMessage();
      ((javax.jms.ObjectMessage) joramMsg).setObject(((javax.jms.ObjectMessage) jmsMsg).getObject());
    } else if (jmsMsg instanceof javax.jms.StreamMessage) {
      joramMsg = new StreamMessage();
      try {
        ((javax.jms.StreamMessage) jmsMsg).reset();
        while (true)
          ((StreamMessage) joramMsg).writeObject(((javax.jms.StreamMessage) jmsMsg).readObject());
      } catch (Exception mE) {}
    } else if (jmsMsg instanceof javax.jms.BytesMessage) {
      joramMsg = new BytesMessage();
      try {
        ((javax.jms.BytesMessage) jmsMsg).reset();
        while (true)
          ((BytesMessage) joramMsg).writeByte(((javax.jms.BytesMessage) jmsMsg).readByte());
      } catch (Exception mE) {}
    } else if (jmsMsg instanceof javax.jms.MapMessage) {
      joramMsg = new MapMessage();
      Enumeration mapNames = ((javax.jms.MapMessage) jmsMsg).getMapNames();
      String mapName;
      while (mapNames.hasMoreElements()) {
        mapName = (String) mapNames.nextElement();
        ((javax.jms.MapMessage) joramMsg).setObject(mapName, 
                                               ((javax.jms.MapMessage)
                                                jmsMsg).getObject(mapName));
      }
    } else {
      joramMsg = new Message();
    }

    joramMsg.setJMSDestination(jmsMsg.getJMSDestination());
    joramMsg.setJMSCorrelationID(jmsMsg.getJMSCorrelationID());
    joramMsg.setJMSReplyTo(jmsMsg.getJMSReplyTo());
    joramMsg.setJMSType(jmsMsg.getJMSType());
    joramMsg.setJMSMessageID(jmsMsg.getJMSMessageID());

    Enumeration names = jmsMsg.getPropertyNames();
    if (names != null) {
      String name;
      while (names.hasMoreElements()) {
        name = (String) names.nextElement();
        try {
          joramMsg.setObjectProperty(name, jmsMsg.getObjectProperty(name));
        } catch (JMSException e) {
          // Joram not support other Optional JMSX, just ignore.
          if (! name.startsWith("JMSX") && ! name.startsWith("JMS_"))
            throw e;
        }
      }
    }

    return joramMsg;
  }

  /**
   * Method preparing the message for sending; resets header values, and
   * serializes the body (done in subclasses).
   *
   * @exception MessageFormatException  If an error occurs while serializing.
   */
  protected void prepare() throws JMSException {
    momMsg.redelivered = false;
  }

  
  /**
   * @return the momMsg
   */
  public org.objectweb.joram.shared.messages.Message getMomMsg() {
    return momMsg;
  }

  public String toString() {
      StringBuffer strbuf = new StringBuffer();
      toString(strbuf);
      return strbuf.toString();
  }
  
  public void toString(StringBuffer strbuf) {
    try {
      strbuf.append('(');
      strbuf.append(super.toString());
      strbuf.append(",JMSMessageID=").append(getJMSMessageID());
      try {
        strbuf.append(",JMSDestination=").append(getJMSDestination());
      } catch (JMSException exc) {
        logger.log(BasicLevel.ERROR, "Message.toString()", exc);
      }
      strbuf.append(",JMSCorrelationID=").append(getJMSCorrelationID());
      strbuf.append(",JMSDeliveryMode=").append(getJMSDeliveryMode());
      strbuf.append(",JMSExpiration=").append(getJMSExpiration());
      strbuf.append(",JMSPriority=").append(getJMSPriority());
      strbuf.append(",JMSRedelivered=").append(getJMSRedelivered());
      try {
        strbuf.append(",JMSReplyTo=").append(getJMSReplyTo());
      } catch (JMSException exc) {
        logger.log(BasicLevel.ERROR, "Message.toString()", exc);
      }
      strbuf.append(",JMSTimestamp=").append(getJMSTimestamp());
      strbuf.append(",JMSType=").append(getJMSType());
      strbuf.append(')');
    } catch (JMSException exc) {
      // Should never happened
    }
  }
}
