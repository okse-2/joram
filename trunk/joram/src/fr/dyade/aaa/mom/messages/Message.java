/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.messages;

import fr.dyade.aaa.mom.excepts.*;

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
public class Message implements Cloneable, Serializable
{
  /** The message type (SIMPLE, TEXT, OBJECT, MAP, STREAM, BYTES). */
  int type;

  /** The message identifier. */
  String id = null;
  /** The message priority (from 0 to 9, 9 being the highest). */
  int priority = 4;
  /** The message expiration time (0 for infinite time-to-live). */
  long expiration = 0;
  /** The message time stamp. */
  long timestamp;
  /** The message destination identifier. */
  String toId = null;
  /** <code>true</code> if the message destination is a queue. */
  boolean toQueue;
  /** The correlation identifier field. */
  String correlationId = null;
  /** The reply to destination identifier. */
  String replyToId = null;
  /** <code>true</code> if the "reply to" destination is a queue. */
  boolean replyToQueue;

  /**
   * Table holding header fields that may be required by particular
   * clients (such as JMS clients).
   */
  Hashtable optionalHeader = null;

  /** The bytes body. */
  byte[] body_bytes = null;
  /** The map body. */
  Hashtable body_map = null;
  /** The text body. */
  String body_text = null;
  /** <code>true</code> if the body is read-only. */
  boolean bodyRO = false;

  /**
   * The message properties table.
   * <p>
   * <b>Key:</b> property name<br>
   * <b>Object:</b> property (native objects)
   */
  Hashtable properties = null;
  /** <code>true</code> if the properties are read-only. */
  boolean propertiesRO = false;

  /** The number of delivery attempts for this message. */
  public int deliveryCount = 0;
  /**
   * <code>true</code> if the message has been denied at least once by a
   * consumer.
   */
  public boolean denied = false;
  /**
   * The identifier of the consumer, used by queues for managing
   * acknowledgemebnts.
   */
  public String consId = null;
  /**
   * The number of acknowledgements a message still expects from its 
   * subscribers before having been fully consumed (field used by JMS proxies).
   */
  public int acksCounter = 0;

  /** <code>true</code> if the message target destination is deleted. */
  public boolean deletedDest = false;
  /** <code>true</code> if the message expired. */
  public boolean expired = false;
  /** <code>true</code> if the message could not be written on the dest. */
  public boolean notWritable = false;
  /** <code>true</code> if the message is considered as undeliverable. */
  public boolean undeliverable = false;
  

  /**
   * Constructs a <code>Message</code> instance.
   */
  public Message()
  {
    this.type = MessageType.SIMPLE;
  }

  
  /** Sets the message identifier. */ 
  public void setIdentifier(String id)
  {
    this.id = id;
  }

  /**
   * Sets the message priority.
   *
   * @param priority  Priority value: 0 the lowest, 9 the highest, 4 normal.
   */ 
  public void setPriority(int priority)
  {
    if (priority >= 0 && priority <= 9)
      this.priority = priority;
  }

  /** Sets the message expiration. */
  public void setExpiration(long expiration)
  {
    if (expiration >= 0)
      this.expiration = expiration;
  }

  /** Sets the message time stamp. */
  public void setTimestamp(long timestamp)
  {
    this.timestamp = timestamp;
  }

  /**
   * Sets the message destination.
   *
   * @param id  The destination identifier.
   * @param queue  <code>true</code> if the destination is a queue.
   */
  public void setDestination(String id, boolean queue)
  {
    this.toId = id;
    this.toQueue = queue;
  }

  /** Sets the message correlation identifier. */
  public void setCorrelationId(String correlationId)
  {
    this.correlationId = correlationId;
  }

  /**
   * Sets the destination to which a reply should be sent.
   *
   * @param id  The destination identifier.
   * @param queue  <code>true</code> if the destination is a queue.
   */
  public void setReplyTo(String id, boolean queue)
  {
    this.replyToId = id;
    this.replyToQueue = queue;
  }

  /**
   * Sets an optional header field value.
   *
   * @param name  The header field name.
   * @param value  The corresponding value.
   */
  public void setOptionalHeader(String name, Object value)
  {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid header name: " + name);

    if (value == null)
      return;

    if (optionalHeader == null)
      optionalHeader = new Hashtable();

    optionalHeader.put(name, value);
  }

  /** Returns the message type. */
  public int getType()
  {
    return type;
  }

  /** Returns the message identifier. */
  public String getIdentifier()
  {
    return id;
  }

  /** Returns the message priority. */
  public int getPriority()
  {
    return priority;
  }

  /** Returns the message expiration time. */
  public long getExpiration()
  {
    return expiration;
  }
  
  /** Returns the message time stamp. */
  public long getTimestamp()
  {
    return timestamp;
  }

  /** Returns the message destination identifier. */
  public String getDestinationId()
  {
    return toId;
  }

  /** Returns <code>true</code> if the destination is a queue. */
  public boolean toQueue()
  {
    return toQueue;
  }

  /** Returns the message correlation identifier. */
  public String getCorrelationId()
  {
    return correlationId;
  }

  /** Returns the destination id the reply should be sent to. */
  public String getReplyToId()
  {
    return replyToId;
  }

  /** Returns <code>true</code> if the reply to destination is a queue. */
  public boolean replyToQueue()
  {
    return replyToQueue;
  }

  /**
   * Returns an optional header field value.
   *
   * @param name  The header field name.
   */
  public Object getOptionalHeader(String name)
  {
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
  public void setBooleanProperty(String name, boolean value)
         throws MessageROException
  {
    preparePropSetting(name);
    properties.put(name, new Boolean(value));
  }

  /**
   * Sets a property as a byte value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setByteProperty(String name, byte value)
         throws MessageROException
  {
    preparePropSetting(name);
    properties.put(name, new Byte(value));
  }

  /**
   * Sets a property as a double value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setDoubleProperty(String name, double value)
         throws MessageROException
  {
    preparePropSetting(name);
    properties.put(name, new Double(value));
  }

  /**
   * Sets a property as a float value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setFloatProperty(String name, float value)
         throws MessageROException
  {
    preparePropSetting(name);
    properties.put(name, new Float(value));
  }

  /**
   * Sets a property as an int value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setIntProperty(String name, int value) throws MessageROException
  {
    preparePropSetting(name);
    properties.put(name, new Integer(value));
  }

  /**
   * Sets a property as a long value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setLongProperty(String name, long value)
         throws MessageROException
  {
    preparePropSetting(name);
    properties.put(name, new Long(value));
  }

  /**
   * Sets a property value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   * @exception MessageValueException  If the value is not a Java primitive
   *              object.
   */
  public void setObjectProperty(String name, Object value)
         throws MessageException
  {
    preparePropSetting(name);

    if (value instanceof Boolean || value instanceof Number
        || value instanceof String)
      properties.put(name, value);

    else
      throw new MessageValueException("Can't set non primitive Java object"
                                      + " as a property value.");
  }

  /**
   * Sets a property as a short value.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setShortProperty(String name, short value)
         throws MessageROException
  {
    preparePropSetting(name);
    properties.put(name, new Short(value));
  }

  /**
   * Sets a property as a String.
   *
   * @param name  The property name.
   * @param value  The property value.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  public void setStringProperty(String name, String value)
         throws MessageROException
  {
    preparePropSetting(name);
    properties.put(name, new String(value));
  }

  /**
   * Returns a property as a boolean value.
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public boolean getBooleanProperty(String name) throws MessageValueException 
  {
    if (properties == null)
      return Boolean.valueOf(null).booleanValue();
    return ConversionHelper.toBoolean(properties.get(name));
  }
  
  /**
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public byte getByteProperty(String name) throws MessageValueException 
  {
    if (properties == null)
      return Byte.valueOf(null).byteValue();
    return ConversionHelper.toByte(properties.get(name));
  }

  /**
   * Returns a property as a double value.
   *
   * @param name  The property name.
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public double getDoubleProperty(String name) throws MessageValueException
  {
    if (properties == null)
      return Double.valueOf(null).doubleValue();
    return ConversionHelper.toDouble(properties.get(name));
  }

  /**
   * Returns a property as a float value.
   *
   * @param name  The property name.
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public float getFloatProperty(String name) throws MessageValueException
  {
    if (properties == null)
      return Float.valueOf(null).floatValue();
    return ConversionHelper.toFloat(properties.get(name));
  }

  /**
   * Returns a property as a int value.
   *
   * @param name  The property name.
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public int getIntProperty(String name) throws MessageValueException
  {
    if (properties == null)
      return Integer.valueOf(null).intValue();
    return ConversionHelper.toInt(properties.get(name));
  }

  /**
   * Returns a property as a long value.
   *
   * @param name  The property name.
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public long getLongProperty(String name) throws MessageValueException
  {
    if (properties == null)
      return Long.valueOf(null).longValue();
    return ConversionHelper.toLong(properties.get(name));
  }

  /**
   * Returns a property as an object.
   *
   * @param name  The property name.
   */
  public Object getObjectProperty(String name)
  {
    if (properties == null)
      return null;
    return properties.get(name);
  }

  /**
   * Returns a property as a short value.
   *
   * @param name  The property name.
   *
   * @exception MessageValueException  If the property type is invalid.
   */
  public short getShortProperty(String name) throws MessageValueException
  {
    if (properties == null)
      return Short.valueOf(null).shortValue();
    return ConversionHelper.toShort(properties.get(name));
  }

  /**
   * Returns a property as a String.
   *
   * @param name  The property name.
   */
  public String getStringProperty(String name)
  {
    if (properties == null)
      return null;
    return ConversionHelper.toString(properties.get(name));
  }
  
  /**
   * Returns <code>true</code> if a given property exists.
   *
   * @param name  The name of the property to check.
   */
  public boolean propertyExists(String name)
  {
    if (properties == null)
      return false;

    return properties.containsKey(name);
  }

  /** Returns an enumeration of the properties names. */
  public Enumeration getPropertyNames()
  {
    if (properties == null)
      return (new Hashtable()).keys();

    return properties.keys();
  }

  /** Empties the properties table. */
  public void clearProperties()
  {
    propertiesRO = false;

    if (properties == null)
      return;

    properties.clear();
    properties = null;
  }

  
  /**
   * Sets an object as the body of the message. 
   *
   * @exception IOException  In case of an error while setting the object.
   * @exception  MessageROException  If the message body is read-only.
   */
  public void setObject(Object object) throws IOException, MessageROException
  {
    if (bodyRO)
      throw new MessageROException("Can't set the body as it is READ-ONLY.");

    if (object == null)
      body_bytes = null;
    else {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(object);
      oos.flush();
      body_bytes = baos.toByteArray();
      oos.close();
      baos.close();
    }
    type = MessageType.OBJECT;
  }

  /**
   * Sets a map as the body of the message.
   *
   * @exception IOException  In case of an error while setting the map.
   * @exception MessageROException  If the message body is read-only.
   */
  public void setMap(Hashtable map) throws Exception
  {
    if (bodyRO)
      throw new MessageROException("Can't set the body as it is READ-ONLY.");

    body_map = map;
    type = MessageType.MAP;
  }

  /**
   * Sets a String as the body of the message.
   *
   * @exception MessageROException  If the message body is read-only.
   */
  public void setText(String text) throws MessageROException
  {
    if (bodyRO)
      throw new MessageROException("Can't set the body as it is READ-ONLY.");

    body_text = text;
    type = MessageType.TEXT;
  }

  /**
   * Sets the message body as a stream of bytes. 
   *
   * @exception MessageROException  If the message body is read-only.
   */
  public void setStream(byte[] bytes) throws MessageROException
  {
    if (bodyRO)
      throw new MessageROException("Can't set the body as it is READ-ONLY.");

    body_bytes = bytes;
    type = MessageType.STREAM;
  }

  /**
   * Sets the message body as an array of bytes. 
   *
   * @exception MessageROException  If the message body is read-only.
   */
  public void setBytes(byte[] bytes) throws MessageROException
  {
    if (bodyRO)
      throw new MessageROException("Can't set the body as it is READ-ONLY.");

    body_bytes = bytes;
    type = MessageType.BYTES;
  } 

  /**
   * Returns the object body of the message.
   *
   * @exception IOException  In case of an error while getting the object.
   * @exception ClassNotFoundException  If the object class is unknown.
   */
  public Object getObject() throws Exception
  {
    if (body_bytes == null || type != MessageType.OBJECT)
      return null;

    ByteArrayInputStream bais = new ByteArrayInputStream(body_bytes);
    ObjectInputStream ois = new ObjectInputStream(bais);
    return ois.readObject();
  }

  /**
   * Returns the map body of the message.
   */ 
  public Hashtable getMap()
  {
    return body_map;
  }

  /** Gets the String body of the message. */
  public String getText()
  {
    return body_text;
  }

  /** Returns the stream of bytes body of the message. */
  public byte[] getStream()
  {
    if (type != MessageType.STREAM)
      return null;

    return body_bytes;
  }

  /** Returns the array of bytes body of the message. */
  public byte[] getBytes()
  {
    if (type != MessageType.BYTES)
      return null;

    return body_bytes;
  }

  /** 
   * Method clearing the message body.
   */
  public void clearBody()
  {
    body_bytes = null;
    body_map = null;
    body_text = null;
    bodyRO = false;
  }

  /** Returns <code>true</code> if the message is valid. */
  public boolean isValid()
  {
    if (expiration == 0)
      return true;

    return ((expiration - System.currentTimeMillis()) > 0);
  }

  /** Clones this object. */
  public Object clone()
  {
    try {
      Message clone = (Message) super.clone();
      if (body_map != null) {
        clone.body_map = new Hashtable();
        clone.body_map.putAll(body_map);
      }
      if (optionalHeader != null) {
        clone.optionalHeader = new Hashtable();
        clone.optionalHeader.putAll(optionalHeader);
      }
      if (properties != null) {
        clone.properties = new Hashtable();
        clone.properties.putAll(properties);
      }
      return clone;
    }
    catch (CloneNotSupportedException cE) {
      return null;
    }
  }

  /**
   * Method actually preparing the setting of a new property.
   *
   * @param name  The property name.
   *
   * @exception MessageROException  If the message properties are read-only.
   */
  private void preparePropSetting(String name) throws MessageROException
  {
    if (propertiesRO) {
      throw new MessageROException("Can't set property as the message "
                                   + "properties are READ-ONLY.");
    }

    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    if (properties == null)
      properties = new Hashtable();
  }

  /**
   * Specializes the serialization method for protecting the message's
   * properties and body as soon as it is sent.
   */
  private void writeObject(ObjectOutputStream s) throws IOException
  {
    s.defaultWriteObject();
    bodyRO = true;
    propertiesRO = true;
  }


  /**
   * Transforms this message into a vector of primitive values that can
   * be vehiculated through the SOAP protocol.
   */
  public Vector soapCode()
  {
    Vector vec = new Vector();
   
    // Building a hashtable containg the fields values: 
    Hashtable fieldsTb = new Hashtable();

    fieldsTb.put("type", new Integer(type));
    fieldsTb.put("id", id);
    fieldsTb.put("priority", new Integer(priority));
    fieldsTb.put("expiration", new Long(expiration));
    fieldsTb.put("timestamp", new Long(timestamp));
    fieldsTb.put("toId", toId);
    fieldsTb.put("toQueue", new Boolean(toQueue));
    if (correlationId != null)
      fieldsTb.put("correlationId", correlationId);
    if (replyToId != null) {
      fieldsTb.put("replyToId", replyToId);
      fieldsTb.put("replyToQueue", new Boolean(replyToQueue));
    }
    if (body_bytes != null)
      fieldsTb.put("body_bytes", body_bytes);
    else if (body_map != null)
      fieldsTb.put("body_map", body_map);
    else if (body_text != null)
      fieldsTb.put("body_text", body_text);
    fieldsTb.put("bodyRO", new Boolean(bodyRO));
    fieldsTb.put("propertiesRO", new Boolean(propertiesRO));
    fieldsTb.put("deliveryCount", new Integer(deliveryCount));
    fieldsTb.put("denied", new Boolean(denied));
    if (consId != null)
      fieldsTb.put("consId", consId);
    fieldsTb.put("acksCounter", new Integer(acksCounter));
    fieldsTb.put("deletedDest", new Boolean(deletedDest));
    fieldsTb.put("expired", new Boolean(expired));
    fieldsTb.put("notWritable", new Boolean(notWritable));
    fieldsTb.put("undeliverable", new Boolean(undeliverable));

    vec.add(fieldsTb);

    // Adding the hashtable of optional headers:
    vec.add(optionalHeader);

    // Adding the hashtable of properties:
    vec.add(properties);

    return vec;
  }

  /** 
   * Transforms a vector of primitive values into a <code>Message</code>
   * instance.
   */
  public static Message soapDecode(Vector vec) 
  {
    Hashtable fieldsTb = (Hashtable) vec.remove(0);
    Hashtable optTb = (Hashtable) vec.remove(0);
    Hashtable propsTb = (Hashtable) vec.remove(0);

    Message msg = new Message();

    try {
      msg.type = ConversionHelper.toInt(fieldsTb.get("type"));
      msg.id = (String) fieldsTb.get("id");
      msg.priority = ConversionHelper.toInt(fieldsTb.get("priority"));
      msg.expiration = ConversionHelper.toLong(fieldsTb.get("expiration"));
      msg.timestamp = ConversionHelper.toLong(fieldsTb.get("timestamp"));
      msg.toId = (String) fieldsTb.get("toId");
      msg.toQueue = ConversionHelper.toBoolean(fieldsTb.get("toQueue"));
      msg.correlationId = (String) fieldsTb.get("correlationId");
      msg.replyToId = (String) fieldsTb.get("replyToId");
      if (msg.replyToId != null) {
        msg.replyToQueue =
          ConversionHelper.toBoolean(fieldsTb.get("replyToQueue"));
      }
      msg.body_bytes = ConversionHelper.toBytes(fieldsTb.get("body_bytes"));
      msg.body_map = (Hashtable) fieldsTb.get("body_map");
      msg.body_text = (String) fieldsTb.get("body_text");
      msg.bodyRO = ConversionHelper.toBoolean(fieldsTb.get("bodyRO"));
      msg.propertiesRO =
        ConversionHelper.toBoolean(fieldsTb.get("propertiesRO"));
      msg.deliveryCount = ConversionHelper.toInt(fieldsTb.get("deliveryCount"));
      msg.denied = ConversionHelper.toBoolean(fieldsTb.get("denied"));
      msg.consId = (String) fieldsTb.get("consId");
      msg.acksCounter = ConversionHelper.toInt(fieldsTb.get("acksCounter"));
      msg.deletedDest = ConversionHelper.toBoolean(fieldsTb.get("deletedDest"));
      msg.expired = ConversionHelper.toBoolean(fieldsTb.get("expired"));
      msg.notWritable = ConversionHelper.toBoolean(fieldsTb.get("notWritable"));
      msg.undeliverable =
        ConversionHelper.toBoolean(fieldsTb.get("undeliverable"));

      msg.optionalHeader = optTb;
      msg.properties = propsTb;
    }
    // Should never happen!
    catch (MessageValueException exc) {}
  
    return msg;
  }
}
