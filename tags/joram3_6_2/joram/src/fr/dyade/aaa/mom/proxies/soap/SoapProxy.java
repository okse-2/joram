/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
 */
package fr.dyade.aaa.mom.proxies.soap;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.DeleteNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.excepts.ProxyException;
import fr.dyade.aaa.mom.jms.*;
import fr.dyade.aaa.mom.proxies.ProxyImpl;
import fr.dyade.aaa.util.Queue;

import org.objectweb.util.monolog.api.BasicLevel;

import java.util.Hashtable;
import java.util.Vector;
import java.lang.reflect.Method;

/**
 * A <code>SoapProxy</code> is a SOAP proxy for JMS clients connecting to the
 * MOM through a <code>SoapConnection</code>.
 */ 
public class SoapProxy extends Agent
                       implements fr.dyade.aaa.mom.proxies.ProxyAgentItf
{
  /**
   * Static reference to this agent AgentId for the convenience of the
   * <code>fr.dyade.aaa.mom.dest.AdminTopicImpl</code> class.
   */
  public static AgentId id = null;

  /**
   * Static reference to this proxy for the convenience of the
   * <code>SoapProxyService</code>.
   */
  static SoapProxy ref;

  /**
   * The reference to the <code>ProxyImpl</code> object providing this
   * proxy with its behaviour.
   */
  private ProxyImpl proxyImpl;

  /** Initial SOAP administrator's name of the local server. */
  private String initialAdminName;
  /** Initial SOAP administrator's password of the local server. */
  private String initialAdminPass;

  /** Table holding the alive connections. */
  private Hashtable cnxTable = new Hashtable();

  /** Table holding the queues of replies destinated to the proxy's clients. */
  private Hashtable repliesTable = new Hashtable();

  /** Connections counter. */
  private int cnxCounter = 1;


  /**
   * Initializes the <code>SoapProxy</code> as a service.
   *
   * @exception Exception  If the proxy could not be deployed.
   */
  public static void init(String args, boolean firstTime) throws Exception
  {
    if (! firstTime)
      return;

    SoapProxy sp;

    if (args != null) {
      java.util.StringTokenizer st = new java.util.StringTokenizer(args);
      try {
        String adminName = st.nextToken();
        String adminPass = st.nextToken();

        sp = new SoapProxy(adminName, adminPass);
      }
      catch (Exception exc) {
        throw new Exception("Invalid SoapProxy service arguments: " + args);
      }
    }
    else
      sp = new SoapProxy();
    
    sp.deploy();
  }

  /**
   * Constructs a <code>SoapProxy</code> agent.
   */
  public SoapProxy()
  {
    // SoapProxy is pinned in memory...
    super(true);

    proxyImpl = new ProxyImpl();

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Constructs a <code>SoapProxy</code> agent for an administrator client.
   *
   * @param name  Administrator's initial name.
   * @param pass  Administrator's initial password.
   */
  private SoapProxy(String name, String pass)
  {
    // SoapProxy is pinned in memory...
    super(true);
    initialAdminName = name;
    initialAdminPass = pass;

    proxyImpl = new ProxyImpl(name, pass);

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, this + ": created.");
  }


  public String toString()
  {
    return "SoapProxy:" + this.getId();
  }


  /** (Re)initializes the agent when (re)loading. */
  public void initialize(boolean firstTime) throws Exception
  {
    super.initialize(firstTime);
    proxyImpl.initialize(firstTime, this);
    
    ref = this;

    if (firstTime)
      return;

    id = this.getId();

    cnxTable.clear();
    repliesTable.clear();
  }


  /** Returns the <code>AgentId</code> of this proxy agent. */
  public AgentId getAgentId()
  {
    return getId();
  }

  /** Sends a notification to a given agent. */ 
  public void sendNot(AgentId to, Notification not)
  {
    sendTo(to, not);
  }
   
  /**
   * Sends an <code>AbstractJmsReply</code> to a given client.
   *
   * @param id  Identifies the client context within which the reply should
   *          be sent.
   * @param reply  The reply to send to the client.
   */
  public void sendToClient(int id, AbstractJmsReply reply)
  {
    Queue repliesQueue = getRepliesQueue(id);
    repliesQueue.push(reply);

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "--- " + this
                              + " pushed reply " + reply
                              + " with id " + reply.getCorrelationId()
                              + " in queue of connection: " + id);
  }

  /**
   * Overrides the <code>Agent</code> class <code>react</code> method for
   * providing the SOAP client proxy with its specific behaviour.
   */ 
  public void react(AgentId from, Notification not) throws Exception
  {
    if (not instanceof DeleteNot)
      doReact(from, (DeleteNot) not);
    else
      proxyImpl.react(from, not);
  }


  /**
   * Returns a connection identifier for a connecting user, or -1 if it is
   * not a valid user.
   */
  int setConnection(String name, String password, int timeout)
  { 
    if (name == null || password == null)
      return -1;

    try {
      fr.dyade.aaa.mom.dest.AdminTopicImpl.ref.getProxyId(name, password);
    }
    catch (Exception exc) {
      // If the requester isn't the administrator, refusing its connection:
      if (initialAdminName == null
          || initialAdminPass == null
          || ! name.equals(initialAdminName)
          || ! password.equals(initialAdminPass))
        return -1;
    }

    int cnxId = nextCnxCounter();

    cnxTable.put(new Integer(cnxId), new SoapCnx(cnxId, timeout));

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG,
                              this + ": created new connection " + cnxId);

    return cnxId;
  }

  /**
   * Decodes a Hashtable containing an <code>AbstractJmsRequest</code>
   * and passes the decoded request to the <code>ProxyImpl</code> for
   * processing.
   *
   * @exception Exception  If the connection has been closed.
   */
  void serviceReact(int cnxId, Hashtable h) throws Exception
  {
    lockConnection(cnxId);
      
    String className = (String) h.get("className");
    Class clazz = Class.forName(className);
    Class [] classParam = { new Hashtable().getClass() };
    Method m = clazz.getMethod("soapDecode",classParam);
    AbstractJmsRequest request = 
      (AbstractJmsRequest) m.invoke(null,new Object[]{h});
    
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "--- " + this
                              + " passes request " + request + " with id "
                              + request.getRequestId() + " to proxy's cnx "
                              + cnxId);
    
    if (request instanceof CnxCloseRequest) {
      serviceDoReact(cnxId, (CnxCloseRequest) request);
      return;
    }
    
    proxyImpl.reactToClientRequest(cnxId, request);
    releaseConnection(cnxId);
  }

  /**
   * Returns a hashtable containing a MOM reply destinated to a given client.
   *
   * @exception Exception  If the connection has been closed.
   */
  Hashtable getReply(int cnxId) throws Exception
  {
    lockConnection(cnxId);
    
    Queue repliesQueue = getRepliesQueue(cnxId);
    AbstractJmsReply reply = (AbstractJmsReply) repliesQueue.get();
    repliesQueue.pop();

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "--- " + this
                              + " returns reply " + reply + " with id "
                              + reply.getCorrelationId() + " to cnx "
                              + cnxId);
      
    Hashtable h = reply.soapCode();

    releaseConnection(cnxId);

    return h;
  }

  /**
   * Method implementing the SOAP proxy reaction to a
   * <code>CnxCloseRequest</code> notification notifying a
   * closing connection.
   */
  private void serviceDoReact(int cnxId, CnxCloseRequest not)
  {
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "--- " + this
                              + " notified of the closing connection: "
                              + cnxId);

    proxyImpl.closeConnection(cnxId);
    sendToClient(cnxId, new CnxCloseReply());

    cnxTable.remove(new Integer(cnxId));
    repliesTable.remove(new Integer(cnxId));

    checkConnections();
  }

  /**
   * Method implementing the JMS proxy reaction to a
   * <code>fr.dyade.aaa.agent.DeleteNot</code> notification notifying 
   * to delete the proxy.
   */
  private void doReact(AgentId from, DeleteNot not)
  {
    try {
      proxyImpl.deleteProxy(from);
      super.react(from, not);
    }
    catch (Exception exc) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
        MomTracing.dbgProxy.log(BasicLevel.WARN,
                                "Deletion request received"
                                + " from invalid agent: " + from);
    }
  }

  /**
   * Returns the <code>fr.dyade.aaa.util.Queue</code> FIFO queue dedicated
   * to a given connection context.
   */
  private Queue getRepliesQueue(int cnxId)
  {
    Integer key = new Integer(cnxId);
    Queue q = (Queue) repliesTable.get(key);

    if (q != null)
      return q;

    q = new Queue();
    q.start();

    repliesTable.put(key, q);

    return q;
  }

  /** Returns the next connection identifier. */
  private synchronized int nextCnxCounter()
  {
    if (cnxCounter == Integer.MAX_VALUE)
      cnxCounter = 0;

    return cnxCounter++;
  }

 
  /**
   * Locks a given connection so that it is not destroyed by an other thread. 
   *
   * @exception Exception  If the connection has been closed.
   */
  private synchronized void lockConnection(int id) throws Exception
  {
    SoapCnx cnx = (SoapCnx) cnxTable.get(new Integer(id));

    if (cnx == null)
      throw new Exception("Connection " + id + " has been removed.");

    cnx.locked = true;
  }

  /** Updates a given connection's expiration. */
  private synchronized void releaseConnection(int id)
  {
    try {
      SoapCnx cnx = (SoapCnx) cnxTable.get(new Integer(id));
      cnx.update();
      cnx.locked = false;
    }
    catch (Exception exc) {}
  }

  /** Checks the connections table. */
  private synchronized void checkConnections()
  {
    java.util.Enumeration ids = cnxTable.keys();
    while (ids.hasMoreElements())
      ((SoapCnx) cnxTable.get(ids.nextElement())).check();
  }
      

 
  /** The <code>SoapCnx</code> class allows to manipulate SOAP connections. */ 
  private class SoapCnx implements java.io.Serializable
  {
    /** Identifier of the connection. */
    private int id = 0;
    /** Timer of the connection, 0 for none. */
    private int timeout = 0;
    /** Expiration time of the connection, 0 for none. */
    private long expiration = 0;
    /** <code>true</code> if the connection is locked. */
    private boolean locked = false;

    /** Constructs a <code>SoapCnx</code> instance. */
    private SoapCnx(int id, int timeout)
    {
      this.id = id;
      this.timeout = timeout;
    }

    /** Updates the connection's expiration time. */
    private void update()
    {
      if (timeout > 0)
        expiration = System.currentTimeMillis() + timeout * 1000;
    }

    /** Checks the connection's validity. */
    private void check()
    {
      if (locked || timeout == 0 || System.currentTimeMillis() < expiration)
        return;

      cnxTable.remove(new Integer(id));
      
      Queue repliesQueue = getRepliesQueue(id);
      ProxyException exc = new ProxyException("Connection " + id
                                              + " is closed.");
      repliesQueue.push(new MomExceptionReply(exc));
      repliesTable.remove(new Integer(id));

      if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
        MomTracing.dbgProxy.log(BasicLevel.WARN, "Connection " + id
                                + " expired and is removed.");
    
      proxyImpl.closeConnection(id);
    } 
  }
}
