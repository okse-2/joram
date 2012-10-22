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
 * The <code>FtpQueue</code> class allows administrators to create FTP queues.
 * <p>
 * A FTP queue is special destinations allowing to transfer file by FTP. It wraps the FTP
 * transfer of a file through a message exchange. The sender starts the transfer by sending
 * a JMS message, and the receiver is notified of the transfer completion by a JMS message.
 */
public class FtpQueue {
  /**
   * Class name of default handler allowing to transfer file through FTP.
   */
  public final static String DefaultFTPImpl = "com.scalagent.joram.mom.dest.ftp.TransferImplRef";
  /**
   * Class name of handler allowing to transfer file using JFTP.
   */
  public final static String JFTPImpl = "com.scalagent.joram.mom.dest.ftp.TransferImplJftp";

  /**
   * Administration method creating and deploying a FTP queue on the local server.
   * <p>
   * The request fails if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   * 
   * @param name  The name of the created queue.
   * @return the created destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see #create(int, String)
   */
  public static Queue create(String name) throws ConnectException, AdminException {
    return create(AdminModule.getLocalServerId(), name);
  }

  /**
   * Administration method creating and deploying a FTP queue on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the queue.
   * @param name      The name of the created queue.
   * @return the created  destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(int serverId,
                             String name) throws ConnectException, AdminException {
    return create(AdminModule.getLocalServerId(), name, null);
  }

  /**
   * Administration method creating and deploying a FTP queue on a given server.
   * <p>
   * A set of properties is used to configure the FTP destination:<ul>
   * <li>user - the user name for the FTP.</li>
   * <li>pass - the user password for FTP.</li>
   * <li>path - the local directory to store the file transferred. Default is the running
   * directory (path=null).</li>
   * <li>ftpImplName: The implementation of the FTP transfer, we provide two implementations:<ul>
   * <li>the default one based on JDK URL:<br>com.scalagent.joram.mom.dest.ftp.TransferImplRef</li>
   * <li>the second is based on JFTP:<br>com.scalagent.joram.mom.dest.ftp.TransferImplJftp.</li></ul>
   * </li>
   * </ul>
   * The user and pass options are optional, because this information can be set through an URL by the
   * sender as a message property like this:<br>
   * <code>msg.setStringProperty("url", "ftp://user:pass@host/file;type=i");</code>
   * <p>
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
    if (!props.containsKey("ftpImplName"))
      props.setProperty("ftpImplName", DefaultFTPImpl);
    Queue queue = Queue.create(serverId, name, Queue.FTP_QUEUE, props);
    return queue;
  }

}
