/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.util.Date;
import fr.dyade.aaa.util.*;

/**
 * 
 */
class Message implements Serializable {
  public static final String RCS_VERSION="@(#)$Id: Message.java,v 1.7 2002-01-16 12:46:47 joram Exp $"; 

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
  transient long deadline;

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
    if (deadline != -1L) {
      strbuf.append(",deadline=").append(new Date(deadline));
    }
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
    out.writeLong(deadline);
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
    deadline = in.readLong();
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
   * Construct a new message.
   * @param from	id of source Agent.
   * @param to    	id of destination Agent.
   * @param not    	Notification to be signaled.
   */
  public Message(AgentId from, AgentId to, Notification not) {
    if (from != null)
      this.from = (AgentId) from.clone();
    this.to = (AgentId) to.clone();
    this.not = (Notification) not.clone();
    this.deadline = -1L;
  }

  /**
   * Construct a new message with a deadline.
   * @param from	id of source Agent.
   * @param to    	id of destination Agent.
   * @param not    	Notification to be signaled.
   * @param deadline	Deadline.
   */
  public Message(AgentId from, AgentId to, Notification not, long deadline) {
    this(from, to, not);
    this.deadline = deadline;
  }

}
