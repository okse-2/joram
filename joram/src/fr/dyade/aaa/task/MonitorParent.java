/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 */

package fr.dyade.aaa.task;

import java.io.*;
import fr.dyade.aaa.agent.*;


/**
  * Interface which a <code>Monitor</code>'s parent satisfies.
  *
  * @author	Lacourte Serge
  * @version	v1.0
  *
  * @see Monitor
  */
public interface MonitorParent {

public static final String RCS_VERSION="@(#)$Id: MonitorParent.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 

  
  /**
    * Reacts to a status change from child.
    *
    * @param child	child changing status
    * @param status	new child status
    */
  public void childReport(Monitor child, int status) throws Exception;

  /**
    * Allows a enclosed <code>CommandMonitor</code> object to send an
    * <code>IndexedCommand</code>.
    * Registers the object so that it can be forwarded the
    * <code>IndexedReport</code> to.
    *
    * @param monitor	object sending the command
    * @param to		agent target of command
    * @param command	command to send
    */
  public void sendTo(CommandMonitor monitor, AgentId to, IndexedCommand command) throws Exception;
}
