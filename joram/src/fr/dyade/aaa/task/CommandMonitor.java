/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import java.io.*;
import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.task.Task.Status;


/**
 * The main use of <code>Monitor</code> objects is to provide for handling
 * an asynchronous <code>IndexedCommand</code> <code>IndexedReport</code>
 * communication without creating additional and temporary
 * <code>ServiceTask</code> or <code>Composed</code> agents. Class
 * <code>CommandMonitor</code> provides a base implementation
 * for this use of <code>Monitor</code>s.
 * <p>
 * A <code>CommandMonitor</code> object holds an <code>IndexedCommand</code>
 * notification and a target agent id, and sends the notification to the agent
 * when <code>start</code> is called. The associated <code>IndexedReport</code>
 * notification is forwarded back to the <code>Monitor</code> object by its
 * enclosing agent calling <code>commandReport</code>.
 */
public class CommandMonitor extends ObjectMonitor {

public static final String RCS_VERSION="@(#)$Id: CommandMonitor.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


  /** target agent for command */
  AgentId target;
  /** command to send to target */
  IndexedCommand command;

  /**
   * Constructor.
   *
   * @param parent	object to report status to
   * @param target	target agent for command
   * @param command	command to send to target
   */
  public CommandMonitor(MonitorParent parent,
			AgentId target,
			IndexedCommand command) {
    super(parent);
    this.target = target;
    this.command = command;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",target=" + target +
      ",command=" + command + ")";
  }

  /**
   * Starts monitor execution.
   */
  public void start() throws Exception {
    setStatus(Status.RUN);
    sendTo(this, target, command);
  }
  
  /**
   * Reacts to a the command report from target agent.
   *
   * @param report	report from agent
   */
  public void commandReport(IndexedReport report) throws Exception {
    switch (report.getStatus()) {
    case Status.DONE:
      setReturnValue(report);
      break;
    case Status.FAIL:
      setErrorMessage(report.getErrorMessage());
      break;
    }
    setStatus(report.getStatus());
  }
}
