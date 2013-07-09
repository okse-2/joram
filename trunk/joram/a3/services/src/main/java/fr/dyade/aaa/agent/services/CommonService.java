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
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.ow2.util.substitution.engine.DefaultSubstitutionEngine;
import org.ow2.util.substitution.resolver.ChainedResolver;
import org.ow2.util.substitution.resolver.PropertiesResolver;
import org.ow2.util.substitution.resolver.RecursiveResolver;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.ServerDesc;
import fr.dyade.aaa.common.Debug;

/**
 * 
 */
public class CommonService { 
	public static final Logger logmon = Debug.getLogger(CommonService.class.getName());
	
	// The reserved words.
	/** the server id */
	public static final String SID = "sid";
	/** persistence directory */
	public static final String STORAGE = "storage";
	/** the path to the configuration (a3servers.xml, ...) */
	public static final String PATH_TO_CONF = "pathToConf";
	
	private short sid = 0;
	private String path = "s0";
	
	protected BundleContext bundleContext;
  
	public CommonService(final BundleContext bundleContext) {
	  this.bundleContext = bundleContext;
	}
	
  protected void doStart() throws Exception {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "CommonService.doStart()");
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
       logmon.log(BasicLevel.DEBUG, "CommonService ServerDesc register: " + serverDesc); 
  }

  protected void doStop() {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "CommonService.doStop(): AgentServer status = " + AgentServer.getStatusInfo());
  	if (AgentServer.getStatus() == AgentServer.Status.STARTING ||
  			AgentServer.getStatus() == AgentServer.Status.STARTED) {
  		AgentServer.stop();
  		AgentServer.reset();
  	}
  }

  //************ ManagedService ************
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
	    Object key = en.nextElement();
	    if (SID.equals(key) || STORAGE.equals(key) || PATH_TO_CONF.equals(key)) {
	    	// reserved word.
	    	continue;
	    } else {
	    	Object value = properties.get(key);
	    	if (value != null && key instanceof String && value instanceof String)
	    		System.setProperty((String) key, (String) value);
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