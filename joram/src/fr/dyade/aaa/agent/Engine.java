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
 *   <COMIT TRANSACTION>
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
 *
 * @author  Andre Freyssinet
 */
abstract class Engine implements Runnable {

  /** RCS version number of this file: $Revision: 1.1.1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: Engine.java,v 1.1.1.1 2000-05-30 11:45:24 tachkeni Exp $";

  /**
   * Temporary queue used to store all notifications sent during a
   * reaction.
   */
  protected Queue mq;
  /**
   * 
   */ 
  protected MessageQueue qin;
  /**
   * 
   */ 
  protected MessageQueue qout;

  /**
   * Boolean variable used to stop the engine.
   */ 
  protected volatile boolean canStop;

  /**
   * 
   */ 
  Agent agent = null;

  /**
   * 
   */ 
  Thread thread;

  /**
   * send <code>ExceptionNotification</code> notification in case of exception
   * in agent specific code.
   * Constant value for the <code>recoveryPolicy</code> variable.
   */
  static final int RP_EXC_NOT = 0;
  /**
   * stop agent server in case of exception in agent specific code.
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

  static Engine newInstance(Queue mq,
			    MessageQueue qin,
			    MessageQueue qout) {
    if (Server.isTransient(Server.getServerId()))
      return new TransientEngine(mq, qin, qout);
    else
      return new TransactionEngine(mq, qin, qout);
  }

  protected Engine(Queue mq,
		   MessageQueue qin,
		   MessageQueue qout) {
    canStop = false;
    this.mq = mq;
    this.qin = qin;
    this.qout =qout ;    
    thread = new Thread(this, "Engine#" + Server.serverId);
    thread.setDaemon(false);
  }

  void start() {
    String rp = Debug.properties.getProperty("Engine.recoveryPolicy");
    if (rp != null) {
      for (int i = rpStrings.length; i-- > 0;) {
	if (rp.equals(rpStrings[i])) {
	  recoveryPolicy = i;
	  break;
	}
      }
    }
    canStop = true;
    thread.start();
  }
  
  void stop() {
    if (canStop) thread.stop();
  }

  abstract public void run();
}

final class TransactionEngine extends Engine {
  TransactionEngine(Queue mq,
		    MessageQueue qin,
		    MessageQueue qout) {
    super(mq, qin, qout);
  }

/**
 *
 * <p><hr>
 * <b>Handling errors.</b><p>
 * Two types of errors may occur: errors of first type are detected in the
 * source code and signaled by an <code>Exception</code>; serious errors lead
 * to an <code>Error</code> being raised then the engine exits. In the first
 * case the exception may be handled at any level, even partially. Most of them
 * are signaled up to the ngine loop. Two cases are then distinguished depending
 * on the recovery policy:
 * <ul>
 * <li>if <code>recoveryPolicy</code> is set to <code>RP_EXC_NOT</code> (default
 * value) then the agent state and the message queue are restored (ROLLBACK); an
 * <code>ExceptionNotification</code> notification is sent to the sender and the
 * engine may then proceed with next notification;
 * <li>if <code>recoveryPolicy</code> is set to <code></code> the engine stops
 * agent server.
 * </ul>
 */
  public void run() {
    try {
      main_loop:
      while (Server.isRunning) {
	agent = null;
	canStop = true;

	// Get a notification, then execute the right reaction.
	Message msg = (Message) qin.get();
	
	canStop = false;
	if (! Server.isRunning) break;

	try {
	  agent = Agent.load(msg.to);
	} catch (UnknownAgentException exc) {
	  if ((Debug.engineLoop) || (Debug.error))
	    //  The destination agent don't exists, send an error
	    // notification to sending agent.
	    Debug.trace("Engine: UnknownAgent[" + msg.to + "].react(" +
			msg.from + ", " + msg.not + ")",
			false);
	  agent = null;
	  Server.channel.sendTo(msg.from,
				 new UnknownAgent(msg.to, msg.not));
	} catch (Exception exc) {
	  if ((Debug.engineLoop) || (Debug.error))
	    //  Can't load agent then send an error notification
	    // to sending agent.
	    Debug.trace("Engine: BadAgent[" + msg.to + "].react(" +
			msg.from + ", " + msg.not + ")",
			false);
	  agent = null;
	  switch (recoveryPolicy) {
	  case RP_EXC_NOT:
	  default:
	    break;
	  case RP_EXIT:
	    Server.stop();
	    break main_loop;
	  }
	  Server.channel.sendTo(msg.from,
				new ExceptionNotification(msg.to,
							  msg.not,
							  exc));
	}

	if (agent != null) {
	  if (Debug.engineLoop)
	    Debug.trace("Engine: " + agent + ".react(" +
			msg.from + ", " + msg.not + ")",
			false);
	  try {
	    if(Server.MONITOR_AGENT) {
	      if(agent.monitored) {
	        agent.notifyInputListeners(msg.not);
	      }
	    }
	    agent.react(msg.from, msg.not);
	    if(Server.MONITOR_AGENT) {
	      if(agent.monitored) {
		agent.onReactionEnd();
	      }
	    }
	  } catch (Exception exc) {
	    if ((Debug.engineLoop) || (Debug.error))
	      Debug.trace("Engine: Exception in " +
			  agent + ".react(" + msg.from + ", " + msg.not + ")",
			  exc);
	    switch (recoveryPolicy) {
	    case RP_EXC_NOT:
	    default:
	      break;
	    case RP_EXIT:
	      Server.stop();
	      break main_loop;
	    }
	    //  In case of unrecoverable error during the reaction we have
	    // to rollback:
	    Server.transaction.begin();
	    //	=> reload the state of agent.
	    agent = Agent.reload(msg.to);
	    //	=> remove the failed notification.
	    qin.pop();
	    msg.delete();
	    //	=> clean the Channel queue of all pushed notifications.
	    Server.channel.clean();
	    //	=> send an error notification to client agent.
	    Server.channel.sendTo(msg.from,
				   new ExceptionNotification(msg.to, msg.not, exc));
	    Server.channel.dispatch();
	    Server.transaction.commit();
	    // The transaction has commited, then validate the sending message.
	    qin.validate();
	    qout.validate();
	    Server.transaction.release();
	    //	=> continue.
	    continue;
	  }
	}

	Server.transaction.begin();
	//  Suppress the processed notification from message queue,
	// then deletes it.
	qin.pop();
	msg.delete();
	// Push all new notifications in qin and qout, then saves changes.
	Server.channel.dispatch();
	// Saves the agent state then commit the transaction.
	if (agent != null) agent.save();
	Server.transaction.commit();
	// The transaction has commited, then validate the sending messages.
	qin.validate();
	qout.validate();
	Server.transaction.release();
      }
    } catch (Throwable exc) {
      //  There is an unrecoverable exception during the transaction
      // we must exit from server.
      Debug.trace("Engine: Fatal error", exc);
      canStop = false;
      Server.stop();
    }
  }
}

final class TransientEngine extends Engine {
  TransientEngine(Queue mq,
		  MessageQueue qin,
		  MessageQueue qout) {
    super(mq, qin, qout);
  }

  public void run() {
    try {
      main_loop:
      while (Server.isRunning) {
	agent = null;
	canStop = true;

	// Get a notification, then execute the right reaction.
	Message msg = (Message) mq.get();
	
	canStop = false;
	if (! Server.isRunning) break;

	try {
	  agent = Agent.load(msg.to);
	} catch (UnknownAgentException exc) {
	  if ((Debug.engineLoop) || (Debug.error))
	    //  The destination agent don't exists, send an error
	    // notification to sending agent.
	    Debug.trace("Engine: UnknownAgent.react(" +
			msg.from + ", " + msg.not + ")",
			false);
	  agent = null;
	  Server.channel.sendTo(msg.from,
				 new UnknownAgent(msg.to, msg.not));
	} catch (Exception exc) {
	  if ((Debug.engineLoop) || (Debug.error))
	    //  Can't load agent then send an error notification
	    // to sending agent.
	    Debug.trace("Engine: BadAgent[" + msg.to + "].react(" +
			msg.from + ", " + msg.not + ")",
			false);
	  agent = null;
	  switch (recoveryPolicy) {
	  case RP_EXC_NOT:
	  default:
	    break;
	  case RP_EXIT:
	    Server.stop();
	    break main_loop;
	  }
	  Server.channel.sendTo(msg.from,
				 new ExceptionNotification(msg.to,
							   msg.not,
							   exc));
	}

	if (agent != null) {
	  if (Debug.engineLoop) 
	    Debug.trace("Engine: " + agent + ".react(" +
			msg.from + ", " + msg.not + ")",
			false);
	  try {
	    if(Server.MONITOR_AGENT) {
	      if(agent.monitored) {
	        agent.notifyInputListeners(msg.not);
	      }
	    }
	    agent.react(msg.from, msg.not);
	    if(Server.MONITOR_AGENT) {
	      if(agent.monitored) {
		agent.onReactionEnd();
	      }
	    }
	  } catch(Exception exc) {
	    if ((Debug.engineLoop) || (Debug.error))
	    if ((Debug.engineLoop) || (Debug.error))
	      Debug.trace("Engine: Exception in " +
			  agent + ".react(" + msg.from + ", " + msg.not + ")",
			  exc);
	    switch (recoveryPolicy) {
	    case RP_EXC_NOT:
	    default:
	      break;
	    case RP_EXIT:
	      Server.stop();
	      break main_loop;
	    }
	    Server.channel.sendTo(msg.from,
				   new ExceptionNotification(msg.to, msg.not, exc));
	  }
	}

	mq.pop();
	Server.channel.dispatch();
      }
    } catch (Throwable exc) {
      //  There is an unrecoverable exception during the transaction
      // we must exit from server.
      Debug.trace("Engine: Fatal error", exc);
      canStop = false;
      Server.stop();
    }
  }
}
