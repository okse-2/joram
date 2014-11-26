/*
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.osgi.framework.ServiceRegistration;

import fr.dyade.aaa.common.osgi.Activator;
import fr.dyade.aaa.util.management.MXServer;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * Implementation of the MXServer interface allowing the a3rt independence
 * from the JMX framework.
 */
public class JMXServer implements MXServer {

  public static boolean registerAsService = false;
  
  private MBeanServer mxserver = null;

  private Map<ObjectName, ServiceRegistration> registeredServices = new HashMap<ObjectName, ServiceRegistration>();

  public JMXServer(MBeanServer mxserver) {
    this.mxserver = mxserver;
  }

  public JMXServer() {
    try {
      // Try to get the default platform MBeanServer (since JDK 1.5)
      Class<?> clazz = Class.forName("java.lang.management.ManagementFactory");
      Method method = clazz.getMethod("getPlatformMBeanServer", (Class[]) null);
      mxserver = (MBeanServer) method.invoke(null, (Object[]) null);
    } catch (Exception exc) {
      // Prior JDK1.5 (with JMXRI implementation).
      mxserver = MBeanServerFactory.createMBeanServer("AgentServer");
    }
    MXWrapper.setMXServer(this);
  }

  private void registerOSGi(Object obj, ObjectName objName) {
    if (!registerAsService) return;

    Hashtable<String, String> registrationProperties = objName.getKeyPropertyList();
    registrationProperties.put("domain", objName.getDomain());
    if (registeredServices.containsKey(objName)) {
      ServiceRegistration registration = (ServiceRegistration) registeredServices.get(objName);
      registration.setProperties(registrationProperties);
      return;
    }
    
    Set<String> serviceNames = new HashSet<String>();
    computeOSGiServiceNames(obj.getClass(), obj, serviceNames);
    ServiceRegistration registration = Activator.context.registerService((String[]) serviceNames.toArray(new String[serviceNames.size()]), obj, registrationProperties);
    registeredServices.put(objName, registration);
  }

  private void computeOSGiServiceNames(Class<?> beanClass, Object bean, Set<String> registered) {
    if (beanClass == null)  return;

    Class<?>[] interfaces = beanClass.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      if (interfaces[i].getName().endsWith("MBean") && !registered.contains(interfaces[i].getName())) {
        registered.add(interfaces[i].getName());
        computeOSGiServiceNames(interfaces[i], bean, registered);
      }
    }
    computeOSGiServiceNames(beanClass.getSuperclass(), bean, registered);
  }

  public synchronized void registerMBean(Object bean, String fullName) throws Exception {
    if (mxserver == null) return;
    
    ObjectName objName = ObjectName.getInstance(fullName);
    mxserver.registerMBean(bean, objName);
    registerOSGi(bean, objName);
  }

  public synchronized void unregisterMBean(String fullName) throws Exception {
    if (mxserver == null) return;
    
    ObjectName objName = ObjectName.getInstance(fullName);
    mxserver.unregisterMBean(objName);

    if (registerAsService) {
      ServiceRegistration registration = (ServiceRegistration) registeredServices.remove(objName);
      if (registration != null) {
        registration.unregister();
      }
    }
  }

  public Object getAttribute(String objectName, String attribute) throws Exception {
    if (mxserver == null) return null;

    return mxserver.getAttribute(new ObjectName(objectName), attribute);
  }
  
  public List<String> getAttributeNames(String objectName) throws Exception {
    if (mxserver == null)  return null;

    MBeanAttributeInfo[] attrs = mxserver.getMBeanInfo(new ObjectName(objectName)).getAttributes();
    List<String> names = new ArrayList<String>();
    for (int i = 0; i < attrs.length; i++) {
      names.add(attrs[i].getName());
    }
    return names;
  }
  
  public Set<String> queryNames(String objectName) throws MalformedObjectNameException {
    if (mxserver == null) return null;

    Set<ObjectName> objectNames = mxserver.queryNames(new ObjectName(objectName), null);
    Set<String> names = new HashSet<String>();
    for (Iterator<ObjectName> iterator = objectNames.iterator(); iterator.hasNext();) {
      ObjectName objName = iterator.next();
      names.add(objName.getCanonicalName());
    }
    return names;
  }
  
}
