/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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

import javax.management.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.objectweb.util.monolog.api.LoggerFactory;

import fr.dyade.aaa.agent.Debug;
import fr.dyade.aaa.agent.management.*;

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
    this.mxserver = MBeanServerFactory.createMBeanServer("AgentServer");;
    this.domain = "AgentServer";
    MXWrapper.setMXServer(this);
  }

  public void registerMBean(Object bean,
                            String domain,
                            String name,
                            String type,
                            String desc) throws Exception {
    if (mxserver == null) return;

    StringBuffer strbuf = new StringBuffer();
    strbuf.append(domain);
    strbuf.append(":name=").append(name);
    strbuf.append(",type=").append(type);
    if (desc != null)
      strbuf.append(',').append(desc);

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

  public void registerMBean(Object bean,
                            String name,
                            String type,
                            String desc) throws Exception {
    registerMBean(bean, domain, name, type, desc);
  }

  public void unregisterMBean(String domain,
                              String name,
                              String type,
                              String desc) throws Exception {
    if (mxserver == null) return;

    StringBuffer strbuf = new StringBuffer();
    strbuf.append(domain);
    strbuf.append(":name=").append(name);
    strbuf.append(",type=").append(type);
    if (desc != null)
      strbuf.append(',').append(desc);

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

  public void unregisterMBean(String name,
                              String type,
                              String desc) throws Exception {
    unregisterMBean(domain, name, type, desc);
  }
}
