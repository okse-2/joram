/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.jndi2.server;

import java.net.ServerSocket;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.jndi2.impl.Trace;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 *  Class of a JNDI centralized server. This agent may be accessed either by
 * TCP connections or agent notifications.
 */
public class JndiServer {
  /**
   *  This property allows to Enable/disable SO_TIMEOUT with the specified timeout
   * in milliseconds, default value is 10.000L (10 second).
   * <p>
   *  This property can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  public static final String SO_TIMEOUT_PROP = "fr.dyade.aaa.jndi2.server.soTimeout";

  /**
   * Default value for SO_TIMEOUT property.
   */
  public static final int DEFAULT_SO_TIMEOUT = 10000;

  /**
   *  This property allows to set the number of listening thread, default value is 3.
   * <p>
   *  This property can be fixed either from <code>java</code> launching
   * command, or in <code>a3servers.xml</code> configuration file.
   */
  public static final String POOL_SIZE_PROP = "fr.dyade.aaa.jndi2.server.poolSize";

  /**
   * Default value for POOL_SIZE property.
   */
  public static final int DEFAULT_POOL_SIZE = 3;
  
  private static TcpServer tcpServer;

  public static void init(String args, boolean firstTime) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG,
                       "JndiServer.init(" + args + ',' + firstTime + ')');
    
    int port = Integer.parseInt(args);

    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    ServerSocket serverSocket = new ServerSocket(port);

    int poolSize = AgentServer.getInteger(POOL_SIZE_PROP, DEFAULT_POOL_SIZE).intValue();

    int timeout = AgentServer.getInteger(SO_TIMEOUT_PROP, DEFAULT_SO_TIMEOUT).intValue();

    tcpServer = new TcpServer(serverSocket, poolSize, timeout, getDefault());

    if (firstTime) {
      RequestManager manager = new RequestManager();
      AgentEntryPoint agentEP = new AgentEntryPoint();    
      agentEP.setRequestManager(manager);
      TcpEntryPoint tcpEP = new TcpEntryPoint();
      tcpEP.setRequestManager(manager);
      Container container = new Container();    
      container.addEntryPoint(agentEP);
      container.addEntryPoint(tcpEP);
      container.setLifeCycleListener(manager);
      manager.setContainer(container);
      container.deploy();
    }

    try {
      MXWrapper.registerMBean(tcpServer, "JNDI", "service=tcp");
    } catch (Exception exc) {
      Trace.logger.log(BasicLevel.WARN, " JNDI server jmx failed", exc);
    }

    tcpServer.start();
  }
  
  /**
   * Stops the <code>JndiServer</code> service.
   */ 
  public static void stopService() {
    try {
      MXWrapper.unregisterMBean("JNDI", "service=tcp");
    } catch (Exception exc) {
      Trace.logger.log(BasicLevel.WARN, "JNDI server jmx failed", exc);
    }

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

