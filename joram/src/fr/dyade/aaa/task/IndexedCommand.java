/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import java.io.*;
import fr.dyade.aaa.agent.*;


/**
  * Notification describing a service task.
  * The task is to be performed by a <code>MonitorAgent</code> agent.
  * Task end is reported via an <code>IndexedReport</code> notification.
  * <p>
  * This class is a variation of <code>Command</code>, which it may eventually
  * replace.
  *
  * @author	Lacourte Serge
  * @version	v1.0
  *
  * @see	IndexedReport
  * @see	Monitor
  * @see	Command
  */
public class IndexedCommand extends Notification {

public static final String RCS_VERSION="@(#)$Id: IndexedCommand.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


  /** identifier local to agent issuing command */
  private int id;

  /**
   * Creates a notification to be sent.
   */
  public IndexedCommand() {
    this.id = 0;
  }

  /**
   * Accesses property.
   * Allows differed setting of <code>id</code> variable.
   * Does not allow changing the variable.
   *
   * @param id		command identifier
   */
  public void setId(int id) throws Exception {
    if (this.id != 0) {
      throw new IllegalArgumentException("cannot change id: " + this);
    }
    this.id = id;
  }

  /**
   * Accesses property.
   *
   * @return		command identifier
   */
  public int getId() { return id; }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",id=" + id + ")";
  }
}
