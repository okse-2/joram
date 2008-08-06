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

import com.scalagent.kjoram.jms.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Vector;

/**
 * A <code>SoapDriver</code> gets server deliveries through RCP SOAP calls.
 */
class SoapDriver extends com.scalagent.kjoram.Driver {

  String serviceUrl = null;
  int cnxId = -1;
  HttpConnection httpConnection = null;
  String name = null;

  /**
   * Constructs a <code>SoapDriver</code> daemon.
   *
   * @param cnx  The connection the driver belongs to.
   */
  SoapDriver(com.scalagent.kjoram.Connection cnx,
             String serviceUrl,
             int cnxId) {
    super(cnx);
    this.serviceUrl = serviceUrl;
    this.cnxId = cnxId;
    httpConnection = new HttpConnection(serviceUrl);
    name = cnx.getUserName();
  }


  /**
   * Returns an <code>AbstractJmsReply</code> delivered by the server.
   *
   * @exception IOException  If the SOAP call failed, or if the SOAP service
   *              is unable to process the call, or if the driver closes.
   */
  protected AbstractJmsReply getDelivery() throws IOException {
    AbstractJmsReply reply = null;

    try {
      reply = httpConnection.call(new GetReply(name,cnxId),name,cnxId);

      if (reply == null) return null;
    } catch (Exception exc) {
      throw new IOException("The SOAP call failed: " + exc.getMessage());
    }

    if (reply instanceof CnxCloseReply) {
      throw new IOException("Driver is closing.");
    } else if (reply instanceof AbstractJmsReply) {
      return (AbstractJmsReply) reply;
    } else {
      throw new IOException("The SOAP service failed to process the call: "
                            + reply);
    }
  }

  /** Shuts down the driver. */
  public void shutdown() {
  }
}
