/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
package com.scalagent.task;

import fr.dyade.aaa.agent.*;

/**
 * Notification providing a parameter to a <code>Task</code> agent prior
 * to a <code>Condition</code> notification allowing it to start.
 *
 * @see		Task
 */
public class ParamNotification extends Notification {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

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
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output buffer to fill in
   * @return resulting buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",parameter=").append(parameter);
    output.append(')');

    return output;
  }
}
