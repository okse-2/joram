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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;

public class Activator implements BundleActivator {

  public static final Logger logmon = Debug.getLogger(Activator.class.getName());

  public static final String AGENT_SERVER_ID_PROPERTY = "fr.dyade.aaa.agent.AgentServer.id";

  public static final String AGENT_SERVER_CLUSTERID_PROPERTY = "fr.dyade.aaa.agent.AgentServer.clusterid";

  public static final String AGENT_SERVER_STORAGE_PROPERTY = "fr.dyade.aaa.agent.AgentServer.storage";

  public static BundleContext context;

  public void start(BundleContext context) throws Exception {
    Activator.context = context;

    short sid = getShortProperty(AGENT_SERVER_ID_PROPERTY, (short) 0);
    String path = getProperty(AGENT_SERVER_STORAGE_PROPERTY, "s" + sid);
    short cid = getShortProperty(AGENT_SERVER_CLUSTERID_PROPERTY, AgentServer.NULL_ID);

    try {
      AgentServer.init(sid, path, null, cid);
    } catch (Exception exc) {
      System.out.println(AgentServer.getName() + "initialization failed: " + AgentServer.ERRORSTRING);
      System.out.println(exc.toString());
      System.out.println(AgentServer.ENDSTRING);
      logmon.log(BasicLevel.ERROR, AgentServer.getName() + " initialization failed", exc);
      throw exc;
    }

    try {
      String errStr = AgentServer.start();
      if (errStr == null) {
        System.out.println(AgentServer.getName() + " started: " + AgentServer.OKSTRING);
      } else {
        System.out.println(AgentServer.getName() + " started: " + AgentServer.ERRORSTRING);
        System.out.print(errStr);
        System.out.println(AgentServer.ENDSTRING);
      }
    } catch (Exception exc) {
      System.out.println(AgentServer.getName() + " start failed: " + AgentServer.ERRORSTRING);
      System.out.print(exc.toString());
      System.out.println(AgentServer.ENDSTRING);
      logmon.log(BasicLevel.ERROR, AgentServer.getName() + " failed", exc);
      throw exc;
    }
  }

  public void stop(BundleContext context) throws Exception {
    AgentServer.stop();
    AgentServer.reset();
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
