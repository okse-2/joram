/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

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
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see	MonitorParent
 * @see	MonitorAgent
 * @see	IndexedCommand
 */
public interface Monitor extends Serializable {

public static final String RCS_VERSION="@(#)$Id: Monitor.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


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
