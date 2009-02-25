/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): David Feliot
 */
package fr.dyade.aaa.jndi2.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.NamingException;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.jndi2.impl.MissingContextException;
import fr.dyade.aaa.jndi2.impl.MissingRecordException;
import fr.dyade.aaa.jndi2.impl.NotOwnerException;
import fr.dyade.aaa.jndi2.impl.ObjectRecord;
import fr.dyade.aaa.jndi2.impl.Record;
import fr.dyade.aaa.jndi2.impl.ServerImpl;
import fr.dyade.aaa.jndi2.msg.BindRequest;
import fr.dyade.aaa.jndi2.msg.ChangeOwnerRequest;
import fr.dyade.aaa.jndi2.msg.CreateSubcontextRequest;
import fr.dyade.aaa.jndi2.msg.DestroySubcontextRequest;
import fr.dyade.aaa.jndi2.msg.JndiAdminRequest;
import fr.dyade.aaa.jndi2.msg.JndiError;
import fr.dyade.aaa.jndi2.msg.JndiReadRequest;
import fr.dyade.aaa.jndi2.msg.JndiReply;
import fr.dyade.aaa.jndi2.msg.JndiRequest;
import fr.dyade.aaa.jndi2.msg.ListBindingsReply;
import fr.dyade.aaa.jndi2.msg.ListBindingsRequest;
import fr.dyade.aaa.jndi2.msg.ListReply;
import fr.dyade.aaa.jndi2.msg.ListRequest;
import fr.dyade.aaa.jndi2.msg.LookupReply;
import fr.dyade.aaa.jndi2.msg.LookupRequest;
import fr.dyade.aaa.jndi2.msg.UnbindRequest;

public class RequestManager implements LifeCycleListener, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private Container container;

  private transient ServerImpl impl;

  public void setContainer(Container container) {
    this.container = container;
  }

  public final AgentId getId() {
    return container.getId();
  }

  public void sendTo(AgentId to, Notification not) {
    container.sendNotification(to, not);
  }

  public void agentInitialize(boolean firstTime) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "\n\nRequestManager.agentInitialize(" + 
                       firstTime + ')');
    // Create the object that handles the
    // naming data.
    impl = new ServerImpl(
      AgentServer.getTransaction(),
      getId(),
      getRootOwnerId());    
    impl.initialize();
  }

  public void agentFinalize(boolean lastTime) {}

  /**
   * Returns the root naming context owner identifier.
   * May be overridden by a subclass.
   */
  protected AgentId getRootOwnerId() {
    return getId();
  }

  protected final ServerImpl getServerImpl() {
    return impl;
  }

  public JndiReply invoke(RequestContext reqCtx) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "RequestManager.invoke(" + reqCtx + ')');
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

  protected JndiReply invokeReadRequest(RequestContext reqCtx) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "RequestManager.invokeReadRequest(" + reqCtx + ')');
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

  protected JndiReply invokeWriteRequest(
    RequestContext reqCtx)
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "RequestManager.invokeWriteRequest(" + reqCtx + ',' + ')');
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

  protected JndiReply invokeAdminRequest(
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

  private void bind(BindRequest request) throws NamingException {
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

  private void unbind(UnbindRequest request) throws NamingException {
    impl.unbind(request.getName());
  }

  private Record lookup(LookupRequest request) throws NamingException {
    return impl.lookup(request.getName());
  }

  private NameClassPair[] list(ListRequest request) throws NamingException {
    return impl.list(request.getName());
  }

  private Binding[] listBindings(ListBindingsRequest request) throws NamingException {
    return impl.listBindings(request.getName());
  }

  protected void createSubcontext(CreateSubcontextRequest request) throws NamingException {
    impl.createSubcontext(
      request.getName());
  }

  private void destroySubcontext(DestroySubcontextRequest request) throws NamingException {
    impl.destroySubcontext(
      request.getName());
  }

  protected void changeOwner(ChangeOwnerRequest request) throws NamingException {
    AgentId newOwnerId;
    try {
      newOwnerId =
        AgentId.fromString(request.getOwnerId());
    } catch (Exception exc) {
      NamingException ne = 
        new NamingException(exc.toString());
      ne.setRootCause(exc);
      throw ne;
    }
    if (getId().equals(newOwnerId))
      throw new NamingException("Server already owner");
    impl.changeOwner(request.getName(), newOwnerId);
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
      Trace.logger.log(BasicLevel.DEBUG, "RequestManager.invokeOwner(" +
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
      Trace.logger.log(BasicLevel.DEBUG, "RequestManager.onMissingContext(" +
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
      Trace.logger.log(BasicLevel.DEBUG, "RequestManager.onMissingRecord(" +
                       mre + ',' + reqCtx + ')');
    return new JndiError(mre.getNameNotFoundException());
  }

  public void writeBag(ObjectOutputStream out)
    throws IOException {
    impl.writeBag(out);
  }

  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
    impl = new ServerImpl(
      AgentServer.getTransaction(),
      getId(),
      getRootOwnerId());
    impl.readBag(in);
  }
}
