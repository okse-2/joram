/*
 * Copyright (C) 2001 - 2002 SCALAGENT
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, fr.dyade.aaa.ns,
 * fr.dyade.aaa.jndi and fr.dyade.aaa.joram, released September 11, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package fr.dyade.aaa.ns;

import java.io.*;
import java.util.*;
import java.rmi.*;

import fr.dyade.aaa.util.*;
import fr.dyade.aaa.agent.*;

import fr.dyade.aaa.ns.SimpleReport.Status;

/**
 * A <code>NameService</code> agent provides basic naming service functions,
 * associating agent identifiers with symbolic names.
 * There is no global naming service. Each <code>NameService</code> agent
 * defines a proper name space. One must then know what <code>NameService</code>
 * agent to call for a given name.
 * <p>
 * The naming service functions are provided by an agent. This means that they
 * may not be called directly as Java functions, and that notifications are
 * sent to the agent instead. This means also that the answer is not known
 * synchronously, and the the caller has to give its identity so that the
 * <code>NameService</code> agent is able to send back a reply at a later time.
 * The reply itself is a notification.
 * <p>
 * The requests to the <code>NameService</code> agent are
 * <code>SimpleCommand</code> notifications, and the replies are
 * <code>SimpleReport</code> notifications. Specialized commands are
 * <code>RegisterCommand</code>, <code>RegisterObject</code>, 
 * <code>BindObject</code>, <code>UnregisterCommand</code>, 
 * <code>LookupCommand</code>, <code>LookupObject</code> and 
 * <code>ListObject</code> notifications.
 * The regular reports are <code>SimpleReport</code> notifications. 
 * Specialized reports are <code>LookupReport</code>,
 * <code>LookupReportObject</code>, <code>BindReportObject</code> 
 * and <code>ListReportObject</code> notifications.
 * <p>
 * As <code>NameService</code> agents are agents, it is necessary to know
 * the <code>NameService</code> agent identifier to call it. However the
 * naming service itself is dedicated to providing agent identifiers from
 * external names. To resolve this seemingly contradictory statement, this class
 * provides two ways to retrieve <code>NameService</code> agent identifiers
 * apart from calling another <code>NameService</code> agent. Those are the
 * notions of default and named <code>NameService</code> agents.
 * <p>
 * A default <code>NameService</code> agent is associated with an agent server.
 * It is unique in an agent server, and is created when the
 * <code>NameService</code> service is declared in the agent servers
 * configuration file. This is done by adding the line
 * <dd><code>&lt;service class="fr.dyade.aaa.ns.NameService"/&gt;</code></dd><br>
 * in the server configuration block. Its identifier may be retrieved
 * using functions <code>getDefault</code>, the function with an agent
 * server id parameter providing the identifier of the default
 * <code>NameService</code> agent of that agent server.
 * <p>
 * A named <code>NameService</code> agent is created by the constructor with
 * a name parameter, and then deployed as any agent needs to be. The name
 * is interpreted as a file name where the agent identifier is stored.
 * The file name is local to the target agent server, and if it is not
 * absolute it is interpreted from the directory where the agent server
 * has been started.
 *
 * @see		RegisterCommand
 * @see		UnregisterCommand
 * @see		LookupCommand
 * @see		RegisterObject
 * @see		BindObject
 * @see		LookupObject
 * @see		ListObject
 * @see		SimpleReport
 */
public class NameService extends Agent {
  public static final String RCS_VERSION="@(#)$Id: NameService.java,v 1.7 2002-10-21 08:41:14 maistrfr Exp $";

  /** initializes service only once */
  private static boolean initialized = false;

  /**
   * Name of default name service, if any.
   * <p>
   * This name is used internally, as the agent name, but it has no
   * relation with the name service name as it may be set in a constructor,
   * or as it may be used in the functions <code>loadId</code> and
   * <code>remove</code>. Those names are used as file names, and the default
   * name service id is stored in no file.
   */
  protected static String defaultName = "defaultNameService";

  /**
   * Initializes the package as a well known service.
   * <p>
   * Creates a <code>NameService</code> agent with the well known stamp
   * <code>AgentId.NameServiceStamp</code>.
   *
   * @param args	parameters from the configuration file
   * @param firstTime	<code>true</code> when agent server starts anew
   *
   * @exception Exception
   *	unspecialized exception
   */
  public static void init(String args, boolean firstTime) throws Exception {
    if (initialized) return;
    initialized = true;

    if (! firstTime) return;

    NameService nameService = new NameService();
    nameService.deploy();
  }

  /**
   * Stop the service.
   */
  public static void stop () {
  }

  /**
   * Gets the identifier of the default name service in an agent server.
   *
   * @param serverId	id of agent server
   * @return		id of default name service agent
   *
   * @exception Exception
   *	unspecialized exception
   */
  public static AgentId getDefault(short serverId) {
    return new AgentId(serverId, serverId, AgentId.NameServiceStamp);
  }

  /**
   * Gets the identifier of the default name service in this agent server.
   *
   * @param serverId	id of agent server
   * @return		id of default name service agent
   *
   * @exception Exception
   *	unspecialized exception
   */
  public static AgentId getDefault() {
    return getDefault(AgentServer.getServerId());
  }


  /** keeps name/AgentId associations */
  private Hashtable table;

  /**
   * Creates the default name service agent with well known stamp
   * <code>AgentId.NameServiceStamp</code>.
   * <p>
   * There is no need for registering this agent id in a file.
   *
   * @exception IOException
   *	unspecialized exception
   */
  private NameService() throws IOException {
    super(defaultName, false, AgentId.NameServiceStamp);
    table = new Hashtable();
  }

  /**
   * Creates a local agent.
   *
   * If the agent name is null, its id is not registered in a file.
   *
   * @param name	file name to keep this agent id in, may be null
   *
   * @exception IOException
   *	unspecialized exception
   */
  public NameService(String name) throws IOException {
    if (name != null) {
      // check name not already used
      File file = new File(name);
      if (file.exists())
	throw new IllegalArgumentException(name + " file exists");
      FileWriter fw = new FileWriter(file);
      fw.write(getId().toString());
      fw.close();
    }
    
    table = new Hashtable();
  }

  /**
   * Gets an agent id from its name.
   *
   * @param name	file name provided at agent creation
   * @return		the agent identifier
   *
   * @exception IOException
   *	unspecialized exception
   */
  public static AgentId loadId(String name) throws IOException {
    File file = new File(name);
    BufferedReader fr = new BufferedReader(new FileReader(file));
    String line = fr.readLine();
    if (line == null)
      throw new EOFException();
    AgentId id = AgentId.fromString(line);
    fr.close();
    return id;
  }

  /**
   * Removes the file holding the agent id.
   *
   * @param name   file name provided at agent creation
   *
   * @exception IOException
   *	unspecialized exception
   */
  public static void remove(String name) throws IOException {
    if (name == null)
      return;

    File file = new File(name);
    file.delete();
  }

  
  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      "," + table.size() + table.toString() + ")";
  }

  /**
   * Reacts to <code>NameService</code> specific notifications.
   * Analyzes the notification type, then calls the appropriate
   * <code>doReact</code> function. By default calls <code>react</code>
   * from base class.
   * Handled notification types are :
   *	<code>RegisterCommand</code>,
   *	<code>UnregisterCommand</code>,
   *	<code>LookupCommand</code>,
   *	<code>RegisterObject</code>,
   *	<code>LookupObject</code>,
   *	<code>ListObject</code>
   *	<code>BindObject</code>
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof SimpleCommand) {
      try {
	if (not instanceof RegisterCommand) {
	  doReact((RegisterCommand) not);
	} else if (not instanceof UnregisterCommand) {
	  doReact((UnregisterCommand) not);
	} else if (not instanceof LookupCommand) {
	  doReact((LookupCommand) not);
	} else if (not instanceof RegisterObject) {
	  doReact((RegisterObject) not);
	} else if (not instanceof LookupObject) {
	  doReact((LookupObject) not);
	} else if (not instanceof ListObject) {
	  doReact((ListObject) not);
	} else if (not instanceof BindObject) {
	  doReact((BindObject) not);
	} else {
	  throw new IllegalArgumentException("unknown command");
	}
      } catch (Exception exc) {
	// report error to requesting agent
	sendTo(((SimpleCommand) not).getReport(),
	       new SimpleReport((SimpleCommand) not,
                                Status.FAIL,
                                exc.toString()));
      }
    } else {
      super.react(from, not);
    }
  }

  /**
   * Reacts to <code>RegisterCommand</code> notifications.
   * Calls <code>register</code>.
   *
   * @param not	   notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void doReact(RegisterCommand not) throws Exception {
    if (not.getRebind() == false) {
      Object agent = table.get(not.getName());
      if (agent != null) {
        if (agent instanceof AgentId) {
          AgentId client = ((SimpleCommand) not).getReport();
          if (! client.isNullId())
            sendTo(client, new LookupReport(not,
                                            Status.DONE,
                                            null,
                                            (AgentId) agent));
          return;
        } else {
          throw new AlreadyBoundException(not.getName() +
                                          " already exists into the database");
        }
      }
    }
    register(not.getName(), not.getAgent());
    AgentId client = ((SimpleCommand) not).getReport();
    if (! client.isNullId())
      sendTo(client, new SimpleReport(not, Status.DONE, null));
  }
  /**
   * Reacts to <code>RegisterObject</code> notifications.
   *
   * @param not  notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void doReact(RegisterObject not) throws Exception {
    table.put(not.getName(),not.getObject());
    AgentId client = ((SimpleCommand) not).getReport();
    if (! client.isNullId())
      sendTo(client, new SimpleReport(not, Status.DONE, null));
  }
  /**
   * Reacts to <code>BindObject</code> notifications.
   *
   * @param not  notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void doReact(BindObject not) throws Exception {
    String name = not.getName();
    Object o = table.get(name);
    if (o == null) {
      table.put(name,not.getObject());
      AgentId client = ((SimpleCommand) not).getReport();
      if (! client.isNullId())
        sendTo(client, new BindReportObject(not, Status.DONE, null));
    } else {
      AgentId client = ((SimpleCommand) not).getReport();
      if (! client.isNullId())
        sendTo(client,
               new BindReportObject(not,
                                    Status.FAIL,
                                    name + " is already bound."));
    }
  }

  /**
   * Reacts to <code>UnregisterCommand</code> notifications.
   * Calls <code>unregister</code>.
   *
   * @param not	  notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void doReact(UnregisterCommand not) throws Exception {
    unregister(not.getName());
    AgentId client = ((SimpleCommand) not).getReport();
    if (! client.isNullId())
      sendTo(client, new SimpleReport(not, Status.DONE, null));
  }

  /**
   * Reacts to <code>LookupCommand</code> notifications.
   * Calls <code>lookup</code>.
   *
   * @param not	  notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void doReact(LookupCommand not) throws Exception {
    AgentId agent = lookup(not.getName());
    AgentId client = ((SimpleCommand) not).getReport();
    if (! client.isNullId())
      sendTo(client, new LookupReport(not, Status.DONE, null, agent));
  }

  /**
   * Reacts to <code>LookupObject</code> notifications.
   *
   * @param not  notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void doReact(LookupObject not) throws Exception {
    Object obj = table.get(not.getName());
    if (obj == null) {
      throw new IllegalArgumentException("no value for " + not.getName());
    }
    AgentId client = ((SimpleCommand) not).getReport();
    if (! client.isNullId())
      sendTo(client, new LookupReportObject(not, Status.DONE, null, obj));
  }
  /**
   * Reacts to <code>ListObject</code> notifications.
   *
   * @param not  notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void doReact(ListObject not) throws Exception {
    Object obj;
    if (not.getName().length() == 0) {
      obj = table;
    } else {
      obj = table.get(not.getName());
    }
    if (obj == null) 
      throw new IllegalArgumentException("no value for " + name);
    AgentId client = ((SimpleCommand) not).getReport();
    if (! client.isNullId())
      sendTo(client, new ListReportObject(not, Status.DONE, null, obj));
  }
    
  /**
   * Registers <code>agent</code> with <code>name</code>.
   * Overwrites possibly existing value for <code>name</code>.
   *
   * @param name	any string
   * @param agent	not null agent id
   *
   * @exception Exception
   *	unspecialized exception
   */
  protected void register(String name, AgentId agent) throws Exception {
    table.put(name, agent);
  }

  /**
   * Unregisters value associated with <code>name</code>.
   * Does nothing if no associated value.
   *
   * @param name	any string
   *
   * @exception Exception
   *	unspecialized exception
   */
  protected void unregister(String name) throws Exception {
    table.remove(name);
  }

  /**
   * Looks up value associated with <code>name</code>.
   *
   * @param name	any string
   * @return		agent id registered with name
   *
   * @exception IllegalArgumentException
   *	if no value is registered with name
   * @exception Exception
   *	unspecialized exception
   */
  protected AgentId lookup(String name) throws Exception {
    Object agent = table.get(name);
    if (agent == null)
      throw new IllegalArgumentException("no value for " + name);
    return (AgentId) agent;
  }
}
