/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */
package fr.dyade.aaa.task;

import java.io.*;

import fr.dyade.aaa.agent.*;

/**
 * Structure to keep management data associated with a sub-task.
 *
 * @see	Composed
 */
public class TaskHandle implements Serializable {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: TaskHandle.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 

  /** sub-task identifier */
  public AgentId id;
  /** sub-task status, as known by parent */
  public int status;
  /** sub-task result, when status is DONE */
  public Object result;

  /**
   * Initializes object to be decoded.
   */
  public TaskHandle() {
    this(null);
  }

  /**
   * Initializes status to <code>NONE</code> and result to null.
   *
   * @param id		sub-task identifier
   */
  public TaskHandle(AgentId id) {
    this.id = id;
    status = Task.Status.NONE;
    result = null;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	string image for this object
   */
  public String toString() {
    return "(" +
      "id=" + id +
      ",status=" + Task.Status.toString(status) +
      ",result=" + result + ")";
  }
}
