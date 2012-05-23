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

import org.objectweb.joram.client.jms.admin.AdminItf;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

import fr.dyade.aaa.common.Debug;

public class AdminWrapperTracker {
	public static final Logger logmon = Debug.getLogger(AdminWrapperTracker.class.getName());
	public final static String NAME = "name";
	public final static String HOST = "host";
	public final static String PORT = "port";
	public final static String USER = "user";
	
	private ServiceTracker serviceTracker = null;
	private Filter filter;
	
	public AdminWrapperTracker(final BundleContext bundleContext, String name, String host, String port, String user) throws InvalidSyntaxException {
		StringBuffer buff = new StringBuffer();
		buff.append("(&");
		buff.append("(").append(Constants.OBJECTCLASS).append("=").append(AdminItf.class.getName()).append(")");
		if (name != null)
			buff.append("(").append(NAME).append("=").append(name).append(")");
		if (host != null)
			buff.append("(").append(HOST).append("=").append(host).append(")");
		if (port != null)
			buff.append("(").append(PORT).append("=").append(port).append(")");
		if (user != null)
			buff.append("(").append(USER).append("=").append(user).append(")");
		buff.append(")");
		filter = bundleContext.createFilter(buff.toString());
		if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "AdminWrapperTracker: filter = " + filter);
		serviceTracker = new ServiceTracker(bundleContext, filter, null);
		// open the service tracker
		serviceTracker.open();
  }
	
	public AdminItf getAdminWrapper() {
	  AdminItf wrapper = null;
    try {
	    wrapper = (AdminItf) serviceTracker.waitForService(30000);
    } catch (InterruptedException e) {
    	if (logmon.isLoggable(BasicLevel.DEBUG))
    		logmon.log(BasicLevel.DEBUG, "AdminWrapperTracker.getAdminWrapper", e);    	
    }
		if (wrapper == null) {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "AdminWrapperTracker.getAdminWrapper:: the wrapper is null.");    	
			throw new java.lang.NullPointerException("AddminWrapper no matching for this filter " + filter);
		}
		return wrapper;
	}
	
	public void close() {
		serviceTracker.close();
	}
}
