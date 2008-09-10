/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.kjoram.ksoap;

import com.scalagent.kjoram.Connection;
import com.scalagent.kjoram.FactoryParameters;
import com.scalagent.kjoram.excepts.*;

import java.util.Vector;
import java.util.Hashtable;


/**
 * A <code>SoapConnectionFactory</code> instance is a factory of SOAP
 * connections.
 */
public class SoapConnectionFactory extends com.scalagent.kjoram.ConnectionFactory
{
  /**
   * Constructs a <code>SoapConnectionFactory</code> instance.
   *
   * @param host  Name or IP address of the server's host.
   * @param port  Server's listening port.
   * @param timeout  Duration in seconds during which a SOAP connection might
   *          be inactive before being considered as dead (0 for never).
   */
  public SoapConnectionFactory(String host, int port, int timeout)
  {
    super(host, port);
    params.soapCnxPendingTimer = timeout;
  }

  /**
   * Constructs an empty <code>SoapConnectionFactory</code> instance.
   */
  public SoapConnectionFactory()
  {}


  /**
   * Method inherited from the <code>ConnectionFactory</code> class.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public Connection createConnection(String name, String password)
         throws JMSException
  {
    return new Connection(params, new SoapConnection(params, name, password));
  }

  public static Object decode(Hashtable h) {
    SoapConnectionFactory ret = new SoapConnectionFactory();
    FactoryParameters params = 
      new FactoryParameters((String) h.get("host"),
                            ((Integer) h.get("port")).intValue());
    params.connectingTimer = ((Integer) h.get("connectingTimer")).intValue();
    params.txPendingTimer = ((Integer) h.get("txPendingTimer")).intValue();
    params.soapCnxPendingTimer = ((Integer) h.get("cnxPendingTimer")).intValue();
    ret.setParameters(params);
    ret.setId(ret.getClass().getName() + ":" + 
              params.getHost()+ ":" + params.getPort());
    ret.addInstanceTable(ret.getId(), ret);
    return ret;
  }
}
