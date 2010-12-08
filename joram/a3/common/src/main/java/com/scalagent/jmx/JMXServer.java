/*
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;

import org.osgi.framework.ServiceRegistration;

import fr.dyade.aaa.common.osgi.Activator;
import fr.dyade.aaa.util.management.MXServer;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * 
 */
public class JMXServer implements MXServer {
  
  private MBeanServer mxserver = null;

  private Map registeredServices = new HashMap();
  private Map registeredMBeans = new HashMap();

  public static boolean registerAsService = false;

  public JMXServer(MBeanServer mxserver) {
    this.mxserver = mxserver;
    MXWrapper.setMXServer(this);
  }

  public JMXServer() {
    try {
      // Try to get the default platform MBeanServer (since JDK 1.5)
      Class clazz = Class.forName("java.lang.management.ManagementFactory");
      Method method = clazz.getMethod("getPlatformMBeanServer", (Class[]) null);
      mxserver = (MBeanServer) method.invoke(null, (Object[]) null);
    } catch (Exception exc) {
      // Prior JDK1.5 (with JMXRI implementation).
      mxserver = MBeanServerFactory.createMBeanServer("AgentServer");
    }
    MXWrapper.setMXServer(this);
  }

  private void registerOSGi(Object obj, ObjectName objName) {
    if (!registerAsService) {
      return;
    }
    Hashtable registrationProperties = objName.getKeyPropertyList();
    registrationProperties.put("domain", objName.getDomain());
    if (registeredServices.containsKey(objName)) {
      ServiceRegistration registration = (ServiceRegistration) registeredServices.get(objName);
      registration.setProperties(registrationProperties);
      return;
    }
    
    Set serviceNames = new HashSet();
    computeOSGiServiceNames(obj.getClass(), obj, serviceNames);
    ServiceRegistration registration = Activator.context.registerService((String[]) serviceNames.toArray(new String[serviceNames.size()]), obj, registrationProperties);
    registeredServices.put(objName, registration);
  }

  private void computeOSGiServiceNames(Class beanClass, Object bean, Set registered) {
    if (beanClass == null) {
      return;
    }
    Class[] interfaces = beanClass.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      if (interfaces[i].getName().endsWith("MBean") && !registered.contains(interfaces[i].getName())) {
        registered.add(interfaces[i].getName());
        computeOSGiServiceNames(interfaces[i], bean, registered);
      }
    }
    computeOSGiServiceNames(beanClass.getSuperclass(), bean, registered);
  }

  public String registerMBean(Object bean, String fullName) throws Exception {
    if (mxserver == null) return null;

    try {
      ObjectName objName = ObjectName.getInstance(fullName);
      registeredMBeans.put(objName, bean);
      mxserver.registerMBean(bean, objName);
      registerOSGi(bean, objName);
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
    
    return fullName;
  }

  public void unregisterMBean(String fullName) throws Exception {
    if (mxserver == null)
      return;
    try {
      ObjectName objName = ObjectName.getInstance(fullName);
      mxserver.unregisterMBean(objName);
      registeredMBeans.remove(objName);

      if (registerAsService) {
        ServiceRegistration registration = (ServiceRegistration) registeredServices.remove(objName);
        if (registration != null) {
          registration.unregister();
        }
      }
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
  
  public void setAttribute(ObjectName name, Attribute attribute) throws Exception {
    if (mxserver != null)
        mxserver.setAttribute(name, attribute);
  }
  
  public Object getMBeanInstance(ObjectName objName) {
    return registeredMBeans.get(objName);
  }
  
  /**
   * Adds a listener to a registered MBean.
   */
  public void addNotificationListener(ObjectName name,
                               NotificationListener listener,
                               NotificationFilter filter,
                               Object handback) throws Exception {
    mxserver.addNotificationListener(name, listener, filter, handback);
  }
  
  /**
   * Removes a listener from a registered MBean.
   */
  public void removeNotificationListener(ObjectName name,
                                  NotificationListener listener) throws Exception {
    mxserver.removeNotificationListener(name, listener);
  }
  
  /**
   * Removes a listener from a registered MBean.
   */
  public void removeNotificationListener(ObjectName name,
                                  NotificationListener listener,
                                  NotificationFilter filter,
                                  Object handback) throws Exception {
    mxserver.removeNotificationListener(name, listener, filter, handback);
  }

  public Object getAttribute(ObjectName objectName, String attribute) throws Exception {
    if (mxserver == null) {
      return null;
    }
    return mxserver.getAttribute(objectName, attribute);
  }
  
  public MBeanAttributeInfo[] getAttributes(ObjectName objectName) throws Exception {
    if (mxserver == null) {
      return null;
    }
    return mxserver.getMBeanInfo(objectName).getAttributes();
  }
  
  public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws Exception {
  	if (mxserver == null) {
  		return null;
  	}
  	return mxserver.setAttributes(name, attributes);
  }
  
  public Set queryNames(ObjectName objectName) {
    if (mxserver == null) {
      return null;
    }
    return mxserver.queryNames(objectName, null);
  }
  
}
