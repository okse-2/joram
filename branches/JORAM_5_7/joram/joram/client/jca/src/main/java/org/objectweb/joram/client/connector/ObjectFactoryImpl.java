/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - ScalAgent Distributed Technologies
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s):  Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.connector;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * This class is a factory for building non managed outbound connection 
 * factories from their JNDI reference.
 */
public class ObjectFactoryImpl implements javax.naming.spi.ObjectFactory
{
  String cf =
    "org.objectweb.joram.client.connector.OutboundConnectionFactory";
  String qcf =
    "org.objectweb.joram.client.connector.OutboundQueueConnectionFactory";
  String tcf =
    "org.objectweb.joram.client.connector.OutboundTopicConnectionFactory";


  /** Returns a factory given its reference. */
  public Object getObjectInstance(Object obj,
                                  Name name,
                                  Context ctx,
                                  java.util.Hashtable env)
    throws Exception {

    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " getObjectInstance(" + obj + 
                                    ", " + name +
                                    ", " + ctx +
                                    ", " + env + ")");

    Reference ref = (Reference) obj;

    String hostName = (String) ref.get("hostName").getContent();
    Integer serverPort = 
      new Integer((String) ref.get("serverPort").getContent());
    String userName = (String) ref.get("userName").getContent();
    String password = (String) ref.get("password").getContent();
    String identityClass = (String) ref.get("identityClass").getContent();

    ManagedConnectionFactoryImpl mcf = null;

    if (ref.getClassName().equals(cf))
      mcf = new ManagedConnectionFactoryImpl();
    else if (ref.getClassName().equals(qcf))
      mcf = new ManagedQueueConnectionFactoryImpl();
    else if (ref.getClassName().equals(tcf))
      mcf = new ManagedTopicConnectionFactoryImpl();
    else
      return null;

    mcf.setCollocated(new Boolean(false));
    mcf.setHostName(hostName);
    mcf.setServerPort(serverPort);
    mcf.setUserName(userName);
    mcf.setPassword(password);
    mcf.setIdentityClass(identityClass);

    try {
      return mcf.createConnectionFactory();
    }
    catch (Exception exc) {
      return null;
    }
  }
}
