/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2001 Dyade
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
 * Contributor(s): David Feliot (ScalAgent DT)
 *                 Andre Freyssinet (ScalAgent DT)
 */
package org.objectweb.joram.mom.proxies;

import fr.dyade.aaa.agent.*;

import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.mom.dest.*;

import org.objectweb.util.monolog.api.BasicLevel;

import java.util.*;

/**
 * A <code>ConnectionManager</code> is started as a service in each
 * MOM agent server for allowing connections with external clients.
 */
public class ConnectionManager extends Agent {
  /** Incoming messages flow (msgs/s) requested, if any (-1 if none). */
  public static int inFlow = -1;

  /**
   * Identifier of the connection manager agent.
   */
  private static AgentId mgrId;

  /**
   * Timer provided by the connection manager.
   */
  private static fr.dyade.aaa.util.Timer timer;

  private static Hashtable connections = new Hashtable();

  /**
   * Returns the identifier of the connection manager agent.
   */
  public final static AgentId getManagerId() {
    return mgrId;
  }

  /**
   * Returns the timer provided by the connection manager.
   */
  public final static fr.dyade.aaa.util.Timer getTimer() {
    return timer;
  }

  static void removeConnection(UserConnection c) {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, 
        "ConnectionManager.removeConnection(" + c + ')');
    CKey ck = new CKey(
      c.getUserName(), c.getKey());
    connections.remove(ck);
  }

  public static UserConnection getConnection(String name, int key) {
    CKey ck = new CKey(name, key);
    return (UserConnection)connections.get(ck);
  }

  /**
   * Initializes the connection manager as a service.
   * Creates and deploys the aministration topic, the
   * connection manager agent and if requested the 
   * administration user proxy.
   *
   * @param args name and passwiord of the administrator (optional).
   * @param firstTime  <code>true</code> when the agent server starts.
   * @exception Exception  Thrown when processing the String argument
   *   or in case of a problem when deploying the ConnectionFactory.
   */
  public static void init(String args, boolean firstTime) 
    throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "ConnectionManager.init(" + args + ',' + firstTime + ')');    

    timer = new fr.dyade.aaa.util.Timer();

    if (! firstTime) return;

    AdminTopic adminTopic = new AdminTopic();
    adminTopic.deploy();

    ConnectionManager mgr = new ConnectionManager();
    mgr.deploy();
    mgrId = mgr.getId();

    if (args != null) {
      String initialAdminName = null;
      String initialAdminPass = null;
      StringTokenizer st = new StringTokenizer(args);

      if (st.countTokens() >= 2) {
        initialAdminName = st.nextToken();
        initialAdminPass = st.nextToken();        
      }
      
      if (st.hasMoreTokens()) {
        try {
          inFlow = Integer.parseInt(st.nextToken());
        } catch (Exception exc) {
          inFlow = -1;
        }
      }

      if (initialAdminName != null && initialAdminPass != null) {
        UserAgent userAgent = new UserAgent();
        userAgent.deploy();

        AdminNotification adminNot =
          new AdminNotification(
            userAgent.getId(),
            initialAdminName,
            initialAdminPass);

        Channel.sendTo(adminTopic.getId(), adminNot);
      }
    }
  }

  /**
   * Stops the <code>ConnectionManager</code> service.
   */ 
  public static void stopService() {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG,
        "ConnectionManager.stop()");
    // Do nothing
  }

  /**
   * Creates a <code>UserConnection</code> to communicate 
   * with the user specified by its name and password.
   *
   * @param userName name of the user
   * @param userPassword password of the user
   * @param timeout disconnection timeout
   * @param context a context associated with the connection
   * (e.g. tcp reliable context)
   */
  public static UserConnection openConnection(
    String userName,
    String userPassword,
    int timeout,
    Object context) throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "ConnectionManager.openConnection(" + 
        userName + ',' + userPassword + ',' + timeout + ')');
    if (mgrId == null) throw new IllegalStateException(
      "Connection manager not initialized");
    UserConnection uc = new UserConnection(
      userName, userPassword, timeout, context);
    try {
      OpenConnectionNot not = 
        new OpenConnectionNot(uc);
      synchronized (uc) {
        Channel.sendTo(mgrId, not);
        uc.wait();
        Exception exc = not.getException();
        if (exc != null) {
          throw exc;
        }
        connections.put(
          new CKey(userName, uc.getKey()), uc);
      }
    } catch (InterruptedException exc) {}
    return uc;
  }  

  private ConnectionManager() {
    super(true);
  }

  public void agentInitialize(boolean firstTime) 
    throws Exception {
    mgrId = getId();
  }

  public void react(AgentId from, Notification not) 
    throws Exception {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(
        BasicLevel.DEBUG, "ConnectionManager.react(" + 
        from + ',' + not + ')');
    if (not instanceof OpenConnectionNot) {
      doReact(from, (OpenConnectionNot)not);
    } else {
      super.react(from, not);
    }
  }

  /**
   * Forwards the open connection request to the specified
   * user proxy.
   */
  private void doReact(AgentId from, OpenConnectionNot not) {
    UserConnection uc = not.getUserConnection();
    if (uc != null) {
      try {
        AgentId proxyId = AdminTopicImpl.ref.getProxyId(
          uc.getUserName(), 
          uc.getUserPassword());
        sendTo(proxyId, not);
      } catch (Exception exc) {
        if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgProxy.log(BasicLevel.DEBUG, "", exc);
        synchronized (uc) {
          not.setException(exc);
          uc.notify();
        }
      }
    }
  }

  static class CKey {
    private String name;
    private int key;

    CKey(String name,
         int key) {
      this.name = name;
      this.key = key;
    }

    public int hashCode() {
      return key;
    }

    public boolean equals(Object obj) {
      if (obj instanceof CKey) {
        CKey ck = (CKey)obj;
        if (! name.equals(ck.name)) return false;
        return key == ck.key;
      }
      return false;
    }
  }
}
