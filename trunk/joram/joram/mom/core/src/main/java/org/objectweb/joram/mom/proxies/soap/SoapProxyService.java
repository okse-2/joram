/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
 * Copyright (C) 2003 - 2004 Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.proxies.soap;

import java.util.Hashtable;

import org.objectweb.joram.mom.dest.AdminTopic;
import org.objectweb.joram.mom.notifications.GetProxyIdNot;
import org.objectweb.joram.mom.proxies.ConnectionManager;
import org.objectweb.joram.mom.proxies.OpenConnectionNot;
import org.objectweb.joram.mom.proxies.StandardConnectionContext;
import org.objectweb.joram.shared.client.AbstractJmsMessage;
import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.client.CnxCloseReply;
import org.objectweb.joram.shared.excepts.StateException;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.Queue;

/**
 * The <code>SoapProxyService</code> class implements the SOAP service
 * accessed by the JORAM clients using a <code>SoapConnection</code> for
 * connecting to the MOM. 
 * <p>
 */
public class SoapProxyService {
  /** logger */
  public static Logger logger = Debug.getLogger(SoapProxyService.class.getName());

  private Hashtable connections;

  /**
   * Service method: called by the SOAP client for instantiating the SOAP
   * service and starting the embedded JORAM server.
   *
   * @param serverId  Identifier of the embedded server.
   * @param serverName  Name of the embedded server.
   *
   * @exception Exception  If the embedded server could not start.
   */
  public void start(int serverId, String serverName) throws Exception {
    String[] args = {"" + serverId, serverName};
    AgentServer.init(args);
    AgentServer.start();

    connections = new Hashtable();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SoapProxyService started.");
  }

  /**
   * Service method: returns the identifier of a given user connection, 
   * or -1 if it is not a valid user of the SOAP proxy.
   *
   * @param identityMap Map of the user Identity.
   * @param heartBeat
   * @return connection identifier
   * @exception Exception  If the proxy is not deployed.
   */
  public int setConnection(Hashtable identityMap, 
                           int heartBeat) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "SoapProxyService.setConnection(" + identityMap + ',' + heartBeat + ')');

    Identity identity = (Identity) Identity.soapDecode(identityMap);
    
    GetProxyIdNot gpin = new GetProxyIdNot(identity, null);
    gpin.invoke(AdminTopic.getDefault());
    AgentId proxyId = gpin.getProxyId();
    
    OpenConnectionNot ocn = new OpenConnectionNot(false, heartBeat);	
    ocn.invoke(proxyId);

    StandardConnectionContext cc = (StandardConnectionContext) ocn.getConnectionContext();
    ProxyConnectionContext pcc = new ProxyConnectionContext(proxyId, cc.getQueue());
    connections.put(new ConnectionKey(identity.getUserName(), cc.getKey()), pcc);
    
    return cc.getKey();
  }
  
  /**
   * Service method: passes a hashtable containing an
   * <code>AbstractJmsRequest</code> client request or MOM messages to the
   * proxy.
   *
   * @param cnxId  The sending connection.
   * @param h  Hashtable containing a request or MOM messages.
   *
   * @exception Exception  If the connection has been closed.
   */
  public void send(String name, int cnxId, java.util.Hashtable h) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "SoapProxyService.send(" + name + ',' + cnxId + ',' + h + ')');

    AbstractJmsRequest request = (AbstractJmsRequest) AbstractJmsMessage.soapDecode(h);
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + " forwards request " + request + " with id " + request.getRequestId());

    ProxyConnectionContext ctx = (ProxyConnectionContext) connections.get(new ConnectionKey(name, cnxId));
    if (ctx == null)
      throw new StateException("Connection " + name + ':' + cnxId + " closed.");

    ConnectionManager.sendToProxy(ctx.proxyId, cnxId, request, request);
  }

  /**
   * Service method: returns a Hashtable containing an
   * <code>AbstractJmsReply</code> reply or MOM messages destinated to a
   * given connection context.
   *
   * @param cnxId  The identifier of the requesting connection.
   *
   * @exception Exception  If the connection has been closed.
   */
  public java.util.Hashtable getReply(String name, int cnxId) throws Exception {
    ConnectionKey ckey = new ConnectionKey(name, cnxId);
    ProxyConnectionContext ctx = (ProxyConnectionContext) connections.get(ckey);
    if (ctx == null)
      throw new StateException("Connection " + name + ':' + cnxId + " closed.");

    Object obj = ctx.replyQueue.get();
    if (obj instanceof Exception) {
      connections.remove(ckey);
      throw (Exception)obj;
    }
    
    AbstractJmsReply reply = (AbstractJmsReply) obj;
    ctx.replyQueue.pop();
    if (reply instanceof CnxCloseReply)
      connections.remove(ckey);

    return reply.soapCode();
  }

  static class ConnectionKey {
    private String userName;
    private int key;

    public ConnectionKey(String userName, int key) {
      this.userName = userName;
      this.key = key;
    }

    public int hashCode() {
      return userName.hashCode() + key;
    }

    public boolean equals(Object obj) {
      if (obj instanceof ConnectionKey) {
        ConnectionKey ck = (ConnectionKey)obj;
        return (ck.userName.equals(userName) &&
                ck.key == key);
      }
      return false;
    }
  }

  static class ProxyConnectionContext {
    AgentId proxyId;    
    Queue replyQueue;

    ProxyConnectionContext(AgentId proxyId,
                      Queue replyQueue) {
      this.proxyId = proxyId;
      this.replyQueue = replyQueue;
    }
  }
}
