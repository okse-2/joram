/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package fr.dyade.aaa.agent.services;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.ow2.util.substitution.engine.DefaultSubstitutionEngine;
import org.ow2.util.substitution.resolver.ChainedResolver;
import org.ow2.util.substitution.resolver.PropertiesResolver;
import org.ow2.util.substitution.resolver.RecursiveResolver;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.ServerDesc;
import fr.dyade.aaa.common.Debug;

/**
 * Start the a3 server with a ConfigAdmin file.
 * <p>The default values 
 * <ul>
 *  <li> sid (server id) : 0
 *  <li> storage : s0
 *  <li> pathToConf : "."
 * </ul>
 * 
 * <p><hr>
 * The reserved words for properties:
 * <ul>
 *  <li> sid
 *  <li> storage
 *  <li> pathToConf
 * </ul>
 * 
 * <p><hr>
 * A simple example:
 * <p><blockquote><pre>
 *  &lt;configadmin&gt;
 *   &lt;configuration pid="fr.dyade.aaa.agent.services.A3ManagedService"&gt;
 *     &lt;property name="sid"&gt;0&lt;/property&gt;
 *     &lt;property name="storage"&gt;your_path/s0&lt;/property&gt;
 *     &lt;property name="pathToConf"&gt;your_path/conf&lt;/property&gt;
 *     &lt;!--  properties  --&gt;
 *     &lt;property name="Transaction"&gt;fr.dyade.aaa.util.NTransaction&lt;/property&gt;
 *     &lt;property name="Transaction.UseLockFile"&gt;false&lt;/property&gt;
 *   &lt;/configuration&gt;
 *  &lt;/configadmin&gt;
 * </pre></blockquote>
 * 
 */
public class A3ManagedService implements ManagedService, BundleActivator { 

	public static final Logger logmon = Debug.getLogger(A3ManagedService.class.getName());
	// The reserved words.
	/** the server id */
	public static final String SID = "sid";
	/** persistence directory */
	public static final String STORAGE = "storage";
	/** the path to the configuration (a3servers.xml, ...) */
	public static final String PATH_TO_CONF = "pathToConf";
	
	private short sid = 0;
	private String path = "s0";
	
  private BundleContext bundleContext;
  private ServiceRegistration registration;

  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(final BundleContext bundleContext) throws Exception {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "start(" + bundleContext + ')');
    this.bundleContext = bundleContext;
    Properties props = new Properties();
    props.setProperty(Constants.SERVICE_PID, A3ManagedService.class.getName());
    registration = this.bundleContext.registerService(
    		ManagedService.class.getName(),
        this,
        props);
  }
  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
  	doStop();
  	registration.unregister();
  }

  protected void doStart() throws Exception {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "A3ManagedService.doStart()");
  	 try {
       AgentServer.init(sid, path, null);
     } catch (Exception exc) {
       logmon.log(BasicLevel.ERROR, AgentServer.getName() + " initialization failed", exc);
       throw exc;
     }

     try {
       String errStr = AgentServer.start();
       if (errStr == null) {
      	 if (logmon.isLoggable(BasicLevel.INFO))
           logmon.log(BasicLevel.INFO, AgentServer.getName() + " started: " + AgentServer.OKSTRING);
       } else {
      	 if (logmon.isLoggable(BasicLevel.INFO)) {
           logmon.log(BasicLevel.INFO, AgentServer.getName() + " started: " + AgentServer.ERRORSTRING +
          		 "\n" + errStr + "\n" + AgentServer.ENDSTRING);
      	 }
       }
     } catch (Exception exc) {
       logmon.log(BasicLevel.ERROR, AgentServer.getName() + " failed", exc);
       throw exc;
     }
     
     // Register ServerDesc
     ServerDesc serverDesc = AgentServer.getServerDesc(sid);
     Properties props = new Properties();
     props.setProperty("sid", ""+sid);
     props.setProperty("name", serverDesc.getServerName());
     props.setProperty("host", serverDesc.getHostname());
     props.setProperty("port", ""+serverDesc.getPort());
     bundleContext.registerService(
    		 ServerDesc.class.getName(),
    		 serverDesc,
         props);
     if (logmon.isLoggable(BasicLevel.DEBUG))
       logmon.log(BasicLevel.DEBUG, "A3managedService ServerDesc register: " + serverDesc); 
  }

  protected void doStop() {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "A3ManagedService.doStop(): AgentServer status = " + AgentServer.getStatusInfo());
  	if (AgentServer.getStatus() == AgentServer.Status.STARTING ||
  			AgentServer.getStatus() == AgentServer.Status.STARTED) {
  		AgentServer.stop();
  		AgentServer.reset();
  	}
  }

  //************ ManagedService ************
  public String getName() {
	  return "A3ManagedService";
  }

  /* (non-Javadoc)
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  public void updated(Dictionary properties) throws ConfigurationException {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "updated(" + properties + ')');
  	
  	if (properties == null) {
  		doStop();
  		return;
  	}
  	
  	String sidStr = (String) properties.get(SID);
  	if (sidStr != null && sidStr.length() > 0) {
  		sid = new Short(sidStr).shortValue();
  	}
  	
		DefaultSubstitutionEngine engine = new DefaultSubstitutionEngine();
		ChainedResolver resolver = new ChainedResolver();
		resolver.getResolvers().add(new PropertiesResolver(System.getProperties()));
		engine.setResolver(new RecursiveResolver(engine, resolver));
		
		String storage = (String) properties.get(STORAGE);
		if (storage != null && storage.length() > 0) {
			path = engine.substitute(storage);
		}
		
		String pathToConf = (String) properties.get(PATH_TO_CONF);
		if (pathToConf != null && pathToConf.length() > 0) {
			System.setProperty(AgentServer.CFG_DIR_PROPERTY, engine.substitute(pathToConf));
		}
		
		// set System properties if needed
		Enumeration en = properties.keys();
		while (en.hasMoreElements()) {
	    String key = (String) en.nextElement();
	    if (SID.equals(key) || STORAGE.equals(key) || PATH_TO_CONF.equals(key)) {
	    	// reserved word.
	    	continue;
	    } else {
	    	String value = (String) properties.get(key);
	    	if (value != null)
	    		System.setProperty(key, value);
	    }
    }
		
		// start the a3 server
		try {
	    doStart();
    } catch (Exception e) {
    	if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "updated:: doStart EXCEPTION", e);
	    throw new ConfigurationException(null, e.getMessage());
    }
  }
}