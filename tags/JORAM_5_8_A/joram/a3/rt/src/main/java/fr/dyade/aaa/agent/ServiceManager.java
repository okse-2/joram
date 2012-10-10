/*
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Strings;

/**
 * Object which manages services.
 * There is only one <code>ServiceManager</code> object per agent server.
 * The <code>ServiceManager</code> object is initialized in <code>init</code>,
 * called from <code>AgentServer.init</code>. This classes reuses the
 * persistency service provided by <code>Transaction</code>.
 */
public class ServiceManager implements Serializable {
  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  /** the unique <code>ServiceManager</code> in the agent server */
  static ServiceManager manager;

  static Logger xlogmon = null;

  private static String name = null;

  public final static String getName() {
    if (name == null)
      name = AgentServer.getName() + ".ServiceManager";
    return name;
  }
  
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
    
// TODO(AF): no longer needed.
//    if (manager.trackers == null) {
//      manager.trackers = new HashMap();
//    }
    save();
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
    return (ServiceManager) AgentServer.getTransaction().load("serviceManager");
  }

  /**
   * Saves object in persistent storage.
   */
  static void save() throws IOException {
    AgentServer.getTransaction().save(manager, "serviceManager");
    // with OSGi the service start asynchronously, 
    // and perhaps without running transaction.
    AgentServer.getTransaction().begin();
    AgentServer.getTransaction().commit(true);
    if (xlogmon.isLoggable(BasicLevel.DEBUG))
      xlogmon.log(BasicLevel.DEBUG, getName() + " service manager saved.");
  }

  /** repository holding <code>Service</code>s */
  Hashtable<String, ServiceDesc> registry;

// TODO(AF): no longer needed.
//  transient Map trackers;

  /**
   * Default constructor.
   */
  private ServiceManager() {
    registry = new Hashtable<String, ServiceDesc>();
  }

  /**
   * Start a <code>Service</code> defined by its descriptor.
   *
   * @param desc	service descriptor.
   */
  public static void start(ServiceDesc desc) throws Exception {
    if (xlogmon.isLoggable(BasicLevel.DEBUG))
      xlogmon.log(BasicLevel.DEBUG, getName() + " start service: " + desc);

    if (desc.running)
      throw new Exception("Service already running");
    Class ptypes[] = new Class[] { String.class, Boolean.TYPE };
    Object args[] = new Object[] { desc.getArguments(), new Boolean(!desc.isInitialized()) };
    
    Class service = null;
    service = Class.forName(desc.getClassName());
    Method init = service.getMethod("init", ptypes);

    init.invoke(null, args);
    desc.running = true;
    desc.initialized = true;
    
    if (xlogmon.isLoggable(BasicLevel.DEBUG)) {
      xlogmon.log(BasicLevel.DEBUG, getName() + " service started");
    }
    save();
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
    for (Enumeration<ServiceDesc> e = manager.registry.elements(); e.hasMoreElements() ;) {
      ServiceDesc desc = e.nextElement();
      try {
        start(desc);
      } catch (Exception exc) {
        xlogmon.log(BasicLevel.ERROR,
                   getName() + ", cannot start service:" + desc.getClassName(), exc);
      }
    }
  }

  /**
   * Stop a <code>Service</code> defined by its descriptor.
   *
   * @param desc	service descriptor.
   */
  public static void stop(ServiceDesc desc) throws Exception {
    // DF: idempotency (could be done in AgentAdmin)
    if (! desc.running) return;
//       throw new Exception("Service already stopped");
    Class service = Class.forName(desc.getClassName());
    Method stop = service.getMethod("stopService", new Class[0]);
    stop.invoke(null, (Object[]) null);
    desc.running = false;

    if (xlogmon.isLoggable(BasicLevel.DEBUG))
      xlogmon.log(BasicLevel.DEBUG, "Service " + desc.scname + " stopped.");
  }

  /**
   * Stop a <code>Service</code> identified by its name.
   *
   * @param scname	service class name.
   */
  public static void stop(String scname) throws Exception {
    ServiceDesc desc = manager.registry.get(scname);
    if (desc == null)
      throw new NoSuchElementException("Unknown service: " + scname);
    stop(desc);
  }

  /**
   * Stops all running services.
   */
  static void stop() {
    if ((manager == null) || (manager.registry == null))
      return;

    for (Enumeration<ServiceDesc> e = manager.registry.elements(); e.hasMoreElements();) {
      ServiceDesc desc = e.nextElement();
      try {
        if (xlogmon.isLoggable(BasicLevel.DEBUG))
          xlogmon.log(BasicLevel.DEBUG, getName() + ", stops: " + desc);

        if (desc.running)
          stop(desc);

        if (xlogmon.isLoggable(BasicLevel.DEBUG))
          xlogmon.log(BasicLevel.DEBUG, getName() + ", service stopped");
      } catch (Throwable exc) {
        if (xlogmon.isLoggable(BasicLevel.WARN))
          xlogmon.log(BasicLevel.WARN, getName() + ", cannot stop service: " + desc, exc);
      }
    }
  }

  /**
   * Registers a new <code>Service</code> object.
   *
   * @param scname	service class name.
   * @param args	launching arguments.
   */
  public static void register(String scname, String args) {
  	synchronized (manager) {
  		ServiceDesc desc = (ServiceDesc) manager.registry.get(scname);
  		if (xlogmon.isLoggable(BasicLevel.DEBUG))
  			xlogmon.log(BasicLevel.DEBUG, getName() + ", register " + scname + " -> " + desc);
  		if (desc == null) {
  			desc =  new ServiceDesc(scname, args);
  			manager.registry.put(scname, desc);
  		} else {
  			desc.args = args;
  		}
  		try {
  			save();
  		} catch (IOException e) {
  			if (xlogmon.isLoggable(BasicLevel.ERROR))
  				xlogmon.log(BasicLevel.ERROR, getName() + ", register save service manager." + scname, e);
  		}
  	}
  }

  /**
   * Unregisters useless <code>Service</code>.
   *
   * @param scname	service class name.
   */
  static void unregister(String scname) {
  	if (xlogmon.isLoggable(BasicLevel.DEBUG))
  		xlogmon.log(BasicLevel.DEBUG, getName() + ", unregister " + scname);
  	synchronized (manager) {
  		manager.registry.remove(scname);
  		try {
  			save();
  		} catch (IOException e) {
  			if (xlogmon.isLoggable(BasicLevel.ERROR))
  				xlogmon.log(BasicLevel.ERROR, getName() + ", unregister save service manager." + scname, e);
  		}
  	}
  }
  
  public static ServiceDesc getService(String serviceClassName) {
    return (ServiceDesc) manager.registry.get(serviceClassName);
  }

  static ServiceDesc[] getServices() {
    ServiceDesc[] services = new ServiceDesc[manager.registry.size()];
    int i = 0;
    for (Enumeration<ServiceDesc> e = manager.registry.elements(); e.hasMoreElements();) {
      services[i++] = e.nextElement();
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
    StringBuffer output = new StringBuffer();
    output.append('(');
    output.append(super.toString());
    output.append(",registry=").append(Strings.toString(registry));
    output.append(')');
    return output.toString();
  }
}
