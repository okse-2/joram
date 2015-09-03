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
import java.util.Vector;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.objectweb.joram.shared.admin.AbstractAdminMessage;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableHelper;
import fr.dyade.aaa.common.encoding.Encoder;
import fr.dyade.aaa.common.stream.Properties;
import fr.dyade.aaa.common.stream.StreamUtil;
import fr.dyade.aaa.common.stream.Streamable;

/**
 * Implements the <code>Message</code> data structure.
 */
public final class Message implements Cloneable, Serializable, Streamable, Encodable {
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

  /** 
   * Body of the message. 
   * on client side, used getBody and setBody instead of direct access to the body.
   */
  public transient byte[] body = null;
  
  /**
   * The offset of the subarray in <code>body</code> to be used; 
   * must be non-negative and no larger than <code>array.length</code>.
   */
  public transient int bodyOffset;

  /**
   * The length of the subarray in <code>body</code> to be used; 
   * must be non-negative and no larger than <code>array.length - offset</code>.
   * Value <code>-1</code> means that there is no subarray in <code>body</code> 
   * and <code>bodyOffset</code> is ignored.
   * Default value is <code>bodyOffset</code>.
   */
  public transient int bodyLength = -1;

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
   * Be careful, this type is coded on 4 bits (see writeTo and readFrom methods).
   */
  public transient int type = SIMPLE;

  /**
   * The JMSType header field contains a message type identifier supplied by a
   * client when a message is sent.
   */
  public transient String jmsType = null;

  /**
   * The message priority from 0 to 9, 9 being the highest.
   * By default, the priority is 4.
   * Be careful, this type is coded on 4 bits (see writeTo and readFrom methods).
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

  /** The message destination name. */
  public transient String toName = null;

  /** The message destination type. */
  public transient byte toType;
  
  /** <code>true</code> if compressed body. */
  public transient boolean compressed;
  
  /** 
   * If the message body is upper than the <code>compressedMinSize</code>,
   * this message body is compressed.
   */
  public transient int compressedMinSize;
  
  public transient int compressionLevel = Deflater.BEST_SPEED;
  
  /** the message delivery time value. */
  public transient long deliveryTime;
  
  /** The client connection identification */
  public transient String clientID;

  /**
   * Sets the message destination.
   *
   * @param id  The destination identifier.
   * @param name The destination name.
   * @param type The type of the destination.
   */
  public final void setDestination(String id, String name, byte type) {
    toId = id;
    toName = name;
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
  
  /** The reply to destination name. */
  public transient String replyToName = null;
  
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
  public final void setReplyTo(String id, String name, byte type) {
    replyToId = id;
    replyToName = name;
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
  	setBody(toBytes(text));
  }

  /**
   * Returns the text body of the message.
   * @throws Exception In case of an error while getting the text
   */
  public String getText() throws Exception {
    if (body == null) {
      return null;
    }
    return (String) fromBytes(getBody());
  }

  /**
   * Sets an object as the body of the message. 
   *
   * @exception IOException  In case of an error while setting the object.
   */
  public void setObject(Serializable object) throws IOException {
    type = Message.OBJECT;
    setBody(toBytes(object));
  }

  /**
   * Returns the object body of the message.
   *
   * @exception Exception  In case of an error while getting the object.
   */
  public Serializable getObject() throws Exception {
    // TODO (AF): May be, we should verify that it is an Object message!!
    return fromBytes(getBody());
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
      setBody(baos.toByteArray());
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
     bais = new ByteArrayInputStream(getBody());
     adminMsg = AbstractAdminMessage.read(bais);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "ERROR: getAdminMessage()", e);
    }
    return adminMsg;
  }

  /**
   * set the body.
   * compress if the body length > compressedMinSize
   * 
   * @param body a byte array
   * @throws IOException if an I/O error has occurred
   */
  public void setBody(byte[] body) throws IOException {
    if (compressedMinSize > 0 && body != null && body.length > compressedMinSize) {
      long length = body.length;
      this.body = compress(body, compressionLevel);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, type + " : setBody: compressedMinSize = " + compressedMinSize + 
            ", compressionLevel = " + compressionLevel +
            ", body.length  before = " + length + 
            ", after = " + this.body.length + 
            ", compression = " + (100-this.body.length*100/length) + "%");
      compressed = true;
    } else {
      compressed = false;
      this.body = body;
    }
    bodyOffset = 0;
    if (this.body != null) {
      bodyLength = this.body.length;
    }
  }
  
  public void trimBody() {
    if (body != null && bodyLength >= 0
        && (bodyOffset > 0 || bodyLength != body.length)) {
      // Create a new byte array that fits the body
      byte[] newBody = new byte[bodyLength];
      System.arraycopy(body, bodyOffset, newBody, 0, bodyLength);
      // Replace the body with the new body
      body = newBody;
      bodyOffset = 0;
    }
  }
  
  /**
   * get the body
   * Uncompress if compressed
   * 
   * @return the body
   * @throws IOException if an I/O error has occurred
   */
  public byte[] getBody() throws IOException {
    trimBody();
    
    if (compressed && (body != null)) {
      body = uncompress(body);
      bodyLength = body.length;
      compressed = false;
    }
    return body;
  }
  
  /**
   * set body = null
   */
  public void clearBody() {
    body = null;
    bodyLength = -1;
  }
  
  
  /**
   * @return true if body == null
   */
  public boolean isNullBody() {
    return body == null;
  }
  
  /**
   * @return the body length, 0 if body == null
   */
  public int getBodyLength() {
    if (body == null) return 0;
    if (bodyLength < 0) {
      return body.length;
    } else {
      return bodyLength;
    }
  }
 
  /**
   * compress byte array
   * 
   * @param toCompress a byte array to compress
   * @return the compressed byte array
   * @throws IOException if an I/O error has occurred
   */
  public static byte[] compress(byte[] toCompress, int compressionLevel) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "compress(" + toCompress + ')');

    int bodySize = toCompress.length;
    ByteArrayOutputStream baos = new ByteArrayOutputStream(bodySize+4);
    baos.write((byte) (bodySize >>> 24));
    baos.write((byte) (bodySize >>> 16));
    baos.write((byte) (bodySize >>> 8));
    baos.write((byte) (bodySize >>> 0));
    
    Deflater compresser = new Deflater(compressionLevel);
    compresser.setInput(toCompress);
    compresser.finish();

    byte[] buff = new byte[1024];
    while(!compresser.finished()) {
      int count = compresser.deflate(buff);
      baos.write(buff, 0, count);
    }
    baos.close();
    compresser.end();
    return baos.toByteArray();
  }
  
  /**
   * Uncompress byte array
   * 
   * @param toUncompress a compressed byte array
   * @return the uncompressed byte array
   * @throws IOException if an I/O error has occurred
   */
  public static byte[] uncompress(byte[] toUncompress) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "uncompress(" + toUncompress + ')');
    
    try {
      Inflater decompresser = new Inflater();
      int size = (((toUncompress[0] &0xFF) << 24) | 
          ((toUncompress[1] &0xFF) << 16) |
          ((toUncompress[2] &0xFF) << 8) | 
          (toUncompress[3] &0xFF));

      byte[] uncompressed = new byte[size];
      decompresser.setInput(toUncompress, 4, toUncompress.length-4);
      int resultLength = decompresser.inflate(uncompressed);
      decompresser.end();
      return uncompressed;
    } catch (DataFormatException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, e);
      throw new IOException(e);
    }
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
    if (toName != null)
      strbuf.append('(').append(toName).append(')');
    strbuf.append(",replyToId=").append(replyToId);
    if (replyToName != null)
      strbuf.append('(').append(replyToName).append(')');
    strbuf.append(",correlationId=").append(correlationId);
    strbuf.append(",compressed=").append(compressed);
    strbuf.append(",deliveryTime=").append(deliveryTime);
    strbuf.append(",clientID=").append(clientID);
    strbuf.append(')');
  }

  /** Clones the message. */
  public Object clone() {
    try {
      Message clone = (Message) super.clone();
      if (body != null) {
        // AF: May be we can share the body as it should be RO.
        if (bodyLength < 0) {
          clone.body = new byte[body.length];
          System.arraycopy(body, 0, clone.body, 0, body.length);
        } else {
          clone.body = new byte[bodyLength];
          clone.bodyOffset = 0;
          clone.bodyLength = bodyLength;
          System.arraycopy(body, bodyOffset, clone.body, 0, bodyLength);
        }
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

  private static final short typeFlag = 0x0001;
  private static final short replyToIdFlag = 0x0002;
//  private static final short replyToTypeFlag = 0x0004;
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
    if (bodyLength < 0) {
      StreamUtil.writeTo(body, os);
    } else {
      StreamUtil.writeTo(body, bodyOffset, bodyLength, os);
    }
  }

  public void writeHeaderTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(id, os);
    StreamUtil.writeTo(toId, os);
    StreamUtil.writeTo(toName, os);
    StreamUtil.writeTo(toType, os);
    StreamUtil.writeTo(timestamp, os);
    StreamUtil.writeTo(compressed, os);
    StreamUtil.writeTo(deliveryTime, os);
    StreamUtil.writeTo(clientID, os);

    // One short is used to know which fields are set
    short s = 0;
    if (type != SIMPLE) { s |= typeFlag; }
    if (replyToId != null) { s |= replyToIdFlag; }
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
//    if ((type != SIMPLE) || (priority != DEFAULT_PRIORITY)) {
//      byte b = (byte) (((priority << 4) & 0xF0) | (type & 0x0F));
//      StreamUtil.writeTo(b, os);
//    }
    if (replyToId != null) {
      StreamUtil.writeTo(replyToId, os);
      StreamUtil.writeTo(replyToName, os);
      StreamUtil.writeTo(replyToType, os);
    }
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
    if (body != null) {
      bodyLength = body.length;
    }
  }

  public void readHeaderFrom(InputStream is) throws IOException {
    id = StreamUtil.readStringFrom(is);
    toId = StreamUtil.readStringFrom(is);
    toName = StreamUtil.readStringFrom(is);
    toType = StreamUtil.readByteFrom(is);
    timestamp = StreamUtil.readLongFrom(is);
    compressed = StreamUtil.readBooleanFrom(is);
    deliveryTime = StreamUtil.readLongFrom(is);
    clientID = StreamUtil.readStringFrom(is);
    
    short s = StreamUtil.readShortFrom(is);

    if ((s & typeFlag) != 0) { type = StreamUtil.readIntFrom(is); }
//    if ((s & (typeFlag|priorityFlag)) != 0) {
//      byte b = StreamUtil.readByteFrom(is);
//      type = b & 0x0F;
//      priority = (b >> 4) & 0xF0;
//    } else {
//      type = SIMPLE;
//      priority = DEFAULT_PRIORITY;
//    }
    if ((s & replyToIdFlag) != 0) {
      replyToId = StreamUtil.readStringFrom(is);
      replyToName = StreamUtil.readStringFrom(is);
      replyToType = StreamUtil.readByteFrom(is);
    }
    if ((s & propertiesFlag) != 0) { properties = StreamUtil.readPropertiesFrom(is); }
    priority = DEFAULT_PRIORITY;
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
  
  public int getEncodableClassId() {
    // Not defined
    return -1;
  }
  
  public int getEncodedSize() throws Exception {
    int encodedSize = EncodableHelper.getStringEncodedSize(id);
    
    encodedSize += EncodableHelper.getStringEncodedSize(toId);
    encodedSize += EncodableHelper.getNullableStringEncodedSize(toName);
    encodedSize += BYTE_ENCODED_SIZE + LONG_ENCODED_SIZE + BOOLEAN_ENCODED_SIZE + LONG_ENCODED_SIZE + SHORT_ENCODED_SIZE;

    if (type != org.objectweb.joram.shared.messages.Message.SIMPLE) { encodedSize += BYTE_ENCODED_SIZE; }
    if (replyToId != null) {
      encodedSize += EncodableHelper.getStringEncodedSize(replyToId);
      encodedSize += EncodableHelper.getStringEncodedSize(replyToName);
      encodedSize += BYTE_ENCODED_SIZE;
    }
    if (properties != null) { encodedSize += properties.getEncodedSize(); }
    if (priority != org.objectweb.joram.shared.messages.Message.DEFAULT_PRIORITY) { encodedSize += INT_ENCODED_SIZE; }
    if (expiration != 0) { encodedSize += LONG_ENCODED_SIZE; }
    if (correlationId != null) { encodedSize += EncodableHelper.getStringEncodedSize(correlationId); }
    if (deliveryCount != 0) { encodedSize += INT_ENCODED_SIZE; }
    if (jmsType != null) { encodedSize += EncodableHelper.getStringEncodedSize(jmsType); }
    
    if (bodyLength < 0) {
      encodedSize += EncodableHelper.getNullableByteArrayEncodedSize(body);
    } else {
      encodedSize += EncodableHelper.getNullableByteArrayEncodedSize(body,
          bodyLength);
    }
    encodedSize += EncodableHelper.getNullableStringEncodedSize(clientID);
    
    return encodedSize;
  }
  
  public void encode(Encoder encoder) throws Exception {
    encoder.encodeString(id);
    
    encoder.encodeString(toId);
    encoder.encodeNullableString(toName);
    encoder.encodeByte(toType);
    encoder.encodeUnsignedLong(timestamp);
    encoder.encodeBoolean(compressed);
    encoder.encodeUnsignedLong(deliveryTime);

    // One short is used to know which fields are set
    short s = 0;
    if (type != org.objectweb.joram.shared.messages.Message.SIMPLE) { s |= typeFlag; }
    if (replyToId != null) { s |= replyToIdFlag; }
    if (properties != null) { s |= propertiesFlag; }
    if (priority != org.objectweb.joram.shared.messages.Message.DEFAULT_PRIORITY) { s |= priorityFlag; }
    if (expiration != 0) { s |= expirationFlag; }
    if (correlationId != null) { s |= corrrelationIdFlag; }
    if (deliveryCount != 0) { s |= deliveryCountFlag; }
    if (jmsType != null) { s |= jmsTypeFlag; }
    if (redelivered) { s |= redeliveredFlag; }
    if (persistent) { s |= persistentFlag; }
    
    encoder.encode16(s);
    
    if (type != org.objectweb.joram.shared.messages.Message.SIMPLE) { encoder.encodeByte((byte) type); }
    if (replyToId != null) {
      encoder.encodeString(replyToId);
      encoder.encodeString(replyToName);
      encoder.encodeByte(replyToType);
    }
    if (properties != null) { properties.encode(encoder); }
    if (priority != org.objectweb.joram.shared.messages.Message.DEFAULT_PRIORITY) { encoder.encodeUnsignedInt(priority); }
    if (expiration != 0) { encoder.encodeUnsignedLong(expiration); }
    if (correlationId != null) { encoder.encodeString(correlationId); }
    if (deliveryCount != 0) { encoder.encodeUnsignedInt(deliveryCount); }
    if (jmsType != null) { encoder.encodeString(jmsType); }
    
    if (bodyLength < 0) {
      encoder.encodeNullableByteArray(body);
    } else {
      encoder.encodeNullableByteArray(body, bodyOffset, bodyLength);
    }
    encoder.encodeNullableString(clientID);
  }
  
  public void decode(Decoder decoder) throws Exception {    
    id = decoder.decodeString();
    
    toId = decoder.decodeString();
    toName = decoder.decodeNullableString();
    toType = decoder.decodeByte();
    timestamp = decoder.decodeUnsignedLong();
    compressed = decoder.decodeBoolean();
    deliveryTime = decoder.decodeUnsignedLong();
    
    short s = decoder.decode16();

    if ((s & typeFlag) != 0) { type = decoder.decodeByte(); }
    if ((s & replyToIdFlag) != 0) {
      replyToId = decoder.decodeString();
      replyToName = decoder.decodeString();
      replyToType = decoder.decodeByte();
    }
    if ((s & propertiesFlag) != 0) {
      properties = new Properties();
      properties.decode(decoder); 
    }
    priority = org.objectweb.joram.shared.messages.Message.DEFAULT_PRIORITY;
    if ((s & priorityFlag) != 0) { priority = decoder.decodeUnsignedInt(); }
    if ((s & expirationFlag) != 0) { expiration = decoder.decodeUnsignedLong(); }
    if ((s & corrrelationIdFlag) != 0) { correlationId = decoder.decodeString(); }
    if ((s & deliveryCountFlag) != 0) { deliveryCount = decoder.decodeUnsignedInt(); }
    if ((s & jmsTypeFlag) != 0) { jmsType = decoder.decodeString(); }
    redelivered = (s & redeliveredFlag) != 0;
    persistent = (s & persistentFlag) != 0;
    
    body = decoder.decodeNullableByteArray();
    bodyOffset = 0;
    if (body != null) {
      bodyLength = body.length;
    }
    clientID = decoder.decodeNullableString();
  }
  
  public static int getMessageVectorEncodedSize(Vector<Message> messages) throws Exception {
    int res = BOOLEAN_ENCODED_SIZE;
    if (messages != null) {
      res += INT_ENCODED_SIZE;
      for (Message msg : messages) {
        res += msg.getEncodedSize();
      }
    }
    return res;
  }
  
  public static void encodeMessageVector(Vector<Message> messages, Encoder encoder) throws Exception {
    if (messages == null) {
      encoder.encodeBoolean(true);
    } else {
      encoder.encodeBoolean(false);
      encoder.encodeUnsignedInt(messages.size());
      for (Message msg : messages) {
        msg.encode(encoder);
      }
    }
  }
  
  public static Vector<Message> decodeMessageVector(Decoder decoder) throws Exception {
    boolean nullFlag = decoder.decodeBoolean();
    if (nullFlag) {
      return null;
    } else {
      int size = decoder.decodeUnsignedInt();
      Vector messages = new Vector(size);
      for (int i=0; i<size; i++) {
        Message msg = new Message();
        msg.decode(decoder);
        messages.addElement(msg);
      }
      return messages;
    }
  }
  
}
