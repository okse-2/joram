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
package fr.dyade.aaa.jndi2.server;

import fr.dyade.aaa.joram.admin.AdministeredObject;

import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * The <code>JndiSoapService</code> class implements a JNDI access through a
 * SOAP service.
 * <p>
 * Actually, this service is a "classical" JNDI client accessing the JNDI
 * server.
 */
public class JndiSoapService
{
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
  public void init(String jndiHost, int jndiPort) throws NamingException
  {
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
   * @param vec  Coded object.
   *
   * @exception NamingException  If the binding fails, or if the object could
   *              not be decoded.
   */
  public void bind(String name, Vector vec) throws NamingException
  {
    ctx.bind(name, AdministeredObject.decode(vec));
  }

  /**
   * Service method: decodes and rebinds an object.
   *
   * @param name  Name to use for rebinding the object.
   * @param vec  Coded object.
   *
   * @exception NamingException  If the rebinding fails, or if the object could
   *              not be decoded.
   */
  public void rebind(String name, Vector vec) throws NamingException
  {
    ctx.rebind(name, AdministeredObject.decode(vec));
  }

  /**
   * Service method: retrieves an object and returns it coded.
   *
   * @exception NamingException  If the lookup fails or if the object is not
   *              codable.
   */
  public Vector lookup(String name) throws NamingException
  {
    Object obj = ctx.lookup(name);
    if (obj instanceof AdministeredObject)
      return ((AdministeredObject) obj).code();
    else
      throw new NamingException("Non codable object: "
                                + obj.getClass().getName());
  }

  /**
   * Service method: unbinds an object.
   *
   * @exception NamingException  If the unbinding fails.
   */
  public void unbind(String name) throws NamingException
  {
    ctx.unbind(name);
  }
}
