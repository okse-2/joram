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

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>AgentVector</code> class. This class should be completed to
 * reflected the totally of Vector interface, then it should be public.
 */
final class AgentVector extends AgentObject {
  /** RCS version number of this file: $Revision: 1.11 $ */
  public static final String RCS_VERSION="@(#)$Id: AgentFactory.java,v 1.11 2002-10-21 08:41:13 maistrfr Exp $";

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
 */
final class AgentFactory extends Agent {
  /** RCS version number of this file: $Revision: 1.11 $ */
  public static final String RCS_VERSION="@(#)$Id: AgentFactory.java,v 1.11 2002-10-21 08:41:13 maistrfr Exp $";

  /** Persistent vector containing id's of all fixed agents. */
  private transient AgentVector fixedAgentIdList;

//   /** Used for server monitoring. */
//   public static Vector agentsList;
//   /** Used for server monitoring. */
//   private static AdminEventReactor adminEventReactor;

  /**
   * Allocates a new <code>AgentFactory</code> agent.
   * An <code>AgentFactory</code> agent must be created on every agent
   * server the first time it runs.
   */
  AgentFactory() {
    super("AgentFactory#" + AgentServer.getServerId(),
	  true,
	  AgentId.factoryId);
  }


  /**
   * Returns log topic for factory agent.
   */
  protected String getLogTopic() {
    return Debug.A3Agent + ".AgentFactory.#" + AgentServer.getServerId();
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
    AgentServer.engine.nbFixedAgents = fixedAgentIdList.vector.size();
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
    AgentServer.engine.nbFixedAgents -= 1;
    // If the server is transient, save it now.
    if (AgentServer.isTransient())
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
    AgentServer.engine.nbFixedAgents += 1;
    // If the server is transient, save it now.
    if (AgentServer.isTransient())
      fixedAgentIdList.save();
  }

  /**
   * Commit changed happened during AgentFactory reaction.
   */
  protected void save() throws IOException {
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
      AgentCreateRequest cnot = (AgentCreateRequest) not;
      try {
	// Restore the new agent state.
	ObjectInputStream ois =
	  new ObjectInputStream(
	    new ByteArrayInputStream(
	      cnot.agentState, 0, cnot.agentState.length));
	Agent ag = (Agent) ois.readObject();
	try {
	  ois.close();
	} catch (IOException exc) {}
        
        createAgent(ag);

        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     "AgentFactory" + id +
                     ", create Agent" + ag.id + " [" + ag.name + "]");
	if (cnot.reply != null) {
          AgentCreateReply reply = new AgentCreateReply(ag.getId());
          reply.setContext(cnot.getContext());
	  sendTo(cnot.reply, reply);          
        }
      } catch (Exception exc) {
 	//  If there is an explicit reply request send it the
	// ExceptionNotification to the requester else to the
	// sender.
	cnot.agentState = null;
        logmon.log(BasicLevel.ERROR,
                   "AgentFactory" + id + ", can't create Agent" + cnot.deploy,
                   exc);
        ExceptionNotification excNot = 
          new ExceptionNotification(getId(), cnot, exc);
        excNot.setContext(cnot.getContext());
	if (cnot.reply != null) {
	  sendTo(cnot.reply, excNot);
	} else {
	  sendTo(from, excNot);
	}
      }
    } else if (not instanceof AgentDeleteRequest) {
      try {
        deleteAgent(from);
	if (((AgentDeleteRequest) not).reply != null)
          sendTo(((AgentDeleteRequest) not).reply, new DeleteAck(from));
      } catch (Exception exc) {
	if (((AgentDeleteRequest) not).reply != null)
          sendTo(((AgentDeleteRequest) not).reply, new DeleteAck(from, exc));
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
   * Deletes an agent in the local agent server.
   */
  void deleteAgent(AgentId from) throws Exception {
    Agent ag;
    try {
      ag = AgentServer.engine.load(from);
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "AgentFactory" + id +
                   ", delete Agent" + ag.id + " [" + ag.name + "]");
      AgentServer.transaction.delete(ag.id.toString());
    } catch (UnknownAgentException exc) {
      logmon.log(BasicLevel.ERROR,
                 "AgentFactory" + id +
                 ", can't delete unknown Agent" + from);
      throw new Exception("Can't delete unknown Agent" + from);
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR,
                 "AgentFactory" + id + ", can't delete Agent" + from, exc);
      throw new Exception("Can't delete Agent" + from);
    }
    if (ag.isFixed()) removeFixedAgentId(ag.id);
    AgentServer.engine.agents.remove(ag.getId());
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
    agent.deployed = true;
    if (agent.isFixed()) {
      // Subscribe the agent in pre-loading list.
      addFixedAgentId(agent.getId());
    }
    // Initialize the agent
    agent.initialize(true);
    if (agent.logmon == null)
      agent.logmon = Debug.getLogger(fr.dyade.aaa.agent.Debug.A3Agent +
                                     ".#" + AgentServer.getServerId());;
    agent.save();

    // Memorize the agent creation and ...
    AgentServer.engine.now += 1;
    if (AgentServer.engine.agents.size() > (AgentServer.engine.NbMaxAgents + AgentServer.engine.nbFixedAgents))
      AgentServer.engine.garbage();
    
    AgentServer.engine.agents.put(agent.getId(), agent);
  }
}

