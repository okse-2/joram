/*
 * Copyright (C) 2002-2003 SCALAGENT
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
class Message implements Serializable {
  public static final String RCS_VERSION="@(#)$Id: Message.java,v 1.14 2003-06-23 13:37:51 fmaistre Exp $"; 

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
  transient Update update;
 
  /**
   * The deadline of current message specified in number of milliseconds
   * since the standard base time known as "the epoch".
   */
//   transient long deadline;

  /**
   * Returns a string representation for this object.
   *
   * @return	A string representation of this object. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append("(from=");
    strbuf.append(from).append(",to=");
    strbuf.append(to).append(",not=[");
    strbuf.append(not).append("],update=[");
    strbuf.append(update).append(']');
//     if (deadline != -1L) {
//       strbuf.append(",deadline=").append(new Date(deadline));
//     }
    strbuf.append(')');
    
    return strbuf.toString();
  }

  /**
   *  The writeObject method is responsible for writing the state of the
   * object for its particular class so that the corresponding readObject
   * method can restore it. 
   */
  private void writeObject(java.io.ObjectOutputStream out)
       throws IOException {
    out.writeShort(from.from);
    out.writeShort(from.to);
    out.writeInt(from.stamp);
    out.writeShort(to.from);
    out.writeShort(to.to);
    out.writeInt(to.stamp);
//     out.writeLong(deadline);
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
    from = new AgentId(in.readShort(), in.readShort(), in.readInt());
    to = new AgentId(in.readShort(), in.readShort(), in.readInt());
//     deadline = in.readLong();
    not = (Notification) in.readObject();
    short l;
    while ((l = in.readShort()) != -1) {
      if (update == null)
	update = new Update(l, in.readShort(), in.readInt());
      else
	new Update(l, in.readShort(), in.readInt(), update);
    }
  }

  /**
   *  Saves the object state on persistent storage.
   */
  void save() throws IOException {
    AgentServer.transaction.save(this,
			    "@" +
			    update.l + Transaction.separator +
			    update.c + Transaction.separator +
			    update.stamp);
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
    AgentServer.transaction.delete("@" +
			      update.l + Transaction.separator +
			      update.c + Transaction.separator +
			      update.stamp);
  }

  /**
   * Private constructor used in PoolCnxNetwork to create a ghost message
   * in order to force an acknowledge transmission.
   */
  Message(AgentId from, AgentId to) {
    this.from = from;
    this.to = to;
    this.not = null;
    this.update = new Update(from.getTo(), to.getTo(), 0);
  }

  /**
   * Construct a new message.
   * @param from	id of source Agent.
   * @param to    	id of destination Agent.
   * @param not    	Notification to be signaled.
   */
  public Message(AgentId from, AgentId to, Notification not) {
    this.from = from;
    this.to = to;
    this.not = (Notification) not.clone();
//     this.deadline = -1L;
  }

  /**
   * Construct a new message with a deadline.
   * @param from	id of source Agent.
   * @param to    	id of destination Agent.
   * @param not    	Notification to be signaled.
   * @param deadline	Deadline.
   */
//   public Message(AgentId from, AgentId to, Notification not, long deadline) {
//     this(from, to, not);
//     this.deadline = deadline;
//   }

  private static Pool pool = null;

  static {
    int size = Integer.getInteger("fr.dyade.aaa.agent.Message$Pool.size", 150).intValue();
    pool = new Pool(size);
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
    this.not = null;	/* to let gc do its work */
    this.update = null; /* to let gc do its work */
    pool.freeElement(this);
  }

  private Message() {}
  
  private void set(AgentId from, AgentId to, Notification not) {
    this.from = (AgentId) from;
    this.to = (AgentId) to;
    // Be careful, normally we have to clone the notification !!!
    this.not = (Notification) not.clone();
//     this.deadline = -1L;
  }

  static class Pool {
    int elementCount = 0;
    Object[] elementData =  null;

    private Logger logmon = null;
    private long cpt1, cpt2, alloc, free, min, max;

    public Pool(int capacity) {
      elementData = new Object[capacity];
      logmon = Debug.getLogger(getClass().getName());
      logmon.log(BasicLevel.DEBUG, "Message$Pool: " + capacity);
    }

    public final synchronized void freeElement(Object obj) {
      // If there is enough free element, let the gc get this element. 
      if (elementCount == elementData.length) {
        free += 1;
        return;
      }
      elementData[elementCount] = obj;
      elementCount += 1;

      if (elementCount > max) max = elementCount;
    }

    public final synchronized Object allocElement() throws Exception {
      if (elementCount == 0) {
        alloc += 1;
        throw new Exception();
      }
      elementCount -= 1;
      Object obj = elementData[elementCount];
      elementData[elementCount] = null; /* to let gc do its work */

      if (elementCount < min) min = elementCount;
      cpt1 += 1; cpt2 += elementCount;
      if ((cpt1 & 0xFFFFFL) == 0L) {
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG,
                     "Message$Pool=" + (cpt2/cpt1) + '/' + elementCount +
                     ", " + min + '/' + max + ", " + alloc + ", " + free);
          alloc = 0; free = 0; min = elementData.length; max = 0;
        }
      }
    
      return obj;
    }
  }
}
