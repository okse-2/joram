/*
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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

import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

public final class MXWrapper {
  /**
   *  Name of the property that allow to configure the JMX server proxy: it
   * gives the name of the implementation class of the MXServer interface.
   * If the property is not defined JMX is not used.
   */
  public final static String ServerImpl = "MXServer";

  public static MXServer mxserver = null;
  
  public static void init() throws Exception {
    if (mxserver != null)
      return;
    String mxname = System.getProperty(ServerImpl);
    if ((mxname != null) && (mxname.length() > 0))
      Class.forName(mxname).newInstance();
  }
  
  public static String objectName(String domain, String name) {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append(domain).append(':').append(name);
    return strbuf.toString();
  }

  public static String  registerMBean(Object bean, String domain, String name) throws Exception {
    return registerMBean(bean, objectName(domain, name));
  }
  
  public static String registerMBean(Object bean, String fullName) throws Exception {
    if (mxserver == null)
      return null;
    return mxserver.registerMBean(bean, fullName);
  }

  public static void unregisterMBean(String domain, String name) throws Exception {
    unregisterMBean(objectName(domain, name));
  }

  public static void unregisterMBean(String fullName) throws Exception {
    if (mxserver == null)
      return;
    mxserver.unregisterMBean(fullName);
  }

  public static void setMXServer(MXServer server) {
    mxserver = server;
  }

  public static MXServer getMXServer() {
    return mxserver;
  }
  
  public static Object getAttribute(ObjectName objectName, String attribute) throws Exception {
    if (mxserver == null) {
      return null;
    }
    return mxserver.getAttribute(objectName, attribute);
  }
  
  public static MBeanAttributeInfo[] getAttributes(ObjectName objectName) throws Exception {
    if (mxserver == null) {
      return null;
    }
    return mxserver.getAttributes(objectName);
  }
  
  public static Set queryNames(ObjectName objectName) {
    if (mxserver == null) {
      return null;
    }
    return mxserver.queryNames(objectName);
  }
  
}
