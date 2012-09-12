/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2012 ScalAgent Distributed Technologies
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
package org.ow2.joram.admin;

import org.objectweb.joram.mom.dest.QueueMBean;
import org.objectweb.joram.mom.dest.TopicMBean;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.ConnectionManager;
import org.objectweb.joram.mom.proxies.UserAgentMBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import fr.dyade.aaa.agent.EngineMBean;
import fr.dyade.aaa.agent.NetworkMBean;

public class JoramAdminOSGi extends JoramAdmin implements ServiceTrackerCustomizer {

  private static Filter filter;

  private ServiceTracker serviceTracker;

  private BundleContext context;

  static {
    try {
      filter = FrameworkUtil.createFilter("(|(" + Constants.OBJECTCLASS + "=" + QueueMBean.class.getName()
          + ")(" + Constants.OBJECTCLASS + "=" + TopicMBean.class.getName()
          + ")(" + Constants.OBJECTCLASS + "=" + ClientSubscriptionMBean.class.getName()
          + ")(" + Constants.OBJECTCLASS + "=" + UserAgentMBean.class.getName()
          + ")(" + Constants.OBJECTCLASS + "=" + NetworkMBean.class.getName()
          + ")(" + Constants.OBJECTCLASS + "=" + EngineMBean.class.getName()
          + "))");
    } catch (InvalidSyntaxException exc) {
      exc.printStackTrace();
    }
  }

  public JoramAdminOSGi(BundleContext context) {
    this.context = context;
  }

  public boolean connect(String login, String password) throws Exception {
    return ConnectionManager.getCurrentInstance().checkCredentials(login, password);
  }

  public void start() {
    serviceTracker = new ServiceTracker(context, filter, this);
    serviceTracker.open();
  }

  public void stop() {
    serviceTracker.close();
  }

  public void disconnect() {
  }

  public Object addingService(ServiceReference reference) {
    Object service = context.getService(reference);
    super.handleAdminObjectAdded(service);
    return service;
  }

  public void removedService(ServiceReference reference, Object service) {
    super.handleAdminObjectRemoved(service);
  }

  public void modifiedService(ServiceReference arg0, Object arg1) {
  }

}
