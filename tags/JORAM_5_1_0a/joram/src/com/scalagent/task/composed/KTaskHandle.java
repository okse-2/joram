/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
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
package com.scalagent.task.composed;

import java.io.*;

import fr.dyade.aaa.agent.*;
import com.scalagent.task.*;

/**
 * <code>TaskHandle</code> structure used in the <code>Delegating</code> class.
 *
 * @see	Delegating
 */
public class KTaskHandle extends TaskHandle {
  /** sub-task key, provided by parent */
  public int key;

  /**
   * Constructor.
   * Initializes status to <code>NONE</code>.
   */
  public KTaskHandle(AgentId id, int key) {
    super(id);
    this.key = key;
  }

  /**
   * Constructor with default value <code>0</code> for <code>key</code>.
   * Initializes status to <code>NONE</code>.
   */
  public KTaskHandle(AgentId id) {
    this(id, 0);
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + id.toString() + ",key=" + key + ")";
  }
}
