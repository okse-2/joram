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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import fr.dyade.aaa.util.*;

/**
 * Object which manages services.
 * There is only one <code>ServiceManager</code> object per agent server.
 * The <code>ServiceManager</code> object is initialized in <code>init</code>,
 * called from <code>AgentServer.init</code>. This classes reuses the
 * persistency service provided by <code>Transaction</code>.
 *
 * @author	Freyssinet Andre
 */
public class ServiceManager implements Serializable {
  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: ServiceManager.java,v 1.3 2001-08-31 08:13:59 tachkeni Exp $"; 

  /** the unique <code>ServiceManager</code> in the agent server */
  static ServiceManager manager;
  
  /**
   * Initializes the <code>ServiceManager</code> object. Synchronize the
   * persistent image and the configuration file.
   *
   * @exception Exception	unspecialized exception
   */
  static void init() throws Exception {
    manager = ServiceManager.load();
    if (manager == null) {
      manager = new ServiceManager();
    }
  }

  /**
   * Builds object from persistent image.
   *
   * @return	loaded object or null if no persistent image exists
   *
   * @exception IOException
   *	when accessing the stored image
   * @exception ClassNotFoundException
   *	if the corresponding image class may not be found
   */
  static ServiceManager load() throws IOException, ClassNotFoundException {
    return (ServiceManager) AgentServer.transaction.load("serviceManager");
  }

  /**
   * Saves object in persistent storage.
   */
  static void save() throws IOException {
    AgentServer.transaction.save(manager, "serviceManager");
  }

  /** repository holding <code>Service</code>s */
  Hashtable registry;

  /**
   * Default constructor.
   */
  private ServiceManager() {
    registry = new Hashtable();
  }

  /**
   * Start a <code>Service</code> defined by its descriptor.
   *
   * @param desc	service descriptor.
   */
  static void start (ServiceDesc desc) throws Exception {
    if (desc.running)
      throw new Exception("Service already running");
    Class ptypes[] = new Class[2];
    Object args[] = new Object[2];

    ptypes[0] = Class.forName("java.lang.String");
    ptypes[1] = Boolean.TYPE;
    Class service = Class.forName(desc.getClassName());
    Method init = service.getMethod("init", ptypes);
    args[0] = desc.getArguments();
    args[1] = new Boolean(! desc.isInitialized());
    init.invoke(null, args);
    desc.running = true;
  }

  /**
   * Start a <code>Service</code> identified by its name.
   *
   * @param scname	service class name.
   */
  static void start (String scname) throws Exception {
    ServiceDesc desc = (ServiceDesc) manager.registry.get(scname);
    if (desc == null)
      throw new NoSuchElementException("Unknown service: " + scname);
    start(desc);
  }

  /**
   * Starts all defined services.
   */
  static void start() {
    // Launch all services defined in A3CML file
    for (Enumeration e = manager.registry.elements();
	 e.hasMoreElements() ;) {
      ServiceDesc desc = (ServiceDesc) e.nextElement();
      try {
	start(desc);
      } catch (Exception exc) {
	if (Debug.error)
	  Debug.trace("cannot start service: " + desc.getClassName(), exc);
      }
    }
  }

  /**
   * Stop a <code>Service</code> defined by its descriptor.
   *
   * @param desc	service descriptor.
   */
  static void stop(ServiceDesc desc) throws Exception {
    if (! desc.running)
      throw new Exception("Service already stopped");
    Class service = Class.forName(desc.getClassName());
    Method stop = service.getMethod("stop", new Class[0]);
    stop.invoke(null, new Object[0]);
    desc.running = false;
  }

  /**
   * Stop a <code>Service</code> identified by its name.
   *
   * @param scname	service class name.
   */
  static void stop(String scname) throws Exception {
    ServiceDesc desc = (ServiceDesc) manager.registry.get(scname);
    if (desc == null)
      throw new NoSuchElementException("Unknown service: " + scname);
    stop(desc);
  }

  /**
   * Stops all running services.
   */
  static void stop () {
    for (Enumeration e = manager.registry.elements();
	 e.hasMoreElements() ;) {
      ServiceDesc desc = (ServiceDesc) e.nextElement();
      try {
	if (desc.running) stop(desc);
      } catch (Throwable exc) {
	if (Debug.error)
	  Debug.trace("cannot stop service " + desc.getClassName(), exc);
      }
    }
  }

  /**
   * Registers a new <code>Service</code> object.
   *
   * @param scname	service class name.
   * @param args	launching arguments.
   */
  static void register(String scname, String args) {
    synchronized (manager) {
      // Temporary fix
      ServiceDesc desc =  new ServiceDesc(scname, args);
      if (manager.registry.put(scname, desc) != null)
	// It is already initialized
	desc.setInitialized(true);
    }
  }

  /**
   * Unregisters useless <code>Service</code>.
   *
   * @param scname	service class name.
   */
  static void unregister(String scname) {
    synchronized (manager) {
      manager.registry.remove(scname);
    }
  }

  static ServiceDesc[] getServices() {
    Collection values = manager.registry.values();
    ServiceDesc[] services = new ServiceDesc[values.size()];
    try {
      services = (ServiceDesc[]) values.toArray(services);
    } catch (ArrayStoreException exc) {
      if (Debug.error) Debug.trace("Can't get services.", exc);
    }
    return services;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",registry=" + Strings.toString(registry) + ")";
  }
}
