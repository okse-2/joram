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
 * <code>Composed</code> task with a parallel order of execution of sub-tasks.
 */
public class Parallel extends Composed {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: Parallel.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 

  /**
   * Default constructor
   */
  public Parallel() {
    super();
  }

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   * @param parent	agent to report status to
   * @param child	children tasks ids
   */
  public Parallel(short to, AgentId parent, AgentId child[]) {
    super(to, parent, child);
  }

  /**
   * Starts sub-tasks that can be started (all their dependent tasks
   * have completed).
   */
  protected void startSubtasks() throws Exception {
    int i;
    for (i = 0; i < child.length; i ++) {
      if (child[i].status != Status.WAIT)
	break;
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", " + " start sub-task #" + i);
      if (parameter != null)
	sendTo(child[i].id, new ParamNotification(parameter));
      sendTo(child[i].id, new Condition());
      child[i].status = Status.RUN;
    }
  }

  /**
   * Builds the result object when this task completes.
   * <p>
   * Returns the array of sub-tasks results, or null if all results are null.
   *
   * @return	the result object of the task
   */
  protected Object buildResult() throws Exception {
    boolean nullReturn = true;
    for (int i = child.length; i-- > 0;) {
      if (child[i].result != null) {
	nullReturn = false;
	break;
      }
    }
    if (nullReturn)
      return null;

    Object result[] = new Object[child.length];
    for (int i = child.length; i-- > 0;)
      result[i] = child[i].result;
    return result;
  }
}
