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

import java.io.IOException;
import fr.dyade.aaa.util.*;

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
 *   Agent agent = Agent.load(msg.to);
 *   // execute relevant reaction, all notification sent during this
 *   // reaction is inserted into persistant queue in order to processed
 *   // by the channel.
 *   agent.react(msg.from, msg.not);
 *   // save changes, then commit.
 *   <BEGIN TRANSACTION>
 *   qin.pop();
 *   channel.dispatch();
 *   agent.save();
 *   <COMMIT TRANSACTION>
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
 *
 * @author  Andre Freyssinet
 */
abstract class Engine implements Runnable, MessageConsumer {
  /** RCS version number of this file: $Revision: 1.5 $ */
  public static final String RCS_VERSION="@(#)$Id: Engine.java,v 1.5 2001-05-14 16:26:39 tachkeni Exp $";

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
  Thread thread = null;

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
   * Creates a new instance of Engine (real class depends of server type).
   *
   * @param isTransient	the transactionnal type of server.
   * @return		the corresponding <code>engine</code>'s instance.
   */
  static Engine newInstance(boolean isTransient) throws Exception {
    if (isTransient)
      return new TransientEngine();
    else
      return new TransactionEngine();
  }

  /**
   * Initializes a new <code>Engine</code> object (can only be used by
   * subclasses).
   */
  protected Engine() {
    name = "Engine#" + AgentServer.getServerId();

    if (Debug.debug && Debug.A3Server)
      Debug.trace(getName() + " created.", false);

    qin = new MessageQueue();

    isRunning = false;
    canStop = false;
    thread = null;   
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

    thread = new Thread(this, name);
    thread.setDaemon(false);

    if (Debug.debug && Debug.A3Server)
      Debug.trace(getName() + " starting.", false);

    String rp = Debug.properties.getProperty("Engine.recoveryPolicy");
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

    if (Debug.debug && Debug.A3Server)
      Debug.trace(getName() + " started.", false);
  }
  
  /**
   * Forces the engine to stop executing.
   *
   * @see start
   */
  public void stop() {
    isRunning = false;

    if (Debug.debug && Debug.A3Server)
      Debug.trace(getName() + " stopped", false);

    if (thread == null)
      // The session is idle.
      return;

    if (canStop) thread.interrupt();
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
	  msg = (Message) qin.get();
	} catch (InterruptedException exc) {
	  continue;
	}
	
	canStop = false;
	if (! isRunning) break;

	try {
	  agent = Agent.load(msg.to);
	} catch (UnknownAgentException exc) {
	  if ((Debug.engineLoop) || (Debug.error))
	    //  The destination agent don't exists, send an error
	    // notification to sending agent.
	    Debug.trace(getName() + ": UnknownAgent[" + msg.to + "].react(" +
			msg.from + ", " + msg.not + ")",
			false);
	  agent = null;
	  Channel.channel.sendTo(AgentId.nullId,
				 msg.from,
				 new UnknownAgent(msg.to, msg.not));
	} catch (Exception exc) {
	  if ((Debug.engineLoop) || (Debug.error))
	    //  Can't load agent then send an error notification
	    // to sending agent.
	    Debug.trace(getName() + ": BadAgent[" + msg.to + "].react(" +
			msg.from + ", " + msg.not + ")",
			false);
	  agent = null;
	  switch (recoveryPolicy) {
	  case RP_EXC_NOT:
	  default:
	    Channel.channel.sendTo(AgentId.nullId,
				   msg.from,
				   new ExceptionNotification(msg.to,
							     msg.not,
							     exc));
	    break;
	  case RP_EXIT:
	    AgentServer.stop();
	    break main_loop;
	  }
	}

	if (agent != null) {
	  if (Debug.engineLoop)
	    Debug.trace(getName() + ": " + agent + ".react(" +
			msg.from + ", " + msg.not + ")",
			false);
	  try {
	    if(AgentServer.MONITOR_AGENT) {
	      if(agent.monitored) {
	        agent.notifyInputListeners(msg.not);
	      }
	    }
	    try {
	      agent.react(msg.from, msg.not);
	    } catch (Error err) {
	      Debug.trace(getName() + ": Error in " +
			  agent + ".react(" + msg.from + ", " + msg.not + ")",
			  err);
	      throw new Exception(err.toString());
	    }
	    if(AgentServer.MONITOR_AGENT) {
	      if(agent.monitored) {
		agent.onReactionEnd();
	      }
	    }
	  } catch (Exception exc) {
	    if ((Debug.engineLoop) || (Debug.error))
	      Debug.trace(getName() + ": Exception in " +
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
	      AgentServer.stop();
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
      Debug.trace(getName() + ": Fatal error", exc);
      canStop = false;
      AgentServer.stop();
    }

    if (Debug.debug && Debug.A3Server)
      Debug.trace(getName() + ": Stopped.", false);
  }

  /**
   * Commit the agent reaction in case of rigth termination.
   */
  abstract void commit() throws IOException;

  /**
   * Abort the agent reaction in case of error during execution.
   */
  abstract void abort(Exception exc) throws Exception;

  /**
   * Returns a string representation of this engine. 
   *
   * @return	A string representation of this agent. 
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

final class TransactionEngine extends Engine {
  /** Logical timestamp information for messages in "local" domain. */
  int stamp;
  /** True if the timestamp is modified since last save. */
  boolean modified = false;

  TransactionEngine() throws Exception {
    super();
    restore();
    if (modified) save();
  }

  /**
   * Saves logical clock information to persistent storage.
   */
  public void save() throws IOException {
    if (modified) {
      AgentServer.transaction.save(new Integer(stamp), getName());
      modified = false;
    }
  }

  /**
   * Restores logical clock information from persistent storage.
   */
  public void restore() throws Exception {
    Integer obj = (Integer) AgentServer.transaction.load(getName());
    if (obj == null) {
      stamp = 0;
      modified = true;
    } else {
      stamp = obj.intValue();
      modified = false;
    }
  }

  /**
   *  Adds a message in "ready to deliver" list. This method allocates a
   * new time stamp to the message ; be Careful, changing the stamp imply
   * the filename change too.
   */
  public void post(Message msg) throws IOException {
    modified = true;
    msg.update = new Update(AgentServer.getServerId(),
			    AgentServer.getServerId(),
			    ++stamp);
    msg.save();
    qin.push(msg);
  }

  /**
   * Commit the agent reaction in case of rigth termination:<ul>
   * <li>suppress the processed notification from message queue,
   * then deletes it ;
   * <li>push all new notifications in qin and qout, and saves them ;
   * <li>saves the agent state ;
   * <li>then commit the transaction to validate all changes.
   * </ul>
   */
  void commit() throws IOException {
    AgentServer.transaction.begin();
    //  Suppress the processed notification from message queue,
    // then deletes it.
    qin.pop();
    msg.delete();
    // Push all new notifications in qin and qout, then saves changes.
    Channel.dispatch();
    // Saves the agent state then commit the transaction.
    if (agent != null) agent.save();
    AgentServer.transaction.commit();
    // The transaction has commited, then validate all messages.
    Channel.validate();
    AgentServer.transaction.release();
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
    AgentServer.transaction.begin();
    // Reload the state of agent.
    agent = Agent.reload(msg.to);
    // Remove the failed notification.
    qin.pop();
    msg.delete();
    // Clean the Channel queue of all pushed notifications.
    Channel.clean();
    // Send an error notification to client agent.
    Channel.channel.sendTo(AgentId.nullId,
			   msg.from,
			   new ExceptionNotification(msg.to, msg.not, exc));
    Channel.dispatch();
    AgentServer.transaction.commit();
    // The transaction has commited, then validate all messages.
    Channel.validate();
    AgentServer.transaction.release();
  }
}

final class TransientEngine extends Engine {
  TransientEngine() {
    super();
  }

  /**
   * Saves logical clock information to persistent storage.
   */
  public void save() {}

  /**
   * Restores logical clock information from persistent storage.
   */
  public void restore() {}

  /**
   *  Adds a message in "ready to deliver" list. There is no need to allocate
   * a time stamp to the message as there is no persistent storage.
   */
  public void post(Message msg) {
    qin.push(msg);
  }

  /**
   * Commit the agent reaction in case of rigth termination.
   */
  void commit() throws IOException {
    // Suppress the processed notification from message queue,
    // then deletes it.
    qin.pop();
    // Push all new notifications in qin and qout, then saves changes.
    Channel.dispatch();
    // The transaction has commited, then validate all messages.
    Channel.validate();
  }

  /**
   * Abort the agent reaction in case of error during execution.
   */
  void abort(Exception exc) throws Exception {
    // Remove the failed notification.
    qin.pop();
    // Clean the Channel queue of all pushed notifications.
    Channel.clean();
    // Send an error notification to client agent.
    Channel.channel.sendTo(AgentId.nullId,
			   msg.from,
			   new ExceptionNotification(msg.to, msg.not, exc));
    Channel.dispatch();
    // The transaction has commited, then validate all messages.
    Channel.validate();
  }
}
