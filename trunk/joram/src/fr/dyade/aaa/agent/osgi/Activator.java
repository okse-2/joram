/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009-2010 ScalAgent Distributed Technologies
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

import java.util.Properties;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;

import fr.dyade.aaa.agent.AdminProxy;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.Service;

public class Activator implements BundleActivator {

  public static final Logger logmon = Debug.getLogger(Activator.class.getName());

  public static final String AGENT_SERVER_ID_PROPERTY = "fr.dyade.aaa.agent.AgentServer.id";

  public static final String AGENT_SERVER_CLUSTERID_PROPERTY = "fr.dyade.aaa.agent.AgentServer.clusterid";

  public static final String AGENT_SERVER_STORAGE_PROPERTY = "fr.dyade.aaa.agent.AgentServer.storage";

  public static BundleContext context;

  private ServiceRegistration adminproxyRegistration;

  public static void stopFramework() {
    Bundle[] bundles = Activator.context.getBundles();
    for (int i = 0; i < bundles.length; i++) {
      Bundle bundle = bundles[i];
      if (bundle.getHeaders().get("Bundle-SymbolicName").equals("org.apache.felix.framework")) {
        if (Activator.context.getBundle().getState() != Bundle.STOPPING) {
          try {
            bundle.stop();
          } catch (BundleException exc) {
            if (logmon.isLoggable(BasicLevel.ERROR))
              logmon.log(BasicLevel.ERROR, "Error when stopping OSGi framework : ", exc);
          }
          return;
        }
      }
    }
  }

  public void start(BundleContext context) throws Exception {
    Activator.context = context;
    AgentServer.isOSGi = true;

    short sid = getShortProperty(AGENT_SERVER_ID_PROPERTY, (short) 0);
    String path = getProperty(AGENT_SERVER_STORAGE_PROPERTY, "s" + sid);
    short cid = getShortProperty(AGENT_SERVER_CLUSTERID_PROPERTY, AgentServer.NULL_ID);

    Properties props = new Properties();
    props.put(Service.SERVICE_NAME_PROP, AdminProxy.class.getName());
    adminproxyRegistration = context.registerService(Service.class.getName(), new Service(), props);

    try {
      AgentServer.init(sid, path, null, cid);
      AgentServer.start();
    } catch (RuntimeException exc) {
      logmon.log(BasicLevel.ERROR, "Error when starting AgentServer: ", exc);
    }
  }

  public void stop(BundleContext context) throws Exception {
    AgentServer.stop();
    AgentServer.reset();

    adminproxyRegistration.unregister();
    Activator.context = null;
  }

  private short getShortProperty(String propName, short defaultValue) {
    String propValue = context.getProperty(propName);
    if (propValue != null) {
      return Short.parseShort(propValue);
    }
    return defaultValue;
  }

  private String getProperty(String propName, String defaultValue) {
    String propValue = context.getProperty(propName);
    if (propValue != null) {
      return propValue;
    }
    return defaultValue;
  }

}
