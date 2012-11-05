/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 CNES
 * Copyright (C) 2006 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s):
 */
#ifndef MESSAGE_H
#define MESSAGE_H

#include "Types.H"
#include "Vector.H"
#include "XStream.H"
#include "XoramException.H"

class Session;
class Destination;

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
 public:
  enum {
    /**
     * This delivery mode instructs the Joram's server to log the message to
     * stable storage as part of the client's send operation.
     */
    PERSISTENT,
    /**
     * This is the lowest-overhead delivery mode because it does not require
     * that the message be logged to stable storage.
     */
    NON_PERSISTENT
  };
};

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
class Message : Streamable {
 public:
  Session* session;

  int type;

  byte* body;
 
  /** A simple message carries an empty body. */
  const static int SIMPLE = 0;
  /** A text message carries a String body. */
  const static int TEXT = 1;
  /** An object message carries a serializable object. */
  const static int OBJECT = 2;
  /** A map message carries an hashtable. */
  const static int MAP = 3;
  /** A stream message carries a bytes stream. */
  const static int STREAM = 4;
  /** A bytes message carries an array of bytes. */
  const static int BYTES = 5;
  /** A admin message carries an array of bytes. */
  const static int ADMIN = 6;

  /** body length */
  int length;

 private:
  /** The message identifier. */
  char* id;

  /** <code>true</code> if the message must be persisted. **/
  boolean persistent;
  /**
   * The message priority from 0 to 9, 9 being the highest.
   * By default, the priority is 4?
   */
  int priority;
 
  /** The message expiration time, by default 0 for infinite time-to-live. */
  long long expiration;

  /** The message time stamp. */
  long long timestamp;

  /**
   * <code>true</code> if the message has been denied at least once by a
   * consumer.
   */
  boolean redelivered;
  /** The number of delivery attempts for this message. */
  int deliveryCount;

  /** The message destination identifier. */
  char* toId;
  /** The message destination type. */
  char toType;

  /** The reply to destination identifier. */
  char* replyToId;
  /** <code>true</code> if the "reply to" destination is a queue. */
  char replyToType;

  /** The correlation identifier field. */
  char* correlationId;

  Properties* properties;
  Properties* optionalHeader; // TODO: Useless

  void checkPropertyName(char* name) throw (MessageFormatException);

  const static int redeliveredFlag =   0x00000004;
  const static int persistentFlag =    0x00000008;
  const static int deletedDestFlag =   0x00000010;
  const static int expiredFlag =       0x00000020;
  const static int notWriteableFlag =  0x00000040;
  const static int undeliverableFlag = 0x00000080;

 public:
  /**
   * Non API method, should be hidden.
   * Constructs a bright new <code>Message</code>.
   */
  Message();
  
  ~Message();

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
  void acknowledge();

  /**
   * Returns the message correlation identifier.
   */
  char* getCorrelationID();

  /**
   * Sets the correlation ID for the message.
   */
  void setCorrelationID(char* correlationID);
         
  /**
   * Returns <code>true</code> if the message is persistent.
   */
  int getDeliveryMode();

  /**
   * Sets the DeliveryMode value for this message.
   */
  void setDeliveryMode(int deliveryMode);

  /**
   * Returns the message destination.
   */
  Destination* getDestination();

  /**
   * Sets the Destination object for this message.
   */
  void setDestination(Destination* destination);

  /**
   * Returns the message expiration time.
   */
  long long getExpiration();

  /**
   * Sets the message's expiration value.
   */
  void setExpiration(long long expiration);

  /**
   * Returns the message identifier. This field contains a value that uniquely
   * identifies each message sent by a provider.
   * <br>
   * When a message is sent, this field is ignored. When the send or publish
   * method returns, it contains a provider-assigned value.
   *
   * @return the message identifier.
   */
  char* getMessageID();

  /**
   * Sets the message ID. This field is set when a message is sent. This
   * method can be used to change the value for a message that has been
   * received.
   *
   * @param id the message identifier.
   */
  void setMessageID(char* id);

  /**
   * Returns the message priority level. There is ten levels of priority
   * value, with 0 as the lowest priority and 9 as the highest.
   *
   * @return the default message priority.
   */
  int getPriority();

  /**
   * Sets the priority level for this message. The Joram's server set this
   * field when a message is sent. This method can be used to change the value
   * for a message that has been received.
   *
   * @param priority the priority level for this message.
   */
  void setPriority(int priority);

  /**
   * Gets an indication of whether this message is being redelivered.
   *
   * @return true if this message is being redelivered
   */
  boolean getRedelivered();

  /**
   * Specifies whether this message is being redelivered.
   * This field is set at the time the message is delivered. This method can
   * be used to change the value for a message that has been received.
   *
   * @param redelivered an indication of whether this message is being
   *			 redelivered.
   */
  void setRedelivered(boolean redelivered);

  /**
   * Gets the Destination object to which a reply to this message should
   * be sent.
   *
   * @return Destination to which to send a response to this message.
   */
  Destination* getReplyTo();

  /**
   * Sets the Destination object to which a reply to this message should
   * be sent.
   *
   * @param replyTo Destination to which to send a response to this message.
   */
  void setReplyTo(Destination* replyTo);

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
  long long getTimestamp();

  /**
   * Sets the message timestamp.
   * This field is sent when a message is sent. This method can be used to
   * change the value for a message that has been received.
   *
   * @param timestamp the timestamp for this message.
   */
  void setTimestamp(long long timestamp);

  // ==================================================
  // Methods about properties
  // ==================================================

  void clearProperties();

  boolean getBooleanProperty(char* name);

  byte getByteProperty(char* name);

  double getDoubleProperty(char* name);

  float getFloatProperty(char* name);

  int getIntProperty(char* name);

  long getLongProperty(char* name);

  short getShortProperty(char* name);

  char* getStringProperty(char* name);

  void setBooleanProperty(char* name, boolean value);

  void setByteProperty(char* name, byte value);

  void setDoubleProperty(char* name, double value);

  void setFloatProperty(char* name, float value);

  void setIntProperty(char* name, int value);

  void setLongProperty(char* name, long value);

  void setShortProperty(char* name, short value);

  void setStringProperty(char* name, char* value);

  // ==================================================
  // Cloneable interface
  // ==================================================

  Message* clone();

  // ==================================================
  // Streamable interface
  // ==================================================
  virtual void writeTo(OutputStream* os) throw (IOException);
  virtual void readFrom(InputStream* is) throw (IOException);

  static void writeVectorTo(Vector<Message>* messages,
                            OutputStream* os) throw (IOException);
  static Vector<Message>* readVectorFrom(InputStream* is) throw (IOException);
};

#endif /* MESSAGE_H */
