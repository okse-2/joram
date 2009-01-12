/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
 * Initial developer(s): David Feliot
 */
package fr.dyade.aaa.jndi2.ha;

import java.net.ServerSocket;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.jndi2.server.AgentEntryPoint;
import fr.dyade.aaa.jndi2.server.Container;
import fr.dyade.aaa.jndi2.server.RequestManager;
import fr.dyade.aaa.jndi2.server.Trace;

public class HAJndiServer {

  private static HATcpServer tcpServer;

  public static void init(String args, boolean firstTime) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "HAJndiServer.init(" + 
                       args + ',' + firstTime + ')');
    int port = Integer.parseInt(args);

    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    ServerSocket serverSocket = new ServerSocket(port);

    tcpServer = new HATcpServer(
      serverSocket,
      3,
      getDefault());
    tcpServer.start();

    if (firstTime) {
      RequestManager manager = new RequestManager();
      
      HARequestManager haManager = new HARequestManager();
      haManager.setRequestManager(manager);

      AgentEntryPoint agentEP = new AgentEntryPoint();    
      agentEP.setRequestManager(manager);
      
      HAEntryPoint haEP = new HAEntryPoint();
      haEP.setHARequestManager(haManager);

      Container container = new Container();    
      container.addEntryPoint(agentEP);
      container.addEntryPoint(haEP);
      container.setLifeCycleListener(haManager);
      container.setBagSerializer(haManager);
      manager.setContainer(container);
      container.deploy();
    }
  }

  /**
   * Stops the <code>JndiServer</code> service.
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
