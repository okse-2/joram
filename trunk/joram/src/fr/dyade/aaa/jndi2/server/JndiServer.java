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
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): David Feliot, Nicolas Tachker
 */
package fr.dyade.aaa.jndi2.server;

import java.io.*;
import javax.naming.*;
import java.net.*;

import fr.dyade.aaa.jndi2.impl.*; 
import fr.dyade.aaa.jndi2.msg.*; 
import fr.dyade.aaa.jndi2.msg.SerialOutputStream;
import fr.dyade.aaa.util.*;
import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.ip.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class JndiServer extends ServerAgent {
  
  /**
   * The listen socket of the proxy is statically
   * created (@see init).
   */
  private static ServerSocket serverSocket;
  
  /**
   * Overrides the default behavior that creates a
   * server socket. The socket is statically created
   * (@see init).
   */
  protected ServerSocket getServerSocket() {
    return serverSocket;
  }

  public static void init(String args, boolean firstTime) throws Exception {    
    int port = Integer.parseInt(args);

    // Create the socket here in order to throw an exception
    // if the socket can't be created (even if firstTime is false).
    serverSocket = new ServerSocket(port);

    if (! firstTime) return;
    else {
      JndiServer a = new JndiServer();
      a.setNbMonitor(5);
      a.setPort(port); 
      a.deploy();          
    }
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
    return new AgentId(serverId, serverId, AgentId.LocalJndiServiceStamp);
  }

  private transient ServerImpl impl;
  private transient Transaction transaction;

  public JndiServer() {
    super(null, AgentId.LocalJndiServiceStamp);
  }

  public void initialize(boolean firstTime) throws Exception {
    this.transaction = AgentServer.getTransaction();
    this.impl = new ServerImpl(transaction);
    if (firstTime) {
      // The transaction is the factory's reaction transaction.
      impl.initialize();
    }
    super.initialize(firstTime);
  }

  /**
   * Stops the <code>JndiServer</code> service.
   */ 
  public static void stopService() {
    // Do nothing
  }

  public void react(AgentId from, Notification not) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "\n\nJndiServer.react(" + 
                       from + ',' + not + ')');
    if (not instanceof JndiScriptRequestNot) {
      doReact(from, (JndiScriptRequestNot)not);
    } else {
      super.react(from, not);
    }
  }

  private void doReact(AgentId from, JndiScriptRequestNot not) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "JndiServer.doReact(" + from +
                       ",(JndiScriptRequestNot)" + not + ')');
    JndiRequest[] requests = not.getRequests();
    JndiReply[] replies = new JndiReply[requests.length];
    for (int i = 0; i < requests.length; i++) {
      replies[i] = invoke(requests[i]);
    }
    if (not.reply()) {
      sendTo(from, new JndiScriptReplyNot(replies));
    }
  }
  
  public void doRequest(Monitor monitor) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "\n\nJndiServer.doRequest(" + 
                       monitor.getId() + ')');
    Socket socket = monitor.getSocket();
    SerialOutputStream sender = new SerialOutputStream(socket.getOutputStream());
    ObjectInputStream receiver  = new ObjectInputStream(socket.getInputStream());
    Loop:
    while (true) {        
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, 
                         " -> readObject (" + 
                         monitor.getId() + ')');
      
      // Get a request
      JndiRequest request;
      try {
         request = (JndiRequest)receiver.readObject();
      } catch (IOException exc) {
        if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, 
                         " -> exit (" + 
                         monitor.getId() + ')');
        break Loop;
      }
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, " -> begins (" + 
                         monitor.getId() + ')');
      // Start a transaction
      transaction.begin();
      JndiReply reply = invoke(request);
      if (reply instanceof JndiError) {
        if (Trace.logger.isLoggable(BasicLevel.DEBUG))
          Trace.logger.log(BasicLevel.DEBUG, " -> rollback (" + 
                           monitor.getId() + ')');
        // Rollback
        transaction.rollback();
        transaction.release();
      } else {
        if (Trace.logger.isLoggable(BasicLevel.DEBUG))
          Trace.logger.log(BasicLevel.DEBUG, " -> commit (" + 
                           monitor.getId() + ')');
        // Commit
        transaction.commit();
        transaction.release();
      } 
      sender.writeObject(reply);
    }
  }

  private JndiReply invoke(JndiRequest request) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "JndiServer.invoke(" + request + ')');
    try {
      if (request instanceof BindRequest) {
        bind((BindRequest)request);
        return new JndiReply();
      } else if (request instanceof LookupRequest) {
        Object obj = lookup((LookupRequest)request);
        if (obj instanceof ObjectRecord) {
          ObjectRecord or = (ObjectRecord)obj;
          return new LookupReply(or.getObject());
        } else {
          // This is a context record
          return new JndiReply();
        }
      } else if (request instanceof UnbindRequest) {
        unbind((UnbindRequest)request);
        return new JndiReply();
      } else if (request instanceof ListBindingsRequest) {
        Object obj = listBindings((ListBindingsRequest)request);
        return new ListBindingsReply((Binding[])obj);
      } else if (request instanceof ListRequest) {
        Object obj = list((ListRequest)request);
        return new ListReply((NameClassPair[])obj);
      } else if (request instanceof CreateSubcontextRequest) {
        createSubcontext((CreateSubcontextRequest)request);
        return new JndiReply();
      } else if (request instanceof DestroySubcontextRequest) {
        destroySubcontext((DestroySubcontextRequest)request);
        return new JndiReply();
      } else {
        return new JndiError(new NamingException("Unknown operation"));
      }
    } catch (NamingException nexc) {
      return new JndiError(nexc);
    } catch (IOException ioexc) {
      NamingException nexc2 = new NamingException();
      nexc2.setRootCause(ioexc);
      return new JndiError(nexc2);
    }
  }

  private void bind(BindRequest request) throws NamingException, IOException {
    if (request.isRebind()) {
      impl.rebind(request.getName(), request.getObject());
    } else {
      impl.bind(request.getName(), request.getObject());
    }    
  }

  private void unbind(UnbindRequest request) throws NamingException, IOException {
    impl.unbind(request.getName());
  }

  private Record lookup(LookupRequest request) throws NamingException, IOException {
    return impl.lookup(request.getName());
  }

  private NameClassPair[] list(ListRequest request) throws NamingException, IOException {
    return impl.list(request.getName());
  }

  private Binding[] listBindings(ListBindingsRequest request) throws NamingException, IOException {
    return impl.listBindings(request.getName());
  }

  private void createSubcontext(CreateSubcontextRequest request) throws NamingException, IOException {
    impl.createSubcontext(request.getName());
  }

  private void destroySubcontext(DestroySubcontextRequest request) throws NamingException, IOException {
    impl.destroySubcontext(request.getName());
  }

  private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
    // Do nothing
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
    // Do nothing
  }
}

