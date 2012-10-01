/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2011 ScalAgent Distributed Technologies
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
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.stream.Properties;
import fr.dyade.aaa.common.stream.StreamUtil;
import fr.dyade.aaa.common.stream.Streamable;

/**
 * Implements the <code>Message</code> data structure.
 */
public final class Message implements Cloneable, Serializable, Streamable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 3L;

  // default value from javax.jms.Message (jms 1.1)
  public static final int NON_PERSISTENT = 1;
  public static final int PERSISTENT = 2;
  public static final int DEFAULT_DELIVERY_MODE = PERSISTENT;
  public static final int DEFAULT_PRIORITY = 4;
  public static final long DEFAULT_TIME_TO_LIVE = 0;
  
  /** logger */
  public static Logger logger = Debug.getLogger(Message.class.getName());

  /**
   * Constructs a bright new <code>Message</code>.
   */
  public Message() {}

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
   * If the value is not a Java primitive object its string representation is used.
   *
   * @param name    The property name.
   * @param value  The property value.
   *
   * @exception IllegalArgumentException  If the key name is illegal (null or empty string).
   */
  public void setProperty(String name, Object value) {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);
    
    if (properties == null)
      properties = new Properties();
    
    if (value instanceof Boolean || value instanceof Number || value instanceof String) {
      properties.put(name, value);
    } else {
      properties.put(name, value.toString());
    }
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
   * The JMSType header field contains a message type identifier supplied by a
   * client when a message is sent.
   */
  public transient String jmsType = null;

  /**
   * The message priority from 0 to 9, 9 being the highest.
   * By default, the priority is 4?
   */
  public transient int priority = DEFAULT_PRIORITY;
 
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
  public transient byte toType;

  /**
   * Sets the message destination.
   *
   * @param id  The destination identifier.
   * @param type The type of the destination.
   */
  public final void setDestination(String id, byte type) {
    toId = id;
    toType = type;
  }

//  /** Returns the message destination identifier. */
//  public final String getDestinationId() {
//    return toId;
//  }
//
//  /** Returns <code>true</code> if the destination is a queue. */
//  public final String getDestinationType() {
//    return toType;
//  }

  /** The reply to destination identifier. */
  public transient String replyToId = null;
  /** <code>true</code> if the "reply to" destination is a queue. */
  public transient byte replyToType;

//  /** Returns the destination id the reply should be sent to. */
//  public final String getReplyToId() {
//    return replyToId;
//  }
//
//  /** Returns <code>true</code> if the reply to destination is a queue. */
//  public final String replyToType() {
//    return replyToType;
//  }

  /**
   * Sets the destination to which a reply should be sent.
   *
   * @param id  The destination identifier.
   * @param type The destination type.
   */
  public final void setReplyTo(String id, byte type) {
    replyToId = id;
    replyToType = type;
  }

  /** The correlation identifier field. */
  public transient String correlationId = null;
 
  /** The number of delivery attempts for this message. */
  public transient  int deliveryCount = 0;

  /**
   * convert serializable object to byte[]
   * 
   * @param object the serializable object
   * @return the byte array
   * @throws IOException In case of error 
   */
  private byte[] toBytes(Serializable object) throws IOException {
  	if (object == null)
  		return null;
  	ByteArrayOutputStream baos = null;
  	ObjectOutputStream oos = null;
  	try {
  		baos = new ByteArrayOutputStream();
  		oos = new ObjectOutputStream(baos);
  		oos.writeObject(object);
  		oos.flush();
  		return baos.toByteArray();
  	} finally {
  		if (oos != null)
  			oos.close();
  		if (baos != null)
  			baos.close();
  	}
  }
  
  /**
   * convert byte[] to serializable object 
   * 
   * @param body the byte array
   * @return the serializable object
   * @throws Exception In case of error
   */
  private Serializable fromBytes(byte[] body) throws Exception {
  	if (body == null) 
  		return null;

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
  			logger.log(BasicLevel.ERROR, "ERROR: fromBytes(body)", exc);
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
   * Sets a String as the body of the message.
   * @throws IOException In case of an error while setting the text
   */
  public void setText(String text) throws IOException {
  	body = toBytes(text);
  }

  /**
   * Returns the text body of the message.
   * @throws Exception In case of an error while getting the text
   */
  public String getText() throws Exception {
    if (body == null) {
      return null;
    }
    return (String) fromBytes(body);
  }

  /**
   * Sets an object as the body of the message. 
   *
   * @exception IOException  In case of an error while setting the object.
   */
  public void setObject(Serializable object) throws IOException {
    type = Message.OBJECT;
    body = toBytes(object);
  }

  /**
   * Returns the object body of the message.
   *
   * @exception Exception  In case of an error while getting the object.
   */
  public Serializable getObject() throws Exception {
    // TODO (AF): May be, we should verify that it is an Object message!!
    return fromBytes(body);
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

  private static final short typeFlag = 0x0001;
  private static final short replyToIdFlag = 0x0002;
  private static final short replyToTypeFlag = 0x0004;
  private static final short propertiesFlag = 0x0008;
  private static final short priorityFlag = 0x0010;
  private static final short expirationFlag = 0x0020;
  private static final short corrrelationIdFlag = 0x0040;
  private static final short deliveryCountFlag = 0x0080;
  private static final short jmsTypeFlag = 0x0100;
  private static final short redeliveredFlag = 0x0200;
  private static final short persistentFlag = 0x0400;

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
    StreamUtil.writeTo(id, os);
    StreamUtil.writeTo(toId, os);
    StreamUtil.writeTo(toType, os);
    StreamUtil.writeTo(timestamp, os);

    // One short is used to know which fields are set
    short s = 0;
    if (type != SIMPLE) { s |= typeFlag; }
    if (replyToId != null) { s |= replyToIdFlag; }
    if (replyToType != 0) { s |= replyToTypeFlag; }
    if (properties != null) { s |= propertiesFlag; }
    if (priority != DEFAULT_PRIORITY) { s |= priorityFlag; }
    if (expiration != 0) { s |= expirationFlag; }
    if (correlationId != null) { s |= corrrelationIdFlag; }
    if (deliveryCount != 0) { s |= deliveryCountFlag; }
    if (jmsType != null) { s |= jmsTypeFlag; }
    if (redelivered) { s |= redeliveredFlag; }
    if (persistent) { s |= persistentFlag; }
    
    StreamUtil.writeTo(s, os);
    
    if (type != SIMPLE) { StreamUtil.writeTo(type, os); }
    if (replyToId != null) { StreamUtil.writeTo(replyToId, os); }
    if (replyToType != 0) { StreamUtil.writeTo(replyToType, os); }
    if (properties != null) { StreamUtil.writeTo(properties, os); }
    if (priority != DEFAULT_PRIORITY) { StreamUtil.writeTo(priority, os); }
    if (expiration != 0) { StreamUtil.writeTo(expiration, os); }
    if (correlationId != null) { StreamUtil.writeTo(correlationId, os); }
    if (deliveryCount != 0) { StreamUtil.writeTo(deliveryCount, os); }
    if (jmsType != null) { StreamUtil.writeTo(jmsType, os); }

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
    id = StreamUtil.readStringFrom(is);
    toId = StreamUtil.readStringFrom(is);
    toType = StreamUtil.readByteFrom(is);
    timestamp = StreamUtil.readLongFrom(is);
    
    short s = StreamUtil.readShortFrom(is);

    if ((s & typeFlag) != 0) { type = StreamUtil.readIntFrom(is); }
    if ((s & replyToIdFlag) != 0) { replyToId = StreamUtil.readStringFrom(is); }
    if ((s & replyToTypeFlag) != 0) { replyToType = StreamUtil.readByteFrom(is); }
    if ((s & propertiesFlag) != 0) { properties = StreamUtil.readPropertiesFrom(is); }
    if ((s & priorityFlag) != 0) { priority = StreamUtil.readIntFrom(is); }
    if ((s & expirationFlag) != 0) { expiration = StreamUtil.readLongFrom(is); }
    if ((s & corrrelationIdFlag) != 0) { correlationId = StreamUtil.readStringFrom(is); }
    if ((s & deliveryCountFlag) != 0) { deliveryCount = StreamUtil.readIntFrom(is); }
    if ((s & jmsTypeFlag) != 0) { jmsType = StreamUtil.readStringFrom(is); }
    redelivered = (s & redeliveredFlag) != 0;
    persistent = (s & persistentFlag) != 0;

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
    
    if (size == -1) return null;

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

  /**
   * @throws ClassNotFoundException  
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    readFrom(in);
  }
}
