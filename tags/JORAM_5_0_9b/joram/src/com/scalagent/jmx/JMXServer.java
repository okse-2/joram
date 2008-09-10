/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
package com.scalagent.jmx;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;

import fr.dyade.aaa.util.management.MXServer;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * 
 */
public class JMXServer implements MXServer, Serializable {
  
  public MBeanServer mxserver = null;

  public JMXServer(MBeanServer mxserver) {
    this.mxserver = mxserver;
    MXWrapper.setMXServer(this);
  }

  public JMXServer() {
    try {
      // Try to get the default platform MBeanServer (since JDK 1.5)
      Class clazz = Class.forName("java.lang.management.ManagementFactory");
      Method method = clazz.getMethod("getPlatformMBeanServer", null);
      this.mxserver = (MBeanServer) method.invoke(null, null);
    } catch (Exception exc) {
      // Prior JDK1.5 (with JMXRI implementation).
      this.mxserver = MBeanServerFactory.createMBeanServer("AgentServer");
    }
    MXWrapper.setMXServer(this);
  }

  public void registerMBean(Object bean,
                            String domain,
                            String name) throws Exception {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append(domain).append(':').append(name);
    registerMBean(bean, strbuf.toString());
  }

  public void unregisterMBean(String domain,
                              String name) throws Exception {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append(domain);
    strbuf.append(':').append(name);
    unregisterMBean(strbuf.toString());
  }

  public void registerMBean(Object bean, String fullName) throws Exception {
    if (mxserver == null)
      return;
    try {
      mxserver.registerMBean(bean, new ObjectName(fullName));
    } catch (InstanceAlreadyExistsException exc) {
      // The MBean is already under the control of the MBean server.
      throw exc;
    } catch (MBeanRegistrationException exc) {
      // The preRegister (MBeanRegistration  interface) method of the MBean
      // has thrown an exception. The MBean will not be registered.
      throw exc;
    } catch (NotCompliantMBeanException exc) {
      // This object is not a JMX compliant MBean
      throw exc;
    } catch (RuntimeOperationsException exc) {
      // Wraps a java.lang.IllegalArgumentException
      throw exc;
    }
  }

  public void unregisterMBean(String fullName) throws Exception {
    if (mxserver == null)
      return;
    try {
      mxserver.unregisterMBean(new ObjectName(fullName));
    } catch (InstanceNotFoundException exc) {
      // The MBean is not registered in the MBean server.
      throw exc;
    } catch (MBeanRegistrationException exc) {
      // The preDeregister (MBeanRegistration  interface) method of the MBean
      // has thrown an exception.
      throw exc;
    } catch (RuntimeOperationsException exc) {
      // Wraps a java.lang.IllegalArgumentException
      throw exc;
    }
  }
}
