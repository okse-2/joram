/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
import fr.dyade.aaa.util.*;

/**
 * <code>AgentIdStamp</code> class defines static members, variable and
 * functions, to manage the allocation of new identifiers. It locally
 * maintains a pair of counters, one for local agent server, one for remote
 * agent server, keeping track of the last allocated stamp for the given
 * target domain. Stamps are allocated in growing order and are never reused
 * once allocated, even after agents are deleted.<p><hr>
 * Some initial stamps are reserved for statically identifying system and
 * well known services as factory. They are defined in <code>AgentId</code>
 * class.
 */
final class AgentIdStamp implements Serializable {
  /** Static reference to local <code>AgentIdStamp</code> object. */
  static AgentIdStamp stamp = null;

  /** Stamp counter for local agent server. */
  private int local;
  /** Stamp counter for remote agent server. */
  private int remote;

  /**
   * Initializes <code>AgentIdStamp</code> classe.
   *
   * @exception IOException		IO problem during loading.
   * @exception ClassNotFoundException	should never happened
   */
  static void init()
    throws IOException, ClassNotFoundException {
    stamp = load();
    if (stamp == null) {
      stamp = new AgentIdStamp();
      stamp.save();
    }
  }

  AgentIdStamp() {
    local = AgentId.MaxIdStamp;
    remote = AgentId.MaxIdStamp;
  }

  /**
   *  Saves the object state on persistent storage.
   */
  void save() throws IOException {
    AgentServer.transaction.save(this, "AgentIdStamp");
  }

  /**
   * Restores the object state from the persistent storage.
   */
  static AgentIdStamp
  load() throws IOException, ClassNotFoundException {
    return (AgentIdStamp) AgentServer.transaction.load("AgentIdStamp");
  }

  /**
   * The <code>writeObject</code> method is responsible for writing the
   * state of the object for its particular class so that the corresponding
   * <code>readObject</code> method can restore it.
   *
   * @param out the underlying output stream.
   */
  private void writeObject(java.io.ObjectOutputStream out)
       throws IOException {
    out.writeInt(local);
    out.writeInt(remote);
  }

  /**
   * The <code>readObject</code> is responsible for reading from the stream
   * and restoring the classes fields.
   *
   * @param in	the underlying input stream.
   */
  private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    local = in.readInt();
    remote = in.readInt();
  }

  /**
   * Allocates a new stamp for the target agent server.
   *
   * @param to	The target agent server
   */
  synchronized int newStamp(short to) throws IOException {
    int current = (to == AgentServer.getServerId())?(++local):(++remote);
    save();
    return current;
  }
}

/**
 * An <code>AgentId</code> allows for uniquely identifying and localizing
 * an agent throughout the distributed system. It defines variable members
 * describing the identifier and all needed function members to manipulate
 * the structure (creation, serialization, etc).
 * <p><hr>
 * However before describing the structure of an AgentId we must take into
 * account a second requirement: an agent may be created onto a remote agent
 * server, and the creating entity needs to know the new identifier created
 * for that agent. As agents live in an asynchronous world it is not so easy
 * to get back the identifier from the remote server. We decided instead to
 * make the creating entity  responsible for creating the identifier.<p>
 * The two requirements are then:
 * <ul>
 * <li>static localization of agents to allow the system to forward
 *     notifications ;
 * <li>local generation of identifiers for  remote agents.
 * </ul><hr>
 * The AgentId is then built of three parts:
 * <ul>
 * <li>the identification of the agent server hosting the creating agent
 *     (from field),
 * <li>the identification of the agent server hosting the created agent
 *     (to field),
 * <li>a stamp, local to the agent server hosting the creating agent (stamp
 *     field) ; see <a href="AgentIdStamp.html">AgentIdStamp</a> class.
 * </ul>
 * The three fields form the unique global identifier of the agent, that is
 * two agents may share a common stamp, as long as their from or to fields
 * differ. The to field identifies the agent server hosting the agent, so it
 * is used by the channel to forward notifications to the agent.<p> 
 *
 * @author  Andr� Freyssinet
 *
 * @see AgentIdStamp
 */
public final class AgentId implements Serializable {
  //  Declares all fields transient in order to avoid useless
  // description of each during serialization.

  /** The identification of the agent server hosting the creating agent. */
  transient short from;
  /** The identification of the agent server hosting the created agent. */
  transient short to;
  /** The stamp, local to the agent server hosting the creating agent. */
  transient int stamp;

  /**
   * A temporary string representation of object in order to improve
   * performances.
   */
  transient String str = null;

  /**
   * The <code>writeObject</code> method is responsible for writing the
   * state of the object for its particular class so that the corresponding
   * <code>readObject</code> method can restore it.
   *
   * @param out the underlying output stream.
   */
  private void writeObject(java.io.ObjectOutputStream out)
      throws IOException {
    out.writeShort(from);
    out.writeShort(to);
    out.writeInt(stamp);
  }
 
  /**
   * The <code>readObject</code> is responsible for reading from the stream
   * and restoring the classes fields.
   *
   * @param in	the underlying input stream.
   */
  private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    from = in.readShort();
    to = in.readShort();
    stamp = in.readInt();
  }
 
  // ***** ***** ***** *****
  // Reserved stamps for system services.
  // ***** ***** ***** *****

  /** Reserved stamp for NullId. */
  public static final int NullIdStamp = 0;
  /** Reserved stamp for factory <code>AgentId</code>. */
  public static final int FactoryIdStamp = 1;
  /** Reserved stamp for admin <code>AgentId</code>. */
  public static final int AdminIdStamp = 2;
  /** Maximum reserved stamp for system services. */
  public static final int MaxSystemIdStamp = 2;

  // ***** ***** ***** *****
  // Reserved stamps for well known services.
  // ***** ***** ***** *****

  /** Minimum reserved stamp for well known services. */
  public static int MinWKSIdStamp = MaxSystemIdStamp + 1;
  /** Reserved stamp for name service <code>AgentId</code>. */
  public static int NameServiceStamp = 4;
  /** Reserved stamp for scheduler service <code>AgentId</code>. */
  public static int SchedulerServiceStamp = 5;
  /** Reserved stamp for fileTransfert service <code>AgentId</code>. */
  public static int FileTransfertStamp = 6;
  /** Reserved stamp for JNDI service <code>AgentId</code>. */
  public static int JndiServiceStamp = 7;
  /** Reserved stamp for local JNDI service <code>AgentId</code>. */
  public static int LocalJndiServiceStamp = 8;
  /** Reserved stamp for SCAdmin proxy <code>AgentId</code>. */
  public static int SCAdminProxyStamp = 9;
  /** Reserved stamp for JORAM administration topic <code>AgentId</code>. */
  public static int JoramAdminStamp = 10;
  /** Maximum reserved stamp for well known services. */
  public static int MaxWKSIdStamp = 1024;
  /** Maximum reserved stamp. */
  public static int MaxIdStamp = MaxWKSIdStamp;

  // ***** ***** ***** *****
  // Statically reserved id.
  // ***** ***** ***** *****

  /** null <code>AgentId</code>. */
  public final static AgentId nullId = new AgentId((short) 0,
						   (short) 0,
						   NullIdStamp);
  /**
   * Used by channel to send messages without source agent (from proxy
   * or engine). The from field does not be nullId because the destination
   * node use the from.to field to get the from node id.
   *
   * @see Engine
   * @see Channel#sendTo(AgentId, Notification)
   */
  static AgentId localId;
  /**
   * <code>AgentId</code> for local factory agent.
   * @see AgentFactory
   */
  static AgentId factoryId;
  /**
   * <code>AgentId</code> for local admin agent.
   * @see AgentAdmin
   */
  static AgentId adminId;

  /**
   * Returns the <code>AgentId</code> for a remote factory agent.
   *
   * @param sid	remote server id.
   * @return	the <code>AgentId</code> for a remote factory agent.
   */
  public final static AgentId factoryId(short sid) {
    return new AgentId(sid, sid, FactoryIdStamp);
  }

  public final static AgentId localId(short sid) {
    return new AgentId(sid, sid, NullIdStamp);
  }

  /**
   * Statically initializes <code>AgentId</code> class.
   */
  static void init()
    throws IOException, ClassNotFoundException {
    // Initialize well known ids
    localId = new AgentId(AgentServer.getServerId(),
			  AgentServer.getServerId(),
			  NullIdStamp);
    factoryId = new AgentId(AgentServer.getServerId(),
			    AgentServer.getServerId(),
			    FactoryIdStamp);
    adminId = new AgentId(AgentServer.getServerId(),
			    AgentServer.getServerId(),
			    AdminIdStamp);
    // Initialize stamp values
    AgentIdStamp.init();
  }

  /**
   * Allocates an <code>AgentId</code> object for a new agent hosted by
   * this agent server.
   */ 
  AgentId() throws IOException {
    this(AgentServer.getServerId());
  }

  /**
   * Allocates an <code>AgentId</code> object for a new agent hosted by
   * specified agent server.
   *
   * @param to 	The identification of the agent server hosting the agent.
   */ 
  AgentId(short to) throws IOException {
    this(AgentServer.getServerId(), to, AgentIdStamp.stamp.newStamp(to));
  }

  /**
   * Allocates a new <code>AgentId</code> object. 
   *
   * @param from   The identification of the agent server hosting the
   *		creating agent.
   * @param to 	   The identification of the agent server hosting the agent.
   * @param stamp  The stamp of agent.
   */
  public AgentId(short from, short to, int stamp) {
    this.from = from;
    this.to = to;
    this.stamp = stamp;
  }

  /**
   * 
   */
  public final short getFrom() {
    return from;
  }

  /**
   * 
   */
  public final short getTo() {
    return to;
  }

  /**
   * 
   */
  public final int getStamp() {
    return stamp;
  }

  /**
   * Parses the string argument as an <code>AgentId</code>.
   *
   * @return	The <code>AgentId</code> object represented by the argument.
   */
  public static AgentId fromString(String str) {
    if (str == null) return null;
    if (str.charAt(0) != '#')
      throw new IllegalArgumentException(str + ": bad id");
    short from;
    short to;
    int stamp;
    try {
      String buf = str.substring(1);
      int index = buf.indexOf('.');
      from = Short.parseShort(buf.substring(0, index));
      buf = buf.substring(index+1);
      index = buf.indexOf('.');
      to = Short.parseShort(buf.substring(0, index));
      buf = buf.substring(index+1);
      stamp = Integer.parseInt(buf);
    } catch (Exception exc) {
      throw new IllegalArgumentException(str + ": " + exc);
    }
    return new AgentId(from, to, stamp);
  }

  /**
   * Returns a string representation of this <code>AgentId</code> object.
   *
   * @return	A string representation of this object. 
   */
  public final String toString() {
    if (str == null) 
      str = "#" + from + '.' + to + '.' + stamp;
    return str;
  }

  /**
   * Returns a hashcode for this <code>AgentId</code> object.
   *
   * @return	a hash code value for this object, equal to the primitive
   *	<code>int</code> value represented by the stamp field.
   */
  public int hashCode() {
    return stamp;
  }

  /**
   * 
   * @return	<code>true</code> if this id is equals to NullId;
   *	<code>false</code> otherwise.
   */
  public final boolean isNullId() {
    return (stamp == NullIdStamp);
  }

  /**
   * Indicates whether some other agent id. is "equal to" this one. This method
   * returns <code>true</code> if and only if obj is an <code>AgentId</code>
   * and refer to the same agent (from, to and stamp fields are equals).
   *
   * @param obj	 the reference object with which to compare.
   * @return	 <code>true</code> if this object is the same as the obj
   *		 argument; <code>false</code> otherwise.
   */
   public boolean equals(Object obj) {
    if ((obj instanceof AgentId) &&
	(((AgentId) obj).from == from) &&
	(((AgentId) obj).to == to) &&
	(((AgentId) obj).stamp == stamp)) {
      return true;
    } else {
      return false;
    }
  }
}
