/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France-Telecom R&D
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.objectweb.joram.mom.dest.AdminTopic;
import org.objectweb.joram.mom.notifications.AcknowledgeRequest;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.GetProxyIdNot;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.ConsumerAckRequest;
import org.objectweb.joram.shared.client.JmsRequestGroup;
import org.objectweb.joram.shared.client.ProducerMessages;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * A <code>ConnectionManager</code> is started as a service in each
 * MOM agent server for allowing connections with external clients.
 */
public class ConnectionManager implements ConnectionManagerMBean {
  /** logger */
  public static Logger logger = Debug.getLogger(ConnectionManager.class.getName());

  public static final String MULTI_CNX_SYNC = 
    "org.objectweb.joram.mom.proxies.ConnectionManager.multiCnxSync";
  
  public static final String MULTI_CNX_SYNC_DELAY =
    "org.objectweb.joram.mom.proxies.ConnectionManager.multiCnxSyncDelay";
  
  private static boolean multiCnxSync = AgentServer.getBoolean(MULTI_CNX_SYNC);
  
  private static long multiThreadSyncDelay = 
    AgentServer.getLong(MULTI_CNX_SYNC_DELAY, 1).longValue();

  private static final String MBEAN_NAME = "type=Connection";
  
  /** <code>true</code> if new connections are accepted. */
  private boolean activated = true;
  
  /** List of registered managers (tcp, ssl, local, ...) */
  private List managers = new ArrayList();

  /** Unique ConnectionManager instance. */
  private static ConnectionManager currentInstance;
  
  public static final boolean DIRECT_NOTIFICATION = true;
  
  public static final void sendToProxy(AgentId proxyId, int cnxKey,
      AbstractJmsRequest req, Object msg) {
    /* JORAM_PERF_BRANCH:
    RequestNot rn = new RequestNot(cnxKey, msg);
    if (multiCnxSync
        && (req instanceof ProducerMessages || 
            req instanceof JmsRequestGroup)) {
      MultiCnxSync mcs = ConnectionManager.getMultiCnxSync(proxyId);
      mcs.send(rn);
    } else {
      if (req instanceof ProducerMessages) {
        rn.setPriority(0);
      }
      Channel.sendTo(proxyId, rn);
    }*/
    // JORAM_PERF_BRANCH:
    if (multiCnxSync
        && (req instanceof ProducerMessages || 
            req instanceof JmsRequestGroup)) {
      RequestNot rn = new RequestNot(cnxKey, msg);
      MultiCnxSync mcs = ConnectionManager.getMultiCnxSync(proxyId);
      mcs.send(rn);
    } else {
      if (DIRECT_NOTIFICATION) {
        if (req instanceof ProducerMessages) {
          ProducerMessages pm = (ProducerMessages) req;
          AgentId destId = AgentId.fromString(req.getTarget());
          ClientMessages not = new ClientMessages(cnxKey, pm.getRequestId(),
              pm.getMessages());
          if (destId.getTo() == proxyId.getTo()) {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, " -> local sending");
            not.setPersistent(false);
            not.setExpiration(0L);
            if (pm.getAsyncSend()) {
              not.setAsyncSend(true);
            }
          } else {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, " -> remote sending");
            if (!pm.getAsyncSend()) {
              Channel.sendTo(proxyId,
                  new SendReplyNot(cnxKey, pm.getRequestId()));
            }
          }
          not.setPriority(0);
          Channel.sendTo(destId, not);
        } else if (req instanceof ConsumerAckRequest) {
          ConsumerAckRequest car = (ConsumerAckRequest) req;
          if (car.getQueueMode()) {
            AgentId qId = AgentId.fromString(req.getTarget());
            AcknowledgeRequest not = new AcknowledgeRequest(cnxKey,
                car.getRequestId(), car.getIds());
            if (qId.getTo() == proxyId.getTo()) {
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, " -> local acking");
              not.setPersistent(false);
              Channel.sendTo(qId, not);
            } else {
              Channel.sendTo(qId, not);
            }
          } else {
            RequestNot rn = new RequestNot(cnxKey, msg);
            Channel.sendTo(proxyId, rn);
          }
        } else {
          RequestNot rn = new RequestNot(cnxKey, msg);
          Channel.sendTo(proxyId, rn);
        }
      } else { 
        RequestNot rn = new RequestNot(cnxKey, msg);
        Channel.sendTo(proxyId, rn);
      }
    }
    // JORAM_PERF_BRANCH.
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
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
        UserAgent userAgent = new UserAgent(initialAdminName, AgentId.JoramAdminPxStamp);
        userAgent.deploy();

        Identity identity = createIdentity(Identity.getRootName(initialAdminName), 
                                           initialAdminPass, 
                                           Identity.getRootIdentityClass(initialAdminName));

        AdminNotification adminNot = new AdminNotification(userAgent.getId(), identity);
        Channel.sendTo(adminTopic.getId(), adminNot);
      }
      
      try {
        MXWrapper.registerMBean(getCurrentInstance(), "Joram#" + AgentServer.getServerId(), MBEAN_NAME);
      } catch (Exception e) {
        logger.log(BasicLevel.DEBUG, "registerMBean", e);
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
  private static Identity createIdentity(String adminName, 
                                         String adminPassword, 
                                         String identityClassName) throws Exception {
    Identity identity = null;
    try {
    	Class<?> clazz = Class.forName(identityClassName);
    	identity = (Identity) clazz.newInstance();
    	if (adminPassword != null)
        identity.setIdentity(adminName, adminPassword);
      else
        identity.setUserName(adminName);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: ConnectionManager.createIdentity: ", e);
      throw new Exception(e.getMessage());
    }
    return identity;
  }
  
  /**
   * Stops the <code>ConnectionManager</code> service.
   */ 
  public static void stopService() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ConnectionManager.stop()");
    try {
      MXWrapper.unregisterMBean("Joram#" + AgentServer.getServerId(), MBEAN_NAME);
    } catch (Exception e) {
      logger.log(BasicLevel.DEBUG, "unregisterMBean", e);
    }
    getCurrentInstance().removeAllManagers();
  }

  public void activate() {
    for (Iterator iterator = managers.iterator(); iterator.hasNext();) {
      ConnectionManagerMBean cnxManager = (ConnectionManagerMBean) iterator.next();
      cnxManager.activate();
    }
    activated = true;
  }

  public void closeAllConnections() {
    for (Iterator iterator = managers.iterator(); iterator.hasNext();) {
      ConnectionManagerMBean cnxManager = (ConnectionManagerMBean) iterator.next();
      cnxManager.closeAllConnections();
    }
  }

  public void deactivate() {
    for (Iterator iterator = managers.iterator(); iterator.hasNext();) {
      ConnectionManagerMBean cnxManager = (ConnectionManagerMBean) iterator.next();
      cnxManager.deactivate();
    }
    activated = false;
  }

  public int getRunningConnectionsCount() {
    int count = 0;
    for (Iterator iterator = managers.iterator(); iterator.hasNext();) {
      ConnectionManagerMBean cnxManager = (ConnectionManagerMBean) iterator.next();
      count += cnxManager.getRunningConnectionsCount();
    }
    return count;
  }

  public boolean isActivated() {
    return activated;
  }

  public synchronized static ConnectionManager getCurrentInstance() {
    if (currentInstance == null) {
      currentInstance = new ConnectionManager();
    }
    return currentInstance;
  }

  /**
   * Registers a new manager.
   */
  public void addManager(ConnectionManagerMBean manager) {
    managers.add(manager);
    try {
      MXWrapper.registerMBean(manager, "Joram#" + AgentServer.getServerId(), manager.getMBeanName());
    } catch (Exception e) {
      logger.log(BasicLevel.DEBUG, "registerMBean", e);
    }
  }

  /**
   * Removes a registered manager.
   */
  public void removeManager(ConnectionManagerMBean manager) {
    boolean isRemoved = managers.remove(manager);
    if (isRemoved) {
      try {
        MXWrapper.unregisterMBean("Joram#" + AgentServer.getServerId(), manager.getMBeanName());
      } catch (Exception e) {
        logger.log(BasicLevel.DEBUG, "unregisterMBean", e);
      }
    }
  }
  
  private void removeAllManagers() {
    ConnectionManagerMBean[] array = (ConnectionManagerMBean[]) managers.toArray(new ConnectionManagerMBean[managers.size()]);
    for (int i = 0; i < array.length; i++) {
      removeManager(array[i]);
    }
  }

  public String getMBeanName() {
    return MBEAN_NAME;
  }

  public int getFailedLoginCount() {
    int failedCount = 0;
    for (Iterator iterator = managers.iterator(); iterator.hasNext();) {
      ConnectionManagerMBean cnxManager = (ConnectionManagerMBean) iterator.next();
      failedCount += cnxManager.getFailedLoginCount();
    }
    return failedCount;
  }

  public int getInitiatedConnectionCount() {
    int initCount = 0;
    for (Iterator iterator = managers.iterator(); iterator.hasNext();) {
      ConnectionManagerMBean cnxManager = (ConnectionManagerMBean) iterator.next();
      initCount += cnxManager.getInitiatedConnectionCount();
    }
    return initCount;
  }

  /**
   * Checks the validity of the given name and password.
   * 
   * @param userName the name of the user
   * @param password the password of the user
   * @return <code>true</code> if the name and password match an existing user.
   */
  public boolean checkCredentials(String userName, String password) {
    try {
      Identity identity = createIdentity(Identity.getRootName(userName), password,
          Identity.getRootIdentityClass(userName));
      GetProxyIdNot gpin = new GetProxyIdNot(identity, null);
      gpin.invoke(AdminTopic.getDefault());
      return true;
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      return false;
    }
  }
}
