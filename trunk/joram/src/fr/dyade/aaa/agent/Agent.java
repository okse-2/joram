/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.management.MXWrapper;

/**
 * The <code>Agent</code> class represents the basic component in our model.
 * <i>agents</i> are "reactive" objects which behave according to 
 * "event -> reaction"model: an event embodies a significant state change
 * which one or many agents may react to.<p>
 * Class <code>Agent</code> defines the generic interface and the common
 * behavior for all agents; every agent is an object of a class deriving
 * from class Agent. Agents are the elementary programming and execution
 * entities; they only communicate using notifications through the message
 * bus, and are controlled by the execution engine.<p>
 * The reactive behavior is implemented by function member React, which
 * defines the reaction of the agent when receiving a notification; this
 * function member is called by the execution engine.<p>
 * Agents are persistent objects, and the Agent class realizes a
 * "swap-in/swap-out" mechanism which allows loading (or finding) in main
 * memory the agents to activate, and unloading the agents idle since a while.
 * <p><hr>
 * Agents must be created in two steps: 
 * <ul>
 * <li>locally creating the object in memory (via constructor),
 * <li>configure it (for example via get/set methods),
 * <li>the deploy it .
 * </ul>
 * <p>
 * The following code would then create a simple agent and deploy it:
 * <p><blockquote><pre>
 *     Agent ag = new Agent();
 *     ag.deploy();
 * </pre></blockquote>
 * <p>
 * 
 * @see Notification
 * @see Engine
 * @see Channel
 */
public abstract class Agent implements AgentMBean, Serializable {
  /** Define serialVersionUID for interoperability. */
  static final long serialVersionUID = 1L;

  /**
   * <code>true</code> if the agent state has changed.
   * <p>
   * This field value is initialized as <code>true</code>, so that by default
   * the agent state is saved after a reaction.
   */
  private transient boolean updated = true;

  /**
   * Sets the <code>updated</code> field to <code>false</code> so that the
   * agent state is not saved after the current reaction; the field is set
   * back to <code>true</code> for the next reaction.
   */
  protected void setNoSave() {
    updated = false;
  }

  /**
   * Sets the <code>updated</code> field to <code>true</code> so that the
   * agent state is saved after the current reaction.
   */
  protected void setSave() {
    updated = true;
  }

  /**
   * Indicates to the Engine component that a commit is needed.
   */
  protected final boolean needToBeCommited() {
    try {
      ((EngineThread) Thread.currentThread()).engine.needToBeCommited = true;
      return true;
    } catch (ClassCastException exc) {
      return false;
    }
  }

  /**
   * Saves the agent state unless not requested.
   */
  protected final void save() throws IOException {
    if (updated) {
      AgentServer.getTransaction().save(this, id.toString());
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "Agent" + id + " [" + name + "] saved");
    } else {
      updated = true;
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "Agent" + id + " [" + name + "] not saved");
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
  final static Agent
  load(AgentId id) throws IOException, ClassNotFoundException {
    Agent ag = (Agent) AgentServer.getTransaction().load(id.toString());
    if (ag != null) {
      ag.id = id;
      ag.deployed = true;
    }
    return ag;
  }

  //  Declares all fields transient in order to avoid useless
  // description of each during serialization.

  /**
   * Global unique identifier of the agent. Each agent is identified by a
   * unique identifier allowing the agent to be found. The identifiers format
   * is detailed in <a href="AgentId.html">AgentId</a> class.
   */
  transient AgentId id;
  /** Symbolic name of the agent */
  public transient String name;

  /**
   * Returns this <code>Agent</code>'s name.
   *
   * @return this <code>Agent</code>'s name.
   */
  public String getName() {
    if ((name == null) || (name == nullName)) {
      return getClass().getName() + id.toString();
    } else {
      return name;
    }
  }

  /**
   * Sets this <code>Agent</code>'s name.
   *
   * @param name	the <code>Agent</code>'s name.
   */
  public void setName(String name) {
    if (name == null)
      this.name = nullName;
    else
      this.name = name;
  }

  /**
   * Some agents must be loaded at any time, this can be enforced by this
   * member variable. If <code>true</code> agent is pinned in memory.
   */
  protected transient boolean fixed;
  
  protected transient Logger logmon = null;

  /**
   * Returns default log topic for agents. Its method should be overridden
   * in subclass in order to permit fine configuration of logging system.
   * By default it returns <code>Debug.A3Agent</code>.
   */
  protected String getLogTopic() {
    return fr.dyade.aaa.agent.Debug.A3Agent;
  }

  public static final String nullName = "";

  /**
   *  the <code>last</code> variable contains the virtual time of the
   * last access. It is used by swap-out policy.
   *
   * @see garbage
   */
  transient long last;

  private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
      out.writeUTF(name);
      out.writeBoolean(fixed);
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
      if ((name = in.readUTF()).equals(nullName))
	name = nullName;
      fixed = in.readBoolean();
      updated = true;
  }

  /**
   * Allocates a new Agent object. The resulting object <b>is not an agent</b>;
   * before it can react to a notification you must deploy it. This constructor
   * has the same effect as
   * <code>Agent(AgentServer.getServerId(), null, false)</code>.
   *
   * @see Agent#Agent(short, java.lang.String, boolean)
   * @see #deploy()
   */
  public Agent() {
    this(null, false);
  }

  /**
   * Allocates a new Agent object. This constructor has the same effect
   * as <code>Agent(AgentServer.getServerId(), null, fixed)</code>.
   * 
   * @param fixed if <code>true</code> agent is pinned in memory
   *
   * @see Agent#Agent(short, String, boolean)
   */
  public Agent(boolean fixed) {
    this(null, fixed);
  }

  /**
   * Allocates a new Agent object. This constructor has the same effect
   * as <code>Agent(AgentServer.getServerId(), name, false)</code>.
   * 
   * @param name  symbolic name
   *
   * @see Agent#Agent(short, java.lang.String, boolean)
   */
  public Agent(String name) {
    this(name, false);
  }

  /**
   * Allocates a new Agent object. This constructor has the same effect
   * as <code>Agent(AgentServer.getServerId(), name, fixed)</code>.
   * 
   * @param name  symbolic name
   * @param fixed if <code>true</code> agent is pinned in memory
   *
   * @see Agent#Agent(short, java.lang.String, boolean)
   */
  public Agent(String name, boolean fixed) {
    this(AgentServer.getServerId(), name, fixed);
  }

  /**
   * Allocates a new Agent object. This constructor has the same effect
   * as <code>Agent(to, null, false)</code>.
   *
   * @param to	  Identication of target agent server
   *
   * @see Agent#Agent(short, java.lang.String, boolean)
   */
  public Agent(short to) {
    this(to, null, false);
  }

  /**
   * Allocates a new Agent object. This constructor has the same effect
   * as <code>Agent(to, name, false)</code>.
   *
   * @param to	  Identication of target agent server
   * @param name  symbolic name
   *
   * @see Agent#Agent(short, java.lang.String, boolean)
   */
  public Agent(short to, String name) {
    this(to, name, false);
  }

  /**
   * Allocates a new Agent object. This constructor has the same effect
   * as <code>Agent(to, null, fixed)</code>.
   *
   * @param to	  Identication of target agent server
   * @param fixed if <code>true</code> agent is pinned in memory
   *
   * @see Agent#Agent(short, java.lang.String, boolean)
   */
  public Agent(short to, boolean fixed) {
    this(to, null, fixed);
  }

  /**
   * Allocates a new Agent object. The resulting object <b>is not an agent</b>;
   * before it can react to a notification you must deploy it.
   *
   * @param to	  Identication of target agent server
   * @param name  symbolic name
   * @param fixed if <code>true</code> agent is pinned in memory
   *
   * @see #deploy()
   */
  public Agent(short to, String name, boolean fixed) {
    AgentId id = null;

    try {
      id = new AgentId(to);
    } catch (IOException exc) {
      logmon = Debug.getLogger(fr.dyade.aaa.agent.Debug.A3Agent +
                               ".#" + AgentServer.getServerId());
      logmon.log(BasicLevel.ERROR,
                 AgentServer.getName() + ", can't allocate new AgentId", exc);
      // TODO: throw an exception...
    }
    initState(name, fixed, id);
  }

  /**
   * Constructor used to build "system" agents like <code>AgentFactory</code>.
   * System agents are created from the <code>agent</code> package. This
   * constructor takes the agent id as a parameter instead of building it.
   *
   * @param name  symbolic name
   * @param fixed if <code>true</code> agent is pinned in memory
   * @param stamp well known stamp
   */
  Agent(String name, boolean fixed, AgentId id) {
    initState(name, fixed, id);
  }

  private void initState(String name, boolean fixed, AgentId id) {
    if (name == null)
      this.name = nullName;
    else
      this.name = name;
    this.fixed = fixed;
    this.id = id;
    // Get the logging monitor from current server MonologLoggerFactory
    this.logmon = Debug.getLogger(getLogTopic());
  }

  /**
   * Constructor used to build Well Known Services agents.
   * <p>
   * System agents are created from the <code>agent</code> package.
   * WKS agents are similar to system agents, except that they may be
   * defined in separate packages, and they do not necessarily exist on all
   * agent servers. Their creation is controlled from the configuration file
   * of the agent server.<p>
   * This constructor takes the agent id as a parameter instead of building it.
   * Since the constructor has been made public, the consistency of agent ids
   * allocation must be enforced. This is done by the constructor checking
   * that the id stamp is comprised in the <code>AgentId.MinWKSIdStamp</code>
   * - <code>AgentId.MaxWKSIdStamp</code> interval.
   *
   * @param name	symbolic name
   * @param fixed	if <code>true</code> agent is pinned in memory
   * @param stamp	well known stamp
   */
  public Agent(String name, boolean fixed, int stamp) {
    if (stamp < AgentId.MinWKSIdStamp || stamp > AgentId.MaxWKSIdStamp) {
      logmon = Debug.getLogger(fr.dyade.aaa.agent.Debug.A3Agent + ".#" + AgentServer.getServerId());
      logmon.log(BasicLevel.ERROR,
                 AgentServer.getName() + ", well known service stamp out of range: " + stamp);
      throw new IllegalArgumentException("Well known service stamp out of range: " + stamp);
    }
    AgentId id = new AgentId(AgentServer.getServerId(),
                             AgentServer.getServerId(),
                             stamp);
    initState(name, fixed, id);
  }

  /**
   * Determines if the current <code>Agent</code> has already been deployed.
   */
  transient boolean deployed = false;

  /**
   * Returns if the currently <code>Agent</code> has already been deployed.
   */
  public boolean isDeployed() {
    return deployed;
  }

  /**
   * Deploys a new <i>agent</i>.
   * It works by sending a notification to a special agent, of class Factory,
   * running on the target agent server. The notification asks for a remote
   * creation of the agent. This solution presents the advantage of reusing
   * the standard communication mechanisms of the agent machine.<p>
   * The whole process involves then the following steps:
   * <ul>
   * <li>serializing the object state,
   * <li>building an <code>AgentCreateRequest</code> notification with the
   *     resulting bytes stream,
   * <li>sending it to the target Factory agent.
   * </ul>
   * In reaction, the factory agent builds the agent in the target server
   * from the serialized image, and saves it into operational storage.
   *
   * @exception IOException
   *	unspecialized exception
   */
  public final void deploy() throws IOException {
    deploy(null);
  }

  /**
   * Deploys a new <i>agent</i>.
   * It works as <a href="#deploy()">deploy()</a> method above; after the
   * agent creation, the Factory agent sends an <code>AgentCreateReply</code>
   * notification.
   *
   * @param reply	agent to reply to
   * @exception IOException
   *	unspecialized exception
   */
  public final void deploy(AgentId reply) throws IOException {
    if ((id == null) || id.isNullId()) {
      logmon.log(BasicLevel.ERROR,
                 AgentServer.getName() + ", can't deploy " + this.toString() + ", id is null");
      throw new IOException("Can't deploy agent, id is null");
    }
    if (deployed) {
      logmon.log(BasicLevel.ERROR,
                 AgentServer.getName() + ", can't deploy " + this.toString() + ", already deployed");
      throw new IOException("Can't deploy agent, already deployed");
    }

    //  If we use sendTo agent's method the from field is the agent id, and
    // on reception the from node (from.to) can be false.
    Channel.sendTo(AgentId.factoryId(id.getTo()),
                   new AgentCreateRequest(this, reply));
    deployed = true;

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, this.toString() + " deployed");
  }

  /**
   * Returns a string representation of this agent, including the agent's
   * class, name, global identication, and fixed property.
   *
   * @return	A string representation of this agent. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append('(').append(super.toString());
    strbuf.append(",name=").append(name);
    strbuf.append(",id=").append(id.toString());
    strbuf.append(",fixed=").append(fixed);
    strbuf.append(')');

    return strbuf.toString();
  }

  /**
   * Returns String format of the global unique identifier of the agent.
   *
   * @return the global unique identifier of the agent.
   */
  public final String getAgentId() {
    return id.toString();
  }

  /**
   * Returns the global unique identifier of the agent. Each agent is
   * identified by a unique identifier allowing the agent to be found.
   * The identifiers format is detailed in <a href="AgentId.html">AgentId</a>
   * class.
   *
   * @return the global unique identifier of the agent.
   */
  public final AgentId getId() {
    return id;
  }

  /** 
   * Tests if the agent is pinned in memory.
   *
   * @return true if this agent is a pinned in memory; false otherwise.
   */
  public final  boolean isFixed() {
    return fixed;
  }

  /**
   * Gives this agent an opportunity to initialize after having been deployed,
   * and each time it is loaded into memory.
   * <p>
   * This function is first called by the factory agent, just after it deploys
   * the agent.
   * <p>
   * This function is used by agents with a <code>fixed</code> field set to
   * <code>true</code> to initialize their transient variables, as it is called
   * each time the agent server is restarted.
   * <p>
   * This function is not declared <code>final</code> so that derived classes
   * may change their reload policy. The implementation of this method provided
   * by the <code>Agent</code> class just registers the JMS MBean.
   *
   * @param firstTime		true when first called by the factory
   *
   * @exception Exception
   *	unspecialized exception
   */
  protected void agentInitialize(boolean firstTime) throws Exception {
    // Get the logging monitor from current server MonologLoggerFactory
    this.logmon = Debug.getLogger(getLogTopic());
    // Initializes the updated field to true:
    this.updated = true;

    try {
      MXWrapper.registerMBean(this, "AgentServer", getMBeanName());
    } catch (Exception exc) {
      logmon.log(BasicLevel.WARN, getName() + " jmx failed", exc);
    }

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "Agent" + id + " [" + name +
                 (firstTime?"] , first initialized":"] , initialized"));
  }

  private String getMBeanName() {
    return new StringBuffer()
      .append("server=").append(AgentServer.getName())
      .append(",cons=Engine#").append(getId().getTo())
      .append(",agent=").append((name == nullName)?getId().toString():name)
      .toString();
  }

  /**
   * This method sends a notification to the agent which id is given in
   * parameter. During an agent reaction alls notifications sent are buffered
   * until reaction commit.
   * <p>
   * Be careful if you use this method outside of an agent reaction,
   * its behavior is slightly different: each notification is immediately
   * sent using a local transaction.
   *
   * @see Channel#sendTo
   *
   * @param to   the unique id. of destination <code>Agent</code>.
   * @param not  the notification to send.
   */
  protected final void
  sendTo(AgentId to, Notification not) {
//     try {
//       EngineThread thread = (EngineThread) Thread.currentThread();
//       // Use the engine's sendTo method that push message in temporary queue
//       // until the end of current reaction.
//       thread.engine.push(getId(), to, not);
//     } catch (ClassCastException exc) {
//       //  Be careful, the destination node use the from.to field to
//       // get the from node id.
//       Channel.channel.directSendTo(getId(), to, not);
//     }

//  if (Class.EngineThread.isAssignable(Thread.currentThread())) {
    if (Thread.currentThread() == AgentServer.engine.thread) {
      AgentServer.engine.push(getId(), to, not);
    } else {
      Channel.channel.directSendTo(getId(), to, not);
    }
  }

  /**
   * This method sends a notification to the agent which id is wrapped
   * in the specified role.
   *
   * @param role  the destination <code>Role</code>.
   * @param not   the notification to send.
   */
  protected final void sendTo(Role role, Notification not) {
    if (role == null) return;
    sendTo(role.getListener(), not);
  }
 
  /**
   * Sends a notification to all the agents registered in a role.
   *
   * @param role  the destination <code>MultiplRole</code>.
   * @param not   the notification to send.
   */
  protected final void
  sendTo(RoleMultiple role, Notification not) {
    if (role == null) return;
    Enumeration to = role.getListeners();
    if (to == null)
      return;
    while (to.hasMoreElements())
      sendTo((AgentId) to.nextElement(), not);
  }

  /**
   * Permits this agent to destroy it. If necessary, its method should be 
   * overloaded to work properly.
   */
  public void delete() {
    delete(null);
  }

  /**
   * Permits this agent to destroy it. If necessary, its method should be
   *overloaded to work properly. 
   *
   * @param agent	Id of agent to notify.
   */
  public void delete(AgentId agent) {
    if (deployed)
      sendTo(AgentId.factoryId(id.getTo()),
	     new AgentDeleteRequest(agent));
  }
 
  /**
   * Defines the reaction of the agent when receiving a notification. This
   * member function implements the common reactive behavior of an agent, it
   * is called by the execution engine (see <a href="Engine.html">Engine</a>
   * class).<p>
   * If there is no corresponding reaction, the agent send an
   * <code>UnknownNotification</code> notification to the sender.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof DeleteNot) {
      delete(((DeleteNot) not).reply);
    } else if ((not instanceof UnknownAgent) ||
               (not instanceof UnknownNotification) ||
               (not instanceof ExceptionNotification)) {
      logmon.log(BasicLevel.WARN,
                 this.toString() + ".react(" + from + ", " + not + ")");
     } else {
      logmon.log(BasicLevel.ERROR,
                 this.toString() + ".react(" + from + ", " + not + ")");
      sendTo(from, new UnknownNotification(id, not));
    }
  }

  /**
   * Called to inform this agent that it is garbaged and that it should free
   * any active resources that it has allocated.
   * A subclass of <code>Agent</code> should override this method if it has
   * any operation that it wants to perform before it is garbaged. For example,
   * an agent with threads (a ProxyAgent for example) would use the initialize
   * method to create the threads and the <code>agentFinalize</code> method to
   * stop them.
   * <p>
   * Be careful, the notification sending is not allowed in this method.
   * <p>
   * The implementation of this method provided by the <code>Agent</code> class
   * just unregister the JMX MBean if needed.
   *
   * @param lastTime	true when last called by the factory on agent deletion.
   */
  public void agentFinalize(boolean lastTime) {
    try {
      MXWrapper.unregisterMBean("AgentServer", getMBeanName());
    } catch (Exception exc) {
      logmon.log(BasicLevel.WARN, getName() + " jmx failed", exc);
    }
  }
}
