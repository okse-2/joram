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
  /** The boot timestamp of source server */
  transient int boot;

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
    strbuf.append(",boot=").append(boot);
    strbuf.append(')');
    
    return strbuf;
  }

  private byte iobuf[] = new byte [28];

  /**
   *  The writeObject method is responsible for writing the state of the
   * object for its particular class so that the corresponding readObject
   * method can restore it. 
   */
  private void writeObject(java.io.ObjectOutputStream out)
       throws IOException {
    // Sets sender's AgentId
    iobuf[0] = (byte) (from.from >>>  8);
    iobuf[1] = (byte) (from.from >>>  0);
    iobuf[2] = (byte) (from.to >>>  8);
    iobuf[3] = (byte) (from.to >>>  0);
    iobuf[4] = (byte) (from.stamp >>>  24);
    iobuf[5] = (byte) (from.stamp >>>  16);
    iobuf[6] = (byte) (from.stamp >>>  8);
    iobuf[7] = (byte) (from.stamp >>>  0);
    // Sets adressee's AgentId
    iobuf[8]  = (byte) (to.from >>>  8);
    iobuf[9]  = (byte) (to.from >>>  0);
    iobuf[10] = (byte) (to.to >>>  8);
    iobuf[11] = (byte) (to.to >>>  0);
    iobuf[12] = (byte) (to.stamp >>>  24);
    iobuf[13] = (byte) (to.stamp >>>  16);
    iobuf[14] = (byte) (to.stamp >>>  8);
    iobuf[15] = (byte) (to.stamp >>>  0);
    // Sets source server id of message
    iobuf[16]  = (byte) (source >>>  8);
    iobuf[17]  = (byte) (source >>>  0);
    // Sets destination server id of message
    iobuf[18] = (byte) (dest >>>  8);
    iobuf[19] = (byte) (dest >>>  0);
    // Sets stamp of message
    iobuf[20] = (byte) (stamp >>>  24);
    iobuf[21] = (byte) (stamp >>>  16);
    iobuf[22] = (byte) (stamp >>>  8);
    iobuf[23] = (byte) (stamp >>>  0);
//     // Sets boot timestamp of source server
    iobuf[24] = (byte) (boot >>>  24);
    iobuf[25] = (byte) (boot >>>  16);
    iobuf[26] = (byte) (boot >>>  8);
    iobuf[27] = (byte) (boot >>>  0);
    // Writes data on stream
    out.write(iobuf, 0, 28);
    // Writes notification object
    out.writeObject(not);
  }
    
  /**
   *  The readObject method is responsible for reading from the stream and
   * restoring the classes fields.
   */
  private void readObject(java.io.ObjectInputStream in)
       throws IOException, ClassNotFoundException {
    iobuf = new byte[28];

    in.readFully(iobuf, 0, 28);
    // Gets sender's AgentId
    from = new AgentId((short) (((iobuf[0] & 0xFF) <<  8) + (iobuf[1] & 0xFF)),
                       (short) (((iobuf[2] & 0xFF) <<  8) + (iobuf[3] & 0xFF)),
                       ((iobuf[4] & 0xFF) << 24) + ((iobuf[5] & 0xFF) << 16) +
                       ((iobuf[6] & 0xFF) <<  8) + ((iobuf[7] & 0xFF) <<  0));
    // Gets adressee's AgentId
    to = new AgentId((short) (((iobuf[8] & 0xFF) <<  8) + (iobuf[9] & 0xFF)),
                     (short) (((iobuf[10] & 0xFF) <<  8) + (iobuf[11] & 0xFF)),
                     ((iobuf[12] & 0xFF) << 24) + ((iobuf[13] & 0xFF) << 16) +
                     ((iobuf[14] & 0xFF) <<  8) + ((iobuf[15] & 0xFF) <<  0));
    // Gets source server id of message
    source = (short) (((iobuf[16] & 0xFF) <<  8) + ((iobuf[17] & 0xFF) <<  0));
    // Gets destination server id of message
    dest = (short) (((iobuf[18] & 0xFF) <<  8) + ((iobuf[19] & 0xFF) <<  0));
    // Gets stamp of message
    stamp = ((iobuf[20] & 0xFF) << 24) + ((iobuf[21] & 0xFF) << 16) +
      ((iobuf[22] & 0xFF) <<  8) + ((iobuf[23] & 0xFF) <<  0);
    // Gets boot timestamp of source server
    boot = ((iobuf[24] & 0xFF) << 24) + ((iobuf[25] & 0xFF) << 16) +
      ((iobuf[26] & 0xFF) <<  8) + ((iobuf[27] & 0xFF) <<  0);
    // Reads notification object
    not = (Notification) in.readObject();
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
    if (stringId == null) {
      char[] buf = (char[]) (perThreadBuffer.get());
      int idx = getChars(stamp, buf, BUFLEN);
      buf[--idx] = '_';
      idx = getChars(dest, buf, idx);
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

  private Message() {}

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

  static Message alloc() {
    Message msg = null;
    
    try {
      msg = (Message) pool.allocElement();
    } catch (Exception exc) {
      return new Message();
    }
    return msg;
  }

  static Message alloc(AgentId from, AgentId to, Notification not) {
    Message msg = alloc();
    msg.set(from, to, not);
    return msg;
  }

  void free() {
    not = null;	/* to let gc do its work */
    pool.freeElement(this);
  }
  
  private void set(AgentId from, AgentId to, Notification not) {
    this.from = (AgentId) from;
    this.to = (AgentId) to;
    if (not != null) this.not = (Notification) not.clone();
    stringId = null;
  }
}
