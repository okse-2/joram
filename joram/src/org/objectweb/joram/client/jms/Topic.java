/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
 */
package org.objectweb.joram.client.jms;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.shared.admin.*;

import java.net.ConnectException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
 
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Implements the <code>javax.jms.Topic</code> interface and provides
 * JORAM specific administration and monitoring methods.
 */
public class Topic extends Destination implements javax.jms.Topic
{
  /**
   * Constructs a topic.
   *
   * @param agentId  Identifier of the topic agent.
   */
  public Topic(String agentId)
  {
    super(agentId);
  }

  /**
   * Constructs an empty topic.
   */
  public Topic()
  {}

  /** Returns a String image of the topic. */
  public String toString()
  {
    return "Topic:" + agentId;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getTopicName() throws JMSException
  {
    return getName();
  }


  /**
   * Decodes a <code>Topic</code> which traveled through the SOAP protocol.
   */ 
  public Object decode(Hashtable h) {
    return new Topic((String) h.get("agentId"));
  }


  /**
   * Admin method creating and deploying (or retrieving) a topic on a given
   * server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param name       The name of the topic.
   * @param className  The topic class name.
   * @param prop       The topic properties.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Topic create(int serverId,
                             String name,
                             String className,
                             Properties prop)
                throws ConnectException, AdminException
  {
    String topicId = doCreate(serverId, name, className, prop);
    return new Topic(topicId);
  }

  /**
   * Admin method creating and deploying a topic on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param className  The topic class name.
   * @param prop       The topic properties.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Topic create(int serverId,
                             String className,
                             Properties prop)
                throws ConnectException, AdminException
  {
    return create(serverId, null, className, prop);
  }

  /**
   * Admin method creating and deploying a topic on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param prop       The topic properties.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Topic create(int serverId, Properties prop)
                throws ConnectException, AdminException
  {
    return create(serverId, "org.objectweb.joram.mom.dest.Topic", prop);
  }
 
  /**
   * Admin method creating and deploying a topic on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Topic create(int serverId)
                throws ConnectException, AdminException
  {
    return create(serverId, null);
  }

  /**
   * Admin method creating and deploying a topic on the local server. 
   * <p>
   * The request fails if the destination deployement fails server side.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Topic create() throws ConnectException, AdminException
  {
    return create(AdminModule.getLocalServer(), null);
  }

  /**
   * Monitoring method returning the hierarchical father of this topic,
   * null if none.
   * <p>
   * The request fails if the topic is deleted server side.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public Topic getHierarchicalFather() throws ConnectException, AdminException
  {
    Monitor_GetFather request = new Monitor_GetFather(agentId);
    Monitor_GetFatherRep reply =
      (Monitor_GetFatherRep) AdminModule.doRequest(request);

    if (reply.getFatherId() == null)
      return null;
    else
      return new Topic(reply.getFatherId());
  }

  /**
   * Monitoring method returning the list describing the cluster this topic
   * is part of.
   * <p>
   * The request fails if the topic is deleted server side.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public List getClusterFellows() throws ConnectException, AdminException
  {
    Monitor_GetCluster request = new Monitor_GetCluster(agentId);
    Monitor_GetClusterRep reply =
      (Monitor_GetClusterRep) AdminModule.doRequest(request);

    Vector topics = reply.getTopics();
    Vector list = new Vector();
    for (int i = 0; i < topics.size(); i++)
      list.add(new Topic((String) topics.get(i)));
    return list;
  }

  /**
   * Monitoring method returning the number of subscriptions on this topic.
   * <p>
   * The request fails if the topic is deleted server side.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public int getSubscriptions() throws ConnectException, AdminException
  {
    Monitor_GetSubscriptions request = new Monitor_GetSubscriptions(agentId);
    Monitor_GetNumberRep reply =
      (Monitor_GetNumberRep) AdminModule.doRequest(request);
    return reply.getNumber();
  }
}
