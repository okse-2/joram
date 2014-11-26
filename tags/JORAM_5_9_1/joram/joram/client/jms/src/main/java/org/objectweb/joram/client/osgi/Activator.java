/*
 * Copyright (C) 2009 - 2013 ScalAgent Distributed Technologies
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
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import fr.dyade.aaa.common.Debug;


/**
 * The activator for Destination, User and ConnectionFactory ConfigAdmins.
 * 
 */
public class Activator implements BundleActivator {
  
  public static final Logger logmon = Debug.getLogger(Activator.class.getName());
  
  private JoramAdminConnect joramAdminConnect;
  private DestinationMSF destMSF = null;
  private UserMSF userMSF = null;
  private ConnectionFactoryMSF cfMSF = null;
  
  private ServiceConnectionFactory scf = null;
  private ServiceQueue sq = null;
  private ServiceTopic st = null;
  private ServiceUser su = null;
  private ServiceAcquisitionQueue saq = null;
  private ServiceAcquisitionTopic sat = null;
  private ServiceDistributionQueue sdq = null;
  private ServiceDistributionTopic sdt = null;
  private ServiceSchedulerQueue ssq = null;
  private ServiceFtpQueue sfq = null;
  private ServiceAliasQueue salq = null;
  private ServiceAdmin sadmin = null;
  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "Activator.start(" + context + ')');
    
  	joramAdminConnect = new JoramAdminConnect();
  	joramAdminConnect.registerMBean();

  	// register *MSF 
  	destMSF = new DestinationMSF(context);
  	userMSF = new UserMSF(context);
  	cfMSF = new ConnectionFactoryMSF(context);
  	
  	// register Service
  	scf = new ServiceConnectionFactory(context);
  	sq = new ServiceQueue(context);
  	st = new ServiceTopic(context);
  	su = new ServiceUser(context);
  	saq = new ServiceAcquisitionQueue(context);
  	sat = new ServiceAcquisitionTopic(context);
  	sdq = new ServiceDistributionQueue(context);
  	sdt = new ServiceDistributionTopic(context);
  	ssq = new ServiceSchedulerQueue(context);
  	sfq = new ServiceFtpQueue(context);
  	salq = new ServiceAliasQueue(context);
  	sadmin = new ServiceAdmin(context);
  }

  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "Activator.stop(" + context + ')');
    
    joramAdminConnect.unregisterMBean();
    destMSF.doStop();
    userMSF.doStop();
    cfMSF.doStop();
    
    scf.doStop();
    sq.doStop();
    st.doStop();
    su.doStop();
    saq.doStop();
    sat.doStop();
    sdq.doStop();
    sdt.doStop();
    ssq.doStop();
    sfq.doStop();
    salq.doStop();
    sadmin.doStop();
  }

}
