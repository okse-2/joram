/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2007 ScalAgent Distributed Technologies
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

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.objectweb.joram.shared.excepts.*;

import fr.dyade.aaa.util.Transaction;
import fr.dyade.aaa.agent.AgentServer;

import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/** 
 * The <code>Message</code> class actually provides the transport facility
 * for the data exchanged during MOM operations.
 * <p>
 * A message content is always wrapped as a bytes array, it is charaterized
 * by properties and "header" fields.
 */
public final class Message implements Serializable {
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

  public transient org.objectweb.joram.shared.messages.Message msg;

  /**
   * Constructs a <code>Message</code> instance.
   */
  public Message(org.objectweb.joram.shared.messages.Message msg) {
    this.msg = msg;
  }

  /** Sets the message identifier. */ 
  public void setIdentifier(String id) {
    msg.id = id;
  }

  /** Sets the message persistence mode. */
  public void setPersistent(boolean persistent) {
    msg.persistent = persistent;
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

  /**
   * Sets the message expiration.
   *
   * @param expiration	The expiration time.
   */
  public void setExpiration(long expiration) {
    if (expiration >= 0)
      msg.expiration = expiration;
  }

  /** Sets the message time stamp. */
  public void setTimestamp(long timestamp) {
    msg.timestamp = timestamp;
  }

//   /**
//    * Sets the message destination.
//    *
//    * @param id  The destination identifier.
//    * @param type The type of the destination.
//    */
//   public void setDestination(String id, String type) {
//     msg.setDestination(id, type);
//   }

  /** Sets the message correlation identifier. */
  public void setCorrelationId(String correlationId) {
    msg.correlationId = correlationId;
  }

//   /**
//    * Sets the destination to which a reply should be sent.
//    *
//    * @param id  The destination identifier.
//    * @param type The destination type.
//    */
//   public void setReplyTo(String id, String type) {
//     msg.setReplyTo(id, type);
//   }

  /** Returns the message type. */
  public int getType() {
    return msg.type;
  }

  /** Returns the message identifier. */
  public String getIdentifier() {
    return msg.id;
  }

  /** Returns <code>true</code> if the message is persistent. */
  public boolean getPersistent() {
    return msg.persistent;
  }

  /** Returns the message priority. */
  public int getPriority() {
    return msg.priority;
  }

  /** Returns the message expiration time. */
  public long getExpiration() {
    return msg.expiration;
  }
  
  /** Returns the message time stamp. */
  public long getTimestamp() {
    return msg.timestamp;
  }

//   /** Returns the message destination identifier. */
//   public final String getDestinationId() {
//     return msg.toId;
//   }

//   /** Returns <code>true</code> if the destination is a queue. */
//   public final String toType() {
//     return msg.toType;
//   }

  /** Returns the message correlation identifier. */
  public final String getCorrelationId() {
    return msg.correlationId;
  }

//   /** Returns the destination id the reply should be sent to. */
//   public final String getReplyToId() {
//     return msg.replyToId;
//   }

//   /** Returns <code>true</code> if the reply to destination is a queue. */
//   public final String replyToType() {
//     return msg.replyToType;
//   }

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
   * Returns <code>true</code> if the message is valid.
   *
   * @param currentTime	The current time to verify the expiration time.
   */
  public boolean isValid(long currentTime) {
    if (msg.expiration == 0)
      return true;

    return ((msg.expiration - currentTime) > 0);
  }

//   /** Clones the message. */
// AF: No longer needed, just inherit from super class.
//   public Object clone() {
//     if (JoramTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
//       JoramTracing.dbgMessage.log(BasicLevel.DEBUG,
//                                     "Message.clone()");
//     Message clone = (Message) super.clone();
//     return clone;
//   }

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

//   /* ***** ***** ***** ***** *****
//    * Streamable interface
//    * ***** ***** ***** ***** ***** */

//   /**
//    *  The object implements the writeTo method to write its contents to
//    * the output stream.
//    *
//    * @param os the stream to write the object to
//    */
//   public void writeTo(OutputStream os) throws IOException {
//     StreamUtil.writeTo(type, os);
//     super.writeTo(os);
//   }

//   /**
//    *  The object implements the readFrom method to restore its contents from
//    * the input stream.
//    *
//    * @param is the stream to read data from in order to restore the object
//    */
//   public void readMessage(InputStream is) throws IOException {
//     int type = StreamUtil.readIntFrom(is);
//     super.readFrom(is);
//   }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Serializable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeLong(order);
//     out.writeBoolean(denied);
//     out.writeInt(acksCounter);
//     out.writeInt(durableAcksCounter);
    msg.writeTo(out);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    order = in.readLong();
//     denied = in.readBoolean();
//     acksCounter = in.readInt();
    acksCounter = 0;
//     durableAcksCounter = in.readInt();
    durableAcksCounter = 0;

    msg = new org.objectweb.joram.shared.messages.Message();
    msg.readFrom(in);
  }
}
