/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): David Feliot, Nicolas Tachker
 */
package fr.dyade.aaa.jndi2.server;

import java.io.*;
import javax.naming.*;
import java.net.*;
import java.util.*;

import fr.dyade.aaa.jndi2.impl.*; 
import fr.dyade.aaa.jndi2.msg.*; 
import fr.dyade.aaa.jndi2.msg.SerialOutputStream;
import fr.dyade.aaa.util.*;
import fr.dyade.aaa.agent.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * Class of a JNDI centralized server. This is an
 * agent that may be accessed either by TCP connections
 * or agent notifications.
 */
public class JndiServer extends Agent {
  
  protected static ServerSocket serverSocket;

  protected ServerSocket getServerSocket() {
    return serverSocket;
  }

  public static void init(String args, boolean firstTime) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "JndiServer.init(" + 
                       args + ',' + firstTime + ')');
    int port = Integer.parseInt(args);

    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    serverSocket = new ServerSocket(port);

    if (! firstTime) return;
    else {
      JndiServer a = new JndiServer();
      a.deploy();
    }
  }

  /**
   * Stops the <code>JndiServer</code> service.
   */ 
  public static void stopService() {
    // Do nothing   
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

  private int nbm;

  private transient TcpServer tcpServer;

  private transient ServerImpl impl;

  /**
   * Constructs a new JNDI server agent.
   * This agent cannot be swapped and has 
   * a reserved identifier on each agent server.
   */
  public JndiServer() {
    super("", true, AgentId.LocalJndiServiceStamp);
    this.nbm = 3;
  }

  /**
   * Initializes the JNDI server.
   */
  public void agentInitialize(boolean firstTime) throws Exception {
    // 1- Create the object that handles the
    //    naming data.
    impl = new ServerImpl(
      AgentServer.getTransaction(),
      getId(),
      getRootOwnerId());    
    impl.initialize();
    
    // 2- Create the TCP entry point.
    tcpServer = new TcpServer(
      getServerSocket(),
      nbm,
      getId());
    tcpServer.start();
  }

  /**
   * Returns the root naming context owner
   * identifier.
   * May be overridden by a subclass.
   */
  protected AgentId getRootOwnerId() {
    return getId();
  }

  protected final ServerImpl getServerImpl() {
    return impl;
  }

  public void agentFinalize(boolean lastTime) {
    tcpServer.stop();
  }

  /**
   * Notification entry point.
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "\n\nJndiServer.react(" + 
                       from + ',' + not + ')');
    if (not instanceof JndiScriptRequestNot) {
      doReact(from, (JndiScriptRequestNot)not);
    } else if (not instanceof TcpRequestNot) {
      doReact((TcpRequestNot)not);
    } else {
      super.react(from, not);
    }
    setNoSave();
  }

  /**
   * Reacts to a TCP connection request. This is the
   * TCP entry point.
   *
   * @param not the TCP connection request
   */
  private void doReact(TcpRequestNot not) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "JndiServer.doReact((TcpRequestNot)" + 
                       not + ')');
    Socket s = not.getSocket();
    if (s != null) {
      try {
        TcpRequestContext reqCtx = 
          new TcpRequestContext(s);
        JndiReply reply = invoke(reqCtx);
        if (reply != null) {
          reqCtx.reply(reply);
        }
      } catch (Exception exc) {
        Trace.logger.log(BasicLevel.WARN, "", exc);
      }
    } else {
      Trace.logger.log(BasicLevel.WARN, "Drop " + not);
    }
  }

  /**
   * Reacts to a JNDI script request. This is the notification
   * entry point.
   * 
   * @param not the JNDI script
   */
  private void doReact(AgentId from, JndiScriptRequestNot not) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "JndiServer.doReact(" + from +
                       ",(JndiScriptRequestNot)" + not + ')');
    JndiRequest[] requests = not.getRequests();
    JndiReply[] replies = new JndiReply[requests.length];
    for (int i = 0; i < requests.length; i++) {
      AgentRequestContext reqCtx = new AgentRequestContext(
        requests[i], from, not.reply());
      replies[i] = invoke(reqCtx);
    }
    if (not.reply()) {
      // Reply to all the operations from the input
      // script except those that are asynchronous.
      // This can't happen in a centralized server.
      // But in a distributed JNDI configuration, this
      // server may be waiting for a notification reply 
      // from an other naming server. 
      // These asynchronous operations
      // are acknowledged in separate notifications.
      sendTo(from, new JndiScriptReplyNot(replies));
    }
  }

  protected synchronized JndiReply invoke(RequestContext reqCtx) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "JndiServer.invoke(" + reqCtx + ')');
    JndiRequest request = reqCtx.getRequest();
    try {      
      if (request instanceof JndiReadRequest) {
        // 1- Dispatch the read requests
        return invokeReadRequest(reqCtx);
      } if (request instanceof JndiAdminRequest) {
        return invokeAdminRequest(reqCtx);
      } else {
        // 2- Dispatch the write requests
        return invokeWriteRequest(reqCtx);
      }
    } catch (MissingContextException mce) {
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, "", mce);
      return onMissingContext(mce, reqCtx);
    } catch (MissingRecordException nnfe) {
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, "", nnfe);
      return onMissingRecord(nnfe, reqCtx);
    } catch (NamingException nexc) {
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, "", nexc);
      return new JndiError(nexc);
    }
  }

  protected synchronized JndiReply invokeReadRequest(RequestContext reqCtx) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "JndiServer.invokeReadRequest(" + reqCtx + ')');
    JndiRequest request = reqCtx.getRequest();
    if (request instanceof LookupRequest) {
      Object obj = lookup((LookupRequest)request);
      if (obj != null) {
        ObjectRecord or = (ObjectRecord)obj;
        return new LookupReply(or.getObject());
      } else {
        // This is a context record
        return new JndiReply();
      }
    } else if (request instanceof ListBindingsRequest) {
      Object obj = listBindings((ListBindingsRequest)request);
      return new ListBindingsReply((Binding[])obj);
    } else if (request instanceof ListRequest) {
      Object obj = list((ListRequest)request);
      return new ListReply((NameClassPair[])obj);
    } else {
      return new JndiError(
        new NamingException("Unknown operation"));
    }
  }

  protected synchronized JndiReply invokeWriteRequest(
    RequestContext reqCtx)
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "JndiServer.invokeWriteRequest(" + 
                       reqCtx + ',' + ')');
    try {
      JndiRequest request = reqCtx.getRequest();
      JndiReply reply;
      if (request instanceof BindRequest) {
        bind((BindRequest)request);        
        return new JndiReply();
      } else if (request instanceof UnbindRequest) {
        unbind((UnbindRequest)request);
        return new JndiReply();
      } else if (request instanceof CreateSubcontextRequest) {
        createSubcontext(
          (CreateSubcontextRequest)request);
        return new JndiReply();
      } else if (request instanceof DestroySubcontextRequest) {
        destroySubcontext(
          (DestroySubcontextRequest)request);
        return new JndiReply();
      } else {
        return new JndiError(
          new NamingException("Unknown operation"));
      }
    } catch (NotOwnerException noexc) {
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, "", noexc);
      return invokeOwner(
        (AgentId)noexc.getOwner(),
        reqCtx);
    }
  }

  protected synchronized JndiReply invokeAdminRequest(
    RequestContext reqCtx)
    throws NamingException {
    JndiRequest request = reqCtx.getRequest();
    if (request instanceof ChangeOwnerRequest) {
      changeOwner((ChangeOwnerRequest)request);
      return new JndiReply();
    } else {
      return new JndiError(
        new NamingException("Unknown admin operation"));
    }
  }

  private void bind(BindRequest request) 
    throws NamingException {
    if (request.isRebind()) {
      impl.rebind(
        request.getName(), 
        request.getObject());
    } else {
      impl.bind(
        request.getName(), 
        request.getObject());
    } 
  }

  private void unbind(UnbindRequest request) 
    throws NamingException {
    impl.unbind(request.getName());
  }

  private Record lookup(LookupRequest request) 
    throws NamingException {
    return impl.lookup(request.getName());
  }

  private NameClassPair[] list(ListRequest request) 
    throws NamingException {
    return impl.list(request.getName());
  }

  private Binding[] listBindings(ListBindingsRequest request) 
    throws NamingException {
    return impl.listBindings(request.getName());
  }

  protected void createSubcontext(CreateSubcontextRequest request) 
    throws NamingException {
    impl.createSubcontext(
      request.getName());
  }

  private void destroySubcontext(DestroySubcontextRequest request)
    throws NamingException {
    impl.destroySubcontext(
      request.getName());
  }

  protected void changeOwner(ChangeOwnerRequest request)
    throws NamingException {
    AgentId serverId;
    try {
      serverId =
        AgentId.fromString(request.getOwnerId());
    } catch (Exception exc) {
      NamingException ne = 
        new NamingException(exc.toString());
      ne.setRootCause(exc);
      throw ne;
    }
    if (getId().equals(serverId))
      throw new NamingException("Server already owner");
    impl.changeOwner(serverId);
  }

  /**
   * A centralized JNDI server returns a JNDI error 
   * explaining that this server is not the owner 
   * of the context on which the JNDI operation is called.
   * A subclass may override this behavior in order
   * to invoke the owner of the naming context.
   *
   * @param owner the identifier of the naming server that 
   * owns the naming context on which the
   * JNDI operation is called.
   * 
   * @param reqCtx the JNDI request context that raised
   * the exception.
   *
   * @return the JNDI reply.
   * May be <code>null</code> if the owner invocation 
   * is asynchronous.
   */
  protected JndiReply invokeOwner(AgentId owner,
                                  RequestContext reqCtx) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "JndiServer.invokeOwner(" +
                       owner + ',' + reqCtx + ')');
    return new JndiError(new NamingException("Not owner"));
  }
  
  /**
   * In a centralized JNDI server a missing context shows 
   * that the naming data are unconsistent. So it throws an error.
   * A subclass may override this behavior in order
   * to try to resolve the missing context.
   *
   * @param mce the missing context exception
   *
   * @param reqCtx the JNDI request context that raised
   * the exception.
   *
   * @return the JNDI reply.
   * May be <code>null</code> if the resolution is asynchronous.   
   */
  protected JndiReply onMissingContext(MissingContextException mce,
                                       RequestContext reqCtx) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "JndiServer.onMissingContext(" +
                       mce + ',' + reqCtx + ')');
    throw new Error(mce.toString());
  }

  /**
   * In a centralized JNDI server a missing record shows that the
   * name asked by the JNDI request doesn't exist. So the
   * <code>NameNotFoundException</code> is directly forwarded to 
   * the client.
   * A subclass may override this behavior in order
   * to try to resolve the missing record.
   * 
   * @param mre the missing context exception
   *
   * @param reqCtx the JNDI request context that raised
   * the exception.
   * 
   * @return the JNDI reply.
   * May be <code>null</code> if the resolution is asynchronous.
   */
  protected JndiReply onMissingRecord(MissingRecordException mre,
                                      RequestContext reqCtx) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "JndiServer.onMissingRecord(" +
                       mre + ',' + reqCtx + ')');
    return new JndiError(mre.getNameNotFoundException());
  }

}

