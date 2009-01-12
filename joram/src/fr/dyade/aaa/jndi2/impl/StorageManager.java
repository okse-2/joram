/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.NamingException;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.Transaction;

public class StorageManager {

  public static final String ROOT = "jndiStorage";

  public static final String CTX_COUNTER = "jndiCtxCounter";

  public static final String CTX_INDEX = "jndiCtxIndex";

  private long contextCounter;

  private Transaction transaction;
  
  private Hashtable nameToIdIndex;

  private Object serverId;

  public StorageManager(Transaction transaction,
                        Object serverId) {
    this.transaction = transaction;
    this.serverId = serverId;
  }

  public void initialize() throws Exception {
    // Load the local context counter
    Long contextCounterL = (Long)transaction.load(
      CTX_COUNTER);
    if (contextCounterL == null) {
      contextCounter = 0;
    } else {
      contextCounter = contextCounterL.longValue();
    }

    // Load the context index
    nameToIdIndex = (Hashtable)transaction.load(CTX_INDEX);
    if (nameToIdIndex == null) {
      nameToIdIndex = new Hashtable();
    }
  }

  public NamingContext newNamingContext(Object ownerId,
                                        NamingContextId ncid,
                                        CompositeName name) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "StorageManager.newNamingContext(" + 
                       ownerId + ',' + name + ')');
    if (ncid == null) {
      ncid = newNamingContextId();
    }
    NamingContext nc = new NamingContext(
      ncid, ownerId, name);
    addNamingContext(
      nc, 
      name);
    return nc;
  }

  public void addNamingContext(NamingContext nc,
                               CompositeName name)
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "StorageManager.addNamingContext(" +  nc + ',' +  name + ')');
    nameToIdIndex.put(name, nc.getId());
    storeIndex();
    storeNamingContext(nc);    
  }

  private NamingContextId newNamingContextId() throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "StorageManager.newNamingContextId()");
    NamingContextId ncid = new NamingContextId(
      serverId, contextCounter);
    contextCounter++;
    try {
      transaction.save(new Long(contextCounter), CTX_COUNTER);
      return ncid;
    } catch (IOException ioexc) {
      NamingException nexc = new NamingException();
      nexc.setRootCause(ioexc);
      throw nexc;
    }
  }

  public void storeNamingContext(NamingContext nc) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "StorageManager.storeNamingContext(" + nc + ')');
    try {
      transaction.save(nc, ROOT, nc.getId().toString());
    } catch (IOException exc) {
      NamingException ne = new NamingException(exc.getMessage());
      ne.setRootCause(exc);
      throw ne;
    }
  }

  public NamingContext loadNamingContext(NamingContextId ncid) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "StorageManager.loadNamingContext(" + ncid + ')');
    return loadNamingContext(ncid.toString());
  }
  
  public NamingContext loadNamingContext(String fileName) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "StorageManager.loadNamingContext(" + fileName + ')');
    try {
      Object obj = transaction.load(
        ROOT, fileName);
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(
          BasicLevel.DEBUG, 
          " -> obj = " + obj);
      return (NamingContext)obj;
    } catch (IOException exc) {
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, "", exc);
      NamingException ne = new NamingException(exc.getMessage());
      ne.setRootCause(exc);
      throw ne;
    } catch (ClassNotFoundException exc2) {
      if (Trace.logger.isLoggable(BasicLevel.DEBUG))
        Trace.logger.log(BasicLevel.DEBUG, "", exc2);
      NamingException ne = new NamingException(exc2.getMessage());
      ne.setRootCause(exc2);
      throw ne;
    }    
  }

  public void delete(NamingContextId ncid,
                     CompositeName name) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "StorageManager.delete(" + ncid + ',' + name + ')');
    transaction.delete(ROOT, ncid.toString());
    nameToIdIndex.remove(name);
    storeIndex();
  }

  private void storeIndex() throws NamingException {
    try {
      transaction.save(nameToIdIndex, CTX_INDEX);
    } catch (IOException exc) {
      NamingException ne = new NamingException(
        exc.getMessage());
      ne.setRootCause(exc);
      throw ne;
    }
  }
    
  public Enumeration getContextIds() {
    return nameToIdIndex.elements();
  }

  public Enumeration getContextNames() {
    return nameToIdIndex.keys();
  }

  public NamingContextId getIdFromName(CompositeName name) {
    return (NamingContextId)nameToIdIndex.get(name);
  }

  public void writeBag(ObjectOutputStream out)
    throws IOException {
    out.writeLong(contextCounter);
    out.writeObject(nameToIdIndex);
  }

  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
    contextCounter = in.readLong();
    nameToIdIndex = (Hashtable)in.readObject();
  }
}
