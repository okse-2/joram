/*
 * Copyright (C) 2009 - 2012 ScalAgent Distributed Technologies
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

import org.objectweb.joram.client.jms.admin.JoramAdminConnect;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


/**
 * The activator for Destination, User and ConnectionFactory ConfigAdmins.
 * 
 */
public class Activator implements BundleActivator {
  
  private JoramAdminConnect joramAdminConnect;
  private DestinationMSF destMSF = null;
  private UserMSF userMSF = null;
  private ConnectionFactoryMSF cfMSF = null;
  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
  	joramAdminConnect = new JoramAdminConnect();
  	joramAdminConnect.registerMBean();

  	// register *MSF 
  	destMSF = new DestinationMSF(context);
  	userMSF = new UserMSF(context);
  	cfMSF = new ConnectionFactoryMSF(context);
  }

  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    joramAdminConnect.unregisterMBean();
    destMSF.doStop();
    userMSF.doStop();
    cfMSF.doStop();
  }

}
