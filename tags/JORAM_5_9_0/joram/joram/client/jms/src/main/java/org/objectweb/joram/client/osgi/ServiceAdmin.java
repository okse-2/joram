/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.osgi;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.admin.AdminItf;
import org.objectweb.joram.client.jms.admin.JoramAdmin;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import fr.dyade.aaa.common.Debug;

/**
 * Create Admin Wrapper with a ConfigAdmin file.
 * <p>Default values:
 * <ul>
 *  <li> adminUser : root
 *  <li> adminPass : root
 *  <li> adminHost : localhost
 *  <li> adminPort : 16010
 *  <li> name : admin
 *  <li> collocated : true
 *  <li> connectingTimer : 60
 *  <li> identityClassName : org.objectweb.joram.shared.security.SimpleIdentity
 * </ul>
 * 
 * <p><hr>
 * The reserved words for properties:
 * <ul>
 *  <li> adminHost
 *  <li> adminPort
 *  <li> adminUser
 *  <li> adminPass
 *  <li> name
 *  <li> identityClassName
 *  <li> collocated
 *  <li> connectingTimer
 * </ul>
 * 
 * <p><hr>
 * A simple example:
 * <p><blockquote><pre>
 *  &lt;configadmin&gt;
 *   &lt;factory-configuration pid="joram.admin"&gt;
 *     &lt;property name="name"&gt;wrapper&lt;/property&gt;
 *     &lt;property name="adminUser"&gt;anonymous&lt;/property&gt;
 *     &lt;property name="adminPass"&gt;anonymous&lt;/property&gt;
 *     &lt;!--  properties  --&gt;
 *   &lt;/factory-configuration&gt;
 *  &lt;/configadmin&gt;
 * </pre></blockquote>
 * <p>
 */
public class ServiceAdmin implements ManagedServiceFactory {
	public static final Logger logmon = Debug.getLogger(ServiceAdmin.class.getName());
	public static final String PID_NAME = "joram.admin";
	
	//reserved words
	public static final String ADMIN_HOST = "adminHost";
	public static final String ADMIN_PORT = "adminPort";
	public static final String ADMIN_USERNAME = "adminUser";
	public static final String ADMIN_USERPASS = "adminPass";
	public static final String NAME = "name";
	public static final String IDENTITYCLASS = "identityClassName";
	public static final String COLLOCATED = "collocated";
	public static final String CONNECTING_TIMER = "connectingTimer";
	
  private BundleContext bundleContext;
  private ServiceRegistration registration;
  private HashMap<String, AdminItf> wrappers;
  private HashMap<String, Connection> connections;

  public ServiceAdmin(final BundleContext bundleContext) {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "ServiceAdmin(" + bundleContext + ')');
    this.bundleContext = bundleContext;
    Properties props = new Properties();
    props.setProperty(Constants.SERVICE_PID, PID_NAME);
    registration = this.bundleContext.registerService(
    		ManagedServiceFactory.class.getName(),
        this,
        props);
    wrappers = new HashMap<String, AdminItf>();
    connections = new HashMap<String, Connection>();
  }
  
  private final boolean isSet(String value) {
    return value != null && value.length() > 0;
  }
  
	public void doStop() {
	  // unregister the service
	  if (registration != null)
	    registration.unregister();
//	  if (connections != null && !connections.isEmpty()) {
//	    Collection<Connection> cnxs = connections.values();
//	    Iterator<Connection> it = cnxs.iterator();
//	    while (it.hasNext()) {
//        Connection connection = (Connection) it.next();
//        try {
//          connection.close();
//        } catch (JMSException e) { }
//      }
//	    connections.clear();
//	  }
  }
	
	public void doUpdated(String pid, Dictionary properties) {
	  if (logmon.isLoggable(BasicLevel.DEBUG))
	    logmon.log(BasicLevel.DEBUG, "doUpdated(" + pid + ", " + properties + ')');

	  try {
	    AdminItf wrapper = wrappers.get(pid);

	    if (wrapper == null) {
	      String name = (String) properties.get(NAME);
	      if (!isSet(name)) 
	        name = "admin";

	      String hostName = (String) properties.get(ADMIN_HOST);
	      if (!isSet(hostName)) 
	        hostName = "localhost";

	      String serverPortValue = (String) properties.get(ADMIN_PORT);
	      if (!isSet(serverPortValue)) 
	        serverPortValue = "16010";
	      int serverPort = Integer.parseInt(serverPortValue);

	      String rootName = (String) properties.get(ADMIN_USERNAME);
	      if (!isSet(rootName)) 
	        rootName = "root";

	      String rootPasswd = (String) properties.get(ADMIN_USERPASS);
	      if (!isSet(rootPasswd)) 
	        rootPasswd = "root";

	      String collocatedValue = (String) properties.get(COLLOCATED);
	      if (!isSet(collocatedValue)) 
	        collocatedValue = "true";
	      boolean collocated = Boolean.parseBoolean(collocatedValue);

	      String identityClassName = (String) properties.get(IDENTITYCLASS);
	      if (!isSet(identityClassName)) {
	        identityClassName = SimpleIdentity.class.getName();
	      }

	      String connectingTimerValue = (String) properties.get(CONNECTING_TIMER);
	      if (!isSet(connectingTimerValue)) 
	        connectingTimerValue = "60";
	      int connectingTimer = Integer.parseInt(connectingTimerValue);

	      ConnectionFactory cf = null;

	      if (collocated)
	        cf = LocalConnectionFactory.create();
	      else
	        cf = TcpConnectionFactory.create(hostName, serverPort);

	      if (connectingTimer == 0)
	        cf.getParameters().connectingTimer = 60;
	      else
	        cf.getParameters().connectingTimer = connectingTimer;

	      cf.setIdentityClassName(identityClassName);

	      Connection cnx = null;
	      int nbRetry = 10;
	      while (nbRetry > 0) {
	        try {
	          cnx = cf.createConnection(rootName, rootPasswd);
	          cnx.start();
	          wrapper = new JoramAdmin(cnx);
	          break;
	        } catch (JMSException e) {
	          Thread.sleep(100);
	          nbRetry--;
	          if (logmon.isLoggable(BasicLevel.DEBUG))
	            logmon.log(BasicLevel.DEBUG, "doUpdated admin connect retry =  " + (10 - nbRetry));
	        }
	      }

	      if (wrapper == null) {
	        if (logmon.isLoggable(BasicLevel.ERROR))
	          logmon.log(BasicLevel.ERROR, "doUpdated admin wrapper not start !");
	        return;
	      }

	      // register wrapper as a service
	      Properties props = new Properties();
	      props.setProperty("name", name);
	      props.setProperty("host", hostName);
	      props.setProperty("port", ""+serverPort);
	      props.setProperty("user", rootName);

	      registration =  bundleContext.registerService(
	          AdminItf.class.getName(),
	          wrapper,
	          props);

	      if (logmon.isLoggable(BasicLevel.DEBUG))
	        logmon.log(BasicLevel.DEBUG, "doUpdated registration =  " + registration);

	      wrappers.put(pid, wrapper);
	      connections.put(pid, cnx);
	    }

	  } catch (Exception e) {
	    if (logmon.isLoggable(BasicLevel.ERROR))
	      logmon.log(BasicLevel.ERROR, "doUpdated ", e);
	  }
	}
  
  //************ ManagedServiceFactory ************
  public String getName() {
	  return PID_NAME;
  }

	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedServiceFactory#updated(java.lang.String, java.util.Dictionary)
	 */
	public void updated(final String pid, final Dictionary properties)
      throws ConfigurationException {
		if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "updated(" + pid + ", " + properties + ')');
		
		if (properties == null) {
			deleted(pid);
			return;
		}
		
		new Thread(new Runnable() {
      public void run() {
        doUpdated(pid, properties);
      }
    }).start();
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
	 */
	public void deleted(String pid) {
		if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "deleted(" + pid + ')');
	  
		if (wrappers.containsKey(pid)) {
			AdminItf wrapper = wrappers.remove(pid);
			Connection cnx = connections.get(pid);
			try {
				if (wrapper != null)
				  wrapper.close();
				if (cnx != null)
				  cnx.close();
      } catch (Exception e) {
      	if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN, "deleted " + wrapper, e);
      }
		}
  }
}