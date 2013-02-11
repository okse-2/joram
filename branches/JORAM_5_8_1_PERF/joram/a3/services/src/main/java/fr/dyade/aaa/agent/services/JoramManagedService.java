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
import java.util.Properties;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

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
 *   &lt;configuration pid="joram.server"&gt;
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
public class JoramManagedService extends CommonService implements ManagedService { 
	public static final Logger logmon = Debug.getLogger(JoramManagedService.class.getName());
	
	/** the PID name */
	public static final String PID_NAME= "joram.server";

	private ServiceRegistration registration;
	private boolean initialized = false; 
  
  /**
   * 
   */
  public JoramManagedService(final BundleContext bundleContext) throws Exception {
    super(bundleContext);
  	if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "JoramManagedService<" + bundleContext + '>');
    Properties props = new Properties();
    props.setProperty(Constants.SERVICE_PID, PID_NAME);
    registration = bundleContext.registerService(
    		ManagedService.class.getName(),
        this,
        props);
  }


  protected void doStop() {
  	super.doStop();
  	registration.unregister();
  }

  //************ ManagedService ************
  public String getName() {
	  return PID_NAME;
  }

  /* (non-Javadoc)
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  public void updated(Dictionary properties) throws ConfigurationException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "JoramManagerService.updated(" + properties + ')');
    if (properties == null) {
      if (initialized)
        doStop();
      return;
    }
    
    initialized = true;
  	super.updated(properties);
  }
}