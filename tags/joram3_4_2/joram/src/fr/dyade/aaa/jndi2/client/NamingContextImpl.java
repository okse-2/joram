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
 * Contributor(s): David Feliot, Nicolas Tachker, Frederic Maistre
 */
package fr.dyade.aaa.jndi2.client;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.jndi2.msg.*;

public class NamingContextImpl implements Context {

  private NamingConnection connection;

  private CompositeName contextPath;

  public NamingContextImpl(NamingConnection connection,
                           CompositeName contextPath) throws NamingException { 
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
        new NamingConnection(connection.getHostName(),
                             connection.getPort()), path);
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
    } else {
      return ((ListReply)reply).getEnumeration();
    }
  }

  public NamingEnumeration listBindings(Name name) throws NamingException {
    return listBindings(name.toString());
  }

  public NamingEnumeration listBindings(String name) throws NamingException {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "NamingContextImpl.listBindings(" + 
                       name + ')');
    JndiReply reply = connection.invoke(
      new ListBindingsRequest(merge(contextPath, name)));    
    if (reply instanceof JndiError) {
      NamingException exc = ((JndiError)reply).getException();
      exc.fillInStackTrace();
      throw exc;
    } else {
      ListBindingsReply lbr = (ListBindingsReply)reply;

      // 1- resolve contexts
      Binding[] bindings = lbr.getContexts();
      for (int i = 0; i < bindings.length; i++) {
        CompositeName ctxName = (CompositeName)contextPath.clone();
        ctxName.add(bindings[i].getName());
        bindings[i].setObject(new NamingContextImpl(
          new NamingConnection(connection.getHostName(),
                               connection.getPort()), ctxName));
      }

      // 2- resolve references
      lbr.resolveReferences();
      return lbr.getEnumeration();
    }
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
    } else {
      return new NamingContextImpl(
        new NamingConnection(connection.getHostName(),
                             connection.getPort()), path);
    }
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
}
