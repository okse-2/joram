/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2013 ScalAgent Distributed Technologies
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
import java.util.Map;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import org.objectweb.joram.client.jms.admin.AdminMessage;
import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * Implements the <code>javax.jms.Message</code> interface.
 * <p>
 * A Joram message encapsulates a proprietary message which is used for effective
 * MOM transport facility. It defines the message header and properties, and the
 * acknowledge method used for all messages.
 * <p>
 * JMS messages are composed of the following parts:<ul>
 * <li>Header - All messages support the same set of header fields. Header fields contain
 * values used by both clients and providers to identify and route messages.</li>
 * <li>Properties - Each message contains a built-in facility for supporting application-defined
 * property values. Properties provide an efficient mechanism for supporting message filtering.</li>
 * <li>Body - The JMS API defines several types of message body, which cover the majority of
 * messaging styles currently in use.</li>
 * </ul>
 * The JMS API defines five types of message body:<ul>
 * <li>Stream - A StreamMessage object's message body contains a stream of primitive values.</li>
 * <li>Map - A MapMessage object's message body contains a set of name-value pairs, where names
 * are String objects, and values are Java primitives.</li>
 * <li>Text - A TextMessage object's message body contains a java.lang.String object.</li>
 * <li>Object - An ObjectMessage object's message body contains a Serializable Java object.</li>
 * <li>Bytes - A BytesMessage object's message body contains a stream of uninterpreted bytes.</li>
 * </ul>
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

  /**
   * Set the compressed min size.
   * If set, overload the value.
   * @param compressedMinSize the compressed min size value
   */
  public void setCompressedMinSize(int compressedMinSize) {
    momMsg.compressedMinSize = compressedMinSize;
  }
  
  /**
   * @return the compressed min size value
   */
  public int getCompressedMinSize() {
    return momMsg.compressedMinSize;
  }
  
  /**
   * @return true if compressed
   */
  public boolean isCompressed() {
    return momMsg.compressed;
  }
  
  /**
   * Set the compression level.
   * if set, overload the value.
   * @param compressionLevel the compression level (0-9)
   */
  public void setCompressionLevel(int compressionLevel) {
    momMsg.compressionLevel = compressionLevel;
  }
  
  /**
   * @return the compression level
   */
  public int getCompressionLevel() {
    return momMsg.compressionLevel;
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
   * Acknowledges all previously consumed messages of the session of this consumed message.
   * <p>
   * All consumed JMS messages support the acknowledge method for use when a client has specified
   * that its JMS session's consumed messages are to be explicitly acknowledged. By invoking acknowledge
   * on a consumed message, a client implicitly acknowledges all messages consumed by the session that
   * the message was delivered to.
   * <p>
   * Calls to acknowledge are ignored for both transacted sessions and sessions specified to use implicit
   * acknowledgement modes.
   * <p>
   * Messages that have been received but not acknowledged may be redelivered.
   *
   * @exception IllegalStateException  If the session is closed.
   * @exception JMSException  If the acknowledgement fails for any other reason.
   */
  public void acknowledge() throws JMSException {
    if (session.getAcknowledgeMode() == Session.INDIVIDUAL_ACKNOWLEDGE)
      session.acknowledge((org.objectweb.joram.client.jms.Destination)getJMSDestination(), getJMSMessageID());
    
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
   * Clears the message's body.
   * <p>
   * Calling this method leaves the message body in the same state as an empty body in
   * a newly created message.
   *
   * @exception JMSException  Actually never thrown.
   */
  public void clearBody() throws JMSException {
    momMsg.clearBody();
    RObody = false;
  }

  /** set message read-only */
  private void setReadOnly() {
    propertiesRO = true;
    RObody = true;
  }

  /**
   * API method.
   * Returns the unique message identifier.
   * <p>
   * The JMSMessageID header field contains a value that uniquely identifies each message
   * sent by Joram. When a message is sent, JMSMessageID is ignored, when the send or publish
   * method returns, it contains an unique identifier assigned by Joram.
   * <p>
   * All JMSMessageID values starts with the prefix 'ID:'.
   * 
   * @return the unique message identifier.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final String getJMSMessageID() throws JMSException {
    return momMsg.id;
  }
 
  /**
   * API method.
   * Sets the message identifier.
   * <p>
   * This field is set when a message is sent, this method can only be used to change
   * the value for a message that has been received.
   * 
   * @param id the identifier for this message. 
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void setJMSMessageID(String id) throws JMSException {
    momMsg.id = id;
  }
 
  /**
   * API method.
   * Returns the message priority.
   * <p>
   * The JMS API defines ten levels of priority value, with 0 as the lowest priority
   * and 9 as the highest. In addition, clients should consider priorities 0-4 as
   * gradations of normal priority and priorities 5-9 as gradations of expedited priority.
   * <p>
   * Default prioprity is defined by Message.DEFAULT_PRIORITY.
   * 
   * @return the message priority.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final int getJMSPriority() throws JMSException {
    return momMsg.priority;
  }
   
  /**
   * API method.
   * Sets the priority level for this message.
   * <p>
   * This field is set when a message is sent, this method can be used to change the value
   * for a message that has been received.
   * 
   * @param priority the priority of this message. 
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
   * Returns the message destination. This field is set by the provider at sending, it
   * contains the destination to which the message is being sent.
   * <p>
   * When a message is sent, this field is ignored. After completion of the send or publish
   * method, the field holds the destination specified by the method.
   * <p>
   * When a message is received, its JMSDestination value must be equivalent to the value
   * assigned when it was sent. This field can be overloaded for received messages.
   * 
   * @return the destination of this message.
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
   * API method.
   * Set the message destination.
   * <p>
   * This field is set when message is sent, this method can only be used to change the
   * value for a message that has been received.
   * 
   * @param dest the destination for this message.
   *
   * @exception JMSException  If the destination id not a Joram's one.
   */
  public final void setJMSDestination(javax.jms.Destination dest) throws JMSException {
    jmsDest = dest;
    if ((dest != null) &&
        (dest instanceof org.objectweb.joram.client.jms.Destination)) {
      Destination d = (org.objectweb.joram.client.jms.Destination) dest;
      momMsg.setDestination(d.getName(), d.getType());
      return;
    }
    momMsg.setDestination(null, (byte) 0);
  }

  /**
   * API method.
   * Returns the message expiration time.
   * <p>
   * When a message is sent, the JMSExpiration header field is ignored. After completion of
   * the send or publish method, it holds the expiration time of the message. This is the sum
   * of the time-to-live value specified by the client and the GMT at the time of the send or
   * publish.
   * <p>
   * If the time-to-live is specified as zero, JMSExpiration is set to zero to indicate that
   * the message does not expire.
   * <p>
   * When a message's expiration time is reached, it is either discarded or forwarded to a
   * DeadMessageQueue.
   * 
   * @return the time the message expires, which is the sum of the time-to-live value specified
   *         by the client and the GMT at the time of the send.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final long getJMSExpiration() throws JMSException {
    return momMsg.expiration;
  }

  /**
   * API method.
   * Sets the message's expiration value.
   * <p>
   * This field is set when a message is sent, this method can only be used to change
   * the value for a message that has been received.
   * 
   * @param expiration the message's expiration time.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void setJMSExpiration(long expiration) throws JMSException {
    if (expiration >= 0)
      momMsg.expiration = expiration;
  }

  /**
   * API method.
   * Gets an indication of whether this message is being redelivered.
   * <p>
   * If a client receives a message with the JMSRedelivered field set, it can
   * access the JMSXDeliveryCount property to determine the number of attempts
   * to deliver this message.
   * 
   * @return true if this message is being redelivered.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final boolean getJMSRedelivered() throws JMSException {
    return momMsg.redelivered;
  }

  /**
   * API method.
   * Specifies whether this message is being redelivered.
   * <p>
   * This field is set at the time the message is delivered, this method can only
   * be used to change the value for a message that has been received.
   * 
   * @param redelivered an indication of whether this message is being redelivered. 
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void setJMSRedelivered(boolean redelivered) throws JMSException {
    momMsg.redelivered = redelivered;
  }

  /**
   * API method.
   * Gets the Destination object to which a reply to this message should be sent.
   *
   * @return Destination to which to send a response to this message.
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
   * Sets the Destination object to which a reply to this message should be sent.
   * <p>
   * The JMSReplyTo header field contains the destination where a reply to the current
   * message should be sent. The destination may be either a Queue object or a Topic object.
   * 
   * @param replyTo Destination to which to send a response to this message.
   *
   * @exception JMSException  If the destination id not a Joram's one.
   */
  public final void setJMSReplyTo(javax.jms.Destination replyTo) throws JMSException {
    if ((replyTo != null) &&
        (replyTo instanceof org.objectweb.joram.client.jms.Destination)) {
      Destination d = (org.objectweb.joram.client.jms.Destination) replyTo;
      momMsg.setReplyTo(d.getName(), d.getType());
      return;
    }
    momMsg.setReplyTo(null, (byte) 0);
  }

  /**
   * API method.
   * Returns the message time stamp.
   * <p>
   * The JMSTimestamp header field contains the time a message was handed off to Joram to be sent.
   * It is not the time the message was actually transmitted, because the actual send may occur
   * later due to transactions or other client-side queueing of messages.
   * <p>
   * When a message is sent, JMSTimestamp is ignored. When the send or publish method returns, it
   * contains a time value somewhere in the interval between the call and the return.
   * <p>
   * Since timestamps take some effort to create and increase a message's size, some Joram allows
   * to optimize message overhead if they are given a hint that the timestamp is not used by an
   * application. By calling the MessageProducer.setDisableMessageTimestamp method, a JMS client
   * enables this potential optimization for all messages sent by that message producer.
   * 
   * @return the message timestamp.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final long getJMSTimestamp() throws JMSException {
    return momMsg.timestamp;
  }
  
  /**
   * API method.
   * Sets the message timestamp.
   * <p>
   * This field is set when a message is sent, this method can only be used to change
   * the value for a message that has been received.
   * 
   * @param timestamp the timestamp for this message. 
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void setJMSTimestamp(long timestamp) throws JMSException {
    momMsg.timestamp = timestamp;
  }

  /**
   * API method.
   * Returns the message correlation identifier.
   * 
   * @return the correlation ID for the message.
   * 
   * @exception JMSException  Actually never thrown.
   */
  public final String getJMSCorrelationID() throws JMSException {
    return momMsg.correlationId;
  }

  /**
   * API method.
   * Sets the correlation identifier for the message.
   * <p>
   * A client can use the JMSCorrelationID header field to link one message with
   * another. A typical use is to link a response message with its request message.
   * 
   * @param correlationID the message ID of a message being referred to. 
   *
   * @exception JMSException  Actually never thrown.
   */ 
  public final void setJMSCorrelationID(String correlationID) throws JMSException {
    momMsg.correlationId = correlationID;
  }
  
  /**
   * API method.
   * Gets the correlation ID as an array of bytes for the message.
   * 
   * @return the correlation ID for the message as an array of bytes.
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
   * Sets the correlation ID as an array of bytes for the message.
   * <p>
   * The use of a byte[] value for JMSCorrelationID is non-portable.
   * 
   * @param correlationID the message ID value as an array of bytes. 
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void setJMSCorrelationIDAsBytes(byte[] correlationID) {
    momMsg.correlationId = ConversionHelper.toString(correlationID);
  }

  /**
   * API method.
   * Gets the DeliveryMode value specified for this message.
   * <p>
   * The delivery modes supported are DeliveryMode.PERSISTENT and DeliveryMode.NON_PERSISTENT.
   * 
   * @return the delivery mode for this message.
   *
   * @exception JMSException  Actually never thrown.
   * 
   * @see javax.jms.DeliveryMode
   */
  public final int getJMSDeliveryMode() throws JMSException {
    if (momMsg.persistent) 
      return javax.jms.DeliveryMode.PERSISTENT;
    return javax.jms.DeliveryMode.NON_PERSISTENT;
  }

  /**
   * API method.
   * Sets the DeliveryMode value for this message.
   * <p>
   * JMS providers set this field when a message is sent. This method can be used
   * to change the value for a message that has been received.
   * 
   * @param deliveryMode the delivery mode for this message. 
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
   * Gets the message type identifier supplied by the client when the message was sent.
   * 
   * @return the message type
   *
   * @exception JMSException  Actually never thrown.
   */
  public final String getJMSType() throws JMSException {
    return momMsg.jmsType;
  }

  /**
   * API method.
   * Sets the message type.
   * <p>
   * Joram does not define a standard message definition repository, this field can
   * be used freely by the JMS applications.
   * 
   * @param type the message type. 
   *
   * @exception JMSException  Actually never thrown.
   */
  public final void setJMSType(String type) throws JMSException {
    momMsg.jmsType = type;
  }

  // =========================================================
  // API part about properties
  // =========================================================

  /** <code>true</code> if the properties are read-only. */
  public boolean propertiesRO = false;

  /**
   * API method.
   * Clears the message's properties.
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
   * Indicates whether a property value exists.
   *
   * @param name  the name of the property to test.
   * @return true if the property exists.
   *
   * @exception JMSException  Actually never thrown.
   */
  public final boolean propertyExists(String name) throws JMSException {
    if (momMsg.properties == null)
      return false;

    return momMsg.properties.containsKey(name);
  }

  /**
   * Copies all of the mappings from the properties of this message to
   * the specified map. These mappings will replace any mappings that
   * this Map had for any of the keys currently in the properties.
   */
  public void getProperties(Map h) {
    if (momMsg.properties == null) return;
    momMsg.properties.copyInto(h);
  }

  /**
   * API method.
   * Returns an Enumeration of all the property names.
   * <p>
   * Note that JMS standard header fields are not considered properties and are not
   * returned in this enumeration.
   *
   * @return An enumeration of all the names of property values.
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
   * Sets a boolean property value with the specified name into the message.
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
   * Sets a byte property value with the specified name into the message.
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
   * Sets a short property value with the specified name into the message.
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
   * Sets an int property value with the specified name into the message.

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
   * Sets a long property value with the specified name into the message.

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
   * Sets a floaf property value with the specified name into the message.

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
   * Sets a double property value with the specified name into the message.

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
   * Sets a String property value with the specified name into the message.

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
   * API method.
   * Sets an object property value.
   * <p>
   * Note that this method works only for the objectified primitive object
   * types (Integer, Double, Long ...) and String objects.
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
        momMsg.setProperty(name, ConversionHelper.toString(value));
      } else if (name.equals("JMSXGroupSeq")) {
        try {
          momMsg.setProperty(name, new Integer(ConversionHelper.toInt(value)));
        } catch (MessageValueException mE) {
          throw new MessageFormatException(mE.getMessage());
        }
      } else {
        throw new JMSException("Property names with prefix 'JMSX' are reserved.");
      }
    } else if (name.startsWith("JMS_JORAM")) {
      if (propertiesRO)
        throw new MessageNotWriteableException("Can't set property as the message properties are READ-ONLY.");
      momMsg.setProperty(name, value);
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
   * Returns the value of the boolean property with the specified name.
   *
   * @param name  The property name.
   * @return the property value for the specified name.
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
   * Returns the value of the byte property with the specified name.
   *
   * @param name  The property name.
   * @return the property value for the specified name.
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
   * Returns the value of the short property with the specified name.
   *
   * @param name  The property name.
   * @return the property value for the specified name.
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
   * Returns the value of the int property with the specified name.
   *
   * @param name  The property name.
   * @return the property value for the specified name.
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
   * Returns the value of the long property with the specified name.
   *
   * @param name  The property name.
   * @return the property value for the specified name.
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
   * Returns the value of the float property with the specified name.
   *
   * @param name  The property name.
   * @return the property value for the specified name.
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
   * Returns the value of the double property with the specified name.
   *
   * @param name  The property name.
   * @return the property value for the specified name.
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
   * Returns the value of the String property with the specified name.
   *
   * @param name  The property name.
   * @return the property value for the specified name.
   *
   * @exception MessageFormatException  If the property type is invalid.
   * @exception JMSException  If the name is invalid.
   */
  public final String getStringProperty(String name) throws JMSException {
      return ConversionHelper.toString(doGetProperty(name));
  }

  /**
   * API method.
   * Returns the value of the object property with the specified name.
   * <p>
   * This method can be used to return, in objectified format, an object that has
   * been stored as a property in the message with the equivalent setObjectProperty
   * method call, or its equivalent primitive settypeProperty method.
   * 
   * @param name The property name.
   * @return the Java object property value with the specified name, in objectified
   * format; if there is no property by this name, a null value is returned.
   *
   * @exception JMSException  If the name is invalid.
   */
  public final Object getObjectProperty(String name) throws JMSException {
    return doGetProperty(name);
  }

  /**
   * Method actually getting a property.
   * @return the property value for the specified name.
   *
   * @param name  The property name.
   */
  private final Object doGetProperty(String name) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    if (name.equals("JMSXDeliveryCount")) {
      return new Integer(momMsg.deliveryCount);
    }
    return momMsg.getProperty(name);
  }

  /**
   * Converts a non-Joram JMS message into a Joram message.
   *
   * @param jmsMsg a JMS message.
   * @return a Joram message.
   * 
   * @exception JMSException  If an error occurs while building the message.
   */
  static public Message convertJMSMessage(javax.jms.Message jmsMsg) throws JMSException {
    if (jmsMsg == null) return null;
    
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
    joramMsg.setJMSExpiration(jmsMsg.getJMSExpiration());

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
   * Prepare a JMS message for sending.
   */
  public static void prepareJMSMessage(Message msg) throws JMSException {
    msg.prepare();
  }
  
  /**
   * Method preparing the message for sending; resets header values, and
   * serializes the body (done in subclasses).

   * @throws JMSException 
   */
  protected void prepare() throws JMSException {
    momMsg.redelivered = false;
    momMsg.deliveryCount = 0;
  }

  
  /**
   * @return the momMsg
   */
  public org.objectweb.joram.shared.messages.Message getMomMsg() {
    return momMsg;
  }

  public final String toString() {
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
      if (!momMsg.isNullBody())
        strbuf.append(",size=").append(momMsg.getBodyLength());
      strbuf.append(')');
    } catch (JMSException exc) {
      // Should never happened
      logger.log(BasicLevel.ERROR, "Message.toString()", exc);
    }
  }
}
