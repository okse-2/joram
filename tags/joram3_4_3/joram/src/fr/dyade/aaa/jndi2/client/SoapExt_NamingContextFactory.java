/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.jndi2.client;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NamingException;

import java.util.Hashtable;

/**
 * Extends the <code>NamingContextFactory</code> for allowing clients to
 * access JNDI through a SOAP service.
 */
public class SoapExt_NamingContextFactory extends NamingContextFactory
{
  /**
   * Extended method returning when requested a JNDI initial context for
   * accessing a SOAP service.
   *
   * @param env  Contains the configuration parameters.
   *
   * @exception NamingException  Thrown if the parameters are invalid.
   */
  public Context getInitialContext(Hashtable env) throws NamingException
  {
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
