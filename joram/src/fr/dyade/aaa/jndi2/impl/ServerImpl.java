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

public class ServerImpl {

  public static final String ROOT = "jndiStorage";

  public static final String NC_FILE = "nc.jndi";
  
  private Hashtable contexts;

  private Transaction transaction;

  public ServerImpl(Transaction transaction) throws NamingException { 
    this.transaction = transaction;
    this.contexts = new Hashtable();
  }
  
  public void initialize() throws NamingException {
    // Create the root naming context.
    CompositeName rootName = new CompositeName();
    if (loadNamingContext(rootName) == null) {
      createNamingContext(rootName);
    }
  }

  public void bind(CompositeName path, Object obj) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.bind(" + path + ',' + obj + ')');

    // The root context is (in a way) already bound since
    // it is already a context.
    if (path.size() == 0) throw new NameAlreadyBoundException();
    
    String lastName = (String)path.remove(path.size() - 1);
    NamingContext nc = loadNamingContext(path);

    // The target context and/or intermediate contexts don't exist.
    // As a csq, the name is not found.
    if (nc == null) throw new NameNotFoundException();

    Record r = nc.getRecord(lastName);
    if (r != null) throw new NameAlreadyBoundException();
    else {
      nc.addRecord(new ObjectRecord(lastName, obj));
      storeNamingContext(path, nc);
    }
  }

  private void createNamingContext(CompositeName name) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.createNamingContext(" + name + ')');
    NamingContext nc = new NamingContext();
    // 1- Put it in the cache.
    contexts.put(name, nc);
    // 2- Store it on the disk.
    storeNamingContext(name, nc);
  }

  private NamingContext loadNamingContext(CompositeName name) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.loadNamingContext(" + name + ')');

    // 1- From the cache    
    NamingContext nc = (NamingContext)contexts.get(name);
    if (nc == null) {
      try {
        // 2- From the disk
        if (Trace.logger.isLoggable(BasicLevel.DEBUG))
          Trace.logger.log(BasicLevel.DEBUG, " -> Load from disk");
        String filePath = ROOT;
        if (name.size() > 0) {
          filePath = filePath + File.separator + name.toString();
        }
        nc = (NamingContext)transaction.load(filePath, NC_FILE);
        if (nc != null) contexts.put(name, nc);
      } catch (IOException exc) {
        NamingException ne = new NamingException(exc.getMessage());
        ne.setRootCause(exc);
        throw ne;
      } catch (ClassNotFoundException exc2) {
        NamingException ne = new NamingException(exc2.getMessage());
        ne.setRootCause(exc2);
        throw ne;
      }
    }
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, " -> nc = " + nc);
    return nc;
  }

  public void rebind(CompositeName path, Object obj) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.rebind(" + path + ',' + obj + ')');

    // The root context cannot become a name-value pair.
    if (path.size() == 0) throw new NamingException("Cannot rebind the root context");

    String lastName = (String)path.remove(path.size() - 1);
    NamingContext nc = loadNamingContext(path);

    // The target context and/or intermediate contexts don't exist.
    // As a csq, the name is not found.
    if (nc == null) throw new NameNotFoundException();

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
    storeNamingContext(path, nc);
  }

  public Record lookup(CompositeName path) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.lookup(" + path + ')');    

    if (path.size() == 0) {
      return new ContextRecord(null);
    }

    String lastName = (String)path.remove(path.size() - 1);
    NamingContext nc = loadNamingContext(path);

    // The target context and/or intermediate contexts don't exist.
    // As a csq, the name is not found.
    if (nc == null) throw new NameNotFoundException();

    Record r = nc.getRecord(lastName);
    if (r != null) {
      return r;
    } else {
      throw new NameNotFoundException();
    }
  }

  private void storeNamingContext(CompositeName name, NamingContext nc) 
    throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ServerImpl.storeNamingContext(" + name + ',' + nc + ')');
    try {
      String filePath = ROOT;
      if (name.size() > 0) {
        filePath = filePath + File.separator + name.toString();
      }
      transaction.save(nc, filePath, NC_FILE);
    } catch (IOException exc) {
      NamingException ne = new NamingException(exc.getMessage());
      ne.setRootCause(exc);
      throw ne;
    }
  }

  public void unbind(CompositeName path) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.unbind(" + path + ')');

    // The root context cannot be deleted.
    if (path.size() == 0) throw new NamingException("Cannot unbind the root context");

    CompositeName parentPath = (CompositeName)path.clone();    
    String lastName = (String)parentPath.remove(parentPath.size() - 1);
    NamingContext parentNc = loadNamingContext(parentPath);

    // The target context and/or intermediate contexts don't exist.
    // As a csq, the name is not found.
    if (parentNc == null) throw new NameNotFoundException();

    Record r = parentNc.getRecord(lastName);
    if (r != null) { 
      if (r instanceof ContextRecord) {
        destroySubcontext(path);
      } else if (r instanceof ObjectRecord) {
        parentNc.removeRecord(lastName);
        storeNamingContext(parentPath, parentNc);
      }
    }
    // else do nothing (idempotency)
  }

  public NameClassPair[] list(CompositeName path) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.list(" + path + ')');

    NamingContext nc = loadNamingContext(path);

    if (nc == null) {
      if (path.size() > 0) {
        CompositeName parentPath = (CompositeName)path.clone();    
        String lastName = (String)parentPath.remove(parentPath.size() - 1);
        NamingContext parentNc = loadNamingContext(parentPath);        

        // The target context and/or intermediate contexts don't exist.
        // As a csq, the name is not found.
        if (parentNc == null) throw new NameNotFoundException();

        Record r = parentNc.getRecord(lastName);
        if (r != null) {
          if (r instanceof ObjectRecord) {
            throw new NotContextException();
          } else {
            throw new Error("Missing context");
          }
        } else {
          throw new NameNotFoundException();
        }
      } else {
        throw new Error("Missing root context");
      }
    } else {
      return nc.getNameClassPairs();
    }
  }

  public Binding[] listBindings(CompositeName path) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.list(" + path + ')');

    NamingContext nc = loadNamingContext(path);

    if (nc == null) {
      if (path.size() > 0) {
        CompositeName parentPath = (CompositeName)path.clone();    
        String lastName = (String)parentPath.remove(parentPath.size() - 1);
        NamingContext parentNc = loadNamingContext(parentPath);        

        // The target context and/or intermediate contexts don't exist.
        // As a csq, the name is not found.
        if (parentNc == null) throw new NameNotFoundException();

        Record r = parentNc.getRecord(lastName);
        if (r != null) {
          if (r instanceof ObjectRecord) {
            throw new NotContextException();
          } else {
            throw new Error("Missing context");
          }
        } else {
          throw new NameNotFoundException();
        }
      } else {
        throw new Error("Missing root context");
      }
    } else {
      return nc.getBindings();
    }
  }

  public void createSubcontext(CompositeName path) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "ServerImpl.createSubcontext(" + path + ')');

    // The root already exists.
    if (path.size() == 0) throw new NameAlreadyBoundException();

    CompositeName parentPath = (CompositeName)path.clone();    
    String lastName = (String)parentPath.remove(parentPath.size() - 1);
    NamingContext parentNc = loadNamingContext(parentPath);

    // The target context and/or intermediate contexts don't exist.
    // As a csq, the name is not found.
    if (parentNc == null) throw new NameNotFoundException();

    if (parentNc.getRecord(lastName) != null) 
      throw new NameAlreadyBoundException();
    parentNc.addRecord(new ContextRecord(lastName));
    storeNamingContext(parentPath, parentNc);
    createNamingContext(path);
  }

  public void destroySubcontext(CompositeName path) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "ServerImpl.destroySubcontext(" + path + ')');

    if (path.size() == 0) 
      throw new NamingException("Cannot delete root context.");

    CompositeName parentPath = (CompositeName)path.clone();
    String lastName = (String)parentPath.remove(parentPath.size() - 1);
    NamingContext parentNc = loadNamingContext(parentPath);

    // The target context and/or intermediate contexts don't exist.
    // As a csq, the name is not found.
    if (parentNc == null) throw new NameNotFoundException();

    Record r = parentNc.getRecord(lastName);
    if (r != null) {
      if (r instanceof ObjectRecord) throw new NotContextException();

      NamingContext nc = loadNamingContext(path);
      if (nc != null) {
        if (nc.size() > 0) {
          throw new ContextNotEmptyException();
        } else {
          // Remove from the cache
          contexts.remove(path);
          
          // Remove from the disk
          String filePath = ROOT;
          if (path.size() > 0) {
            filePath = filePath + File.separator + path.toString();
          }
          transaction.delete(filePath, NC_FILE);
          
          // Remove from the parent context
          parentNc.removeRecord(lastName);
          storeNamingContext(parentPath, parentNc);          
        }
      } else throw new Error("Missing naming context");      
    }
    // else do nothing (idempotency)
  }
}

  
