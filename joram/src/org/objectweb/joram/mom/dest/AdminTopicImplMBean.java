/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
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
 * Contributor(s):
 */
package org.objectweb.joram.mom.dest;


/**
 * The <code>AdminTopicImplMBean</code> interface defines the JMX
 * instrumentation for administering a JORAM server (through an
 * <code>AdminTopic</code>).
 */
public interface AdminTopicImplMBean
{
  /** Returns the identifiers of the administered queues. */
  public String getAdministeredQueuesIds();

  /** Returns the identifiers of the administered topics. */
  public String getAdministeredTopicsIds();

  /**
   * Returns the names and proxies identifiers of the administered JMS users.
   */
  public String getAdministeredJmsUsers();

  /**
   * Creates a local JMS user.
   *
   * @param name  User name.
   * @param password  User password.
   *
   * @exception Exception  If the user already exists.
   */
  public void createLocalJmsUser(String name, String password)
              throws Exception;

  /**
   * Creates a local <code>Topic</code> agent.
   */
  public void createLocalTopic();


  /**
   * Creates a local <code>Queue</code> agent.
   */
  public void createLocalQueue();
}
