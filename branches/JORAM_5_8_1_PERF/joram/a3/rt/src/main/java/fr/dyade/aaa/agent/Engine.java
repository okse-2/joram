/*
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

// JORAM_PERF_BRANCH
public interface Engine extends MessageConsumer {
  
  boolean isEngineThread();
  
  void resetAverageLoad();
  
  float getAverageLoad1();
  
  float getAverageLoad5();
  
  float getAverageLoad15();
  
  boolean isAgentProfiling();
  
  void setAgentProfiling(boolean agentProfiling);
  
  long getReactTime();
  
  long getCommitTime();

  void init() throws Exception;
  
  int getNbWaitingMessages();
  
  String dumpAgent(AgentId id) throws IOException, ClassNotFoundException;
  
  void push(AgentId to, Notification not);
  
  void push(AgentId from, AgentId to, Notification not);
  
  void createAgent(AgentId id, Agent agent) throws Exception;
  
  void deleteAgent(AgentId from) throws Exception;

}
