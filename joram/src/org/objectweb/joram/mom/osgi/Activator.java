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
package org.objectweb.joram.mom.osgi;

import java.util.Properties;

import org.objectweb.joram.mom.dest.MonitoringQueue;
import org.objectweb.joram.mom.dest.MonitoringTopic;
import org.objectweb.joram.mom.proxies.ConnectionManager;
import org.objectweb.joram.mom.proxies.tcp.SSLTcpProxyService;
import org.objectweb.joram.mom.proxies.tcp.TcpProxyService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import fr.dyade.aaa.common.Service;

public class Activator implements BundleActivator {
  
  private ServiceRegistration connectionManagerRegistration;

  private ServiceRegistration tcpProxyServiceRegistration;

  private ServiceRegistration sslTcpProxyServiceRegistration;

  private ServiceRegistration monitoringTopicRegistration;

  private ServiceRegistration monitoringQueueRegistration;

  public void start(BundleContext context) throws Exception {
    Properties props = new Properties();
    props.put(Service.SERVICE_NAME_PROP, ConnectionManager.class.getName());
    connectionManagerRegistration = context.registerService(Service.class.getName(), new Service(), props);

    props.put(Service.SERVICE_NAME_PROP, TcpProxyService.class.getName());
    tcpProxyServiceRegistration = context.registerService(Service.class.getName(), new Service(), props);

    props.put(Service.SERVICE_NAME_PROP, SSLTcpProxyService.class.getName());
    sslTcpProxyServiceRegistration = context.registerService(Service.class.getName(), new Service(), props);

    props.put(Service.SERVICE_NAME_PROP, MonitoringTopic.class.getName());
    monitoringTopicRegistration = context.registerService(Service.class.getName(), new Service(), props);

    props.put(Service.SERVICE_NAME_PROP, MonitoringQueue.class.getName());
    monitoringQueueRegistration = context.registerService(Service.class.getName(), new Service(), props);
  }

  public void stop(BundleContext context) throws Exception {
    connectionManagerRegistration.unregister();
    tcpProxyServiceRegistration.unregister();
    sslTcpProxyServiceRegistration.unregister();
    monitoringTopicRegistration.unregister();
    monitoringQueueRegistration.unregister();
  }

}
