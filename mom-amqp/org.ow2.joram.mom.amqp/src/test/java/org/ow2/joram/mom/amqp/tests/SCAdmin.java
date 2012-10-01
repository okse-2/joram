/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2011 ScalAgent Distributed Technologies
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
package org.ow2.joram.mom.amqp.tests;

import java.io.File;

import org.ow2.joram.mom.amqp.MetaData;

/**
 * Simplified Admin interface used to manage the start and the stop of non
 * collocated AgentServers.
 */
public interface SCAdmin {
  
  public static final String TEST_RESOURCES_DIR = System.getProperty("user.dir") + "/target/test-classes/";
  
  public static final String A3DEBUG_LOCATION = new File(TEST_RESOURCES_DIR + "a3debug.cfg").getAbsolutePath();
  
  public static final String A3SERVERS_LOCATION = new File(TEST_RESOURCES_DIR + "a3servers.xml").getAbsolutePath();

  public static final String RUNNING_DIR = System.getProperty("user.dir") + "/target/run";

  public static final String JORAM_VERSION = MetaData.joram_version;

  /**
   * Starts a new AgentServer.
   * 
   * @param sid
   *          id of the server to start
   */
  public void startAgentServer(short sid) throws Exception;

  /**
   * Kills a given AgentServer. The server must have been started with this
   * Admin interface.
   * 
   * @param sid
   *          id of the server to kill
   */
  public void killAgentServer(short sid) throws Exception;

  /**
   * Cleanly stops an AgentServer. The server must have been started with this
   * Admin interface.
   * 
   * @param sid
   *          id of the server to stop
   */
  public void stopAgentServer(short sid) throws Exception;

  /**
   * Cleans run directory
   */
  public void cleanRunDir() throws Exception;

}
