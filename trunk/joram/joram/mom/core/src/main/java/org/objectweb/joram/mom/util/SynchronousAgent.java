/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
 */
package org.objectweb.joram.mom.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.objectweb.joram.mom.dest.AdminTopic;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.FwdAdminRequestNot;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.CreateDestinationRequest;
import org.objectweb.joram.shared.admin.CreateUserRequest;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.common.Debug;

public class SynchronousAgent extends Agent {
  public static Logger logger = Debug.getLogger(SynchronousAgent.class.getName());
  
  static private SynchronousAgent INSTANCE = null;
  
  //TODO: Doesn't work. Might be useless because this agent is to be merged to AdminTopic
//  static private Object monitor = new Object();
  
  static public synchronized SynchronousAgent getSynchronousAgent()
      throws IOException, InterruptedException {
    if(INSTANCE == null) {
      SynchronousAgent syncAgent = new SynchronousAgent();
      syncAgent.deploy();
      //Doesn't work, sleeps instead to wait to the agent deployment, avoiding NullPointerException
//      monitor.wait();
      while(INSTANCE==null)
        Thread.sleep(100);
    }
    return INSTANCE;
  }

  /**
   * 
   */
  private static final long serialVersionUID = 3842240802167239809L;

  private int nextMsgId;  
  private Map<String, Message> requests;
  
  public SynchronousAgent() {
    super(true);
    requests = new HashMap<String,Message>();
    nextMsgId = 0;
  }
  
  @Override
  protected void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
    INSTANCE = this;
//    notifyInitialized();
  }
  
//  private static synchronized void notifyInitialized() {
//    monitor.notifyAll();    
//  }
  
  @Override
  public void agentFinalize(boolean lastTime) {
    super.agentFinalize(lastTime);
    INSTANCE = null;
  }
  
  public synchronized boolean createDestination(short serverId, String name, String className, Properties props, byte expectedType) throws InterruptedException {
    if(logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "createDestination("+serverId+","+name+","+className+","+props+","+expectedType+")");
    CreateDestinationRequest req = new CreateDestinationRequest(
        serverId, name, className, props, expectedType);
    String msgId = nextMsgId();
    FwdAdminRequestNot not = new FwdAdminRequestNot(req,
        getId(),
        msgId,
        null);
    sendTo(AdminTopic.getDefault(), not);
    while(!requests.containsKey(msgId))
      wait();
    Message msg = requests.get(msgId);
    AdminReply reply = (AdminReply) msg.getAdminMessage();
    return reply.succeeded();        
  }
  
  /**
   * 
   * @param serverId
   * @param userName
   * @param password
   * @param identityClass
   * @param props
   * @return True if the request succeeds, false otherwise
   * @throws Exception
   * @throws ClassNotFoundException
   * @throws InterruptedException
   */
  public synchronized boolean createUser(short serverId, String userName, 
      String password, String identityClass, Properties props)
      throws Exception {
    //Set identity
    Identity identity = (Identity) (identityClass==null?
        SimpleIdentity.class.newInstance():
        Class.forName(identityClass).newInstance());
    if(password==null)
      identity.setUserName(userName);
    else
      identity.setIdentity(userName, password);
    
    CreateUserRequest req = new CreateUserRequest(identity,serverId,props);
    String msgId = nextMsgId();
    FwdAdminRequestNot not = new FwdAdminRequestNot(req,
        getId(),
        msgId,
        null);
    sendTo(AdminTopic.getDefault(), not);
    while(!requests.containsKey(msgId))
      wait();
    Message msg = requests.remove(msgId);
    AdminReply reply = (AdminReply) msg.getAdminMessage();
    return reply.succeeded();        
  }
  
  private synchronized String nextMsgId() {
    return "ID:"+getAgentId()+"m"+nextMsgId++;
  }
  
  public void react(AgentId from, Notification not) throws Exception {
    if(not instanceof ClientMessages) {
      ClientMessages clientMsg = (ClientMessages) not;
      if(logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,this.toString() 
            + ".react(" + from + ", " + not + "): Reply received.");
      synchronized (this) {
        try {
          for(Object o : clientMsg.getMessages()) {
            Message msg = (Message) o;
            requests.put(msg.correlationId,msg);
          }
          notifyAll();
          if(logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG,this.toString()
                + ".react(" + from + ", " + not + "): Notified.");
        } catch(IllegalMonitorStateException e) {
          if(logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.ERROR,e);          
        }
      }
    } else {
      super.react(from,not);
    }
  }
}
