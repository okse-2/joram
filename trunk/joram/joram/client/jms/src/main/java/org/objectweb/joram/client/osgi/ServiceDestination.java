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
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.jms.InvalidDestinationException;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminItf;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;

import fr.dyade.aaa.common.Debug;

/**
 *
 */
public abstract class ServiceDestination {
	public static final Logger logmon = Debug.getLogger(ServiceDestination.class.getName());
	
  //reserved words
	public static final String ADMIN_WRAPPER = "adminWrapper";
	public static final String ADMIN_HOST = "adminHost";
	public static final String ADMIN_PORT = "adminPort";
	public static final String ADMIN_USERNAME = "adminUser";
	public static final String NAME = "name";
	public static final String SERVERID = "serverId";
	public static final String TYPE = "type";
	public static final String FREEREADING = "freeReading";
	public static final String FREEWRITING = "freeWriting";
	public static final String READERS = "readers";
	public static final String WRITERS = "writers";
	public static final String THRESHOLD = "threshold";
	public static final String NB_MAXMSG = "nbMaxMsg";
	public static final String JNDINAME = "jndiName";
	public static final String DMQ = "dmq";
	public static final String DMQ_SID = "dmqSid";

	
  protected BundleContext bundleContext;
	private HashMap<String, Destination> destinations;
	private HashMap<String, String> jndiNames;
	private JndiHelper jndiHelper;

  public ServiceDestination(final BundleContext bundleContext) {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "ServiceDestination(" + bundleContext + ')');
    this.bundleContext = bundleContext;
    destinations = new HashMap<String, Destination>();
    jndiHelper = new JndiHelper();
    jndiNames = new HashMap<String, String>();
  }
  
  protected final boolean isSet(String value) {
    return value != null && value.length() > 0;
  }
  
  private Destination createDestination(AdminItf wrapper,
  		int serverId, 
  		String name, 
  		String className, 
  		Properties props,
  		boolean isQueue) throws ConnectException, AdminException {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG,
  				"createDestination(" + wrapper + ", "+ serverId + "," + name + "," + className + "," + props + "," + isQueue + ")");
  	if (isQueue) {
  		return wrapper.createQueue(serverId, name, className, props);
  	} else {
  		return wrapper.createTopic(serverId, name, className, props);
  	}
  }
  
  private User getUser(AdminItf wrapper, String user, int serverId) throws ConnectException, AdminException {
  	User[] users = wrapper.getUsers(serverId);
  	for (int i = 0; i < users.length; i++) {
  		if (users[i].getName().equals(user))
  			return users[i];
  	}
  	return null;
  }
  
  private void setRight(AdminItf wrapper,
  		Destination dest, 
  		int serverId, 
  		boolean freeReading, 
  		boolean freeWriting, 
  		String readers, 
  		String writers) throws ConnectException, AdminException {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "setRight(" + dest + ", " + serverId + ", " + freeReading + 
  				", " + freeWriting + ", " + readers + ", " + writers + ')');
  	if (freeReading)
      dest.setFreeReading();

    if (freeWriting)
      dest.setFreeWriting();

    if (isSet(readers)) {
    	StringTokenizer tk = new StringTokenizer(readers, " ");
    	while (tk.hasMoreElements()) {
    		String userName = (String) tk.nextElement();
	      User user = getUser(wrapper, userName, serverId);
	      if (user != null) {
	      	dest.setReader(user);
	      } else {
	      	if (logmon.isLoggable(BasicLevel.WARN))
	      		logmon.log(BasicLevel.WARN, "setRight: can't set reader's right for the user \"" + userName + "\", this user is unknown."); 
	      }
      }
    }

    if (isSet(writers)) {
    	StringTokenizer tk = new StringTokenizer(writers, " ");
    	while (tk.hasMoreElements()) {
    		String userName = (String) tk.nextElement();
    		User user = getUser(wrapper, userName, serverId);
    		if (user != null) {
    			dest.setWriter(user);
    		} else {
	      	if (logmon.isLoggable(BasicLevel.WARN))
	      		logmon.log(BasicLevel.WARN, "setRight: can't set writer's right for the user \"" + userName + "\", this user is unknown."); 
	      }
    	}
    }
  }
  
  private void setQueueThreshold(Queue queue, String thresholdStr, String nbMaxMsgStr) throws ConnectException, AdminException { 
  	if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "setQueueThreshold(" + queue + ", " + thresholdStr + ", " + nbMaxMsgStr + ')');

  	if (isSet(thresholdStr)) {
  		int threshold = new Integer(thresholdStr).intValue();
  		if (threshold > 0)
  			queue.setThreshold(threshold);
  	}

  	if (isSet(nbMaxMsgStr)) {
  		int nbMaxMsg = new Integer(nbMaxMsgStr).intValue();
  		if (nbMaxMsg > 0)
  			queue.setNbMaxMsg(nbMaxMsg);
  	}
  }

  private void setDestinationDMQ(AdminItf wrapper, Destination dest, String dmq, int dmqSid) throws ConnectException, AdminException {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "setDestinationDMQ(" + wrapper + ", " + dest + ", " + dmq + ", " +  + dmqSid +')');
  	Destination[] destinations = wrapper.getDestinations(dmqSid);
  	if (destinations != null) {
  	  if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, "setDestinationDMQ : destinations = " + Arrays.toString(destinations));
  		for (int i = 0; i < destinations.length; i++) {
  			if (destinations[i] instanceof Queue && (dmq.equals(destinations[i].getName()) || dmq.equals(destinations[i].getAdminName()))) {
  				try {
  					dest.setDMQ((Queue) destinations[i]);
  					if (logmon.isLoggable(BasicLevel.DEBUG))
  					  logmon.log(BasicLevel.DEBUG, "setDestinationDMQ : the dmq " + destinations[i] +" is set on " + dest);
  					return;
  				} catch (InvalidDestinationException e) {
  					if (logmon.isLoggable(BasicLevel.DEBUG))
  						logmon.log(BasicLevel.DEBUG, "EXCEPTION:: setDestinationDMQ", e);
  				}
  			}
  		}
  	}
  	if (logmon.isLoggable(BasicLevel.WARN))
  		logmon.log(BasicLevel.WARN, "setDestinationDMQ: the DMQ \"" + dmq + "\" on server \"" + dmqSid + "\" not found.");
  }

  /**
   * Create the destination 
   * 
   * @param pid the pid
   * @param properties the destination properties
   */
  public void doUpdated(String pid, Dictionary properties, String className, boolean isQueue) {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "doUpdated(" + pid + ", " + properties + ')');
  	
  	try {
  		String wrapperName = (String) properties.get(ADMIN_WRAPPER);
  		String adminHost = (String) properties.get(ADMIN_HOST);
  		String adminPort = (String) properties.get(ADMIN_PORT);
  		String adminUser = (String) properties.get(ADMIN_USERNAME);
  		AdminItf wrapper = AdminWrapperHelper.getWrapper(bundleContext, new AdminStruct(wrapperName, adminHost, adminPort, adminUser));
  		
  		int sid = 0;
  		String name = (String) properties.get(NAME);
  		String serverId = (String) properties.get(SERVERID);
  		
  		Properties props = new Properties();
  		Enumeration keys = properties.keys();
  		while (keys.hasMoreElements()) {
  			String key = (String) keys.nextElement();
  			// TODO: remove unused properties
  			props.setProperty(key, (String) properties.get(key));
  		}

  		if (isSet(serverId)) {
  			sid = new Integer(serverId).intValue();
  		} else {
  			if (logmon.isLoggable(BasicLevel.DEBUG))
  	  		logmon.log(BasicLevel.DEBUG, "doUpdated serverId not set used 0");
  		}

  		Destination dest = null;
  		if (!destinations.containsKey(pid)) {
  			// create destination
  			dest = createDestination(wrapper, sid, name, className, props, isQueue);
  		} else {
  			dest = destinations.get(pid);
  			dest.setProperties(props);
  		}

  		if (dest == null) {
  			if (logmon.isLoggable(BasicLevel.WARN))
  	  		logmon.log(BasicLevel.WARN, "updated destination is null.");
  			throw new ConfigurationException(null, "doUpdated: destination is null.");
  		}
  		
  		//set right
  		boolean freeReading = new Boolean((String) properties.get(FREEREADING)).booleanValue();
  		boolean freeWriting = new Boolean((String) properties.get(FREEWRITING)).booleanValue();
  		String readers = (String) properties.get(READERS);
  		String writers = (String) properties.get(WRITERS);
  		setRight(wrapper, dest, sid, freeReading, freeWriting, readers, writers);

  		if (dest instanceof Queue) {
  			String threshold = (String) properties.get(THRESHOLD);
  			String nbMaxMsg = (String) properties.get(NB_MAXMSG);
  			setQueueThreshold((Queue) dest, threshold, nbMaxMsg);
  		}

  		// bind the destination
  		String jndiName = (String) properties.get(JNDINAME);
  		if (isSet(jndiName)) {
  			jndiHelper.rebind(jndiName, dest);
  			jndiNames.put(pid, jndiName);
  		}

  		// set the DMQ
  		String dmq = (String) properties.get(DMQ);
  		if (isSet(dmq)) {
  			String dmqSid = (String) properties.get(DMQ_SID);
  			if (!isSet(dmqSid))
  				dmqSid = ""+wrapper.getLocalServerId();
  			setDestinationDMQ(wrapper, dest, dmq, new Integer(dmqSid).intValue());
  		}

  		destinations.put(pid, dest);
  	} catch (Exception e) {
  		if (logmon.isLoggable(BasicLevel.ERROR))
	  		logmon.log(BasicLevel.ERROR, "doUpdated ", e);
  	}
  }

	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
	 */
	public void deleted(String pid) {
		if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "deleted(" + pid + ')');
	  
		if (destinations.containsKey(pid)) {
			Destination dest = destinations.remove(pid);
			try {
				if (dest != null)
					dest.delete();
				String jndiName = jndiNames.remove(pid);
				if (jndiName != null)
					jndiHelper.unbind(jndiName);
      } catch (Exception e) {
      	if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN, "deleted " + dest, e);
      }
		}
  }
}