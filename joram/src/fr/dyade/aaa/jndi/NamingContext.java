/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, fr.dyade.aaa.ns,
 * fr.dyade.aaa.jndi and fr.dyade.aaa.joram, released September 11, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package fr.dyade.aaa.jndi;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.naming.*;

/**
 *
 *  @author Nicolas Tachker
 *
 *	@see ProxyContext
 */
public class NamingContext implements Context {
  protected Socket sock = null;
  protected ObjectOutputStream out = null;
  protected ObjectInputStream in = null;
  protected int port;
  protected String host;

  public NamingContext () throws Exception {
	this("localhost",16400);
  }

  public NamingContext (String host, int port) throws Exception {
	sock = new Socket(host, port);
	if (sock != null) {
	  sock.setTcpNoDelay(true);
	  sock.setSoTimeout(0);
	  sock.setSoLinger(true,1000);
	  out = new ObjectOutputStream(sock.getOutputStream());
	  in = new ObjectInputStream(sock.getInputStream());
	}
  }

  private Object readWrite(String name, String cmd) {
	return readWrite(name,cmd,null);
  }
  private Object readWrite(String name, String cmd, Object obj) {
	try {
	  if (out != null) {
		out.writeObject(new MessageContext(name,cmd,obj));
		out.flush();
		out.reset();
	  }
	  if (in != null) {
		return in.readObject();
	  }
	  return null;
	}catch(Exception e) {
	  System.out.println("Exception in NamingContext ==> readWrite");
	  e.printStackTrace();
	  return null;
	}
  }
  private void write(String name, String cmd, Object obj) {
	try {
	  if (out != null) {
		out.writeObject(new MessageContext(name,cmd,obj));
		out.flush();
		out.reset();
	  }
	}catch(Exception e) {
	  System.out.println("Exception in NamingContext ==> write");
	  e.printStackTrace();
	}
  }

  public Object lookup(Name name) throws NamingException {
	return lookup(name.toString());
  }
  public Object lookup(String name) throws NamingException {
	if (name.length() == 0) {
	  return this;
	}
	Object obj = readWrite(name,"lookup");
	if(obj == null)
	  throw(new NameNotFoundException(name + " not found."));
	if (obj instanceof Reference) {
	  try {
		obj = javax.naming.spi.NamingManager.getObjectInstance(obj,null,null,null);
	  } catch (Exception e) {
		throw(new NamingException("NamingContext lookup : getObjectInstance()"));
	  }
	}
	return obj;
  }
  public NamingEnumeration list(Name name) throws NamingException {
	return list(name.toString());
  }
  public NamingEnumeration list(String name) throws NamingException {
	Object obj = readWrite(name,"list");
	if (obj instanceof Hashtable) {
	  NamingEnumeration namEnum = new ListName((Hashtable) obj);
	  return namEnum;
	} else if (obj instanceof Context) {
	  //TODO, we just have one Context
	}
	return null;
  }
  public void bind(Name name, Object obj) throws NamingException {
	bind(name.toString(),obj);
  }
  public void bind(String name, Object obj) throws NamingException {
	Object exception = null;
	if (obj instanceof Referenceable) {
	  Reference ref = null;
	  try {
		ref = ((Referenceable) obj).getReference();
	  } catch (Exception e) {
		throw(new NamingException("NamingContext bind : " + obj.getClass().getName() + ", naming exception was encountered while retrieving the reference"));
	  }
	  exception = readWrite(name,"bind", ref);
	  if (exception != null)
		throw(new NameAlreadyBoundException(name + exception.toString()));
	} else if(obj instanceof Serializable) {
	  exception = readWrite(name,"bind", obj);
	  if (exception != null)
		throw(new NameAlreadyBoundException(name + exception.toString()));
	} else {
	  throw(new NamingException("NamingContext bind : " + obj.getClass().getName() + " is not Referenceable or Serializable"));
	}
  }
  public void rebind(Name name, Object obj) throws NamingException {
	bind(name.toString(),obj);
  }
  public void rebind( String name , Object obj ) throws NamingException {
	if (obj instanceof Referenceable) {
	  Reference ref = null;
	  try {
		ref = ((Referenceable) obj).getReference();
	  } catch (Exception e) {
		throw(new NamingException("NamingContext bind : " + obj.getClass().getName() + ", naming exception was encountered while retrieving the reference"));
	  }
	  write(name,"rebind", ref);
	} else if(obj instanceof Serializable) {
	  write(name,"rebind", obj);
	} else {
	  throw(new NamingException("NamingContext bind : " + obj.getClass().getName() + " is not Referenceable or Serializable"));
	}
  }
  public void unbind(Name name) throws NamingException {
	unbind(name.toString());
  }
  public void unbind(String name) throws NamingException {
	write(name,"unbind",null);
  }
    
  /**
   * close ObjectOutputStream, ObjectInputStream and Socket
   */
  public void close() throws NamingException {
	try {
	  if (out != null) {
		out.close();
		out = null;
	  }
	  if (in != null) {
		in.close();
		in = null;
	  }
	  if (sock != null) {
		sock.close();
		sock = null;
	  }
	} catch (Exception e) {
	  e.printStackTrace();
	}
  }

  //============================ begin not yet available ===========================
  public Object addToEnvironment(Name propName, Object propVal) throws NamingException {
	return addToEnvironment(propName.toString(),propVal);
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
  public Context createSubcontext(Name name) throws NamingException {
	return createSubcontext(name.toString());
  }
  public Context createSubcontext(String name) throws NamingException {
	throw(new NamingException("Not yet available"));
  }
  public void destroySubcontext(Name name) throws NamingException {
	destroySubcontext(name.toString());
  }
  public void destroySubcontext(String name) throws NamingException {
	throw(new NamingException("Not yet available"));
  }
  public Hashtable getEnvironment() throws NamingException {
	throw(new NamingException("Not yet available"));
  }
  public String getNameInNamespace() throws NamingException {
	throw(new NamingException("Not yet available"));
  }
  public NameParser getNameParser(Name name) throws NamingException {
	return getNameParser(name.toString());
  }
  public NameParser getNameParser(String name) throws NamingException {
	throw(new NamingException("Not yet available"));
  }
  public NamingEnumeration listBindings(Name name) throws NamingException {
	return listBindings(name.toString());
  }
  public NamingEnumeration listBindings(String name) throws NamingException {
	throw(new NamingException("Not yet available"));
  }
  public Object lookupLink(Name name) throws NamingException {
	return lookupLink(name.toString());
  }
  public Object lookupLink(String name) throws NamingException {
	throw(new NamingException("Not yet available"));
  }
  public Object removeFromEnvironment(String propName) throws NamingException {
	throw(new NamingException("Not yet available"));
  }
  public void rename(Name oldName, Name newName) throws NamingException {
	rename(oldName.toString(),newName.toString());
  }
  public void rename(String oldName, String newName) throws NamingException {
	throw(new NamingException("Not yet available"));
  }
  //============================ end not yet available =======================
}
