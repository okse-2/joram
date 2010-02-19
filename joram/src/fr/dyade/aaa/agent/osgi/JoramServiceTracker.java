/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2010 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent.osgi;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import fr.dyade.aaa.agent.ServiceDesc;
import fr.dyade.aaa.agent.ServiceManager;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.Service;

public class JoramServiceTracker extends ServiceTracker {

  public static final Logger logmon = Debug.getLogger(Activator.class.getName());

  private ServiceDesc serviceDesc;

  public JoramServiceTracker(ServiceDesc desc) throws Exception {
    super(Activator.context, Activator.context.createFilter('(' + Service.SERVICE_NAME_PROP + '='
        + desc.getClassName() + ')'), null);
    serviceDesc = desc;
  }

  public Object addingService(ServiceReference reference) {
    Object service = context.getService(reference);
    try {
      ServiceManager.doStart(serviceDesc);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "Error starting service. ", exc);
    }
    return service;
  }

  public void removedService(ServiceReference reference, Object service) {
    try {
      ServiceManager.stop(serviceDesc);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "Error stopping service.", exc);
    }
    super.removedService(reference, service);
  }
}