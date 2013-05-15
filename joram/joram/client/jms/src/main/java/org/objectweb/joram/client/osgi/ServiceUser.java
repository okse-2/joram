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
package org.objectweb.joram.client.osgi;

import java.net.ConnectException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminItf;
import org.objectweb.joram.client.jms.admin.User;
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
 * Create Users with a ConfigAdmin file.
 * <p>Default values:
 * <ul>
 *  <li> user : anonymous
 *  <li> password : anonymous
 *  <li> serverId : 0
 *  <li> identityClassName : org.objectweb.joram.shared.security.SimpleIdentity
 * </ul>
 * 
 * <p><hr>
 * The reserved words for properties:
 * <ul>
 *  <li> adminWrapper
 *  <li> adminHost
 *  <li> adminPort
 *  <li> adminUser
 *  <li> name
 *  <li> password
 *  <li> serverId
 *  <li> identityClassName
 *  <li> subName
 *  <li> threshold
 *  <li> nbMaxMsg
 *  <li> jndiName
 *  <li> dmq
 *  <li> dmqSid
 * </ul>
 * 
 * <p><hr>
 * A simple example:
 * <p><blockquote><pre>
 *  &lt;configadmin&gt;
 *   &lt;factory-configuration pid="joram.user"&gt;
 *     &lt;property name="adminWrapper"&gt;ra&lt;/property&gt;
 *     &lt;property name="serverId"&gt;0&lt;/property&gt;
 *     &lt;property name="name"&gt;anonymous&lt;/property&gt;
 *     &lt;property name="password"&gt;anonymous&lt;/property&gt;
 *     &lt;!--  properties  --&gt;
 *   &lt;/factory-configuration&gt;
 *  &lt;/configadmin&gt;
 * </pre></blockquote>
 * <p>
 */
public class ServiceUser implements ManagedServiceFactory {
	public static final Logger logmon = Debug.getLogger(ServiceUser.class.getName());
	public static final String PID_NAME = "joram.user";
	
	//reserved words
	public static final String ADMIN_WRAPPER = "adminWrapper";
	public static final String ADMIN_HOST = "adminHost";
	public static final String ADMIN_PORT = "adminPort";
	public static final String ADMIN_USERNAME = "adminUser";
	public static final String NAME = "name";
	public static final String PASSWORD = "password";
	public static final String SERVERID = "serverId";
	public static final String IDENTITYCLASS = "identityClassName";
	public static final String SUBNAME = "subName";
	public static final String THRESHOLD = "threshold";
	public static final String NB_MAXMSG = "nbMaxMsg";
	public static final String JNDINAME = "jndiName";
	public static final String DMQ = "dmq";
	public static final String DMQ_SID = "dmqSid";
	
  private BundleContext bundleContext;
  private ServiceRegistration registration;
  private HashMap<String, User> users;
  private HashMap<String, String> jndiNames;
	private JndiHelper jndiHelper;

  public ServiceUser(final BundleContext bundleContext) {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "ServiceUser(" + bundleContext + ')');
    this.bundleContext = bundleContext;
    Properties props = new Properties();
    props.setProperty(Constants.SERVICE_PID, PID_NAME);
    registration = this.bundleContext.registerService(
    		ManagedServiceFactory.class.getName(),
        this,
        props);
    users = new HashMap<String, User>();
    jndiHelper = new JndiHelper();
    jndiNames = new HashMap<String, String>();
  }
  
  private final boolean isSet(String value) {
    return value != null && value.length() > 0;
  }
  
  private void setUserDMQ(AdminItf wrapper, User user, String dmq, int dmqSid) throws ConnectException, AdminException {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "setUserDMQ(" + wrapper + ", " + user + ", " + dmq + ", " +  + dmqSid +')');
  	Destination[] destinations = wrapper.getDestinations(dmqSid);
  	if (destinations != null) {
  		for (int i = 0; i < destinations.length; i++) {
  			if (destinations[i] instanceof Queue && dmq.equals(destinations[i].getName())) {
  				user.setDMQ((Queue) destinations[i]);
  				return;
  			}
  		}
  	}
  	if (logmon.isLoggable(BasicLevel.WARN))
  		logmon.log(BasicLevel.WARN, "setDestinationDMQ: the DMQ \"" + dmq + "\" on server \"" + dmqSid + "\" not found.");
  }
  
	public void doStop() {
	  // unregister the service
	  if (registration != null)
	    registration.unregister();
  }
	
  public void doUpdated(String pid, Dictionary properties) {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "doUpdated(" + pid + ", " + properties + ')');

  	try {
  		User user = users.get(pid);

  		String wrapperName = (String) properties.get(ADMIN_WRAPPER);
  		String adminHost = (String) properties.get(ADMIN_HOST);
  		String adminPort = (String) properties.get(ADMIN_PORT);
  		String adminUser = (String) properties.get(ADMIN_USERNAME);
  		AdminItf wrapper = AdminWrapperHelper.getWrapper(bundleContext, new AdminStruct(wrapperName, adminHost, adminPort, adminUser));

  		if (user == null) {
  			String name = (String) properties.get(NAME);
  			if (!isSet(name)) 
  				name = "anonymous";

  			String password = (String) properties.get(PASSWORD);
  			if (!isSet(password)) 
  				password = "anonymous";

  			int sid = 0;
  			String serverId = (String) properties.get(SERVERID);
  			if (isSet(serverId)) {
  				sid = new Integer(serverId).intValue();
  			} else {
  				if (logmon.isLoggable(BasicLevel.DEBUG))
  					logmon.log(BasicLevel.DEBUG, "doUpdated serverId not set used 0");
  			}

  			String identityClassName = (String) properties.get(IDENTITYCLASS);
  			if (!isSet(identityClassName)) {
  				identityClassName = SimpleIdentity.class.getName();
  			}

  			Properties props = new Properties();
  			Enumeration keys = properties.keys();
  			while (keys.hasMoreElements()) {
  				String key = (String) keys.nextElement();
  				// TODO: remove unused properties
  				props.setProperty(key, (String) properties.get(key));
  			}
  			//create the user
  			user = wrapper.createUser(name, password, sid, identityClassName, props);
  			// add user to the users Map
  			users.put(pid, user);
  		}

  		String threshold = (String) properties.get(THRESHOLD);
  		String subName = (String) properties.get(SUBNAME);
  		if (isSet(threshold)) {
  			int th = new Integer(threshold).intValue();
  			if (isSet(subName))
  				user.setThreshold(subName, th);
  			else
  				user.setThreshold(th);
  		}

  		String nbMaxMsg = (String) properties.get(NB_MAXMSG);
  		if (isSet(subName) && isSet(nbMaxMsg))
  			user.setNbMaxMsg(subName, new Integer(nbMaxMsg).intValue());

  		// bind the user
  		String jndiName = (String) properties.get(JNDINAME);
  		if (isSet(jndiName)) {
  			jndiHelper.rebind(jndiName, user);
  			jndiNames.put(pid, jndiName);
  		}

  		// set the DMQ
  		String dmq = (String) properties.get(DMQ);
  		if (isSet(dmq)) {
  			String dmqSid = (String) properties.get(DMQ_SID);
  			if (!isSet(dmqSid))
  				dmqSid = ""+wrapper.getLocalServerId();
  			setUserDMQ(wrapper, user, dmq, new Integer(dmqSid).intValue());
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
	  
		if (users.containsKey(pid)) {
			User user = users.remove(pid);
			try {
				if (user != null)
					user.delete();
				String jndiName = jndiNames.remove(pid);
				if (jndiName != null)
					jndiHelper.unbind(jndiName);
      } catch (Exception e) {
      	if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN, "deleted " + user, e);
      }
		}
  }
}