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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;

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
 *   // reaction is inserted into persistant queue in order to processed
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
abstract class Engine implements Runnable, MessageConsumer, EngineMBean {
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
   * is true then the engine is waiting and it can interupted, else it
   * handles a notification and it will exit after (the engine tests the
   * <code><a href="#isRunning">isRunning</a></code> variable between
   * each reaction)
   */
  protected volatile boolean canStop;

  /** This table is used to maintain a list of agents already in memory
   * using the AgentId as primary key.
   */
  Hashtable agents;
  /** Virtual time counter use in FIFO swap-in/swap-out mechanisms. */
  long now = 0;
  /** Maximum number of memory loaded agents. */
  int NbMaxAgents = 100;

  /** Vector containing id's of all fixed agents. */
  Vector fixedAgentIdList = null;

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
    String cname = "fr.dyade.aaa.agent.TransactionEngine";
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
  protected Engine() {
    name = "Engine#" + AgentServer.getServerId();

    // Get the logging monitor from current server MonologLoggerFactory
    logmon = Debug.getLogger(Debug.A3Engine +
                             ".#" + AgentServer.getServerId());
    logmon.log(BasicLevel.DEBUG,
               getName() + " created [" + getClass().getName() + "].");

    NbMaxAgents = Integer.getInteger("NbMaxAgents", NbMaxAgents).intValue();
    qin = new MessageQueue(name, AgentServer.getTransaction().isPersistent());
    if (! AgentServer.getTransaction().isPersistent()) {
      NbMaxAgents = Integer.MAX_VALUE;
    }
    mq = new Queue();
 
    isRunning = false;
    canStop = false;
    thread = null;   
  }

  void init() throws Exception {
    // Before any agent may be used, the environment, including the hash table,
    // must be initialized.
    agents = new Hashtable();
    try {
      // Creates or initializes AgentFactory, then loads and initializes
      // all fixed agents.
      fixedAgentIdList = (Vector) AgentServer.transaction.load(getName() + ".fixed");
      if (fixedAgentIdList == null) {
        // It's the first launching of this engine, in other case theres is
        // at least the factory in fixedAgentIdList.
        fixedAgentIdList = new Vector();
        // Creates factory
        AgentFactory factory = new AgentFactory(AgentId.factoryId);
        createAgent(factory);
        factory.agentInitialize(true);
        logmon.log(BasicLevel.WARN, getName() + ", factory created");
      }

      // loads all fixed agents
      for (int i=0; i<fixedAgentIdList.size(); ) {
	try {
	  Agent ag = load((AgentId) fixedAgentIdList.elementAt(i));
          i += 1;
	} catch (Exception exc) {
          logmon.log(BasicLevel.ERROR,
                     getName() + ", can't restore fixed agent#" + 
                     fixedAgentIdList.elementAt(i), exc);
          fixedAgentIdList.removeElementAt(i);
	}
      }
    } catch (IOException exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", can't initialize");
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
      ag[i].agentFinalize(false);
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
  void createAgent(Agent agent) throws Exception {
    agent.deployed = true;
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
      AgentServer.transaction.delete(ag.id.toString());
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
    ag.agentFinalize(true);
  }

  /**
   *  The <code>garbage</code> method should be called regularly , to swap out
   * from memory all the agents which have not been accessed for a time.
   */
  void garbage() {
    if (agents.size() < (NbMaxAgents + fixedAgentIdList.size()))
      return;

    logmon.log(BasicLevel.WARN, getName() + ", garbaged");
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
        ag[i].agentFinalize(false);
        ag[i] = null;
      }
    }
  }

  /**
   * Removes an <code>AgentId</code> in the <code>fixedAgentIdList</code>
   * <code>Vector</code>.
   *
   * @param id	the <code>AgentId</code> of no more used fixed agent.
   */
  void removeFixedAgentId(AgentId id) throws IOException {
    fixedAgentIdList.removeElement(id);
    AgentServer.transaction.save(fixedAgentIdList, getName() + ".fixed");
  }

  /**
   * Adds an <code>AgentId</code> in the <code>fixedAgentIdList</code>
   * <code>Vector</code>.
   *
   * @param id	the <code>AgentId</code> of new fixed agent.
   */
  void addFixedAgentId(AgentId id) throws IOException {   
    fixedAgentIdList.addElement(id);
    AgentServer.transaction.save(fixedAgentIdList, getName() + ".fixed");
  }

  /**
   *   Method used for debug and monitoring. It returns an enumeration
   * of all agents loaded.
   */
  AgentId[] getLoadedAgentIdlist() {
    AgentId list[] = new AgentId[agents.size()];
    int i = 0;
    for (Enumeration e = agents.elements(); e.hasMoreElements() ;)
      list[i++] = ((Agent) e.nextElement()).id;
    return list;
  }

  public String dumpAgent(String id) throws Exception {
    return dumpAgent(AgentId.fromString(id));
  }

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
   *  Be carefull, if the save method can be overloaded to optimize the save
   * processus, the load procedure used by engine is always load.
   *
   * @param	id		The agent identification.
   * @return			The corresponding agent.
   *
   * @exception	IOException
   *	If an I/O error occurs.
   * @exception	ClassNotFoundException
   *	Should never happen (the agent has already been loaded in deploy).
   * @exception	UnknownAgentException
   *	There is no correponding agent on secondary storage.
   * @exception Exception
   *	when executing class specific initialization
   */
  final Agent load(AgentId id)
    throws IOException, ClassNotFoundException, Exception {
    now += 1;
    Agent ag = (Agent) agents.get(id);
    if (ag == null)  return reload(id);

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
      agents.put(ag.id, ag);
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + "Agent" + ag.id + " [" + ag.name + "] loaded");

      try {
        // Set current agent running in order to allow from field fixed
        // for sendTo during agentInitialize (We assume that only Engine
        // use this method).
        agent = ag;
        ag.agentInitialize(false); // initializes agent
      } catch (Throwable exc) {
        agent = null;
        // AF: May be we have to delete the agent or not to allow
        // reaction on it.
        logmon.log(BasicLevel.ERROR,
                   getName() + "Can't initialize Agent" + ag.id +
                   " [" + ag.name + "]",
                   exc);
      }
      if (ag.logmon == null)
        ag.logmon = Debug.getLogger(fr.dyade.aaa.agent.Debug.A3Agent +
                                    ".#" + AgentServer.getServerId());
    } else {
      throw new UnknownAgentException();
    }
    ag.last = now;
    return ag;
  }

  /**
   * Insert a message in the <code>MessageQueue</code>.
   * This method is used during initialisation to restore the component
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
   * Invalidates all messages pushed in queue during transaction session.
   */
  public void invalidate() {
    qin.invalidate();
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

  protected boolean needToBeCommited = false;
  protected long timeout = Long.MAX_VALUE;

  protected void onTimeOut() {}

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
	  msg = (Message) qin.get(timeout);
          if (msg == null) {
            onTimeOut();
            continue;
          }
	} catch (InterruptedException exc) {
	  continue;
	}
	
	canStop = false;
	if (! isRunning) break;

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
                 getName() + ": Fatal error",
                 exc);
      canStop = false;
      // Stop the AgentServer
      AgentServer.stop(false);
    } finally {
      terminate();
      logmon.log(BasicLevel.DEBUG, getName() + " stopped.");
    }
  }

  /**
   * Commit the agent reaction in case of rigth termination.
   */
  abstract void commit() throws Exception;

  /**
   * Abort the agent reaction in case of error during execution.
   */
  abstract void abort(Exception exc) throws Exception;

  /**
   * Returns a string representation of this engine. 
   *
   * @return	A string representation of this engine.
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append(getName());
    if (! isRunning) {
      strbuf.append(" is stopped.");
    } else if (agent == null) {
      strbuf.append(" is waiting new message.");
    } else {
      strbuf.append(" is running, qin[").append(qin.size()).append("].\n");
      strbuf.append("Agent [");
      strbuf.append(agent);
      strbuf.append("]\nNotification [");
      strbuf.append(msg.not);
      strbuf.append("]\nFrom Agent");
      strbuf.append(msg.from);
    }
    return strbuf.toString();
  }
}
