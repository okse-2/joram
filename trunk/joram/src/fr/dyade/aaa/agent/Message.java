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
import fr.dyade.aaa.util.*;

public class Message implements Serializable {

public static final String RCS_VERSION="@(#)$Id: Message.java,v 1.2 2000-08-01 09:13:28 tachkeni Exp $"; 

  //  Declares all fields transient in order to avoid useless
  // description of each during serialization.
  transient AgentId from;
  transient AgentId to;
  transient Notification not;
  transient Update update;
 
  /**
   * Returns a string representation for this object.
   *
   * @return	A string representation of this object. 
   */
  public String toString() {
    return "(" + getClass().getName() +
      ",from=" + from + ",to=" + to +
      ",not=" + not + ")";
  }

  private void writeObject(java.io.ObjectOutputStream out)
       throws IOException {
    out.writeShort(from.from);
    out.writeShort(from.to);
    out.writeInt(from.stamp);
    out.writeShort(to.from);
    out.writeShort(to.to);
    out.writeInt(to.stamp);
    out.writeObject(not);
    // TODO: in order to optimize the serialization, we have to
    // serialize each update...
    Update next = update;
    while (next != null) {
      out.writeShort(next.l);
      out.writeShort(next.c);
      out.writeInt(next.stamp);
      next = next.next;
    }
    out.writeShort(-1);
  }
    
  private void readObject(java.io.ObjectInputStream in)
       throws IOException, ClassNotFoundException {
    from = new AgentId(in.readShort(), in.readShort(), in.readInt());
    to = new AgentId(in.readShort(), in.readShort(), in.readInt());
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
    Server.transaction.save(this,
			    "@" +
			    update.l + Server.transaction.separator +
			    update.c + Server.transaction.separator +
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
    return (Message) Server.transaction.load(name);
  }

  /**
   * Deletes the current object in persistent storage.
   */
  void delete()  throws IOException {
    Server.transaction.delete("@" +
			      update.l + Server.transaction.separator +
			      update.c + Server.transaction.separator +
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
  }
}
