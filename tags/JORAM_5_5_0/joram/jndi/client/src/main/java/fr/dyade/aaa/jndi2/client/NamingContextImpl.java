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
 * Contributor(s): David Feliot, Nicolas Tachker, Frederic Maistre
 */
package fr.dyade.aaa.jndi2.client;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Referenceable;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.jndi2.msg.BindRequest;
import fr.dyade.aaa.jndi2.msg.CreateSubcontextRequest;
import fr.dyade.aaa.jndi2.msg.DestroySubcontextRequest;
import fr.dyade.aaa.jndi2.msg.JndiError;
import fr.dyade.aaa.jndi2.msg.JndiReply;
import fr.dyade.aaa.jndi2.msg.ListBindingsReply;
import fr.dyade.aaa.jndi2.msg.ListBindingsRequest;
import fr.dyade.aaa.jndi2.msg.ListReply;
import fr.dyade.aaa.jndi2.msg.ListRequest;
import fr.dyade.aaa.jndi2.msg.LookupReply;
import fr.dyade.aaa.jndi2.msg.LookupRequest;
import fr.dyade.aaa.jndi2.msg.UnbindRequest;

public class NamingContextImpl implements Context {

  private NamingConnection connection;

  private CompositeName contextPath;

  public NamingContextImpl(NamingConnection connection, CompositeName contextPath) { 
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "NamingContextImpl.<init>(" + connection + ',' + contextPath + ')');
    this.connection = connection;
    this.contextPath = contextPath;
  }

  /** Empty constructor, called by the subclass. */
  protected NamingContextImpl()
  {
    contextPath = new CompositeName();
  }

  public void bind(Name name, Object obj) throws NamingException {
    bind(name.toString(), obj);
  }

  public void bind(String name, Object obj) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "NamingContextImpl.bind(" + 
                       name + ',' + obj + ')');
    if (obj instanceof Referenceable) {
      obj = ((Referenceable)obj).getReference();
    }
    JndiReply reply = connection.invoke(
      new BindRequest(merge(contextPath, name), obj));
    if (reply instanceof JndiError) {
      NamingException exc = ((JndiError)reply).getException();
      exc.fillInStackTrace();
      throw exc;
    }
  }

  public void rebind(Name name, Object obj) throws NamingException {
    rebind(name.toString(), obj);
  }

  public void rebind(String name, Object obj) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "NamingContextImpl.rebind(" + 
                       name + ',' + obj + ')');
    if (obj instanceof Referenceable) {
      obj = ((Referenceable)obj).getReference();
    }
    JndiReply reply = connection.invoke(
      new BindRequest(merge(contextPath, name), obj, true));
    if (reply instanceof JndiError) {
      NamingException exc = ((JndiError)reply).getException();
      exc.fillInStackTrace();
      throw exc;
    }
  }

  public Object lookup(Name name) throws NamingException {
    return lookup(name.toString());
  }

  public Object lookup(String name) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "NamingContextImpl.lookup(" + 
                       name + ')');
    CompositeName path = merge(contextPath, name);
    JndiReply reply = connection.invoke(new LookupRequest(path));
    if (reply instanceof JndiError) {
      NamingException exc = ((JndiError)reply).getException();
      exc.fillInStackTrace();
      throw exc;
    } else if (reply instanceof LookupReply) {
      Object obj = ((LookupReply)reply).getObject();
      return obj;
    } else {
      return new NamingContextImpl(
        connection.cloneConnection(), path);
    } 
  }

  public void unbind(Name name) throws NamingException {
    unbind(name.toString());
  }

  public void unbind(String name) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "NamingContextImpl.unbind(" + 
                       name + ')');
    JndiReply reply = connection.invoke(
      new UnbindRequest(merge(contextPath, name)));    
    if (reply instanceof JndiError) {
      NamingException exc = ((JndiError)reply).getException();
      exc.fillInStackTrace();
      throw exc;
    }
  }

  public void close() throws NamingException {}

  protected void finalize() throws Throwable {
    close();
  }

  public Hashtable getEnvironment() throws NamingException {
    return connection.getEnvironment();
  }

  public NamingEnumeration list(Name name) throws NamingException {
    return list(name.toString());
  }
  public NamingEnumeration list(String name) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "NamingContextImpl.list(" + 
                       name + ')');
    JndiReply reply = connection.invoke(
      new ListRequest(merge(contextPath, name)));    
    if (reply instanceof JndiError) {
      NamingException exc = ((JndiError)reply).getException();
      exc.fillInStackTrace();
      throw exc;
    }
    return ((ListReply)reply).getEnumeration();
  }

  public NamingEnumeration listBindings(Name name) throws NamingException {
    return listBindings(name.toString());
  }

  public NamingEnumeration listBindings(String name) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "NamingContextImpl.listBindings(" + name + ')');
    
    CompositeName queryPath = merge(contextPath, name);
    JndiReply reply = connection.invoke(new ListBindingsRequest(queryPath));
    if (reply instanceof JndiError) {
      NamingException exc = ((JndiError)reply).getException();
      exc.fillInStackTrace();
      throw exc;
    }
    
    ListBindingsReply lbr = (ListBindingsReply)reply;

    // 1- resolve contexts
    Binding[] bindings = lbr.getContexts();
    for (int i = 0; i < bindings.length; i++) {
      CompositeName subCtxPath = (CompositeName)queryPath.clone();
      subCtxPath.add(bindings[i].getName());
      bindings[i].setObject(new NamingContextImpl(
        connection.cloneConnection(), subCtxPath));
    }

    // 2- resolve references
    lbr.resolveReferences();
    return lbr.getEnumeration();
  }

  public Context createSubcontext(Name name) throws NamingException {
    return createSubcontext(name.toString());
  }

  public Context createSubcontext(String name) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "NamingContextImpl.createSubcontext(" + 
                       name + ')');
    CompositeName path = merge(contextPath, name);
    JndiReply reply = connection.invoke(
      new CreateSubcontextRequest(path));    
    if (reply instanceof JndiError) {
      NamingException exc = ((JndiError)reply).getException();
      exc.fillInStackTrace();
      throw exc;
    }
    return new NamingContextImpl(connection.cloneConnection(), path);
  }

  public void destroySubcontext(Name name) throws NamingException {
    destroySubcontext(name.toString());
  }

  public void destroySubcontext(String name) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "NamingContextImpl.destroySubcontext(" + 
                       name + ')');
    JndiReply reply = connection.invoke(
      new DestroySubcontextRequest(merge(contextPath, name)));    
    if (reply instanceof JndiError) {
      NamingException exc = ((JndiError)reply).getException();
      exc.fillInStackTrace();
      throw exc;
    }
  }

  public String getNameInNamespace() throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, 
                       "NamingContextImpl.getNameInNamespace()");
    return contextPath.toString();
  }

  /**
   * @param propName 
   * @param propVal  
   */
  public Object addToEnvironment(Name propName, Object propVal) throws NamingException {
    throw(new NamingException("Not yet available"));
  }
  
  public Object addToEnvironment(String propName, Object propVal) throws NamingException {
    throw(new NamingException("Not yet available"));
  }
  
  public Name composeName(Name name, Name prefix) throws NamingException {
    throw(new NamingException("Not yet available"));
  }
  
  public String composeName(String name, String prefix) throws NamingException {
    throw(new NamingException("Not yet available"));
  }
  
  public NameParser getNameParser(Name name) throws NamingException {
    throw(new NamingException("Not yet available"));
  }
  
  public NameParser getNameParser(String name) throws NamingException {
    throw(new NamingException("Not yet available"));
  }
  
  public Object lookupLink(Name name) throws NamingException {
    throw(new NamingException("Not yet available"));
  }
  
  public Object lookupLink(String name) throws NamingException {
    throw(new NamingException("Not yet available"));
  }
  
  public Object removeFromEnvironment(String propName) throws NamingException {
    throw(new NamingException("Not yet available"));
  }
  
  public void rename(Name oldName, Name newName) throws NamingException {
    throw(new NamingException("Not yet available"));
  }
  
  public void rename(String oldName, String newName) throws NamingException {
    throw(new NamingException("Not yet available"));
  }

  private static CompositeName merge(CompositeName path, String name) throws NamingException {    
    if (name == null) throw new InvalidNameException();
    CompositeName res = new CompositeName(name);
    trim(res);
    for (int i = path.size() - 1; i > -1 ; i--) {
      res.add(0, path.get(i));
    }
    return res;
  }

  private static CompositeName trim(CompositeName name) throws NamingException {
    int i = 0;
    while (i < name.size()) {
      String s = (String)name.remove(i);
      s = s.trim();
      if (s.length() > 0) {
        name.add(i, s);
        i++;
      }
    }
    return name;
  }

  public String toString() {
    return '(' + super.toString() + 
      ",connection=" + connection + 
      ",contextPath=" + contextPath + ')';
  }
}
