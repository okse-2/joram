/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

import java.io.IOException;
import java.util.Vector;


/**
 * The delivery modes supported are PERSISTENT and NON_PERSISTENT.
 * <br>
 * A client marks a message as persistent if it feels that the application
 * will have problems if the message is lost in transit. A client marks a
 * message as non-persistent if an occasional lost message is tolerable.
 * Clients use delivery mode to tell to the Joram's server  how to balance
 * message transport reliability with throughput.
 */
class DeliveryMode {
    /**
     * This delivery mode instructs the Joram's server to log the message to
     * stable storage as part of the client's send operation.
     */
  public final static int PERSISTENT = 1;
    /**
     * This is the lowest-overhead delivery mode because it does not require
     * that the message be logged to stable storage.
     */
  public final static int NON_PERSISTENT = 2;
}

/**
 * The Message class defines the Joram's message structure.
 * Joram's messages are composed of the following parts:<ul>
 * <li>Header - Header fields contain values used by both clients and providers
 * to identify and route messages.
 * <li>Properties - This part contains a built-in facility for supporting
 * application-defined property values. Properties provide an efficient
 * mechanism for supporting application-defined message filtering.
 * <li>Body - This part contains application specific data.
 * </ul>
 * Property values are set prior to sending a message. When a client receives
 * a message, its properties are in read-only mode. If a client attempts to
 * set properties at this point, a MessageNotWriteableException is thrown. If
 * clearProperties is called, the properties can now be both read from and
 * written to.
 * <br>
 * A message selector allows a client to specify, by header field references
 * and property references, the messages it is interested in. Only messages
 * whose header and property values match the selector are delivered.
 * <br>
 * A message selector matches a message if the selector evaluates to true
 * when the message's header field values and property values are substituted
 * for their corresponding identifiers in the selector.
 * <br>
 * A message selector is a String whose syntax is based on a subset of the
 * SQL92 conditional expression syntax. If the value of a message selector is
 * an empty string, the value is treated as a null and indicates that there
 * is no message selector for the message consumer.
  */
public class Message implements Streamable {
  Session session;

  int type = SIMPLE;
 
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

  /** The message identifier. */
  String id;

  /** <code>true</code> if the message must be persisted. **/
  boolean persistent;
  /**
   * The message priority from 0 to 9, 9 being the highest.
   * By default, the priority is 4?
   */
  int priority;
 
  /** The message expiration time, by default 0 for infinite time-to-live. */
  long expiration;

  /** The message time stamp. */
  long timestamp;

  /**
   * <code>true</code> if the message has been denied at least once by a
   * consumer.
   */
  boolean redelivered;
  /** The number of delivery attempts for this message. */
  int deliveryCount;

  /** The message destination identifier. */
  String toId;
  /** The message destination type. */
  byte toType;

  /** The reply to destination identifier. */
  String replyToId;
  /** <code>true</code> if the "reply to" destination is a queue. */
  byte replyToType;

  /** The correlation identifier field. */
  String correlationId;

  Properties properties;
  Properties optionalHeader; // TODO: Useless

  byte[] body;
  int length;
 
  /**
   * Non API method, should be hidden.
   * Constructs a bright new <code>Message</code>.
   */
  public Message() {
    session = null;

    type = SIMPLE;

    id = null;
    persistent = true;
    priority = 4;
    expiration = 0;
    timestamp = -1;
    redelivered = false;
    deliveryCount = 0;
    toId = null;
    replyToId = null;
    correlationId = null;
  }

  /**
   * Acknowledges all consumed messages of the session of this message.
   * <br>
   * Calls to acknowledge are ignored for both transacted sessions and
   * sessions specified to use implicit acknowledgement modes.
   * <br>
   * A client may individually acknowledge each message as it is consumed, or
   * it may choose to acknowledge a group of messages by calling acknowledge
   * on the last received message of the group (thereby acknowledging all
   * messages consumed by the session).
   * <br>
   * Messages that have been received but not acknowledged may be redelivered.
   */
  void acknowledge() throws IllegalStateException {
    if (session == null) throw new IllegalStateException();
    //session.acknowledge();
  }


  /**
   * Returns the message correlation identifier.
   */
  String getCorrelationID() {
    return correlationId;
  }

  /**
   * Sets the correlation ID for the message.
   */
  void setCorrelationID(String correlationID) {
    this.correlationId = correlationID;
  }
         
  /**
   * Returns <code>true</code> if the message is persistent.
   */
  int getDeliveryMode() {
    if (persistent) 
      return DeliveryMode.PERSISTENT;
    else
      return DeliveryMode.NON_PERSISTENT;
  }

  /**
   * Sets the DeliveryMode value for this message.
   */
  void setDeliveryMode(int deliveryMode) throws JoramException {
    if (deliveryMode != DeliveryMode.PERSISTENT &&
        deliveryMode != DeliveryMode.NON_PERSISTENT)
      throw new JoramException("Invalid delivery mode.");

    persistent = (deliveryMode == DeliveryMode.PERSISTENT);
  }

  /**
   * Returns the message destination.
   */
  Destination getDestination() throws JoramException {
    return Destination.newInstance(toId, toType, null);
  }

  /**
   * Sets the Destination object for this message.
   */
  void setDestination(Destination destination) {
    toId = destination.getUID();
    toType = destination.getType();
  }

  /**
   * Returns the message expiration time.
   */
  long getExpiration() {
    return expiration;
  }

  /**
   * Sets the message's expiration value.
   */
  void setExpiration(long expiration) {
    this.expiration = expiration;
  }

  /**
   * Returns the message identifier. This field contains a value that uniquely
   * identifies each message sent by a provider.
   * <br>
   * When a message is sent, this field is ignored. When the send or publish
   * method returns, it contains a provider-assigned value.
   *
   * @return the message identifier.
   */
  String getMessageID() {
    return id;
  }

  /**
   * Sets the message ID. This field is set when a message is sent. This
   * method can be used to change the value for a message that has been
   * received.
   *
   * @param id the message identifier.
   */
  void setMessageID(String id) {
    this.id = id;
  }

  /**
   * Returns the message priority level. There is ten levels of priority
   * value, with 0 as the lowest priority and 9 as the highest.
   *
   * @return the default message priority.
   */
  int getPriority() {
    return priority;
  }

  /**
   * Sets the priority level for this message. The Joram's server set this
   * field when a message is sent. This method can be used to change the value
   * for a message that has been received.
   *
   * @param priority the priority level for this message.
   */
  void setPriority(int priority) {
    this.priority = priority;
  }

  /**
   * Gets an indication of whether this message is being redelivered.
   *
   * @return true if this message is being redelivered
   */
  boolean getRedelivered() {
    return redelivered;
  }

  /**
   * Specifies whether this message is being redelivered.
   * This field is set at the time the message is delivered. This method can
   * be used to change the value for a message that has been received.
   *
   * @param redelivered an indication of whether this message is being
   *			 redelivered.
   */
  void setRedelivered(boolean redelivered) {
    this.redelivered = redelivered;
  }

  /**
   * Gets the Destination object to which a reply to this message should
   * be sent.
   *
   * @return Destination to which to send a response to this message.
   */
  Destination getReplyTo() throws JoramException {
    if (replyToId != null)
      return Destination.newInstance(replyToId, replyToType, null);

    return null;
  }


  /**
   * Sets the Destination object to which a reply to this message should
   * be sent.
   *
   * @param replyTo Destination to which to send a response to this message.
   */
  void setReplyTo(Destination replyTo) {
    replyToId = replyTo.getUID();
    replyToType = replyTo.getType();
  }

  /**
   * Returns the message timestamp.
   * This header field contains the time a message was transmit to the
   * Joram's server to be sent.
   * When a message is sent, this field is ignored. When the send or
   * publish method returns, it contains a time value somewhere in the
   * interval between the call and the return.
   * The value is in millis-seconds.
   *
   * @return the message timestamp
   */
  long getTimestamp() {
    return timestamp;
  }

  /**
   * Sets the message timestamp.
   * This field is sent when a message is sent. This method can be used to
   * change the value for a message that has been received.
   *
   * @param timestamp the timestamp for this message.
   */
  void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  // ==================================================
  // Methods about properties
  // ==================================================

  void clearProperties() {
    if (properties == null) return;
    properties.clear();
  } 

  void checkPropertyName(String name) throws MessageFormatException {
    if ((name == null) || (name.length() == 0))
      throw new MessageFormatException("Invalid property name");

    if (name.startsWith("JMS"))
      throw new MessageFormatException("Invalid property names with prefix 'JMS'");

    if (name.equalsIgnoreCase("NULL") ||
        name.equalsIgnoreCase("TRUE") ||
        name.equalsIgnoreCase("FALSE") ||
        name.equalsIgnoreCase("NOT") ||
        name.equalsIgnoreCase("AND") ||
        name.equalsIgnoreCase("OR") ||
        name.equalsIgnoreCase("BETWEEN") ||
        name.equalsIgnoreCase("LIKE") ||
        name.equalsIgnoreCase("IN") ||
        name.equalsIgnoreCase("IS") ||
        name.equalsIgnoreCase("ESCAPE"))
      throw new MessageFormatException("Invalid property names using SQL terminal");
  }

  boolean getBooleanProperty(String name) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) return false;

    return properties.getBooleanProperty(name);
  }

  byte getByteProperty(String name) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) throw new MessageFormatException();

    return properties.getByteProperty(name);
  }

  double getDoubleProperty(String name) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) throw new MessageFormatException();

    return properties.getDoubleProperty(name);
  }

  float getFloatProperty(String name) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) throw new MessageFormatException();

    return properties.getFloatProperty(name);
  }

  int getIntProperty(String name) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) throw new MessageFormatException();

    return properties.getIntProperty(name);
  }

  long getLongProperty(String name) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) throw new MessageFormatException();

    return properties.getLongProperty(name);
  }

  short getShortProperty(String name) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) throw new MessageFormatException();

    return properties.getShortProperty(name);
  }

  String getStringProperty(String name) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) throw new MessageFormatException();

    return properties.getStringProperty(name);
  }

  void setBooleanProperty(String name, boolean value) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) properties = new Properties();

    properties.setBooleanProperty(name, value);
  }

  void setByteProperty(String name, byte value) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) properties = new Properties();

    properties.setByteProperty(name, value);
  }

  void setDoubleProperty(String name, double value) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) properties = new Properties();

    properties.setDoubleProperty(name, value);
  }

  void setFloatProperty(String name, float value) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) properties = new Properties();

    properties.setFloatProperty(name, value);
  }

  void setIntProperty(String name, int value) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) properties = new Properties();

    properties.setIntProperty(name, value);
  }

  void setLongProperty(String name, long value) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) properties = new Properties();

    properties.setLongProperty(name, value);
  }

  void setShortProperty(String name, short value) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) properties = new Properties();

    properties.setShortProperty(name, value);
  }

  void setStringProperty(String name, String value) throws MessageFormatException {
    checkPropertyName(name);
    if (properties == null) properties = new Properties();

    properties.setStringProperty(name, value);
  }

  // ==================================================
  // Cloneable interface
  // ==================================================  
  public Message clone() {
    Message clone = null;
    switch (type) {
    case TEXT:
      clone = new TextMessage();
      break;
    case STREAM:
      try {
        clone = new StreamMessage();
      } catch (JoramException e) {
        //e.printStackTrace();
      }
      break;
    case BYTES:
      clone = new BytesMessage();
      break;
    case ADMIN:
      clone = new AdminMessage();
      break;
//    case OBJECT:
//      retMsg = new ObjectMessage();
//    case MAP:
//      retMsg = new MapMessage();
    default:
      clone = new Message();
    }
    clone.session = session;
    clone.type = type;
    clone.id = id;
    clone.persistent = persistent;
    clone.priority = priority;
    clone.expiration = expiration;
    clone.timestamp = timestamp;
    clone.redelivered = redelivered;
    clone.toId = toId;
    clone.toType = toType;
    clone.replyToId = replyToId;
    clone.replyToType = replyToType;
    clone.correlationId = correlationId;

    if (body != null) {
      // AF: May be we can share the body as it should be RO.
      clone.body = new byte[body.length];
      clone.length = length;
      for (int i = 0; i < body.length; i++)
        clone.body[i] = body[i];
    }
    if (optionalHeader != null) {
      clone.optionalHeader = (Properties) optionalHeader.clone();
    }
    if (properties != null) {
      clone.properties = (Properties) properties.clone();
    }
    return clone;
  }
  
  // ==================================================
  // Streamable interface
  // ==================================================

  public static final int redeliveredFlag =   0x00000004;
  public static final int persistentFlag =    0x00000008;
  public static final int deletedDestFlag =   0x00000010;
  public static final int expiredFlag =       0x00000020;
  public static final int notWriteableFlag =  0x00000040;
  public static final int undeliverableFlag = 0x00000080;

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputXStream os) throws IOException {
    int bool = 0;

    os.writeInt(type);
    os.writeProperties(optionalHeader);
    os.writeProperties(properties);
    os.writeString(id);
    os.writeInt(priority);
    os.writeString(toId);
    try {
      os.writeString(Destination.typeToString(toType));
    } catch (JoramException exc) {
      throw new IOException("Bad destination type");
    }
    os.writeLong(expiration);
    os.writeString(replyToId);
    try {
      os.writeString(Destination.typeToString(replyToType));
    } catch (JoramException exc) {
      throw new IOException("Bad replyTo type");
    }
    os.writeLong(timestamp);
    os.writeString(correlationId);
    os.writeInt(deliveryCount);

    bool = bool | (redelivered?redeliveredFlag:0);
    bool = bool | (persistent?persistentFlag:0);
//     bool = bool | (deletedDest?deletedDestFlag:0);
//     bool = bool | (expired?expiredFlag:0);
//     bool = bool | (notWriteable?notWriteableFlag:0);
//     bool = bool | (undeliverable?undeliverableFlag:0);

    os.writeInt(bool);
    os.writeByteArray(body);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputXStream is) throws IOException {
    type = is.readInt();
    optionalHeader = is.readProperties();
    properties = is.readProperties();
    id = is.readString();
    priority = is.readInt();
    toId = is.readString();
    try {
      toType = Destination.stringToType(is.readString());
    } catch (JoramException exc) {
      throw new IOException("Bad destination type");
    }
    expiration = is.readLong();
    replyToId = is.readString();
    try {
      replyToType = Destination.stringToType(is.readString());
    } catch (JoramException exc) {
      throw new IOException("Bad destination type");
    }
    timestamp = is.readLong();
    correlationId = is.readString();
    deliveryCount = is.readInt();

    int bool = is.readInt();
    redelivered = ((bool & redeliveredFlag) != 0);
    persistent = ((bool & persistentFlag) != 0);
//     deletedDest = ((bool & deletedDestFlag) != 0);
//     expired = ((bool & expiredFlag) != 0);
//     notWriteable = ((bool & notWriteableFlag) != 0);
//     undeliverable = ((bool & undeliverableFlag) != 0);
    body = is.readByteArray();
  }


  /**
   *  The object allows to write to the output stream a vector of message.
   *
   * @param messages 	the vector of messages
   * @param os 		the stream to write the vector to
   */
  static void writeVectorTo(Vector messages,
                            OutputXStream os) throws IOException {
    if (messages == null) {
      os.writeInt(-1);
    } else {
      int size = messages.size();
      os.writeInt(size);
      for (int i=0; i<size; i++) {
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
  static Vector readVectorFrom(InputXStream is) throws IOException {
    int size = is.readInt();
    if (size == -1) {
      return null;
    } else {
      Vector messages = new Vector(size);
      for (int i=0; i<size; i++) {
        Message msg = new Message();
        msg.readFrom(is);
        messages.addElement(msg);
      }
      return messages;
    }
  }
}
