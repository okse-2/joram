/*
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */


package fr.dyade.aaa.agent;

import java.io.*;
import java.util.*;

/**
 * The <code>AgentVector</code> class. This class should be completed to
 * reflected the totally of Vector interface, then it should be public.
 *
 * @author  Andre Freyssinet
 * @version 1.0, 12/10/97
 */
final class AgentVector extends AgentObject {

  /** RCS version number of this file: $Revision: 1.2 $ */
  public static final String RCS_VERSION="@(#)$Id: AgentFactory.java,v 1.2 2000-08-01 09:13:26 tachkeni Exp $";

  /**
   * Determines if the currently <code>AgentVector</code> has been modified
   * since last save.
   */
  protected boolean isModified;
  /** The <code>Vector</code> into which the components are stored. */
  protected Vector vector;

  /**
   * Allocates an empty <code>AgentVector</code>.
   *
   * @param name	symbolic name
   */
  public AgentVector(String name) {
    super(name);
    isModified = true;
    vector = new Vector();
  }
  /**
   * Adds the specified component to the end of this vector.
   * The <code>AgentVector</code> is tagged modified.
   *
   * @param obj	the component to be added.
   */
  public final void addElement(Object obj) {
    isModified = true;
    vector.addElement(obj);
  }

  /**
   * Removes the first (lowest-indexed) occurrence of the argument from
   * this vector. The <code>AgentVector</code> is tagged modified.
   *
   * @param obj	the component to be removed.
   * @return	<code>true</code>if the argument was a component of
   *		this vector; <code>false</code> otherwise.
   */
  public final boolean removeElement(Object obj) {
    isModified = true;
    return vector.removeElement(obj);
  }

  /**
   * Returns an enumeration of the components of this vector. The returned
   * <code>Enumeration</code> object will generate all items in this vector.
   *
   * @return an enumeration of the components of this vector.
   */
  public final Enumeration elements() {
    return vector.elements();
  }

  /**
   * Saves the <code>AgentVector</code> object. Be careful, this method
   * should only be used in the <code>save</code> method of including Agent
   * in order to preserve the atomicity. The vector is only saved if it has
   * modified since last save.
   *
   * @exception Exception	unspecialized exception
   */
  void save() throws IOException {
    if (isModified) {
      super.save();
      isModified = false;
    }
  }
}

/**
 * <code>Agent</code> used to allow remote agent creation. Every agent
 * server hosts a factory agent, they all use a predefined stamp identifier
 * <code>AgentId.factoryId</code>. The factory must be able to create all
 * types of objects, actually it is supposed that the corresponding classes
 * can be reached.<p>
 * The agent creation process involves the following steps
 * <ul>
 * <li>locally creating the <code>Agent</code> object in memory ;
 * <li>serializing the agent state, building an <code>AgentCreateRequest</code>
 * 	notification with the resulting string, sending it to the target
 * 	Factory agent ;
 * <li>the factory agent building the object in memory from the serialized
 * 	image, and saving it into operational storage ;
 * <li>the factory agent calling the initialize method.
 * </ul>
 * The AgentDeleteRequest class of notification follows a similar process
 * to remotely delete agents.
 *
 * @author  Andre Freyssinet
 */
public final class AgentFactory extends Agent {

  /** RCS version number of this file: $$ */
  public static final String RCS_VERSION="@(#)$Id: AgentFactory.java,v 1.2 2000-08-01 09:13:26 tachkeni Exp $";

  /** Persistent vector containing id's of all fixed agents. */
  private transient AgentVector fixedAgentIdList;

  /** Used for server monitoring. */
  public static Vector agentsList;
  /** Used for server monitoring. */
  private static AdminEventReactor adminEventReactor;

  /**
   * Allocates a new <code>AgentFactory</code> agent.
   * An <code>AgentFactory</code> agent must be created on every agent
   * server the first time it runs.
   */
  public AgentFactory() {
    super("AgentFactory#" + Server.serverId,
	  true,
	  AgentId.factoryId);
    if (Server.ADMINISTRED)
        agentsList = new Vector();
  }

  /**
   * Gives this agent an opportunity to initialize after having been
   * deployed, and each time it is loaded into memory. Loads the list of
   * agents with a <code>fixed</code> field set to <code>true</code>.
   *
   * @param firstTime		true when first called by the factory
   *
   * @exception Exception
   *	unspecialized exception
   */
  protected void initialize(boolean firstTime) throws Exception {
    if (firstTime)
      fixedAgentIdList = new AgentVector("fixedAgentIdList");
    else
      fixedAgentIdList = (AgentVector) AgentObject.load("fixedAgentIdList");
    super.initialize(firstTime);
  }

  /**
   * Returns an enumeration of <code>AgentId</code> for all fixed agents
   * of this agent server.
   *
   * @return an enumeration of id for all fixed agents.
   */
  Enumeration getFixedAgentIdList() {
    return fixedAgentIdList.elements();
  }

  /**
   * Removes an <code>AgentId</code> in the <code>fixedAgentIdList</code>
   * <code>Vector</code>.
   *
   * @param id	the <code>AgentId</code> of no more used fixed agent.
   */
  void removeFixedAgentId(AgentId id) throws IOException {
    fixedAgentIdList.removeElement(id);
    // If the server is transient, save it now.
    if (Server.isTransient(Server.getServerId()))
      fixedAgentIdList.save();
  }

  /**
   * Adds an <code>AgentId</code> in the <code>fixedAgentIdList</code>
   * <code>Vector</code>.
   *
   * @param id	the <code>AgentId</code> of new fixed agent.
   */
  void addFixedAgentId(AgentId id) throws IOException {
    fixedAgentIdList.addElement(id);
    // If the server is transient, save it now.
    if (Server.isTransient(Server.getServerId()))
      fixedAgentIdList.save();
  }

  /**
   * Commit changed happened during AgentFactory reaction.
   */
  void save() throws IOException {
    // Save AgentFactory state.
    super.save();
    // Save fixed agent id. list.
    fixedAgentIdList.save();
  }

  /**
   * Reacts to notifications ... .
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof AgentCreateRequest) {
      try {
	// Restore the new agent state.
	byte[] agentState = ((AgentCreateRequest) not).agentState;
	ByteArrayInputStream is = new ByteArrayInputStream(agentState, 0, agentState.length);
	ObjectInputStream ois = new ObjectInputStream(is);
	Agent ag = (Agent) ois.readObject();
	is.close();

	createAgent(ag);

	if (Debug.createAgent)
	  Debug.trace(name + " creates " + ag, false);

	if (((AgentCreateRequest) not).reply != null)
	  sendTo(((AgentCreateRequest) not).reply,
		 new AgentCreateReply(ag.getId()));

	if (Server.ADMINISTRED){
	  AgentDesc desc = new AgentDesc(ag.name,ag.getId(),ag.isFixed());
	  if (Server.admin && (adminEventReactor != null) &&
	      adminEventReactor.hasListeners(ServerEventType.AGENT_CREATED)) {
	    if (Debug.admin)
	      Debug.trace(name + "send Add" + desc, false);
	    adminEventReactor.factoryEventReact(desc,ServerEventType.AGENT_CREATED);
	  }
	  agentsList.addElement(desc);
	}
      } catch (Exception exc) {
	if (Debug.debug && Debug.error)
	  Debug.trace(name + " creation failed.", exc);
	//  If there is an explicit reply request send it the
	// ExceptionNotification to the requester else to the
	// sender.
	((AgentCreateRequest) not).agentState = null;
	if (((AgentCreateRequest) not).reply != null) {
	  sendTo(((AgentCreateRequest) not).reply,
		 new ExceptionNotification(getId(), not, exc));
	} else {
	  sendTo(from,
		 new ExceptionNotification(getId(), not, exc));
	}
      }
    } else if (not instanceof AgentDeleteRequest) {
      Agent ag;
      try {
	ag = Agent.load(from);
	Server.transaction.delete(ag.id.toString());
      } catch (UnknownAgentException exc) {
	sendTo(from,
	       new ExceptionNotification(getId(),
					 not,
					 new Exception("Unknown agent" + from)));
	return;
      } catch (Exception exc) {
	sendTo(from,
	       new ExceptionNotification(getId(),
					 not,
					 new Exception("Can't delete agent" + from)));
	return;
      }
      if (ag.isFixed()) {
	removeFixedAgentId(ag.id);
      }
      agents.remove(ag.getId());

      if (Server.ADMINISTRED){
	AgentDesc desc = new AgentDesc(ag.name,ag.getId(),ag.isFixed());
	if (Server.admin && (adminEventReactor != null) && 
	    adminEventReactor.hasListeners(ServerEventType.AGENT_DELETED)) {
	  if (Debug.admin)
	    Debug.trace(name + "send Removed" + desc, false);
	  adminEventReactor.factoryEventReact(desc,ServerEventType.AGENT_DELETED);
	}
	agentsList.removeElement(desc);
      } 

    } else {
      try {
	super.react(from, not);
      } catch (Exception exc) {
	sendTo(from,
	       new ExceptionNotification(getId(), not, exc));
      }
    }
  }

  /**
   * Creates and initializes an agent in the local agent server.
   * <p>
   * This function is private to the package and is used only in the static
   * <code>init</code> function of the <code>Agent</code> class to create
   * system agents.
   *
   * @param agent	agent object to create
   *
   * @exception Exception
   *	unspecialized exception
   */
  void createAgent(Agent agent) throws Exception {
    if (agent.isFixed()) {
      // Subscribe the agent in pre-loading list.
      addFixedAgentId(agent.getId());
    }
    // Initialize the agent
    agent.initialize(true);
    agent.save();

    // Memorize the agent creation and ...
    now += 1;
    if (agents.size() > (NbMaxAgents + nbFixedAgents))
      garbage();
    
    agents.put(agent.getId(), agent);
  }

  /**
   * set the EventReactor (now the UdpAdminProxy).
   */
  public static void setEventReactor(AdminEventReactor aer){
    adminEventReactor = aer;
  }
}

