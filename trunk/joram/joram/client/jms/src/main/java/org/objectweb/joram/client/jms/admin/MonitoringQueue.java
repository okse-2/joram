/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - 2015 ScalAgent Distributed Technologies
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

import org.objectweb.joram.client.jms.Queue;

/**
 * The <code>MonitoringQueue</code> class allows administrators to create acquisition
 * queue for JMX monitoring data.
 * <p>
 * A monitoring acquisition destination is an acquisition destination configured to
 * transform JMX monitoring information into JMS messages. It works in 2 modes:
 * <ul>
 * <li>If the acquisition.period attribute is set (value greater than 0) the destination
 * periodically scans the selected JMX attributes and generates a message with the value
 * of these attributes.</li>
 * <li>In the other case, the user must send to the destination a message with the list
 * of JMX attributes to scan and the destination creates a message with these values, or
 * use the last known JMX attributes.</li>
 * </ul>
 * Each message is delivered to an unique consumer.
 * <p>
 * This queue is based on JMX monitoring so you must enable JMX monitoring to use it.
 */
public class MonitoringQueue {
  /**
   * Class name of handler allowing to acquire JMX monitoring data.
   */
  public final static String JMXAcquisition = "org.objectweb.joram.mom.dest.MonitoringAcquisition";
  
  /**
   * Administration method creating and deploying a JMX acquisition queue on the local server.
   * <p>
   * The request fails if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   * 
   * @return the created destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see #create(int, String, Properties)
   */
  public static Queue create() throws ConnectException, AdminException {
    return create(AdminModule.getLocalServerId());
  }

  /**
   * Administration method creating and deploying a JMX acquisition queue on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the queue.
   * @return the created destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see #create(int, String, Properties)
   */
  public static Queue create(int serverId) throws ConnectException, AdminException {
    return create(serverId, (String) null);
  }

  /**
   * Administration method creating and deploying a JMX acquisition queue on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the queue.
   * @param name      The name of the created queue.
   * @return the created destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see #create(int, String, Properties)
   */
  public static Queue create(int serverId,
                             String name) throws ConnectException, AdminException {
    return create(serverId, name, null);
  }

  /**
   * Administration method creating and deploying a JMX acquisition queue on a given server.
   * <p>
   * A set of properties is used to configure the distribution destination:<ul>
   * <li>period – .</li>
   * <li>acquisition.period - The period between two acquisitions, default is 0 (no periodic acquisition).
   * If this last case the acquisition must be triggered by an incoming message, the destination properties
   * can then be overloaded by the message ones.</li>
   * <li>persistent - Tells if produced messages will be persistent, default is true (JMS default).</li>
   * <li>expiration - Tells the life expectancy of produced messages, default is 0 (JMS default time to live).</li>
   * <li>priority - Tells the JMS priority of produced messages, default is 4 (JMS default).</li>
   * <li>Additionally to common acquisition parameters (see above), properties are used to indicate the list
   * of JMX attributes that will be monitored. The property key is the name of the MBean and the value is a
   * comma separated list of attributes to monitor for this MBean. The '*' character is allowed to monitor
   * every parameter of the MBean. Accessing multiple MBeans is possible using wildcard characters, as defined
   * in the javax.management.ObjectName class.</li>
   * </ul>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the queue.
   * @param name      The name of the created queue.
   * @param props     A Properties object containing all needed parameters.
   * @return the created destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(int serverId,
                             String name,
                             Properties props) throws ConnectException, AdminException {
    if (props == null)
      props = new Properties();
    props.setProperty("acquisition.className", JMXAcquisition);
    Queue queue = Queue.create(serverId, name, Queue.ACQUISITION_QUEUE, props);
    return queue;
  }
}
