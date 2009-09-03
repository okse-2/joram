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
package com.scalagent.jmx;

import java.io.*;
import java.util.*;

import com.sun.jdmk.comm.*;
import javax.management.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.objectweb.util.monolog.api.LoggerFactory;

import fr.dyade.aaa.util.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * A <code>JMXRIHttpService</code> service provides a JMX server throught the
 * Sun RI. An HTTP adaptor is launch to access management functions in running
 * agent servers.
 * <p>
 * The <code>JMXRIHttpService</code> service needs an argument: the TCP port.
 * By default port 8082 is used.
 */
public class JMXRIHttpService {
  static HtmlAdaptorServer adapterServer = null;

  /**
   * Initializes the service.
   *
   * @param args	parameters from the configuration file
   * @param firstTime	<code>true</code> when service starts anew
   */
  public static void init(String args,
                          boolean firstTime) throws Exception {
    int port = 8082;
    if (args != null && args.length()!=0) {
      try {
	port = Integer.parseInt(args);
      } catch (NumberFormatException exc) {}
    }

    try {
      adapterServer = new HtmlAdaptorServer();
      adapterServer.setPort(port);
      MXWrapper.registerMBean(adapterServer,
                              "JMXRIHttpService",
                              "name=htmladapter,port=" + port);

      startService();
    } catch (Exception exc) {
      Debug.getLogger("com.scalagent.jmx").log(
        BasicLevel.ERROR, "JMXRIService initialization failed", exc);
      throw exc;
    }
  }
 
  public static void startService() {
    Debug.getLogger("com.scalagent.jmx").log(BasicLevel.DEBUG,
                                             "JMXRIHttpService.startService");

    adapterServer.start();
  }

  public static void stopService() {
    Debug.getLogger("com.scalagent.jmx").log(BasicLevel.DEBUG,
                                             "JMXRIHttpService.stopService");

    adapterServer.stop();
  }

}
