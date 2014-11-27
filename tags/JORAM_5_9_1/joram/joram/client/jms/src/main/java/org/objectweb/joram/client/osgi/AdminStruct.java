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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
 

public class AdminStruct {
	public static final Logger logmon = Debug.getLogger(AdminStruct.class.getName());
	public String wrapperName;
	public String adminHost;
	public String adminPort;
	public String adminUser;

	/**
	 * 
	 * @param wrapperName the admin wrapper name
	 * @param adminHost   the admin host name
	 * @param adminPort   the admin port
	 * @param adminUser   the admin user name
	 */
	public AdminStruct(String wrapperName, String adminHost, String adminPort,
	    String adminUser) {
		if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "AdminStruct<" + wrapperName + ", " + adminHost + ", " + adminPort + ", " + adminUser + '>');
		this.wrapperName = wrapperName;
		this.adminHost = adminHost;
		this.adminPort = adminPort;
		this.adminUser = adminUser;
	}

	
	/* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
	  return "AdminStruct [wrapperName=" + wrapperName + ", adminHost="
	      + adminHost + ", adminPort=" + adminPort + ", adminUser=" + adminUser
	      + "]";
  }

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((adminHost == null) ? 0 : adminHost.hashCode());
		result = prime * result + ((adminPort == null) ? 0 : adminPort.hashCode());
		result = prime * result + ((adminUser == null) ? 0 : adminUser.hashCode());
		result = prime * result + ((wrapperName == null) ? 0 : wrapperName.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdminStruct other = (AdminStruct) obj;

		if (wrapperName != null) {
			if (wrapperName.equals(other.wrapperName))
				return true;
			else if (other.wrapperName != null)
				return false;
		}
		if (adminHost == null) {
			if (other.adminHost != null)
				return false;
		} else if (!adminHost.equals(other.adminHost))
			return false;
		if (adminPort == null) {
			if (other.adminPort != null)
				return false;
		} else if (!adminPort.equals(other.adminPort))
			return false;
		// if (adminUser == null) {
		// if (other.adminUser != null)
		// return false;
		// } else if (!adminUser.equals(other.adminUser))
		// return false;
		return true;
	}
}