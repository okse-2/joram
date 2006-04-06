/*
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies 
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.util.Date;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

/**
 * The internal message structure.
 * A message is divided in 2 parts:<ul>
 * <li>An immutable part with source and destination agent unique id, and
 * notification object.
 * <li>A variable part containing information about message routing (next hop)
 * and the current stamp of the message.
 * </ul>
 */
final class Message implements Serializable {
  static final long serialVersionUID =  -2179939607085028300L;

  /** <code>AgentId</code> of sender. */
  transient AgentId from;
  /** <code>AgentId</code> of destination agent. */
  transient AgentId to;
  /** The notification. */
  transient Notification not;

  /** The unique id. of source server */
  transient short source;
  /** The unique id. of destination server*/
  transient short dest;
  /** The current stamp of the message */
  transient int stamp;

  /** Get the unique server id. of the sender of this message */
  short getSource() {
    return source;
  }
 
  /** Get the unique server id. of the addressee of this message */
  short getDest() {
    return dest;
  }

  /** Get the stamp of this message */
  int getStamp() {
    return stamp;
  }

  /**
   * Returns a string representation for this object.
   *
   * @return	A string representation of this object. 
   */
  public String toString() {
    return appendToString(new StringBuffer()).toString();
  }

  /**
   *  Adds a string representation for this object in the
   * StringBuffer parameter.
   *
   * @return	A string representation of this object. 
   */
  public StringBuffer appendToString(StringBuffer strbuf) {
    strbuf.append('(').append(super.toString());
    strbuf.append(",from=").append(from);
    strbuf.append(",to=").append(to);
    strbuf.append(",not=").append(not);
    strbuf.append(",source=").append(source);
    strbuf.append(",dest=").append(dest);
    strbuf.append(",stamp=").append(stamp);
    strbuf.append(')');
    
    return strbuf;
  }

  final static int LENGTH = 25;

  final static int PERSISTENT = 0x01;
  final static int DETACHABLE = 0x10;

  transient private byte iobuf[] = new byte [LENGTH];

  /**
   *  The writeObject method is responsible for writing the state of the
   * object for its particular class so that the corresponding readObject
   * method can restore it.
   *  Be careful this method should only be used for saving messages in
   * persistent storage, sending messages will be done by another way.
   */
  private void writeObject(java.io.ObjectOutputStream out)
       throws IOException {
    int idx = writeToBuf(iobuf, 0);

    // Writes notification attributes
    iobuf[idx++] = (byte) ((not.persistent?PERSISTENT:0) |
                           (not.detachable?DETACHABLE:0));

    // Writes data on stream
    out.write(iobuf, 0, LENGTH);
    
    if (! not.detachable) {
      // Writes notification object
      out.writeObject(not);
    }
  }

  /**
   *  Write the Message internal state to the buffer.
   */
  int writeToBuf(byte[] buf, int idx) {
      // Writes sender's AgentId
    buf[idx++] = (byte) (from.from >>>  8);
    buf[idx++] = (byte) (from.from >>>  0);
    buf[idx++] = (byte) (from.to >>>  8);
    buf[idx++] = (byte) (from.to >>>  0);
    buf[idx++] = (byte) (from.stamp >>>  24);
    buf[idx++] = (byte) (from.stamp >>>  16);
    buf[idx++] = (byte) (from.stamp >>>  8);
    buf[idx++] = (byte) (from.stamp >>>  0);
    // Writes adressee's AgentId
    buf[idx++]  = (byte) (to.from >>>  8);
    buf[idx++]  = (byte) (to.from >>>  0);
    buf[idx++] = (byte) (to.to >>>  8);
    buf[idx++] = (byte) (to.to >>>  0);
    buf[idx++] = (byte) (to.stamp >>>  24);
    buf[idx++] = (byte) (to.stamp >>>  16);
    buf[idx++] = (byte) (to.stamp >>>  8);
    buf[idx++] = (byte) (to.stamp >>>  0);
    // Writes source server id of message
    buf[idx++]  = (byte) (source >>>  8);
    buf[idx++]  = (byte) (source >>>  0);
    // Writes destination server id of message
    buf[idx++] = (byte) (dest >>>  8);
    buf[idx++] = (byte) (dest >>>  0);
    // Writes stamp of message
    buf[idx++] = (byte) (stamp >>>  24);
    buf[idx++] = (byte) (stamp >>>  16);
    buf[idx++] = (byte) (stamp >>>  8);
    buf[idx++] = (byte) (stamp >>>  0);

    return idx;
  }

  /**
   *  The readObject method is responsible for reading from the stream and
   * restoring the classes fields.
   *  Be careful this method should only be used for restoring messages from
   * persistent storage, receiving messages will be done by another way.
   */
  private void readObject(java.io.ObjectInputStream in)
       throws IOException, ClassNotFoundException {
    iobuf = new byte[25];
    in.readFully(iobuf, 0, 25);

    int idx = readFromBuf(iobuf, 0);

    // Reads notification attributes
    boolean persistent = ((iobuf[idx] & PERSISTENT) == 0)?false:true;
    boolean detachable = ((iobuf[idx] & DETACHABLE) == 0)?false:true;

    if (! detachable) {
      // Reads notification object
      not = (Notification) in.readObject();
      not.detachable = false;
      not.detached = false;
    }
  }

  int readFromBuf(byte[] buf, int idx) {
    // Reads sender's AgentId
    from = new AgentId(
      (short) (((buf[idx++] & 0xFF) <<  8) + (buf[idx++] & 0xFF)),
      (short) (((buf[idx++] & 0xFF) <<  8) + (buf[idx++] & 0xFF)),
      ((buf[idx++] & 0xFF) << 24) + ((buf[idx++] & 0xFF) << 16) +
      ((buf[idx++] & 0xFF) <<  8) + ((buf[idx++] & 0xFF) <<  0));
    // Reads adressee's AgentId
    to = new AgentId(
      (short) (((buf[idx++] & 0xFF) <<  8) + (buf[idx++] & 0xFF)),
      (short) (((buf[idx++] & 0xFF) <<  8) + (buf[idx++] & 0xFF)),
      ((buf[idx++] & 0xFF) << 24) + ((buf[idx++] & 0xFF) << 16) +
      ((buf[idx++] & 0xFF) <<  8) + ((buf[idx++] & 0xFF) <<  0));
    // Reads source server id of message
    source = (short) (((buf[idx++] & 0xFF) <<  8) + 
                      ((buf[idx++] & 0xFF) <<  0));
    // Reads destination server id of message
    dest = (short) (((buf[idx++] & 0xFF) <<  8) +
                    ((buf[idx++] & 0xFF) <<  0));
    // Reads stamp of message
    stamp = ((buf[idx++] & 0xFF) << 24) + ((buf[idx++] & 0xFF) << 16) +
      ((buf[idx++] & 0xFF) <<  8) + ((buf[idx++] & 0xFF) <<  0);

    return idx;
  }

  transient private String stringId = null;

  final String toStringId() {
    if (stringId == null) {
      stringId = StringId.toStringId('@', '_', dest, stamp, -1);
    }
    return stringId;
  }


  /**
   * Tests if the associated notification is persistent or not.
   *
   * @return true if the associated notification is persistent.
   */
  boolean isPersistent() {
    return ((not != null) && not.persistent);
  }

  /**
   *  Saves the object state on persistent storage.
   */
  void save() throws IOException {
    if (isPersistent()) {
      AgentServer.getTransaction().save(this, toStringId());
      if (not.detachable) {
        not.messageId = StringId.toStringId('N', '_', dest, stamp, -1);
        AgentServer.getTransaction().save(not, not.messageId);
      }
    }
  }

  /**
   * Restores the object state from the persistent storage.
   *
   * @exception IOException
   *	when accessing the stored image
   * @exception ClassNotFoundException
   *	if the stored image class may not be found
   */
  static Message
  load(String name) throws IOException, ClassNotFoundException {
    Message msg = (Message) AgentServer.getTransaction().load(name);
    if (msg.not == null) {
      String messageId = StringId.toStringId('N', '_', msg.dest, msg.stamp, -1);
      msg.not = (Notification) AgentServer.getTransaction().load(messageId);
      msg.not.messageId = messageId;
      msg.not.detachable = true;
      msg.not.detached = false;
    }
    msg.not.persistent = true;

    return msg;
  }

  /**
   * Deletes the current object in persistent storage.
   */
  void delete()  throws IOException {
    if (isPersistent()) {
      AgentServer.getTransaction().delete(toStringId());
      if (not.detachable && ! not.detached) {
        // The Notification is not stored with message, and it is not detached
        // so it may be deleted individually.
        AgentServer.getTransaction().delete(not.getMessageId());
      }
    }
  }

  /**
   * Construct a new message.
   */
  private Message() {}

  private static Pool pool = null;

  static {
    int size = Integer.getInteger("fr.dyade.aaa.agent.Message$Pool.size", 150).intValue();
    pool = new Pool("Message", size);
  }

  /**
   * Allocates a message from the pool.
   */
  static Message alloc() {
    Message msg = null;
    
    try {
      msg = (Message) pool.allocElement();
    } catch (Exception exc) {
      return new Message();
    }
    return msg;
  }

  /**
   * Allocates a message from the pool.
   *
   * @param from	id of source Agent.
   * @param to    	id of destination Agent.
   * @param not    	Notification to be signaled.
   */
  static Message alloc(AgentId from, AgentId to, Notification not) {
    Message msg = alloc();
    msg.set(from, to, not);
    return msg;
  }

  /**
   * Frees the message to the pool.
   */
  void free() {
    not = null;	/* to let gc do its work */
    stringId = null;
    pool.freeElement(this);
  }
  
  private void set(AgentId from, AgentId to, Notification not) {
    this.from = (AgentId) from;
    this.to = (AgentId) to;
    if (not != null) {
      this.not = (Notification) not.clone();
      this.not.detached = not.detached;
      this.not.messageId = not.messageId;
    }
  }
}
