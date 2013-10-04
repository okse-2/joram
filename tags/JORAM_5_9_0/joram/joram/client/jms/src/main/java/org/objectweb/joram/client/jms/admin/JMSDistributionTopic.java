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
 * Contributor(s): 
 */
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;
import java.util.Properties;

import org.objectweb.joram.client.jms.Topic;

/**
 * The <code>JMSDistributionTopic</code> class allows administrators to create JMS
 * distribution topics (JMS bridge out).
 * <p>
 * The JMS bridge destinations rely on a particular Joram service which purpose is to maintain
 * valid connections with the foreign XMQ servers.
 */
public class JMSDistributionTopic {
  /**
   * Class name of handler allowing to distribute messages to a foreign JMS provider.
   */
  public final static String JMSDistribution = "org.objectweb.joram.mom.dest.jms.JMSDistribution";
  
  /**
   * Administration method creating and deploying a JMS distribution topic on the local server.
   * <p>
   * The request fails if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   * 
   * @param dest  The name of the foreign destination.
   * @return the created bridge destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see #create(int, String, String, Properties)
   */
  public static Topic create(String dest) throws ConnectException, AdminException {
    return create(AdminModule.getLocalServerId(), dest);
  }

  /**
   * Administration method creating and deploying a JMS distribution topic on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the topic.
   * @param dest      The name of the foreign destination.
   * @return the created bridge destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see #create(int, String, String, Properties)
   */
  public static Topic create(int serverId,
                             String dest) throws ConnectException, AdminException {
    return create(serverId, (String) null, dest);
  }

  /**
   * Administration method creating and deploying a JMS distribution topic on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the topic.
   * @param name      The name of the created topic.
   * @param dest      The name of the foreign destination.
   * @return the created bridge destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see #create(int, String, String, Properties)
   */
  public static Topic create(int serverId,
                             String name,
                             String dest) throws ConnectException, AdminException {
    return create(serverId, name, dest, null);
  }

  /**
   * Administration method creating and deploying a JMS distribution topic on a given server.
   * <p>
   * A set of properties is used to configure the distribution destination:<ul>
   * <li>period – Tells the time to wait before another distribution attempt. Default is 0, which
   * means there won't be other attempts.</li>
   * <li>distribution.batch –  If set to true, the destination will try to distribute each time every waiting
   * message, regardless of distribution errors. This can lead to the loss of message ordering, but will
   * prevent a blocking message from blocking every following message. When set to false, the distribution
   * process will stop on the first error. Default is false.</li>
   * <li>distribution.async - If set to true, the messages are asynchronously forwarded through a daemon.</li>
   * <li>jms.ConnectionUpdatePeriod - Period between two update phases allowing the discovering of new
   * Connections.</li>
   * <li>jms.Routing - This property allows to filter the connections used to forward the messages. It can
   * be set either globally at creation or specifically for a message at sending.</li>
   * </ul>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the topic.
   * @param name      The name of the created topic.
   * @param dest      The name of the foreign destination.
   * @param props     A Properties object containing all needed parameters.
   * @return the created bridge destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Topic create(int serverId,
                             String name,
                             String dest,
                             Properties props) throws ConnectException, AdminException {
    if (props == null)
      props = new Properties();
    if (!props.containsKey("distribution.className"))
      props.setProperty("distribution.className", JMSDistribution);
    if (!props.containsKey("jms.DestinationName"))
      props.setProperty("jms.DestinationName", dest);
    Topic topic = Topic.create(serverId, name, Topic.DISTRIBUTION_TOPIC, props);
    return topic;
  }
}
