/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import fr.dyade.aaa.agent.*;
import java.io.*;


/**
 * <code>TaskHandle</code> structure used in the <code>Delegating</code> class.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see	Delegating
 */
public class KTaskHandle extends TaskHandle {

public static final String RCS_VERSION="@(#)$Id: KTaskHandle.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 


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
