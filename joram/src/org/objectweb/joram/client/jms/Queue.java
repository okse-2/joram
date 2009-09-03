/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import java.net.ConnectException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Properties;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.objectweb.util.monolog.api.BasicLevel;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;

import org.objectweb.joram.shared.admin.*;

import fr.dyade.aaa.util.management.MXWrapper;

/**
 *  Implements the <code>javax.jms.Queue</code> interface and provides
 * Joram specific administration and monitoring methods. This is a proxy
 * object a client uses to specify the destination of messages it is
 * sending and the source of messages it receives.
 *  
 */
public class Queue extends Destination implements javax.jms.Queue, QueueMBean {
  private final static String QUEUE_TYPE = "queue";

  public static boolean isQueue(String type) {
    return Destination.isAssignableTo(type, QUEUE_TYPE);
  }

  // Used by jndi2 SoapObjectHelper
  public Queue() {
    super(QUEUE_TYPE);
  }

  public Queue(String name) {
    super(name, QUEUE_TYPE);
  }

  protected Queue(String name, String type) {
    super(name, type);
  }

  /**
   * Gets the The Joram's internal unique identifier of this queue.
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   * @return	The Joram's internal unique identifier.
   */
  public String getQueueName() throws JMSException {
    return getName();
  }

  /**
   *  Admin method creating and deploying (or retrieving) a queue on a
   * given server. First a destination with the specified name is searched
   * on the given server, if it does not exist it is created. In any case,
   * its provider-specific address is returned.
   * <p>
   *  The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   * @param className  The MOM's queue class name.
   * @param prop       The queue properties.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(int serverId,
                             String name,
                             String className,
                             Properties prop)
    throws ConnectException, AdminException {
    Queue queue = new Queue();
    doCreate(serverId, name, className, prop, queue, QUEUE_TYPE);

    StringBuffer buff = new StringBuffer();
    buff.append("type=").append(QUEUE_TYPE);
    buff.append(",name=").append(name);
    try {
      MXWrapper.registerMBean(queue, "joramClient", buff.toString());
    } catch (Exception e) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "registerMBean", e);
    }
    return queue;
  }

  /**
   * Admin method creating and deploying a queue on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param className  The queue class name.
   * @param prop       The queue properties.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(int serverId,
                             String className,
                             Properties prop)
                throws ConnectException, AdminException {
    return create(serverId, null, className, prop);
  }

  /**
   * Admin method creating and deploying a queue on a given server.
   * It creates a Joram's standart queue.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param prop       The queue properties.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(int serverId, Properties prop)
                throws ConnectException, AdminException {
    return create(serverId, "org.objectweb.joram.mom.dest.Queue", prop);
  }

  /**
   * Admin method creating and deploying (or retrieving) a queue on a given
   * server with a given name. First a destination with the specified name is
   * searched on the given server, if it does not exist it is created. In any
   * case, its provider-specific address is returned.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
   *
   * @param serverId  The identifier of the server where deploying the queue.
   * @param name      The queue name. 
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(int serverId, String name)
                throws ConnectException, AdminException {
    return create(serverId, name, "org.objectweb.joram.mom.dest.Queue", null);
  }

  /**
   * Admin method creating and deploying (or retrieving) a queue on the
   * local server. First a destination with the specified name is searched
   * on the given server, if it does not exist it is created. In any case,
   * its provider-specific address is returned.
   * <p>
   * The request fails if the destination deployement fails server side.
   *
   * @param name      The queue name. 
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(String name)
                throws ConnectException, AdminException {
    return create(AdminModule.getLocalServerId(),
                  name,
                  "org.objectweb.joram.mom.dest.Queue",
                  null);
  }
 
  /**
   * Admin method creating and deploying a queue on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(int serverId)
                throws ConnectException, AdminException {
    return create(serverId, null, "org.objectweb.joram.mom.dest.Queue", null);
  }

  /**
   * Admin method creating and deploying a queue on the local server. 
   * <p>
   * The request fails if the destination deployement fails server side.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create() throws ConnectException, AdminException {
    return create(AdminModule.getLocalServerId());
  }

  /**
   * Admin method setting or unsetting the threshold for this queue.
   * <p>
   * The request fails if the queue is deleted server side.
   *
   * @param threshold  The threshold value to be set (-1 for unsetting
   *                   previous value).
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void setThreshold(int threshold)
              throws ConnectException, AdminException
  {
    if (threshold == -1)
      AdminModule.doRequest(new UnsetQueueThreshold(agentId));
    else
      AdminModule.doRequest(new SetQueueThreshold(agentId, threshold));
  } 

  /** 
   * Monitoring method returning the threshold of this queue, -1 if not set.
   * <p>
   * The request fails if the queue is deleted server side.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public int getThreshold() throws ConnectException, AdminException
  {
    Monitor_GetDMQSettings request = new Monitor_GetDMQSettings(agentId);
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) AdminModule.doRequest(request);
    
    if (reply.getThreshold() == null)
      return -1;
    else
      return reply.getThreshold().intValue();
  }

  /**
   * Admin method setting nbMaxMsg for this queue.
   * <p>
   * The request fails if the queue is deleted server side.
   *
   * @param nbMaxMsg  nb Max of Message (-1 no limit).
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void setNbMaxMsg(int nbMaxMsg)
    throws ConnectException, AdminException {
    AdminModule.doRequest(new SetNbMaxMsg(agentId, nbMaxMsg));
  } 

  /** 
   * Monitoring method returning the nbMaxMsg of this queue, -1 if no limit.
   * <p>
   * The request fails if the queue is deleted server side.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public int getNbMaxMsg() 
    throws ConnectException, AdminException {
    Monitor_GetNbMaxMsg request = new Monitor_GetNbMaxMsg(agentId);
    Monitor_GetNbMaxMsgRep reply;
    reply = (Monitor_GetNbMaxMsgRep) AdminModule.doRequest(request);
    return reply.getNbMaxMsg();
  }
   
  /**
   * Monitoring method returning the number of pending messages on this queue.
   * <p>
   * The request fails if the queue is deleted server side.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public int getPendingMessages() throws ConnectException, AdminException
  {
    Monitor_GetPendingMessages request =
      new Monitor_GetPendingMessages(agentId);
    Monitor_GetNumberRep reply;
    reply = (Monitor_GetNumberRep) AdminModule.doRequest(request);

    return reply.getNumber();
  }

  /**
   * Monitoring method returning the number of pending requests on this queue.
   * <p>
   * The request fails if the queue is deleted server side.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public int getPendingRequests() throws ConnectException, AdminException
  {
    Monitor_GetPendingRequests request =
      new Monitor_GetPendingRequests(agentId);
    Monitor_GetNumberRep reply =
      (Monitor_GetNumberRep) AdminModule.doRequest(request);

    return reply.getNumber();
  }

  public String[] getMessageIds() 
    throws AdminException, ConnectException {
    GetQueueMessageIdsRep reply = 
      (GetQueueMessageIdsRep)AdminModule.doRequest(
        new GetQueueMessageIds(agentId));
    return reply.getMessageIds();
  }
  
  public javax.jms.Message readMessage(String msgId) throws AdminException, ConnectException, JMSException {
    GetQueueMessageRep reply = 
      (GetQueueMessageRep)AdminModule.doRequest(
        new GetQueueMessage(agentId, msgId));
    return Message.wrapMomMessage(null, reply.getMessage());
  }

  public String getMessageDigest(String msgId)
    throws AdminException, ConnectException, JMSException {
    GetQueueMessageRep reply = 
      (GetQueueMessageRep)AdminModule.doRequest(
        new GetQueueMessage(agentId, msgId));
    javax.jms.Message msg = Message.wrapMomMessage(null, reply.getMessage());
    StringBuffer strbuf = new StringBuffer();
    strbuf.append("Message: ").append(msg.getJMSMessageID());
    strbuf.append("\n\tTo: ").append(msg.getJMSDestination());
    strbuf.append("\n\tCorrelationId: ").append(msg.getJMSCorrelationID());
    strbuf.append("\n\tDeliveryMode: ").append(msg.getJMSDeliveryMode());
    strbuf.append("\n\tExpiration: ").append(msg.getJMSExpiration());
    strbuf.append("\n\tPriority: ").append(msg.getJMSPriority());
    strbuf.append("\n\tRedelivered: ").append(msg.getJMSRedelivered());
    strbuf.append("\n\tReplyTo: ").append(msg.getJMSReplyTo());
    strbuf.append("\n\tTimestamp: ").append(msg.getJMSTimestamp());
    strbuf.append("\n\tType: ").append(msg.getJMSType());
    return strbuf.toString();
  }

  public Properties getMessageHeader(String msgId)
    throws AdminException, ConnectException, JMSException {
    GetQueueMessageRep reply = 
      (GetQueueMessageRep)AdminModule.doRequest(
        new GetQueueMessage(agentId, msgId));
    Message msg = (Message) Message.wrapMomMessage(null, reply.getMessage());

    Properties prop = new Properties();
    prop.setProperty("JMSMessageID", msg.getJMSMessageID());
    prop.setProperty("JMSDestination", msg.getJMSDestination().toString());
    if (msg.getJMSCorrelationID() != null)
      prop.setProperty("JMSCorrelationID", msg.getJMSCorrelationID());
    prop.setProperty("JMSDeliveryMode",
                     new Integer(msg.getJMSDeliveryMode()).toString());
    prop.setProperty("JMSExpiration",
                     new Long(msg.getJMSExpiration()).toString());
    prop.setProperty("JMSPriority",
                     new Integer(msg.getJMSPriority()).toString());
    prop.setProperty("JMSRedelivered",
                     new Boolean(msg.getJMSRedelivered()).toString());
    if (msg.getJMSReplyTo() != null)
      prop.setProperty("JMSReplyTo", msg.getJMSReplyTo().toString());
    prop.setProperty("JMSTimestamp",
                     new Long(msg.getJMSTimestamp()).toString());
    if (msg.getJMSType() != null)
      prop.setProperty("JMSType", msg.getJMSType());

    if (msg.momMsg != null) {
      msg.momMsg.getOptionalHeader(prop);
    }

    return prop;
  }

  public Properties getMessageProperties(String msgId)
    throws AdminException, ConnectException, JMSException {
    GetQueueMessageRep reply = 
      (GetQueueMessageRep)AdminModule.doRequest(
        new GetQueueMessage(agentId, msgId));
    Message msg = (Message) Message.wrapMomMessage(null, reply.getMessage());

    Properties prop = new Properties();
    if (msg.momMsg != null) {
      msg.momMsg.getProperties(prop);
    }
    return prop;
  }

  public void deleteMessage(
    String msgId)
    throws AdminException, ConnectException {
    AdminModule.doRequest(new DeleteQueueMessage(agentId, msgId));
  }

  public void clear() 
    throws AdminException, ConnectException {
    AdminModule.doRequest(new ClearQueue(agentId));
  }

  /**
   * Adds a queue into the cluster this queue belongs to.
   * If this queue doesn't belong to a cluster then a cluster is
   * created by clustering this queue with the added queue.
   * <p>
   * The request fails if one or both of the queues are deleted, or
   * can't belong to a cluster.
   *
   * @param addedQueue queue added to the cluster
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void addClusteredQueue(Queue addedQueue)
    throws ConnectException, AdminException {
    AdminModule.doRequest(
      new AddQueueCluster(agentId, addedQueue.getName()));
  }

  /**
   * Removes a queue from the cluster this queue belongs to.
   * <p>
   * The request fails if the queue does not exist or is not part of any 
   * cluster.
   *
   * @param removedQueue queue removed from the cluster
   * 
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void removeClusteredQueue(Queue removedQueue)
    throws ConnectException, AdminException {
    AdminModule.doRequest(
      new RemoveQueueCluster(agentId, removedQueue.getName()));
  }

  /**
   * Returns the reference of the queues that belong to the cluster.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public String[] getQueueClusterElements()
    throws ConnectException, AdminException {
    AdminReply reply = AdminModule.doRequest(
      new ListClusterQueue(agentId));
    Vector list = (Vector)reply.getReplyObject();
    String[] res = new String[list.size()];
    list.copyInto(res);
    return res;
  }
}
