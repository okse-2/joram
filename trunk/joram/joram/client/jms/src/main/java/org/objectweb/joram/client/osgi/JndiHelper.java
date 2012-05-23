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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * 
 */
public class JndiHelper {
	public static final Logger logmon = Debug.getLogger(JndiHelper.class.getName());
  
  private final boolean isSet(String value) {
    return value != null && value.length() > 0;
  }
  
  private Context getInitialContext() throws IOException, NamingException {
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
    	logmon.log(BasicLevel.DEBUG, "getInitialContext() - Load jndi.properties file");
    }
    
    Context jndiCtx;
    Properties props = new Properties();
    InputStream in = Class.class.getResourceAsStream("/jndi.properties");
    
    if (in == null) {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
      	logmon.log(BasicLevel.DEBUG, "jndi.properties not found.");
      }
    } else {
      props.load(in);
    }

    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    jndiCtx = new InitialContext(props);
    return jndiCtx;
  }
  
  /**
   * rebind the object.
   * 
   * @param jndiName the jndi name
   * @param obj object to rebind
   * @throws NamingException
   */
  public void rebind(String jndiName, Object obj) throws NamingException {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "bind(" + jndiName + ", " + obj + ')');
  	if (isSet(jndiName)) {
  		Context ctx = null;
  		ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
  		try {
  			try {
  				ctx = getInitialContext();
  				ctx.rebind(jndiName, obj);
  			} catch (Exception e) {
  				if (logmon.isLoggable(BasicLevel.WARN))
  					logmon.log(BasicLevel.WARN, "EXCEPTION:: rebind: " + jndiName, e);
  				throw new NamingException(e.getMessage());
  			}
  		} finally {
  			Thread.currentThread().setContextClassLoader(oldClassLoader);
  			// Closing the JNDI context.
  			try {
  				if (ctx != null)
  					ctx.close();
  			}	catch (Exception exc) {}
  		}
  	}
  }
  
  /**
   * Unbind the jndi name
   * 
   * @param jndiName the jndi name
   */
  public void unbind(String jndiName) {
  	if (logmon.isLoggable(BasicLevel.DEBUG))
  		logmon.log(BasicLevel.DEBUG, "unbind(" + jndiName + ')');
  	if (isSet(jndiName)) {
  		Context ctx = null;
  		ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
  		try {
  			try {
  				ctx = getInitialContext();
  				ctx.unbind(jndiName);
  			} catch (Exception e) {
  				if (logmon.isLoggable(BasicLevel.WARN))
  					logmon.log(BasicLevel.WARN, "EXCEPTION:: unbind: " + jndiName, e);
  			}
  		} finally {
  			Thread.currentThread().setContextClassLoader(oldClassLoader);
  			// Closing the JNDI context.
  			try {
  				if (ctx != null)
  					ctx.close();
  			}	catch (Exception exc) {}
  		}
  	}
  }
  
  /**
   * Lookup the jndi name
   * 
   * @param jndiName the jndi name
   */
  public Object lookup(String jndiName) throws NamingException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "lookup(" + jndiName + ')');
    Context ctx = null;
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      try {
        ctx = getInitialContext();
        return ctx.lookup(jndiName);
      } catch (IOException e) {
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN, "EXCEPTION:: lookup: " + jndiName, e);
        throw new NamingException(e.getMessage());
      }
    } finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
      // Closing the JNDI context.
      try {
        if (ctx != null)
          ctx.close();
      } catch (Exception exc) {}
    }
  }
}