/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France-Telecom R&D
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
package org.objectweb.joram.mom.proxies;

import java.util.Hashtable;
import java.util.StringTokenizer;

import org.objectweb.joram.mom.dest.AdminTopic;
import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.JmsRequestGroup;
import org.objectweb.joram.shared.client.ProducerMessages;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;

/**
 * A <code>ConnectionManager</code> is started as a service in each
 * MOM agent server for allowing connections with external clients.
 */
public class ConnectionManager {
  
  public static final String MULTI_CNX_SYNC = 
    "org.objectweb.joram.mom.proxies.ConnectionManager.multiCnxSync";
  
  public static final String MULTI_CNX_SYNC_DELAY =
    "org.objectweb.joram.mom.proxies.ConnectionManager.multiCnxSyncDelay";
  
  private static boolean multiCnxSync = AgentServer.getBoolean(MULTI_CNX_SYNC);
  
  private static long multiThreadSyncDelay = 
    AgentServer.getLong(MULTI_CNX_SYNC_DELAY, 1).longValue();
  
  public static final void sendToProxy(AgentId proxyId, int cnxKey,
      AbstractJmsRequest req, Object msg) {
    RequestNot rn = new RequestNot(cnxKey, msg);
    if (multiCnxSync
        && (req instanceof ProducerMessages || 
            req instanceof JmsRequestGroup)) {
      MultiCnxSync mcs = ConnectionManager.getMultiCnxSync(proxyId);
      mcs.send(rn);
    } else {
      Channel.sendTo(proxyId, rn);
    }
    if (req instanceof ProducerMessages) {
      FlowControl.flowControl();
    }
  }
  
  public static final long getMultiThreadSyncDelay() {
    return multiThreadSyncDelay;
  }
  
  private static Hashtable multiCnxSyncTable = new Hashtable();
  
  public static MultiCnxSync getMultiCnxSync(AgentId proxyId) {
    synchronized (multiCnxSyncTable) {
      MultiCnxSync mcs = (MultiCnxSync) multiCnxSyncTable.get(proxyId);
      if (mcs == null) {
        mcs = new MultiCnxSync(proxyId);
        multiCnxSyncTable.put(proxyId, mcs);
      }
      return mcs;
    }
  }
  
  /**
   *  Limit of incoming messages flow (msgs/s) requested if any, default
   * value is -1 (no limitation).
   *  This value can be adjusted by setting <tt>ConnectionManager.inFlow</tt>
   * property. This property can be fixed either from <code>java</code>
   * launching command, or in <code>a3servers.xml</code> configuration file.
   */
  public static int inFlow = -1;

  /**
   * Initializes the connection manager as a service.
   * Creates and deploys the administration topic, the connection manager
   * agent and if requested the  administration user proxy.
   *
   * @param args name and password of the administrator (optional).
   * @param firstTime  <code>true</code> when the agent server starts.
   * @exception Exception  Thrown when processing the String argument
   *   or in case of a problem when deploying the ConnectionFactory.
   */
  public static void init(String args, boolean firstTime) 
    throws Exception {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG,
                                "ConnectionManager.init(" + args + ',' + firstTime + ')');    
    if (! firstTime) return;

    AdminTopic adminTopic = new AdminTopic();
    adminTopic.deploy();
    
    inFlow = AgentServer.getInteger("ConnectionManager.inFlow", inFlow).intValue();

    if (args != null) {
      String initialAdminName = null;
      String initialAdminPass = null;
      StringTokenizer st = new StringTokenizer(args);

      if (st.countTokens() >= 1) {
        initialAdminName = st.nextToken();     
      } 
      if (st.hasMoreTokens()) {
        initialAdminPass = st.nextToken();   
      }
      
      // AF: deprecated, will be deleted.
      if (st.hasMoreTokens()) {
        try {
          inFlow = Integer.parseInt(st.nextToken());
        } catch (Exception exc) {
          inFlow = -1;
        }
      }

      if (initialAdminName != null) {
        UserAgent userAgent = new UserAgent(AgentId.JoramAdminPxStamp);
        userAgent.deploy();

        Identity identity = 
          createIdentity(Identity.getRootName(initialAdminName), 
              initialAdminPass, 
              Identity.getRootIdentityClass(initialAdminName));
        AdminNotification adminNot =
          new AdminNotification(userAgent.getId(), identity);

        Channel.sendTo(adminTopic.getId(), adminNot);
      }
    }
  }

  /**
   * Create an admin Identity.
   * 
   * @param adminName         Name of the admin.
   * @param adminPassword     Password of the admin.
   * @param identityClassName identity class name.
   * @return identity  admin Identity.
   * @throws Exception
   */
  private static Identity createIdentity(
      String adminName, 
      String adminPassword, 
      String identityClassName) throws Exception {
    Identity identity = null;
    try {
      Class clazz = Class.forName(identityClassName);
      identity = (Identity) clazz.newInstance();
      if (adminPassword != null)
        identity.setIdentity(adminName, adminPassword);
      else
        identity.setUserName(adminName);
    } catch (Exception e) {
      if (JoramTracing.dbgProxy.isLoggable(BasicLevel.ERROR))
        JoramTracing.dbgProxy.log(BasicLevel.ERROR, "EXCEPTION:: ConnectionManager.createIdentity: ", e);
      throw new Exception(e.getMessage());
    }
    return identity;
  }
  
  /**
   * Stops the <code>ConnectionManager</code> service.
   */ 
  public static void stopService() {
    if (JoramTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgProxy.log(BasicLevel.DEBUG, "ConnectionManager.stop()");
  }
}
