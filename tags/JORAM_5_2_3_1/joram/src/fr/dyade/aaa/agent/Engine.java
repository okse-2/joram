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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Queue;

class EngineThread extends Thread {
  Engine engine = null;

  EngineThread(Engine engine) {
    super(AgentServer.getThreadGroup(), engine, engine.getName());
    this.engine = engine;
  }
}

/**
 * The <code>Engine</code> class provides multiprogramming of agents. It
 * realizes the program loop which successively gets the notifications from
 * the message queue and calls the relevant reaction function member of the
 * target agent. The engine's basic behaviour is:
 * <p><blockquote><pre>
 * While (true) {
 *   // get next message in channel
 *   Message msg = qin.get();
 *   // get the agent to process event
 *   Agent agent = load(msg.to);
 *   // execute relevant reaction, all notification sent during this
 *   // reaction is inserted into persistent queue in order to processed
 *   // by the channel.
 *   agent.react(msg.from, msg.not);
 *   // save changes, then commit.
 *   &lt;BEGIN TRANSACTION&gt;
 *   qin.pop();
 *   channel.dispatch();
 *   agent.save();
 *   &lt;COMMIT TRANSACTION&gt;
 * }
 * </pre></blockquote>
 * <p>
 * The <code>Engine</code> class ensures the atomic handling of an agent
 * reacting to a notification:
 * <ul>
 * <li>if the reaction completes, a COMMIT ensures all changes related to
 * the reaction are committed (state change of the agent, notifications
 * signaled during the reaction, deletion of the handled notification);
 * <li>if anything goes wrong during the reaction, a ROLLBACK undoes the
 * changes; depending on the error kind it may be necessary to execute
 * additional operations to resynchronize the database and the memory
 * objects, and to allow the main program to continue.
 * </ul>
 * <p><hr>
 * <b>Handling errors.</b><p>
 * Two types of errors may occur: errors of first type are detected in the
 * source code and signaled by an <code>Exception</code>; serious errors lead
 * to an <code>Error</code> being raised then the engine exits. In the first
 * case the exception may be handled at any level, even partially. Most of
 * them are signaled up to the engine loop. Two cases are then distinguished
 * depending on the recovery policy:<ul>
 * <li>if <code>recoveryPolicy</code> is set to <code>RP_EXC_NOT</code>
 * (default value) then the agent state and the message queue are restored
 * (ROLLBACK); an <code>ExceptionNotification</code> notification is sent
 * to the sender and the engine may then proceed with next notification;
 * <li>if <code>recoveryPolicy</code> is set to <code>RP_EXIT</code> the engine
 * stops the agent server.
 * </ul>
 */
class Engine implements Runnable, MessageConsumer, EngineMBean {
  /**
   * Queue of messages to be delivered to local agents.
   */ 
  protected MessageQueue qin;

  /**
   * Boolean variable used to stop the engine properly. The engine tests
   * this variable between each reaction, and stops if it is false.
   */
  protected volatile boolean isRunning;

  /**
   * Boolean variable used to stop the engine properly. If this variable
   * is true then the engine is waiting and it can interrupted, else it
   * handles a notification and it will exit after (the engine tests the
   * <code><a href="#isRunning">isRunning</a></code> variable between
   * each reaction)
   */
  protected volatile boolean canStop;

  /** Logical timestamp information for messages in "local" domain. */
  private int stamp;

  /** Buffer used to optimize */
  private byte[] stampBuf = null;

  /** True if the timestamp is modified since last save. */
  private boolean modified = false;

  /** This table is used to maintain a list of agents already in memory
   * using the AgentId as primary key.
   */
  Hashtable agents;
  /** Virtual time counter use in FIFO swap-in/swap-out mechanisms. */
  long now = 0;
  /** Maximum number of memory loaded agents. */
  int NbMaxAgents = 100;

  /**
   * Returns the number of agent's reaction since last boot.
   *
   * @return	the number of agent's reaction since last boot
   */
  public long getNbReactions() {
    return now;
  }

  /**
   * Returns the maximum number of agents loaded in memory.
   *
   * @return	the maximum number of agents loaded in memory
   */
  public int getNbMaxAgents() {
    return NbMaxAgents;
  }

  /**
   * Sets the maximum number of agents that can be loaded simultaneously
   * in memory.
   *
   * @parama NbMaxAgents	the maximum number of agents
   */
  public void setNbMaxAgents(int NbMaxAgents) {
    this.NbMaxAgents = NbMaxAgents;
  }

  /**
   * Returns the number of agents actually loaded in memory.
   *
   * @return	the maximum number of agents actually loaded in memory
   */
  public int getNbAgents() {
    return agents.size();
  }

  /**
   * Gets the number of messages posted to this engine since creation.
   *
   *  return	the number of messages.
   */
  public int getNbMessages() {
    return stamp;
  }

  /**
   * Gets the number of waiting messages in this engine.
   *
   *  return	the number of waiting messages.
   */
  public int getNbWaitingMessages() {
    return qin.size();
  }

  /** Vector containing id's of all fixed agents. */
  Vector fixedAgentIdList = null;

  /**
   * Returns the number of fixed agents.
   *
   * @return	the number of fixed agents
   */
  public int getNbFixedAgents() {
    return fixedAgentIdList.size();
  }

  /**
   * The current agent running.
   */ 
  Agent agent = null;

  /**
   * The message in progress.
   */ 
  Message msg = null;

  /**
   * The active component of this engine.
   */ 
  EngineThread thread = null;

  /**
   * Send <code>ExceptionNotification</code> notification in case of exception
   * in agent specific code.
   * Constant value for the <code>recoveryPolicy</code> variable.
   */
  static final int RP_EXC_NOT = 0;
  /**
   * Stop agent server in case of exception in agent specific code.
   * Constant value for the <code>recoveryPolicy</code> variable.
   */
  static final int RP_EXIT = 1;
  /**
   * String representations of <code>RP_*</code> constant values
   * for the <code>recoveryPolicy</code> variable.
   */
  static final String[] rpStrings = {
                                     "notification",
                                     "exit"
  };
  /**
   * recovery policy in case of exception in agent specific code.
   * Default value is <code>RP_EXC_NOT</code>.
   */
  int recoveryPolicy = RP_EXC_NOT;

  private String name;

  /**
   * Returns this <code>Engine</code>'s name.
   *
   * @return this <code>Engine</code>'s name.
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns the corresponding domain's name.
   *
   * @return this domain's name.
   */
  public final String getDomainName() {
    return "engine";
  }

  /**
   * Creates a new instance of Engine (real class depends of server type).
   *
   * @return		the corresponding <code>engine</code>'s instance.
   */
  static Engine newInstance() throws Exception {
    String cname = "fr.dyade.aaa.agent.Engine";
    cname = AgentServer.getProperty("Engine", cname);

    Class eclass = Class.forName(cname);
    return (Engine) eclass.newInstance();
  }

  protected Queue mq;

  /**
   * Push a new message in temporary queue until the end of current reaction.
   * As this method is only  called by engine's thread it does not need to be
   * synchronized.
   */
  final void push(AgentId from,
                  AgentId to,
                  Notification not) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 getName() + ", push(" + from + ", " + to + ", " + not + ")");
    if ((to == null) || to.isNullId())
      return;

    mq.push(Message.alloc(from, to, not));
  }

  /**
   * Dispatch messages between the <a href="MessageConsumer.html">
   * <code>MessageConsumer</code></a>: <a href="Engine.html">
   * <code>Engine</code></a> component and <a href="Network.html">
   * <code>Network</code></a> components.<p>
   * Handle persistent information in respect with engine transaction.
   * <p><hr>
   * Be careful, this method must only be used during a transaction in
   * order to ensure the mutual exclusion.
   *
   * @exception IOException	error when accessing the local persistent
   *				storage.
   */
  final void dispatch() throws Exception {
    Message msg = null;

    while (! mq.isEmpty()) {
      try {
        msg = (Message) mq.get();
      } catch (InterruptedException exc) {
        continue;
      }

      if (msg.from == null) msg.from = AgentId.localId;
      Channel.post(msg);
      mq.pop();
    }
    Channel.save();
  }

  /**
   * Cleans the Channel queue of all pushed notifications.
   * <p><hr>
   * Be careful, this method must only be used during a transaction in
   * order to ensure the mutual exclusion.
   */
  final void clean() {
    mq.removeAllElements();
  }

  protected Logger logmon = null;

  /**
   * Initializes a new <code>Engine</code> object (can only be used by
   * subclasses).
   */
  protected Engine() throws Exception {
    name = "Engine#" + AgentServer.getServerId();

    // Get the logging monitor from current server MonologLoggerFactory
    logmon = Debug.getLogger(Debug.A3Engine + ".#" + AgentServer.getServerId());

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + " created [" + getClass().getName() + "].");

    NbMaxAgents = AgentServer.getInteger("NbMaxAgents", NbMaxAgents).intValue();
    qin = new MessageVector(name, AgentServer.getTransaction().isPersistent());
    if (! AgentServer.getTransaction().isPersistent()) {
      NbMaxAgents = Integer.MAX_VALUE;
    }
    mq = new Queue();

    isRunning = false;
    canStop = false;
    thread = null;

    needToBeCommited = false;

    restore();
    if (modified) save();
  }

  void init() throws Exception {
    // Before any agent may be used, the environment, including the hash table,
    // must be initialized.
    agents = new Hashtable();
    try {
      // Creates or initializes AgentFactory, then loads and initializes
      // all fixed agents.
      fixedAgentIdList = (Vector) AgentServer.getTransaction().load(getName() + ".fixed");
      if (fixedAgentIdList == null) {
        // It's the first launching of this engine, in other case there is
        // at least the factory in fixedAgentIdList.
        fixedAgentIdList = new Vector();
        // Creates factory
        AgentFactory factory = new AgentFactory(AgentId.factoryId);
        createAgent(AgentId.factoryId, factory);
        factory.save();
        logmon.log(BasicLevel.INFO, getName() + ", factory created");
      }

      // loads all fixed agents
      for (int i=0; i<fixedAgentIdList.size(); ) {
        try {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       getName() + ", loads fixed agent" + fixedAgentIdList.elementAt(i));
          load((AgentId) fixedAgentIdList.elementAt(i));
          i += 1;
        } catch (Exception exc) {
          logmon.log(BasicLevel.ERROR,
                     getName() + ", can't restore fixed agent" +  fixedAgentIdList.elementAt(i), exc);
          fixedAgentIdList.removeElementAt(i);
        }
      }
    } catch (IOException exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", can't initialize", exc);
      throw exc;
    }
    logmon.log(BasicLevel.DEBUG, getName() + ", initialized");
  }

  void terminate() {
    logmon.log(BasicLevel.DEBUG, getName() + ", ends");
    Agent[] ag = new Agent[agents.size()];
    int i = 0;
    for (Enumeration e = agents.elements() ; e.hasMoreElements() ;) {
      ag[i++] = (Agent) e.nextElement();
    }
    for (i--; i>=0; i--) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "Agent" + ag[i].id + " [" + ag[i].name + "] garbaged");
      agents.remove(ag[i].id);
      try {
        // Set current agent running in order to allow from field fixed
        // for sendTo during agentFinalize (We assume that only Engine
        // use this method).
        agent = ag[i];
        ag[i].agentFinalize(false);
      } catch (Exception exc) {
        logmon.log(BasicLevel.ERROR,
                   "Agent" + ag[i].id + " [" + ag[i].name + "] error during agentFinalize", exc);
      } finally {
        agent = null;
      }
      ag[i] = null;
    }
  }

  /**
   * Creates and initializes an agent.
   *
   * @param agent	agent object to create
   *
   * @exception Exception
   *	unspecialized exception
   */
  final void createAgent(AgentId id, Agent agent) throws Exception {
    agent.id = id;
    agent.deployed = true;
    agent.agentInitialize(true);
    createAgent(agent);
  }

  /**
   * Creates and initializes an agent.
   *
   * @param agent	agent object to create
   *
   * @exception Exception
   *	unspecialized exception
   */
  final void createAgent(Agent agent) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", creates: " + agent);

    if (agent.isFixed()) {
      // Subscribe the agent in pre-loading list.
      addFixedAgentId(agent.getId());
    }
    if (agent.logmon == null)
      agent.logmon = Debug.getLogger(fr.dyade.aaa.agent.Debug.A3Agent +
                                     ".#" + AgentServer.getServerId());
    agent.save();

    // Memorize the agent creation and ...
    now += 1;
    garbage();

    agents.put(agent.getId(), agent);
  }

  /**
   * Deletes an agent.
   *
   * @param agent	agent to delete
   */
  void deleteAgent(AgentId from) throws Exception {
    Agent ag;
    try {
      ag = load(from);
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", delete Agent" + ag.id + " [" + ag.name + "]");
      AgentServer.getTransaction().delete(ag.id.toString());
    } catch (UnknownAgentException exc) {
      logmon.log(BasicLevel.ERROR,
                 getName() +
                 ", can't delete unknown Agent" + from);
      throw new Exception("Can't delete unknown Agent" + from);
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR,
                 getName() + ", can't delete Agent" + from, exc);
      throw new Exception("Can't delete Agent" + from);
    }
    if (ag.isFixed())
      removeFixedAgentId(ag.id);
    agents.remove(ag.getId());
    try {
      // Set current agent running in order to allow from field fixed
      // for sendTo during agentFinalize (We assume that only Engine
      // use this method).
      agent = ag;
      ag.agentFinalize(true);
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR,
                 "Agent" + ag.id + " [" + ag.name + "] error during agentFinalize", exc);
    } finally {
      agent = null;
    }
  }

  /**
   *  The <code>garbage</code> method should be called regularly , to swap out
   * from memory all the agents which have not been accessed for a time.
   */
  void garbage() {
    if (agents.size() < (NbMaxAgents + fixedAgentIdList.size()))
      return;

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 getName() + ", garbage: " + agents.size() +
                 '/' + NbMaxAgents + '+' + fixedAgentIdList.size() +
                 ' ' + now);
    long deadline = now - NbMaxAgents;
    Agent[] ag = new Agent[agents.size()];
    int i = 0;
    for (Enumeration e = agents.elements() ; e.hasMoreElements() ;) {
      ag[i++] = (Agent) e.nextElement();
    }
    for (i--; i>=0; i--) {
      if ((ag[i].last <= deadline) && (!ag[i].fixed)) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     "Agent" + ag[i].id + " [" + ag[i].name + "] garbaged");
        agents.remove(ag[i].id);
        try {
          // Set current agent running in order to allow from field fixed
          // for sendTo during agentFinalize (We assume that only Engine
          // use this method).
          agent = ag[i];
          ag[i].agentFinalize(false);
        } catch (Exception exc) {
          logmon.log(BasicLevel.ERROR,
                     "Agent" + ag[i].id + " [" + ag[i].name + "] error during agentFinalize", exc);
        } finally {
          agent = null;
        }
        ag[i] = null;
      }
    }

    logmon.log(BasicLevel.DEBUG,
               getName() + ", garbage: " + agents.size());
  }

  /**
   * Removes an <code>AgentId</code> in the <code>fixedAgentIdList</code>
   * <code>Vector</code>.
   *
   * @param id	the <code>AgentId</code> of no more used fixed agent.
   */
  void removeFixedAgentId(AgentId id) throws IOException {
    fixedAgentIdList.removeElement(id);
    AgentServer.getTransaction().save(fixedAgentIdList, getName() + ".fixed");
  }

  /**
   * Adds an <code>AgentId</code> in the <code>fixedAgentIdList</code>
   * <code>Vector</code>.
   *
   * @param id	the <code>AgentId</code> of new fixed agent.
   */
  void addFixedAgentId(AgentId id) throws IOException {   
    fixedAgentIdList.addElement(id);
    AgentServer.getTransaction().save(fixedAgentIdList, getName() + ".fixed");
  }

  /**
   *   Method used for debug and monitoring. It returns an enumeration
   * of all agents loaded in memory.
   */
  AgentId[] getLoadedAgentIdlist() {
    AgentId list[] = new AgentId[agents.size()];
    int i = 0;
    for (Enumeration e = agents.elements(); e.hasMoreElements() ;)
      list[i++] = ((Agent) e.nextElement()).id;
    return list;
  }

  /**
   *  Returns a string representation of the specified agent.
   *
   * @param id	The string representation of the agent's unique identification.
   * @return	A string representation of the specified agent.
   * @see 	Engine#dumpAgent(AgentId)
   */
  public String dumpAgent(String id) throws Exception {
    return dumpAgent(AgentId.fromString(id));
  }

  /**
   *  Returns a string representation of the specified agent. If the agent
   * is not present it is loaded in memory, be careful it is not initialized
   * (agentInitialize) nor cached in agents vector.
   *
   * @param id	The agent's unique identification.
   * @return	A string representation of specified agent.
   */
  public String dumpAgent(AgentId id)
  throws IOException, ClassNotFoundException, Exception {
    Agent ag = (Agent) agents.get(id);
    if (ag == null) {
      ag = Agent.load(id);
      if (ag == null) {
        return id.toString() + " unknown";
      }
    }
    return ag.toString();
  } 

  /**
   *  The <code>load</code> method return the <code>Agent</code> object
   * designed by the <code>AgentId</code> parameter. If the <code>Agent</code>
   * object is not already present in the server memory, it is loaded from
   * the storage.
   *
   *  Be careful, if the save method can be overloaded to optimize the save
   * process, the load procedure used by engine is always load.
   *
   * @param	id		The agent identification.
   * @return			The corresponding agent.
   *
   * @exception	IOException
   *	If an I/O error occurs.
   * @exception	ClassNotFoundException
   *	Should never happen (the agent has already been loaded in deploy).
   * @exception	UnknownAgentException
   *	There is no corresponding agent on secondary storage.
   * @exception Exception
   *	when executing class specific initialization
   */
  final Agent load(AgentId id)
  throws IOException, ClassNotFoundException, Exception {
    now += 1;

    Agent ag = (Agent) agents.get(id);
    if (ag == null)  {
      ag = reload(id);
      garbage();
    }
    ag.last = now;

    return ag;
  }

  /**
   * The <code>reload</code> method return the <code>Agent</code> object
   * loaded from the storage.
   *
   * @param	id		The agent identification.
   * @return			The corresponding agent.
   *
   * @exception IOException
   *	when accessing the stored image
   * @exception ClassNotFoundException
   *	if the stored image class may not be found
   * @exception Exception
   *	unspecialized exception
   */
  final Agent reload(AgentId id)
  throws IOException, ClassNotFoundException, Exception {
    Agent ag = null;
    if ((ag = Agent.load(id)) != null) {
      try {
        // Set current agent running in order to allow from field fixed
        // for sendTo during agentInitialize (We assume that only Engine
        // use this method).
        agent = ag;
        ag.agentInitialize(false);
      } catch (Throwable exc) {
        agent = null;
        // AF: May be we have to delete the agent or not to allow
        // reaction on it.
        logmon.log(BasicLevel.ERROR,
                   getName() + "Can't initialize Agent" + ag.id + " [" + ag.name + "]",
                   exc);
        throw new Exception(getName() + "Can't initialize Agent" + ag.id);
      }
      if (ag.logmon == null)
        ag.logmon = Debug.getLogger(fr.dyade.aaa.agent.Debug.A3Agent +
                                    ".#" + AgentServer.getServerId());
      agents.put(ag.id, ag);
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + "Agent" + ag.id + " [" + ag.name + "] loaded");
    } else {
      throw new UnknownAgentException();
    }

    return ag;
  }

  /**
   * Insert a message in the <code>MessageQueue</code>.
   * This method is used during initialization to restore the component
   * state from persistent storage.
   *
   * @param msg		the message
   */
  public void insert(Message msg) {
    qin.insert(msg);
  }

  /**
   * Validates all messages pushed in queue during transaction session.
   */
  public void validate() {
    qin.validate();
  }

  /**
   * Causes this engine to begin execution.
   *
   * @see stop
   */
  public void start() {
    if (isRunning) return;

    thread = new EngineThread(this);
    thread.setDaemon(false);

    logmon.log(BasicLevel.DEBUG, getName() + " starting.");

    String rp = AgentServer.getProperty("Engine.recoveryPolicy");
    if (rp != null) {
      for (int i = rpStrings.length; i-- > 0;) {
        if (rp.equals(rpStrings[i])) {
          recoveryPolicy = i;
          break;
        }
      }
    }
    isRunning = true;
    canStop = true;
    thread.start();

    logmon.log(BasicLevel.DEBUG, getName() + " started.");
  }

  /**
   * Forces the engine to stop executing.
   *
   * @see start
   */
  public void stop() {
    logmon.log(BasicLevel.DEBUG, getName() + ", stops.");
    isRunning = false;

    if (thread != null) {
      while (thread.isAlive()) {
        if (canStop) {

          if (thread.isAlive())
            thread.interrupt();
        }
        try {
          thread.join(1000L);
        } catch (InterruptedException exc) {
          continue;
        }
      }
      thread = null;
    }
  }

  /**
   * Get this engine's <code>MessageQueue</code> qin.
   *
   * @return this <code>Engine</code>'s queue.
   */
  public MessageQueue getQueue() {
    return qin;
  }

  /**
   * Tests if the engine is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Saves logical clock information to persistent storage.
   */
  public void save() throws IOException {
    if (modified) {
      stampBuf[0] = (byte)((stamp >>> 24) & 0xFF);
      stampBuf[1] = (byte)((stamp >>> 16) & 0xFF);
      stampBuf[2] = (byte)((stamp >>>  8) & 0xFF);
      stampBuf[3] = (byte)(stamp & 0xFF);
      AgentServer.getTransaction().saveByteArray(stampBuf, getName());
      modified = false;
    }
  }

  /**
   * Restores logical clock information from persistent storage.
   */
  public void restore() throws Exception {
    stampBuf = AgentServer.getTransaction().loadByteArray(getName());
    if (stampBuf == null) {
      stamp = 0;
      stampBuf = new byte[4];
      modified = true;
    } else {
      stamp = ((stampBuf[0] & 0xFF) << 24) +
      ((stampBuf[1] & 0xFF) << 16) +
      ((stampBuf[2] & 0xFF) <<  8) +
      (stampBuf[3] & 0xFF);
      modified = false;
    }
  }

  /**
   * This operation always throws an IllegalStateException.
   */
  public void delete() throws IllegalStateException {
    throw new IllegalStateException();
  }

  protected final int getStamp() {
    return stamp;
  }

  protected final void setStamp(int stamp) {
    modified = true;
    this.stamp = stamp;
  }

  protected final void stamp(Message msg) {
    modified = true;
    msg.source = AgentServer.getServerId();
    msg.dest = AgentServer.getServerId();
    msg.stamp = ++stamp;
  }

  /**
   *  Adds a message in "ready to deliver" list. This method allocates a
   * new time stamp to the message ; be Careful, changing the stamp imply
   * the filename change too.
   */
  public void post(Message msg) throws Exception {
    if (msg.isPersistent()) {
      stamp(msg);
      msg.save();
    }

    qin.push(msg);
  }

  protected boolean needToBeCommited = false;
  protected long timeout = Long.MAX_VALUE;

  protected void onTimeOut() throws Exception {}

  /**
   * Main loop of agent server <code>Engine</code>.
   */
  public void run() {
    try {
      main_loop:
        while (isRunning) {
          agent = null;
          canStop = true;

          // Get a notification, then execute the right reaction.
          try {
            msg = qin.get(timeout);
            if (msg == null) {
              onTimeOut();
              continue;
            }
          } catch (InterruptedException exc) {
            continue;
          }

          canStop = false;
          if (! isRunning) break;

          if ((msg.from == null) || (msg.to == null) || (msg.not == null)) {
            // The notification is malformed.
            logmon.log(BasicLevel.ERROR,
                       AgentServer.getName() + ": Bad message [" +
                       msg.from + ", " + msg.to + ", " + msg.not + ']');
            // Remove the failed notification ..
            qin.pop();
            // .. then deletes it ..
            msg.delete();
            // .. and frees it.
            msg.free();

            continue;
          }

          if ((msg.not.expiration <= 0L) ||
              (msg.not.expiration >= System.currentTimeMillis())) {
            // The message is valid, try to load the destination agent
            try {
              agent = load(msg.to);
            } catch (UnknownAgentException exc) {
              //  The destination agent don't exists, send an error
              // notification to sending agent.
              logmon.log(BasicLevel.ERROR,
                         getName() + ": Unknown agent, " + msg.to + ".react(" +
                         msg.from + ", " + msg.not + ")");
              agent = null;
              push(AgentId.localId,
                   msg.from,
                   new UnknownAgent(msg.to, msg.not));
            } catch (Exception exc) {
              //  Can't load agent then send an error notification
              // to sending agent.
              logmon.log(BasicLevel.ERROR,
                         getName() + ": Can't load agent, " + msg.to + ".react(" +
                         msg.from + ", " + msg.not + ")",
                         exc);
              agent = null;
              // Stop the AgentServer
              AgentServer.stop(false);
              break main_loop;
            }
          } else {
            if (msg.not.deadNotificationAgentId != null) {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG,
                           getName() + ": forward expired notification " +
                           msg.from + ", " + msg.not + " to " +
                           msg.not.deadNotificationAgentId);
              }
              ExpiredNot expiredNot = new ExpiredNot(msg.not, msg.from, msg.to);
              push(AgentId.localId, msg.not.deadNotificationAgentId, expiredNot);
            } else {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG,
                           getName() + ": removes expired notification " +
                           msg.from + ", " + msg.not);
              }
            }
          }

          if (agent != null) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         getName() + ": " + agent + ".react(" +
                         msg.from + ", " + msg.not + ")");
            try {
              agent.react(msg.from, msg.not);
            } catch (Exception exc) {
              logmon.log(BasicLevel.ERROR,
                         getName() + ": Uncaught exception during react, " +
                         agent + ".react(" + msg.from + ", " + msg.not + ")",
                         exc);
              switch (recoveryPolicy) {
              case RP_EXC_NOT:
              default:
                // In case of unrecoverable error during the reaction we have
                // to rollback.
                abort(exc);
              // then continue.
              continue;
              case RP_EXIT:
                // Stop the AgentServer
                AgentServer.stop(false);
                break main_loop;
              }
            }
          }

          // Commit all changes then continue.
          commit();
        }
    } catch (Throwable exc) {
      //  There is an unrecoverable exception during the transaction
      // we must exit from server.
      logmon.log(BasicLevel.FATAL,
                 getName() + ": Fatal error", exc);
      canStop = false;
      // Stop the AgentServer
      AgentServer.stop(false);
    } finally {
      terminate();
      logmon.log(BasicLevel.DEBUG, getName() + " stopped.");
    }
  }

  /**
   * Commit the agent reaction in case of right termination:<ul>
   * <li>suppress the processed notification from message queue,
   * then deletes it ;
   * <li>push all new notifications in qin and qout, and saves them ;
   * <li>saves the agent state ;
   * <li>then commit the transaction to validate all changes.
   * </ul>
   */
  void commit() throws Exception {
    AgentServer.getTransaction().begin();
    // Suppress the processed notification from message queue ..
    qin.pop();
    // .. then deletes it ..
    msg.delete();
    // .. and frees it.
    msg.free();
    // Post all notifications temporary kept in mq to the right consumers,
    // then saves changes.
    dispatch();
    // Saves the agent state then commit the transaction.
    if (agent != null) agent.save();
    AgentServer.getTransaction().commit(false);
    // The transaction has committed, then validate all messages.
    Channel.validate();
    AgentServer.getTransaction().release();
  }

  /**
   * Abort the agent reaction in case of error during execution. In case
   * of unrecoverable error during the reaction we have to rollback:<ul>
   * <li>reload the previous state of agent ;
   * <li>remove the failed notification ;
   * <li>clean the Channel queue of all pushed notifications ;
   * <li>send an error notification to the sender ;
   * <li>then commit the transaction to validate all changes.
   * </ul>
   */
  void abort(Exception exc) throws Exception {
    AgentServer.getTransaction().begin();
    // Reload the state of agent.
    try {
      agent = reload(msg.to);
    } catch (Exception exc2) {
      logmon.log(BasicLevel.ERROR,
                 getName() + ", can't reload Agent" + msg.to, exc2);
      throw new Exception("Can't reload Agent" + msg.to);
    }

    // Remove the failed notification ..
    qin.pop();
    // .. then deletes it ..
    msg.delete();
    // .. and frees it.
    msg.free();
    // Clean the Channel queue of all pushed notifications.
    clean();
    // Send an error notification to client agent.
    push(AgentId.localId,
         msg.from,
         new ExceptionNotification(msg.to, msg.not, exc));
    dispatch();
    AgentServer.getTransaction().commit(false);
    // The transaction has committed, then validate all messages.
    Channel.validate();
    AgentServer.getTransaction().release();
  }

  /**
   * Returns a string representation of this engine. 
   *
   * @return	A string representation of this engine.
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append('(').append(super.toString());
    strbuf.append(",name=").append(getName());
    strbuf.append(",running=").append(isRunning());
    strbuf.append(",agent=").append(agent).append(')');

    return strbuf.toString();
  }
}
