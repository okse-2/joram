/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package framework;


/**
 * Admin interface used to manage the start and the stop of non collocated
 * AgentServers.
 */
public interface SCAdminItf {

  /**
   * Starts a new AgentServer.
   * 
   * @param sid
   *          id of the server to start
   */
  public void startAgentServer(short sid) throws Exception;

  /**
   * Starts a new AgentServer.
   * 
   * @param sid
   *          id of the server to start
   * @param jvmargs
   *          additional arguments given when launching the server
   */
  public void startAgentServer(short sid, String[] jvmargs) throws Exception;

  /**
   * Starts a new AgentServer.
   * 
   * @param sid
   *          id of the server to start
   * @param cid
   *          cluster id of the server to start
   * @param jvmargs
   *          additional arguments given when launching the server
   */
  public void startAgentServer(short sid, short cid, String[] jvmargs) throws Exception;

  /**
   * Kills a given AgentServer. The server must have been started with this
   * Admin interface.
   * 
   * @param sid
   *          id of the server to kill
   */
  public void killAgentServer(short sid);

  /**
   * Cleanly stops an AgentServer. The server must have been started with this
   * Admin interface.
   * 
   * @param sid
   *          id of the server to stop
   */
  public void stopAgentServer(short sid) throws Exception;

  /**
   * Cleanly stops an AgentServer accessible on the given telnet port.
   * 
   * @param telnetPort
   *          the port on which the server is reachable.
   */
  public void stopAgentServerExt(int telnetPort) throws Exception;

}
