/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
package com.scalagent.kjndi.ksoap;

import java.util.Vector;
import java.util.Hashtable;

public class SoapNamingContext {
  /** SOAP service's URL. */
  private String serviceUrl = null;

  HttpConnection httpConnection = null;

  public SoapNamingContext(String soapHost, int soapPort)
         throws Exception {

    // Building the service URL:
    serviceUrl = "http://" + soapHost + ":" + soapPort
      + "/soap/servlet/rpcrouter";

    httpConnection = new HttpConnection(serviceUrl);
  } 

  public void bind(String name, Object obj) throws Exception {
    httpConnection.call("bind",name,obj);
  }

  public void rebind(String name, Object obj) throws Exception {
    httpConnection.call("rebind",name,obj);
  }

  public Object lookup(String name) throws Exception {
    return httpConnection.call("lookup",name,null);
  }

  public void unbind(String name) throws Exception {
    httpConnection.call("unbind",name,null);
  }
}
