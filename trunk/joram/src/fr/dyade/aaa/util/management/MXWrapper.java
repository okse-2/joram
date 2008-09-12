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

import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

public final class MXWrapper {
  /**
   *  Name of the property that allow to configure the JMX server proxy: it
   * gives the name of the implementation class of the MXServer interface.
   * If the property is not defined JMX is not used.
   */
  public final static String ServerImpl = "MXServer";

  public static MXServer mxserver = null;
  
  private static Logger logger = Debug.getLogger("fr.dyade.aaa.util.management");

  public static void init() {
    if (mxserver != null)
      return;

    String mxname = System.getProperty(ServerImpl);

    // Be careful, do not call Debug.getLogger before initializing the
    // MXServer (see Debug.init).

    try {
      if ((mxname != null) && (mxname.length() > 0))
        Class.forName(mxname).newInstance();
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "can't instantiate MXServer: " + mxname, exc);
    }

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "MXWrapper.ServerImpl -> " + mxname);
  }

  public static void registerMBean(Object bean, String domain, String name) throws Exception {
    if (mxserver == null)
      return;

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "registerMBean: " + name + " -> " + mxserver);

    mxserver.registerMBean(bean, domain, name);
  }

  public static void unregisterMBean(String domain, String name) throws Exception {
    if (mxserver == null) return;

    if (logger.isLoggable(BasicLevel.INFO))
    logger.log(BasicLevel.INFO, "unregisterMBean: " + name + " -> " + mxserver);

    mxserver.unregisterMBean(domain, name);
  }
  
  public static void registerMBean(Object bean, String fullName) throws Exception {
    if (mxserver == null)
      return;

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "registerMBean: " + fullName + " -> " + mxserver);

    mxserver.registerMBean(bean, fullName);
  }

  public static void unregisterMBean(String fullName) throws Exception {
    if (mxserver == null)
      return;

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "unregisterMBean: " + fullName + " -> " + mxserver);
    
    mxserver.unregisterMBean(fullName);
  }

  public static void setMXServer(MXServer server) {
    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "setMXServer: " + server);

    mxserver = server;
  }

  public static MXServer getMXServer() {
    return mxserver;
  }
  
  public static Object getAttribute(ObjectName objectName, String attribute) {
    if (mxserver == null) {
      return null;
    }
    try {
      return mxserver.getAttribute(objectName, attribute);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, " getAttribute " + attribute + " on " + objectName + " error.", exc);
    }
    return null;
  }
  
  public static MBeanAttributeInfo[] getAttributes(ObjectName objectName) {
    if (mxserver == null) {
      return null;
    }
    try {
      return mxserver.getAttributes(objectName);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.ERROR, " getAttributes  on " + objectName + " error.", exc);
    }
    return null;
  }
  
  public static Set queryNames(ObjectName objectName) {
    if (mxserver == null) {
      return null;
    }
    return mxserver.queryNames(objectName);
  }
  
}
