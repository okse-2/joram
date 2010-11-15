/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest;

import org.objectweb.joram.shared.security.SimpleIdentity;

public interface AdminTopicMBean extends TopicMBean {

  /**
   * Creates a new topic on the selected server.
   * 
   * @param name
   *          the topic name
   * @param topicClassName
   *          the topic class to instantiate
   * @param serverId
   *          the server where the topic will be deployed
   */
  public void createTopic(String name, String topicClassName, int serverId);

  /**
   * Creates a new topic on the selected server. The topic will be an instance
   * of {@link Topic}.
   * 
   * @param name
   *          the topic name
   * @param serverId
   *          the server where the topic will be deployed
   */
  public void createTopic(String name, int serverId);

  /**
   * Creates a new topic on the local server.
   * 
   * @param name
   *          the topic name
   */
  public void createTopic(String name);

  /**
   * Creates a new queue on the selected server.
   * 
   * @param name
   *          the topic name
   * @param queueClassName
   *          the queue class to instantiate
   * @param serverId
   *          the server where the queue will be deployed
   */
  public void createQueue(String name, String queueClassName, int serverId);

  /**
   * Creates a new queue on the selected server. The queue will be an instance
   * of {@link Queue}.
   * 
   * @param name
   *          the topic name
   * @param serverId
   *          the server where the queue will be deployed
   */
  public void createQueue(String name, int serverId);

  /**
   * Creates a new queue on the local server.
   * 
   * @param name
   *          the queue name
   */
  public void createQueue(String name);

  /**
   * Creates a new user on the selected server.
   * 
   * @param user
   *          the user name
   * @param passwd
   *          the user password
   * @param serverId
   *          the server where the user will be created
   * @param identityClassName
   *          the identity class name to instantiate
   */
  public void createUser(String user, String passwd, int serverId, String identityClassName) throws Exception;

  /**
   * Creates a new user on the selected server. {@link SimpleIdentity} class is
   * used.
   * 
   * @param user
   *          the user name
   * @param passwd
   *          the user password
   * @param serverId
   *          the server where the user will be created
   */
  public void createUser(String user, String passwd, int serverId) throws Exception;

  /**
   * Creates a new user on the local server. {@link SimpleIdentity} class is
   * used.
   * 
   * @param user
   *          the user name
   * @param passwd
   *          the user password
   */
  public void createUser(String user, String passwd) throws Exception;

}
