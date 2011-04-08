/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.tools.jasp.osgi;

import java.util.Properties;

import javax.jms.ConnectionFactory;

import org.codehaus.stomp.jms.StompConnect;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class Activator implements BundleActivator {
  ConnectionFactory cf = null;
  StompConnect connect = null;
  
  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    cf = new LocalConnectionFactory();

    Properties props = new Properties();
    props.setProperty("java.naming.factory.initial",  "fr.dyade.aaa.jndi2.client.NamingContextFactory");
    props.setProperty("java.naming.factory.host",  "localhost");
    props.setProperty("java.naming.factory.port",  "16400");

    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

    connect = new StompConnect();
    connect.setConnectionFactory(cf);
    connect.start();
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    connect.stop();
    connect = null;
  }

}
