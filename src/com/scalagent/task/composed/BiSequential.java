/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
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
package com.scalagent.task.composed;

import java.io.*;

import java.util.*;
import fr.dyade.aaa.agent.*;
import com.scalagent.task.*;

/**
 * <code>Composed</code> task handling the execution of two subtasks
 * with a sequential order. This class has been defined to be used in GCT,
 * as the dynamic number of sub-tasks in a <code>Composed</code> task may not
 * yet be represented in OCL.
 * The name Program is used in the variables names, however any
 * <code>Task</code> may be used as a sub-task.
 */
public class BiSequential extends Sequential {
  /**
   * Assuming the sequential order: firstProgram then secondProgram
   */
  public final Role firstProgram = new Role("firstProgram");

  /**
   * Assuming the sequential order: firstProgram then secondProgram
   */
  public final Role secondProgram = new Role("secondProgram");

  /**
   * Default constructor
   */
  public BiSequential() {
    super();
  }

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   * @param child	children tasks ids
   */
  public BiSequential(short to, AgentId parent, AgentId child[]) {
    super(to, parent, child);
  }


  /**
   * Invoked by the Configurator.
   *
   * @param to		agent server id where agent is to be deployed
   * @param name	name of the agent
   */
  public BiSequential(short to, String name) {
    super(to, name);
  }


  /**
   * Gives this agent an opportunity to initialize after having been deployed,
   * and each time it is loaded into memory.
   *
   * @param firstTime	<code>true</code> when first called by the factory
   */
  public void initialize(boolean firstTime) throws Exception {
    if (firstTime) {
      setTwin();
    }
  }


  /** first subtask to be executed */
  public void setFirstProgram(AgentId id) {
    firstProgram.setListener(id);
  }

  /** second subtask to be executed */
  public void setSecondProgram(AgentId id) {
    secondProgram.setListener(id);
  }


  /**
   * Calls the <code>setChild</code> on the two subtasks
   */
  public void setTwin() {
    AgentId[] twin = new AgentId[2];
    twin[0] = firstProgram.getListener();
    twin[1] = secondProgram.getListener();
    setChild(twin);
  }   

}
