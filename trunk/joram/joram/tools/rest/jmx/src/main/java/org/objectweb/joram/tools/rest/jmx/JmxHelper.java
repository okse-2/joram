/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2016 ScalAgent Distributed Technologies
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
package org.objectweb.joram.tools.rest.jmx;

import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleContext;

import fr.dyade.aaa.common.Debug;

public class JmxHelper {

  public static final String BUNDLE_REST_JMX_ROOT = "rest.jmx.root";
  public static final String BUNDLE_REST_JMX_PASS = "rest.jmx.password";
  
  public static Logger logger = Debug.getLogger(JmxHelper.class.getName());
  private static JmxHelper helper = null;
  private String restJmxRoot;
  private String restJmxPass;

  private JmxHelper() { }
  
  static public JmxHelper getInstance() {
    if (helper == null)
      helper = new JmxHelper();
    return helper;
  }
  
  /**
   * @return the restJmxRoot
   */
  public String getRestJmxRoot() {
    return restJmxRoot;
  }

  /**
   * @return the restJmxPass
   */
  public String getRestJmxPass() {
    return restJmxPass;
  }

  public boolean authenticationRequired() {
    return restJmxRoot != null && !restJmxRoot.isEmpty() &&
        restJmxPass != null && !restJmxPass.isEmpty();
  }
 
  public void init(BundleContext bundleContext) throws Exception {
    restJmxRoot = bundleContext.getProperty(BUNDLE_REST_JMX_ROOT);
    restJmxPass = bundleContext.getProperty(BUNDLE_REST_JMX_PASS);
  }
}
