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

import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.EncodableFactoryRepository;

/**
 * Context enabling an <code>AgentEngine</code> implemented in
 * another package to invoke operations that cannot be accessed 
 * outside of the <code>fr.dyade.aaa.agent</code> package.
 * An <code>AgentEngineContext</code> should be for the unique private use of its associated 
 * <code>AgentEngine</code> and should not be shared with any other components otherwise
 * the agent server security would be broken.
 */
class AgentEngineContextImpl implements AgentEngineContext {
  
  private static EncodableFactory messageFactory = EncodableFactoryRepository
      .getFactory(AgentServer.MESSAGE_CLASS_ID);
  
  /**
   * Package constructor not accessible from outside this package.
   */
  AgentEngineContextImpl() {}

  /**
   * Creates an instance of <code>AgentFactory</code>.
   * @return an instance of <code>AgentFactory</code>
   * @throws IOException
   */
  public Agent createAgentFactory() throws IOException {
    AgentFactory factory = new AgentFactory(AgentId.factoryId);
    factory.save();
    return factory;
  }

  /**
   * Initializes an agent after creation. First time is true.
   * @param id the identifier of the agent to initialize
   * @param agent the agent to initialize
   * @throws Exception
   */
  public void initializeAgent(AgentId id, Agent agent)
      throws Exception {
    agent.id = id;
    agent.deployed = true;
    agent.agentInitialize(true);
  }

  /**
   * Sends a notification to the specified destination
   * with a local id.
   * @param to the notification destination
   * @param not the notification to send
   */
  public void directSendTo(AgentId to, Notification not) {
    Channel.channel.directSendTo(AgentId.localId, to, not);
  }

  /**
   * Initializes the agent's logger.
   * @param agent the agent to initialize
   * @throws Exception
   */
  public void initAgentLogger(Agent agent) throws Exception {
    if (agent.logmon == null)
      agent.logmon = Debug.getLogger(Agent.class.getName());
  }

  /**
   * Loads the specified agent.
   * @param id the identifier of the agent to load
   * @return the loaded agent
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public Agent loadAgent(AgentId id) throws IOException,
      ClassNotFoundException {
    return Agent.load(id);
  }

  /**
   * Sets the agent last reaction count.
   * @param ag the agent to modify
   * @param last the reaction count to set
   */
  public void setAgentLast(Agent ag, long last) {
    ag.last = last;
  }

  /**
   * Initializes a reloaded agent. First time is false.
   * @param agent
   * @throws Exception
   */
  public void initializeReloadedAgent(Agent agent) throws Exception {
    agent.agentInitialize(false);
  }

  /**
   * Sets the agent as to be saved.
   * @param agent the agent to update
   */
  public void setSaveAgent(Agent agent) {
    agent.setSave();
  }

  /**
   * Saves the agent
   * @param agent the agent to save
   * @throws IOException
   */
  public void saveAgent(Agent agent) throws IOException {
    agent.save();
  }

  /**
   * Creates a message.
   * @param from the source of the message
   * @param to the destination of the message
   * @param not the notification to be transmitted by the message
   * @return
   */
  public Message createMessage(AgentId from, AgentId to, Notification not) {
    Message msg = (Message) messageFactory.createEncodable();
    msg.from = from;
    msg.to = to;
    if (not != null) {
      msg.not = not;
      msg.not.detached = not.detached;
      msg.not.messageId = not.messageId;
    }
    return msg;
  }
  
  /**
   * Deletes and frees the specified message.
   * @param msg
   */
  public void deleteMessage(Message msg) {
    msg.delete();
    msg.free();
  }

  /**
   * Returns the local agent id.
   * @return the local agent id
   */
  public AgentId getLocalAgentId() {
    return AgentId.localId;
  }

  /**
   * Increments the reaction counter of
   * the specified agent.
   * @param ag the agent to update
   */
  public void incReactNumber(Agent ag) {
    ag.reactNb += 1;
  }

  /**
   * Validates the Channel.
   */
  public void validateChannel() {
    Channel.validate();
  }

  /**
   * Checks the message 'from'.
   * @param msg the message to check
   */
  public void checkMessageFrom(Message msg) {
    if (msg.from == null)
      msg.from = AgentId.localId;
  }

  /**
   * Returns the specified <code>MessageConsumer</code>
   * @param id the identifier of the <code>MessageConsumer</code>
   * @return the specified <code>MessageConsumer</code>
   * @throws UnknownServerException
   */
  public MessageConsumer getConsumer(short id)
      throws UnknownServerException {
    return AgentServer.getConsumer(id);
  }

  /**
   * Posts a message in the channel.
   * @param msg the message to post
   * @throws Exception
   */
  public void channelPost(Message msg) throws Exception {
    Channel.post(msg);
  }

  /**
   * Saves the channel
   * @throws IOException
   */
  public void saveChannel() throws IOException {
    Channel.save();
  }

  /**
   * Stamps and saves the specified message.
   * @param msg the message to stamp and save
   * @param stamp the stamp to assign
   * @throws IOException
   */
  public void stampAndSave(Message msg, int stamp)
      throws IOException {
    msg.source = AgentServer.getServerId();
    msg.dest = AgentServer.getServerId();
    msg.stamp = stamp;
    msg.save();
  }

}
