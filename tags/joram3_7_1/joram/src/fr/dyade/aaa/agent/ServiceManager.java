/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.agent;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.*;
import fr.dyade.aaa.agent.conf.*;

/**
 * Object which manages services.
 * There is only one <code>ServiceManager</code> object per agent server.
 * The <code>ServiceManager</code> object is initialized in <code>init</code>,
 * called from <code>AgentServer.init</code>. This classes reuses the
 * persistency service provided by <code>Transaction</code>.
 */
public class ServiceManager implements Serializable {
  /** the unique <code>ServiceManager</code> in the agent server */
  static ServiceManager manager;

  static Logger xlogmon = null;
  
  /**
   * Initializes the <code>ServiceManager</code> object. Synchronize the
   * persistent image and the configuration file.
   *
   * @exception Exception	unspecialized exception
   */
  static void init() throws Exception {
    // Get the logging monitor from current server MonologMonitorFactory
    xlogmon = Debug.getLogger(Debug.A3Service);

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
  static void start(ServiceDesc desc) throws Exception {
    xlogmon.log(BasicLevel.DEBUG,
                "ServiceManager#" + AgentServer.getServerId() +
                " start service " + desc);

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
    desc.initialized = true;

    xlogmon.log(BasicLevel.DEBUG,
                "ServiceManager#" + AgentServer.getServerId() +
                " service started");
  }

  /**
   * Start a <code>Service</code> identified by its name.
   *
   * @param scname	service class name.
   */
  static void start(String scname) throws Exception {
    ServiceDesc desc = (ServiceDesc) manager.registry.get(scname);
    if (desc == null)
      throw new NoSuchElementException("Unknown service: " + scname);
    start(desc);
  }

  /**
   * Starts all defined services.
   */
  static void start() throws Exception {
    // Launch all services defined in A3CML file
    for (Enumeration e = manager.registry.elements();
	 e.hasMoreElements() ;) {
      ServiceDesc desc = (ServiceDesc) e.nextElement();
      try {
	start(desc);
      } catch (Exception exc) {
        xlogmon.log(BasicLevel.ERROR,
                   "AgentServer#" + AgentServer.getServerId() +
                   ".ServiceManager, cannot start service:" +
                   desc.getClassName(), exc);
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
    Method stop = service.getMethod("stopService", new Class[0]);
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
  static void stop() {
    if ((manager == null) ||
        (manager.registry == null)) return;

    for (Enumeration e = manager.registry.elements();
	 e.hasMoreElements() ;) {
      ServiceDesc desc = (ServiceDesc) e.nextElement();
      try {
        if (xlogmon.isLoggable(BasicLevel.DEBUG))
          xlogmon.log(BasicLevel.DEBUG,
                      "AgentServer#" + AgentServer.getServerId() +
                      ".ServiceManager, stops " + desc);

	if (desc.running) stop(desc);

        if (xlogmon.isLoggable(BasicLevel.DEBUG))
          xlogmon.log(BasicLevel.DEBUG,
                      "AgentServer#" + AgentServer.getServerId() +
                      ".ServiceManager, " + desc + " stopped");
      } catch (Throwable exc) {
        xlogmon.log(BasicLevel.WARN,
                   "AgentServer#" + AgentServer.getServerId() +
                   ".ServiceManager, cannot stop service " +
                   desc.getClassName(), exc);
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
      ServiceDesc desc = (ServiceDesc) manager.registry.get(scname);
      xlogmon.log(BasicLevel.DEBUG,
                  "AgentServer#" + AgentServer.getServerId() +
                  ".ServiceManager.register " + scname + " -> " + desc);
      if (desc == null) {
        desc =  new ServiceDesc(scname, args);
        manager.registry.put(scname, desc);
      } else {
        desc.args = args;
      }
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
    ServiceDesc[] services = new ServiceDesc[manager.registry.size()];
    int i = 0;
    for (Enumeration e = manager.registry.elements(); e.hasMoreElements();) {
      services[i++] = (ServiceDesc) e.nextElement();
    }
// 1.2    Collection values = manager.registry.values();
// 1.2    ServiceDesc[] services = new ServiceDesc[values.size()];
// 1.2    try {
// 1.2      services = (ServiceDesc[]) values.toArray(services);
// 1.2    } catch (ArrayStoreException exc) {
// 1.2      xlogmon.log(BasicLevel.ERROR,
// 1.2                 "AgentServer#" + AgentServer.getServerId() +
// 1.2                 ".ServiceManager, can't get services.", exc);
// 1.2    }
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
