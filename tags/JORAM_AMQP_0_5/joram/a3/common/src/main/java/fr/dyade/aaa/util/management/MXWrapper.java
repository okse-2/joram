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

import com.scalagent.jmx.JMXServer;

public final class MXWrapper {

  public final static String NO_JMX = "JoramNoJMX";

  private static JMXServer mxserver = null;

  private static boolean firstTime = true;

  private static void init() {
    if (firstTime) {
      firstTime = false;
      // Initializes the JMX Wrapper
      boolean noJmx = Boolean.getBoolean(NO_JMX);
      if (!noJmx) {
        mxserver = new JMXServer();
      }
    }
  }

  public static String objectName(String domain, String name) {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append(domain).append(':').append(name);
    return strbuf.toString();
  }

  public static void registerMBean(Object bean, String domain, String name) throws Exception {
    registerMBean(bean, objectName(domain, name));
  }

  public static void registerMBean(Object bean, String fullName) throws Exception {
    init();
    if (mxserver != null)
      mxserver.registerMBean(bean, fullName);
  }

  public static void unregisterMBean(String domain, String name) throws Exception {
    unregisterMBean(objectName(domain, name));
  }

  public static void unregisterMBean(String fullName) throws Exception {
    init();
    if (mxserver != null)
      mxserver.unregisterMBean(fullName);
  }

  public static void setMXServer(JMXServer server) {
    mxserver = server;
  }

  public static JMXServer getMXServer() {
    return mxserver;
  }

  public static Object getAttribute(String objectName, String attribute) throws Exception {
    init();
    if (mxserver == null) {
      return null;
    }
    return mxserver.getAttribute(objectName, attribute);
  }

  public static Set queryNames(String objectName) throws Exception {
    init();
    if (mxserver == null) {
      return null;
    }
    return mxserver.queryNames(objectName);
  }

  public static List getAttributeNames(String mBean) throws Exception {
    init();
    if (mxserver == null) {
      return null;
    }
    return mxserver.getAttributeNames(mBean);
  }

}
