/*
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package fr.dyade.aaa.agent;

import java.io.IOException;

/**
 * The <code>AgentEngine</code> provides multiprogramming of agents. It
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
 * The <code>AgentEngine</code> ensures the atomic handling of an agent
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
public interface AgentEngine extends MessageConsumer {
  
  /**
   * Checks if the current thread calling this method 
   * belongs to the engine.
   * 
   * @return true if the current thread calling this method 
   * belongs to the engine
   */
  boolean isEngineThread();
  
  void resetAverageLoad();
  
  /**
   * Returns the load averages for the last minute.
   * @return the load averages for the last minute.
   */
  float getAverageLoad1();
  
  /**
   * Returns the load averages for the past 5 minutes.
   * @return the load averages for the past 5 minutes.
   */
  float getAverageLoad5();
  
  /**
   * Returns the load averages for the past 15 minutes.
   * @return the load averages for the past 15 minutes.
   */
  float getAverageLoad15();
  
  /**
   * Returns true if the agent profiling is on.
   * 
   * @see fr.dyade.aaa.agent.EngineMBean#isAgentProfiling()
   */
  boolean isAgentProfiling();
  
  /**
   * Sets the agent profiling.
   * 
   * @see fr.dyade.aaa.agent.EngineMBean#setAgentProfiling(boolean)
   */
  void setAgentProfiling(boolean agentProfiling);
  
  /**
   * @return the reactTime
   */
  long getReactTime();
  
  /**
   * @return the commitTime
   */
  long getCommitTime();
  
  /**
   * Initializes the engine. The <code>AgentEngineContext</code> 
   * parameter should be for the unique private use of this 
   * <code>AgentEngine</code> and should not be shared 
   * with any other components otherwise
   * the agent server security would be broken.
   * @param agentEngineContext context enabling this 
   * <code>AgentEngine</code> to invoke operations
   * that cannot be accessed outside of the 
   * <code>fr.dyade.aaa.agent</code> package.
   * The <code>AgentEngineContext</code> 
   * should be for the unique private use of this 
   * <code>AgentEngine</code> and should not be shared 
   * with any other components otherwise
   * the agent server security would be broken.
   * @throws Exception
   */
  void init(AgentEngineContext agentEngineContext) throws Exception;
  
  /**
   * Gets the number of waiting messages in this engine.
   *
   *  return  the number of waiting messages.
   */
  int getNbWaitingMessages();
  
  /**
   *  Returns a string representation of the specified agent. If the agent
   * is not present it is loaded in memory, be careful it is not initialized
   * (agentInitialize) nor cached in agents vector.
   *
   * @param id  The agent's unique identification.
   * @return  A string representation of specified agent.
   */
  String dumpAgent(AgentId id) throws IOException, ClassNotFoundException;
  
  /**
   * Puts a notification in the output queue.
   * @param to the destination
   * @param not the notification to push
   */
  void push(AgentId to, Notification not);
  
  /**
   * Puts a notification in the output queue.
   * @param from the source
   * @param to the destination
   * @param not the notification to push
   */
  void push(AgentId from, AgentId to, Notification not);
  
  /**
   * Creates and initializes an agent.
   *
   * @param agent agent object to create
   *
   * @exception Exception
   *  unspecialized exception
   */
  void createAgent(AgentId id, Agent agent) throws Exception;
  
  /**
   * Deletes an agent.
   *
   * @param agent agent to delete
   */
  void deleteAgent(AgentId from) throws Exception;
  
  /**
   * Returns the flag to avoid transactions.
   * 
   * @return the flag to avoid transactions
   */
  boolean isNoTxIfTransient();

}
