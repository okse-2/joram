/*
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

import java.io.IOException;

/**
 * Context enabling an <code>AgentEngine</code> implemented in
 * another package to invoke operations that cannot be accessed 
 * outside of the <code>fr.dyade.aaa.agent</code> package.
 * An <code>AgentEngineContext</code> should be for the unique private use of its associated 
 * <code>AgentEngine</code> and should not be shared with any other components otherwise
 * the agent server security would be broken.
 */
public interface AgentEngineContext {

  /**
   * Creates an instance of <code>AgentFactory</code>.
   * @return an instance of <code>AgentFactory</code>
   * @throws IOException
   */
  Agent createAgentFactory() throws IOException;

  /**
   * Initializes an agent after creation. First time is true.
   * @param id the identifier of the agent to initialize
   * @param agent the agent to initialize
   * @throws Exception
   */
  void initializeAgent(AgentId id, Agent agent)
      throws Exception;

  /**
   * Sends a notification to the specified destination
   * with a local id.
   * @param to the notification destination
   * @param not the notification to send
   */
  void directSendTo(AgentId to, Notification not);
  
  /**
   * Initializes the agent's logger.
   * @param agent the agent to initialize
   * @throws Exception
   */
  void initAgentLogger(Agent agent) throws Exception;

  /**
   * Loads the specified agent.
   * @param id the identifier of the agent to load
   * @return the loaded agent
   * @throws IOException
   * @throws ClassNotFoundException
   */
  Agent loadAgent(AgentId id) throws IOException,
      ClassNotFoundException;

  /**
   * Sets the agent last reaction count.
   * @param ag the agent to modify
   * @param last the reaction count to set
   */
  void setAgentLast(Agent ag, long last);

  /**
   * Initializes a reloaded agent. First time is false.
   * @param agent
   * @throws Exception
   */
  void initializeReloadedAgent(Agent agent) throws Exception;

  /**
   * Sets the agent as to be saved.
   * @param agent the agent to update
   */
  void setSaveAgent(Agent agent);

  /**
   * Saves the agent
   * @param agent the agent to save
   * @throws IOException
   */
  void saveAgent(Agent agent) throws IOException;
  
  /**
   * Creates a message.
   * @param from the source of the message
   * @param to the destination of the message
   * @param not the notification to be transmitted by the message
   * @return
   */
  Message createMessage(AgentId from, AgentId to, Notification not);

  /**
   * Deletes and frees the specified message.
   * @param msg
   */
  void deleteMessage(Message msg);

  /**
   * Returns the local agent id.
   * @return the local agent id
   */
  AgentId getLocalAgentId();

  /**
   * Increments the reaction counter of
   * the specified agent.
   * @param ag the agent to update
   */
  void incReactNumber(Agent ag);

  /**
   * Validates the Channel.
   */
  void validateChannel();

  /**
   * Checks the message 'from'.
   * @param msg the message to check
   */
  void checkMessageFrom(Message msg);

  /**
   * Returns the specified <code>MessageConsumer</code>
   * @param id the identifier of the <code>MessageConsumer</code>
   * @return the specified <code>MessageConsumer</code>
   * @throws UnknownServerException
   */
  MessageConsumer getConsumer(short id)
      throws UnknownServerException;

  /**
   * Posts a message in the channel.
   * @param msg the message to post
   * @throws Exception
   */
  void channelPost(Message msg) throws Exception;

  /**
   * Saves the channel
   * @throws IOException
   */
  void saveChannel() throws IOException;

  /**
   * Stamps and saves the specified message.
   * @param msg the message to stamp and save
   * @param stamp the stamp to assign
   * @throws IOException
   */
  void stampAndSave(Message msg, int stamp)
      throws IOException;

}
