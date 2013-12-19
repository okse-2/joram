/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2013 ScalAgent Distributed Technologies
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
import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.dest.QueueArrivalState;
import org.objectweb.joram.mom.dest.QueueDeliveryTable;
import org.objectweb.joram.mom.messages.Message;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.GetProxyIdNot;
import org.objectweb.joram.mom.util.JoramHelper;
import org.objectweb.joram.mom.util.MessageIdListFactory;
import org.objectweb.joram.mom.util.MessageIdListImpl;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.JmsRequestGroup;
import org.objectweb.joram.shared.client.CommitRequest;
import org.objectweb.joram.shared.client.MomExceptionReply;
import org.objectweb.joram.shared.client.ProducerMessages;
import org.objectweb.joram.shared.client.ServerReply;
import org.objectweb.joram.shared.excepts.DestinationException;
import org.objectweb.joram.shared.excepts.MomException;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Callback;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.CountDownCallback;
import fr.dyade.aaa.agent.UnknownAgentException;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.EncodableFactoryRepository;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * A <code>ConnectionManager</code> is started as a service in each
 * MOM agent server for allowing connections with external clients.
 */
public class ConnectionManager implements ConnectionManagerMBean {
  /** logger */
  public static Logger logger = Debug.getLogger(ConnectionManager.class.getName());

  /**
   *  Name of property allowing to activate the synchronization mode of multiples
   * connections.
   * <p>
   *  This mode allows to pack commands that occurs in a same time in order to minimize
   * the number of transactions.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command or
   * a3servers.xml configuration file.
   */
  public static final String MULTI_CNX_SYNC = 
    "org.objectweb.joram.mom.proxies.ConnectionManager.multiCnxSync";
  
  /**
   * True if the synchronization mode is activated.
   */
  private static boolean multiCnxSync = AgentServer.getBoolean(MULTI_CNX_SYNC);

  /**
   *  Name of property allowing to configure the synchronization mode of multiples
   * connections.
   * <p>
   *  This property allows to define the duration of instant to pack the commands.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command or
   * a3servers.xml configuration file.
   */
  public static final String MULTI_CNX_SYNC_DELAY =
    "org.objectweb.joram.mom.proxies.ConnectionManager.multiCnxSyncDelay";
  
  /**
   * Duration in ms of instant to pack the commands.
   */
  private static long multiThreadSyncDelay = 
    AgentServer.getLong(MULTI_CNX_SYNC_DELAY, 1).longValue();
  
  /**
   *  Name of property allowing to define the threshold beyond which the flow-control
   * of incoming messages is activated. Default value is 25.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command or
   * a3servers.xml configuration file.
   */
  public static final String CTRLFLOW_THRESHOLD =
    "org.objectweb.joram.mom.proxies.ConnectionManager.CtrlFlowThreshold";
  
  /**
   * Threshold beyond which the flow-control of incoming messages is activated.
   */
  private static int ctrlFlowThreshold = AgentServer.getInteger(CTRLFLOW_THRESHOLD, 25).intValue();
  
  /**
   *  Name of property allowing to define the average throughput of the server for the
   * calculation of flow control. Default value is 100000.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command or
   * a3servers.xml configuration file.
   */
  public static final String CTRLFLOW_THROUGHPUT =
    "org.objectweb.joram.mom.proxies.ConnectionManager.CtrlFlowThroughput";
  
  /**
   *  Definition of average delay for the implementation of flow control (it is computed
   * from the CTRLFLOW_THROUGHPUT parameter.
   */
  private static long ctrlFlowDelay = 1000000000L/AgentServer.getLong(CTRLFLOW_THROUGHPUT, 100000L).longValue();
  
  public static final String DIRECT_NOTIFICATION =
    "org.objectweb.joram.mom.proxies.ConnectionManager.DirectNotification";
  
  private static boolean directNotification = AgentServer.getBoolean(DIRECT_NOTIFICATION);

  private static final String MBEAN_NAME = "type=Connection";
  
  /** <code>true</code> if new connections are accepted. */
  private boolean activated = true;
  
  /** List of registered managers (tcp, ssl, local, ...) */
  private List managers = new ArrayList();

  /** Unique ConnectionManager instance. */
  private static ConnectionManager currentInstance;
  
  static {
    EncodableFactoryRepository.putFactory(JoramHelper.MESSAGE_CLASS_ID, new Message.MessageFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.QUEUE_CLASS_ID, new Queue.QueueFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.USER_AGENT_CLASS_ID, new UserAgent.UserAgentFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.CLIENT_SUBSCRIPTION_CLASS_ID, new ClientSubscription.ClientSubscriptionFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.CLIENT_CONTEXT_CLASS_ID, new ClientContext.ClientContextFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.MESSAGE_ID_LIST_IMPL_CLASS_ID, new MessageIdListImpl.MessageIdListImplEncodableFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.USER_AGENT_ARRIVAL_STATE_CLASS_ID, new UserAgentArrivalState.UserAgentArrivalStateFactory());
    EncodableFactoryRepository.putFactory(JoramHelper.QUEUE_DELIVERY_TABLE_CLASS_ID, new QueueDeliveryTable.Factory());
    EncodableFactoryRepository.putFactory(JoramHelper.QUEUE_ARRIVAL_STATE_CLASS_ID, new QueueArrivalState.Factory());
  }
  
  private static CountDownCallback createCallback(final AbstractJmsRequest req, final ConnectionContext ctx) {
    return new CountDownCallback(new Callback() {
      
      public void failed(List<Throwable> errors) {
        if (! directNotification) {
          // Errors are handled by UserAgent
          return;
        }
        
        Throwable error = errors.get(0);
        if (error instanceof MomException) {
          MomException exc = (MomException) error;
          ctx.pushReply(new MomExceptionReply(req.getRequestId(), exc));
        } else if (error instanceof UnknownAgentException) {
          UnknownAgentException uae = (UnknownAgentException) error;
          DestinationException exc = new DestinationException("Destination "
              + uae.getUnknownAgentId() + " does not exist.");
          ctx.pushReply(new MomExceptionReply(req.getRequestId(), exc));
        } else {
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, errors.toString(), error);
        }
      }
      
      public void done() {
        ctx.pushReply(new ServerReply(req.getRequestId()));
      }
    });
  }
  
  private static void flowControl() {
    int load = AgentServer.getEngineLoad();
    if (load > ctrlFlowThreshold) {
      try {
        long delay = load * ctrlFlowDelay;
        Thread.sleep(delay/1000000, (int) (delay%1000000));
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  public static final void sendToProxy(AgentId proxyId, int cnxKey,
      AbstractJmsRequest req, Object msg, ConnectionContext ctx) {
    RequestNot rn = new RequestNot(cnxKey, msg);
    if (multiCnxSync
        && (req instanceof ProducerMessages || 
            req instanceof JmsRequestGroup)) {
      MultiCnxSync mcs = ConnectionManager.getMultiCnxSync(proxyId);
      mcs.send(rn);
    } else {
      if (req instanceof ProducerMessages) {
        if (! ((ProducerMessages) req).getAsyncSend()) {
          rn.setCountDownCallback(createCallback(req, ctx));
        }
        flowControl();
      } else if (req instanceof CommitRequest) {
        if (! ((CommitRequest) req).getAsyncSend()) {
          rn.setCountDownCallback(createCallback(req, ctx));
        }
        flowControl();
      }
      
      if (directNotification) {
        if (req instanceof ProducerMessages) {
          ProducerMessages pm = (ProducerMessages) req;
          AgentId destId = AgentId.fromString(req.getTarget());

          if (pm.getAsyncSend()) {
            ClientMessages not = new ClientMessages(cnxKey, pm.getRequestId(),
                pm.getMessages());
            not.setPersistent(false);
            not.setExpiration(0L);
            not.setProxyId(proxyId);
            not.setAsyncSend(true);
            Channel.sendTo(destId, not);
          } else {
            if (destId.getTo() == proxyId.getTo()) {
              ClientMessages not = new ClientMessages(cnxKey,
                  pm.getRequestId(), pm.getMessages());
              not.setPersistent(false);
              not.setExpiration(0L);
              not.setProxyId(proxyId);
              not.setAsyncSend(false);    
              not.setCountDownCallback(createCallback(req, ctx));
              Channel.sendTo(destId, not);
            } else {
              // Remote destination
              Channel.sendTo(proxyId, rn);
            }
          }
        } else {
          Channel.sendTo(proxyId, rn);
        }
      } else {
        Channel.sendTo(proxyId, rn);
      }
    }
    if ((inFlow != -1) && (req instanceof ProducerMessages)) {
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
  
  /**
   * Activates/deactivates the connection manager.
   * 
   * @param activate true, activates the connection manager.
   */
  public static void setActivate(boolean activate) {
    if (currentInstance == null) return;
    if (activate) {
      currentInstance.activate();
    } else {
      currentInstance.deactivate();
    }
  }
}
