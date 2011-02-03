/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
  /** <code>true</code> if the message destination is a queue. */
  transient boolean toQueue;
  /** The correlation identifier field. */
  transient String correlationId = null;
  /** The reply to destination identifier. */
  transient String replyToId = null;
  /** <code>true</code> if the "reply to" destination is a queue. */
  transient boolean replyToQueue;

  /**
   * Table holding header fields that may be required by particular
   * clients (such as JMS clients).
   */
  transient Hashtable optionalHeader = null;

  /** The bytes body. */
  transient byte[] body_bytes = null;
  /** The map body. */
  transient HashMap body_map = null;
  /** The text body. */
  transient String body_text = null;
  /** <code>true</code> if the body is read-only. */
  transient boolean bodyRO = false;

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
  public Message()
  {
    this.type = MessageType.SIMPLE;
  }

  
  /** Sets the message identifier. */ 
  public void setIdentifier(String id)
  {
    this.id = id;
  }

  /** Sets the message persistence mode. */
  public void setPersistent(boolean persistent)
  {
    this.persistent = persistent;
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

  /** Returns <code>true</code> if the message is persistent. */
  public boolean getPersistent()
  {
    return persistent;
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
  public void setMap(HashMap map) throws Exception
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
    if (body_bytes == null)
      return null;
    ByteArrayInputStream bais = new ByteArrayInputStream(body_bytes);
    ObjectInputStream ois = new ObjectInputStream(bais);
    return ois.readObject();
  }

  /**
   * Returns the map body of the message.
   */ 
  public Map getMap()
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
    return body_bytes;
  }

  /** Returns the array of bytes body of the message. */
  public byte[] getBytes()
  {
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

  /** Clones the message. */
  public Object clone()
  {
    try {
      Message clone = (Message) super.clone();
      if (body_map != null) {
        clone.body_map = new HashMap();
        if (body_map.keySet() != null) {
          for (Iterator it = body_map.keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();
            clone.body_map.put(key,body_map.get(key));
          }
        }
      }
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
      return clone;
    }
    catch (CloneNotSupportedException cE) {
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
  public static Message soapDecode(Hashtable h) 
  {
    if (h == null) return null;

    Hashtable fieldsTb = (Hashtable) h.get("fieldsTb");

    Message msg = new Message();

    try {
      msg.type = ConversionHelper.toInt(fieldsTb.get("type"));
      msg.id = (String) fieldsTb.get("id");
      msg.persistent = ConversionHelper.toBoolean(fieldsTb.get("persistent"));
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
      msg.body_map = (HashMap) fieldsTb.get("body_map");
      msg.body_text = (String) fieldsTb.get("body_text");
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
    }
    // Should never happen!
    catch (MessageValueException exc) {}
  
    return msg;
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

  private static void writeString(ObjectOutputStream os,
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

  private static String readString(ObjectInputStream is) throws IOException {
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

  /**
   * Specializes the serialization method for protecting the message's
   * properties and body as soon as it is sent.
   */
  private void writeObject(ObjectOutputStream os) throws IOException
  {
//     os.defaultWriteObject();

    os.writeInt(type);
    os.writeBoolean(persistent);
    writeString(os, id);
    os.writeInt(priority);
    os.writeLong(expiration);
    os.writeLong(timestamp);
    writeString(os, toId);
    os.writeBoolean(toQueue);
    writeString(os, correlationId);
    writeString(os, replyToId);
    os.writeBoolean(replyToQueue);

    os.writeObject(optionalHeader);

    if (type == MessageType.SIMPLE) {
    } else if (type == MessageType.TEXT) {
      writeString(os, body_text);
    } else if ((type == MessageType.OBJECT) ||
               (type == MessageType.STREAM) ||
               (type == MessageType.BYTES)) {
      if (body_bytes == null) {
        os.writeInt(-1);
      } else {
        os.writeInt(body_bytes.length);
        os.write(body_bytes);
      }
    } else if (type == MessageType.MAP) {
      os.writeObject(body_map);
    }
    os.writeBoolean(bodyRO);

    os.writeObject(properties);
    
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
               throws IOException, ClassNotFoundException
  {
//     is.defaultReadObject();

    type = is.readInt();
    persistent = is.readBoolean();
    id = readString(is);
    priority = is.readInt();
    expiration = is.readLong();
    timestamp = is.readLong();
    toId = readString(is);
    toQueue = is.readBoolean();
    correlationId = readString(is);
    replyToId = readString(is);
    replyToQueue = is.readBoolean();

    optionalHeader = (Hashtable) is.readObject();

    clearBody();
    if (type == MessageType.SIMPLE) {
    } else if (type == MessageType.TEXT) {
      body_text = readString(is);
    } else if ((type == MessageType.OBJECT) ||
               (type == MessageType.STREAM) ||
               (type == MessageType.BYTES)) {
      int length = is.readInt();
      if (length ==  -1) {
        body_bytes = null;
      } else {
        body_bytes = new byte[length];
        is.readFully(body_bytes);
      }
    } else if (type == MessageType.MAP) {
      body_map = (HashMap) is.readObject();
    }
    bodyRO = is.readBoolean();

    properties = (Hashtable) is.readObject();
    
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
  }

  public String toString() {
    return '(' + super.toString() +
      ",id=" + id + 
      ",body_bytes=" + body_bytes + ')';
  }
}