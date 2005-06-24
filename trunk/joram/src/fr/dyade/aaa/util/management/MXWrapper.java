/*
 * Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.util.management;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.objectweb.util.monolog.api.LoggerFactory;

import fr.dyade.aaa.util.Debug;

public final class MXWrapper {
  /**
   *  Name of the property that allow to configure the JMX server proxy: it
   * gives the name of the implementation class of the MXServer interface.
   * If the property is not defined JMX is not used.
   */
  public final static String ServerImpl = "MXServer";

  public static MXServer mxserver = null;

  public static void init() {
    if (mxserver != null) return;

    String mxname = System.getProperty(ServerImpl);

    // Be careful, do not call Debug.getLogger before initializing the
    // MXServer (see Debug.init).

    try {
      if ((mxname != null) && (mxname.length() > 0))
        Class.forName(mxname).newInstance();
    } catch (Exception exc) {
      Debug.getLogger("fr.dyade.aaa.util.management").log(
        BasicLevel.ERROR, "can't instantiate MXServer: " + mxname, exc);
    }

    Debug.getLogger("fr.dyade.aaa.util.management").log(
      BasicLevel.INFO, "MXWrapper.ServerImpl -> " + mxname);
  }

  public static void registerMBean(Object bean,
                                   String domain,
                                   String name) throws Exception {
    if (mxserver == null) return;

    Debug.getLogger("fr.dyade.aaa.util.management").log(
      BasicLevel.WARN, "registerMBean: " + name + " -> " + mxserver);

    mxserver.registerMBean(bean, domain, name);
  }

  public static void unregisterMBean(String domain, 
                                     String name) throws Exception {
    if (mxserver == null) return;

    Debug.getLogger("fr.dyade.aaa.util.management").log(
      BasicLevel.WARN, "unregisterMBean: " + name + " -> " + mxserver);

    mxserver.unregisterMBean(domain, name);
  }

  public static void setMXServer(MXServer server) {
    Debug.getLogger("fr.dyade.aaa.util.management").log(
      BasicLevel.INFO, "setMXServer: " + server);

    mxserver = server;
  }

  public static MXServer getMXServer() {
    return mxserver;
  }
}
