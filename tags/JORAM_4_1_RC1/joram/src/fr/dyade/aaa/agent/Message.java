/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies 
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
 * 
 */
final class Message implements Serializable {
  static final long serialVersionUID =  -2179939607085028300L;

  //  Declares all fields transient in order to avoid useless
  // description of each during serialization.
  /** <code>AgentId</code> of sender. */
  transient AgentId from;
  /** <code>AgentId</code> of destination agent. */
  transient AgentId to;
  /** The notification. */
  transient Notification not;
  /** The logical date of sending specified by the update of matrix clock. */
  private transient Update update;
 
  short getFromId() {
    return update.getFromId();
  }
 
  short getToId() {
    return update.getToId();
  }

  int getStamp() {
    return update.stamp;
  }

  void setUpdate(Update update) {
    if (this.update != null) this.update.free();
    this.update = update;
    stringId = null;
  }

  Update getUpdate() {
    return update;
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
    strbuf.append(",update=");
    Update current = update;
    while (current != null) {
      current.appendToString(strbuf).append(',');
      current = current.next;
    }
    strbuf.append(')');
    
    return strbuf;
  }

  /**
   *  The writeObject method is responsible for writing the state of the
   * object for its particular class so that the corresponding readObject
   * method can restore it. 
   */
  private void writeObject(java.io.ObjectOutputStream out)
       throws IOException {
    // Writes from AgentId
    out.writeShort(from.from);
    out.writeShort(from.to);
    out.writeInt(from.stamp);
    // Writes to AgentId
    out.writeShort(to.from);
    out.writeShort(to.to);
    out.writeInt(to.stamp);
    out.writeObject(not);
    // In order to optimize the serialization, we serialize each update...
    Update next = update;
    while (next != null) {
      out.writeShort(next.l);
      out.writeShort(next.c);
      out.writeInt(next.stamp);
      next = next.next;
    }
    out.writeShort(-1);
  }
    
  /**
   *  The readObject method is responsible for reading from the stream and
   * restoring the classes fields.
   */
  private void readObject(java.io.ObjectInputStream in)
       throws IOException, ClassNotFoundException {
    // Reads from AgentId
    from = new AgentId(in.readShort(), in.readShort(), in.readInt());
    // Reads to AgentId
    to = new AgentId(in.readShort(), in.readShort(), in.readInt());
    not = (Notification) in.readObject();
    // Gets clock update
    short l;
    while ((l = in.readShort()) != -1) {
      if (update == null)
	update = Update.alloc(l, in.readShort(), in.readInt());
      else
	Update.alloc(l, in.readShort(), in.readInt(), update);
    }
  }

  private final static int BUFLEN = 20;

  // Per-thread buffer for string/stringbuffer conversion
  private static ThreadLocal perThreadBuffer = new ThreadLocal() {
    protected synchronized Object initialValue() {
      return new char[BUFLEN];
    }
  };

  transient private String stringId = null;

  final String toStringId() {
    if ((stringId == null) && (update != null)) {
      char[] buf = (char[]) (perThreadBuffer.get());
      int idx = getChars(update.stamp, buf, BUFLEN);
      buf[--idx] = '_';
      idx = getChars(update.c, buf, idx);
      buf[--idx] = '@';
      stringId = new String(buf, idx, BUFLEN - idx);
    }
    return stringId;
  }

  final static char [] DigitTens = {
    '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
    '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
    '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
    '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
    '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
    '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
    '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
    '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
    '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
    '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
  } ; 

  final static char [] DigitOnes = { 
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  } ;

  private final static int getChars(int i, char[] buf, int idx) {
    int q, r;
    int charPos = idx;
    char sign = 0;

    if (i < 0) { 
      sign = '-';
      i = -i;
    }

    // Generate two digits per iteration
    while (i >= 65536) {
      q = i / 100;
      // really: r = i - (q * 100);
      r = i - ((q << 6) + (q << 5) + (q << 2));
      i = q;
      buf [--charPos] = DigitOnes[r];
      buf [--charPos] = DigitTens[r];
    }

    // Fall thru to fast mode for smaller numbers
    // assert(i <= 65536, i);
    for (;;) { 
      q = (i * 52429) >>> (16+3);
      r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
      buf [--charPos] = DigitOnes[r];
      i = q;
      if (i == 0) break;
    }
    if (sign != 0) {
      buf [--charPos] = sign;
    }
    return charPos;
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
      AgentServer.transaction.save(this, toStringId());
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
    return (Message) AgentServer.transaction.load(name);
  }

  /**
   * Deletes the current object in persistent storage.
   */
  void delete()  throws IOException {
    if (isPersistent()) {
      AgentServer.transaction.delete(toStringId());
    }
  }

  /**
   * Construct a new message.
   * @param from	id of source Agent.
   * @param to    	id of destination Agent.
   * @param not    	Notification to be signaled.
   */
  private Message(AgentId from, AgentId to, Notification not) {
    this.from = from;
    this.to = to;
    if (not != null) this.not = (Notification) not.clone();
  }

  private static Pool pool = null;

  static {
    int size = Integer.getInteger("fr.dyade.aaa.agent.Message$Pool.size", 150).intValue();
    pool = new Pool("Message", size);
  }

  static Message alloc(AgentId from, AgentId to, Notification not) {
    Message msg = null;
    
    try {
      msg = (Message) pool.allocElement();
    } catch (Exception exc) {
      return new Message(from, to, not);
    }
    msg.set(from, to, not);
    return msg;
  }

  void free() {
    not = null;	/* to let gc do its work */
    if (update != null) update.free();
    update = null;
    pool.freeElement(this);
  }
  
  private void set(AgentId from, AgentId to, Notification not) {
    this.from = (AgentId) from;
    this.to = (AgentId) to;
    if (not != null) this.not = (Notification) not.clone();
    stringId = null;
  }
}
