/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */
package fr.dyade.aaa.task;

import java.io.*;

import org.objectweb.monolog.api.BasicLevel;
import org.objectweb.monolog.api.Monitor;

import fr.dyade.aaa.agent.*;

/**
 * <code>Composed</code> task with a sequential order of execution of
 * sub-tasks.
 */
public class Sequential extends Composed {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: Sequential.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 

  /**
   * Default constructor
   */
  public Sequential() {
    super();
  }

 /**
   * Invoked by the Configurator
   */
  public Sequential(short to, String name) {
    super(to, name);
  }  

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   * @param child	children tasks ids
   */
  public Sequential(short to, AgentId parent, AgentId child[]) {
    super(to, parent, child);
  }

  /**
    * Starts sub-tasks that can be started (all their dependent tasks
    * have completed).
    */
  protected void startSubtasks() throws Exception {
    int i;
    for (i = 0; i < child.length; i ++) {
      if (child[i].status != Status.DONE)
	break;
    }
    if (i == child.length) 
      return;
    if (child[i].status != Status.WAIT) {
      logmon.log(BasicLevel.ERROR,
                 getName() + ", bad sub-task #" + i +
                 " [status=" + Status.toString(child[i].status) + ']');
      throw new IllegalStateException();
    }
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 getName() + ", " + " start sub-task #" + i);
    if (i == 0) {
      if (parameter != null)
	sendTo(child[0].id, new ParamNotification(parameter));
    } else {
      if (child[i-1].result != null)
	sendTo(child[i].id, new ParamNotification(child[i-1].result));
    }
    sendTo(child[i].id, new Condition());
    child[i].status = Status.RUN;
  }

  /**
   * Builds the result object when this task completes.
   * <p>
   * Returns the result of last sub-task.
   *
   * @return	the result object of the task
   */
  protected Object buildResult() throws Exception {
    return child[child.length-1].result;
  }
}
