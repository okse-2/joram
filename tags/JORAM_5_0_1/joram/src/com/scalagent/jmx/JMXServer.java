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
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package com.scalagent.jmx;

import java.io.*;
import java.util.*;
import java.lang.reflect.Method;

import javax.management.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.objectweb.util.monolog.api.LoggerFactory;

import fr.dyade.aaa.util.management.*;

/**
 * 
 */
public class JMXServer implements MXServer {
  public MBeanServer mxserver = null;
  public String domain = null;

  public JMXServer(MBeanServer mxserver,
                   String domain) {
    this.mxserver = mxserver;
    this.domain = domain;
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
    this.domain = "AgentServer";
    MXWrapper.setMXServer(this);
  }

  public void registerMBean(Object bean,
                            String domain,
                            String name) throws Exception {
    if (mxserver == null) return;

    StringBuffer strbuf = new StringBuffer();
    strbuf.append(domain);
    if (name != null)
      strbuf.append(':').append(name);

    try {
      mxserver.registerMBean(bean, new ObjectName(strbuf.toString()));
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

  public void unregisterMBean(String domain,
                              String name) throws Exception {
    if (mxserver == null) return;

    StringBuffer strbuf = new StringBuffer();
    strbuf.append(domain);
    strbuf.append(':').append(name);

    try {
      mxserver.unregisterMBean(new ObjectName(strbuf.toString()));
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