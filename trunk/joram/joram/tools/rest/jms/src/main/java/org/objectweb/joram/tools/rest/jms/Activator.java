/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2016 ScalAgent Distributed Technologies
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
package org.objectweb.joram.tools.rest.jms;

import java.util.Dictionary;
import java.util.Hashtable;

import org.glassfish.jersey.servlet.ServletContainer;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

import fr.dyade.aaa.common.Debug;

/**
 *
 */
public class Activator implements BundleActivator {
  
  public static Logger logger = Debug.getLogger(Activator.class.getName());
  public static final String SERVICE_NAME = "rest.service.name";
  private BundleContext context = null;
	private HttpService httpService;
	public String servletAlias = "/joram";
	private CleanerTask cleanerTask;
	
  public void start(BundleContext bundleContext) throws Exception {
    this.context = bundleContext;
    ServiceReference<HttpService> reference = bundleContext.getServiceReference(HttpService.class);
    if (reference == null) {
      logger.log(BasicLevel.ERROR, "rest.jms.Activator ServiceReference<HttpService> = null");
      throw new Exception("rest.jms.Activator ServiceReference<HttpService> = null");
    }
    httpService = bundleContext.getService(reference);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "rest.jms.activator httpService = " + httpService);
    
    ClassLoader myClassLoader = getClass().getClassLoader();
    ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(myClassLoader);
      
      String serviceName = bundleContext.getProperty(SERVICE_NAME);
      if (serviceName != null && !serviceName.isEmpty()) {
        servletAlias = serviceName.startsWith("/") ? serviceName : "/" + serviceName;
      }
      
      // set properties from felix configuration
      Helper.getInstance().setGlobalProperties(bundleContext);
      
      Dictionary<String, String> jerseyServletParams = new Hashtable<>();
      jerseyServletParams.put("javax.ws.rs.Application", JmsJerseyApplication.class.getName());
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO, "start: REGISTERING SERVLETS " + servletAlias);
      
      HttpContext httpContext = null;
      // register the servlet
      httpService.registerServlet(servletAlias, new ServletContainer(), jerseyServletParams, httpContext);
      
      String period = bundleContext.getProperty(Helper.BUNDLE_CLEANER_PERIOD_PROP);
      if (period != null && !period.isEmpty()) {
        cleanerTask = new CleanerTask();
        cleanerTask.setPeriod(Integer.parseInt(period));
        cleanerTask.start();
      }
      
    } finally {
      Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }
  }

  public void stop(BundleContext bundleContext) throws Exception {
    if (cleanerTask != null)
      cleanerTask.stop();
    Helper.getInstance().closeAll();
    if (httpService != null) {
      if (logger.isLoggable(BasicLevel.INFO))
        logger.log(BasicLevel.INFO, "stop: UNREGISTERING SERVLETS " + servletAlias);
      httpService.unregister(servletAlias);
    }
  }
}
