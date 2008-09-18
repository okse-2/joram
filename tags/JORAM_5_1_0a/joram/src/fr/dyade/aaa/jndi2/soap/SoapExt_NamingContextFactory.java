/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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

import fr.dyade.aaa.jndi2.client.NamingContextFactory;

import javax.naming.Context;
import javax.naming.NamingException;

import java.util.Hashtable;

/**
 * Extends the <code>NamingContextFactory</code> for allowing clients to
 * access JNDI through a SOAP service.
 */
public class SoapExt_NamingContextFactory extends NamingContextFactory {
  /**
   * Extended method returning when requested a JNDI initial context for
   * accessing a SOAP service.
   *
   * @param env  Contains the configuration parameters.
   *
   * @exception NamingException  Thrown if the parameters are invalid.
   */
  public Context getInitialContext(Hashtable env) throws NamingException {
    String soapHost = (String) env.get("java.naming.factory.soapservice.host");

    // No SOAP service described in the configuration, building a "classical"
    // tcp context:
    if (soapHost == null)
      return super.getInitialContext(env);

    int soapPort;
    String jndiHost;
    int jndiPort;

    try {
      Object soapPortObj = env.get("java.naming.factory.soapservice.port");
      soapPort = Integer.parseInt((String) soapPortObj);
    }
    catch (Exception exc) {
      NamingException nEx =
        new NamingException("Invalid java.naming.factory.soapservice.port"
                            + " parameter.");
      nEx.setRootCause(exc);
      throw nEx;
    }

    jndiHost = (String) env.get("java.naming.factory.host");
    if (jndiHost == null)
      throw new NamingException("Missing java.naming.factory.host parameter.");

    try {
      Object jndiPortObj = env.get("java.naming.factory.port");
      jndiPort = Integer.parseInt((String) jndiPortObj);
    }
    catch (Exception exc) {
      NamingException nEx =
        new NamingException("Invalid java.naming.factory.port parameter.");
      nEx.setRootCause(exc);
      throw nEx;
    }

    return new SoapExt_NamingContextImpl(soapHost, soapPort,
                                         jndiHost, jndiPort);
  }
}
