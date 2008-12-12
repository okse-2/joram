/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2008 CNES
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
package org.objectweb.joram.mom.amqp;

import java.util.HashMap;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Notification;

public class NamingAgent extends Agent {
  
  public final static Logger logger = 
    fr.dyade.aaa.util.Debug.getLogger(NamingAgent.class.getName());
  
  private static NamingAgent singleton;
  
  public static NamingAgent getSingleton() {
    return singleton;
  }
  
  /**
   * TODO: Add a new stamp AgentId.AMQPNamingStamp
   */
  public final static int agentIdLocalStamp = AgentId.ControlTopicStamp;
  
  public static AgentId getDefault(short serverId) {
    return new AgentId(
      serverId, serverId, agentIdLocalStamp);
  }

  public static AgentId getDefault() {
    return getDefault(AgentServer.getServerId());
  }
  
  private transient String namingStorageName;
  
  private transient HashMap namingTable;

  /**
   * Fixed in memory to enable a
   * synchronous access.
   */
  public NamingAgent() {
    super("NamingAgent", true, agentIdLocalStamp);
  }
  
  public void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
    singleton = this;
    namingStorageName = "naming" + getId();
    namingTable = (HashMap) AgentServer.getTransaction().load(namingStorageName);
    if (namingTable == null) {
      namingTable = new HashMap();
    }
  }
  
  public void react(Notification not) throws Exception {
    // The persistent objects are not stored in the state
    // of this agent. See the method saveNaming().
    setNoSave();
  }
  
  public Object lookup(String name) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "NamingAgent.lookup(" + 
          name + ')');
    return namingTable.get(name);
  }
  
  public void bind(String name, Object ref) throws Exception {
    Object found = namingTable.get(name);
    if (found != null) throw new Exception("Already bound: " + name);
    namingTable.put(name, ref);
    saveNaming();
  }
  
  private void saveNaming() throws Exception {
    AgentServer.getTransaction().save(namingTable, namingStorageName);
  }

  public void unbind(String name) throws Exception {
    Object found = namingTable.remove(name);
    if (found != null) {
      saveNaming();
    }
  }

}
