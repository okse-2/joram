/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.messages;

import org.objectweb.joram.shared.excepts.*;
import org.objectweb.joram.mom.util.MessagePersistenceModule;
import org.objectweb.joram.shared.messages.MessageTracing;
import org.objectweb.util.monolog.api.BasicLevel;

import java.io.*;
import java.util.*;

/** 
 * The <code>Message</code> class actually provides the transport facility
 * for the data exchanged during MOM operations.
 * <p>
 * A message may either carry a String, or a serializable object, or an
 * hashtable, or bytes, even nothing. It is charaterized by properties and
 * "header" fields.
 */
public class Message implements Cloneable, Serializable {
  transient public boolean pin = true;
  transient private String saveName = null;
  transient public boolean toBeUpdated = false;
  transient protected MessagePersistent messagePersistent = null;
  transient public boolean noBody = false;

  /** The message type (SIMPLE, TEXT, OBJECT, MAP, STREAM, BYTES). */
  transient int type;

  /** <code>true</code> if the message must be persisted. **/
  transient boolean persistent = true;

  /** The message identifier. */
  transient String id = null;
  /** The message priority (from 0 to 9, 9 being the highest). */
  transient int priority = 4;
  /** The message expiration time (0 for infinite time-to-live). */
  transient long expiration = 0;
  /** The message time stamp. */
  transient long timestamp;
  /** The message destination identifier. */
  transient String toId = null;
  /** The message destination type. */
  transient String toType;
  /** The correlation identifier field. */
  transient String correlationId = null;
  /** The reply to destination identifier. */
  transient String replyToId = null;
  /** <code>true</code> if the "reply to" destination is a queue. */
  transient String replyToType;

  /**
   * Table holding header fields that may be required by particular
   * clients (such as JMS clients).
   */
  transient Hashtable optionalHeader = null;

  /** <code>true</code> if the body is read-only. */
  transient boolean bodyRO = false;
  transient protected MessageBody body = null;

  /** The message properties table. */
  transient Hashtable properties = null;
  /** <code>true</code> if the properties are read-only. */
  transient boolean propertiesRO = false;

  /** Arrival position of this message on its queue or proxy. */
  transient public long order;
  /** The number of delivery attempts for this message. */
  transient public int deliveryCount = 0;
  /**
   * <code>true</code> if the message has been denied at least once by a
   * consumer.
   */
  transient public boolean denied = false;
  
  /** <code>true</code> if the message target destination is deleted. */
  transient public boolean deletedDest = false;
  /** <code>true</code> if the message expired. */
  transient public boolean expired = false;
  /** <code>true</code> if the message could not be written on the dest. */
  transient public boolean notWriteable = false;
  /** <code>true</code> if the message is considered as undeliverable. */
  transient public boolean undeliverable = false;

  /**
   * The number of acknowledgements a message still expects from its 
   * subscribers before having been fully consumed by them (field used
   * by JMS proxies).
   */
  public transient int acksCounter;
  /**
   * The number of acknowledgements a message still expects from its 
   * durable subscribers before having been fully consumed by them (field used
   * by JMS proxies).
   */
  public transient int durableAcksCounter;

  /**
   * Constructs a <code>Message</code> instance.
   */
  public Message() {
    this.type = MessageType.SIMPLE;
    body = new MessageBody();

    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "Message <init>");
  }

  public static Message create() {
    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "Message.create()");
    Message message;
    try {
      String momMessageClass = 
        System.getProperty("MomMessageClass", 
                           "org.objectweb.joram.shared.messages.Message");
      message =  (Message) Class.forName(momMessageClass).newInstance();
    } catch (Exception exc) {
      MessageTracing.dbgMessage.log(BasicLevel.ERROR,
                                    "Message.create() : " +
                                    "return new org.objectweb.joram.shared.messages.Message",
                                    exc);
      message =  new Message();
    }
      message.setPin(true);
      return message;
  }

  public void setPin(boolean pin) {
    this.pin = pin;
  }

  public boolean isPin() {
    return pin;
  }

  public void setSaveName(String saveName) {
    this.saveName = saveName;
  }

  public String getSaveName() {
    return saveName;
  }

  public MessageBody getMessageBody() {
    if (body == null)
      body = new MessageBody();

    return body;
  }

  public void setMessageBody(MessageBody body) {
    this.body = body;
  }

  /** get bytes body. */
  protected byte[] getBodyBytes() {
    return getMessageBody().getBodyBytes();
  }
  
  /** get map body. */
  protected HashMap getBodyMap() {
    return getMessageBody().getBodyMap();
  }
  
  /** get text body. */
  protected String getBodyText() {
    return getMessageBody().getBodyText();
  }
  
  /** set bytes body. */
  protected void setBodyBytes(byte[] bytes) {
    MessageBody body = getMessageBody();
    body.setType(type);
    body.setBodyBytes(bytes);
  }
  
  /** set map body. */
  protected void setBodyMap(HashMap map) {
    MessageBody body = getMessageBody();
    body.setType(type);
    body.setBodyMap(map);
  }

  /** set text body. */
  protected void setBodyText(String text) {
    MessageBody body = getMessageBody();
    body.setType(type);
    body.setBodyText(text);
  }

  /** Sets the message identifier. */ 
  public void setIdentifier(String id) {
    this.id = id;
  }

  /** Sets the message persistence mode. */
  public void setPersistent(boolean persistent) {
    this.persistent = persistent;
  }

  /**
   * Sets the message priority.
   *
   * @param priority  Priority value: 0 the lowest, 9 the highest, 4 normal.
   */ 
  public void setPriority(int priority) {
    if (priority >= 0 && priority <= 9)
      this.priority = priority;
  }

  /**
   * Sets the message expiration.
   *
   * @param expiration	The expiration time.
   */
  public void setExpiration(long expiration) {
    if (expiration >= 0)
      this.expiration = expiration;
  }

  /** Sets the message time stamp. */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Sets the message destination.
   *
   * @param id  The destination identifier.
   * @param type The type of the destination.
   */
  public void setDestination(String id, String type) {
    this.toId = id;
    this.toType = type;
  }

  /** Sets the message correlation identifier. */
  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  /**
   * Sets the destination to which a reply should be sent.
   *
   * @param id  The destination identifier.
   * @param type The destination type.
   */
  public void setReplyTo(String id, String type) {
    this.replyToId = id;
    this.replyToType = type;
  }

  /**
   * Sets an optional header field value.
   *
   * @param name  The header field name.
   * @param value  The corresponding value.
   */
  public void setOptionalHeader(String name, Object value) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid header name: " + name);

    if (value == null)
      return;

    if (optionalHeader == null)
      optionalHeader = new Hashtable();

    optionalHeader.put(name, value);
  }

  /**
   *  Copies all of the mappings from the optionalHeader of this message to
   * the specified hashtable. These mappings will replace any mappings that
   * this Hashtable had for any of the keys currently in the optional header.
   */
  public void getOptionalHeader(Hashtable h) {
    if (optionalHeader != null)
      h.putAll(optionalHeader);
  }

  /** Returns the message type. */
  public int getType() {
    return type;
  }

  /** Returns the message identifier. */
  public String getIdentifier() {
    return id;
  }

  /** Returns <code>true</code> if the message is persistent. */
  public boolean getPersistent() {
    return persistent;
  }

  /** Returns the message priority. */
  public int getPriority() {
    return priority;
  }

  /** Returns the message expiration time. */
  public long getExpiration() {
    return expiration;
  }
  
  /** Returns the message time stamp. */
  public long getTimestamp() {
    return timestamp;
  }

  /** Returns the message destination identifier. */
  public final String getDestinationId() {
    return toId;
  }

  /** Returns <code>true</code> if the destination is a queue. */
  public final String toType() {
    return toType;
  }

  /** Returns the message correlation identifier. */
  public final String getCorrelationId() {
    return correlationId;
  }

  /** Returns the destination id the reply should be sent to. */
  public final String getReplyToId() {
    return replyToId;
  }

  /** Returns <code>true</code> if the reply to destination is a queue. */
  public final String replyToType() {
    return replyToType;
  }

  /**
   * Returns an optional header field value.
   *
   * @param name  The header field name.
   */
  public Object getOptionalHeader(String name) {
    if (optionalHeader == null)
      return null;

    return optionalHeader.get(name);
  }

  /**
   * Sets a property as a boolean value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setBooleanProperty(String name, boolean value) throws MessageROException {
    setProperty(name, new Boolean(value));
  }

  /**
   * Sets a property as a byte value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setByteProperty(String name, byte value) throws MessageROException {
    setProperty(name, new Byte(value));
  }

  /**
   * Sets a property as a double value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setDoubleProperty(String name, double value) throws MessageROException {
    setProperty(name, new Double(value));
  }

  /**
   * Sets a property as a float value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setFloatProperty(String name, float value) throws MessageROException {
    setProperty(name, new Float(value));
  }

  /**
   * Sets a property as an int value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setIntProperty(String name, int value) throws MessageROException {
    setProperty(name, new Integer(value));
  }

  /**
   * Sets a property as a long value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setLongProperty(String name, long value) throws MessageROException {
    setProperty(name, new Long(value));
  }

  /**
   * Sets a property as a short value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setShortProperty(String name, short value) throws MessageROException {
    setProperty(name, new Short(value));
  }

  /**
   * Sets a property as a String.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setStringProperty(String name, String value) throws MessageROException {
    setProperty(name, value);
  }

  /**
   * Sets a property value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException
   * 	If the message properties are read-only.
   * @exception MessageValueException
   *	If the value is not a Java primitive object.
   * @exception IllegalArgumentException
   *	If the key name is illegal (null or empty string).
   */
  public void setObjectProperty(String name, Object value) throws MessageException {
    if (value instanceof Boolean || value instanceof Number
        || value instanceof String) {
      setProperty(name, value);
    } else {
      throw new MessageValueException("Can't set non primitive Java object"
                                      + " as a property value.");
    }
  }
 
  /**
   * Sets a property value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException
   * 	If the message properties are read-only.
   * @exception IllegalArgumentException
   *	If the key name is illegal (null or empty string).
   */
  private void setProperty(String name, Object value) throws MessageROException {
    if (propertiesRO)
      throw new MessageROException("Can't set property as the message "
                                   + "properties are READ-ONLY.");

    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    if (properties == null) properties = new Hashtable();
    properties.put(name, value);
  }
 
  /**
   * Returns a property as a boolean value.
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public boolean getBooleanProperty(String name) throws MessageValueException  {
    return ConversionHelper.toBoolean(getObjectProperty(name));
  }
  
  /**
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public byte getByteProperty(String name) throws MessageValueException {
    return ConversionHelper.toByte(getObjectProperty(name));
  }

  /**
   * Returns a property as a double value.
   *
   * @param name  The property name.
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public double getDoubleProperty(String name) throws MessageValueException {
    return ConversionHelper.toDouble(getObjectProperty(name));
  }

  /**
   * Returns a property as a float value.
   *
   * @param name  The property name.
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public float getFloatProperty(String name) throws MessageValueException {
    return ConversionHelper.toFloat(getObjectProperty(name));
  }

  /**
   * Returns a property as a int value.
   *
   * @param name  The property name.
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public int getIntProperty(String name) throws MessageValueException {
    return ConversionHelper.toInt(getObjectProperty(name));
  }

  /**
   * Returns a property as a long value.
   *
   * @param name  The property name.
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public long getLongProperty(String name) throws MessageValueException {
    return ConversionHelper.toLong(getObjectProperty(name));
  }

  /**
   * Returns a property as a short value.
   *
   * @param name  The property name.
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public short getShortProperty(String name) throws MessageValueException {
    return ConversionHelper.toShort(getObjectProperty(name));
  }

  /**
   * Returns a property as a String.
   *
   * @param name  The property name.
   */
  public String getStringProperty(String name) {
    return ConversionHelper.toString(getObjectProperty(name));
  }

  /**
   * Returns a property as an object.
   *
   * @param name  The property name.
   */
  public Object getObjectProperty(String name) {
    if (properties == null) return null;
    return properties.get(name);
  }
  
  /**
   * Returns <code>true</code> if a given property exists.
   *
   * @param name  The name of the property to check.
   */
  public boolean propertyExists(String name) {
    if (properties == null)
      return false;

    return properties.containsKey(name);
  }

  /** Returns an enumeration of the properties names. */
  public Enumeration getPropertyNames() {
    if (properties == null)
      return (new Hashtable()).keys();

    return properties.keys();
  }

  /** Empties the properties table. */
  public void clearProperties() {
    propertiesRO = false;

    if (properties == null)
      return;

    properties.clear();
    properties = null;
  }

  /**
   * Resets the read-only flag, in order to allow the modification
   * of message properties.
   */
  public void resetPropertiesRO() {
    propertiesRO = false;
  }
   
  /**
   *  Copies all of the mappings from the properties of this message to
   * the specified hashtable. These mappings will replace any mappings that
   * this Hashtable had for any of the keys currently in the properties.
   */
  public void getProperties(Hashtable h) {
    if (properties != null)
      h.putAll(properties);
  }

  /**
   * Sets an object as the body of the message. 
   *
   * @exception IOException  In case of an error while setting the object.
   * @exception  MessageROException  If the message body is read-only.
   */
  public void setObject(Object object) throws IOException, MessageROException {
    if (bodyRO)
      throw new MessageROException("Can't set the body as it is READ-ONLY.");

    type = MessageType.OBJECT;

    if (object == null)
      setBodyBytes(null);
    else {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(object);
      oos.flush();
      setBodyBytes(baos.toByteArray());
      oos.close();
      baos.close();
    }
  }

  /**
   * Sets a map as the body of the message.
   *
   * @exception IOException  In case of an error while setting the map.
   * @exception MessageROException  If the message body is read-only.
   */
  public void setMap(HashMap map) throws Exception {
    if (bodyRO)
      throw new MessageROException("Can't set the body as it is READ-ONLY.");

    type = MessageType.MAP;
    setBodyMap(map);
  }

  /**
   * Sets a String as the body of the message.
   *
   * @exception MessageROException  If the message body is read-only.
   */
  public void setText(String text) throws MessageROException {
    if (bodyRO)
      throw new MessageROException("Can't set the body as it is READ-ONLY.");

    type = MessageType.TEXT;
    setBodyText(text);
  }

  /**
   * Sets the message body as a stream of bytes. 
   *
   * @exception MessageROException  If the message body is read-only.
   */
  public void setStream(byte[] bytes) throws MessageROException {
    if (bodyRO)
      throw new MessageROException("Can't set the body as it is READ-ONLY.");

    type = MessageType.STREAM;
    setBodyBytes(bytes);
  }

  /**
   * Sets the message body as an array of bytes. 
   *
   * @exception MessageROException  If the message body is read-only.
   */
  public void setBytes(byte[] bytes) throws MessageROException {
    if (bodyRO)
      throw new MessageROException("Can't set the body as it is READ-ONLY.");

    type = MessageType.BYTES;
    setBodyBytes(bytes);
  } 

  /**
   * Returns the object body of the message.
   *
   * @exception IOException  In case of an error while getting the object.
   * @exception ClassNotFoundException  If the object class is unknown.
   */
  public Object getObject() throws Exception {
    if (getBodyBytes() == null)
      return null;
    ByteArrayInputStream bais = new ByteArrayInputStream(getBodyBytes());
    ObjectInputStream ois = new ObjectInputStream(bais);
    return ois.readObject();
  }

  /**
   * Returns the map body of the message.
   */ 
  public Map getMap() {
    return getBodyMap();
  }

  /** Gets the String body of the message. */
  public String getText() {
    return getBodyText();
  }

  /** Returns the stream of bytes body of the message. */
  public byte[] getStream() {
    return getBodyBytes();
  }

  /** Returns the array of bytes body of the message. */
  public byte[] getBytes() {
    return getBodyBytes();
  }

  /** 
   * Method clearing the message body.
   */
  public void clearBody() {
    setBodyBytes(null);
    setBodyMap(null);
    setBodyText(null);
    bodyRO = false;
  }

  /** set message read-only */
  public void setReadOnly() {
    propertiesRO = true;
    bodyRO = true;
  }

  /**
   * Returns <code>true</code> if the message is valid.
   *
   * @param currentTime	The current time to verify the expiration time.
   */
  public boolean isValid(long currentTime) {
    if (expiration == 0)
      return true;

    return ((expiration - currentTime) > 0);
  }

  /** Clones the message. */
  public Object clone() {
    try {
      if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
        MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                      "Message.clone()");
      Message clone = (Message) super.clone();
      clone.setMessageBody((MessageBody) getMessageBody().clone());

      if (optionalHeader != null) {
        clone.optionalHeader = new Hashtable();
        for (Enumeration e = optionalHeader.keys(); e.hasMoreElements(); ) {
          Object key = e.nextElement();
          clone.optionalHeader.put(key,optionalHeader.get(key));
        }
      }
      if (properties != null) {
        clone.properties = new Hashtable();
        for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
          Object key = e.nextElement();
          clone.properties.put(key,properties.get(key));
        }
      }
      if (messagePersistent != null) {
        clone.messagePersistent = (MessagePersistent) messagePersistent.clone();
        clone.messagePersistent.message = clone;
      }
      return clone;
    } catch (CloneNotSupportedException cE) {
      return null;
    }
  }

  /**
   * Transforms this message into a table of primitive values that can
   * be vehiculated through the SOAP protocol.
   */
  public Hashtable soapCode() {
    Hashtable h = new Hashtable();

    // Building a hashtable containg the fields values: 
    Hashtable fieldsTb = new Hashtable();

    fieldsTb.put("type", new Integer(type));
    fieldsTb.put("id", id);
    fieldsTb.put("persistent", new Boolean(persistent));
    fieldsTb.put("nobody", new Boolean(noBody));
    fieldsTb.put("priority", new Integer(priority));
    fieldsTb.put("expiration", new Long(expiration));
    fieldsTb.put("timestamp", new Long(timestamp));
    fieldsTb.put("toId", toId);
    fieldsTb.put("toType", toType);
    if (correlationId != null)
      fieldsTb.put("correlationId", correlationId);
    if (replyToId != null) {
      fieldsTb.put("replyToId", replyToId);
      fieldsTb.put("replyToType", replyToType);
    }
    if (getBodyBytes() != null)
      fieldsTb.put("body_bytes", getBodyBytes());
    else if (getBodyMap() != null)
      fieldsTb.put("body_map", new Hashtable(getBodyMap()));
    else if (getBodyText() != null)
      fieldsTb.put("body_text", getBodyText());
    fieldsTb.put("bodyRO", new Boolean(bodyRO));
    fieldsTb.put("propertiesRO", new Boolean(propertiesRO));
    fieldsTb.put("deliveryCount", new Integer(deliveryCount));
    fieldsTb.put("denied", new Boolean(denied));
    fieldsTb.put("deletedDest", new Boolean(deletedDest));
    fieldsTb.put("expired", new Boolean(expired));
    fieldsTb.put("notWriteable", new Boolean(notWriteable));
    fieldsTb.put("undeliverable", new Boolean(undeliverable));

    h.put("fieldsTb",fieldsTb);

    // Adding the hashtable of optional headers:
    if (optionalHeader != null)
      h.put("optionalHeader",optionalHeader);

    // Adding the hashtable of properties:
    if (properties != null)
      h.put("properties",properties);

    return h;
  }

  /** 
   * Transforms a table of primitive values into a <code>Message</code>
   * instance.
   */
  public static Message soapDecode(Hashtable h) {
    if (h == null) return null;

    Hashtable fieldsTb = (Hashtable) h.get("fieldsTb");

    Message msg = Message.create();

    try {
      msg.type = ConversionHelper.toInt(fieldsTb.get("type"));
      msg.id = (String) fieldsTb.get("id");
      msg.persistent = ConversionHelper.toBoolean(fieldsTb.get("persistent"));
      msg.noBody = ConversionHelper.toBoolean(fieldsTb.get("nobody"));
      msg.priority = ConversionHelper.toInt(fieldsTb.get("priority"));
      msg.expiration = ConversionHelper.toLong(fieldsTb.get("expiration"));
      msg.timestamp = ConversionHelper.toLong(fieldsTb.get("timestamp"));
      msg.toId = (String) fieldsTb.get("toId");
      msg.toType = (String)fieldsTb.get("toType");
      msg.correlationId = (String) fieldsTb.get("correlationId");
      msg.replyToId = (String) fieldsTb.get("replyToId");
      if (msg.replyToId != null) {
        msg.replyToType = (String)fieldsTb.get("replyToType");
      }
      msg.setBodyBytes(ConversionHelper.toBytes(fieldsTb.get("body_bytes")));
      Hashtable ht = (Hashtable) fieldsTb.get("body_map");
      if (ht != null) {
        msg.setBodyMap(new HashMap(ht));
      } else {
        msg.setBodyMap((HashMap) null);
      }
      msg.setBodyText((String) fieldsTb.get("body_text"));
      msg.bodyRO = ConversionHelper.toBoolean(fieldsTb.get("bodyRO"));
      msg.propertiesRO =
        ConversionHelper.toBoolean(fieldsTb.get("propertiesRO"));
      msg.deliveryCount =
        ConversionHelper.toInt(fieldsTb.get("deliveryCount"));
      msg.denied = ConversionHelper.toBoolean(fieldsTb.get("denied"));
      msg.deletedDest =
        ConversionHelper.toBoolean(fieldsTb.get("deletedDest"));
      msg.expired = ConversionHelper.toBoolean(fieldsTb.get("expired"));
      msg.notWriteable =
        ConversionHelper.toBoolean(fieldsTb.get("notWriteable"));
      msg.undeliverable =
        ConversionHelper.toBoolean(fieldsTb.get("undeliverable"));

      msg.optionalHeader = (Hashtable) h.get("optionalHeader");
      msg.properties = (Hashtable) h.get("properties");
      msg.pin = true;
    }
    // Should never happen!
    catch (MessageValueException exc) {}
  
    return msg;
  }

  public void save(String id) {
    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "Message.save(" + id + ')');

    if (! getPersistent()) 
      return;

    if (messagePersistent == null)
      messagePersistent = new MessagePersistent(this);
    messagePersistent.setPin(true);
    setSaveName(MessagePersistenceModule.getSaveName(id,messagePersistent));
    MessagePersistenceModule.save(id,messagePersistent);

    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "Message.save : saveName=" + saveName);
  }

  public void delete() {
    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "Message.delete()");

    if (! getPersistent()) 
      return;
    if (messagePersistent == null)
      messagePersistent = new MessagePersistent(this);
    MessagePersistenceModule.delete(messagePersistent);
  }
  
  private static void writeString(ObjectOutput os,
                                  String s) throws IOException {
    if (s == null) {
      os.writeInt(-1);
    } else if (s.length() == 0) {
      os.writeInt(0);
    } else {
      byte[] bout = s.getBytes();
      os.writeInt(bout.length);
      os.write(bout);
    }
  }

  private static String readString(ObjectInput is) throws IOException {
    int length = is.readInt();
    if (length == -1) {
      return null;
    } else if (length == 0) {
      return "";
    } else {
      byte[] bin = new byte[length];
      is.readFully(bin);
      return new String(bin);
    }
  }

  final static byte BOOLEAN = 0;
  final static byte BYTE = 1;
  final static byte DOUBLE = 2;
  final static byte FLOAT = 3;
  final static byte INTEGER = 4;
  final static byte LONG = 5;
  final static byte SHORT = 6;
  final static byte STRING = 7;

  private static void writeProperties(ObjectOutput os,
                                      Hashtable h) throws IOException {
    if (h == null) {
      os.writeInt(0);
      return;
    }

    os.writeInt(h.size());
    for (Enumeration e = h.keys() ; e.hasMoreElements() ;) {
      String key = (String) e.nextElement();
      writeString(os, key);
      Object value = h.get(key);
      if (value instanceof Boolean) {
        os.writeByte(BOOLEAN);
        os.writeBoolean(((Boolean) value).booleanValue());
      } else if (value instanceof Byte) {
        os.writeByte(BYTE);
        os.writeByte(((Byte) value).byteValue());
      } else if (value instanceof Double) {
        os.writeByte(DOUBLE);
        os.writeDouble(((Double) value).doubleValue());
      } else if (value instanceof Float) {
        os.writeByte(FLOAT);
        os.writeFloat(((Float) value).floatValue());
      } else if (value instanceof Integer) {
        os.writeByte(INTEGER);
        os.writeInt(((Integer) value).intValue());
      } else if (value instanceof Long) {
        os.writeByte(LONG);
        os.writeLong(((Long) value).longValue());
       } else if (value instanceof Short) {
        os.writeByte(SHORT);
        os.writeShort(((Short) value).shortValue());
      } else if (value instanceof String) {
        os.writeByte(STRING);
        writeString(os, (String) value);
      }
     }
  }

  private static Hashtable readProperties(ObjectInput is) throws IOException, ClassNotFoundException {
    int size = is.readInt();
    if (size == 0) return null;

    Hashtable h = new Hashtable(size);
    
    for (int i = 0; i < size; i++) {
      String key = readString(is);
      byte type = is.readByte();
      if (type == BOOLEAN) {
        h.put(key, new Boolean(is.readBoolean()));
      } else if (type == BYTE) {
        h.put(key, new Byte(is.readByte()));
      } else if (type == DOUBLE) {
        h.put(key, new Double(is.readDouble()));
      } else if (type == FLOAT) {
        h.put(key, new Float(is.readFloat()));
      } else if (type == INTEGER) {
        h.put(key, new Integer(is.readInt()));
      } else if (type == LONG) {
        h.put(key, new Long(is.readLong()));
      } else if (type == SHORT) {
        h.put(key, new Short(is.readShort()));
      } else if (type == STRING) {
        h.put(key, readString(is));
      }
    }
    return h;
  }

  /**
   * Specializes the serialization method for protecting the message's
   * properties and body as soon as it is sent.
   */
  private void writeObject(ObjectOutputStream os) 
//   public void writeExternal(ObjectOutput os)
    throws IOException {
    os.writeInt(type);
    os.writeBoolean(persistent);
    os.writeBoolean(noBody);
    writeString(os, id);
    os.writeInt(priority);
    os.writeLong(expiration);
    os.writeLong(timestamp);
    writeString(os, toId);
    writeString(os, toType);
    writeString(os, correlationId);
    writeString(os, replyToId);
    writeString(os, replyToType);

    os.writeObject(optionalHeader);

    os.writeObject(getMessageBody());
    os.writeBoolean(bodyRO);

    writeProperties(os, properties);

    os.writeLong(order);
    os.writeInt(deliveryCount);

    os.writeBoolean(denied);
    os.writeBoolean(deletedDest);
    os.writeBoolean(expired);
    os.writeBoolean(notWriteable);
    os.writeBoolean(undeliverable);

    bodyRO = true;
  }

  /**
   * Specializes the deserialization method for initializing the message's
   * transient fields.
   */
  private void readObject(ObjectInputStream is)
//   public void readExternal(ObjectInput is)
    throws IOException, ClassNotFoundException {
    type = is.readInt();
    persistent = is.readBoolean();
    noBody = is.readBoolean();
    id = readString(is);
    priority = is.readInt();
    expiration = is.readLong();
    timestamp = is.readLong();
    toId = readString(is);
    toType = readString(is);
    correlationId = readString(is);
    replyToId = readString(is);
    replyToType = readString(is);

    optionalHeader = (Hashtable) is.readObject();

    body = (MessageBody) is.readObject();
    bodyRO = is.readBoolean();

    properties = readProperties(is);
    
    order = is.readLong();
    deliveryCount = is.readInt();

    denied = is.readBoolean();
    deletedDest= is.readBoolean();
    expired = is.readBoolean();
    notWriteable = is.readBoolean();
    undeliverable = is.readBoolean();

    acksCounter = 0;
    durableAcksCounter = 0;
    propertiesRO = true;
    pin = true;
  }

  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append('(');
    buff.append(super.toString());
    buff.append(",type=");
    buff.append(type);
    buff.append(",id=");
    buff.append(id);
    buff.append(",toId=");
    buff.append(toId);
    buff.append(",toType=");
    buff.append(toType);
    buff.append(",replyToId=");
    buff.append(replyToId);
    buff.append(",replyToType=");
    buff.append(replyToType);
    buff.append(",pin=");
    buff.append(pin);
    buff.append(",body=");
    buff.append(getMessageBody());
    buff.append(')');
    return buff.toString();
  }
}
