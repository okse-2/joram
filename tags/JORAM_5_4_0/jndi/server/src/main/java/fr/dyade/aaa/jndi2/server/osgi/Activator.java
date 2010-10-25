/*
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.jndi2.server.osgi;

import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import fr.dyade.aaa.common.Service;
import fr.dyade.aaa.jndi2.distributed.DistributedJndiServer;
import fr.dyade.aaa.jndi2.ha.HADistributedJndiServer;
import fr.dyade.aaa.jndi2.ha.HAJndiServer;
import fr.dyade.aaa.jndi2.server.JndiServer;

public class Activator implements BundleActivator {
  
  private ServiceRegistration jndiServerRegistration;

  private ServiceRegistration distributedJndiServerRegistration;

  private ServiceRegistration haJndiServerRegistration;

  private ServiceRegistration haDistributedJndiServerRegistration;

  public void start(BundleContext context) throws Exception {
    Properties props = new Properties();
    props.put(Service.SERVICE_NAME_PROP, JndiServer.class.getName());
    jndiServerRegistration = context.registerService(Service.class.getName(), new Service(), props);
    
    props = new Properties();
    props.put(Service.SERVICE_NAME_PROP, DistributedJndiServer.class.getName());
    distributedJndiServerRegistration = context.registerService(Service.class.getName(), new Service(), props);
    
    props = new Properties();
    props.put(Service.SERVICE_NAME_PROP, HAJndiServer.class.getName());
    haJndiServerRegistration = context.registerService(Service.class.getName(), new Service(), props);
    
    props = new Properties();
    props.put(Service.SERVICE_NAME_PROP, HADistributedJndiServer.class.getName());
    haDistributedJndiServerRegistration = context.registerService(Service.class.getName(), new Service(), props);
  }

  public void stop(BundleContext context) throws Exception {
    jndiServerRegistration.unregister();
    distributedJndiServerRegistration.unregister();
    haJndiServerRegistration.unregister();
    haDistributedJndiServerRegistration.unregister();
  }

}
