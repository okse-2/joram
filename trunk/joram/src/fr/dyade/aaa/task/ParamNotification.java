/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import fr.dyade.aaa.agent.*;


/**
 * Notification providing a parameter to a <code>Task</code> agent prior
 * to a <code>Condition</code> notification allowing it to start.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Task
 */
public class ParamNotification extends Notification {

public static final String RCS_VERSION="@(#)$Id: ParamNotification.java,v 1.1 2002-03-06 16:52:20 joram Exp $";

  /** parameter value */
  protected Object parameter;

  /**
   * Constructor.
   *
   * @param parameter		parameter value
   */
  public ParamNotification(Object parameter) {
    this.parameter = parameter;
  }

  
  /**
   * Property accessor.
   *
   * @result		parameter value
   */
  public Object getParameter() {
    return parameter;
  }

  /**
   * Provides a string image for this object.
   *
   * @result		string image for this object
   */
  public String toString() {
    return "(" +
      super.toString() +
      ",parameter=" + parameter + ")";
  }
}
