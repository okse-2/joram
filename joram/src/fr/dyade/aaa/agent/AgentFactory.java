/*
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.objectweb.util.monolog.api.BasicLevel;

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
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * Allocates a new <code>AgentFactory</code> agent.
   * An <code>AgentFactory</code> agent must be created on every agent
   * server the first time it runs.
   */
  AgentFactory(AgentId factoryId) {
    super("AgentFactory#" + AgentServer.getServerId(),
	  true,
	  factoryId);
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
  protected void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
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

        // Initializes and creates the agent
// TODO: (ThreadEngine) Thread.currentThread() ...
        AgentServer.engine.createAgent(cnot.deploy, ag);

        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     "AgentFactory" + id +
                     ", create Agent" + ag.id + " [" + ag.name + "]");
	if (cnot.reply != null) {
          AgentCreateReply reply = new AgentCreateReply(ag.getId());
          reply.setContext(cnot.getContext());
	  sendTo(cnot.reply, reply);          
        }
      } catch (Throwable error) {
 	//  If there is an explicit reply request send it the
	// ExceptionNotification to the requester else to the
	// sender.
	cnot.agentState = null;
        logmon.log(BasicLevel.ERROR,
                   "AgentFactory" + id + ", can't create Agent" + cnot.deploy,
                   error);
        ExceptionNotification excNot = 
          new ExceptionNotification(
            getId(), cnot, new AgentException(error));
        excNot.setContext(cnot.getContext());
	if (cnot.reply != null) {
	  sendTo(cnot.reply, excNot);
	} else {
	  sendTo(from, excNot);
	}
      }
    } else if (not instanceof AgentDeleteRequest) {
      try {
// TODO: (ThreadEngine) Thread.currentThread() ...
        AgentServer.engine.deleteAgent(from);
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
}

