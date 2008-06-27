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
package com.scalagent.scheduler.monitor;

import java.io.*;

import fr.dyade.aaa.agent.*;

/**
 * A <code>Monitor</code> object is similar to a <code>Task</code> agent,
 * except that it may be an object internal to an agent. Its reactions are
 * executed directly as function calls, not indirectly via notifications.
 * <p>
 * The main use of <code>Monitor</code> objects is to provide for handling an
 * asynchronous <code>IndexedCommand</code> <code>IndexedReport</code>
 * communication without creating additional and temporary
 * <code>ServiceTask</code> or <code>Composed</code> agents. In this context
 * the <code>IndexedCommand</code> is uniquely identified by the identifier of
 * the agent issuing the command and an identifier local to that agent.
 * <p>
 * A <code>Monitor</code> object refers to a parent <code>MonitorParent</code>
 * object which it reports status changes to by calling its parent
 * <code>childReport</code> function. The eventual parent of a
 * <code>Monitor</code> object is the object's enclosing
 * <code>MonitorAgent</code> agent.
 *
 * @see	MonitorParent
 * @see	MonitorAgent
 * @see	IndexedCommand
 */
public interface Monitor extends Serializable {
  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  String toString();

  /**
   * Property accessor.
   *
   * @return		optional error message
   */
  public String getErrorMessage();

  /**
   * Property accessor.
   *
   * @return		optional return value
   */
  public Object getReturnValue();

  /**
   * Starts monitor execution.
   */
  void start() throws Exception;
}
