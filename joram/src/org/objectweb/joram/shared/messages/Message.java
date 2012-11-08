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
package org.objectweb.joram.shared.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.joram.shared.admin.AbstractAdminMessage;
import org.objectweb.joram.shared.stream.StreamUtil;
import org.objectweb.joram.shared.stream.Streamable;
import org.objectweb.joram.shared.util.Properties;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

/**
 * Implements the <code>Message</code> data structure.
 */
public final class Message implements Cloneable, Serializable, Streamable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 2L;

  /** logger */
  public static Logger logger = Debug.getLogger(Message.class.getName());

  /**
   * Constructs a bright new <code>Message</code>.
   */
  public Message() {}

  /**
   * Table holding header fields that may be required by particular
   * clients (such as JMS clients).
   */
  public transient Properties optionalHeader = null;

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
   * Sets an optional header field value.
   *
   * @param name  The header field name.
   * @param value  The corresponding value.
   */
  public void setOptionalHeader(String name, Object value) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid header name: " + name);

    if (value == null) return;

    if (optionalHeader == null)
      optionalHeader = new Properties();
    optionalHeader.put(name, value);
  }

  /** Body of the message. */
  public transient byte[] body = null;

  /** The message properties table. */
  public transient Properties properties = null;

  /**
   * Returns a property as an object.
   *
   * @param name  The property name.
   */
  public Object getProperty(String name) {
    if (properties == null) return null;
    return properties.get(name);
  }

  /**
   * Sets a property value.
   *
   * @param name  The property name.
   * @param value  The property value.
   */
  public void setProperty(String name, Object value) {
    if (properties == null)
      properties = new Properties();
    properties.put(name, value);
  }

  /** The message identifier. */
  public transient String id = null;
  
  /** <code>true</code> if the message must be persisted. */
  public transient boolean persistent = true;
 
  /** A simple message carries an empty body. */
  public static final int SIMPLE = 0;
  /** A text message carries a String body. */
  public static final int TEXT = 1;
  /** An object message carries a serializable object. */
  public static final int OBJECT = 2;
  /** A map message carries an hashtable. */
  public static final int MAP = 3;
  /** A stream message carries a bytes stream. */
  public static final int STREAM = 4;
  /** A bytes message carries an array of bytes. */
  public static final int BYTES = 5;
  /** A admin message carries a streamable object. */
  public static final int ADMIN = 6;

  /**
   * The client message type: SIMPLE, TEXT, OBJECT, MAP, STREAM, BYTES, ADMIN.
   * By default, the message type is SIMPLE.
   */
  public transient int type = SIMPLE;

  /**
   * The message priority from 0 to 9, 9 being the highest.
   * By default, the priority is 4?
   */
  public transient int priority = 4;
 
  /** The message expiration time, by default 0 for infinite time-to-live. */
  public transient long expiration = 0;

  /** The message time stamp. */
  public transient long timestamp;

  /**
   * <code>true</code> if the message has been denied at least once by a
   * consumer.
   */
  public transient boolean redelivered = false;

  /** The message destination identifier. */
  public transient String toId = null;
  /** The message destination type. */
  public transient String toType;

  /**
   * Sets the message destination.
   *
   * @param id  The destination identifier.
   * @param type The type of the destination.
   */
  public final void setDestination(String id, String type) {
    toId = id;
    toType = type;
  }

  /** Returns the message destination identifier. */
  public final String getDestinationId() {
    return toId;
  }

  /** Returns <code>true</code> if the destination is a queue. */
  public final String getDestinationType() {
    return toType;
  }

  /** The reply to destination identifier. */
  public transient String replyToId = null;
  /** <code>true</code> if the "reply to" destination is a queue. */
  public transient String replyToType;

  /** Returns the destination id the reply should be sent to. */
  public final String getReplyToId() {
    return replyToId;
  }

  /** Returns <code>true</code> if the reply to destination is a queue. */
  public final String replyToType() {
    return replyToType;
  }

  /**
   * Sets the destination to which a reply should be sent.
   *
   * @param id  The destination identifier.
   * @param type The destination type.
   */
  public final void setReplyTo(String id, String type) {
    replyToId = id;
    replyToType = type;
  }

  /** The correlation identifier field. */
  public transient String correlationId = null;
 
  /** The number of delivery attempts for this message. */
  public transient  int deliveryCount = 0;

  /**
   * Sets a String as the body of the message.
   */
  public void setText(String text) {
    if (text == null) {
      body = null;
    } else {
      body = text.getBytes();
    }
  }

  /**
   * Returns the text body of the message.
   */
  public String getText() {
    if (body == null) {
      return null;
    } else {
      return new String(body);
    }
  }

  /**
   * Sets an object as the body of the message. 
   *
   * @exception IOException  In case of an error while setting the object.
   */
  public void setObject(Serializable object) throws IOException {
    type = Message.OBJECT;

    if (object == null) {
      body = null;
    } else {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(object);
      oos.flush();
      body = baos.toByteArray();
      oos.close();
      baos.close();
    }
  }

  /**
   * Returns the object body of the message.
   *
   * @exception Exception  In case of an error while getting the object.
   */
  public Serializable getObject() throws Exception {
    // TODO (AF): May be, we should verify that it is an Object message!!
    if (body == null) return null;

    ByteArrayInputStream bais = null;
    ObjectInputStream ois = null;
    Object obj = null;
    
    try {
      try {
        bais = new ByteArrayInputStream(body);
        ois = new ObjectInputStream(bais);
        obj = ois.readObject();
      } catch (ClassNotFoundException cnfexc) {
        // Could not build serialized object: reason could be linked to 
        // class loaders hierarchy in an application server.
        class Specialized_OIS extends ObjectInputStream {
          Specialized_OIS(InputStream is) throws IOException {
            super(is);
          }

          protected Class resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException {
            String n = osc.getName();
            return Class.forName(n, false, Thread.currentThread().getContextClassLoader());
          }
        }

        bais = new ByteArrayInputStream(body);
        ois = new Specialized_OIS(bais);
        obj = ois.readObject(); 
      }
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "ERROR: getObject()", exc);
      // Don't forget to rethrow the Exception
      throw exc;
    } finally {
      try {
        ois.close();
      } catch (Exception e) {}
      try {
        bais.close();
      } catch (Exception e) {}
    }

    return (Serializable) obj;
  }

  /**
   * Sets an AbstractAdminMessage as the body of the message. 
   *
   * @exception IOException  In case of an error while setting the object.
   */
  public void setAdminMessage(AbstractAdminMessage adminMsg) throws IOException {
    type = Message.ADMIN;

    if (adminMsg == null) {
      body = null;
    } else {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      AbstractAdminMessage.write(adminMsg, baos);
      baos.flush();
      body = baos.toByteArray();
      baos.close();
    }
  }

  /**
   * Returns the AbstractAdminMessage body of the message.
   *
   * @exception IOException  In case of an error while getting the object.
   * @exception ClassNotFoundException  If the object class is unknown.
   */
  public AbstractAdminMessage getAdminMessage() {
    if (body == null) return null;

    ByteArrayInputStream bais = null;
    AbstractAdminMessage adminMsg = null;

    try {
     bais = new ByteArrayInputStream(body);
     adminMsg = AbstractAdminMessage.read(bais);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "ERROR: getAdminMessage()", e);
    }
    return adminMsg;
  }

  public final String toString() {
    StringBuffer strbuf = new StringBuffer();
    toString(strbuf);
    return strbuf.toString();
  }

  public void toString(StringBuffer strbuf) {
    strbuf.append('(').append(super.toString());
    strbuf.append(",id=").append(id);
    strbuf.append(",type=").append(type);
    strbuf.append(",persistent=").append(persistent);
    strbuf.append(",priority=").append(priority);
    strbuf.append(",expiration=").append(expiration);
    strbuf.append(",timestamp=").append(timestamp);
    strbuf.append(",toId=").append(toId);
    strbuf.append(",replyToId=").append(replyToId);
    strbuf.append(",correlationId=").append(correlationId);
    strbuf.append(')');
  }

  /** Clones the message. */
  public Object clone() {
    try {
      Message clone = (Message) super.clone();
      if (body != null) {
        // AF: May be we can share the body as it should be RO.
        clone.body = new byte[body.length];
        System.arraycopy(body, 0, clone.body, 0, body.length);
      }
      if (optionalHeader != null) {
        clone.optionalHeader = (Properties) optionalHeader.clone();
      }
      if (properties != null) {
        clone.properties = (Properties) properties.clone();
      }
      return clone;
    } catch (CloneNotSupportedException cE) {
      // Should never happened!
      return null;
    }
  }

  public Hashtable soapCode() {
    Hashtable h = new Hashtable();
    // AF: TODO
    return h;
  }

  public static Message soapDecode(Hashtable h) {
    // AF: TODO
    return null;
  }

//   public static int bodyROFlag = 	0x00000001;
//   public static int propertiesROFlag =  0x00000002;
  public static int redeliveredFlag =   0x00000004;
  public static int persistentFlag =    0x00000008;

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputStream os) throws IOException {
    writeHeaderTo(os);
    StreamUtil.writeTo(body, os);
  }

  public void writeHeaderTo(OutputStream os) throws IOException {
    int bool = 0;

    StreamUtil.writeTo(type, os);
    StreamUtil.writeTo(optionalHeader, os);
//     bool = bool | (bodyRO?bodyROFlag:0);
//     bool = bool | (propertiesRO?propertiesROFlag:0);
    StreamUtil.writeTo(properties, os);
    StreamUtil.writeTo(id, os);
    StreamUtil.writeTo(priority, os);
    StreamUtil.writeTo(toId, os);
    StreamUtil.writeTo(toType, os);
    StreamUtil.writeTo(expiration, os);
    StreamUtil.writeTo(replyToId, os);
    StreamUtil.writeTo(replyToType, os);
    StreamUtil.writeTo(timestamp, os);
    StreamUtil.writeTo(correlationId, os);
    StreamUtil.writeTo(deliveryCount, os);

    bool = bool | (redelivered?redeliveredFlag:0);
    bool = bool | (persistent?persistentFlag:0);
    StreamUtil.writeTo(bool, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    readHeaderFrom(is);
    body = StreamUtil.readByteArrayFrom(is);
  }

  public void readHeaderFrom(InputStream is) throws IOException {
    type = StreamUtil.readIntFrom(is);
    optionalHeader = StreamUtil.readPropertiesFrom(is);
    properties = StreamUtil.readPropertiesFrom(is);
    id = StreamUtil.readStringFrom(is);
    priority = StreamUtil.readIntFrom(is);
    toId = StreamUtil.readStringFrom(is);
    toType = StreamUtil.readStringFrom(is);
    expiration = StreamUtil.readLongFrom(is);
    replyToId = StreamUtil.readStringFrom(is);
    replyToType = StreamUtil.readStringFrom(is);
    timestamp = StreamUtil.readLongFrom(is);
    correlationId = StreamUtil.readStringFrom(is);
    deliveryCount = StreamUtil.readIntFrom(is);

    int bool = StreamUtil.readIntFrom(is);
//     bodyRO = ((bool & bodyROFlag) != 0);
//     propertiesRO = ((bool & propertiesROFlag) != 0);
    redelivered = ((bool & redeliveredFlag) != 0);
    persistent = ((bool & persistentFlag) != 0);
  }

  /**
   *  this method allows to write to the output stream a vector of message.
   *
   * @param messages 	the vector of messages
   * @param os 		the stream to write the vector to
   */
  public static void writeVectorTo(Vector messages,
                                   OutputStream os) throws IOException {
    if (messages == null) {
      StreamUtil.writeTo(-1, os);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "writeVectorTo: -1");
    } else {
      int size = messages.size();
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "writeVectorTo: " + size);
      StreamUtil.writeTo(size, os);
      for (int i=0; i<size; i++) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "writeVectorTo: msg#" + i);
        ((Message) messages.elementAt(i)).writeTo(os);
      }
    }
  }

  /**
   *  this method allows to read from the input stream a vector of messages.
   *
   * @param is 	the stream to read data from in order to restore the vector
   * @return	the vector of messages
   */
  public static Vector readVectorFrom(InputStream is) throws IOException {
    int size = StreamUtil.readIntFrom(is);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "readVectorFrom: " + size);
    if (size == -1) {
      return null;
    } else {
      Vector messages = new Vector(size);
      for (int i=0; i<size; i++) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "readVectorFrom: msg#" + i);
        Message msg = new Message();
        msg.readFrom(is);
        messages.addElement(msg);
      }
      return messages;
    }
  }

//   /** ***** ***** ***** ***** ***** ***** ***** *****
//    * Externalizable interface
//    * ***** ***** ***** ***** ***** ***** ***** ***** */
  
//   public void writeExternal(ObjectOutput out) throws IOException {
//     writeTo(out);
//   }

//   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//     readFrom(in);
//   }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Serializable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  private void writeObject(ObjectOutputStream out) throws IOException {
    writeTo(out);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    readFrom(in);
  }
}
