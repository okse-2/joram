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
 * The <code>CollectorTopic</code> class allows administrators to create JMS
 * collector topics (JMS bridge in).
 * <p>
 * Collector topics are special destinations usable to collect a document from
 * a specified URL. They can be used to periodically import a file from an URL to
 * a Joram's message and forward to each subscribers.
 */
public class CollectorTopic {
  /**
   * Class name of handler allowing to acquire messages from a specified URL.
   */
  public final static String URLAcquisition = "com.scalagent.joram.mom.dest.collector.URLAcquisition";
  
  /**
   * Administration method creating and deploying an URL acquisition topic on the local server.
   * <p>
   * The request fails if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   * 
   * @param url  the URL locating the element to collect.
   * @return the created destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see #create(int, String, String, Properties)
   */
  public static Topic create(String url) throws ConnectException, AdminException {
    return create(AdminModule.getLocalServerId(), url);
  }

  /**
   * Administration method creating and deploying an URL acquisition topic on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the topic.
   * @param url       the URL locating the element to collect.
   * @return the created destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see #create(int, String, String, Properties)
   */
  public static Topic create(int serverId,
                             String url) throws ConnectException, AdminException {
    return create(serverId, (String) null, url);
  }

  /**
   * Administration method creating and deploying an URL acquisition topic on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the topic.
   * @param name      The name of the created topic.
   * @param url       the URL locating the element to collect.
   * @return the created destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see #create(int, String, String, Properties)
   */
  public static Topic create(int serverId,
                             String name,
                             String url) throws ConnectException, AdminException {
    return create(serverId, name, url, null);
  }

  /**
   * Administration method creating and deploying an URL acquisition topic on a given server.
   * <p>
   * A set of properties is used to configure the distribution destination:<ul>
   * <li>period – .</li>
   * <li>acquisition.period - The period between two acquisitions, default is 0 (no periodic acquisition).
   * If this last case the acquisition must be triggered by an incoming message, the destination properties
   * can then be overloaded by the message ones.</li>
   * <li>persistent - Tells if produced messages will be persistent, default is true (JMS default).</li>
   * <li>expiration - Tells the life expectancy of produced messages, default is 0 (JMS default time to live).</li>
   * <li>priority - Tells the JMS priority of produced messages, default is 4 (JMS default).</li>
   * <li>collector.url - locates the element that will be collected.</li>
   * <li>collector.type - indicates the type of the generated message. Default is Message.BYTES.</li>
   * </ul>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the topic.
   * @param name      The name of the created topic.
   * @param url       the URL locating the element to collect.
   * @param props     A Properties object containing all needed parameters.
   * @return the created destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Topic create(int serverId,
                             String name,
                             String url,
                             Properties props) throws ConnectException, AdminException {
    if (props == null)
      props = new Properties();
    if (!props.containsKey("acquisition.className"))
      props.setProperty("acquisition.className", URLAcquisition);
    if (!props.containsKey("collector.url"))
      props.setProperty("collector.url", url);
    Topic topic = Topic.create(serverId, name, Topic.ACQUISITION_TOPIC, props);
    return topic;
  }
}
