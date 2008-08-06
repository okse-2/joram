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
package org.objectweb.joram.mom.messages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.Vector;

import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.joram.shared.excepts.MessageException;
import org.objectweb.joram.shared.excepts.MessageROException;
import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.stream.StreamUtil;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.util.Transaction;

/** 
 * The <code>Message</code> class actually provides the transport facility
 * for the data exchanged during MOM operations.
 * <p>
 * A message content is always wrapped as a bytes array, it is charaterized
 * by properties and "header" fields.
 */
public final class Message implements Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Arrival position of this message on its queue or proxy. */
  transient public long order;

  /**
   * The number of acknowledgements a message still expects from its 
   * subscribers before having been fully consumed by them (field used
   * by JMS proxies).
   * Be careful, this field is not saved but set to 0 during message
   * loading then claculated during the proxy initialisation.
   */
  public transient int acksCounter;
  /**
   * The number of acknowledgements a message still expects from its 
   * durable subscribers before having been fully consumed by them (field used
   * by JMS proxies).
   * Be careful, this field is not saved but set to 0 during message
   * loading then claculated during the proxy initialisation.
   */
  public transient int durableAcksCounter;

  /**
   * Reference to the MOM message.
   */
  private transient org.objectweb.joram.shared.messages.Message msg;
  /**
   * SoftReference to the body of the MOM message.
   */
  private transient SoftReference body = null;
  /** <code>true</code> if the body is empty. */
  private transient boolean nobody;
  
  /**
   * Constructs a <code>Message</code> instance.
   */
  public Message(org.objectweb.joram.shared.messages.Message msg) {
    this(msg, false);
  }
  
  /**
   * Constructs a <code>Message</code> instance with .
   */
  public Message(org.objectweb.joram.shared.messages.Message msg, boolean soft) {
    this.msg = msg;
    nobody = (msg.body == null);
    if (soft & !nobody)
      body = new SoftReference(msg.body);
  }

  /**
   * Returns the contained message eventually without the body.
   * 
   * @return The contained message.
   */
  public org.objectweb.joram.shared.messages.Message getHeaderMessage() {
  	return msg;
  }

  /**
   * Returns the contained message with body.
   * If needed the body is loaded from repository.
   * 
   * @return The contained message.
   */
  public org.objectweb.joram.shared.messages.Message getFullMessage() {
    // AF: be careful if the message has no body
    if (msg.body != null)
      return msg;

    if (body != null)
      msg.body = (byte[]) body.get();

    if (msg.body != null) {
      // Try lo load the body from repository
    }
    
  	return msg;
  }
  
  /** Returns the message type. */
  public int getType() {
    return msg.type;
  }

  /** Returns the message identifier. */
  public String getIdentifier() {
    return msg.id;
  }

  /** Sets the message identifier. */ 
  public void setIdentifier(String id) {
    msg.id = id;
  }

  /** Returns <code>true</code> if the message is persistent. */
  public boolean getPersistent() {
    return msg.persistent;
  }

  /** Sets the message persistence mode. */
  public void setPersistent(boolean persistent) {
    msg.persistent = persistent;
  }

  /** Returns the message priority. */
  public int getPriority() {
    return msg.priority;
  }

  /**
   * Sets the message priority.
   *
   * @param priority  Priority value: 0 the lowest, 9 the highest, 4 normal.
   */ 
  public void setPriority(int priority) {
    if (priority >= 0 && priority <= 9)
      msg.priority = priority;
  }

  /** Returns the message expiration time. */
  public long getExpiration() {
    return msg.expiration;
  }

  /**
   * Sets the message expiration.
   *
   * @param expiration	The expiration time.
   */
  public void setExpiration(long expiration) {
    if (expiration >= 0)
      msg.expiration = expiration;
  }
  
  /** Returns the message time stamp. */
  public long getTimestamp() {
    return msg.timestamp;
  }

  /** Sets the message time stamp. */
  public void setTimestamp(long timestamp) {
    msg.timestamp = timestamp;
  }

  /** Returns the message correlation identifier. */
  public final String getCorrelationId() {
    return msg.correlationId;
  }

  /** Sets the message correlation identifier. */
  public void setCorrelationId(String correlationId) {
    msg.correlationId = correlationId;
  }

	/** Returns the message delivery count.*/
	public int getDeliveryCount() {
		return msg.deliveryCount;
	}

  /** Sets the message delivery count. */
  public void setDeliveryCount(int deliveryCount) {
    msg.deliveryCount = deliveryCount;
  }

  /** Increments the message delivery count. */
  public void incDeliveryCount() {
    msg.deliveryCount += 1;
  }

	/** Sets the message redelivered flag. */
	public void setRedelivered() {
		msg.redelivered = true;
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
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Invalid property name: " + name);

    if (value instanceof Boolean ||
        value instanceof Number ||
        value instanceof String) {
      msg.setProperty(name, value);
    } else {
      throw new MessageValueException("Can't set non primitive Java object"
                                      + " as a property value.");
    }
  }

//   /**
//    * Sets an object as the body of the message. 
//    * AF: Used to wrap addministration message !!
//    *
//    * @exception IOException  In case of an error while setting the object.
//    */
//   public void setObject(Object object) throws IOException {
//     msg.type = Message.OBJECT;

//     if (object == null) {
//       msg.body = null;
//     } else {
//       ByteArrayOutputStream baos = new ByteArrayOutputStream();
//       ObjectOutputStream oos = new ObjectOutputStream(baos);
//       oos.writeObject(object);
//       oos.flush();
//       msg.body = baos.toByteArray();
//       oos.close();
//       baos.close();
//     }
//   }

//   /**
//    * Returns the object body of the message.
//    * AF: Used to wrap addministration message !!
//    *
//    * @exception IOException  In case of an error while getting the object.
//    * @exception ClassNotFoundException  If the object class is unknown.
//    */
//   public Object getObject() throws Exception {
//     // AF: May be, we should verify that it is an Object message!!
//     if (msg.body == null) return null;

//     ByteArrayInputStream bais = new ByteArrayInputStream(msg.body);
//     ObjectInputStream ois = new ObjectInputStream(bais);
//     Object obj = ois.readObject();
//     ois.close();

//     return obj;
//   }

  /**
   * Returns <code>true</code> if the message is valid. The message is valid if
   * not expired.
   *
   * @param currentTime	The current time to verify the expiration time.
   */
  public boolean isValid(long currentTime) {
    return msg.expiration == 0 || msg.expiration > currentTime;
  }

  /** Name used to store the message */
  transient String txname = null;

  public void setTxName(String txname) {
    this.txname = txname;
  }

  public String getTxName() {
    return txname;
  }

  public static Message load(String txname) throws IOException, ClassNotFoundException {
    if (JoramTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgMessage.log(BasicLevel.DEBUG,
                                  "Message.load:" + txname);

    Message msg = (Message) AgentServer.getTransaction().load(txname);
    msg.txname = txname;

    return msg;
  }

  public void save() {
    if (JoramTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgMessage.log(BasicLevel.DEBUG,
                                  "Message.save:" + txname);

    if (! getPersistent()) return;
    try {
      AgentServer.getTransaction().save(this, txname);
    } catch (IOException exc) {
      JoramTracing.dbgMessage.log(BasicLevel.ERROR,
                                  "Message named [" + txname + "] could not be saved", exc);
    }
  }

  public void delete() {
    if (JoramTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgMessage.log(BasicLevel.DEBUG,
                                  "Message.delete:" + txname);

    if (! getPersistent()) return;
    AgentServer.getTransaction().delete(txname);
  }

  /** Loads all persisted messages. */
  public static Vector loadAll(String msgTxname) {
    if (JoramTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgMessage.log(BasicLevel.DEBUG,
                                  "MessagePersistenceModule.loadAll() " + msgTxname);

    Vector messages = new Vector();

    // Retrieving the names of the persistence message previously saved. 
    Transaction tx = AgentServer.getTransaction();
    String[] names = tx.getList(msgTxname);

    // Retrieving the messages individually persisted.
    for (int i = 0; i < names.length; i++) {
      try {
        Message msg = (Message) tx.load(names[i]);
        msg.txname = names[i];

        if (JoramTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgMessage.log(BasicLevel.DEBUG,
                                      "loadAll: names[" + i + "] = " + msg);
        messages.add(msg);
      } catch (Exception exc) {
        JoramTracing.dbgMessage.log(BasicLevel.ERROR,
                                    "Message named [" + names[i] + "] could not be loaded", exc);
      }
    }
    return messages;
  }

  /** Deletes all persisted objects. */
  public static void deleteAll(String msgTxname) {
    if (JoramTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgMessage.log(BasicLevel.DEBUG,
                                  "MessagePersistenceModule.deleteAll() " + msgTxname);

    Transaction tx = AgentServer.getTransaction();

    // Retrieving the names of the persistence message previously saved. 
    String[] names = tx.getList(msgTxname);

    // Deleting the message.
    for (int i = 0; i < names.length; i++) {
      tx.delete(names[i]);
    }
  }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Serializable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  public final static int SIZE = Integer.MAX_VALUE;

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeLong(order);
    msg.writeHeaderTo(out);
    if ((msg.body != null) && (msg.body.length > SIZE)) {
      StreamUtil.writeTo((byte[]) null, out);
    } else {
      StreamUtil.writeTo(msg.body, out);
    }
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    order = in.readLong();

    acksCounter = 0;
    durableAcksCounter = 0;

    msg = new org.objectweb.joram.shared.messages.Message();
    msg.readHeaderFrom(in);
    msg.body = StreamUtil.readByteArrayFrom(in);
  }
}
