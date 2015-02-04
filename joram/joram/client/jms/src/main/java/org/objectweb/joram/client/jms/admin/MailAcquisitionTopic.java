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

import org.objectweb.joram.client.jms.Topic;

/**
 * The <code>MailAcquisitionTopic</code> class allows administrators to create Mail
 * acquisition topics.
 * <p>
 * Using an e-mail account, mail acquisition destinations allow you to import emails
 * from this external account using POP and turn them into Joram's JMS messages.
 */
public class MailAcquisitionTopic {
  /**
   * Class name of handler allowing to acquire messages from a POP mail provider.
   * <p>
   * This handler is used by default to create <code>MailAcquisitionTopic</code>,
   * the acquisition.className property allows to declare an alternate handler using
   * different protocol (IMAP for example).
   */
  public final static String MailAcquisition = "com.scalagent.joram.mom.dest.mail.MailAcquisition";
  
  /**
   * Administration method creating and deploying a mail acquisition topic on the local server.
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
  public static Topic create() throws ConnectException, AdminException {
    return create(AdminModule.getLocalServerId());
  }

  /**
   * Administration method creating and deploying a mail acquisition topic on a given server.
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
  public static Topic create(int serverId) throws ConnectException, AdminException {
    return create(serverId, (String) null);
  }

  /**
   * Administration method creating and deploying a mail acquisition topic on a given server.
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
  public static Topic create(int serverId,
                             String name) throws ConnectException, AdminException {
    return create(serverId, name, null);
  }

  /**
   * Administration method creating and deploying a mail acquisition topic on a given server.
   * <p>
   * A set of properties is used to configure the acquisition destination:<ul>
   * <li>period – .</li>
   * <li>acquisition.period - The period between two acquisitions, default is 0 (no periodic acquisition).</li>
   * <li>persistent - Tells if produced messages will be persistent, default is true (JMS default).</li>
   * <li>expiration - Tells the life expectancy of produced messages, default is 0 (JMS default time to live).</li>
   * <li>priority - Tells the JMS priority of produced messages, default is 4 (JMS default).</li>
   * <li>popServer - the DNS name or IP address of the POP server.</li>
   * <li>popUser - the login name for the email account.</li>
   * <li>popPassword - the password for the email account.</li>
   * <li>expunge - allows to remove or not email on the server.</li>
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
  public static Topic create(int serverId,
                             String name,
                             Properties props) throws ConnectException, AdminException {
    if (props == null)
      props = new Properties();
    props.setProperty("acquisition.className", MailAcquisition);
    Topic topic = Topic.create(serverId, name, Topic.ACQUISITION_TOPIC, props);
    return topic;
  }
}
