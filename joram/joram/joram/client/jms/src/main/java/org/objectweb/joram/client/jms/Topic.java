/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jms.JMSException;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.shared.admin.ClusterAdd;
import org.objectweb.joram.shared.admin.ClusterLeave;
import org.objectweb.joram.shared.admin.ClusterList;
import org.objectweb.joram.shared.admin.ClusterListReply;
import org.objectweb.joram.shared.admin.GetFatherReply;
import org.objectweb.joram.shared.admin.GetFatherRequest;
import org.objectweb.joram.shared.admin.GetNumberReply;
import org.objectweb.joram.shared.admin.GetSubscriberIds;
import org.objectweb.joram.shared.admin.GetSubscriberIdsRep;
import org.objectweb.joram.shared.admin.GetSubscriptionsRequest;
import org.objectweb.joram.shared.admin.SetFather;

/**
 *  Implements the <code>javax.jms.Topic</code> interface and provides
 * Joram specific administration and monitoring methods. This is a proxy
 * object a client uses to specify the destination of messages it is
 * sending and the source of messages it receives.
 */
public class Topic extends Destination implements javax.jms.Topic, TopicMBean {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public Topic() {
    super(TOPIC_TYPE);
  }

  public Topic(String id) {
    super(id, TOPIC_TYPE);
  }

  protected Topic(String id, byte type) {
    super(id, type);
  }

  /**
   * Returns a String image of the topic.
   * 
   * @return A provider-specific identity values for this topic.
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append("Topic");
    if (adminName != null)
      strbuf.append('[').append(adminName).append(']');
    strbuf.append(':').append(agentId);
    return strbuf.toString();
  }

  /**
   * Gets the The Joram's internal unique identifier of this topic.
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getTopicName() throws JMSException {
    return getName();
  }

  public static Topic createTopic(String agentId, String name) {
    Topic dest = new Topic();
    
    dest.agentId = agentId;
    dest.adminName = name;

    return dest;
  }

  /**
   *  Admin method creating and deploying (or retrieving) a topic on a
   * given server. First a destination with the specified name is searched
   * on the given server, if it does not exist it is created. In any case,
   * its provider-specific address is returned.
   * <p>
   *  The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
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
                             Properties prop) throws ConnectException, AdminException {
    Topic topic = new Topic();
    topic.doCreate(serverId, name, className, prop, topic, TOPIC_TYPE);
    return topic;
  }

  /**
   * Admin method creating and deploying a topic on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
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
                             Properties prop) throws ConnectException, AdminException {
    return create(serverId, null, className, prop);
  }

  /**
   * Admin method creating and deploying a topic on a given server.
   * It creates a Jorram's standard topic.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   * @param prop       The topic properties.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Topic create(int serverId, Properties prop) throws ConnectException, AdminException {
    return create(serverId, "org.objectweb.joram.mom.dest.Topic", prop);
  }

  /**
   * Admin method creating and deploying (or retrieving) a topic on a given
   * server with a given name. First a destination with the specified name is
   * searched on the given server, if it does not exist it is created. In any
   * case, its provider-specific address is returned.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the topic.
   * @param name      The topic name. 
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Topic create(int serverId, String name) throws ConnectException, AdminException {
    return create(serverId, name, "org.objectweb.joram.mom.dest.Topic", null);
  }

  /**
   * Admin method creating and deploying (or retrieving) a topic on the
   * local server. First a destination with the specified name is searched
   * on the given server, if it does not exist it is created. In any case,
   * its provider-specific address is returned.
   * <p>
   * The request fails if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param name      The topic name. 
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Topic create(String name) throws ConnectException, AdminException {
    return create(AdminModule.getLocalServerId(),
                  name,
                  "org.objectweb.joram.mom.dest.Topic",
                  null);
  }

  /**
   * Admin method creating and deploying a topic on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId   The identifier of the server where deploying the topic.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Topic create(int serverId) throws ConnectException, AdminException {
    return create(serverId, null, "org.objectweb.joram.mom.dest.Topic", null);
  }

  /**
   * Admin method creating and deploying a topic on the local server. 
   * <p>
   * The request fails if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Topic create() throws ConnectException, AdminException {
    return create(AdminModule.getLocalServerId());
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
  public Topic getHierarchicalFather() throws ConnectException, AdminException {
    GetFatherRequest request = new GetFatherRequest(agentId);
    GetFatherReply reply = (GetFatherReply) doRequest(request);

    if (reply.getFatherId() == null)
      return null;
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
  public List getClusterFellows() throws ConnectException, AdminException {
    ClusterList request = new ClusterList(agentId);
    ClusterListReply reply = (ClusterListReply) doRequest(request);

    List topics = reply.getDestinations();
    List list = new ArrayList();
    if (topics != null) {
      for (int i = 0; i < topics.size(); i++)
        list.add(new Topic((String) topics.get(i)));
    }
    return list;
  }

  /**
   * Monitoring method returning the number of users that subscribes on
   * this topic.
   * If a client has many subscriptions it is only counted once.
   * <p>
   * The request fails if the topic is deleted server side.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public int getSubscriptions() throws ConnectException, AdminException {
    GetSubscriptionsRequest request = new GetSubscriptionsRequest(agentId);
    GetNumberReply reply = (GetNumberReply) doRequest(request);
    return reply.getNumber();
  }

  public String[] getSubscriberIds() throws AdminException, ConnectException {
    GetSubscriberIdsRep reply = 
      (GetSubscriberIdsRep)doRequest(new GetSubscriberIds(agentId));
    return reply.getSubscriberIds();
  }

  /**
   * Adds a topic into the cluster this topic belongs to.
   * If this topic doesn't belong to a cluster then a cluster is
   * created by clustering this topic with the added topic.
   * <p>
   * The request fails if one or both of the topics are deleted, or
   * can't belong to a cluster.
   *
   * @param addedTopic topic added to the cluster
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void addClusteredTopic(Topic addedTopic) throws ConnectException, AdminException {
    doRequest(new ClusterAdd(agentId, addedTopic.getName()));
  }

  /**
   * Removes this topic from the cluster it belongs to.
   * <p>
   * The request fails if the topic does not exist or is not part of any 
   * cluster.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void removeFromCluster() throws ConnectException, AdminException {
    doRequest(new ClusterLeave(agentId));
  }

  /**
   * Creates a hierarchical relationship between this topic
   * and its father topic.
   * <p>
   * The request fails if one of the topics does not exist or can't be part
   * of a hierarchy.
   *
   * @param parent the topic which will be parent. null to remove previous parent.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void setParent(Topic parent) throws ConnectException, AdminException {
    if (parent == null)
      unsetParent();
    else
      doRequest(new SetFather(parent.getName(), agentId));
  }

  /**
   * Unsets the father of this topic.
   * <p>
   * The request fails if the topic does not exist or is not part of any
   * hierarchy.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void unsetParent() throws ConnectException, AdminException {
    doRequest(new SetFather(null, agentId));
  }
}
