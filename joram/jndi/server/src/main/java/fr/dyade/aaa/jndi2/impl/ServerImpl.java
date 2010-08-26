/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
package fr.dyade.aaa.jndi2.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.ContextNotEmptyException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.util.Transaction;

public class ServerImpl {

   public final static String LOOSE_COUPLING = "fr.dyade.aaa.jndi2.impl.LooseCoupling";
   public static boolean looseCoupling;

  /**
   * Identifier of this server.
   */
  private Object serverId;

  /**
   * Identifier of the server that owns
   * the root naming context.
   */
  private Object rootOwnerId;

  /**
   * Optional update listener.
   * May be <code>null</code>.
   */
  private UpdateListener updateListener;
  
  /**
   * A context manager for the factory
   * operations (new, delete). It also
   * handles a cache and the persistency.
   */
  private ContextManager contextManager;

  /**
   * Constructs a <code>ServerImpl</code>
   *
   * @param transaction Transactional context that 
   * provides atomicity for the write operations
   * performed during a request.
   *
   * @param serverId Identifier of this server.
   *
   * @param rootOwnerId Identifier of the server 
   * that owns the root naming context.
   */
  public ServerImpl(Transaction transaction,
                    Object serverId,
                    Object rootOwnerId) { 
    this.serverId = serverId;
    this.rootOwnerId = rootOwnerId;
    contextManager = new ContextManager(
      transaction, serverId, rootOwnerId);
    
    looseCoupling = AgentServer.getBoolean(LOOSE_COUPLING);
  }

  public void setUpdateListener(UpdateListener updateListener) {
    this.updateListener = updateListener;
  }
  
  public void initialize() throws Exception {
    contextManager.initialize();
    // Creates the root naming context if this
    // server owns it.
    if (rootOwnerId.equals(serverId) || looseCoupling ) {
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.initialize : create root NamingContext" );
      NamingContext rootNc = 
        contextManager.getRootNamingContext();
      if (rootNc == null) {
        rootNc = contextManager.newNamingContext(
            serverId, 
            null, 
            new CompositeName());
      }
    }
  }
  
  /**
   * Binds an object to the specified path.
   *
   * @param path the path of the object
   *
   * @param obj the object to bind
   *
   * @exception NameAlreadyBoundException if the name of
   * the subcontext is already bound.
   * 
   * @exception NameNotFoundException if some of the
   * intermediate names in the path don't exist.
   * 
   * @exception NotOwnerException if the owner of the 
   * parent context is checked and is not the local
   * naming server.
   */
  public void bind(CompositeName path, 
                   Object obj) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.bind(" + 
                       path + ',' + 
                       obj + ',' + ')');
    // The root context is (in a way) already bound since
    // it is already a context.
    if (path.size() == 0) throw new NameAlreadyBoundException();

    path = (CompositeName)path.clone();
    String lastName = (String)path.remove(path.size() - 1);
    NamingContext nc = contextManager.getNamingContext(path);

    bind(nc, lastName, obj, serverId);
    
    if (updateListener != null) {
      updateListener.onUpdate(
        new BindEvent(path,nc.getId(), lastName, obj));
    }
  }

  public void bind(NamingContext nc, 
                   String lastName, 
                   Object obj,
                   Object ownerId) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ServerImpl.bind(" + 
                       nc + ',' +
                       lastName + ',' + 
                       obj + ',' + 
                       ownerId + ')');
    if (! nc.getOwnerId().equals(ownerId) && (!looseCoupling) ) {
      throw new NotOwnerException(
        nc.getOwnerId());
    }

    Record r = nc.getRecord(lastName);
    if (r != null) throw new NameAlreadyBoundException();
    else {
      nc.addRecord(new ObjectRecord(lastName, obj));
      contextManager.storeNamingContext(nc);      
    }
  }

  /**
   * Rebinds an object to the specified path.
   *
   * @param path the path of the object
   *
   * @param obj the object to rebind
   *
   * @exception NameNotFoundException if some of the
   * intermediate names in the path don't exist.
   * 
   * @exception NotOwnerException if the owner of the 
   * parent context is checked and is not the local
   * naming server.
   *
   * @exception NamingException if the specified path
   * is bound to a naming context.
   */
  public void rebind(CompositeName path, 
                     Object obj) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.rebind(" + 
                       path + ',' + 
                       obj + ',' + ')');

    // The root context cannot become a name-value pair.
    if (path.size() == 0) throw new NamingException("Cannot rebind the root context");

    path = (CompositeName)path.clone();
    String lastName = (String)path.remove(path.size() - 1);
    NamingContext nc = contextManager.getNamingContext(path);    

    rebind(nc, lastName, obj, serverId);

    if (updateListener != null) {
      updateListener.onUpdate(
        new RebindEvent(path,nc.getId(), lastName, obj));
    }
  }

  public void rebind(NamingContext nc, 
                     String lastName, 
                     Object obj,
                     Object ownerId) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ServerImpl.rebind(" + 
                       nc + ',' +
                       lastName + ',' + 
                       obj + ',' + 
                       ownerId + ')');
    if (! nc.getOwnerId().equals(ownerId) && (!looseCoupling) ) {
      throw new NotOwnerException(
        nc.getOwnerId());
    }

    Record r = nc.getRecord(lastName);
    if (r != null) {
      if (r instanceof ContextRecord) {
        // DF: seems not consistent to delete recursively the whole
        // context (empty or not) as the reverse operation is not possible
        // (create a context with a name already bound). 
        // So prefer to raise an error.
        // Have to check the spec.
        throw new NamingException("Cannot rebind a context");
      } else {
        ObjectRecord or = (ObjectRecord)r;
        or.setObject(obj);
      }
    } else {
      nc.addRecord(new ObjectRecord(lastName, obj));
    }
    contextManager.storeNamingContext(nc);    
  }

  /**
   * Looks up the specified path.
   *
   * @param path the path to look up
   *
   * @exception NameNotFoundException if some of the
   * names (intermediate and final) in the path don't exist.
   * 
   * @exception NotOwnerException if the owner of the 
   * parent context is checked and is not the local
   * naming server.
   *
   * @return <code>null</code> if the bound object is a context.
   *
   * @exception NameNotFoundException 
   */
  public Record lookup(CompositeName path) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.lookup(" + path + ')');

    if (path.size() == 0) {
      return null;
    }

    path = (CompositeName)path.clone();
    String lastName = (String)path.remove(path.size() - 1);
    NamingContext nc = contextManager.getNamingContext(path);
    
    Record r = nc.getRecord(lastName);
    if (r == null) {
      NameNotFoundException nnfe = 
        new NameNotFoundException();
      nnfe.setResolvedName(path);
      throw new MissingRecordException(
        nc.getId(), nc.getOwnerId(), nnfe);
    } else if (r instanceof ObjectRecord) {      
      return r;
    } else {
      return null;
    }
  }

  /**
   * Unbinds the specified path. This operation is
   * idempotent: does nothing if the final name of
   * the path is not found.
   *
   * @param path the path to unbind
   *
   * @exception NameNotFoundException if some of the
   * intermediate names in the path don't exist.
   * 
   * @exception NotOwnerException if the owner of the 
   * parent context is checked and is not the local
   * naming server.
   *
   * @exception NamingException if the specified path
   * is bound to a naming context.
   */
  public void unbind(CompositeName path) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.unbind(" + 
                       path + ')');

    // The root context cannot be deleted.
    if (path.size() == 0) throw new NamingException("Cannot unbind the root context");

    path = (CompositeName)path.clone();    
    String lastName = (String)path.remove(path.size() - 1);
    NamingContext nc = contextManager.getNamingContext(path);    

    if (unbind(nc, lastName, serverId)) {
      if (updateListener != null) {
        updateListener.onUpdate(
          new UnbindEvent(path,nc.getId(), lastName));
      }
    }
  }

  public boolean unbind(NamingContext nc,
                        String lastName,
                        Object ownerId) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ServerImpl.unbind(" + 
                       nc + ',' +
                       lastName + ',' + 
                       ownerId + ')');
    if (! nc.getOwnerId().equals(ownerId) && (!looseCoupling) ) {
      throw new NotOwnerException(
        nc.getOwnerId());
    }
    Record r = nc.getRecord(lastName);
    if (r != null) { 
      if (r instanceof ContextRecord) {
        throw new NamingException("Cannot unbind a context");
      } else {
        nc.removeRecord(lastName);
        contextManager.storeNamingContext(nc);        
        return true;
      }
    } else {
      // else do nothing (idempotency)
      return false;
    }
  }

  public NameClassPair[] list(CompositeName path) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.list(" + path + ')');
    NamingContext nc = contextManager.getNamingContext(path);
    return nc.getNameClassPairs();
  }

  public Binding[] listBindings(CompositeName path) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.listBindings(" + path + ')');
    NamingContext nc = contextManager.getNamingContext(path);
    return nc.getBindings();
  }

  public void createSubcontext(CompositeName path) 
    throws NamingException {
    createSubcontext(path, serverId);
  }

  /**
   * Create a subcontext.
   *
   * @param path the path of the subcontext
   *
   * @param subcontextOwner identifier of the owner of
   * the subcontext (<code>null</code> if the 
   * owner is the local naming server).
   *
   * @exception NameAlreadyBoundException if the name of
   * the subcontext is already bound.
   * 
   * @exception NameNotFoundException if some of the
   * intermediate names in the path don't exist.
   * 
   * @exception NotOwnerException if the owner of the 
   * parent context is checked and is not the local
   * naming server.
   */
  public void createSubcontext(CompositeName path,
                               Object subcontextOwnerId) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(
        BasicLevel.DEBUG, 
        "ServerImpl.createSubcontext(" + 
        path + ',' + subcontextOwnerId + ')');

    // The root already exists.
    if (path.size() == 0) throw new NameAlreadyBoundException();

    CompositeName parentPath = (CompositeName)path.clone();    
    String lastName = 
      (String)parentPath.remove(parentPath.size() - 1);
    NamingContext parentNc = 
      contextManager.getNamingContext(parentPath);

    NamingContextId ncid = createSubcontext(
      parentNc, lastName, path, null,
      subcontextOwnerId, serverId);

    if (updateListener != null) {
      updateListener.onUpdate(
        new CreateSubcontextEvent(
          parentNc.getId(), lastName, path, ncid, 
          subcontextOwnerId));
    }
  }
    
  public NamingContextId createSubcontext(
    NamingContext parentNc, 
    String lastName,
    CompositeName path,
    NamingContextId ncid,
    Object subcontextOwnerId,
    Object ownerId) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ServerImpl.createSubcontext(" + 
                       parentNc + ',' +
                       lastName + ',' + 
                       path + ',' + 
                       ncid + ',' + 
                       subcontextOwnerId + ',' +
                       ownerId + ')');
    if (! parentNc.getOwnerId().equals(ownerId) && (!looseCoupling) ) {
      throw new NotOwnerException(
        parentNc.getOwnerId());
    }

    if (parentNc.getRecord(lastName) != null) 
      throw new NameAlreadyBoundException();
    NamingContext nc;
    if(!   looseCoupling)
	 nc = 
	    contextManager.newNamingContext(
					    subcontextOwnerId, ncid, path);
    else
	 nc = 
	    contextManager.newNamingContext(
					    subcontextOwnerId, null, path);
    parentNc.addRecord(new ContextRecord(
      lastName, nc.getId()));
    contextManager.storeNamingContext(parentNc);
    return nc.getId();
  }

  /**
   * Destroy a subcontext. This operation is
   * idempotent: does nothing if the final name of
   * the path is not found.
   *
   * @param path the path of the subcontext
   *
   * @exception NameAlreadyBoundException if the name of
   * the subcontext is already bound.
   * 
   * @exception NameNotFoundException if some of the
   * intermediate names in the path don't exist.
   * 
   * @exception NotOwnerException if the owner of the 
   * parent context is checked and is not the local
   * naming server.
   * 
   * @exception NotContextException if the specified path
   * isn't bound to a context.
   */
  public void destroySubcontext(CompositeName path) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ServerImpl.destroySubcontext(" + 
                       path + ')');

    if (path.size() == 0) 
      throw new NamingException("Cannot delete root context.");

    CompositeName parentPath = (CompositeName)path.clone();
    String lastName = (String)parentPath.remove(parentPath.size() - 1);
    NamingContext parentNc = contextManager.getNamingContext(parentPath);
    
    try {
      NamingContext nc = contextManager.getNamingContext(path);
      if (nc.size() > 0) {
        if (Trace.logger.isLoggable(BasicLevel.DEBUG))
          Trace.logger.log(BasicLevel.DEBUG, 
                           " -> not empty: nc = " + nc);
        throw new ContextNotEmptyException();
      }
    } catch (MissingRecordException exc) {
      // else do nothing (idempotency)
      return;
    }

    if (destroySubcontext(parentNc, lastName, path, serverId)) {
      if (updateListener != null) {
        updateListener.onUpdate(
          new DestroySubcontextEvent(
            parentNc.getId(), lastName, path));
      }
    }
  }

  public boolean destroySubcontext(NamingContext parentNc,                                    
                                   String lastName,
                                   CompositeName path,
                                   Object ownerId) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ServerImpl.destroySubcontext(" + 
                       parentNc + ',' +
                       lastName + ',' + 
                       path + ',' +
                       ownerId + ')');
    if (! parentNc.getOwnerId().equals(ownerId) && (!looseCoupling)) {
      throw new NotOwnerException(
        parentNc.getOwnerId());
    }

    Record r = parentNc.getRecord(lastName);
    if (r != null) {
      if (r instanceof ContextRecord) {        
        ContextRecord cr = (ContextRecord)r;
        NamingContextId ctxId = cr.getId();
        contextManager.delete(ctxId, path);
        
        // Remove from the parent context
        parentNc.removeRecord(lastName);
        contextManager.storeNamingContext(parentNc);        
        return true;
      } else {
        throw new NotContextException();
      }
    } else {
      // else do nothing (idempotency)
      return false;
    }
  }

  /**
   * Returns copies of the naming contexts owned by the server
   * which identifier is specified.
   *
   * @param serverId the identifier of the server that owns
   * the naming contexts to get.
   */
  public NamingContextInfo[] copyNamingContexts(Object serverId) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
    Trace.logger.log(BasicLevel.DEBUG, 
                     "ServerImpl.copyNamingContexts(" + serverId + ')');
    return contextManager.copyNamingContexts(serverId);
  }

  public NamingContext getNamingContext(NamingContextId ncid)
    throws NamingException {
    return contextManager.getNamingContext(ncid);
  }

    
 public NamingContext getNamingContext(CompositeName name)
    throws NamingException {
    return contextManager.getNamingContext(name);
  }
  
 public void storeNamingContext(NamingContext nc)
    throws NamingException{
         contextManager.storeNamingContext( nc);
    }

  public void addNamingContext(NamingContextInfo ncInfo)
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ServerImpl.addNamingContext(" + 
                       ncInfo + ')');
    contextManager.addNamingContext(ncInfo);
  }
    
 public NamingContext newNamingContext(Object ownerId,NamingContextId ncid,CompositeName name)
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ServerImpl.newNamingContext(" + 
                       name + ')');
    return contextManager.newNamingContext(ownerId, ncid, name);
  }


  public void changeOwner(CompositeName name, Object newOwnerId)
    throws NamingException {
    NamingContextInfo[] contexts = 
      contextManager.changeOwner(
          name, serverId, newOwnerId);  
    if (updateListener != null) {
      updateListener.onUpdate(
        new ChangeOwnerEvent(
          newOwnerId,
          contexts));
    }
  }

  public void resetNamingContext(NamingContext context)
    throws NamingException {
    contextManager.resetNamingContext(context);
  }

  public void writeBag(ObjectOutputStream out)
    throws IOException {
    contextManager.writeBag(out);
  }

  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
    contextManager.readBag(in);
  }
}

  
