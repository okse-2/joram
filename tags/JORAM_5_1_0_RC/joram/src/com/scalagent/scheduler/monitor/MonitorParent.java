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
  * Interface which a <code>Monitor</code>'s parent satisfies.
  *
  * @see Monitor
  */
public interface MonitorParent {
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
