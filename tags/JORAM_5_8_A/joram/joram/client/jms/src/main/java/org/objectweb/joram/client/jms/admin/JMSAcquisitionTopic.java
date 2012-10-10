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
 * The <code>JMSAcquisitionTopic</code> class allows administrators to create JMS
 * acquisition topics (JMS bridge in).
 * <p>
 * The JMS bridge destinations rely on a particular Joram service which purpose is to maintain
 * valid connections with the foreign XMQ servers.
 */
public class JMSAcquisitionTopic {
  /**
   * Class name of handler allowing to acquire messages to a foreign JMS provider.
   */
  public final static String JMSAcquisition = "org.objectweb.joram.mom.dest.jms.JMSAcquisition";
  
  /**
   * Administration method creating and deploying a JMS acquisition topic on the local server.
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
   * Administration method creating and deploying a JMS acquisition topic on a given server.
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
   * Administration method creating and deploying a JMS acquisition topic on a given server.
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
    Properties props = new Properties();
    return create(serverId, name, dest, props);
  }

  /**
   * Administration method creating and deploying a JMS acquisition topic on a given server.
   * <p>
   * A set of properties is used to configure the distribution destination:<ul>
   * <li>period – .</li>
   * <li>acquisition.period - The period between two acquisitions, default is 0 (no periodic acquisition).</li>
   * <li>persistent - Tells if produced messages will be persistent, default is true (JMS default).</li>
   * <li>expiration - Tells the life expectancy of produced messages, default is 0 (JMS default time to live).</li>
   * <li>priority - Tells the JMS priority of produced messages, default is 4 (JMS default).</li>
   * <li>jms.ConnectionUpdatePeriod - Period between two update phases allowing the discovering of new
   * Connections.</li>
   * <li>jms.DurableSubscriptionName - If the XMQ destination is a topic, this property sets the name of the
   * durable subscription created. If absent, the subscription will not be durable and messages published when
   * connection with XMQ server is failing will be lost.</li>
   * <li>jms.Selector - Expression used for filtering messages from the XMQ destination.</li>
   * <li>jms.Routing - This property allows to filter the connections used to acquire messages.</li>
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
    if (!props.containsKey("acquisition.className"))
      props.setProperty("acquisition.className", JMSAcquisition);
    if (!props.containsKey("jms.DestinationName"))
      props.setProperty("jms.DestinationName", dest);
    Topic topic = Topic.create(serverId, name, Topic.ACQUISITION_TOPIC, props);
    return topic;
  }
}
