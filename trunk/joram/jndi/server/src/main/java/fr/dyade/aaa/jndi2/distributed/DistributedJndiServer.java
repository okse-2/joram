/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.jndi2.distributed;

import java.net.ServerSocket;
import java.util.StringTokenizer;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.jndi2.server.AgentEntryPoint;
import fr.dyade.aaa.jndi2.server.Container;
import fr.dyade.aaa.jndi2.server.JndiServer;
import fr.dyade.aaa.jndi2.server.TcpEntryPoint;
import fr.dyade.aaa.jndi2.server.TcpServer;
import fr.dyade.aaa.jndi2.server.Trace;

/**
 * Class of a JNDI server that belongs to a distributed JNDI
 * configuration. All the servers know each other. A naming 
 * context is owned by a unique server and is replicated in
 * all the servers known by the owner server.
 */
public class DistributedJndiServer {

  private static TcpServer tcpServer;

  public static void init(String args, boolean firstTime) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "DistributedJndiServer.init(" + args + ',' + firstTime + ')');
    StringTokenizer st = new StringTokenizer(args);
    String portS = st.nextToken();
    int port = Integer.parseInt(portS);

    Vector list = new Vector();
    while (st.hasMoreTokens()) {
      String idS = st.nextToken();
      Short id = Short.valueOf(idS);
      list.addElement(id);
    }

    short[] serverIds = new short[list.size()];
    for (int i = 0; i < list.size(); i++) {
      serverIds[i] = 
        ((Short)list.elementAt(i)).shortValue();
    }

    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    ServerSocket serverSocket = new ServerSocket(port);

    int poolSize = AgentServer.getInteger(JndiServer.POOL_SIZE_PROP, JndiServer.DEFAULT_POOL_SIZE).intValue();

    int timeout = AgentServer.getInteger(JndiServer.SO_TIMEOUT_PROP, JndiServer.DEFAULT_SO_TIMEOUT).intValue();

    tcpServer = new TcpServer(serverSocket, poolSize, timeout, getDefault());

    if (firstTime) {
      ReplicationManager manager = new ReplicationManager(serverIds);
      AgentEntryPoint agentEP = new AgentEntryPoint();    
      agentEP.setRequestManager(manager);
      TcpEntryPoint tcpEP = new TcpEntryPoint();
      tcpEP.setRequestManager(manager);
      ReplicationEntryPoint replicationEP = new ReplicationEntryPoint();    
      replicationEP.setRequestManager(manager);

      Container container = new Container();    
      container.addEntryPoint(agentEP);
      container.addEntryPoint(tcpEP);
      container.addEntryPoint(replicationEP);
      container.setLifeCycleListener(manager);
      manager.setContainer(container);
      container.deploy();
    }

    tcpServer.start();
  }

  /**
   * Stops the service.
   */ 
  public static void stopService() {
    tcpServer.stop();
  }

  /**
   * Returns the default JndiServer id on the local agent server.
   *
   * @return the <code>AgentId</code> of the JndiServer
   */
  public static AgentId getDefault() {
    return getDefault(AgentServer.getServerId());
  }

  /**
   * Returns the default JndiServer id on the given agent server.
   *
   * @param serverId the id of the agent server
   * @return the <code>AgentId</code> of the JndiServer
   */
  public static AgentId getDefault(short serverId) {
    return new AgentId(
      serverId, serverId,
      AgentId.LocalJndiServiceStamp);
  }
}
