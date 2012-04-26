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

import java.util.HashMap;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminItf;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

import fr.dyade.aaa.common.Debug;

/**
 * 
 */
public class AdminWrapperHelper {
	public static final Logger logmon = Debug.getLogger(AdminWrapperHelper.class.getName());
	private static HashMap<AdminStruct, AdminWrapperTracker> wrapperTrackers = new HashMap<AdminStruct, AdminWrapperTracker>();
  
  public static AdminItf getWrapper(BundleContext bundleContext, AdminStruct adminStruct) throws AdminException {
  	if (adminStruct == null) 
  		throw new AdminException("The admin struct is null.");
  	
  	if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "getWrapper(" + adminStruct.wrapperName + ", " + adminStruct.adminHost + ", " + adminStruct.adminPort + ", " + adminStruct.adminUser + ')');
  	AdminWrapperTracker wrapperTracker = wrapperTrackers.get(adminStruct);
  	if (wrapperTracker == null) {
  		try {
	      wrapperTracker = new AdminWrapperTracker(bundleContext, adminStruct.wrapperName, adminStruct.adminHost, adminStruct.adminPort, adminStruct.adminUser);
      } catch (InvalidSyntaxException e) {
      	if (logmon.isLoggable(BasicLevel.WARN))
  	  		logmon.log(BasicLevel.WARN, "getWrapper ", e);
      	throw new AdminException("EXCEPTION:: getWrapper: " + e.getMessage());
      }
      
			wrapperTrackers.put(adminStruct, wrapperTracker);
			if (logmon.isLoggable(BasicLevel.DEBUG))
	  		logmon.log(BasicLevel.DEBUG, "getWrapper wrapperTrackers = " + wrapperTrackers);
		}
  	
  	// return the wrapper
  	try  {
  		return wrapperTracker.getAdminWrapper();
  	} catch (Exception e) {
  		wrapperTrackers.remove(adminStruct);
  		if (logmon.isLoggable(BasicLevel.WARN))
	  		logmon.log(BasicLevel.WARN, "getWrapper ", e);
  		throw new AdminException("The admin wrapper tracker not available.");
		}
  }
  
  public void removeWrapperTracker(AdminStruct adminStruct) {
  	AdminWrapperTracker wrapperTracker = wrapperTrackers.remove(adminStruct);
  	if (wrapperTracker != null) {
  		wrapperTracker.close();
  	}
  }
}