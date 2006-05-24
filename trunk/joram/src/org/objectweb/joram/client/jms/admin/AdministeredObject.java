/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms.admin;

import fr.dyade.aaa.jndi2.soap.SoapObjectItf;
import org.objectweb.joram.client.jms.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import java.util.Hashtable;
import java.util.Vector;


/**
 * The <code>AdministeredObject</code> class is the parent class of all
 * JORAM administered objects.
 */
public abstract class AdministeredObject implements java.io.Serializable,
                                                    javax.naming.Referenceable,
                                                    SoapObjectItf
{

  /** Sets the naming reference of an administered object. */
  public Reference getReference() throws NamingException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG, "AdministeredObject.getReference()");
    Reference ref =
      new Reference(this.getClass().getName(),
                    "org.objectweb.joram.client.jms.admin.ObjectFactory",
                    null);
    return ref;
  }
}
