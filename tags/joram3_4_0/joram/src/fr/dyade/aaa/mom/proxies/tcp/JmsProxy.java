/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.proxies.tcp;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.mom.MomTracing;
import fr.dyade.aaa.mom.jms.AbstractJmsReply;
import fr.dyade.aaa.mom.jms.AbstractJmsRequest;
import fr.dyade.aaa.mom.proxies.ProxyImpl;

import org.objectweb.util.monolog.api.BasicLevel;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;


/**
 * A <code>JmsProxy</code> is a TCP proxy for "classical" JMS clients and
 * administrator.
 */ 
public class JmsProxy extends ConnectionFactory
                      implements fr.dyade.aaa.mom.proxies.ProxyAgentItf
{
  /**
   * The reference to the <code>ProxyImpl</code> object providing this
   * proxy with its behaviour.
   */
  private ProxyImpl proxyImpl;


  /**
   * Constructs a <code>JmsProxy</code> agent.
   */
  public JmsProxy()
  {
    super();
    super.multiConn = true;

    proxyImpl = new ProxyImpl(this);
  
    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, this + ": created.");
  }

  /**
   * Constructor called by a <code>ConnectionFactory</code> instance for
   * building a proxy for an administrator client.
   *
   * @param name  Name of the administrator.
   * @param pass  Password of the administrator.
   */
  JmsProxy(String name, String pass)
  {
    super();
    super.multiConn = true;

    proxyImpl = new ProxyImpl(this, name, pass);

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, this + ": created.");
  }

  /** Returns a string view of this JMS proxy. */
  public String toString()
  {
    return "JmsProxy:" + this.getId();
  }


  /**
   * Specializes this <code>Agent</code> method called when (re)deploying 
   * the proxy.
   */
  public void initialize(boolean firstTime) throws Exception
  {
    super.initialize(firstTime);

    if (firstTime)
      return;

    // Reinitializing the proxy:
    proxyImpl.reinitialize(this);
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
   * @param id  Identifies the connection to send the reply through.
   * @param reply  The reply to send to the client.
   */
  public void sendToClient(int id, AbstractJmsReply reply)
  {
    OutputNotification not = new OutputNotification(reply);

    try { 
      super.sendOut(id, not);
    }
    // Closed or broken connection:
    catch (Exception e) {
      if (MomTracing.dbgProxy.isLoggable(BasicLevel.WARN))
        MomTracing.dbgProxy.log(BasicLevel.WARN,
                                "Connection " + id
                                + " broken or closed: could not send reply: "
                                + reply.getClass().getName() + " with id: "
                                + reply.getCorrelationId());
    }
  }

  /**
   * Overrides the <code>Agent</code> class <code>react</code> method for
   * providing the JMS client proxy with its specific behaviour.
   * <p>
   * A JMS proxy specifically reacts to the following notifications:
   * <ul>
   * <li><code>DriverDone</code></li>
   * <li><code>DeleteNot</code></li>
   * </ul>
   */ 
  public void react(AgentId from, Notification not) throws Exception
  {
    if (not instanceof DriverDone)
      doReact(from, (DriverDone) not);
    else if (not instanceof DeleteNot)
      doReact(from, (DeleteNot) not);
    else {
      try {
        proxyImpl.react(from, not);
      }
      catch (UnknownNotificationException exc) {
        super.react(from, not);
      }
    }
  }


  /**
   * Creates a (chain of) filter(s) for transforming the specified InputStream
   * into a <code>TcpInputStream</code>.
   *
   * @param in   An InputStream for this proxy.
   * @return  A NotificationInputStream for this proxy.
   */
  protected NotificationInputStream setInputFilters(InputStream in)
    throws StreamCorruptedException, IOException
  {
    return (new TcpInputStream(in));
  }

  /**
   * Creates a (chain of) filter(s) for transforming the specified
   * OutputStream into a <code>TcpOutputStream</code>.
   *
   * @param out  An OutputStream for this proxy.
   * @return  A NotificationOutputStream for this proxy.
   */
  protected NotificationOutputStream setOutputFilters(OutputStream out)
    throws IOException
  {
    return (new TcpOutputStream(out));
  }


  /**
   * This method overrides the <code>ProxyAgent</code> class
   * <code>driverReact</code> method called by the drivers "in" when filtering
   * a notification out of the input stream.
   * <p>
   * Passes the wrapped <code>AbstractJmsRequest</code> to the
   * <code>ProxyImpl</code> for processing.
   */
  protected void driverReact(int key, Notification not)
  {
    try {
      InputNotification iNot = (InputNotification) not;
      AbstractJmsRequest request = (AbstractJmsRequest) iNot.getObj();
      proxyImpl.reactToClientRequest(key, request);
    }
    // Can't happen as a notification coming from a DriverIn necessarily is
    // an InputNotification wrapping a JMS request.
    catch (ClassCastException exc) {}
  }

 
  /**
   * Method implementing the JMS proxy reaction to a
   * <code>fr.dyade.aaa.agent.DriverDone</code> notification notifying a
   * broken connection.
   */
  private void doReact(AgentId from, DriverDone not) throws Exception
  {
    int cKey = not.getDriverKey();

    if (MomTracing.dbgProxy.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgProxy.log(BasicLevel.DEBUG, "--- " + this
                              + " notified of the closing connection: "
                              + cKey);

    proxyImpl.closeConnection(cKey);
    super.react(from, not);
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
}
