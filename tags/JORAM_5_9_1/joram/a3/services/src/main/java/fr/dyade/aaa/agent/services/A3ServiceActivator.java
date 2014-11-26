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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import fr.dyade.aaa.common.Debug;

/**
 * Activator for the a3 service.
 * need to start/stop agent server with a ConfigAdmin file.
 *
 */
public class A3ServiceActivator implements BundleActivator { 
	public static final Logger logmon = Debug.getLogger(A3ServiceActivator.class.getName());

  private A3ManagedService a3ms = null;
  private JoramManagedService jms = null;
  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(final BundleContext bundleContext) throws Exception {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "start(" + bundleContext + ')');
    a3ms = new A3ManagedService(bundleContext);
    jms = new JoramManagedService(bundleContext);
  }
  
  /* (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext bundleContext) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "stop(" + bundleContext + ')');
    a3ms.doStop();
    jms.doStop();
  }
}