/*
 * Copyright (C) 2001 - 2013 ScalAgent Distributed Technologies
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

public interface EngineMBean {
  /**
   * Returns this <code>Engine</code>'s name.
   *
   * @return this <code>Engine</code>'s name.
   */
  public String getName();

  /**
   * Returns the maximum number of agents loaded in memory.
   *
   * @return	the maximum number of agents loaded in memory
   */
  public int getNbMaxAgents();

  /**
   * Returns the number of agents actually loaded in memory.
   *
   * @return	the maximum number of agents actually loaded in memory
   */
  public int getNbAgents();

  /**
   * Tests if the engine is alive.
   *
   * @return	true if this <code>MessageConsumer</code> is alive; false
   * 		otherwise.
   */
  public boolean isRunning();

  /**
   * Returns the number of agent's reaction since last boot.
   *
   * @return	the number of agent's reaction since last boot
   */
  public long getNbReactions();

  /**
   * Gets the number of messages posted to this engine since creation.
   *
   *  return	the number of messages.
   */
  public int getNbMessages();

  /**
   * Gets the number of waiting messages in this engine.
   *
   *  return	the number of waiting messages.
   */
  public int getNbWaitingMessages();

  /**
   * Returns a report about the distribution of messages type in queue.
   */
  public String report();
  
  /**
   * Returns the number of fixed agents.
   *
   * @return	the number of fixed agents
   */
  public int getNbFixedAgents();

  /**
   * Sets the maximum number of agents that can be loaded simultaneously
   * in memory.
   *
   * @param NbMaxAgents	the maximum number of agents
   */
  public void setNbMaxAgents(int NbMaxAgents);

  public String dumpAgent(String id) throws Exception;
  
  /**
   * Returns true if the agent profiling is on.
   * 
   * @see fr.dyade.aaa.agent.EngineMBean#isAgentProfiling()
   */
  public boolean isAgentProfiling();
  
  /**
   * Sets the agent profiling.
   * If true, the cumulative time of reaction and commit is kept for each agent.
   * In addition the total reaction and commit time is calculated for this engine.
   * 
   * @see fr.dyade.aaa.agent.EngineMBean#setAgentProfiling(boolean)
   */
  public void setAgentProfiling(boolean agentProfiling);
  
  /**
   * Returns the total reaction time calculated for this engine.
   * @return the reactTime
   */
  public long getReactTime();
  
  /**
   * Reset the reaction time for this engine.
   */
  public void resetReactTime();

  /**
   * Returns the total commit time calculated for this engine.
   * @return the commitTime
   */
  public long getCommitTime();
  
  /**
   * Reset the commit time for this engine.
   */
  public void resetCommitTime();
  
  /**
   * Reset react and commit time for this engine.
   */
  public void resetTimer();

  /**
   * Returns the load averages for the last minute.
   * @return the load averages for the last minute.
   */
  public float getAverageLoad1();

  /**
   * Returns the load averages for the past 5 minutes.
   * @return the load averages for the past 5 minutes.
   */
  public float getAverageLoad5();
  
  /**
   * Returns the load averages for the past 15 minutes.
   * @return the load averages for the past 15 minutes.
   */
  public float getAverageLoad15();
  
  /**
   * Returns a string representation of this engine. 
   *
   * @return	A string representation of this engine.
   */
  public String toString();

  /** Causes this engine to begin execution */
  public void start() throws Exception;

  /** Forces the engine to stop executing */
  public void stop();
  
  /**
   * Returns the flag to avoid transactions.
   * @return the flag to avoid transactions
   */
  public boolean isNoTxIfTransient();

}
