/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.osgi;

import java.util.Dictionary;
import java.util.Properties;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import fr.dyade.aaa.common.Debug;

/**
 * Create a queue with a ConfigAdmin file.
 *
 * <p><hr>
 * The reserved words for properties:
 * <ul>
 *  <li> adminWrapper
 *  <li> adminHost
 *  <li> adminPort
 *  <li> adminUser
 *  <li> name
 *  <li> serverId
 *  <li> freeReading
 *  <li> freeWriting
 *  <li> readers
 *  <li> writers
 *  <li> threshold
 *  <li> nbMaxMsg
 *  <li> jndiName
 *  <li> dmq
 *  <li> dmqSid
 * </ul>
 * 
 * <p><hr>
 * A simple example:
 * <p><blockquote><pre>
 *  &lt;configadmin&gt;
 *   &lt;factory-configuration pid="joram.queue"&gt;
 *     &lt;property name="adminWrapper"&gt;ra&lt;/property&gt;
 *     &lt;property name="serverId"&gt;0&lt;/property&gt;
 *     &lt;property name="name"&gt;queue&lt;/property&gt;
 *     &lt;!--  properties  --&gt;
 *     &lt;property name="freeReading"&gt;true&lt;/property&gt;
 *     &lt;property name="writers"&gt;user1 user2&lt;/property&gt;
 *     &lt;property name="jndiName"&gt;queue&lt;/property&gt;
 *   &lt;/factory-configuration&gt;
 *  &lt;/configadmin&gt;
 * </pre></blockquote>
 * <p>
 *
 */
public class ServiceQueue extends ServiceDestination implements ManagedServiceFactory {
  public static final Logger logmon = Debug.getLogger(ServiceQueue.class.getName());
  public static final String PID_NAME = "joram.queue";

  private ServiceRegistration registration;
  private String className = Queue.QUEUE;

  public ServiceQueue(final BundleContext bundleContext) {
    super(bundleContext);
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "ServiceQueue(" + bundleContext + ')');
    Properties props = new Properties();
    props.setProperty(Constants.SERVICE_PID, PID_NAME);
    registration = bundleContext.registerService(
        ManagedServiceFactory.class.getName(),
        this,
        props);
  }

  public void doStop() {
    if (registration != null)
      registration.unregister();
  }

  //************ ManagedServiceFactory ************
  public String getName() {
    return PID_NAME;
  }

  /* (non-Javadoc)
   * @see org.osgi.service.cm.ManagedServiceFactory#updated(java.lang.String, java.util.Dictionary)
   */
  public void updated(final String pid, final Dictionary properties)
      throws ConfigurationException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "ServiceQueue.updated(" + pid + ", " + properties + ')');

    try {
      if (properties == null) {
        deleted(pid);
        return;
      }

      String cn = (String) properties.get("className");
      if (cn != null)
        className = cn;

      new Thread(new Runnable() {
        public void run() {
          doUpdated(pid, properties, className, true);
        }
      }).start();

    } catch (Exception e) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "ServiceQueue.updated ", e);
      throw new ConfigurationException(null, e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
   */
  public void deleted(String pid) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "ServiceQueue.deleted(" + pid + ')');
    super.deleted(pid);
  }
}