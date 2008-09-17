/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
 */
package fr.dyade.aaa.jndi2.soap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import fr.dyade.aaa.util.Debug;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * The <code>JndiSoapService</code> class implements a JNDI access through a
 * SOAP service.
 * <p>
 * Actually, this service is a "classical" JNDI client accessing the JNDI
 * server.
 */
public class JndiSoapService {
  public static final Logger logger = 
    Debug.getLogger(JndiSoapService.class.getName());
  
  /** The service's <code>Context</code>. */
  private Context ctx;

  /**
   * Initializes the <code>JndiSoapService</code>.
   *
   * @param jndiHost  Host hosting the JNDI server.
   * @param jndiPort  JNDI server's port.
   *
   * @exception NamingException  If the JNDI server is not reachable or if
   *              the parameters are invalid.
   */
  public void init(String jndiHost, int jndiPort) throws NamingException {
    java.util.Hashtable env = new java.util.Hashtable();
    env.put("java.naming.factory.initial",
            "fr.dyade.aaa.jndi2.client.NamingContextFactory");
    env.put("java.naming.factory.host", jndiHost);
    env.put("java.naming.factory.port", "" + jndiPort);
    ctx = new InitialContext(env);
  }

  /**
   * Service method: decodes and binds an object.
   *
   * @param name  Name to use for binding the object.
   * @param map  Coded object.
   *
   * @exception NamingException  If the binding fails, or if the object could
   *              not be decoded.
   */
  public void bind(String name, Hashtable map) throws NamingException {
    ctx.bind(name, SoapObjectHelper.soapDecode(map));
  }

  /**
   * Service method: decodes and rebinds an object.
   *
   * @param name  Name to use for rebinding the object.
   * @param map  Coded object.
   *
   * @exception NamingException  If the rebinding fails, or if the object could
   *              not be decoded.
   */
  public void rebind(String name, Hashtable map) throws NamingException {
    ctx.rebind(name, SoapObjectHelper.soapDecode(map));
  }

  /**
   * Service method: retrieves an object and returns it coded.
   *
   * @exception NamingException  If the lookup fails or if the object is not
   *              codable.
   */
  public Hashtable lookup(String name) throws NamingException {
    try {
    return SoapObjectHelper.soapCode(ctx.lookup(name));
    } catch (Throwable exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      throw new NamingException(exc.toString());
    }
  }

  /**
   * Service method: unbinds an object.
   *
   * @exception NamingException  If the unbinding fails.
   */
  public void unbind(String name) throws NamingException {
    ctx.unbind(name);
  }
}
