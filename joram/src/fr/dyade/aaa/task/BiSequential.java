/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import java.io.*;
import fr.dyade.aaa.agent.*;
import java.util.*;

/**
 * <code>Composed</code> task handling the execution of two subtasks
 * with a sequential order. This class has been defined to be used in GCT,
 * as the dynamic number of sub-tasks in a <code>Composed</code> task may not
 * yet be represented in OCL.
 * The name Program is used in the variables names, however any
 * <code>Task</code> may be used as a sub-task.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 */
public class BiSequential extends Sequential {

  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: BiSequential.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


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
