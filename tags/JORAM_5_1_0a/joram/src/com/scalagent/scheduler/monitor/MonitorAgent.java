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
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.*;
import com.scalagent.task.Task.Status;

/**
 * <code>Agent</code> which uses <code>Monitor</code>s to perform its task.
 * <p>
 * This objects maintains two lists for outgoing <code>IndexedCommand</code>s
 * issued by internal <code>Monitor</code>s, and for internal
 * <code>Monitor</code>s created to answer to incoming
 * <code>IndexedCommand</code>s.
 *
 * @see		Monitor
 */
public class MonitorAgent extends Agent implements MonitorParent {
  /** incoming commands */
  InCommandTable incoming;
  /** outgoing commands */
  OutCommandTable outgoing;

  /**
   * Creates an agent to be deployed remotely.
   *
   * @param to		agent server id where agent is to be deployed
   */
  public MonitorAgent(short to) {
    super(to);
    incoming = null;
    outgoing = null;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",incoming=" + incoming +
      ",outgoing=" + outgoing + ")";
  }

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
  public void sendTo(CommandMonitor monitor, AgentId to, IndexedCommand command) throws Exception {
    if (outgoing == null)
      outgoing = new OutCommandTable();
    OutCommandHandle handle = outgoing.addElement(to, monitor);
    // may have to duplicate the command if not done by Channel
    command.setId(handle.id);
    sendTo(to, command);
  }

  /**
   * Registers a <code>Monitor</code> object created to handle an
   * <code>IndexedCommand</code>
   *
   * @param id		command identifier local to source agent
   * @param source	command source agent
   * @param monitor	monitor handling command
   */
  protected void registerInMonitor(int id, AgentId source, Monitor monitor) {
    if (incoming == null)
      incoming = new InCommandTable();
    incoming.addElement(id, source, monitor);
  }
  
  /**
   * Reacts to a status change from child.
   *
   * @param child	child sending report
   * @param report	new child status
   */
  public void childReport(Monitor child, int status) throws Exception {
    if (status != Status.DONE && status != Status.FAIL)
      return;
    // search child in outgoing commands
    if (outgoing != null) {
      try {
	OutCommandHandle handle = outgoing.getElement(child);
	outgoing.removeElement(handle);
      } catch (IllegalArgumentException exc) {}
    }
    // search child in incoming commands
    if (incoming != null) {
      try {
	InCommandHandle handle = incoming.getElement(child);
	incoming.removeElement(handle);
	sendTo(handle.source,
	       new IndexedReport(
		 handle.id, status,
		 (status == Status.FAIL ? child.getErrorMessage() : null),
		 (status == Status.DONE ? child.getReturnValue() : null)));
      } catch (IllegalArgumentException exc) {}
    }
  }

  /**
   * Reacts to <code>IndexedReport</code> notifications corresponding to
   * <code>Monitor</code> <code>IndexedCommand</code>s.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void react(AgentId from, Notification not) throws Exception {
    try {
      if (not instanceof IndexedCommand) {
	// this should be handled by the derived classes
	logmon.log(BasicLevel.ERROR, getName() + ", unknown command in " +
                   toString() + ".react(" + not + ')');
	sendTo(from, new IndexedReport(((IndexedCommand) not).getId(),
				       Status.FAIL, "Unknown command", null));
        return;
      } else if (not instanceof IndexedReport) {
	IndexedReport report = (IndexedReport) not;
	int id = report.getCommand();
	if (id == 0) {
	  super.react(from, not);
	} else {
	  if (outgoing != null) {
	    OutCommandHandle handle = outgoing.getElement(id);
	    handle.monitor.commandReport(report);
	  } else {
	    throw new IllegalStateException("Command not found");
	  }
	}
        return;
      }
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", exception in " +
                 toString() + ".react(" + not + ')', exc);
      return;
    }
    super.react(from, not);
  }
}

/**
 * Table entry for managing incoming commands.
 *
 * @see	MonitorAgent 
 */
class InCommandHandle implements Serializable {
  /** command identifier local to source agent */
  int id;
  /** command source agent */
  AgentId source;
  /** monitor handling command */
  Monitor monitor;

  /**
   * Constructor.
   *
   * @param id		command identifier local to source agent
   * @param source	command source agent
   * @param monitor	monitor handling command
   */
  InCommandHandle(int id, AgentId source, Monitor monitor) {
    this.id = id;
    this.source = source;
    this.monitor = monitor;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",id=" + id +
      ",source=" + source + ")";
  }
}

/**
 * Table for managing incoming commands.
 * <p>
 * Table of <code>InCommandHandle</code> entries with various accessors by
 * value.
 *
 * @see	InCommandHandle 
 */
class InCommandTable implements Serializable {
  /** list of <code>InCommandHandle</code> entries */
  Vector list;

  /**
   * Constructor.
   */
  InCommandTable() {
    list = null;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",list=" + list + ")";
  }

  /**
   * Creates a new entry with all fields set and inserts it in this object.
   *
   * @param id		command identifier local to source agent
   * @param source	source agent identifier
   * @param monitor	monitor created to manage command
   * @return		the created entry
   */
  InCommandHandle addElement(int id, AgentId source, Monitor monitor) {
    InCommandHandle handle = new InCommandHandle(id, source, monitor);
    if (list == null)
      list = new Vector();
    list.addElement(handle);
    return handle;
  }

  /**
   * Removes entry.
   *
   * @param handle	entry to remove
   * @return	<code>true</code> if element was in vector,
   *		<code>false</code> otherwise
   */
  boolean removeElement(InCommandHandle handle) {
    if (list == null)
      return false;
    boolean ret = list.removeElement(handle);
    if (list.size() == 0)
      list = null;
    return ret;
  }

  /**
   * Gets an entry by id - agent.
   *
   * @param id		command identifier local to source agent
   * @param agent	command source agent
   * @return		the corresponding entry
   */
  InCommandHandle getElement(int id, AgentId agent) {
    if (list == null)
      throw new IllegalArgumentException();
    for (int i = 0; i < list.size(); i ++) {
      InCommandHandle handle = (InCommandHandle) list.elementAt(i);
      if (handle.id == id) {
	if (handle.source.equals(agent))
	  return handle;
      }
    }
    throw new IllegalArgumentException();
  }

  /**
   * Gets an entry by monitor.
   *
   * @param monitor	monitor created to manage command
   * @return		the corresponding entry
   */
  InCommandHandle getElement(Monitor monitor) {
    if (list == null)
      throw new IllegalArgumentException();
    for (int i = 0; i < list.size(); i ++) {
      InCommandHandle handle = (InCommandHandle) list.elementAt(i);
      if (handle.monitor == monitor)
	return handle;
    }
    throw new IllegalArgumentException();
  }
}

/**
 * Table entry for managing outgoing commands.
 *
 * @see	MonitorAgent 
 */
class OutCommandHandle implements Serializable {
  /** command identifier local to source agent */
  int id;
  /** command target agent */
  AgentId target;
  /** monitor issuing outgoing command */
  CommandMonitor monitor;

  /**
   * Constructor.
   *
   * @param id		command identifier local to source agent
   * @param target	command target agent
   * @param monitor	monitor issuing outgoing command
   */
  OutCommandHandle(int id, AgentId target, CommandMonitor monitor) {
    this.id = id;
    this.target = target;
    this.monitor = monitor;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",id=" + id +
      ",target=" + target + ")";
  }
}

/**
 * Table for managing outgoing commands.
 * <p>
 * Table of <code>OutCommandHandle</code> entries with various accessors by
 * value.
 * Includes member <code>count</code> to allocate command identifiers.
 * Identifier <code>0</code> is reserved to indicate a <code>null</code>
 * identifier.
 *
 * @see	OutCommandHandle 
 */
class OutCommandTable implements Serializable {
  /** list of <code>OutCommandHandle</code> entries */
  Vector list;
  /** top command identifier */
  int count;

  /**
   * Constructor.
   */
  OutCommandTable() {
    list = null;
    count = 0;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",list=" + list +
      ",count=" + count + ")";
  }

  /**
   * Creates a new entry and inserts it in this object.
   * Allocates a command identifier.
   *
   * @param target	target agent identifier
   * @param monitor	monitor issuing command
   * @return		the created entry
   */
  OutCommandHandle addElement(AgentId target, CommandMonitor monitor) {
    OutCommandHandle handle = new OutCommandHandle(++count, target, monitor);
    if (list == null)
      list = new Vector();
    list.addElement(handle);
    return handle;
  }

  /**
   * Removes entry.
   *
   * @param handle	entry to remove
   * @return	<code>true</code> if element was in vector,
   *		<code>false</code> otherwise
   */
  boolean removeElement(OutCommandHandle handle) {
    if (list == null)
      return false;
    boolean ret = list.removeElement(handle);
    if (list.size() == 0)
      list = null;
    return ret;
  }

  /**
   * Gets an entry by id.
   *
   * @param id	command identifier local to source agent
   * @return	the corresponding entry
   */
  OutCommandHandle getElement(int id) {
    if (list == null)
      throw new IllegalArgumentException();
    for (int i = 0; i < list.size(); i ++) {
      OutCommandHandle handle = (OutCommandHandle) list.elementAt(i);
      if (handle.id == id)
	return handle;
    }
    throw new IllegalArgumentException();
  }

  /**
   * Gets an entry by monitor.
   *
   * @param monitor	monitor issuing command
   * @return	the corresponding entry
   */
  OutCommandHandle getElement(Monitor monitor) {
    if (list == null)
      throw new IllegalArgumentException();
    for (int i = 0; i < list.size(); i ++) {
      OutCommandHandle handle = (OutCommandHandle) list.elementAt(i);
      if (handle.monitor == monitor)
	return handle;
    }
    throw new IllegalArgumentException();
  }
}
