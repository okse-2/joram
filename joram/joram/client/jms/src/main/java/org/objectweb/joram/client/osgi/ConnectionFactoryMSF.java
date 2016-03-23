/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - 2016 ScalAgent Distributed Technologies
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.naming.NamingException;

import org.objectweb.joram.client.jms.admin.AbstractConnectionFactory;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import fr.dyade.aaa.common.Debug;

/**
 * Create connection factories with a ConfigAdmin file.
 * 
 * <p><hr>
 * The reserved words for properties:
 * <ul>
 *  <li> className
 *  <li> host
 *  <li> port
 *  <li> reliableClass
 *  <li> timeout
 *  <li> identityClassName
 *  <li> inInterceptorClassname
 *  <li> outInterceptorClassname
 *  <li> jndiName
 * </ul>
 * 
 * <p><hr>
 * A simple example:
 * <p><blockquote><pre>
 *  &lt;configadmin&gt;
 * &lt;factory-configuration pid="org.objectweb.joram.client.osgi.ConnectionFactoryMSF"&gt;
 *   &lt;property name="className"&gt;org.objectweb.joram.client.jms.tcp.TcpConnectionFactory&lt;/property&gt;
 *   &lt;property name="host"&gt;localhost&lt;/property&gt;
 *   &lt;property name="port"&gt;16010&lt;/property&gt;
 *   &lt;property name="jndiName"&gt;JCF&lt;/property&gt;
 *   &lt;!--  properties  --&gt;
 * &lt;/factory-configuration&gt;
 *  &lt;/configadmin&gt;
 * </pre></blockquote>
 * <p>
 */
public class ConnectionFactoryMSF implements ManagedServiceFactory { 
	public static final Logger logmon = Debug.getLogger(ConnectionFactoryMSF.class.getName());
	
	// reserved words
	public static final String CLASSNAME = "className";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String RELIABLECLASS = "reliableClass";
	public static final String TIMEOUT = "timeout";
	public static final String IDENTITYCLASS = "identityClassName";
	public static final String ININTERCEPTORCLASS = "inInterceptorClassname";
	public static final String OUTINTERCEPTORCLASS = "outInterceptorClassname";
	public static final String JNDINAME = "jndiName";
	// unused 
	public static final String NAME = "name";
	
  private BundleContext bundleContext;
  private ServiceRegistration registration;
  private HashMap<String, AbstractConnectionFactory> cnxFactories;
  private JndiHelper jndiHelper;
  private HashMap<String, String> jndiNames;

  public ConnectionFactoryMSF(final BundleContext bundleContext) {
    this.bundleContext = bundleContext;
    Properties props = new Properties();
    props.setProperty(Constants.SERVICE_PID, ConnectionFactoryMSF.class.getName());
    registration = this.bundleContext.registerService(
    		ManagedServiceFactory.class.getName(),
        this,
        (Dictionary)props);
    cnxFactories = new HashMap<String, AbstractConnectionFactory>();
    jndiHelper = new JndiHelper();
    jndiNames = new HashMap<String, String>();
  }
  
  private final boolean isSet(String value) {
    return value != null && value.length() > 0;
  }
  
  protected void doStart() throws Exception {
  	//TODO
  }

  protected void doStop() {
  	// TODO
  }

  //************ ManagedService ************
  public String getName() {
	  return "ConnectionFactoryMSF";
  }

	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedServiceFactory#updated(java.lang.String, java.util.Dictionary)
	 */
	public void updated(final String pid, final Dictionary properties)
      throws ConfigurationException {
		if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "updated(" + pid + ", " + properties + ')');
		
		if (properties == null) {
			deleted(pid);
			return;
		}

		AbstractConnectionFactory cf = null;
		
		//TODO: name ?
		String name = (String) properties.get(NAME);
		
		String className = (String) properties.get(CLASSNAME);
		if (!isSet(className)) {
			throw new ConfigurationException(CLASSNAME, "this property is required.");
		}
		
		if (TcpConnectionFactory.class.getName().equals(className)) {
			// TcpConnectionFactory
			String host = (String) properties.get(HOST);
			if (!isSet(host))
				host = AbstractConnectionFactory.getDefaultServerHost();
			int port =-1;
			String portStr = (String) properties.get(PORT);
			if (!isSet(portStr))
				port = AbstractConnectionFactory.getDefaultServerPort();
			else	
				port = new Integer(portStr).intValue();
			String reliableClass = (String) properties.get(RELIABLECLASS);
			if (!isSet(reliableClass))
				reliableClass = "org.objectweb.joram.client.jms.tcp.ReliableTcpClient";
			cf  = TcpConnectionFactory.create(host, port, reliableClass);
			
		} else if (LocalConnectionFactory.class.getName().equals(className)) {
			// LocalConnectionFactory
			cf = LocalConnectionFactory.create();
			
		} else {
			throw new ConfigurationException(CLASSNAME, "Unknown class : " + className); 
		}
		
		// put cf in cnxFactories Map
		cnxFactories.put(pid, cf);
		
		String identityClassName = (String) properties.get(IDENTITYCLASS);
		if (!isSet(identityClassName)) {
			identityClassName = SimpleIdentity.class.getName();
		}
		
		String inInterceptorClassname = (String) properties.get(ININTERCEPTORCLASS);
		if (isSet(inInterceptorClassname))
			cf.getParameters().addInInterceptor(inInterceptorClassname);
		String outInterceptorClassname = (String) properties.get(OUTINTERCEPTORCLASS);
		if (isSet(outInterceptorClassname))
			cf.getParameters().addOutInterceptor(outInterceptorClassname);
		
		// remove the reserved word, and set factory parameters
		Properties prop = new Properties();
		Enumeration keys = properties.keys();
		while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      if (key.equals(CLASSNAME))
        continue;
      else if (key.equals(HOST))
        continue;
      else if (key.equals(PORT))
        continue;
      else if (key.equals(RELIABLECLASS))
        continue;
      else if (key.equals(TIMEOUT))
        continue;
      else if (key.equals(IDENTITYCLASS))
        continue;
      else if (key.equals(ININTERCEPTORCLASS))
        continue;
      else if (key.equals(OUTINTERCEPTORCLASS))
        continue;
      else if (key.equals(JNDINAME))
        continue;
      else if (key.equals(NAME))
        continue;
      else if (key.equals("service.pid"))
        continue;
      else if (key.equals("service.factoryPid"))
        continue;
      else if (key.startsWith("jonas"))
        continue;
      // add property
      Object value = properties.get(key);
      if (value != null && value instanceof String)
        prop.put(key, (String) value);
    }
		// set factory parameters
		cf.getParameters().setParameters(prop);
		   
		final String jndiName = (String) properties.get(JNDINAME);
		final AbstractConnectionFactory cff = cf;
		if (isSet(jndiName)) {
			new Thread(new Runnable() {
				public void run() {
					int i = 0;
					while (i < 10) {
						try {
							jndiHelper.rebind(jndiName, cff);
							jndiNames.put(pid, jndiName);
							break;
						} catch (NamingException e) {
							i++;
							try {
	              Thread.sleep(1000);
	              if (logmon.isLoggable(BasicLevel.DEBUG))
	      					logmon.log(BasicLevel.DEBUG, "retry to rebind: " + jndiName);
              } catch (InterruptedException e1) {
              	break;
              }
						}
					}
				}
			}).start();
		}
		
		if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "updated cf created/updated : " + cf);
  }

	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
	 */
	public void deleted(String pid) {
		if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "deleted(" + pid + ')');
	  
		if (cnxFactories.containsKey(pid)) {
			AbstractConnectionFactory factory = cnxFactories.remove(pid);
			String jndiName = jndiNames.remove(pid);
			if (jndiName != null)
				jndiHelper.unbind(jndiName);
		}
  }
}