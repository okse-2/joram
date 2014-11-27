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
import org.objectweb.joram.mom.dest.AdminTopic.DestinationDesc;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.FwdAdminRequestNot;
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.ClearQueue;
import org.objectweb.joram.shared.admin.ClearSubscription;
import org.objectweb.joram.shared.admin.CreateDestinationRequest;
import org.objectweb.joram.shared.admin.CreateUserRequest;
import org.objectweb.joram.shared.admin.DeleteDestination;
import org.objectweb.joram.shared.admin.DeleteQueueMessage;
import org.objectweb.joram.shared.admin.DeleteSubscriptionMessage;
import org.objectweb.joram.shared.admin.DeleteUser;
import org.objectweb.joram.shared.admin.SetReader;
import org.objectweb.joram.shared.admin.SetWriter;
import org.objectweb.joram.shared.admin.UnsetReader;
import org.objectweb.joram.shared.admin.UnsetWriter;
import org.objectweb.joram.shared.excepts.RequestException;
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
  
  //TODO: Should be accessed as a service => Will be done when merged
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

  private int nextReqMsgId;  
  private Map<String, Message> requests;
  
  public SynchronousAgent() {
    super(true);
    requests = new HashMap<String,Message>();
    nextReqMsgId = 0;
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
    String reqId = nextReqMsgId();
    FwdAdminRequestNot not = new FwdAdminRequestNot(req,
        getId(),
        reqId,
        null);
    sendTo(AdminTopic.getDefault(), not);
    while(!requests.containsKey(reqId))
      wait();
    Message msg = requests.get(reqId);
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
    String reqId = nextReqMsgId();
    FwdAdminRequestNot not = new FwdAdminRequestNot(req,
        getId(),
        reqId,
        null);
    sendTo(AdminTopic.getDefault(), not);
    while(!requests.containsKey(reqId))
      wait();
    Message msg = requests.remove(reqId);
    AdminReply reply = (AdminReply) msg.getAdminMessage();
    return reply.succeeded();        
  }
 
  public synchronized boolean deleteUser(String userName, String agentId)
      throws Exception {
    DeleteUser req = new DeleteUser(userName,agentId);
    String reqId = nextReqMsgId();
    FwdAdminRequestNot not = new FwdAdminRequestNot(req,
        getId(),
        reqId,
        null);
    sendTo(AdminTopic.getDefault(), not);
    while(!requests.containsKey(reqId))
      wait();
    Message msg = requests.remove(reqId);
    AdminReply reply = (AdminReply) msg.getAdminMessage();
    return reply.succeeded();        
  }

  public synchronized boolean deleteDest(String agentId)
      throws Exception {
    DeleteDestination req = new DeleteDestination(agentId);
    String reqId = nextReqMsgId();
    FwdAdminRequestNot not = new FwdAdminRequestNot(req,
        getId(),
        reqId,
        null);
    sendTo(AdminTopic.getDefault(), not);
    while(!requests.containsKey(reqId))
      wait();
    Message msg = requests.remove(reqId);
    AdminReply reply = (AdminReply) msg.getAdminMessage();
    return reply.succeeded();        
  }
  
  /**
   * Delete a message in a queue
   * @param queueName Name of the queue
   * @param msgId ID of the message to be deleted
   * @return
   * @throws InterruptedException 
   */
  public synchronized boolean deleteQueueMessage(String queueName, String msgId) throws InterruptedException {
    if(logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,this.toString() 
          + ".deleteQueueMessage(" + queueName + ", " + msgId + ")" +
          		": Method called");
    
    DestinationDesc queueDesc;
    try {
      queueDesc = AdminTopic.lookupDest(queueName, DestinationConstants.QUEUE_TYPE);
    } catch (RequestException e) {
      return false;
    }
    if(logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,this.toString() 
          + ".deleteQueueMessage(" + queueName + ", " + msgId + ")" +
              ": Queue found="+queueDesc!=null);
    if(queueDesc==null) return false;
    
    String reqId = nextReqMsgId();
    DeleteQueueMessage req =
        new DeleteQueueMessage(queueDesc.getId().toString(), msgId);
    FwdAdminRequestNot not =
        new FwdAdminRequestNot(req, getId(), reqId, null);
    sendTo(queueDesc.getId(), not);
    if(logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,this.toString() 
          + ".deleteQueueMessage(" + queueName + ", " + msgId + ")" +
              ": Request sent to "+queueDesc.getId());
    while(!requests.containsKey(reqId))
      wait();
    if(logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,this.toString() 
          + ".deleteQueueMessage(" + queueName + ", " + msgId + ")" +
              ": Reply received");
    Message msg = requests.remove(reqId);
    AdminReply reply = (AdminReply) msg.getAdminMessage();
    return reply.succeeded();
  }
  
  /**
   * Deletes a message in a subscription
   * @param userName Subscriber's name
   * @param subName Subscription name
   * @param msgId ID of the message to be deleted
   * @return
   * @throws InterruptedException 
   */
  public synchronized boolean deleteSubMessage(String userName, String subName, String msgId) throws InterruptedException {
    if(logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,this.toString() 
          + ".deleteSubMessage("+ userName + ", " + subName + ", " + msgId + ")" +
              ": Method called.");
    AgentId userId = AdminTopic.lookupUser(userName);
    if(logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,this.toString() 
          + ".deleteSubMessage("+ userName + ", " + subName + ", " + msgId + ")" +
              ": User found="+userId!=null);
    if(userId==null)
      return false;
    String reqId = nextReqMsgId();
    DeleteSubscriptionMessage req =
        new DeleteSubscriptionMessage(userId.toString(), subName, msgId);
    FwdAdminRequestNot not =
        new FwdAdminRequestNot(req, getId(), reqId, null);
    sendTo(userId, not);
    if(logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,this.toString() 
          + ".deleteSubMessage("+ userName + ", " + subName + ", " + msgId + ")" +
              ": Request sent to "+userId);
    while(!requests.containsKey(reqId))
      wait();
    if(logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,this.toString() 
          + ".deleteSubMessage("+ userName + ", " + subName + ", " + msgId + ")" +
            ": Reply received");
    Message msg = requests.remove(reqId);
    AdminReply reply = (AdminReply) msg.getAdminMessage();
    return reply.succeeded();
  }

  
  /**
   * Clears all pending message of a queue
   * @param queueName Name of the queue
   * @return
   * @throws InterruptedException 
   */
  public synchronized boolean clearQueue(String queueName) throws InterruptedException {
    DestinationDesc queueDesc;
    try {
      queueDesc = AdminTopic.lookupDest(queueName, DestinationConstants.QUEUE_TYPE);
      if(queueDesc==null) return false;
    } catch (RequestException e) {
      return false;
    }
    String reqId = nextReqMsgId();
    ClearQueue req = new ClearQueue(queueDesc.getId().toString());
    FwdAdminRequestNot not =
        new FwdAdminRequestNot(req, getId(), reqId, null);
    sendTo(queueDesc.getId(), not);
    while(!requests.containsKey(reqId))
      wait();
    Message msg = requests.remove(reqId);
    AdminReply reply = (AdminReply) msg.getAdminMessage();
    return reply.succeeded();
  }
  
  /**
   * Clears all pending message of a subscription
   * @param userName Subscriber's name
   * @param subName Subscription name
   * @return
   * @throws InterruptedException 
   */
  public synchronized boolean clearSubscription(String userName, String subName) throws InterruptedException {
    AgentId userId = AdminTopic.lookupUser(userName);
    if(userId==null)
      return false;
    String reqId = nextReqMsgId();
    ClearSubscription req =
        new ClearSubscription(userId.toString(), subName);
    FwdAdminRequestNot not =
        new FwdAdminRequestNot(req, getId(), reqId, null);
    sendTo(userId, not);
    while(!requests.containsKey(reqId))
      wait();
    Message msg = requests.remove(reqId);
    AdminReply reply = (AdminReply) msg.getAdminMessage();
    return reply.succeeded();
  }
  
  public synchronized boolean setFreeWriting(boolean freeWriting, String destId) throws InterruptedException {
    AdminRequest req = null;
    if(freeWriting)
      req = new SetWriter(null, destId);
    else
      req = new UnsetWriter(null, destId);
    String reqId = nextReqMsgId();
    FwdAdminRequestNot not = new FwdAdminRequestNot(req, getId(), reqId);
    sendTo(AdminTopic.getDefault(), not);
    while(!requests.containsKey(reqId))
      wait();
    Message msg = requests.remove(reqId);
    AdminReply reply = (AdminReply) msg.getAdminMessage();
    return reply.succeeded();
  }
  
  public synchronized boolean setFreeReading(boolean freeReading, String destId) throws InterruptedException {
    AdminRequest req = null;
    if(freeReading)
      req = new SetReader(null, destId);
    else
      req = new UnsetReader(null, destId);
    String reqId = nextReqMsgId();
    FwdAdminRequestNot not = new FwdAdminRequestNot(req, getId(), reqId);
    sendTo(AdminTopic.getDefault(), not);
    while(!requests.containsKey(reqId))
      wait();
    Message msg = requests.remove(reqId);
    AdminReply reply = (AdminReply) msg.getAdminMessage();
    return reply.succeeded();
  }
  
  private synchronized String nextReqMsgId() {
    return "ID:"+getAgentId()+"m"+nextReqMsgId++;
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
