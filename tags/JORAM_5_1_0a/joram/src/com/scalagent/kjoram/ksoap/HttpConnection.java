/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * The present code contributor is ScalAgent Distributed Technologies.
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.kjoram.ksoap;

import java.io.IOException;
import java.io.InterruptedIOException;

import com.scalagent.kjoram.excepts.*;
import com.scalagent.kjoram.jms.*;
import com.scalagent.kjoram.JoramTracing;

import com.scalagent.ksoap.*;

/**
 * A <code>HttpConnection</code> class allows to send AbstractJmsRequest
 * and receive AbstractJmsReply
 */
public class HttpConnection {

  /** The http Connection. */
  protected HttpTransport httpConnect = null;
  /** Server's address. */
  protected String serverUrl;
  /** to compress data. */
  protected boolean compress = false;


  public HttpConnection(String serverUrl) {
    this.serverUrl = serverUrl;

    // Create a httpConnection
    httpConnect = new HttpTransport(serverUrl,"ProxyService");
  }

  /**
   * sends a request to the server.
   *
   * @param request  The request to send.
   * @return The server reply.
   */
  public AbstractJmsReply call(AbstractJmsRequest request,String name, int cnxId) throws Exception {
      SoapObject sO;
      Object result = null;

      try {
        httpConnect.reset();

        if (JoramTracing.dbgClient)
          JoramTracing.log(JoramTracing.DEBUG, 
                           "HttpConnection.call(" + request + 
                           "," +  cnxId + ")");

        // Transform JmsRequest in a SoapObject
        sO = ConversionSoapHelper.getSoapObject(request,name,cnxId);

        // Send the request and wait the reply
        int timer=10;
        while (true) {
          try {
            result = httpConnect.call(sO);

            if (JoramTracing.dbgClient)
              JoramTracing.log(JoramTracing.DEBUG,
                               "HttpConnection.call result = " + result);
            break;
          } catch (InterruptedIOException iIOE) {
            httpConnect.reset();
            continue;
          } catch (IOException ioE) {
            ioE.printStackTrace();

            //retry to connect to the proxy if it's not a CnxConnectRequest
            if (!(request instanceof CnxConnectRequest)) {
              if (JoramTracing.dbgClient)
                JoramTracing.log(JoramTracing.DEBUG,
                                 "timer=" + timer);
              timer++;
              Thread.sleep(timer*1000);
              //break;
            } else
              throw ioE;
          }
        }
      }
      // Catching an exception because of...
      catch (Exception e) {
        //e.printStackTrace();
        // ... a broken connection:
        if (e instanceof IOException)
          throw e;
        // ... an interrupted exchange:
        else if (e instanceof InterruptedException)
          throw e;
      }

      // Transform SoapObject in a JmsReply
      AbstractJmsReply reply = (AbstractJmsReply) ConversionSoapHelper.getObject((SoapObject)result);
      if (JoramTracing.dbgClient) {
        JoramTracing.log(JoramTracing.DEBUG,
                         "HttpConnection.call : reply=" + reply);
        JoramTracing.log(JoramTracing.DEBUG,
                         "  cnxId=" + cnxId);
        if (reply != null)
          JoramTracing.log(JoramTracing.DEBUG,
                           "  correlationId=" +reply.getCorrelationId());
        JoramTracing.log(JoramTracing.DEBUG,
                         "  request=" + request);
      }

      // Finally, returning the reply:
      return reply;
    }

}
