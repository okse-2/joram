/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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
 * Initial developer(s): David Feliot
 * Contributor(s):
 */
package fr.dyade.aaa.jndi2.scn;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * Context factory for scnURLContext objects.
 * This factory will be used for all "scn:..." urls provided as Name objects
 * for all JNDI operations.
 */
public class scnURLContextFactory implements ObjectFactory {

  public final static Logger logger;
  
  static {
    logger = fr.dyade.aaa.common.Debug.getLogger("fr.dyade.aaa.jndi2.scn");    
  }

  /**
   * Returns an instance of scnURLContext for a java URL.
   *
   * If url is null, the result is a context for resolving java URLs.
   * If url is a URL, the result is a context named by the URL.
   *
   * @param url 	String with a "scn:" prefix or null.
   * @param name	Name of context, relative to ctx, or null.
   * @param ctx	Context relative to which 'name' is named.
   * @param env	Environment to use when creating the context
   */
  public Object getObjectInstance(Object url, 
                                  Name name, 
                                  Context ctx,
				  Hashtable env)
    throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "scnURLContextFactory.getObjectInstance(" + url + 
                 ',' + name + ',' + ctx + ',' + env + ')');

    if (url == null) {
      return new scnURLContext(env);
    } else if (url instanceof String) {
      // Don't know what to do here 
      return null;
    } else if (url instanceof String[]) {
      // Don't know what to do here 
      return null;
    } else {
      // invalid argument
      throw (new IllegalArgumentException(
        "scnURLContextFactory"));
    }
  }
}

