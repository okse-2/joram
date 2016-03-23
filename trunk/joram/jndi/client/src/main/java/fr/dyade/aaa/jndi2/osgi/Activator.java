/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 - 2016 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.jndi2.osgi;

import java.util.Dictionary;
import java.util.Properties;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.jndi2.scn.scnURLContextFactory;

public class Activator implements BundleActivator {

  public static final Logger logmon = Debug.getLogger(Activator.class.getName());

  public static BundleContext context;
  public ServiceRegistration registration;

  public void start(BundleContext context) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "start(" + context + ')');
    Activator.context = context;

    // The JNDI_URLSCHEME is defined in the org.osgi.enterprise bundle
    // (org.osgi.service.jndi.JNDIConstants.JNDI_URLSCHEME = "osgi.jndi.url.scheme")
    // and we just need the JNDI_URLSCHEME constant in this bundle.
    String JNDI_URLSCHEME = "osgi.jndi.url.scheme";
    Properties props = new Properties();
    props.setProperty(JNDI_URLSCHEME, "scn");
    registration = context.registerService(
        javax.naming.spi.ObjectFactory.class.getName(),
        new scnURLContextFactory(),
        (Dictionary)props);
  }

  public void stop(BundleContext context) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "stop(" + context + ')');

    if (registration != null)
      registration.unregister();
    Activator.context = null;
  }
}
