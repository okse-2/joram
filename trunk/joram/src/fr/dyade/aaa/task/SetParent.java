/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import fr.dyade.aaa.agent.*;


/**
 * Notification configuring a <code>Task</code> agent parent.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Task
 */
public class SetParent extends Notification {

public static final String RCS_VERSION="@(#)$Id: SetParent.java,v 1.1 2002-03-06 16:52:20 joram Exp $";

  /** id of task parent */
  public AgentId parent;

  /**
   * Constructor.
   *
   * @param parent	id of task parent
   */
  public SetParent(AgentId parent) {
    this.parent = parent;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",parent=" + parent + ")";
  }
}
