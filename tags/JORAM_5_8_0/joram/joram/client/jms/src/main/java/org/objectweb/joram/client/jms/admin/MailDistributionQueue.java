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

import org.objectweb.joram.client.jms.Queue;

/**
 * The <code>MailDistributionQueue</code> class allows administrators to create Mail
 * distribution queues.
 * <p>
 * Using an e-mail account, mail destinations allow you to forward Joram's messages to
 * an external email account using SMTP.
 */
public class MailDistributionQueue {
  /**
   * Class name of handler allowing to distribute messages to a SMTP mail server.
   * <p>
   * This handler is used by default to create <code>MailDistributionQueue</code>,
   * the distribution.className property allows to declare an alternate handler using
   * different protocol or implementation.
   */
  public final static String MailDistribution = "com.scalagent.joram.mom.dest.mail.MailDistribution";
  
  /**
   * Administration method creating and deploying a Mail distribution queue on the local server.
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
   * Administration method creating and deploying a Mail distribution queue on a given server.
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
   * Administration method creating and deploying a Mail distribution queue on a given server.
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
   * Administration method creating and deploying a Mail distribution queue on a given server.
   * <p>
   * A set of properties is used to configure the distribution destination:<ul>
   * <li>period – Tells the time to wait before another distribution attempt. Default is 0, which
   * means there won't be other attempts.</li>
   * <li>distribution.batch –  If set to true, the destination will try to distribute each time every waiting
   * message, regardless of distribution errors. This can lead to the loss of message ordering, but will
   * prevent a blocking message from blocking every following message. When set to false, the distribution
   * process will stop on the first error. Default is false.</li>
   * <li>distribution.async - If set to true, the messages are asynchronously forwarded through a daemon.</li>
   * <li>smtpServer - the DNS name or IP address of the SMTP server.</li>
   * <li>from - the email address of the sender.</li>
   * <li>to, cc, bcc - a comma separated list of recipients.</li>
   * <li>subject - the subject of outgoing message.</li>
   * <li>selector - additionally a selector can be added to filter the forwarded messages.</li>
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
    if (!props.containsKey("distribution.className"))
      props.setProperty("distribution.className", MailDistribution);
    Queue queue = Queue.create(serverId, name, Queue.DISTRIBUTION_QUEUE, props);
    return queue;
  }
}
