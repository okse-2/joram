/*
 * Copyright (C) 2001 - 2011 ScalAgent Distributed Technologies
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

import java.util.List;
import java.util.Set;

public final class MXWrapper {
  /**
   * Name of the property allowing the use of the A3 runtime without the JMX management
   * framework. The additional property MXServer allows to configure the name of the
   * implementation class of the MXServer interface (by default com.scalagent.jmx.JMXServer).
   */
  public final static String NO_JMX = "JoramNoJMX";

  private static MXServer mxserver = null;

  static {
    // Initializes the JMX Wrapper
    if (! Boolean.getBoolean(NO_JMX)) {
      try {
        mxserver = (MXServer) Class.forName(System.getProperty("MXServer", "com.scalagent.jmx.JMXServer")).newInstance();
      } catch (Exception exc) {
        mxserver= null;
      }
    }
  }

  public static String objectName(String domain, String name) {
    return new StringBuffer().append(domain).append(':').append(name).toString();
  }

  public static void registerMBean(Object bean, String domain, String name) throws Exception {
    registerMBean(bean, objectName(domain, name));
  }

  public static void registerMBean(Object bean, String fullName) throws Exception {
    if (mxserver != null)
      mxserver.registerMBean(bean, fullName);
  }

  public static void unregisterMBean(String domain, String name) throws Exception {
    unregisterMBean(objectName(domain, name));
  }

  public static void unregisterMBean(String fullName) throws Exception {
    if (mxserver != null)
      mxserver.unregisterMBean(fullName);
  }

  public static void setMXServer(MXServer server) {
    mxserver = server;
  }

  public static Object getAttribute(String objectName, String attribute) throws Exception {
    if (mxserver != null)
      return mxserver.getAttribute(objectName, attribute);
    return null;
  }

  public static Set<String> queryNames(String objectName) throws Exception {
    if (mxserver != null)
      return mxserver.queryNames(objectName);
    return null;
  }

  public static List<String> getAttributeNames(String mBean) throws Exception {
    if (mxserver != null)
      return mxserver.getAttributeNames(mBean);
    return null;
  }
}
