/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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

import java.io.*;
import java.util.*;
import javax.naming.*;

import fr.dyade.aaa.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class ContextManager 
    implements java.io.Serializable {

  private ContextTable contextIdTable;

  private ContextTable contextNameTable;

  private NamingContextId rootContextId;

  private StorageManager storageManager;

  public ContextManager(Transaction transaction,
                        Object serverId,
                        Object rootOwnerId) {    
    if (transaction instanceof NullTransaction) {
      contextNameTable = new SimpleContextTable();
      contextIdTable = new SimpleContextTable();
    } else {
      contextNameTable = new ContextCache();
      contextIdTable = new ContextCache();
    }   
    rootContextId = new NamingContextId(
      rootOwnerId, 0);
    storageManager = new StorageManager(
      transaction, serverId);
  }

  public void initialize() throws Exception {
    storageManager.initialize();
  }

  private void put(NamingContext nc) {
    contextIdTable.put(nc.getId(), nc);    
  }

  private void put(CompositeName name, NamingContext nc) {
    contextNameTable.put(name, nc);
  }

  public NamingContext getNamingContext(NamingContextId ncid) 
    throws NamingException {
    return getNamingContext(ncid, true);
  }

  public NamingContext getNamingContext(
    NamingContextId ncid,
    boolean cache) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ContextManager.getNamingContext(" + 
                       ncid + ',' + cache + ')');
    NamingContext nc = contextIdTable.get(ncid);
    if (nc != null) return nc ;

    nc = storageManager.loadNamingContext(ncid);
    if (cache && nc != null) put(nc);
    return nc;
  }

  private NamingContext getNamingContextFromName(CompositeName name) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(
        BasicLevel.DEBUG, 
        "ContextManager.getNamingContextFromName(" + 
        name + ')');

    // 1- Try to get the context directly from the cache
    NamingContext nc = contextNameTable.get(name);
    if (nc != null) return nc;

    // 2- Try to get the context id from the index of
    // the storage manager.
    NamingContextId ncid = 
      storageManager.getIdFromName(name);
    if (ncid != null) {
      // 3- Get the naming context
      nc = getNamingContext(ncid);
      if (nc == null) throw new Error(
        "Missing context: name=" + name + 
        ", id=" + ncid);
      put(name, nc);
      return nc;
    } else {
      return null;
    }
  }
  
  public NamingContext getNamingContext(CompositeName name) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(
        BasicLevel.DEBUG, 
        "ContextManager.getNamingContext(" + 
        name + ')');

    NamingContext nc = getNamingContextFromName(name);
    if (nc != null) return nc;

    // Go upward the naming path in order to
    // to find out which name is missing.
    CompositeName parentName = name;
    NamingContext parentNc = null;
    int unresolvedIndex = 0;
    for (int i = 0; i < name.size(); i++) {
      parentName = (CompositeName)parentName.clone();
      parentName.remove(parentName.size() - 1);
      parentNc = getNamingContextFromName(parentName);
      if (parentNc != null) {
        unresolvedIndex = name.size() - 1 - i;
        break;
      }
    }

    if (parentNc == null) {
      // Shows that the root context is missing
      throw new MissingContextException(
        rootContextId, name);
    }

    // Find out why the naming context has not been
    // found.
    String unresolvedName = name.get(unresolvedIndex);
    Record record = parentNc.getRecord(unresolvedName);
    if (record == null) {
      NameNotFoundException nnfe = 
        new NameNotFoundException();
      CompositeName resolvedName = new CompositeName();
      for (int j = 0; j < unresolvedIndex; j++) {
        resolvedName.add(name.get(j));
      }
      nnfe.setResolvedName(resolvedName);
      throw new MissingRecordException(
        parentNc.getId(), 
        parentNc.getOwnerId(),
        nnfe);
    } else if (record instanceof ContextRecord) {
      ContextRecord ctxRecord = (ContextRecord)record;
      // The naming context is missing.
      // (we would have found it during the upward search)      
      throw new MissingContextException(
        ctxRecord.getId(), name);
    } else {
      throw new NotContextException();
    }
  }

  public void delete(NamingContextId ncid,
                     CompositeName name) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ContextManager.delete(" + 
                       ncid + ',' + name + ')');
    contextIdTable.remove(ncid);
    contextNameTable.remove(name);
    storageManager.delete(ncid, name);
  }

  public NamingContextInfo[] copyNamingContexts(Object serverId) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ContextManager.getNamingContexts(" + 
                       serverId + ')');    
    Vector contexts = new Vector();
    Enumeration nameEnum = storageManager.getContextNames();
    Enumeration idEnum = storageManager.getContextIds();
    while (idEnum.hasMoreElements()) {
      NamingContextId ncid = 
        (NamingContextId)idEnum.nextElement();
      CompositeName name = 
        (CompositeName)nameEnum.nextElement();
      NamingContext nc = getNamingContext(ncid, false);      
      if (nc.getOwnerId().equals(serverId)) {
        NamingContext ncCopy = (NamingContext)nc.clone();
        contexts.addElement(
          new NamingContextInfo(ncCopy, name));
      }
    }
    
    NamingContextInfo[] res = 
      new NamingContextInfo[contexts.size()];
    contexts.copyInto(res);
    return res;
  }

  public NamingContext newNamingContext(Object ownerId,
                                        NamingContextId ncid,
                                        CompositeName name) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ContextManager.newNamingContext(" + 
                       ownerId + ',' + ncid + ',' + name + ')');
    NamingContext nc = 
      storageManager.newNamingContext(
        ownerId, ncid, name);
    put(nc);
    put(name, nc);
    return nc;
  }

  public void addNamingContext(NamingContextInfo ncInfo)
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ContextManager.addNamingContext(" + 
                       ncInfo + ')');
    NamingContext nc = ncInfo.getNamingContext();
    CompositeName name = ncInfo.getCompositeName();
    storageManager.addNamingContext(
      nc, name);
    put(nc);
    put(name, nc);
  }
  
  public NamingContext getRootNamingContext() 
    throws NamingException {
    return getNamingContext(rootContextId);
  }

  public void storeNamingContext(NamingContext nc) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ContextManager.storeNamingContext(" + 
                       nc + ')');
    storageManager.storeNamingContext(nc);
  }

  public NamingContextInfo[] changeOwner(
    Object formerOwnerId,
    Object newOwnerId)
    throws NamingException {
    Vector updatedContexts = new Vector();
    Enumeration idEnum = storageManager.getContextIds();
    Enumeration nameEnum = storageManager.getContextNames();
    while (idEnum.hasMoreElements()) {
      NamingContextId ncid = 
        (NamingContextId)idEnum.nextElement();
      CompositeName name = 
        (CompositeName)nameEnum.nextElement();
      NamingContext nc = getNamingContext(ncid, false);      
      if (nc.getOwnerId().equals(formerOwnerId)) {
        nc.setOwnerId(newOwnerId);
        storageManager.storeNamingContext(nc);
        updatedContexts.addElement(
          new NamingContextInfo(nc, name));
      }
    }
    NamingContextInfo[] res =
      new NamingContextInfo[updatedContexts.size()];
    updatedContexts.copyInto(res);
    return res;
  }

  public void resetNamingContext(NamingContext context)
    throws NamingException {
    storageManager.storeNamingContext(context);
  }

  public void writeBag(ObjectOutputStream out)
    throws IOException {
    out.writeObject(contextIdTable);
    out.writeObject(contextNameTable);
    storageManager.writeBag(out);
  }

  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
    contextIdTable = (ContextTable)in.readObject();
    contextNameTable = (ContextTable)in.readObject();
    storageManager.readBag(in);
  }
}
