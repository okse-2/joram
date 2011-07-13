/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.jndi2.distributed;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.naming.CompositeName;
import javax.naming.NamingException;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.jndi2.impl.BindEvent;
import fr.dyade.aaa.jndi2.impl.ChangeOwnerEvent;
import fr.dyade.aaa.jndi2.impl.ContextRecord;
import fr.dyade.aaa.jndi2.impl.CreateSubcontextEvent;
import fr.dyade.aaa.jndi2.impl.DestroySubcontextEvent;
import fr.dyade.aaa.jndi2.impl.MissingContextException;
import fr.dyade.aaa.jndi2.impl.MissingRecordException;
import fr.dyade.aaa.jndi2.impl.NamingContext;
import fr.dyade.aaa.jndi2.impl.NamingContextInfo;
import fr.dyade.aaa.jndi2.impl.NotOwnerException;
import fr.dyade.aaa.jndi2.impl.RebindEvent;
import fr.dyade.aaa.jndi2.impl.Record;
import fr.dyade.aaa.jndi2.impl.UnbindEvent;
import fr.dyade.aaa.jndi2.impl.UpdateEvent;
import fr.dyade.aaa.jndi2.impl.UpdateListener;
import fr.dyade.aaa.jndi2.msg.ChangeOwnerRequest;
import fr.dyade.aaa.jndi2.msg.CreateSubcontextRequest;
import fr.dyade.aaa.jndi2.msg.JndiError;
import fr.dyade.aaa.jndi2.msg.JndiReply;
import fr.dyade.aaa.jndi2.msg.JndiRequest;
import fr.dyade.aaa.jndi2.server.JndiReplyNot;
import fr.dyade.aaa.jndi2.server.JndiScriptReplyNot;
import fr.dyade.aaa.jndi2.server.JndiScriptRequestNot;
import fr.dyade.aaa.jndi2.server.RequestContext;
import fr.dyade.aaa.jndi2.server.RequestManager;
import fr.dyade.aaa.jndi2.server.Trace;

public class ReplicationManager extends RequestManager implements UpdateListener {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public final static String INIT_REQUEST_TABLE = "initRequestTable";
  public final static String SYNC_REQUEST_TABLE = "syncRequestTable";
  public final static String WRITE_REQUEST_TABLE = "writeRequestTable";
  public final static String SERVER_LIST = "serverList";

  /**
   * Identifier of the server that owns the root naming context.
   */
  private AgentId rootOwnerId;

  /**
   * List of the initially known servers.
   */
  private short[] serverIds;

  /**
   * List of the JNDI servers discovered by this server.
   * These servers first receive an initialization notification that contain the naming
   * data owned by this server. Then they receive the update notifications about the contexts
   * owned by this server.
   */
  private transient Vector servers;

  /**
   * Table that contains the write requests forwarded to the owner.
   * key = owner identifier (AgentId)
   * value = requests list (RequestContextList)
   */
  private transient Hashtable writeRequestContextLists;

  /**
   * Table that contains the requests (read or write) waiting for the initialization of the context.
   * key = id of the missing context
   * value = requests list (RequestContextList)
   */
  private transient Hashtable initRequestContextLists;

  /**
   * Table that contains the requests (read or write) waiting for the synchronization of the context.
   * key = owner identifier (AgentId)
   * value = requests list (RequestContextList)
   */
  private transient Hashtable syncRequestContextLists;

  private boolean looseCoupling;


  public ReplicationManager(short[] serverIds) {
    this.serverIds = serverIds;
  }

  /**
   * Overrides the <code>JndiServer</code> behavior.
   */
  protected AgentId getRootOwnerId() {
    return rootOwnerId;
  }

  public void agentInitialize(boolean firstTime) throws Exception {
    if (firstTime) {
      looseCoupling = AgentServer.getBoolean(fr.dyade.aaa.jndi2.impl.ServerImpl.LOOSE_COUPLING);
      if (serverIds.length > 0 && (!looseCoupling)) {
        rootOwnerId = DistributedJndiServer.getDefault(serverIds[0]);
      } else {
        rootOwnerId = getId();
      }
    }

    super.agentInitialize(firstTime);

    writeRequestContextLists = (Hashtable) AgentServer.getTransaction().load(WRITE_REQUEST_TABLE);
    if (writeRequestContextLists == null) {
      writeRequestContextLists = new Hashtable();
    }

    initRequestContextLists = (Hashtable) AgentServer.getTransaction().load(INIT_REQUEST_TABLE);
    if (initRequestContextLists == null) {
      initRequestContextLists = new Hashtable();
    }

    syncRequestContextLists = (Hashtable) AgentServer.getTransaction().load(SYNC_REQUEST_TABLE);
    if (syncRequestContextLists == null) {
      syncRequestContextLists = new Hashtable();
    }

    servers = (Vector)AgentServer.getTransaction().load(SERVER_LIST);
    if (servers == null) {
      servers = new Vector();      
      for (int i = 0; i < serverIds.length; i++) {        
        AgentId aid = DistributedJndiServer.getDefault(serverIds[i]);
        servers.addElement(aid);
        sendTo(aid, new InitJndiServerNot(null, null, true));
      }
      saveServers();
    }

    getServerImpl().setUpdateListener(this);
  }

  /**
   * Reacts to an update notification from an other JNDI server.
   */
  void doReact(AgentId from, JndiUpdateNot not) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ReplicationManager[" + getId() + "].doReact(" +
                       from + ',' + not + ')');
    UpdateEvent updateEvent = not.getUpdateEvent();
    try {
      if (updateEvent instanceof BindEvent) {
        onUpdateEvent(from, (BindEvent)updateEvent);
      } else if (updateEvent instanceof RebindEvent) {
        onUpdateEvent(from, (RebindEvent)updateEvent);
      } else if (updateEvent instanceof UnbindEvent) {
        onUpdateEvent(from, (UnbindEvent)updateEvent);
      } else if (updateEvent instanceof CreateSubcontextEvent) {
        onUpdateEvent(from, (CreateSubcontextEvent)updateEvent);
      } else if (updateEvent instanceof DestroySubcontextEvent) {
        onUpdateEvent(from, (DestroySubcontextEvent)updateEvent);
      } else if (updateEvent instanceof ChangeOwnerEvent) {
        onUpdateEvent(from, (ChangeOwnerEvent)updateEvent);
      }
    } catch (NotOwnerException exc) {       
      // This may happen after a change owner event
      Trace.logger.log(BasicLevel.WARN,
                       "Distributed jndi update warn:",
                       exc);
    } catch (NamingException exc) { 
      if(!looseCoupling){
        Trace.logger.log(BasicLevel.ERROR, 
                         "Distributed jndi update error:",
                         exc);
        throw new Error(exc.toString());
      }
    }
  }  

  private void onUpdateEvent(AgentId from, BindEvent evt) 
  throws NamingException {
    if(!looseCoupling){
      getServerImpl().bind(
                           getServerImpl().getNamingContext(
                                                            evt.getUpdatedContextId()),
                                                            evt.getName(), 
                                                            evt.getObject(),
                                                            from);
    }else{
      NamingContext  nc= getServerImpl().getNamingContext(evt.getPath());
      getServerImpl().bind(
                           nc,
                           evt.getName(), 
                           evt.getObject(),
                           from);
    }

  }

  private void onUpdateEvent(AgentId from, RebindEvent evt) 
  throws NamingException {
    if(!looseCoupling){
      getServerImpl().rebind(
                             getServerImpl().getNamingContext(
                                                              evt.getUpdatedContextId()),
                                                              evt.getName(), 
                                                              evt.getObject(),
                                                              from);
    }else{
      NamingContext  nc= getServerImpl().getNamingContext(evt.getPath());
      getServerImpl().rebind(
                             nc,
                             evt.getName(), 
                             evt.getObject(),
                             from);
    }
  }

  private void onUpdateEvent(AgentId from, UnbindEvent evt) 
  throws NamingException {
    if(!looseCoupling){
      getServerImpl().unbind(
                             getServerImpl().getNamingContext(
                                                              evt.getUpdatedContextId()),
                                                              evt.getName(),
                                                              from);
    }else{
      NamingContext  nc= getServerImpl().getNamingContext(evt.getPath());
      getServerImpl().unbind(
                             nc,
                             evt.getName(),
                             from);
    }
  }

  private void onUpdateEvent(AgentId from, CreateSubcontextEvent evt) 
  throws NamingException {
    if(!looseCoupling){
      getServerImpl().createSubcontext(
                                       getServerImpl().getNamingContext(	
                                                                        evt.getUpdatedContextId()),
                                                                        evt.getName(),
                                                                        evt.getPath(),
                                                                        evt.getContextId(),
                                                                        evt.getOwnerId(),
                                                                        from);
    }else{
      CompositeName parentPath = (CompositeName) evt.getPath().clone();    
      parentPath.remove(parentPath.size() - 1);
      NamingContext  nc= getServerImpl().getNamingContext(parentPath);
      getServerImpl().createSubcontext(nc,
                                       evt.getName(),
                                       evt.getPath(),
                                       evt.getContextId(),
                                       getId(),
                                       from);
    }
  }

  private void onUpdateEvent(AgentId from, DestroySubcontextEvent evt) 
  throws NamingException {
    if(!looseCoupling){
      getServerImpl().destroySubcontext(
                                        getServerImpl().getNamingContext(
                                                                         evt.getUpdatedContextId()),
                                                                         evt.getName(), 
                                                                         evt.getPath(),
                                                                         from);
    }else{
      CompositeName parentPath = (CompositeName) evt.getPath().clone();    
      parentPath.remove(parentPath.size() - 1);
      NamingContext  nc= getServerImpl().getNamingContext(parentPath);
      getServerImpl().destroySubcontext(
                                        nc,
                                        evt.getName(), 
                                        evt.getPath(),
                                        from);

    }
  }

  private void onUpdateEvent(AgentId from, ChangeOwnerEvent evt) 
  throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ReplicationManager.onUpdateEvent(" +
                       from + ',' + evt + ')');

    NamingContextInfo[] contexts = evt.getNamingContexts();
    for (int i = 0; i < contexts.length; i++) {
      NamingContext nc = getServerImpl().getNamingContext(
                                                          contexts[i].getNamingContext().getId());

      if (nc == null) {
        // The InitJndiServerNot sent by 
        // the server that created this context may not
        // have been received.
        getServerImpl().addNamingContext(contexts[i]);
        // TODO : NTA uncomment and implement retryRequestsWaitingForMissingContext
        //         retryRequestsWaitingForMissingContext(
        //           contexts[i].getNamingContext().getId());
      } else {
        nc.setOwnerId(contexts[i].getNamingContext().getOwnerId());
        getServerImpl().resetNamingContext(
                                           contexts[i].getNamingContext());
        // DF: must retry the sync and write
        // requests to the new owner.
      }
    }
  }  

  /**
   * Overrides the <code>JndiServer</code> behavior.
   * Send a JNDI request to the owner (JNDI server).
   * Waits for the asynchronous reply.
   */
  protected JndiReply invokeOwner(AgentId owner,
                                  RequestContext reqCtx) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ReplicationManager.invokeOwner(" +
                       owner + ',' + reqCtx + ')');
    JndiRequest request = reqCtx.getRequest();    
    if (request instanceof CreateSubcontextRequest) {
      CreateSubcontextRequest csr = 
        (CreateSubcontextRequest)request;
      request = new CreateRemoteSubcontextRequest(
                                                  csr.getName(), 
                                                  getId());
    }

    sendTo(owner,
           new JndiScriptRequestNot(
                                    new JndiRequest[]{request}, true));
    RequestContextList list = 
      (RequestContextList)writeRequestContextLists.get(owner);
    if (list == null) {
      list = new RequestContextList();
      writeRequestContextLists.put(owner, list);
    }
    list.put(reqCtx);
    saveWriteRequestTable();
    return null;
  }

  void doReact(AgentId from, JndiScriptReplyNot not) 
  throws Exception {
    onReply(from, not.getReplies()[0]);
  }

  void doReact(AgentId from, JndiReplyNot not) 
  throws Exception {
    onReply(from, not.getReply());
  }

  /**
   * Reacts to the reply of a JNDI server that has been called
   * as it is the owner of a naming context.
   */
  private void onReply(AgentId from, JndiReply reply) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ReplicationManager[" + 
                       getId() + "].onReply(" +
                       from + ',' + reply + ')');
    RequestContextList ctxList = 
      (RequestContextList)writeRequestContextLists.get(from);
    RequestContext ctx = ctxList.get();
    ctxList.pop();
    if (ctxList.getSize() == 0) {
      writeRequestContextLists.remove(from);
    }    
    if (ctx != null) {
      ctx.reply(reply);
      saveWriteRequestTable();
    } else {
      Trace.logger.log(BasicLevel.ERROR,
                       "Reply context not found: " + 
                       from + ", " + reply);
    }
  }

  void doReact(AgentId from, 
               InitJndiServerNot not) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ReplicationManager.doReact(" +
                       from + ',' + not + ')');

    if (servers == null)
      return;

    AgentId[] jndiServerIds = not.getJndiServerIds();
    Vector initServers = new Vector();
    if (jndiServerIds != null) {
      for (int i = 0; i < jndiServerIds.length; i++) {
        if (servers.indexOf(jndiServerIds[i]) < 0) {
          initServers.addElement(jndiServerIds[i]);
        }
      }
    }

    // Send back an init notif if:
    // - the init notif is a request
    // - or the server 'from' is unknown
    if (not.isRequest() || servers.indexOf(from) < 0) {
      initServers.addElement(from);
    }

    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       " -> initServers = " + initServers);

    if (initServers.size() > 0) {
      AgentId[] localJndiServerIds = new AgentId[servers.size()];
      servers.copyInto(localJndiServerIds);
      NamingContextInfo[] localContexts = 
        getServerImpl().copyNamingContexts(getId());
      int serversInitialLength = servers.size();
      for (int i = 0; i < initServers.size(); i++) {
        AgentId newServerId = 
          (AgentId)initServers.elementAt(i);
        /** Modif */
        if (!(rootOwnerId.equals(initServers.elementAt(i))))
          sendTo(newServerId, new InitJndiServerNot(
                                                    localJndiServerIds, 
                                                    localContexts,
                                                    (!from.equals(newServerId))));
        if (servers.indexOf(newServerId) < 0) {
          servers.addElement(newServerId);
        }
        // else the server has already been registered.
        // (it is a recovering server)
      }
      if (servers.size() > serversInitialLength) {
        saveServers();
      }
    }

    NamingContextInfo[] contexts = not.getContexts();

    Hashtable  record_compositeName= new Hashtable();
    Hashtable  composite_context= new Hashtable();
    if (contexts != null) {
      Vector newNames = new Vector();
      for (int i = 0; i < contexts.length; i++) {
        if(!looseCoupling) {
          NamingContext nc = getServerImpl().getNamingContext(
                                                              contexts[i].getNamingContext().getId());
          if (nc == null) {
            getServerImpl().addNamingContext(contexts[i]);
            newNames.addElement(contexts[i].getCompositeName());
          }
          // Else the naming context has already been
          // added by an other server that is the (new)
          // owner of this context.

        } else {
          NamingContext nc;
          try{
            nc = getServerImpl().getNamingContext(contexts[i].getCompositeName());
          }catch(MissingRecordException mre){
            nc=null;
          }catch( MissingContextException mce){
            nc=null;
          }
          if(nc == null){
            nc = getServerImpl().newNamingContext( getId(),null,contexts[i].getCompositeName());
            contexts[i].getNamingContext().setOwnerId(getId());
          }

          Enumeration enumRecord = contexts[i].getNamingContext().getEnumRecord();
          while (enumRecord.hasMoreElements()) {          
            Record record =(Record)  enumRecord.nextElement();
            Record r = nc.getRecord(record.getName());
            if (r == null)  
              nc.addRecord(record);
            if(record instanceof ContextRecord){
              CompositeName parentPath = contexts[i].getCompositeName();
              record_compositeName.put(record,parentPath);
            }
          }
          composite_context.put(contexts[i].getCompositeName(),nc);
        }
      }
      if(looseCoupling){
        Enumeration enumKeyRecord = record_compositeName.keys();
        while(enumKeyRecord.hasMoreElements()) { 
          Record recor =(Record)enumKeyRecord.nextElement();
          CompositeName cn =(CompositeName)((CompositeName)(record_compositeName.get(recor))).clone();
          cn.add(recor.getName());
          if(composite_context.containsKey(cn)){
            NamingContext nc  =(NamingContext)composite_context.get(cn);
            ((ContextRecord)recor).setId(nc.getId());
          }	      
        }
        Enumeration enumContext = composite_context.elements();
        while(enumContext.hasMoreElements()) { 
          NamingContext nc  =(NamingContext)enumContext.nextElement();
          getServerImpl().storeNamingContext(nc);
        }
      }



      Vector retryNames = new Vector();
      Vector retryLists = new Vector();
      Enumeration names = initRequestContextLists.keys();
      Enumeration lists = initRequestContextLists.elements();
      while (lists.hasMoreElements()) {
        CompositeName name = 
          (CompositeName)names.nextElement();
        RequestContextList ctxList = 
          (RequestContextList)lists.nextElement();
        boolean retry = false;
        for (int i = 0; i < newNames.size(); i++) {
          CompositeName newName = 
            (CompositeName)newNames.elementAt(i);
          if (name.startsWith(newName)) {
            retry = true;
            break;
          }
        }
        if (retry) {
          retryNames.addElement(name);
          retryLists.addElement(ctxList);
        }
      }

      for (int i = 0; i < retryNames.size(); i++) {
        CompositeName name = 
          (CompositeName)retryNames.elementAt(i);
        RequestContextList ctxList = 
          (RequestContextList)retryLists.elementAt(i);
        initRequestContextLists.remove(name);
        while (ctxList.getSize() > 0) {
          RequestContext reqCtx = ctxList.get();
          JndiReply reply = invoke(reqCtx);
          if (reply != null) {
            reqCtx.reply(reply);
          }
          ctxList.pop();
        }
      }
      saveInitRequestTable();
    }
  }

  protected JndiReply onMissingContext(MissingContextException mce,
                                       RequestContext reqCtx) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ReplicationManager.onMissingContext(" +
                       mce + ',' + reqCtx + ')');
    RequestContextList ctxList = 
      (RequestContextList)initRequestContextLists.get(
                                                      mce.getName());
    if (ctxList == null) {
      ctxList = new RequestContextList();
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, 
                         " -> add a waiting request context: " + 
                         mce.getName());
      initRequestContextLists.put(
                                  mce.getName(), ctxList);
    }
    ctxList.put(reqCtx);
    saveInitRequestTable();
    return null;
  }

  protected JndiReply onMissingRecord(MissingRecordException mre,
                                      RequestContext reqCtx) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ReplicationManager.onMissingRecord(" +
                       mre + ',' + reqCtx + ')');
    CompositeName resolvedName = 
      (CompositeName)mre.getNameNotFoundException().getResolvedName();
    if (mre.getOwnerId().equals(getId()) ||
        resolvedName.equals(reqCtx.getResolvedName())) {
      // The resolved context has already been updated.
      return new JndiError(mre.getNameNotFoundException());
    }

    reqCtx.setResolvedName(resolvedName);
    synchronizeRequest((AgentId)mre.getOwnerId(), reqCtx);
    return null;
  }

  private void synchronizeRequest(AgentId owner, 
                                  RequestContext reqCtx) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(
                       BasicLevel.DEBUG, 
                       "ReplicationManager.synchronizeRequest(" +
                       owner + ',' + reqCtx + ')');
    sendTo(owner, new SyncRequestNot());
    RequestContextList list = 
      (RequestContextList)syncRequestContextLists.get(owner);
    if (list == null) {
      list = new RequestContextList();
      syncRequestContextLists.put(owner, list);      
    }
    list.put(reqCtx);
    saveSyncRequestTable();
  }

  void doReact(AgentId from, SyncRequestNot not) {
    sendTo(from, new SyncReplyNot());
  }

  void doReact(AgentId from, SyncReplyNot not) {
    RequestContextList ctxList = (RequestContextList)syncRequestContextLists.get(from);
    RequestContext ctx = ctxList.get();
    ctxList.pop();
    if (ctxList.getSize() == 0) {
      syncRequestContextLists.remove(from);
    }    
    if (ctx != null) {
      JndiReply reply = invoke(ctx);
      if (reply != null) {
        ctx.reply(reply);
        saveSyncRequestTable();
      } 
    }
  }

  public void onUpdate(UpdateEvent event) {
    for (int i = 0; i < servers.size(); i++) {
      AgentId aid = (AgentId)servers.elementAt(i);
      sendTo(aid, new JndiUpdateNot(event));
    }
  }

  protected void createSubcontext(CreateSubcontextRequest request) 
  throws NamingException {
    if (request instanceof CreateRemoteSubcontextRequest) {
      createRemoteSubcontext((CreateRemoteSubcontextRequest)request);
    } else {
      super.createSubcontext(request);
    }
  }

  private void createRemoteSubcontext(CreateRemoteSubcontextRequest request)
  throws NamingException {
    getServerImpl().createSubcontext(
                                     request.getName(), request.getOwnerId());
  }

  protected void changeOwner(ChangeOwnerRequest request)
  throws NamingException {
    super.changeOwner(request);

    writeRequestContextLists.remove(request.getOwnerId());
    syncRequestContextLists.remove(request.getOwnerId());
    // DF: must reply to those requests because
    // this server is the new owner.
  }

  private void saveInitRequestTable() {
    try {
      AgentServer.getTransaction().save(
                                        initRequestContextLists, INIT_REQUEST_TABLE);
    } catch (IOException exc) {
      throw new Error(exc.toString());
    }
  }

  private void saveWriteRequestTable() {
    try {
      AgentServer.getTransaction().save(
                                        writeRequestContextLists, WRITE_REQUEST_TABLE);
    } catch (IOException exc) {
      throw new Error(exc.toString());
    }
  }

  private void saveSyncRequestTable() {
    try {
      AgentServer.getTransaction().save(
                                        syncRequestContextLists, SYNC_REQUEST_TABLE);
    } catch (IOException exc) {
      throw new Error(exc.toString());
    }
  }

  private void saveServers() {
    try {
      AgentServer.getTransaction().save(servers, SERVER_LIST);
    } catch (IOException exc) {
      throw new Error(exc.toString());
    }
  }

  static class RequestContextList implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Vector list;

    RequestContextList() {
      this.list = new Vector();
    }

    void put(RequestContext ctx) {
      list.addElement(ctx);
    }

    RequestContext get() {
      if( list.size() > 0) return (RequestContext)list.elementAt(0);
      return null;
    }

    void pop() {
      if( list.size() > 0) {
        list.removeElementAt(0);
      }
    }

    int getSize() {
      return list.size();
    }

    public String toString() {
      return '(' + super.toString() + 
      ",list=" + list + ')';
    }
  }
}
