/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import java.io.*;
import fr.dyade.aaa.agent.*;


/**
 * Notification describing a service task.
 * The task is to be performed by an agent, as for a <code>Task</code> agent,
 * except that the performing agent may react to multiple commands, and
 * the particular task is identified by this notification and not by an agent.
 * Task end is reported via a <code>Report</code> notification.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Report
 */
public class Command extends Notification {

public static final String RCS_VERSION="@(#)$Id: Command.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


  /** agent to report completion status to */
  private AgentId report;

  /**
   * Default constructor.
   */
  public Command() {
    this(null);
  }

  /**
   * Creates a notification to be sent.
   *
   * @param report	agent to report status to
   */
  public Command(AgentId report) {
    this.report = report;
  }

  /**
   * Accesses property.
   * Allows differed setting of report variable.
   * Does not allow changing the variable.
   *
   * @param report	agent to report status to
   */
  public void setReport(AgentId report) throws Exception {
    if (this.report != null) {
      throw new IllegalArgumentException("cannot change report: " + this);
    }
    this.report = report;
  }

  /**
   * Accesses property.
   *
   * @return		agent to report status to
   */
  public AgentId getReport() { return report; }


  /**
   * Provides a string image for this object.
   *
   * @return		a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",report=" + report + ")";
  }
}
