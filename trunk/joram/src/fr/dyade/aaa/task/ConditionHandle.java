/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import fr.dyade.aaa.agent.*;
import java.io.*;


/**
 * Structure to keep management data associated with a task execution condition.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see	Task
 */
public class ConditionHandle implements Serializable {

public static final String RCS_VERSION="@(#)$Id: ConditionHandle.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


  /** condition name, may be <code>null</code> */
  public String name;
  /** agent sending condition */
  public AgentId id;
  /** condition status */
  public boolean status;

  /**
   * Initializes object to be decoded.
   */
  public ConditionHandle() {
    this(null, null);
  }

  /**
   * Constructor.
   * Initializes <code>status</code> to <code>false</code>.
   *
   * @param name	condition name, may be <code>null</code>
   * @param id		agent sending condition
   */
  public ConditionHandle(String name, AgentId id) {
    this.name = name;
    this.id = id;
    status = false;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" +
      "name=" + name + "," +
      "id=" + id + "," +
      "status=" + status + ")";
  }
}
